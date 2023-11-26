package com.example.ui_prototype

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ui_prototype.databinding.VideoTemplateBinding

class VideoAdapter(private val videoItems: List<VideoFeed>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: VideoTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(video: VideoFeed) {
            binding.userName.text = video.title
            binding.profileImage.setImageResource(video.profileImageResId)
            binding.videoDescription.text = video.description
            // You can also add code to handle video playback here if needed
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = VideoTemplateBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoItem = videoItems[position]
        holder.bind(videoItem)
    }

    override fun getItemCount(): Int {
        return videoItems.size
    }
}
