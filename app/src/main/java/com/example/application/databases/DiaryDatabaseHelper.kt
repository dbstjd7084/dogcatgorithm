package com.example.application.databases
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.example.application.structs.DiaryEntry
import com.example.application.structs.Mood
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DiaryDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "DiaryDatabase.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_DIARY = "diary"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_MOOD = "mood"
        private const val COLUMN_TITLE = "title" // title 추가
        private const val COLUMN_COMMENT = "comment"
        private const val COLUMN_IMAGE_URI_LIST = "imageUriList"
        private const val IMAGE_URI_SEPARATOR = ","
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createDiaryTable = """
            CREATE TABLE $TABLE_DIARY (
                $COLUMN_DATE TEXT PRIMARY KEY,
                $COLUMN_MOOD TEXT NOT NULL,
                $COLUMN_TITLE TEXT,
                $COLUMN_COMMENT TEXT,
                $COLUMN_IMAGE_URI_LIST TEXT
            )
        """.trimIndent()
        db?.execSQL(createDiaryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DIARY")
        onCreate(db)
    }

    // 누락된 날짜의 다이어리 생성
    fun fillMissedDays(installDate: String, today: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.parse(installDate) ?: return
        val endDate = dateFormat.parse(today) ?: return

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (calendar.time.before(endDate)) {
            val date = dateFormat.format(calendar.time)
            if (getDiaryEntryByDate(date) == null) {
                saveDiaryEntry(DiaryEntry(date, Mood.BAD))
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 마지막 날 체크하기
        val finalDate = dateFormat.format(endDate)
        if (getDiaryEntryByDate(finalDate) == null) {
            saveDiaryEntry(DiaryEntry(finalDate, Mood.BAD))
        }
    }

    // 다이어리 저장 함수
    fun saveDiaryEntry(entry: DiaryEntry) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DATE, entry.date)
            put(COLUMN_MOOD, entry.mood.name)
            put(COLUMN_TITLE, entry.title) // title 저장
            put(COLUMN_COMMENT, entry.comment)
            put(COLUMN_IMAGE_URI_LIST, entry.imageUriList?.joinToString(IMAGE_URI_SEPARATOR))
        }
        db.insertWithOnConflict(TABLE_DIARY, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    // 특정 년월의 다이어리 항목들을 불러오는 함수
    fun getDiaryEntriesByMonth(year: Int, month: Int): List<DiaryEntry> {
        val db = readableDatabase
        val diaryEntries = mutableListOf<DiaryEntry>()

        // 년월을 "YYYY-MM" 형식으로 맞춤
        val monthString = String.format("%04d-%02d", year, month)

        val cursor = db.query(
            TABLE_DIARY,
            arrayOf(COLUMN_DATE, COLUMN_MOOD, COLUMN_TITLE, COLUMN_COMMENT, COLUMN_IMAGE_URI_LIST), // title 포함
            "$COLUMN_DATE LIKE ?",
            arrayOf("$monthString-%"),
            null, null, "$COLUMN_DATE ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val mood = Mood.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD)))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)) // title 가져오기
                val comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT))
                val imageUriListString = cursor.getString(cursor.getColumnIndexOrThrow(
                    COLUMN_IMAGE_URI_LIST
                ))
                val imageUriList = imageUriListString?.split(IMAGE_URI_SEPARATOR)?.filter { it.isNotEmpty() } ?: emptyList()

                diaryEntries.add(DiaryEntry(date, mood, title, comment, imageUriList)) // title 포함
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return diaryEntries
    }

    // 특정 날짜의 다이어리 조회 함수
    fun getDiaryEntryByDate(date: String): DiaryEntry? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DIARY,
            arrayOf(COLUMN_DATE, COLUMN_MOOD, COLUMN_TITLE, COLUMN_COMMENT, COLUMN_IMAGE_URI_LIST), // title 포함
            "$COLUMN_DATE = ?",
            arrayOf(date),
            null, null, null
        )

        var diaryEntry: DiaryEntry? = null
        if (cursor.moveToFirst()) {
            val mood = Mood.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD)))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)) // title 가져오기
            val comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT))
            val imageUriListString = cursor.getString(cursor.getColumnIndexOrThrow(
                COLUMN_IMAGE_URI_LIST
            ))
            val imageUriList = imageUriListString?.split(IMAGE_URI_SEPARATOR)?.filter { it.isNotEmpty() } ?: emptyList()

            diaryEntry = DiaryEntry(date, mood, title, comment, imageUriList) // title 포함
        }
        cursor.close()
        db.close()
        return diaryEntry
    }

    fun deleteDiaryEntryByDate(date: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_DIARY, "$COLUMN_DATE = ?", arrayOf(date))
        db.close()
        return result > 0 // 삭제 성공 여부 반환
    }

    fun updateDiaryEntry(entry: DiaryEntry): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DATE, entry.date)
            put(COLUMN_MOOD, entry.mood.name)
            put(COLUMN_TITLE, entry.title)
            put(COLUMN_COMMENT, entry.comment)
            put(COLUMN_IMAGE_URI_LIST, entry.imageUriList?.joinToString(IMAGE_URI_SEPARATOR))
        }
        val result = db.update(TABLE_DIARY, contentValues, "$COLUMN_DATE = ?", arrayOf(entry.date))
        db.close()
        return result > 0 // 업데이트 성공 여부 반환
    }

    // 특정 날짜의 다이어리 이미지 항목 중 특정 이미지 URI 제거
    fun removeImageUri(date: String, uriToRemove: String): Boolean {
        // 현재 다이어리 항목을 가져옴
        val currentEntry = getDiaryEntryByDate(date) ?: return false

        // imageUriList가 null이 아닐 경우에만 필터링
        val updatedImageUriList = currentEntry.imageUriList?.filterNot { it == uriToRemove } ?: emptyList()

        // 파일 삭제
        try {
            // `file:///data/user/0/...` 형식을 `file:///data/data/...`로 변환
            val correctedUriToRemove = uriToRemove.replace("/data/user/0/", "/data/data/")

            val fileToRemove = File(Uri.parse(correctedUriToRemove).path!!)
            if (fileToRemove.exists()) {
                fileToRemove.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val db = writableDatabase
        // 변경된 항목으로 데이터베이스 업데이트
        val contentValues = ContentValues().apply {
            put(COLUMN_MOOD, currentEntry.mood.name)
            put(COLUMN_TITLE, currentEntry.title)
            put(COLUMN_COMMENT, currentEntry.comment)
            // updatedImageUriList가 비어있을 경우 null 또는 빈 문자열 저장
            put(
                COLUMN_IMAGE_URI_LIST, if (updatedImageUriList.isEmpty()) null else updatedImageUriList.joinToString(
                    IMAGE_URI_SEPARATOR
                ))
        }

        val result = db.update(TABLE_DIARY, contentValues, "$COLUMN_DATE = ?", arrayOf(date))
        db.close()

        return result > 0 // 업데이트 성공 여부 반환
    }

    // 모든 다이어리 가져오기
    fun getAllDiaryEntries(): List<DiaryEntry> {
        val db = readableDatabase
        val diaryEntries = mutableListOf<DiaryEntry>()

        val cursor = db.query(
            TABLE_DIARY,
            arrayOf(COLUMN_DATE, COLUMN_MOOD, COLUMN_TITLE, COLUMN_COMMENT, COLUMN_IMAGE_URI_LIST),
            null, null, null, null, "$COLUMN_DATE ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val mood = Mood.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD)))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT))
                val imageUriListString = cursor.getString(cursor.getColumnIndexOrThrow(
                    COLUMN_IMAGE_URI_LIST
                ))
                val imageUriList = imageUriListString?.split(IMAGE_URI_SEPARATOR)?.filter { it.isNotEmpty() } ?: emptyList()

                diaryEntries.add(DiaryEntry(date, mood, title, comment, imageUriList))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return diaryEntries
    }

}