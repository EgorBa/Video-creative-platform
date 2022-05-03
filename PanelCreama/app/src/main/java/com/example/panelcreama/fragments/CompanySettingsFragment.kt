package com.example.panelcreama.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.panelcreama.R
import com.example.panelcreama.buttons.ImageLoadButton

class CompanySettingsFragment : Fragment() {

    companion object {
        const val TAG = "company_settings"
        fun newInstance(): CompanySettingsFragment {
            return CompanySettingsFragment()
        }
    }

    private lateinit var imageLoadButton: View
    private lateinit var editTextSite: EditText
    private lateinit var editTextCompanyName: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.company_settings_fragment, container, false)
        imageLoadButton = view.findViewById(R.id.loadImageButton)
        editTextSite = view.findViewById(R.id.site)
        editTextCompanyName = view.findViewById(R.id.company_name)
        return view
    }

    fun getStringLogo(): String =
        (imageLoadButton as FragmentContainerView).getFragment<ImageLoadButton>().getString()

    fun getSiteURL(): String = editTextSite.text.toString()
    fun getCompanyName(): String = editTextCompanyName.text.toString()

}