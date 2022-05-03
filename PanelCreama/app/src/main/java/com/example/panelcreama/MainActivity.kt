package com.example.panelcreama

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.panelcreama.controllers.NetworkController
import com.example.panelcreama.fragments.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        var username: String = ""
    }

    private val mock = false
    private lateinit var networkController: NetworkController
    private lateinit var next: Button
    private lateinit var frag: FrameLayout
    private lateinit var profile: ImageView
    private lateinit var companies: ImageView
    private lateinit var promoButton: Button
    private lateinit var language: ImageView
    private lateinit var listener: (() -> Unit)
    private val myRequestId = 10

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var lang = getSharedPreferences("language", 0).getString("language", "ru") ?: "en"
        updateLocale(lang)

        setContentView(R.layout.activity_main)
        initViews()
        requestPermissions()
        networkController = NetworkController(this, ::onLogin, ::onSuccessesUpload)

        language.setOnClickListener {
            makeToast(resources.getString(R.string.change_language))

            lang = if (lang == "ru") {
                "en"
            } else {
                "ru"
            }
            updateLocale(lang)
            saveLocale(lang)
        }

        updatePromoButton()

        listener = {
            val fragment =
                if (username.isBlank()) LoginFragment.newInstance() else CompanySettingsFragment.newInstance()
            val tag = if (username.isBlank()) LoginFragment.TAG else CompanySettingsFragment.TAG
            (fragment as? LoginFragment)?.setCallback { str ->
                run {
                    saveLogin(str)
                    updatePromoButton()
                    onBackPressed()
                }
            }

            supportFragmentManager.commit {
                add(
                    R.id.container,
                    fragment,
                    tag
                )
                addToBackStack(tag)
            }
            changeButtonsViews(tag)
        }

        promoButton.setOnClickListener {
            listener.invoke()
        }

        next.setOnClickListener {
            hideKeyboard(this)

            if (next.text == resources.getString(R.string.next)) {

                if (!hasCompanyName()) {
                    makeToast(resources.getString(R.string.enter_your_company_name))
                    return@setOnClickListener
                }

                if (!hasLogo()) {
                    makeToast(resources.getString(R.string.check_logo))
                    return@setOnClickListener
                }

                supportFragmentManager.commit {
                    add(
                        R.id.container,
                        VideoCreativeSettingsFragment.newInstance(),
                        VideoCreativeSettingsFragment.TAG
                    )
                    addToBackStack(VideoCreativeSettingsFragment.TAG)
                }
                changeButtonsViews(VideoCreativeSettingsFragment.TAG)
            } else {
                if (next.text == resources.getString(R.string.confirm)) {

                    if (!hasAnimationWithoutText()) {
                        makeToast(resources.getString(R.string.check_logo))
                        return@setOnClickListener
                    }

                    supportFragmentManager.commit {
                        add(
                            R.id.container,
                            VideoShowFragment.newInstance(),
                            VideoShowFragment.TAG
                        )
                        addToBackStack(VideoShowFragment.TAG)
                    }
                    changeButtonsViews(VideoShowFragment.TAG)

                    val videoSettingsFragment =
                        supportFragmentManager.findFragmentByTag(CompanySettingsFragment.TAG) as CompanySettingsFragment
                    val videoCreateFragment =
                        supportFragmentManager.findFragmentByTag(VideoCreativeSettingsFragment.TAG) as VideoCreativeSettingsFragment

                    if (mock) {
                        networkController.sendMockRequest()
                    } else {
                        networkController.getColors(videoSettingsFragment, videoCreateFragment)
                    }
                }
            }
        }
        profile.setOnClickListener {
            supportFragmentManager.commit {
                add(R.id.container, ProfileFragment.newInstance(), ProfileFragment.TAG)
                addToBackStack(ProfileFragment.TAG)
            }
            changeButtonsViews(ProfileFragment.TAG)
        }
        companies.setOnClickListener {
            supportFragmentManager.commit {
                val companies = MyCompaniesFragment.newInstance()
                companies.setListener(listener)
                add(R.id.container, companies, MyCompaniesFragment.TAG)
                addToBackStack(MyCompaniesFragment.TAG)
            }
            changeButtonsViews(MyCompaniesFragment.TAG)
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

    private fun initViews(){
        next = findViewById(R.id.next)
        language = findViewById(R.id.language)
        promoButton = findViewById(R.id.button)
        frag = findViewById(R.id.container)
        profile = findViewById(R.id.profile)
        companies = findViewById(R.id.companies)
    }

    private fun requestPermissions(){
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                myRequestId
            )
        }

        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                myRequestId
            )
        }
    }

    private fun updatePromoButton() {
        promoButton.text = resources.getString(
            if (username.isBlank()) R.string.login_title else R.string.try_to_create_video_creative
        )
        val currentCount = supportFragmentManager.fragments.size
        for (i in (0 until currentCount)) {
            (supportFragmentManager.fragments[i] as? MyCompaniesFragment)?.updateButton()
        }
    }

    private fun onLogin() {
        if (username == "") {
            val fragment = LoginFragment.newInstance()
            fragment.setCallback { str ->
                run {
                    saveLogin(str)
                    updatePromoButton()
                    onBackPressed()
                }
            }
            supportFragmentManager.commit {
                add(
                    R.id.container,
                    fragment,
                    LoginFragment.TAG
                )
                addToBackStack(LoginFragment.TAG)
            }
            changeButtonsViews(LoginFragment.TAG)
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
                next.text = resources.getString(R.string.next)
                next.isGone = false
            } else {
                if (tag == VideoCreativeSettingsFragment.TAG) {
                    next.text = resources.getString(R.string.confirm)
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
            is MyCompaniesFragment -> MyCompaniesFragment.TAG
            is ProfileFragment -> ProfileFragment.TAG
            is VideoCreativeSettingsFragment -> VideoCreativeSettingsFragment.TAG
            is LoginFragment -> LoginFragment.TAG
            else -> ""
        }
    }

    private fun hasCompanyName(): Boolean {
        val videoSettingsFragment =
            supportFragmentManager.findFragmentByTag(CompanySettingsFragment.TAG) as CompanySettingsFragment
        return videoSettingsFragment.getCompanyName().isNotBlank()
    }

    private fun hasLogo(): Boolean {
        val videoSettingsFragment =
            supportFragmentManager.findFragmentByTag(CompanySettingsFragment.TAG) as CompanySettingsFragment
        return videoSettingsFragment.getSiteURL()
            .isNotBlank() || videoSettingsFragment.getStringLogo().isNotBlank()
    }

    private fun hasAnimationWithoutText(): Boolean {
        val videoSettingsFragment =
            supportFragmentManager.findFragmentByTag(CompanySettingsFragment.TAG) as CompanySettingsFragment
        return videoSettingsFragment.getSiteURL()
            .isNotBlank() || videoSettingsFragment.getStringLogo().isNotBlank()
    }

    private fun makeToast(text: String) {
        val toast = Toast.makeText(
            applicationContext,
            text,
            Toast.LENGTH_SHORT
        )
        toast.show()
    }

    private fun updateLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(
            config,
            resources.displayMetrics
        )
    }

    private fun saveLocale(lang: String) {
        val prefs = getSharedPreferences("language", 0)
        val editor = prefs.edit()
        editor.putString("language", lang)
        editor.apply()
    }

    private fun saveLogin(username: String) {
        val prefs = getSharedPreferences("username", 0)
        val editor = prefs.edit()
        editor.putString("username", username)
        editor.apply()
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun onSuccessesUpload(file: File) {
        val f =
            supportFragmentManager.findFragmentByTag(VideoShowFragment.TAG) as VideoShowFragment
        f.setVideoView(this@MainActivity, file)
    }

}