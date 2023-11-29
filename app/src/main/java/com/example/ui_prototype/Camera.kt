package com.example.ui_prototype

import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.material3.Snackbar
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import com.example.ui_prototype.databinding.FragmentCameraBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Camera : Fragment() {

    private lateinit var viewBinding: FragmentCameraBinding

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    private var firebaseAuth: FirebaseAuth? = null

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                Log.d(TAG, "Permission: ${it.key}, ${it.value}")
                if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                    Log.d(TAG, "Permission denied: ${it.key}, ${it.value}")
                    permissionGranted = false
                }
            }
            Log.d(TAG, "Permission granted: $permissionGranted")
            if (permissionGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = FragmentCameraBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // clicking the capture button has two actions:
        // 1. short click: take a photo
        // 2. long click: start/stop video recording
        // if there is no video recording in progress, a short click will take a photo
        // if there is no video recording in progress, a long click will start a video recording and change the button's icon from normal icon to stop icon
        // if there is a video recording in progress, a short click will stop the video recording and change the button's icon from stop icon to normal icon
        // if there is a video recording in progress, a long click will do nothing
        viewBinding.captureButton.setOnClickListener {
            if (recording == null) {
                takePhoto()
            } else {
                captureVideo()
            }
        }
        viewBinding.captureButton.setOnLongClickListener {
            if (recording == null) {
                captureVideo()
            }
            true
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return viewBinding.root
    }



    fun showUseMediaPrompt(mediaUri: Uri, onPositive: (String) -> Unit, onNegative: (Unit) -> Unit) {
        // Determine the media type (image or video) based on the Uri
        // ... determine if the Uri is for an image, e.g., by checking the MIME type

        // Inflate the custom layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_media_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.image_preview)
        val videoView = dialogView.findViewById<VideoView>(R.id.video_preview)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.media_description)


        val isImage = mediaUri.let {
            requireContext().contentResolver.getType(it)!!.contains("image")
        }

        Log.d(TAG, "showUseMediaPrompt: isImage = $isImage")



        val mediaController: android.widget.MediaController = android.widget.MediaController(requireContext())
        mediaController.setAnchorView(videoView)

        // Set up the preview based on the media type
        if (isImage) {
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            // Load the image using Glide or similar library
            Glide.with(requireActivity()).load(mediaUri).into(imageView)
        } else {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            // Set the video URI and start playback
            videoView.setVideoURI(mediaUri)

            videoView.setMediaController(mediaController)

            videoView.requestFocus()
            videoView.start()
        }

        // Create the AlertDialog
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Preview")
            setView(dialogView)
            setPositiveButton("Use this") { _, _ ->
                val description = descriptionEditText.text.toString()
                onPositive(description) // Pass the description to the callback
            }
            setNegativeButton("Retake") { dialog, which ->
                // User wants to retake the media
                onNegative(Unit)
            }
            setOnDismissListener {
                // Stop video playback when the dialog is dismissed
                if (!isImage) {
                    videoView.stopPlayback()
                }
            }
        }.create()

        // Show the AlertDialog
        alertDialog.show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.previewView.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // clicking the capture button has two actions:
    // 1. short click: take a photo
    // 2. long click: start/stop video recording
    // if there is no video recording in progress, a short click will take a photo
    // if there is no video recording in progress, a long click will start a video recording and change the button's icon from normal icon to stop icon
    // if there is a video recording in progress, a short click will stop the video recording and change the button's icon from stop icon to normal icon
    // if there is a video recording in progress, a long click will do nothing
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Disable the button to prevent multiple clicks that could cause problems
        viewBinding.captureButton.isEnabled = false

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.CANADA)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    showUseMediaPrompt(output.savedUri!!, { description ->
                        firebaseAuth?.currentUser?.let {
                            val storageRef = Firebase.storage.reference

                            val imageRef = storageRef.child("images/${it.uid}/${output.savedUri?.lastPathSegment}")
                            val imageMetadata = com.google.firebase.storage.StorageMetadata.Builder()
                                .setContentType("image/jpeg")
                                .build()
                            val uploadTask = imageRef.putFile(output.savedUri!!, imageMetadata)
                            uploadTask.addOnProgressListener { taskSnapshot ->
                                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                                Log.d(TAG, "Upload is $progress% done")
                            }
                            uploadTask.addOnFailureListener {
                                // Handle unsuccessful uploads
                                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                            }.addOnSuccessListener { taskSnapshot ->
                                // associate the captured media with the user and store the metadata in usermedia collection
                                val db = Firebase.firestore

                                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                                    val userMedia = hashMapOf(
                                        "uid" to it.uid,
                                        "mediaType" to "image",
                                        "mediaUrl" to uri.toString(),
                                        "mediaName" to output.savedUri?.lastPathSegment,
                                        "mediaDate" to System.currentTimeMillis(),
                                        "description" to description
                                    )
                                    db.collection("usermedia")
                                        .add(userMedia)
                                        .addOnSuccessListener { documentReference ->
                                            Snackbar.make(requireView(), "Image uploaded", Snackbar.LENGTH_SHORT).show()
                                            Log.d(
                                                TAG,
                                                "DocumentSnapshot added with ID: ${documentReference.id}"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Snackbar.make(requireView(), "Image upload failed", Snackbar.LENGTH_SHORT).show()
                                            Log.w(TAG, "Error adding document", e)
                                        }
                                }

                            }
                        }
                    }, {
                        output.savedUri?.let {
                            requireContext().contentResolver.delete(it, null, null)
                        }
                    })
                }
            }
        )

        // Enable the button again once photo has been taken
        viewBinding.captureButton.isEnabled = true
    }


    // clicking the capture button has two actions:
    // 1. short click: take a photo
    // 2. long click: start/stop video recording
    // if there is no video recording in progress, a short click will take a photo
    // if there is no video recording in progress, a long click will start a video recording and change the button's icon from normal icon to stop icon
    // if there is a video recording in progress, a short click will stop the video recording and change the button's icon from stop icon to normal icon
    // if there is a video recording in progress, a long click will do nothing
    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        viewBinding.captureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            viewBinding.captureButton.apply {
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_camera_alt_24)
                isEnabled = true
                isLongClickable = true
            }
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(requireContext().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        // set the capture button icon to stop icon and enable the button
                        viewBinding.captureButton.apply {
                            icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_stop_24)
                            isEnabled = true
                            isLongClickable = false
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            showUseMediaPrompt(recordEvent.outputResults.outputUri, {description ->
                                firebaseAuth?.currentUser?.let {
                                    val storageRef = Firebase.storage.reference

                                    val videoRef = storageRef.child("videos/${it.uid}/${recordEvent.outputResults.outputUri.lastPathSegment}")
                                    val videoMetadata = com.google.firebase.storage.StorageMetadata.Builder()
                                        .setContentType("video/mp4")
                                        .build()
                                    val uploadTask = videoRef.putFile(recordEvent.outputResults.outputUri, videoMetadata)
                                    uploadTask.addOnProgressListener { taskSnapshot ->
                                        val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                                        Log.d(TAG, "Upload is $progress% done")
                                    }
                                    uploadTask.addOnFailureListener {
                                        // Handle unsuccessful uploads
                                        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                                    }.addOnSuccessListener { taskSnapshot ->
                                        // associate the captured media with the user and store the metadata in usermedia collection
                                        val db = Firebase.firestore
                                        taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                                            val userMedia = hashMapOf(
                                                "uid" to it.uid,
                                                "mediaType" to "video",
                                                "mediaUrl" to uri.toString(),
                                                "mediaName" to recordEvent.outputResults.outputUri.lastPathSegment,
                                                "mediaDate" to System.currentTimeMillis(),
                                                "description" to description
                                            )
                                            db.collection("usermedia")
                                                .add(userMedia)
                                                .addOnSuccessListener { documentReference ->
                                                    Snackbar.make(
                                                        requireView(),
                                                        "Video uploaded",
                                                        Snackbar.LENGTH_SHORT
                                                    ).show()
                                                    Log.d(
                                                        TAG,
                                                        "DocumentSnapshot added with ID: ${documentReference.id}"
                                                    )
                                                }
                                                .addOnFailureListener { e ->
                                                    Snackbar.make(
                                                        requireView(),
                                                        "Video upload failed",
                                                        Snackbar.LENGTH_SHORT
                                                    ).show()
                                                    Log.w(TAG, "Error adding document", e)
                                                }
                                        }

                                    }
                                }
                            }, {
                                recordEvent.outputResults.outputUri?.let {
                                    requireContext().contentResolver.delete(it, null, null)
                                }
                            })
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                        // set the capture button icon to normal icon and enable the button
                        viewBinding.captureButton.apply {
                            icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_camera_alt_24)
                            isEnabled = true
                            isLongClickable = true
                        }
                    }
                }
            }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).toTypedArray()
    }

}