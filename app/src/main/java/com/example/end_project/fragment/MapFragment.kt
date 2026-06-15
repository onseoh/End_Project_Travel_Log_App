package com.example.end_project.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.end_project.R
import com.example.end_project.databinding.FragmentMapBinding
import com.example.end_project.db.DBHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var dbHelper: DBHelper

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            enableMyLocation()
            loadMarkersFromPhotoGPS()
        } else {
            Toast.makeText(requireContext(), "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())
        setupOptionsMenu()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        checkPermissionsAndEnableLocation()
        loadMarkersFromPhotoGPS()
    }

    private fun checkPermissionsAndEnableLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_MEDIA_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (::mMap.isInitialized) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        }
    }

    // DB 접근과 EXIF 파싱 모두 IO 스레드에서 수행
    // 메인 스레드에서 호출하면 UI 지연(ANR) 발생 위험
    private fun loadMarkersFromPhotoGPS() {
        lifecycleScope.launch {
            // DB 읽기: IO 스레드
            val records = withContext(Dispatchers.IO) { dbHelper.getAllRecords() }

            mMap.clear()
            val boundsBuilder = LatLngBounds.Builder()
            var hasValidGPS = false

            for (record in records) {
                if (!record.photoUri.isNullOrEmpty()) {
                    // EXIF 파싱(파일 I/O): IO 스레드
                    val latLng = withContext(Dispatchers.IO) {
                        extractGPSFromUri(record.photoUri!!)
                    }
                    if (latLng != null) {
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(record.place)
                                .snippet(record.visitDate)
                        )
                        boundsBuilder.include(latLng)
                        hasValidGPS = true
                    }
                }
            }

            // UI 업데이트는 메인 스레드 (launch 블록 내부이므로 Main)
            if (hasValidGPS) {
                view?.post {
                    val bounds = boundsBuilder.build()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            } else {
                val schUniv = LatLng(36.7690, 126.9314)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(schUniv, 16f))
            }
        }
    }

    // use{} 블록: 예외 발생 여부와 관계없이 inputStream이 항상 close() 됨
    // 기존 코드는 try 내부에서만 close() → 예외 시 스트림 누수 위험
    private fun extractGPSFromUri(uriString: String): LatLng? {
        return try {
            val uri = Uri.parse(uriString)
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                exif.latLong?.let { latLong -> LatLng(latLong[0], latLong[1]) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showSearchDialog() {
        val editText = EditText(requireContext())
        editText.hint = "주소 또는 장소명 입력 (예: 서울역)"

        AlertDialog.Builder(requireContext())
            .setTitle("위치 검색")
            .setView(editText)
            .setPositiveButton("검색") { _, _ ->
                val keyword = editText.text.toString()
                if (keyword.isNotEmpty()) {
                    searchLocation(keyword)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // Geocoder.getFromLocationName()은 네트워크 I/O 수행
    // 메인 스레드 호출 시 NetworkOnMainThreadException 발생 위험 → IO 스레드로 이동
    private fun searchLocation(keyword: String) {
        lifecycleScope.launch {
            val latLng = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale.KOREA)
                    @Suppress("DEPRECATION")
                    val results = geocoder.getFromLocationName(keyword, 1)
                    if (!results.isNullOrEmpty()) {
                        LatLng(results[0].latitude, results[0].longitude)
                    } else null
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            // UI 업데이트: 코루틴 재개 후 Main 스레드에서 실행
            if (latLng != null) {
                moveToLocation(latLng, keyword)
            } else {
                Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveToLocation(latLng: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().position(latLng).title(title))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    private fun setupOptionsMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (!::mMap.isInitialized) return false

                return when (menuItem.itemId) {
                    R.id.menu_normal_map -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                        true
                    }
                    R.id.menu_satellite_map -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        true
                    }
                    R.id.menu_search_map -> {
                        showSearchDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
