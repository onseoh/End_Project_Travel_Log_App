package com.example.end_project.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.end_project.databinding.ItemRecordBinding
import com.example.end_project.model.TravelRecord

class TravelListAdapter(
    private var recordList: List<TravelRecord>,
    private val onItemClick: (TravelRecord) -> Unit
    // onItemLongClick 람다 제거: registerForContextMenu 방식으로 교체됨
    // 길게 누르면 시스템 Context Menu가 자동으로 뜨도록 return false 처리
) : RecyclerView.Adapter<TravelListAdapter.ViewHolder>() {

    // 마지막으로 길게 누른 항목을 Fragment에서 조회할 수 있도록 저장
    private var longClickedRecord: TravelRecord? = null

    fun getLongClickedRecord(): TravelRecord? = longClickedRecord

    inner class ViewHolder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: TravelRecord) {
            binding.tvPlace.text = record.place
            binding.tvDate.text = record.visitDate

            if (!record.photoUri.isNullOrEmpty()) {
                binding.ivPhoto.setImageURI(Uri.parse(record.photoUri))
            } else {
                binding.ivPhoto.setImageDrawable(null)
                binding.ivPhoto.setBackgroundColor(android.graphics.Color.LTGRAY)
            }

            binding.root.setOnClickListener { onItemClick(record) }

            // false 반환: 이벤트를 소비하지 않고 상위 뷰(RecyclerView)로 전달
            // → registerForContextMenu가 감지해 시스템 Context Menu를 띄움
            binding.root.setOnLongClickListener {
                longClickedRecord = record
                false
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

    fun updateData(newData: List<TravelRecord>) {
        recordList = newData
        notifyDataSetChanged()
    }
}
