package com.example.application.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.application.BuildConfig
import com.example.application.R
import com.example.application.databases.FacilityDatabaseHelper
import com.example.application.interfaces.PetFriendlyService
import com.example.application.structs.googlemap.FacilityResponse
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoadActivity : AppCompatActivity() {
    private lateinit var facilityDatabaseHelper: FacilityDatabaseHelper
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingMsg: TextView
    private var isLoading: Boolean = false

    // 공공데이터 API 로깅 구성
    val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    val interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)  // 로깅 인터셉터 추가
        .addInterceptor(interceptor)  // 헤더 추가 인터셉터 추가
        .build()

    // 공공데이터 API 호출 구성
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.odcloud.kr/api/")
        .client(client)  // 설정된 OkHttpClient 사용
        .addConverterFactory(GsonConverterFactory.create())  // Gson 변환기 추가
        .build()

    // 공공데이터 API 키 및 서비스 구성
    private val serviceKey = BuildConfig.PET_FRIENDLY_FACILITIES_DATA_API_KEY
    private val service = retrofit.create(PetFriendlyService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        facilityDatabaseHelper = FacilityDatabaseHelper(this)
        progressBar = findViewById(R.id.progressBar)
        loadingMsg = findViewById(R.id.loadingMsg)

        // 페이드 인
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1300
            fillAfter = true
        }

        findViewById<LinearLayout>(R.id.load).startAnimation(fadeIn)

        // 타이머로 로고 이미지가 사라진 후 메인 액티비티 호출
        Handler(Looper.getMainLooper()).postDelayed({
            // 시설 DB가 비어있을 시에 API 로드
            if (facilityDatabaseHelper.getFacilityCount() == 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    isLoading = true
                    loadFacilities(1, 3500)
                }
            } else {
                // 액티비티 호출간의 0.4초 텀 주기
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingMsg.setText("다 잡았다!!")
                    progressBar.max = 1
                    progressBar.progress = 1
                }, 400)

                Handler(Looper.getMainLooper()).postDelayed({
                    callMain()
                }, 800)
            }
        }, 1300)

        // API 키 local.properties 에서 가져오기
        Log.d("API Key", serviceKey)
    }

    fun loadFacilities(page: Int = 1, amount: Int = 1000) {
        service.getFacilities(page, amount, "JSON", serviceKey).enqueue(object :
            Callback<FacilityResponse> {
            override fun onResponse(call: Call<FacilityResponse>, response: Response<FacilityResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val currentCount = responseBody?.currentCount ?: 0
                    val totalCount = responseBody?.totalCount ?: 0

                    progressBar.max = totalCount / amount + 1

                    facilityDatabaseHelper.insertFacilities(responseBody!!.data)

                    if (currentCount == amount && (page * amount) < totalCount) {
                        loadFacilities(page + 1, amount)
                        loadingMsg.setText("고양이가 물고기를 잡고있어요. 조금만 기다려주세요.. (" + page + "/" + (totalCount/amount + 1) + ")")
                        progressBar.progress = page
                    } else {
                        loadingMsg.setText("다 잡았다!!")
                        progressBar.progress = page

                        callMain()
                        isLoading = false
                    }
                } else {
                    Log.e("[Data API]", "Error: ${response.code()}")
                    facilityDatabaseHelper.clearAllFacilities()
                    loadFacilities(1, 3500)
                }
            }

            override fun onFailure(call: Call<FacilityResponse>, t: Throwable) {
                // 실패 시 성능을 고려해 기본값으로 로드
                Toast.makeText(applicationContext, "공공데이터 API 호출에 실패해 다시 로드합니다..", Toast.LENGTH_SHORT).show()
                facilityDatabaseHelper.clearAllFacilities()
                loadFacilities(page)
            }
        })
    }

    fun callMain() {
        // 메인 액티비티 호출
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 600
            fillAfter = true
        }

        findViewById<LinearLayout>(R.id.load).startAnimation(fadeOut)

        // 타이머로 로고 이미지가 사라진 후 메인 액티비티 호출
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@LoadActivity, MainActivity::class.java))
            finish()
        }, 600)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isLoading) {
            facilityDatabaseHelper.clearAllFacilities()
        }
    }
}