package com.example.application.fragments

import com.example.application.databases.DiaryDatabaseHelper
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.application.R
import com.example.application.activities.MainActivity
import com.example.application.structs.DiaryEntry
import com.example.application.structs.Mood
import java.text.SimpleDateFormat
import java.util.Locale

class WrittenTravelLogFragment : Fragment() {
    private lateinit var containerLayout: LinearLayout
    private lateinit var writtenDiaryReloadBtn: ImageView
    private lateinit var writtenDiaryPlusSearchBtn: ImageView
    private lateinit var searchView: SearchView

    private lateinit var diaryDatabaseHelper: DiaryDatabaseHelper
    private lateinit var mainActivity: MainActivity
    private lateinit var travelLogFragment: TravelLogFragment
    private lateinit var installDate: String
    
    private var PlusSearch: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_written_travel_log, container, false)

        diaryDatabaseHelper = DiaryDatabaseHelper(mainActivity)
        containerLayout = view.findViewById(R.id.writtenDiarysLayout)
        writtenDiaryReloadBtn = view.findViewById(R.id.writtenDiaryReloadBtn)
        writtenDiaryPlusSearchBtn = view.findViewById(R.id.writtenDiaryPlusSearchBtn)
        searchView = view.findViewById(R.id.search_view)
        installDate = mainActivity.getInstallDateAsCalendar()?.let {
            SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(it.time)
        } ?: "설치일이 없음"
        travelLogFragment = mainActivity.travelLogFragment

        displayDiaryEntries()

        writtenDiaryReloadBtn.setOnClickListener {
            displayDiaryEntries()
            mainActivity.showToast("모든 일기를 다시 불러왔습니다!")
        }
        
        writtenDiaryPlusSearchBtn.setOnClickListener { 
            if (PlusSearch) {
                PlusSearch = false
                mainActivity.showToast("내용 포함 검색 모드를 종료합니다.")
                searchView.queryHint = "제목을 입력하세요.."
                filterDiaryEntries(searchView.query.toString())
            } else {
                PlusSearch = true
                mainActivity.showToast("검색 시 내용도 포함해 검색합니다.")
                searchView.queryHint = "제목이나 내용을 입력하세요.."
                filterDiaryEntries(searchView.query.toString())
            }
        }

        searchView.isIconified = false

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterDiaryEntries(newText)
                return true
            }
        })

        return view
    }

    private fun filterDiaryEntries(query: String?) {
        val allDiaryEntries = diaryDatabaseHelper.getAllDiaryEntries()
        val filteredEntries = if (!query.isNullOrEmpty()) {
            if (PlusSearch) {
                allDiaryEntries.filter { entry ->
                    entry.title?.contains(query, ignoreCase = true) == true
                    entry.comment?.contains(query, ignoreCase = true) == true
                }
            } else {
                allDiaryEntries.filter { entry ->
                    entry.title?.contains(query, ignoreCase = true) == true
                }
            }
        } else {
            allDiaryEntries
        }
        displayDiaryEntries(filteredEntries)
    }

    private fun displayDiaryEntries() {
        containerLayout.removeAllViews()

        val allDiaryEntries = diaryDatabaseHelper.getAllDiaryEntries()

        // 다운로드일 표시
        val installDateView = LayoutInflater.from(requireContext()).inflate(R.layout.item_travel_log, null)

        val dateTextView = installDateView.findViewById<TextView>(R.id.date_text_view)
        val iconImageView = installDateView.findViewById<ImageView>(R.id.icon_image_view)

        dateTextView.text = "멍냥고리즘 설치일  -  $installDate"

        // 아이콘 이미지 설정
        iconImageView.setImageResource(R.drawable.icon_install)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(4.dpToPx(), 0, 4.dpToPx(), 4.dpToPx()) // 좌우 마진 설정 (4dp)
        }

        installDateView.layoutParams = params

        // 레이아웃에 추가
        containerLayout.addView(installDateView)

        for (entry in allDiaryEntries) {
            val travelLogView = LayoutInflater.from(requireContext()).inflate(R.layout.item_travel_log, null)

            val dateTextView = travelLogView.findViewById<TextView>(R.id.date_text_view)
            val iconImageView = travelLogView.findViewById<ImageView>(R.id.icon_image_view)

            // 날짜 설정
            val originalDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val desiredDateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
            val parsedDate = originalDateFormat.parse(entry.date)
            val formattedDate = desiredDateFormat.format(parsedDate)

            dateTextView.text = "$formattedDate  -  ${entry.title ?: "제목 없음"}"


            // 아이콘 이미지 설정
            iconImageView.setImageResource(R.drawable.icon_travel_log)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4.dpToPx(), 0, 4.dpToPx(), 4.dpToPx()) // 좌우 마진 설정 (4dp)
            }

            travelLogView.layoutParams = params

            travelLogView.setOnClickListener {
                openDiaryViewDialog(diaryDatabaseHelper.getDiaryEntryByDate(entry.date))
            }

            // 레이아웃에 추가
            containerLayout.addView(travelLogView)
        }
    }

    private fun displayDiaryEntries(entries: List<DiaryEntry> = diaryDatabaseHelper.getAllDiaryEntries()) {
        containerLayout.removeAllViews()

        // 다운로드일 표시
        val installDateView = LayoutInflater.from(requireContext()).inflate(R.layout.item_travel_log, null)

        val dateTextView = installDateView.findViewById<TextView>(R.id.date_text_view)
        val iconImageView = installDateView.findViewById<ImageView>(R.id.icon_image_view)

        dateTextView.text = "멍냥고리즘 설치일  -  $installDate"

        // 아이콘 이미지 설정
        iconImageView.setImageResource(R.drawable.icon_install)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(4.dpToPx(), 0, 4.dpToPx(), 4.dpToPx()) // 좌우 마진 설정 (4dp)
        }

        installDateView.layoutParams = params

        // 레이아웃에 추가
        containerLayout.addView(installDateView)

        for (entry in entries) {
            val travelLogView = LayoutInflater.from(requireContext()).inflate(R.layout.item_travel_log, null)

            val dateTextView = travelLogView.findViewById<TextView>(R.id.date_text_view)
            val iconImageView = travelLogView.findViewById<ImageView>(R.id.icon_image_view)

            // 날짜 설정
            val originalDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val desiredDateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
            val parsedDate = originalDateFormat.parse(entry.date)
            val formattedDate = desiredDateFormat.format(parsedDate)

            dateTextView.text = "$formattedDate  -  ${entry.title ?: "제목 없음"}"

            // 아이콘 이미지 설정
            iconImageView.setImageResource(R.drawable.icon_travel_log)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4.dpToPx(), 0, 4.dpToPx(), 4.dpToPx()) // 좌우 마진 설정 (4dp)
            }

            travelLogView.layoutParams = params

            travelLogView.setOnClickListener {
                openDiaryViewDialog(diaryDatabaseHelper.getDiaryEntryByDate(entry.date))
            }

            // 레이아웃에 추가
            containerLayout.addView(travelLogView)
        }
    }

    // dp를 px로 변환 함수
    private fun Int.dpToPx(): Int {
        val scale = requireContext().resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun openDiaryViewDialog(de: DiaryEntry?) {
        if (de != null) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_diary_view, null)

            // 기분 표시
            val diaryMoodView = dialogView.findViewById<ImageView>(R.id.WdiaryMoodView)
            diaryMoodView.setImageResource(
                if (de.mood == Mood.BAD) R.drawable.icon_sad else R.drawable.icon_happy
            )

            // 날짜 표시
            val diaryDateView = dialogView.findViewById<TextView>(R.id.WdiaryDateView)
            val dateParts = de.date.split("-")
            val dayOfWeek = de.getFormattedDate()
                ?.let { SimpleDateFormat("EEEE", Locale.getDefault()).format(it) }
            diaryDateView.text = dateParts[1] + "월  " +
                    dateParts[2] + "일  " +
                    dayOfWeek

            // 제목 표시
            val diaryTitleView = dialogView.findViewById<TextView>(R.id.WdiaryTitleView)
            if (de.title != null) {
                var content = SpannableString("제목: " + de.title)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                diaryTitleView.text = content
            } else {
                var content = SpannableString("제목: ")
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                diaryTitleView.text = content
            }

            // 다이어리에 이미지가 없을 시 선 제거
            val diaryLine = dialogView.findViewById<View>(R.id.WdiaryLine)
            diaryLine.visibility = if (de.comment.isNullOrEmpty() && de.imageUriList.isNullOrEmpty()) View.GONE else View.VISIBLE

            // 다이어리 내용 표시
            val diaryCommentView = dialogView.findViewById<TextView>(R.id.WdiaryCommentView)
            if (de.comment.isNullOrEmpty()) {
                diaryCommentView.text = ""
            } else {
                diaryCommentView.text = " " + de.comment
            }

            // 다이어리 이미지 표시
            val diaryImagesView = dialogView.findViewById<LinearLayout>(R.id.WdiaryImagesView)
            diaryImagesView.removeAllViews()
            displayImages(mainActivity, de.imageUriList, diaryImagesView, dialogView)

            val dlg = AlertDialog.Builder(mainActivity)
                .setTitle("일기 표시")
                .setView(dialogView)
                .setPositiveButton("닫기") { dialog, _ ->
                }
                .create()
            dlg.show()
        }
    }

    fun displayImages(context: Context, imageUriList: List<String>?, container: LinearLayout, dialog: View) {
        val diaryImagesScrollView = dialog.findViewById<HorizontalScrollView>(R.id.WdiaryImagesScrollView)
        if (imageUriList.isNullOrEmpty()) {
            diaryImagesScrollView.visibility = View.GONE
        }
        diaryImagesScrollView.visibility = View.VISIBLE

        // 각 이미지 URI에 대해 ImageView 동적 생성 및 추가
        imageUriList!!.forEach { uriString ->
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    // width, height 설정
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt(),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt()
                ).apply {
                    // 이미지 간에 좌우 Padding 설정
                    setPadding(0, 5, 0, 5)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Glide를 사용하여 이미지 로드
            Glide.with(context)
                .load(Uri.parse(uriString))
                .into(imageView)

            // container 레이아웃에 다이어리 이미지들 추가
            container.addView(imageView)
        }
    }
}