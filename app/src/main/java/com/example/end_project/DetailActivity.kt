package com.example.end_project

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.end_project.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ListFragment 에서 Intent 로 넘겨준 데이터 받기
        val place = intent.getStringExtra("PLACE")
        val date = intent.getStringExtra("DATE")
        val memo = intent.getStringExtra("MEMO")
        val photoUri = intent.getStringExtra("PHOTO_URI")

        // 뷰에 데이터 세팅
        binding.tvDetailPlace.text = place
        binding.tvDetailDate.text = date
        binding.tvDetailMemo.text = memo

        // 사진이 있으면 보여주고, 없으면 이미지 영역 숨기기
        if (!photoUri.isNullOrEmpty()) {
            binding.ivDetailPhoto.setImageURI(Uri.parse(photoUri))
            binding.ivDetailPhoto.visibility = View.VISIBLE
        } else {
            binding.ivDetailPhoto.visibility = View.GONE
        }
    }
}