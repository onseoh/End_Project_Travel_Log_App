package com.example.end_project.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.end_project.databinding.ItemRecordBinding
import com.example.end_project.model.TravelRecord

class TravelListAdapter(
    private var recordList: List<TravelRecord>,
    private val onItemClick: (TravelRecord) -> Unit,
    private val onItemLongClick: (TravelRecord, View) -> Unit // View 파라미터 추가
) : RecyclerView.Adapter<TravelListAdapter.ViewHolder>() {

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

            // 클릭 이벤트 (View 추가 전달)
            binding.root.setOnClickListener { onItemClick(record) }
            binding.root.setOnLongClickListener {
                onItemLongClick(record, binding.root)
                true
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