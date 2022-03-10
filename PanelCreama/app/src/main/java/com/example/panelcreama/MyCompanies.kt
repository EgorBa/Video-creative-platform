package com.example.panelcreama

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MyCompanies : Fragment() {

    companion object {
        const val TAG = "my_companies"
        fun newInstance(): MyCompanies {
            return MyCompanies()
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
        return view
    }

    fun setListener(listener: (() -> Unit)) {
        this.listener = listener
    }

}