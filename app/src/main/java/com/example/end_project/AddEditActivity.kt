package com.example.end_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.end_project.databinding.ActivityAddEditBinding
import com.example.end_project.db.DBHelper
import com.example.end_project.model.TravelRecord

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var dbHelper: DBHelper

    private var selectedPhotoUri: String? = null
    private var recordId: Int = -1 // -1이면 '새로 추가', 0 이상이면 '기존 기록 수정' 모드

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedPhotoUri = uri.toString()
            binding.ivSelectedPhoto.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)

        // 전달받은 데이터가 있는지 확인 (수정 모드인지 판별)
        checkEditMode()

        binding.btnSelectPhoto.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun checkEditMode() {
        recordId = intent.getIntExtra("RECORD_ID", -1)

        if (recordId != -1) { // 수정 모드일 때 기존 데이터 채워넣기
            binding.etPlace.setText(intent.getStringExtra("PLACE"))
            binding.etDate.setText(intent.getStringExtra("DATE"))
            binding.etMemo.setText(intent.getStringExtra("MEMO"))

            val uriStr = intent.getStringExtra("PHOTO_URI")
            if (!uriStr.isNullOrEmpty()) {
                selectedPhotoUri = uriStr
                binding.ivSelectedPhoto.setImageURI(Uri.parse(uriStr))
            }
            binding.btnSave.text = "수정 완료하기"
        }
    }

    private fun saveRecord() {
        val place = binding.etPlace.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val memo = binding.etMemo.text.toString().trim()

        if (place.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "여행지와 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (recordId == -1) {
            // [Create] 새 데이터 추가
            val newRecord = TravelRecord(place = place, visitDate = date, memo = memo, photoUri = selectedPhotoUri)
            if (dbHelper.insertRecord(newRecord) != -1L) {
                Toast.makeText(this, "저장되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            // [Update] 기존 데이터 수정
            val updatedRecord = TravelRecord(no = recordId, place = place, visitDate = date, memo = memo, photoUri = selectedPhotoUri)
            if (dbHelper.updateRecord(updatedRecord) > 0) {
                Toast.makeText(this, "수정되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}