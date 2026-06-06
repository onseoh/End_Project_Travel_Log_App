package com.example.end_project.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.end_project.databinding.ItemRecordBinding
import com.example.end_project.model.TravelRecord

class TravelListAdapter(
    private var recordList: List<TravelRecord>,
    private val onItemClick: (TravelRecord) -> Unit,
    private val onItemLongClick: (TravelRecord) -> Unit
) : RecyclerView.Adapter<TravelListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: TravelRecord) {
            binding.tvPlace.text = record.place
            binding.tvDate.text = record.visitDate

            // 사진 URI가 있으면 이미지뷰에 띄우고, 없으면 기본 회색 유지
            if (!record.photoUri.isNullOrEmpty()) {
                binding.ivPhoto.setImageURI(Uri.parse(record.photoUri))
            } else {
                binding.ivPhoto.setImageDrawable(null)
                binding.ivPhoto.setBackgroundColor(android.graphics.Color.LTGRAY)
            }

            // 짧게 클릭 (상세/수정 화면 이동)
            binding.root.setOnClickListener { onItemClick(record) }

            // 길게 클릭 (컨텍스트 메뉴 - 삭제/수정)
            binding.root.setOnLongClickListener {
                onItemLongClick(record)
                true // 이벤트 소비
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recordList[position])
    }

    override fun getItemCount(): Int = recordList.size

    // DB 데이터가 바뀌었을 때 리스트를 새로고침하는 함수
    fun updateData(newData: List<TravelRecord>) {
        recordList = newData
        notifyDataSetChanged()
    }
}