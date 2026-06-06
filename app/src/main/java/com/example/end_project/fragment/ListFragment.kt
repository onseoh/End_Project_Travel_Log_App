package com.example.end_project.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.end_project.AddEditActivity
import com.example.end_project.adapter.TravelListAdapter
import com.example.end_project.databinding.FragmentListBinding
import com.example.end_project.db.DBHelper

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

        // 우측 하단 (+) 버튼 클릭 시 AddEditActivity 로 이동
        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddEditActivity::class.java)
            startActivity(intent)
        }
    }

    // 화면이 사용자에게 보여질 때마다 DB에서 최신 데이터를 불러옴
    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = TravelListAdapter(emptyList(),
            onItemClick = { record ->
                // TODO 4단계: 클릭 시 수정 화면으로 이동하며 데이터 넘기기
            },
            onItemLongClick = { record ->
                // TODO 5단계: 길게 클릭 시 메뉴 띄우기 (삭제/수정)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun loadData() {
        // DBHelper 에 만들어둔 모든 기록 가져오기 함수 호출! (경고가 사라집니다)
        val records = dbHelper.getAllRecords()
        adapter.updateData(records)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}