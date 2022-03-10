package com.example.panelcreama

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.panelcreama.buttons.ImageLoadButton

class CompanySettingsFragment : Fragment() {

    companion object {
        const val TAG = "company_settings"
        fun newInstance(): CompanySettingsFragment {
            return CompanySettingsFragment()
        }
    }

    private lateinit var logo: ImageView
    private lateinit var imageLoadButton : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.company_settings_fragment, container, false)
        logo = view.findViewById(R.id.logo)
        imageLoadButton = view.findViewById(R.id.loadImageButton)
        return view
    }

}