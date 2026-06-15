package com.example.end_project.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.end_project.model.TravelRecord

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "TravelLog.db" // 생성될 DB 파일 이름
        private const val TABLE_NAME = "travel_records"

        // 테이블 컬럼명 정의
        private const val COLUMN_NO = "no"
        private const val COLUMN_PLACE = "place"
        private const val COLUMN_VISIT_DATE = "visit_date"
        private const val COLUMN_MEMO = "memo"
        private const val COLUMN_PHOTO_URI = "photo_uri"
    }

    // 앱 설치 후 최초로 DB가 생성될 때 호출됨 (테이블 생성)
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_NO INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_PLACE TEXT, "
                + "$COLUMN_VISIT_DATE TEXT, "
                + "$COLUMN_MEMO TEXT, "
                + "$COLUMN_PHOTO_URI TEXT)")
        db.execSQL(createTable)
    }

    // DB 버전이 올라갈 때 호출됨 (기존 테이블 삭제 후 재생성)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // ---------------------------------------------------------
    // 필수 구현: CRUD (Create, Read, Update, Delete) 기능
    // ---------------------------------------------------------

    // [Create] 새로운 여행 기록 추가
    fun insertRecord(record: TravelRecord): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PLACE, record.place)
            put(COLUMN_VISIT_DATE, record.visitDate)
            put(COLUMN_MEMO, record.memo)
            put(COLUMN_PHOTO_URI, record.photoUri)
        }
        // db.close() 제거: SQLiteOpenHelper가 커넥션을 내부적으로 관리하므로
        // 매번 close()하면 동시 접근 시 "already-closed" 오류 발생
        return db.insert(TABLE_NAME, null, values)
    }

    // [Read] 모든 여행 기록 방문 날짜 최신순으로 조회 (RecyclerView 목록 표시용)
    // 지침: "기록은 날짜를 기준으로 정렬" → ORDER BY visit_date DESC
    fun getAllRecords(): List<TravelRecord> {
        val recordList = ArrayList<TravelRecord>()
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_VISIT_DATE DESC"
        val db = this.readableDatabase

        // use{} 블록: cursor를 항상 자동으로 close() → 누수 방지
        db.rawQuery(selectQuery, null).use { cursor ->
            while (cursor.moveToNext()) {
                recordList.add(
                    TravelRecord(
                        no = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NO)),
                        place = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLACE)),
                        visitDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VISIT_DATE)),
                        memo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO)),
                        photoUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_URI))
                    )
                )
            }
        }
        return recordList
    }

    // [Update] 기존 여행 기록 수정
    fun updateRecord(record: TravelRecord): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PLACE, record.place)
            put(COLUMN_VISIT_DATE, record.visitDate)
            put(COLUMN_MEMO, record.memo)
            put(COLUMN_PHOTO_URI, record.photoUri)
        }
        // db.close() 제거
        return db.update(TABLE_NAME, values, "$COLUMN_NO=?", arrayOf(record.no.toString()))
    }

    // [Delete] 여행 기록 삭제
    fun deleteRecord(no: Int): Int {
        val db = this.writableDatabase
        // db.close() 제거
        return db.delete(TABLE_NAME, "$COLUMN_NO=?", arrayOf(no.toString()))
    }
}