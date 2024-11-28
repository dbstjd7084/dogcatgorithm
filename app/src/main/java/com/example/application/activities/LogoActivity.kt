package com.example.application.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.application.R

class LogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContentView(R.layout.activity_logo)

        // 환영 메세지 출력
        Toast.makeText(this, "멍냥고리즘에 오신 것을 환영합니다!", Toast.LENGTH_SHORT).show()
        
        val logo: ImageView = findViewById(R.id.splashLogo)

        // 애니메이션을 활용해 2.1초 동안 사라지는 페이드 아웃 구성 및 시작
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 2100
            fillAfter = true
        }

        logo.startAnimation(fadeOut)

        // 타이머로 로고 이미지가 사라진 후 로딩 액티비티 호출
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoadActivity::class.java))
            finish()
        }, 2120)
    }
}