package com.example.panelcreama.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.panelcreama.MainActivity
import com.example.panelcreama.R
import java.io.File

class VideoShowFragment : Fragment() {

    private val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    lateinit var videoView: VideoView
    lateinit var progressBar: ProgressBar
    lateinit var download: Button
    var sourceFile: File? = null

    companion object {
        const val TAG = "video_show"
        fun newInstance(): VideoShowFragment {
            return VideoShowFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.video_show_layout_fragment, container, false)
        videoView = view.findViewById(R.id.video_view)
        videoView.visibility = View.GONE
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        download = view.findViewById(R.id.download_btn)
        download.visibility = View.GONE
        download.setOnClickListener {
            sourceFile?.let {
                val id = (1..10000000).random()
                val file = File(downloadDir, "video_$id.mp4")
                if (file.exists()) {
                    file.delete()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }
                file.writeBytes(it.readBytes())
                makeToast(resources.getString(R.string.video_download))
            }
        }
        return view
    }

    fun setVideoView(activity: MainActivity, file: File) {
        progressBar.visibility = View.GONE
        videoView.visibility = View.VISIBLE
        download.visibility = View.VISIBLE
        sourceFile = file
        videoView.setVideoURI(Uri.fromFile(file))
        videoView.setMediaController(MediaController(activity))
        videoView.requestFocus(0)
        videoView.start()
    }

    private fun makeToast(text: String) {
        val toast = Toast.makeText(
            context,
            text,
            Toast.LENGTH_SHORT
        )
        toast.show()
    }


}