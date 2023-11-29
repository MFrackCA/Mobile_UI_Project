package com.example.ui_prototype

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.VideoView
import com.bumptech.glide.Glide

internal class GridVAdapter(
    private val mediaList: List<GridVModel>,
    private val context: Context
) : BaseAdapter() {



    private var layoutInflater: LayoutInflater? = null
    private lateinit var videoView: VideoView;
    private lateinit var imageView: ImageView;

    override fun getCount(): Int {
        return mediaList.size
    }

    override fun getItem(position: Int): Any {
        return mediaList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        // on blow line we are checking if layout inflater
        // is null, if it is null we are initializing it.
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        // on the below line we are checking if convert view is null.
        // If it is null we are initializing it.
        if (convertView == null) {
            // on below line we are passing the layout file
            // which we have to inflate for each item of grid view.
            convertView = layoutInflater!!.inflate(R.layout.my_gridview_item, null)
        }

        videoView = convertView!!.findViewById(R.id.video_preview)
        imageView = convertView!!.findViewById(R.id.image_preview)

        if (mediaList[position].mediaType == "video") {
            imageView.visibility = View.GONE

            videoView.visibility = View.VISIBLE

            val videoUri = Uri.parse(mediaList[position].mediaUrl)

            videoView.setVideoURI(videoUri)

            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
            }

            videoView.start()



        } else {

            videoView.visibility = View.GONE

            imageView.visibility = View.VISIBLE

            val imageUri = Uri.parse(mediaList[position].mediaUrl)

            Glide.with(imageView.context).load(imageUri)
                .placeholder(R.drawable.baseline_image_24).into(imageView)
        }

        return convertView
    }
}
