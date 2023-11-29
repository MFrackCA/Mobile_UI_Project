package com.example.ui_prototype

import android.app.Dialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.DialogFragment

class VideoPlaybackDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoUriString = arguments?.getString("mediaUri") ?: return
        val videoUri = Uri.parse(videoUriString)
        val videoView = view.findViewById<VideoView>(R.id.videoView)

        // Add MediaController for playback controls
        val mediaController = android.widget.MediaController(context)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.start()
            mediaPlayer.isLooping = true // If you want the video to loop
        }
        videoView.setOnCompletionListener { dismiss() } // Close the dialog when video finishes
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Set the dialog to full-screen width
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        // Apply the custom style for rounded corners
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_map_frame)
        return dialog
    }
}
