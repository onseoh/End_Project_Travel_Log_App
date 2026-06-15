package com.example.end_project

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.end_project.databinding.ActivityAddEditBinding
import com.example.end_project.db.DBHelper
import com.example.end_project.model.TravelRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var dbHelper: DBHelper

    private var selectedPhotoUri: String? = null
    private var recordId: Int = -1

    // 카메라 촬영 시 임시 저장할 Uri (FileProvider 경유)
    private var cameraImageUri: Uri? = null

    // 갤러리 선택 런처
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedPhotoUri = uri.toString()
                binding.ivSelectedPhoto.setImageURI(uri)
            }
        }

    // 지침: "카메라/갤러리 선택" 필수 구현 — 카메라 촬영 런처 추가
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {
                selectedPhotoUri = cameraImageUri.toString()
                binding.ivSelectedPhoto.setImageURI(cameraImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)
        checkEditMode()

        // 사진 선택 버튼: 카메라 / 갤러리 선택 다이얼로그
        binding.btnSelectPhoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("사진 선택")
                .setItems(arrayOf("카메라로 촬영", "갤러리에서 선택")) { _, which ->
                    when (which) {
                        0 -> launchCamera()
                        1 -> pickImageLauncher.launch(arrayOf("image/*"))
                    }
                }
                .show()
        }

        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    // FileProvider를 통해 카메라 앱에 임시 파일 Uri를 전달
    // 직접 file:// Uri를 넘기면 FileUriExposedException 발생 → FileProvider 필수
    private fun launchCamera() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File.createTempFile("PHOTO_${timeStamp}_", ".jpg", cacheDir)
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun checkEditMode() {
        recordId = intent.getIntExtra("RECORD_ID", -1)

        if (recordId != -1) {
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
            val newRecord = TravelRecord(
                place = place, visitDate = date, memo = memo, photoUri = selectedPhotoUri
            )
            if (dbHelper.insertRecord(newRecord) != -1L) {
                Toast.makeText(this, "저장되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            val updatedRecord = TravelRecord(
                no = recordId, place = place, visitDate = date, memo = memo, photoUri = selectedPhotoUri
            )
            if (dbHelper.updateRecord(updatedRecord) > 0) {
                Toast.makeText(this, "수정되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
