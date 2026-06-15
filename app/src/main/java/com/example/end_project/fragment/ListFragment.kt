package com.example.end_project.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.end_project.AddEditActivity
import com.example.end_project.DetailActivity
import com.example.end_project.R
import com.example.end_project.adapter.TravelListAdapter
import com.example.end_project.databinding.FragmentListBinding
import com.example.end_project.db.DBHelper
import com.example.end_project.model.TravelRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: TravelListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())
        setupRecyclerView()
        setupOptionsMenu()

        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddEditActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        // onItemLongClick 람다 제거 — registerForContextMenu 방식으로 교체
        adapter = TravelListAdapter(emptyList(),
            onItemClick = { record -> moveToDetailActivity(record) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 지침: "컨텍스트 메뉴 구현 (항목 길게 누르기)"
        // registerForContextMenu: 해당 View를 Context Menu 트리거로 등록
        registerForContextMenu(binding.recyclerView)
    }

    // 지침 필수 구현: 컨텍스트 메뉴 생성
    // RecyclerView 항목을 길게 누르면 시스템이 자동으로 호출
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.context_menu, menu)
        menu.setHeaderTitle("작업 선택")
    }

    // 지침 필수 구현: 컨텍스트 메뉴 항목 선택 처리
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val record = adapter.getLongClickedRecord() ?: return false
        return when (item.itemId) {
            R.id.menu_edit -> {
                moveToEditActivity(record)
                true
            }
            R.id.menu_delete -> {
                // 지침: "컨텍스트 메뉴는 AlertDialog를 반드시 거쳐야 한다"
                showDeleteConfirmDialog(record)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                dbHelper.getAllRecords()
            }
            adapter.updateData(records)
            binding.progressBar.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmDialog(record: TravelRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("기록 삭제")
            .setMessage("'${record.place}' 여행 기록을 정말 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                dbHelper.deleteRecord(record.no)
                Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                loadData()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun moveToDetailActivity(record: TravelRecord) {
        val intent = Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra("PLACE", record.place)
            putExtra("DATE", record.visitDate)
            putExtra("MEMO", record.memo)
            putExtra("PHOTO_URI", record.photoUri)
        }
        startActivity(intent)
    }

    private fun moveToEditActivity(record: TravelRecord) {
        val intent = Intent(requireContext(), AddEditActivity::class.java).apply {
            putExtra("RECORD_ID", record.no)
            putExtra("PLACE", record.place)
            putExtra("DATE", record.visitDate)
            putExtra("MEMO", record.memo)
            putExtra("PHOTO_URI", record.photoUri)
        }
        startActivity(intent)
    }

    private fun setupOptionsMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.options_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_refresh -> {
                        loadData()
                        Toast.makeText(requireContext(), "목록을 새로고침했습니다.", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_info -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("앱 정보")
                            .setMessage("여행 기록 앱 v1.0\n개발자: 이선호\n기말 프로젝트")
                            .setPositiveButton("확인", null)
                            .show()
                        true
                    }
                    R.id.menu_theme -> {
                        showThemeDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showThemeDialog() {
        val themeArray = arrayOf("라이트 모드", "다크 모드", "시스템 기본값")
        AlertDialog.Builder(requireContext())
            .setTitle("테마 선택")
            .setItems(themeArray) { _, which ->
                when (which) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
