package com.example.application.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.application.R
import com.example.application.activities.MainActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class YoutubePlayerFragment: Fragment() {
    private lateinit var mainActivity: MainActivity

    private lateinit var youtubePlayerView: YouTubePlayerView
    private lateinit var fullscreenContainer: ViewGroup
    private var videoId: String? = ""

    companion object {
        private const val ARG_VIDEO_ID = "video_id"

        fun newInstance(videoId: String): YoutubePlayerFragment {
            val fragment = YoutubePlayerFragment()
            val args = Bundle()
            args.putString(ARG_VIDEO_ID, videoId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoId = it.getString(ARG_VIDEO_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_youtube_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        youtubePlayerView = view.findViewById(R.id.youtube_player_view)
        fullscreenContainer = mainActivity.fullScreenContainer

        lifecycle.addObserver(youtubePlayerView)

        youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoId?.let {
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
        }, IFramePlayerOptions.Builder()
            .controls(1) // 플레이어 컨트롤러 표시
            .fullscreen(1) // 전체화면 버튼 활성화
            .build())

        youtubePlayerView.addFullscreenListener(object : FullscreenListener {
            @SuppressLint("NewApi")
            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                requireActivity().window.insetsController?.hide(android.view.WindowInsets.Type.navigationBars())
                // 전체화면 뷰를 fullscreenContainer에 추가
                val parent = youtubePlayerView.parent as ViewGroup
                parent.removeView(youtubePlayerView)
                fullscreenContainer.addView(fullscreenView)

                youtubePlayerView.visibility = View.GONE
                mainActivity.setFullScreen(true)

                // 화면 방향 변경 (가로 모드)
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            @SuppressLint("NewApi")
            override fun onExitFullscreen() {
                requireActivity().window.insetsController?.show(android.view.WindowInsets.Type.navigationBars())
                // fullscreenContainer에서 전체화면 뷰 제거
                fullscreenContainer.removeAllViews()
                val parent = mainActivity.findViewById<FrameLayout>(R.id.youtube_fragment_container)
                parent.addView(youtubePlayerView)

                youtubePlayerView.visibility = View.VISIBLE
                mainActivity.setFullScreen(false)

                // 화면 방향 복구 (세로 모드)
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        youtubePlayerView.release()
    }
}