package com.example.application.activities

import com.example.application.databases.DiaryDatabaseHelper
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.application.R
import com.example.application.adapters.YouTubeVideoAdapter
import com.example.application.fragments.HomeFragment
import com.example.application.fragments.MapFragment
import com.example.application.fragments.TravelLogFragment
import com.example.application.fragments.VideoListFragment
import com.example.application.fragments.WrittenTravelLogFragment
import com.example.application.fragments.YoutubePlayerFragment
import com.example.application.structs.DiaryEntry
import com.example.application.structs.PetInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), YouTubeVideoAdapter.VideoClickListener {
    lateinit var homeFragment: HomeFragment
    lateinit var travelLogFragment: TravelLogFragment
    lateinit var writtenTravelLogFragment: WrittenTravelLogFragment
    lateinit var youtubePlayerFragment: YoutubePlayerFragment
    lateinit var videoListFragment: VideoListFragment
    lateinit var mapFragment: MapFragment

    private lateinit var diaryDatabaseHelper: DiaryDatabaseHelper
    private val PREFS_NAME = "AttendancePrefs"
    private val KEY_INSTALL_DATE = "installDate"
    private lateinit var bottomNavigation: BottomNavigationView

    var imageUri: Uri? = null

    var onDiaryEditMode = false
    private var editDiaryEntry: DiaryEntry? = null
    private var toast: Toast? = null

    private lateinit var TitleUpground: View
    private lateinit var Title: TextView
    private lateinit var fragmentScrollContainer: ScrollView
    lateinit var fullScreenContainer: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContentView(R.layout.activity_main)

        homeFragment = HomeFragment()
        replaceFragment(homeFragment)

        travelLogFragment = TravelLogFragment()

        writtenTravelLogFragment = WrittenTravelLogFragment()

        youtubePlayerFragment = YoutubePlayerFragment()

        videoListFragment = VideoListFragment()

        mapFragment = MapFragment()

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, travelLogFragment).hide(travelLogFragment)
            add(R.id.fragmentContainer, homeFragment).hide(homeFragment)
            add(R.id.fragmentContainer, writtenTravelLogFragment).hide(writtenTravelLogFragment)
        }

        Title = findViewById(R.id.Title)
        TitleUpground = findViewById(R.id.TitleUpground)
        fragmentScrollContainer = findViewById(R.id.fragmentScrollContainer)
        fullScreenContainer = findViewById(R.id.fullscreen_container)

        // 네비게이션 바 클릭 시 이벤트 설정
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_travel_log -> {
                    replaceFragment(travelLogFragment)
                    true
                }
                R.id.home -> {
                    replaceFragment(homeFragment)
                    true
                }
                R.id.nav_written_travel_log -> {
                    showToast("돋보기 버튼을 눌러 검색하세요!")
                    replaceFragment(writtenTravelLogFragment)
                    true
                }
                R.id.nav_pet_information -> {
                    replaceFragment(videoListFragment)
                    true
                }
                R.id.nav_pet_map -> {
                    checkLocationPermission()
                    true
                }
                else -> false
            }
        }

        diaryDatabaseHelper = DiaryDatabaseHelper(this)

        // 앱 설치일 저장 및 출석 체크 호출
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if(!sharedPrefs.contains(KEY_INSTALL_DATE)) {
            val installDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            sharedPrefs.edit().putString(KEY_INSTALL_DATE, installDate).apply()
        }
        checkAttendance()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            showToast("현재 위치 정보 권한이 없습니다.")
        } else replaceFragment(mapFragment)
    }

    override fun onVideoClick(videoId: String) {
        val fragment = YoutubePlayerFragment.newInstance(videoId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkAttendance() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val installDate = sharedPrefs.getString(KEY_INSTALL_DATE, null) ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        diaryDatabaseHelper.fillMissedDays(installDate, today)
    }

    // Fragment 교체 함수
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.fragments.forEach { hide(it) }
            show(fragment)
        }.commit()
    }

    // 권한 요청 및 갤러리 열기 / 다이어리뷰에서
    @SuppressLint("InlinedApi")
    fun requestPermissionsAndOpenGalleryForDiary(diaryEntry: DiaryEntry) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14 (API 34) 이상
                val permissions = arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED) {
                    showToast("현재 제한된 액세스 허용 상태입니다. 모두 허용을 권장합니다.")
                }
                onDiaryEditMode = true
                editDiaryEntry = diaryEntry
                requestPermissions(permissions, MEDIA_PERMISSION_REQUEST_CODE)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13 (API 33) 이상
                onDiaryEditMode = true
                editDiaryEntry = diaryEntry
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), MEDIA_PERMISSION_REQUEST_CODE)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10 (API 29) 이상
                onDiaryEditMode = true
                editDiaryEntry = diaryEntry
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MEDIA_PERMISSION_REQUEST_CODE)
            }
            else -> {
                // Android 9 (API 28) 이하
                onDiaryEditMode = true
                editDiaryEntry = diaryEntry
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MEDIA_PERMISSION_REQUEST_CODE)
            }
        }
    }

    // 다이어리 뷰 전용 갤러리 열어서 데이터베이스에 저장 호출
    private fun openGalleryForDiary(diaryEntry: DiaryEntry) {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickDiaryImageLauncher.launch(gallery)
    }

    // 갤러리 열기 다이어리 전용
    private val pickDiaryImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    imageUri = it
                    addImageToDiaryEntryForDiary(editDiaryEntry!!, saveDiaryImage(imageUri))
                    val newDiaryEntry = diaryDatabaseHelper.getDiaryEntryByDate(editDiaryEntry!!.date)
                    travelLogFragment.diaryImagesView.removeAllViews()
                    travelLogFragment.displayImages(this, newDiaryEntry!!.imageUriList, travelLogFragment.diaryImagesView)
                    onDiaryEditMode = false
                    editDiaryEntry = null
                }
            }
        }

    // 다이어리 이미지 저장
    private fun saveDiaryImage(uri: Uri?): Uri? {
        if (uri == null) return null

        val contentResolver: ContentResolver = contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "diary_image_$timeStamp.jpg"
        val file = File(filesDir, fileName)

        FileOutputStream(file).use { outputStream ->
            inputStream?.use { input ->
                input.copyTo(outputStream)
            }
        }

        return file.toUri() // 이미지 경로 반환
    }

    // DiaryEntry의 imageUriList에 선택한 이미지를 데이터베이스에 추가하고 저장하는 함수
    private fun addImageToDiaryEntryForDiary(diaryEntry: DiaryEntry, uri: Uri?): DiaryEntry {
        if (uri == null) return diaryEntry

        // imageUriList가 null 또는 empty인 경우 처리
        val updatedUriList = (diaryEntry.imageUriList?.toMutableList() ?: mutableListOf()).apply {
            add(uri.toString())
        }

        // 업데이트된 DiaryEntry 저장
        val updatedEntry = diaryEntry.copy(imageUriList = updatedUriList)
        diaryDatabaseHelper.updateDiaryEntry(updatedEntry)  // 데이터베이스에 저장

        if (diaryDatabaseHelper.getDiaryEntryByDate(diaryEntry.date) == null) {
            showToast("이미지 추가 도중 오류가 발생했습니다.")
            return diaryEntry
        }

        showToast("이미지가 성공적으로 추가되었습니다.")

        return diaryDatabaseHelper.getDiaryEntryByDate(diaryEntry.date)!!
    }

    // 권한 요청 및 갤러리 열기
    @SuppressLint("InlinedApi")
    fun requestPermissionsAndOpenGallery() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14 (API 34) 이상
                val permissions = arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED) {
                    showToast("현재 제한된 액세스 허용 상태 입니다. 모두 허용을 권장합니다!")
                }
                requestPermissions(permissions, MEDIA_PERMISSION_REQUEST_CODE)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13 (API 33) 이상
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), MEDIA_PERMISSION_REQUEST_CODE)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10 (API 29) 이상
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MEDIA_PERMISSION_REQUEST_CODE)
            }
            else -> {
                // Android 9 (API 28) 이하
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MEDIA_PERMISSION_REQUEST_CODE)
            }
        }
    }

    // 권한 요청 결과 처리하기
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MEDIA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (onDiaryEditMode) {
                        openGalleryForDiary(editDiaryEntry!!)
                    } else openGallery()  // 권한이 승인되면 갤러리 열기
                } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED) {
                    if (onDiaryEditMode) {
                        openGalleryForDiary(editDiaryEntry!!)
                    } else openGallery()  // 권한이 승인되면 갤러리 열기
                } else {
                    showToast("권한이 필요합니다. 설정에서 권한을 허용해주세요.")
                }
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                replaceFragment(mapFragment)
            } else {
                replaceFragment(mapFragment)
                showToast("위치 권한이 거부 상태 입니다. 내 위치를 불러올 수 없습니다..")
            }
        }
    }

    // 갤러리 열기
    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    if (isImageUri(it)) {
                        imageUri = it
                        homeFragment.petImage.setImageURI(imageUri)
                        savePetImage(it)
                    } else {
                        showToast("선택한 미디어가 이미지 형식이 아닙니다!")
                    }
                }
            }
        }

    fun isImageUri(uri: Uri?): Boolean {
        if (uri == null) return false

        // MimeType을 통해 파일 타입을 판별
        val mimeType = contentResolver.getType(uri)
        return mimeType?.startsWith("image/") == true  // image/로 시작하는 MimeType이 이미지임
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncher.launch(gallery)
    }

    fun loadPetImage(): Uri? {
        val fileName = "pet_image.jpg"
        val file = File(filesDir, fileName)
        return if (file.exists()) Uri.fromFile(file) else null
    }

    fun savePetImage(uri: Uri?) {
        val contentResolver: ContentResolver = contentResolver
        val inputStream: InputStream? = uri?.let { contentResolver.openInputStream(it) }

        // 내부 저장소에 이미지 저장하기
        val file = File(filesDir, "pet_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    // 반려동물 정보 로드
    fun loadPetInfo(): PetInfo? {
        val fileName = "pet_info.txt"
        val file = File(filesDir, fileName)

        if (!file.exists()) {
            return null // 파일이 존재하지 않으면 null 반환
        }

        BufferedReader(FileReader(file)).use { reader ->
            val nameLine = reader.readLine() ?: return null
            val typeLine = reader.readLine() ?: return null
            val likeLine = reader.readLine() ?: return null
            val hateLine = reader.readLine() ?: return null
            val birthCalLine = reader.readLine()

            // 각 필드에서 값 추출
            val name = nameLine.substringAfter("name: ").trim()
            val type = typeLine.substringAfter("type: ").trim()
            val like = likeLine.substringAfter("like: ").trim()
            val hate = hateLine.substringAfter("hate: ").trim()

            return if (birthCalLine != null) {
                try {
                    val birthMillis = birthCalLine.substringAfter("birth: ").trim().toLong()
                    val birth = Calendar.getInstance().apply { timeInMillis = birthMillis }
                    PetInfo(name, type, like, hate, birth)
                } catch (e: NumberFormatException) {
                    PetInfo(name, type, like, hate, null)
                }
            } else {
                PetInfo(name, type, like, hate, null)
            }
        }
    }

    // 반려동물 정보 저장
    fun savePetInfo(petInfo: PetInfo) {
        val fileName = "pet_info.txt"
        val file = File(filesDir, fileName)

        BufferedWriter(FileWriter(file)).use { writer ->
            writer.write("name: ${petInfo.name}\n")
            writer.write("type: ${petInfo.type}\n")
            writer.write("like: ${petInfo.like}\n")
            writer.write("hate: ${petInfo.hate}\n")
            writer.write("birth: ${petInfo.birth?.timeInMillis}\n") // Calendar를 milliseconds로 변환하여 저장
        }

        homeFragment.petInfo = petInfo
    }

    fun getDrawableUri(resourceId: Int): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + applicationContext.resources.getResourcePackageName(resourceId) +
                    '/' + applicationContext.resources.getResourceTypeName(resourceId) +
                    '/' + applicationContext.resources.getResourceEntryName(resourceId)
        )
    }

    companion object {
        private const val MEDIA_PERMISSION_REQUEST_CODE = 1004
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // 앱 설치일 반환
    fun getInstallDateAsCalendar(): Calendar? {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val installDateStr = sharedPrefs.getString(KEY_INSTALL_DATE, null) ?: return null

        // 설치일을 Calendar로 변환
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val installDate = dateFormat.parse(installDateStr) ?: return null

        val calendar = Calendar.getInstance()
        calendar.time = installDate
        return calendar
    }

    fun showToast(msg: String) {
        toast?.cancel()
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        toast?.show()
    }

    fun setFullScreen(isFullScreened: Boolean) {
        if (isFullScreened) {
            Title.visibility = View.GONE
            TitleUpground.visibility = View.GONE
            bottomNavigation.visibility = View.GONE
            fragmentScrollContainer.visibility = View.GONE
            fullScreenContainer.visibility = View.VISIBLE
        } else {
            Title.visibility = View.VISIBLE
            TitleUpground.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            fragmentScrollContainer.visibility = View.VISIBLE
            fullScreenContainer.visibility = View.GONE
        }
    }
}
