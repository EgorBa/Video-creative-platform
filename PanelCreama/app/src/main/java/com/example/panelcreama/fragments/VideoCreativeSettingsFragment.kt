package com.example.panelcreama.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.panelcreama.R
import com.example.panelcreama.buttons.ImageLoadButton

class VideoCreativeSettingsFragment : Fragment() {

    companion object {
        const val TAG = "video_creator"
        fun newInstance(): VideoCreativeSettingsFragment {
            return VideoCreativeSettingsFragment()
        }
    }

    private lateinit var imageLoadButton1: View
    private lateinit var imageLoadButton2: View
    private lateinit var imageLoadButton3: View
    private lateinit var edit1: EditText
    private lateinit var edit2: EditText
    private lateinit var edit3: EditText
    private lateinit var toggle1: ToggleButton
    private lateinit var toggle2: ToggleButton
    private lateinit var toggle3: ToggleButton
    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private lateinit var spinner3: Spinner
    private lateinit var saleEdit1: EditText
    private lateinit var saleEdit2: EditText
    private lateinit var saleEdit3: EditText
    private val mapTranslate: Map<String, String> = mapOf(
        "без анимации" to "simple",
        "посимвольно" to "by symbol",
        "приближение" to "scale",
        "покачивание" to "wiggle",
        "перемещение" to "move"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.video_creative_fragment, container, false)
        imageLoadButton1 = view.findViewById(R.id.btn1)
        imageLoadButton2 = view.findViewById(R.id.btn2)
        imageLoadButton3 = view.findViewById(R.id.btn3)
        edit1 = view.findViewById(R.id.edit1)
        edit2 = view.findViewById(R.id.edit2)
        edit3 = view.findViewById(R.id.edit3)
        toggle1 = view.findViewById(R.id.toggle1)
        toggle2 = view.findViewById(R.id.toggle2)
        toggle3 = view.findViewById(R.id.toggle3)
        spinner1 = view.findViewById(R.id.list_of_types_1)
        spinner2 = view.findViewById(R.id.list_of_types_2)
        spinner3 = view.findViewById(R.id.list_of_types_3)
        saleEdit1 = view.findViewById(R.id.edit_sale1)
        saleEdit2 = view.findViewById(R.id.edit_sale2)
        saleEdit3 = view.findViewById(R.id.edit_sale3)
        return view
    }

    fun getImage1String(): String =
        (imageLoadButton1 as FragmentContainerView).getFragment<ImageLoadButton>().getString()
    fun getImage2String(): String =
        (imageLoadButton2 as FragmentContainerView).getFragment<ImageLoadButton>().getString()
    fun getImage3String(): String =
        (imageLoadButton3 as FragmentContainerView).getFragment<ImageLoadButton>().getString()

    fun getDescr1String(): String = edit1.text.toString()
    fun getDescr2String(): String = edit2.text.toString()
    fun getDescr3String(): String = edit3.text.toString()

    fun getToggle1Value(): Boolean = toggle1.isChecked
    fun getToggle2Value(): Boolean = toggle2.isChecked
    fun getToggle3Value(): Boolean = toggle3.isChecked

    fun getSpinner1Value(): String = getSpinnerValue(spinner1.selectedItem.toString())
    fun getSpinner2Value(): String = getSpinnerValue(spinner2.selectedItem.toString())
    fun getSpinner3Value(): String = getSpinnerValue(spinner3.selectedItem.toString())

    fun getSale1String(): String = saleEdit1.text.toString()
    fun getSale2String(): String = saleEdit2.text.toString()
    fun getSale3String(): String = saleEdit3.text.toString()

    private fun getSpinnerValue(str: String): String = mapTranslate[str] ?: str
}