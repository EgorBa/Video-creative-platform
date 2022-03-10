package com.example.panelcreama

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panelcreama.buttons.ImageLoadButton

class VideoCreativeFragment : Fragment() {

    companion object {
        const val TAG = "video_creator"
        fun newInstance(): VideoCreativeFragment {
            return VideoCreativeFragment()
        }
    }

    private lateinit var imageLoadButton1 : View
    private lateinit var imageLoadButton2 : View
    private lateinit var imageLoadButton3 : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.video_creative_fragment, container, false)
        imageLoadButton1 = view.findViewById(R.id.btn1)
        imageLoadButton2 = view.findViewById(R.id.btn2)
        imageLoadButton3 = view.findViewById(R.id.btn3)
        return view
    }

}