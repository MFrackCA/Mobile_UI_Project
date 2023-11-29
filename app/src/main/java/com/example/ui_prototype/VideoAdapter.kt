package com.example.ui_prototype

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ui_prototype.databinding.VideoTemplateBinding

class VideoAdapter(var mediaItems: List<MediaObj>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: VideoTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.replayButton.setOnClickListener {
                binding.videoView.start() // Restart the video
                it.visibility = View.GONE // Hide the replay button
            }
        }

        fun bind(mediaItem: MediaObj) {
            binding.userName.text = mediaItem.title
            mediaItem.profileImageResId?.let { binding.profileImage.setImageResource(it) }
            binding.videoDescription.text = mediaItem.description

            when (mediaItem.mediaType) {
                "video" -> {
                    binding.videoView.visibility = View.VISIBLE
                    binding.imageView.visibility = View.GONE
                    binding.replayButton.visibility = View.GONE // Hide replay button initially
                    val videoUri = Uri.parse(mediaItem.mediaUri)
                    binding.videoView.setVideoURI(videoUri)

                    binding.videoView.setOnPreparedListener { mp ->
                        mp.start()

                    }
                    // Show replay button when video ends
                    binding.videoView.setOnCompletionListener {
                        binding.replayButton.visibility = View.VISIBLE
                    }

                    binding.videoView.setOnErrorListener { mp, what, extra ->
                        Log.e("VideoAdapter", "MediaPlayer Error: what $what extra $extra")
                        true // Return true if the error has been handled
                    }
                }

                "image" -> {
                    binding.imageView.visibility = View.VISIBLE
                    binding.videoView.visibility = View.GONE
                    Glide.with(binding.imageView.context).load(mediaItem.mediaUri)
                        .placeholder(R.drawable.baseline_image_24).into(binding.imageView)


                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = VideoTemplateBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMediaItems(newMediaItems: List<MediaObj>) {
        mediaItems = newMediaItems
        notifyDataSetChanged()
    }
}