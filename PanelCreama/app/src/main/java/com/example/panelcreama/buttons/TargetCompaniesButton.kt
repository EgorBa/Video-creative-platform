package com.example.panelcreama.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.panelcreama.R

class TargetCompaniesButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val map = mapOf(
        R.id.checkbox1 to Pair(R.id.target1, Pair(R.drawable.vk, R.drawable.vk_pink)),
        R.id.checkbox2 to Pair(R.id.target2, Pair(R.drawable.instagram, R.drawable.instagram_pink)),
        R.id.checkbox3 to Pair(R.id.target3, Pair(R.drawable.mytarget, R.drawable.mytarget_pink)),
        R.id.checkbox4 to Pair(R.id.target4, Pair(R.drawable.yandex, R.drawable.yandex_pink))
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.target_companies, this)
        orientation = VERTICAL
        for ((key, value) in map) {
            findViewById<CheckBox>(key).setOnCheckedChangeListener { _, isChecked ->
                findViewById<ImageView>(value.first).setImageResource(
                    if (isChecked) value.second.first else value.second.second
                )
            }
        }
    }

}