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
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var dbHelper: DBHelper

    // 권한 요청 런처 (현재 위치 및 사진 위치 접근)
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            enableMyLocation()
            loadMarkersFromPhotoGPS() // 권한 획득 후 다시 마커 불러오기 시도
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
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (::mMap.isInitialized) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        }
    }

    private fun loadMarkersFromPhotoGPS() {
        mMap.clear()
        val records = dbHelper.getAllRecords()
        val boundsBuilder = LatLngBounds.Builder()
        var hasValidGPS = false

        for (record in records) {
            val uriString = record.photoUri
            if (!uriString.isNullOrEmpty()) {
                val latLng = extractGPSFromUri(uriString)
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

    private fun extractGPSFromUri(uriString: String): LatLng? {
        try {
            val uri = Uri.parse(uriString)
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val exif = ExifInterface(inputStream)
                val latLong = exif.latLong
                inputStream.close()

                if (latLong != null && latLong.size == 2) {
                    return LatLng(latLong[0], latLong[1])
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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

    private fun searchLocation(keyword: String) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.KOREA)
            val results = geocoder.getFromLocationName(keyword, 1)

            if (!results.isNullOrEmpty()) {
                val address = results[0]
                val latLng = LatLng(address.latitude, address.longitude)
                moveToLocation(latLng, keyword)
            } else {
                Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "네트워크 오류 또는 지원되지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveToLocation(latLng: LatLng, title: String) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
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