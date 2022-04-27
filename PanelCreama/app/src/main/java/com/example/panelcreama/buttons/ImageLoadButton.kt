package com.example.panelcreama.buttons

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.panelcreama.R
import java.io.InputStream

class ImageLoadButton : Fragment() {

    lateinit var background: LinearLayout
        private set
    lateinit var image: ImageView
        private set
    lateinit var close: TextView
        private set
    private var curBitmap: Bitmap? = null
    private var curString: String = ""

    companion object {
        fun newInstance(): ImageLoadButton {
            return ImageLoadButton()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.image_button, container, false)
        background = view.findViewById(R.id.bg)
        image = view.findViewById(R.id.image)
        close = view.findViewById(R.id.close)
        close.setOnClickListener {
            image.setImageBitmap(null)
            curBitmap = null
            curString = ""
            background.isVisible = true
            close.isVisible = false
        }
        val observer = MyLifecycleObserver(this, this)
        lifecycle.addObserver(observer)
        view.setOnClickListener {
            if (!close.isVisible) {
                observer.selectImage()
            }
        }
        return view
    }

    class MyLifecycleObserver(
        private val fragment: Fragment,
        private val imageLoadButton: ImageLoadButton
    ) : DefaultLifecycleObserver {
        private lateinit var getContent: ActivityResultLauncher<String>

        override fun onCreate(owner: LifecycleOwner) {
            getContent =
                fragment.requireActivity().activityResultRegistry.register(
                    fragment.tag.toString(),
                    owner,
                    ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri == null) return@register
                    imageLoadButton.curBitmap =
                        MediaStore.Images.Media.getBitmap(fragment.context?.contentResolver, uri)
                    val inputStream: InputStream? =
                        fragment.context?.contentResolver?.openInputStream(uri)
                    imageLoadButton.curString = inputStream?.let {
                        String(inputStream.readBytes(), charset("ISO-8859-1"))
                    } ?: ""
                    imageLoadButton.image.setImageBitmap(imageLoadButton.curBitmap)
                    imageLoadButton.background.isVisible = false
                    imageLoadButton.close.isVisible = true
                }
        }

        fun selectImage() {
            getContent.launch("image/*")
        }
    }

    fun getString(): String = curString

}