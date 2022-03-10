package com.example.panelcreama

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {

    lateinit var next: Button
    lateinit var frag: FrameLayout
    lateinit var profile: ImageView
    lateinit var companies: ImageView
    lateinit var promoButton: Button
    lateinit var listener: (() -> Unit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        next = findViewById(R.id.next)
        promoButton = findViewById(R.id.button)
        frag = findViewById(R.id.container)
        profile = findViewById(R.id.profile)
        companies = findViewById(R.id.companies)

        listener = {
            supportFragmentManager.commit {
                add(
                    R.id.container,
                    CompanySettingsFragment.newInstance(),
                    CompanySettingsFragment.TAG
                )
                addToBackStack(CompanySettingsFragment.TAG)
            }
            changeButtonsViews(CompanySettingsFragment.TAG)
        }

        promoButton.setOnClickListener {
            listener.invoke()
        }

        next.setOnClickListener {
            supportFragmentManager.commit {
                add(R.id.container, VideoCreativeFragment.newInstance(), VideoCreativeFragment.TAG)
                addToBackStack(VideoCreativeFragment.TAG)
            }
            changeButtonsViews(VideoCreativeFragment.TAG)
        }
        profile.setOnClickListener {
            supportFragmentManager.commit {
                add(R.id.container, Profile.newInstance(), Profile.TAG)
                addToBackStack(Profile.TAG)
            }
            changeButtonsViews(Profile.TAG)
        }
        companies.setOnClickListener {
            supportFragmentManager.commit {
                val companies = MyCompanies.newInstance()
                companies.setListener(listener)
                add(R.id.container, companies, MyCompanies.TAG)
                addToBackStack(MyCompanies.TAG)
            }
            changeButtonsViews(MyCompanies.TAG)
        }
    }

    override fun onBackPressed() {
        val currentCount = supportFragmentManager.fragments.size
        if (currentCount == 0) {
            super.onBackPressed()
        } else {
            if (currentCount == 1) {
                changeButtonsViews(promoIsVisible = true)
            } else {
                val f = supportFragmentManager.fragments[currentCount - 2]
                changeButtonsViews(getTagByFragment(f))
            }
            supportFragmentManager.popBackStack()
        }
    }

    private fun changeButtonsViews(
        tag: String = "",
        promoIsVisible: Boolean = false
    ) {
        if (promoIsVisible) {
            promoButton.isGone = false
            next.isGone = true
        } else {
            promoButton.isGone = true
            if (tag == CompanySettingsFragment.TAG) {
                next.text = "Next"
                next.isGone = false
            } else {
                if (tag == VideoCreativeFragment.TAG) {
                    next.text = "Confirm"
                    next.isGone = false
                } else {
                    next.isGone = true
                }
            }
        }
    }

    private fun getTagByFragment(fragment: Fragment): String {
        return when (fragment) {
            is CompanySettingsFragment -> CompanySettingsFragment.TAG
            is MyCompanies -> MyCompanies.TAG
            is Profile -> Profile.TAG
            is VideoCreativeFragment -> VideoCreativeFragment.TAG
            else -> ""
        }
    }
}