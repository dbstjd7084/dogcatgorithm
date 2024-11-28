package com.example.application.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.application.BuildConfig
import com.example.application.R
import com.example.application.adapters.YouTubeVideoAdapter
import com.example.application.activities.MainActivity
import com.example.application.interfaces.OpenAIApiService
import com.example.application.interfaces.YouTubeApiService
import com.example.application.structs.ai.Message
import com.example.application.structs.ai.OpenAIRequest
import com.example.application.structs.ai.OpenAIResponse
import com.example.application.structs.youtube.YouTubeVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class VideoListFragment: Fragment() {
    private lateinit var mainActivity: MainActivity

    // 유튜브 발급 API 키
    private val youTubeApiKey = BuildConfig.YOUTUBE_API_KEY

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: YouTubeVideoAdapter
    private var videoList = mutableListOf<YouTubeVideo>()

    // 유튜브 API
    private lateinit var Yretrofit: Retrofit
    private lateinit var youTubeApi: YouTubeApiService

    // AI API 키
    private val aiApiKey = BuildConfig.AI_API_KEY

    // ChatGPT API
    private lateinit var Aretrofit: Retrofit
    private lateinit var aiApi: OpenAIApiService

    // AI 멘트
    private lateinit var aiComment: TextView

    private lateinit var keyword: String // 유튜브 검색 키워드

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
        return inflater.inflate(R.layout.fragment_video_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = GridLayoutManager(mainActivity, 2)

        adapter = YouTubeVideoAdapter(videoList, object : YouTubeVideoAdapter.VideoClickListener {
            override fun onVideoClick(videoId: String) {
                mainActivity.onVideoClick(videoId)
            }
        })
        recyclerView.adapter = adapter

        // 유튜브 Retrofit 객체 생성 및 Api 인터페이스 초기화
        Yretrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")  // YouTube Data API v3의 기본 URL
            .addConverterFactory(GsonConverterFactory.create())  // JSON 변환을 위한 Gson
            .build()
        youTubeApi = Yretrofit.create(YouTubeApiService::class.java)

        videoList.clear()
        adapter.notifyDataSetChanged()
        if (mainActivity.homeFragment.petInfo == null) {
            keyword = "반려동물 꿀팁"
        } else if (mainActivity.homeFragment.petInfo!!.type == "dog") {
            keyword = "강아지 꿀팁"
        } else if (mainActivity.homeFragment.petInfo!!.type == "cat") {
            keyword = "고양이 꿀팁"
        } else {
            keyword = "반려동물 꿀팁"
        }
        
        // AI API 설정 및 호출
        // Interceptor를 사용하여 Authorization 헤더 추가
        val interceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithAuth = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer ${aiApiKey}")
                .build()

            chain.proceed(requestWithAuth)
        }
        // OkHttpClient에 Interceptor 추가
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
        Aretrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")  // OpenAI API의 기본 URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        aiApi = Aretrofit.create(OpenAIApiService::class.java)

        // AI 멘트 Id 가져오고, 질문 및 응답
        aiComment = view.findViewById(R.id.AIComment)
        sendMessageToChatGPT()
    }

    private fun searchVideos(query: String, apiKey: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val searchResponse = youTubeApi.searchVideos(query = query, apiKey = apiKey)

            if (searchResponse.isSuccessful) {
                val videoIds = searchResponse.body()?.items?.map { it.id.videoId }?.joinToString(",")

                videoIds?.let {
                    val detailsResponse = youTubeApi.getVideoDetails(videoIds = it, apiKey = apiKey)

                    if (detailsResponse.isSuccessful) {
                        detailsResponse.body()?.items?.forEach { videoItem ->
                            val videoId = videoItem.id
                            val title = videoItem.snippet.title
                            val thumbnailUrl = videoItem.snippet.thumbnails.medium.url
                            val videoUrl = "https://www.youtube.com/watch?v=$videoId"
                            val channelName = videoItem.snippet.channelTitle ?: "Unknown Channel"
                            val viewCount = formatViewCount(videoItem.statistics.viewCount.toLong())
                            val publishedAt = getTimeAgo(videoItem.snippet.publishedAt)

                            videoList.add(YouTubeVideo(title, thumbnailUrl, videoUrl, channelName, viewCount, publishedAt))
                        }

                        // UI 업데이트
                        withContext(Dispatchers.Main) {
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            mainActivity.showToast("영상 세부정보를 가져오는 데 실패했습니다.")
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Log.e("[VideoListFragment]", "Search API 오류: ${searchResponse.code()} - ${searchResponse.message()}")
                    Log.e("[VideoListFragment]", "Search API Body: ${searchResponse.errorBody()?.string()}")
                    mainActivity.showToast("영상을 가져오는 데 실패했습니다.")
                }
            }
        }
    }

    // 유튜브 조회수를 정해진 형식으로 변환
    fun formatViewCount(viewCount: Long): String {
        return when {
            viewCount >= 1_000_000_000 -> "${viewCount / 1_000_000_000}억회"
            viewCount >= 100_000_000 -> String.format("%.1f억회", viewCount / 1_000_000_000.0)
            viewCount >= 100_000 -> "${viewCount / 10_000}만회"
            viewCount >= 10_000 -> String.format("%.1f만회", viewCount / 10_000.0)
            viewCount >= 1_000 -> String.format("%.1f천회", viewCount / 1_000.0)
            else -> "${viewCount}회"
        }
    }

    // 유튜브 게시일을 정해진 형식으로 변환
    fun getTimeAgo(publishedAt: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()) // YouTube API에서 제공하는 형식이라 함.
        val date = sdf.parse(publishedAt) ?: return "Unknown"

        val diffInMillis = System.currentTimeMillis() - date.time
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        val diffInMonths = diffInDays / 30
        val diffInYears = diffInDays / 365
        val diffInWeeks = diffInDays / 7
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)

        return when {
            diffInYears >= 1 -> "${diffInYears}년 전"
            diffInMonths >= 1 -> "${diffInMonths}달 전"
            diffInWeeks >= 2 -> "${diffInWeeks}주 전"
            diffInDays >= 1 -> "${diffInDays}일 전"
            diffInHours >= 1 -> "${diffInHours}시간 전"
            diffInMinutes >= 1 -> "${diffInMinutes}분 전"
            else -> "방금 전"
        }
    }

    // ChatGPT 질문
    fun sendMessageToChatGPT() {
        val petInfo = mainActivity.homeFragment.petInfo
        if (petInfo == null) {
            searchVideos(keyword, youTubeApiKey)
            return
        }
        val request = OpenAIRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                Message("system", "You are a YouTube agent tasked with recommending YouTube videos about pets."),
                Message("user", """
            Based on the pet information below, write one YouTube video keyword and a recommendation comment following the rules.
            Pets can either be a dog or a cat.
            
            Rules:
            If today is the pet's birthday, include a birthday greeting in the comment. If it is not their birthday, do not mention the birthday at all.
            If the dog appears to have an outgoing personality or enjoys outdoor activities like walking, pet cafes, or pet facilities, suggest keywords or comments related to pet-friendly places or activities.
            
            Pet Information:
            Name: ${petInfo.name}
            Type: ${petInfo.type}
            Today's Date: ${Calendar.getInstance()}
            Date of Birth: ${petInfo.birth}
            Likes: ${petInfo.like}
            Dislikes: ${petInfo.hate}
            
            Example:
            Dislikes: Walking -> Tips for dogs who dislike walking
            Likes: Treats -> How to train your dog with treats
            
            Format in Korean:
            keyword: Write one short keyword
            comment:
        """.trimIndent())
            )
        )

        aiApi.getChatCompletion(request).enqueue(object : Callback<OpenAIResponse> {
            override fun onResponse(call: Call<OpenAIResponse>, response: Response<OpenAIResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.choices?.get(0)?.message?.content ?: ""
                    // 응답 내용 처리
                    val keywordRegex = """keyword:\s*(.*)""".toRegex()
                    val commentRegex = """comment:\s*(.*)""".toRegex()

                    keyword = keywordRegex.find(reply)?.groups?.get(1)?.value ?: keyword
                    val getComment = commentRegex.find(reply)?.groups?.get(1)?.value ?: "none"

                    searchVideos(keyword, youTubeApiKey)
                    if (getComment != "none") {
                        aiComment.visibility = View.VISIBLE
                        aiComment.setText(getComment)
                    } else {
                        aiComment.visibility = View.GONE
                    }
                } else {
                    mainActivity.showToast("AI 답변 호출에 실패했습니다. 오류 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                mainActivity.showToast("AI 답변 호출에 실패했습니다.")
            }
        })
    }
}