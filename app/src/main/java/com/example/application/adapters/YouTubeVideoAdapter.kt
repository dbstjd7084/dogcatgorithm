package com.example.application.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.application.R
import com.example.application.structs.youtube.YouTubeVideo

class YouTubeVideoAdapter(
    private val videoList: List<YouTubeVideo>,
    private val videoClickListener: VideoClickListener
) : RecyclerView.Adapter<YouTubeVideoAdapter.VideoViewHolder>() {

    interface VideoClickListener {
        fun onVideoClick(videoId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        holder.titleTextView.text = video.title
        holder.channelNameTextView.text = video.channelName
        holder.viewCountAndPublishedAtTextView.text = "조회수 " + video.viewCount + "ㆍ" + video.publishedAt

        Glide.with(holder.thumbnailImageView.context)
            .load(video.thumbnailUrl)
            .into(holder.thumbnailImageView)

        holder.itemView.setOnClickListener {
            val videoId = Uri.parse(video.videoUrl).getQueryParameter("v") ?: ""
            videoClickListener.onVideoClick(videoId)
        }
    }

    override fun getItemCount(): Int = videoList.size

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.videoTitle)
        val channelNameTextView: TextView = itemView.findViewById(R.id.channelName)
        val viewCountAndPublishedAtTextView: TextView = itemView.findViewById(R.id.viewCountAndPublishedAt)
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.videoThumbnail)
    }
}