package com.example.panelcreama.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.panelcreama.MainActivity
import com.example.panelcreama.R

class MyCompaniesFragment : Fragment() {

    companion object {
        const val TAG = "my_companies"
        fun newInstance(): MyCompaniesFragment {
            return MyCompaniesFragment()
        }
    }

    private lateinit var create: Button
    private var listener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.my_companies, container, false)
        create = view.findViewById(R.id.create)
        create.setOnClickListener {
            listener?.invoke()
        }
        updateButton()
        return view
    }

    fun updateButton() {
        create.text = resources.getString(
            if (MainActivity.username.isBlank()) R.string.login_title else R.string.add_company
        )
    }

    fun setListener(listener: (() -> Unit)) {
        this.listener = listener
    }

}