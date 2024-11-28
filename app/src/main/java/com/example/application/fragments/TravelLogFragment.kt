package com.example.application.fragments

import com.example.application.databases.DiaryDatabaseHelper
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.application.activities.MainActivity
import com.example.application.R
import com.example.application.decorators.BadDecorator
import com.example.application.decorators.DotDecorator
import com.example.application.decorators.DownloadDecorator
import com.example.application.decorators.HappyDecorator
import com.example.application.decorators.SaturdayDecorator
import com.example.application.decorators.SundayDecorator
import com.example.application.structs.DiaryEntry
import com.example.application.structs.Mood
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TravelLogFragment : Fragment() {
    // 다이어리 데이터베이스
    private lateinit var diaryDatabaseHelper: DiaryDatabaseHelper

    // 날짜 선택 레이아웃 관련
    private lateinit var dateSelectLayout: LinearLayout
    private lateinit var yearTextView: TextView
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private lateinit var prevYearBtn: ImageView
    private lateinit var nextYearBtn: ImageView
    private lateinit var btn_january: LinearLayout
    private lateinit var btn_february: LinearLayout
    private lateinit var btn_march: LinearLayout
    private lateinit var btn_april: LinearLayout
    private lateinit var btn_may: LinearLayout
    private lateinit var btn_june: LinearLayout
    private lateinit var btn_july: LinearLayout
    private lateinit var btn_august: LinearLayout
    private lateinit var btn_september: LinearLayout
    private lateinit var btn_october: LinearLayout
    private lateinit var btn_november: LinearLayout
    private lateinit var btn_december: LinearLayout

    // 캘린더뷰 관련
    private lateinit var calendarLayout: LinearLayout
    private lateinit var calendarView: MaterialCalendarView
    private var calendarDay: CalendarDay = CalendarDay.today()

    // 다이어리뷰 관련
    lateinit var nowdiaryEntry: String
    private lateinit var diaryViewLayout: LinearLayout
    private lateinit var diaryMoodView: ImageView
    private lateinit var diaryDateView: TextView
    private lateinit var diaryTitleView: TextView
    private lateinit var diaryLine: View
    private lateinit var diaryCommentView: TextView
    private lateinit var diaryImagesScrollView: HorizontalScrollView
    lateinit var diaryImagesView: LinearLayout
    private val dotColor: Int = Color.WHITE
    private val imageViews = mutableListOf<ImageView>()
    private lateinit var editModeEndBtn: Button

    // 다이어리뷰 편집 관련
    private var onEditMode: Boolean = false

    private lateinit var mainActivity: MainActivity
    private lateinit var installDay: Calendar

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_travel_log, container, false)

        // 날짜 선택 레이아웃 설정
        dateSelectLayout = view.findViewById(R.id.dateSelectLayout)

        yearTextView = view.findViewById(R.id.tvYear)
        yearTextView.text = currentYear.toString()

        prevYearBtn = view.findViewById(R.id.btnPreviousYear)
        nextYearBtn = view.findViewById(R.id.btnNextYear)
        btn_january = view.findViewById(R.id.month_january)
        btn_february = view.findViewById(R.id.month_february)
        btn_march = view.findViewById(R.id.month_march)
        btn_april = view.findViewById(R.id.month_april)
        btn_may = view.findViewById(R.id.month_may)
        btn_june = view.findViewById(R.id.month_june)
        btn_july = view.findViewById(R.id.month_july)
        btn_august = view.findViewById(R.id.month_august)
        btn_september = view.findViewById(R.id.month_september)
        btn_october = view.findViewById(R.id.month_october)
        btn_november = view.findViewById(R.id.month_november)
        btn_december = view.findViewById(R.id.month_december)

        // 캘린더뷰 설정
        calendarLayout = view.findViewById(R.id.calendarViewLayout)
        calendarView = view.findViewById(R.id.calendarView)
        calendarView.setSelectedDate(calendarDay)

        // 다이어리뷰 설정
        nowdiaryEntry = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        diaryViewLayout = view.findViewById(R.id.diaryViewLayout)
        diaryMoodView = view.findViewById(R.id.diaryMoodView)
        diaryDateView = view.findViewById(R.id.diaryDateView)
        diaryTitleView = view.findViewById(R.id.diaryTitleView)
        diaryLine = view.findViewById(R.id.diaryLine)
        diaryCommentView = view.findViewById(R.id.diaryCommentView)
        diaryImagesView = view.findViewById(R.id.diaryImagesView)
        diaryImagesScrollView = view.findViewById(R.id.diaryImagesScrollView)
        editModeEndBtn = view.findViewById(R.id.editModeEndBtn)

        installDay = mainActivity.getInstallDateAsCalendar()!!

        // 데이터베이스 초기화
        diaryDatabaseHelper = DiaryDatabaseHelper(mainActivity)

        // 캘린더뷰 달 변경 막기
        calendarView.setOnMonthChangedListener { widget, date ->
            calendarView.setCurrentDate(calendarDay)
        }

        // 캘린더뷰 타이틀 클릭시 다시 년월 선택창 띄우는 이벤트
        calendarView.setOnTitleClickListener {
            dateSelectLayout.visibility = View.VISIBLE
            calendarLayout.visibility = View.GONE
        }

        // 캘린더뷰 주말, 설치일 색깔 설정
        calendarView.addDecorators(
            DownloadDecorator(installDay),
            SundayDecorator(),
            SaturdayDecorator()
        )

        // 캘린더뷰 날짜 클릭시 해당 날짜의 다이어리 정보 가져오기
        calendarView.setOnDateChangedListener { widget, date, selected ->

            val calendar = Calendar.getInstance().apply {
                set(date.year, date.month - 1, date.day)
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            nowdiaryEntry = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            var diaryEntry = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry)

            if (diaryEntry == null) {
                val dateParts = nowdiaryEntry.split("-")
                val obj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(nowdiaryEntry)
                val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(obj)
                diaryDateView.text = dateParts[1] + "월  " +
                        dateParts[2] + "일  " +
                        dayOfWeek
                if (nowdiaryEntry > today) {
                    AlertDialog.Builder(mainActivity).apply {
                        setTitle("다이어리 안내")
                        setMessage("오늘이 지난 날의 다이어리 입니다. 미리 작성할까요?")
                        setPositiveButton("네!") { _, _ ->
                            diaryDatabaseHelper.saveDiaryEntry(DiaryEntry(nowdiaryEntry, Mood.BAD))
                            refreshDecorators(dateParts[1].toInt())
                            showDiaryEntry(diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry))
                            mainActivity.showToast("다이어리를 꾹 눌러 편집하세요!")
                        }
                        setNegativeButton("아뇨..") { dialog, _ ->
                            diaryViewLayout.visibility = View.GONE
                            calendarLayout.visibility = View.VISIBLE
                            dialog.dismiss() }
                        create().show()

                    }
                } else {
                    // 혹시나 오늘이거나 그 전날의 날짜인 경우
                    diaryDatabaseHelper.saveDiaryEntry(DiaryEntry(nowdiaryEntry, Mood.BAD))
                    refreshDecorators(dateParts[1].toInt())
                    showDiaryEntry(diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry))
                    mainActivity.showToast("다이어리를 꾹 눌러 편집하세요!")
                }
            } else {
                showDiaryEntry(diaryEntry)
            }
        }

        // 다이어리뷰에서 길게 누를 시 해당 날짜의 다이어리 편집하기 이벤트
        registerForContextMenu(diaryImagesView)
        registerForContextMenu(diaryDateView)
        registerForContextMenu(diaryTitleView)
        registerForContextMenu(diaryCommentView)
        registerForContextMenu(diaryMoodView)

        // 년도 전환 버튼 이벤트
        prevYearBtn.setOnClickListener {
            currentYear--
            updateYear()
        }
        nextYearBtn.setOnClickListener {
            if (currentYear > Calendar.getInstance().get(Calendar.YEAR)) {
                mainActivity.showToast("이듬해 " + currentYear + "년을 넘을 수 없습니다")
                return@setOnClickListener
            }
            currentYear++
            updateYear()
        }

        // 해당 월 달력 선택 이벤트
        btn_january.setOnClickListener{ selectYearAndMonth(1) }
        btn_february.setOnClickListener{ selectYearAndMonth(2) }
        btn_march.setOnClickListener{ selectYearAndMonth(3) }
        btn_april.setOnClickListener{ selectYearAndMonth(4) }
        btn_may.setOnClickListener{ selectYearAndMonth(5) }
        btn_june.setOnClickListener{ selectYearAndMonth(6) }
        btn_july.setOnClickListener{ selectYearAndMonth(7) }
        btn_august.setOnClickListener{ selectYearAndMonth(8) }
        btn_september.setOnClickListener{ selectYearAndMonth(9) }
        btn_october.setOnClickListener{ selectYearAndMonth(10) }
        btn_november.setOnClickListener{ selectYearAndMonth(11) }
        btn_december.setOnClickListener{ selectYearAndMonth(12) }

        // 다이어리뷰 클릭시 이벤트
        diaryViewLayout.setOnClickListener {
            if (!onEditMode) {
                diaryViewLayout.visibility = View.GONE
                calendarLayout.visibility = View.VISIBLE
            }
        }

        // 다이어리 날짜 클릭시 이벤트
        diaryDateView.setOnClickListener {
            diaryViewLayout.visibility = View.GONE
            calendarLayout.visibility = View.VISIBLE
            refreshDecorators(nowdiaryEntry.split("-")[1].toInt())
        }

        // 다이어리 표정 클릭시 이벤트
        diaryMoodView.setOnClickListener {
            if (onEditMode) {
                val de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry) ?: return@setOnClickListener
                if (de.mood == Mood.GOOD) {
                    de.mood = Mood.BAD
                    if (!diaryDatabaseHelper.updateDiaryEntry(de)) {
                        mainActivity.showToast("데이터베이스 업데이트 도중 오류가 발생했습니다!")
                    }
                    diaryMoodView.setImageResource(R.drawable.icon_sad)
                } else {
                    de.mood = Mood.GOOD
                    if (!diaryDatabaseHelper.updateDiaryEntry(de)) {
                        mainActivity.showToast("데이터베이스 업데이트 도중 오류가 발생했습니다!")
                    }
                    diaryMoodView.setImageResource(R.drawable.icon_happy)
                }
            }
        }

        diaryTitleView.setOnClickListener {
            if (onEditMode) openDiaryEditDialog()
        }
        diaryCommentView.setOnClickListener {
            if (onEditMode) openDiaryEditDialog()
        }

        editModeEndBtn.setOnClickListener {
            onEditMode = false
            editModeEndBtn.visibility = View.GONE
            mainActivity.showToast("다이어리 편집 모드를 종료합니다.")
            val de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry) ?: return@setOnClickListener
            diaryImagesView.removeAllViews()
            displayImages(mainActivity, de.imageUriList, diaryImagesView)
            if (de.imageUriList.isNullOrEmpty()) {
                diaryImagesScrollView.visibility = View.GONE
            } else diaryImagesScrollView.visibility = View.VISIBLE
            if (diaryCommentView.text.contains("이곳을 클릭하여")) diaryCommentView.setText("")
        }
        return view
    }

    private fun updateYear() {
        yearTextView.text = currentYear.toString()
    }

    private fun selectYearAndMonth(month: Int) {
        dateSelectLayout.visibility = View.GONE
        calendarLayout.visibility = View.VISIBLE
        calendarDay = CalendarDay.from(currentYear, month, 1)
        calendarView.setCurrentDate(calendarDay)
        refreshDecorators(month)
    }

    // 다이어리의 사진 레이아웃에 사진 추가하는 함수
    fun displayImages(context: Context, imageUriList: List<String>?, container: LinearLayout) {
        imageViews.clear()

        if (imageUriList.isNullOrEmpty()) {
            if (onEditMode) {
                val imageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        // width, height 둘다 242dp로 설정
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            242f,
                            context.resources.displayMetrics
                        ).toInt(),
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            242f,
                            context.resources.displayMetrics
                        ).toInt()
                    ).apply {
                        // 이미지 간에 좌우 간격 설정
                        setMargins(
                            TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                5f,
                                context.resources.displayMetrics
                            ).toInt(),
                            0,
                            TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                5f,
                                context.resources.displayMetrics
                            ).toInt(),
                            0
                        )
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                // 이미지 설정
                imageView.setImageResource(R.drawable.icon_plus)

                // diaryImagesView 레이아웃에 다이어리 이미지들 추가
                diaryImagesView.addView(imageView)

                // + 아이콘 클릭시 사진 선택 이벤트
                imageView.setOnClickListener {
                    val de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry)!!
                    mainActivity.requestPermissionsAndOpenGalleryForDiary(de)
                }

                diaryImagesScrollView.visibility = View.VISIBLE

                return
            } else {
                diaryImagesScrollView.visibility = View.GONE
                return
            }
        }
        diaryImagesScrollView.visibility = View.VISIBLE

        // 각 이미지 URI에 대해 ImageView 동적 생성 및 추가
        imageUriList!!.forEach { uriString ->
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    // width, height 둘다 242dp로 설정
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt(),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt()
                ).apply {
                    // 이미지 간에 좌우 간격 설정
                    setMargins(
                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt(),
                        0,
                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt(),
                        0
                    )
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Glide를 사용하여 이미지 로드
            Glide.with(context)
                .load(Uri.parse(uriString))
                .into(imageView)

            // container 레이아웃에 다이어리 이미지들 추가
            container.addView(imageView)

            imageViews.add(imageView)
            registerForContextMenu(imageView)

            imageView.setOnClickListener {
                if (onEditMode) {
                    AlertDialog.Builder(mainActivity).apply {
                        setTitle("사진 삭제")
                        setMessage("삭제 이후 사진을 복구할 수 없습니다. 삭제할까요?")
                        setPositiveButton("네!") { _, _ ->
                            if (!diaryDatabaseHelper.removeImageUri(diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry)!!.date, uriString)) {
                                mainActivity.showToast("사진 삭제에 오류가 발생했습니다.")
                                return@setPositiveButton
                            }
                            unregisterForContextMenu(imageView)
                            container.removeView(imageView)
                            imageViews.remove(imageView)

                            val de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry) ?: return@setPositiveButton
                            diaryImagesView.removeAllViews()
                            displayImages(mainActivity, de.imageUriList, diaryImagesView)
                        }
                        setNegativeButton("아뇨..") { dialog, _ -> dialog.dismiss() }
                        create().show()

                    }
                } else {
                    // 이미지 확대
                    val dialog = Dialog(mainActivity)
                    dialog.setContentView(R.layout.dialog_image_zoom) // 확대된 이미지를 보여줄 레이아웃 파일

                    val zoomedImageView = dialog.findViewById<ImageView>(R.id.zoomed_image_view)

                    // Glide를 사용하여 이미지 로드
                    Glide.with(mainActivity)
                        .load(Uri.parse(uriString))
                        .into(zoomedImageView)

                    zoomedImageView.setOnClickListener {
                        dialog.dismiss() // 이미지 클릭 시 다이얼로그 닫기
                    }

                    dialog.show() // 다이얼로그 표시
                }
            }
        }
        if (onEditMode) {
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    // width, height 둘다 242dp로 설정
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt(),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt()
                ).apply {
                    // 이미지 간에 좌우 간격 설정
                    setMargins(
                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt(),
                        0,
                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt(),
                        0
                    )
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // 이미지 설정
            imageView.setImageResource(R.drawable.icon_plus)

            // diaryImagesView 레이아웃에 다이어리 이미지들 추가
            diaryImagesView.addView(imageView)

            diaryImagesScrollView.visibility = View.VISIBLE

            // + 아이콘 클릭시 사진 선택 이벤트
            imageView.setOnClickListener {
                val de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry) ?: return@setOnClickListener
                mainActivity.requestPermissionsAndOpenGalleryForDiary(de)
            }
        }
    }

    private fun showDiaryEntry(diaryEntry: DiaryEntry?) {
        calendarLayout.visibility = View.GONE
        diaryViewLayout.visibility = View.VISIBLE
        if (!onEditMode) editModeEndBtn.visibility = View.GONE
        else editModeEndBtn.visibility = View.VISIBLE
        if (diaryEntry != null) {
            // 표정 이미지 변경
            diaryMoodView.setImageResource(
                if (diaryEntry.mood == Mood.BAD) R.drawable.icon_sad else R.drawable.icon_happy
            )
            // 날짜 텍스트 뷰 설정
            val dateParts = diaryEntry.date.split("-")
            val dayOfWeek = diaryEntry.getFormattedDate()
                ?.let { SimpleDateFormat("EEEE", Locale.getDefault()).format(it) }
            diaryDateView.text = dateParts[1] + "월  " +
                    dateParts[2] + "일  " +
                    dayOfWeek
            // 다이어리 제목 설정
            if (diaryEntry.title != null) {
                var content = SpannableString("제목: " + diaryEntry.title)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                diaryTitleView.text = content
            } else {
                var content = SpannableString("제목: ")
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                diaryTitleView.text = content
            }
            // 다이어리에 이ㅁㄴ미지가 없을 시 선 제거
            diaryLine.visibility = if (diaryEntry.comment.isNullOrEmpty() && diaryEntry.imageUriList.isNullOrEmpty()) View.GONE else View.VISIBLE
            // 다이어리 내용 설정
            if (diaryEntry.comment.isNullOrEmpty()) {
                if (onEditMode) {
                    diaryCommentView.text = " 이곳을 클릭하여 내용을 입력하세요!"
                } else diaryCommentView.text = ""
            } else {
                diaryCommentView.text = " " + diaryEntry.comment
            }
            // 추가된 ImageView 초기화 후 이미지 추가
            diaryImagesView.removeAllViews()
            displayImages(mainActivity, diaryEntry.imageUriList, diaryImagesView)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val mInflater = mainActivity.menuInflater
        // 다이어리 클릭시 컨텍스트 메뉴 띄우기
        if (v === diaryImagesView ||
            v === diaryMoodView ||
            v === diaryDateView ||
            v === diaryTitleView ||
            v === diaryCommentView) {
            menu!!.setHeaderTitle("다이어리 내용")
            mInflater.inflate(R.menu.options_diary_edit_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 다이어리 내용 변경 클릭시
            R.id.diaryEntryEdit -> {
                if (onEditMode) {
                    mainActivity.showToast("편집 모드 상태 입니다. 편집할 내용을 눌러주세요.")
                } else {
                    onEditMode = true
                    editModeEndBtn.visibility = View.VISIBLE
                    mainActivity.showToast("다이어리 편집 모드로 전환했습니다. 편집할 내용을 눌러주세요.")
                    var de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry) ?: return true

                    if (de.comment.isNullOrEmpty()) diaryCommentView.setText(" 이곳을 클릭하여 내용을 입력하세요!")
                    // 다이어리 이미지 목록 없을 시 + 추가해서 업로드 할 수 있게
                    if (de.imageUriList == null ||
                        de.imageUriList!!.size == 0) {
                        val imageView = ImageView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                // width, height 둘다 242dp로 설정
                                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt(),
                                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt()
                            ).apply {
                                // 이미지 간에 좌우 간격 설정
                                setMargins(
                                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt(),
                                    0,
                                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt(),
                                    0
                                )
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        // 이미지 설정
                        imageView.setImageResource(R.drawable.icon_plus)

                        // diaryImagesView 레이아웃에 다이어리 이미지들 추가
                        diaryImagesView.addView(imageView)

                        diaryImagesScrollView.visibility = View.VISIBLE

                        // + 아이콘 클릭시 사진 선택 이벤트
                        imageView.setOnClickListener {
                            de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry)!!
                            mainActivity.requestPermissionsAndOpenGalleryForDiary(de)
                        }
                    } else { // 이미 있다면 + 추가해서 추가로 이미지 업로드 할 수 있게
                        val imageView = ImageView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                // width, height 둘다 300dp로 설정
                                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt(),
                                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 242f, context.resources.displayMetrics).toInt()
                            ).apply {
                                // 이미지 간에 좌우 간격 설정
                                setMargins(
                                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt(),
                                    0,
                                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt(),
                                    0
                                )
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        // 이미지 설정
                        imageView.setImageResource(R.drawable.icon_plus)

                        // diaryImagesView 레이아웃에 다이어리 이미지들 추가
                        diaryImagesView.addView(imageView)

                        diaryImagesScrollView.visibility = View.VISIBLE

                        // + 아이콘 클릭시 사진 선택 이벤트
                        imageView.setOnClickListener {
                            de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry) ?: return@setOnClickListener
                            mainActivity.requestPermissionsAndOpenGalleryForDiary(de)
                        }
                    }
                }
                return true
            }
            // 다이어리 내용 삭제 클릭시
            R.id.diaryEntryDelete -> {
                val deleted = diaryDatabaseHelper.deleteDiaryEntryByDate(nowdiaryEntry)
                if (deleted) {
                    mainActivity.showToast("성공적으로 데이터를 삭제했습니다!")
                    diaryDateView.setText("00월  00일  0요일")
                    diaryTitleView.setText("")
                    diaryCommentView.setText("")
                    diaryMoodView.setImageResource(R.drawable.icon_sad)
                    diaryImagesView.removeAllViews()
                    refreshDecorators(nowdiaryEntry.split("-")[1].toInt())
                } else mainActivity.showToast("데이터를 삭제하는 데 실패했습니다..")
                diaryViewLayout.visibility = View.GONE
                calendarLayout.visibility = View.VISIBLE
                return true
            }
        }
        return false
    }

    fun refreshDecorators(month: Int) {
        calendarView.removeDecorators()
        val diaryEntries = diaryDatabaseHelper.getDiaryEntriesByMonth(currentYear, month)
        calendarView.addDecorators(
            DownloadDecorator(installDay),
            SundayDecorator(),
            SaturdayDecorator(),
            HappyDecorator(
                mainActivity,
                diaryEntries
            ),
            BadDecorator(
                mainActivity,
                diaryEntries
            ),
            DotDecorator(
                dotColor,
                diaryEntries
            )
        )
    }

    private fun openDiaryEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_diary_edit, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.diaryEditTitle)
        val etComment = dialogView.findViewById<EditText>(R.id.diaryEditComment)
        var de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry) ?: return

        etTitle.setText(de.title)
        etComment.setText(de.comment)

        val dlg = AlertDialog.Builder(mainActivity)
            .setTitle("일기 편집")
            .setView(dialogView)
            .setPositiveButton("확인") { dialog, _ ->
                if (!etTitle.text.isNullOrBlank()) {
                    val title = etTitle.text.toString()
                    de.title = title
                }
                if (!etComment.text.isNullOrBlank()) {
                    val comment = etComment.text.toString()
                    de.comment = comment
                }

                // 변경 사항 데이터베이스에 업데이트
                if (!diaryDatabaseHelper.updateDiaryEntry(de)) mainActivity.showToast("다이어리 업데이트 도중 오류가 발생했습니다!")

                // 업데이트 된 데이터베이스에서 다시 가져오기
                de = diaryDatabaseHelper.getDiaryEntryByDate(nowdiaryEntry) ?: return@setPositiveButton

                showDiaryEntry(de)
                mainActivity.showToast("성공적으로 변경했습니다.")
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dlg.show()
    }
}