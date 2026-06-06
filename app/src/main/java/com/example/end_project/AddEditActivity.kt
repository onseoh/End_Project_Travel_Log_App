package com.example.end_project

import android.content.Intent
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
    private var selectedPhotoUri: String? = null // 선택된 사진의 URI를 저장할 변수

    // 갤러리에서 이미지를 선택해오는 런처(도구)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            // 앱을 껐다 켜도 사진 권한을 유지하기 위한 설정
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            selectedPhotoUri = uri.toString()
            binding.ivSelectedPhoto.setImageURI(uri) // 화면에 선택된 사진 보여주기
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)

        // 1. 사진 선택 버튼 클릭
        binding.btnSelectPhoto.setOnClickListener {
            // 이미지 파일만 선택할 수 있도록 갤러리 열기
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        // 2. 저장하기 버튼 클릭
        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun saveRecord() {
        val place = binding.etPlace.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val memo = binding.etMemo.text.toString().trim()

        // 입력값 검사 (빈칸 방지)
        if (place.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "여행지와 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // DB에 넣을 데이터 객체 생성
        val newRecord = TravelRecord(
            place = place,
            visitDate = date,
            memo = memo,
            photoUri = selectedPhotoUri
        )

        // DB에 데이터 삽입 (3단계에서 만든 DBHelper 사용!)
        val result = dbHelper.insertRecord(newRecord)

        if (result != -1L) {
            Toast.makeText(this, "저장되었습니다!", Toast.LENGTH_SHORT).show()
            finish() // 액티비티 종료 (목록 화면으로 돌아감)
        } else {
            Toast.makeText(this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}