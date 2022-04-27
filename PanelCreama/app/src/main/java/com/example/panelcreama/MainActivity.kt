package com.example.panelcreama

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        var username: String = ""
    }

    val mock = false
    lateinit var next: Button
    lateinit var frag: FrameLayout
    lateinit var profile: ImageView
    lateinit var companies: ImageView
    lateinit var promoButton: Button
    lateinit var language: ImageView
    lateinit var listener: (() -> Unit)
    private var checkListIds = arrayListOf<Int>()
    private var orderListIds = arrayListOf<Int>()
    private val myRequestId = 10
    private val database = FirebaseDatabase.getInstance().reference.root
    private lateinit var auth: FirebaseAuth

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = getSharedPreferences("language", 0)
        var lang: String = sp.getString("language", "ru") ?: "en"
        updateLocale(lang)

        setContentView(R.layout.activity_main)
        next = findViewById(R.id.next)
        language = findViewById(R.id.language)
        promoButton = findViewById(R.id.button)
        frag = findViewById(R.id.container)
        profile = findViewById(R.id.profile)
        companies = findViewById(R.id.companies)

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

        auth = FirebaseAuth.getInstance()
        if (auth.uid == null) {
            auth.signInAnonymously().addOnCompleteListener {
                Log.d("result", "auth is done")
                username = getSharedPreferences("username", 0).getString("username", "") ?: ""
                login()
            }
        } else {
            username = getSharedPreferences("username", 0).getString("username", "") ?: ""
            login()
        }
        updatePromoButton()

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
                        VideoCreativeFragment.newInstance(),
                        VideoCreativeFragment.TAG
                    )
                    addToBackStack(VideoCreativeFragment.TAG)
                }
                changeButtonsViews(VideoCreativeFragment.TAG)
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
                        supportFragmentManager.findFragmentByTag(VideoCreativeFragment.TAG) as VideoCreativeFragment

                    if (mock) {
                        sendMockRequest()
                    } else {
                        getColors(videoSettingsFragment, videoCreateFragment)
                    }
                }
            }
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

    private fun updatePromoButton() {
        promoButton.text = resources.getString(
            if (username.isBlank()) R.string.login_title else R.string.try_to_create_video_creative
        )
        val currentCount = supportFragmentManager.fragments.size
        for (i in (0 until currentCount)) {
            (supportFragmentManager.fragments[i] as? MyCompanies)?.updateButton()
        }
    }

    private fun login() {
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
                if (tag == VideoCreativeFragment.TAG) {
                    next.text = resources.getString(R.string.confirm)
                    next.isGone = false
                } else {
                    next.isGone = true
                }
            }
        }
    }

    private fun addImagesToDB(videoCreateFragment: VideoCreativeFragment) {
        val videoSettingsFragment =
            supportFragmentManager.findFragmentByTag(CompanySettingsFragment.TAG) as CompanySettingsFragment
        val id0 = (1..10000000).random()
        val id1 = (1..10000000).random()
        val id2 = (1..10000000).random()
        val id3 = (1..10000000).random()
        addImageToDB(
            id0,
            videoCreateFragment.getSpinner1Value(),
            videoCreateFragment.getToggle1Value(),
            videoCreateFragment.getSale1String(),
            videoCreateFragment.getImage1String(),
            videoCreateFragment.getDescr1String()
        )
        addImageToDB(
            id1,
            videoCreateFragment.getSpinner2Value(),
            videoCreateFragment.getToggle2Value(),
            videoCreateFragment.getSale2String(),
            videoCreateFragment.getImage2String(),
            videoCreateFragment.getDescr2String()
        )
        addImageToDB(
            id2,
            videoCreateFragment.getSpinner3Value(),
            videoCreateFragment.getToggle3Value(),
            videoCreateFragment.getSale3String(),
            videoCreateFragment.getImage3String(),
            videoCreateFragment.getDescr3String()
        )
        addImageToDB(
            id3,
            "simple",
            false,
            "",
            "",
            videoSettingsFragment.getCompanyName(),
            true
        )
    }

    private fun addImageToDB(
        id: Int,
        animationType: String,
        clearBg: Boolean,
        sale: String,
        image: String,
        descr: String,
        useTextType: Boolean = false
    ) {
        orderListIds.add(id)
        database.child("images").child(id.toString()).child("image").setValue(image)
            .addOnCompleteListener {
                database.child("images").child(id.toString()).child("descr").setValue(descr)
                    .addOnCompleteListener {
                        if (image == "" && descr == "") {
                            checkListIds.add(id)
                        }
                        sendGenerateVideo(id, animationType, clearBg, sale, useTextType)
                    }
            }
    }

    private fun getColors(
        videoSettingsFragment: CompanySettingsFragment,
        videoCreateFragment: VideoCreativeFragment
    ) {
        if (videoSettingsFragment.getStringLogo() != "") {
            val id = (1..10000000).random()
            database.child("logos").child(id.toString()).child("image")
                .setValue(videoSettingsFragment.getStringLogo())
                .addOnCompleteListener {
                    sendExtractColors(videoCreateFragment, id = id)
                }
        } else {
            sendExtractColors(videoCreateFragment, site = videoSettingsFragment.getSiteURL())
        }
    }

    private fun getTagByFragment(fragment: Fragment): String {
        return when (fragment) {
            is CompanySettingsFragment -> CompanySettingsFragment.TAG
            is MyCompanies -> MyCompanies.TAG
            is Profile -> Profile.TAG
            is VideoCreativeFragment -> VideoCreativeFragment.TAG
            is LoginFragment -> LoginFragment.TAG
            else -> ""
        }
    }

//    private fun sendGet(im: String) {
//        val queue = Volley.newRequestQueue(this)
//        Log.d("lolkek", im)
//        val url = "https://afternoon-waters-50114.herokuapp.com/create/1?desc1=lol&im1=$im"
//        val stringRequest = StringRequest(
//            Request.Method.GET,
//            url,
//            { response ->
//                val json = JSONObject(response)
//                val utfDecoder: CharsetDecoder = Charset.forName("UTF-8").newDecoder()
//                val data: ByteBuffer =
//                    ByteBuffer.wrap(json.getString("video").toByteArray(charset("ISO-8859-1")))
//                val decodedData: CharBuffer = utfDecoder.decode(data)
//                val isoEncoder: CharsetEncoder = Charset.forName("ISO-8859-1").newEncoder()
//                val encodedData: ByteBuffer = isoEncoder.encode(decodedData);
//                val s = encodedData.array()
//
//                val filepath = "samplefile.avi"
//                val file = File(getExternalFilesDir(null).toString(), filepath)
//                if (file.exists()) {
//                    file.delete()
//                }
//                if (!file.exists()) {
//                    file.createNewFile()
//                }
//                FileOutputStream(file).use { fos -> fos.write(s) }
//
////                val f =
////                    supportFragmentManager.findFragmentByTag(VideoCreativeFragment.TAG) as VideoCreativeFragment
////                f.videoView.setVideoURI(Uri.fromFile(file))
////                f.videoView.setMediaController(MediaController(this))
////                f.videoView.requestFocus(0)
////                f.videoView.start()
//
//            },
//            {
//                val toast = Toast.makeText(
//                    applicationContext,
//                    it.toString(),
//                    Toast.LENGTH_SHORT
//                )
//                toast.show()
//
//                val filepath = "error.txt"
//                val file = File(getExternalFilesDir(null).toString(), filepath)
//                if (file.exists()) {
//                    file.delete()
//                }
//                if (!file.exists()) {
//                    file.createNewFile()
//                }
//                FileWriter(file).use { fos -> fos.write(url) }
//            }
//        )
//        stringRequest.retryPolicy = DefaultRetryPolicy(
//            30000,
//            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        )
//        queue.add(stringRequest)
//    }

    var colors = ""

    private fun sendExtractColors(
        videoCreateFragment: VideoCreativeFragment,
        site: String = "",
        id: Int = 0
    ) {
        makeToast("Start extracting colors")
        val queue = Volley.newRequestQueue(this)
        val url = "https://afternoon-waters-50114.herokuapp.com/logo/1?logo_id=$id&site=$site"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
                val json = JSONObject(it)
                colors = json["colors"].toString()
                makeToast("Colors extracted")
                addImagesToDB(videoCreateFragment)
            },
            {
                makeToast("Some problems with extracting colors")
            }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    private fun sendGenerateVideo(id: Int, animationType: String, clearBg: Boolean, sale: String, useTypeText: Boolean) {
        database.child("videos").child(id.toString()).child("video")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        checkListIds.add(id)
                        if (checkListIds.size == 4) {
                            sendGenerateCreative()
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

        val queue = Volley.newRequestQueue(this)
        val url =
            "https://afternoon-waters-50114.herokuapp.com/generate/${if (useTypeText) 0 else 1}?id=$id&colors=${
                URLEncoder.encode(
                    colors,
                    "UTF-8"
                )
        }&animation_type=$animationType&clear_bg=$clearBg&sale=$sale"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
                makeToast("Part of creative is generated")
            },
            {
                makeToast("Generating a part of creative, please wait")
            }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    private fun sendGenerateCreative() {
        makeToast("Generating creative")

        val allIds = orderListIds[0].toString() + orderListIds[1].toString() + orderListIds[2].toString() + orderListIds[3].toString()
        database.child("videos").child(allIds).child("video")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        val s = dataSnapshot.value.toString().toByteArray(charset("ISO-8859-1"))

                        val filepath = "samplefile.mp4"
                        val file = File(getExternalFilesDir(null).toString(), filepath)
                        if (file.exists()) {
                            file.delete()
                        }
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        FileOutputStream(file).use { fos -> fos.write(s) }

                        val f =
                            supportFragmentManager.findFragmentByTag(VideoShowFragment.TAG) as VideoShowFragment
                        f.setVideoView(this@MainActivity, file)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        val queue = Volley.newRequestQueue(this)
        val url =
            "https://afternoon-waters-50114.herokuapp.com/videos/1?id0=${orderListIds[0]}&id1=${orderListIds[1]}&id2=${orderListIds[2]}&id3=${orderListIds[3]}"
        checkListIds.clear()
        orderListIds.clear()
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
//                val json = JSONObject(it)
//                val utfDecoder: CharsetDecoder = Charset.forName("UTF-8").newDecoder()
//                val data: ByteBuffer =
//                    ByteBuffer.wrap(json.getString("video").toByteArray(charset("ISO-8859-1")))
//                val decodedData: CharBuffer = utfDecoder.decode(data)
//                val isoEncoder: CharsetEncoder = Charset.forName("ISO-8859-1").newEncoder()
//                val encodedData: ByteBuffer = isoEncoder.encode(decodedData);
//                val s = encodedData.array()
//
//                val filepath = "samplefile.mp4"
//                val file = File(getExternalFilesDir(null).toString(), filepath)
//                if (file.exists()) {
//                    file.delete()
//                }
//                if (!file.exists()) {
//                    file.createNewFile()
//                }
//                FileOutputStream(file).use { fos -> fos.write(s) }
//
//                val f =
//                    supportFragmentManager.findFragmentByTag(VideoShowFragment.TAG) as VideoShowFragment
//                f.videoView.setVideoURI(Uri.fromFile(file))
//                f.videoView.setMediaController(MediaController(this))
//                f.videoView.requestFocus(0)
//                f.videoView.start()

                makeToast("Creative is generated")
            },
            {
                makeToast("Generating creative, please wait")
            }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    private fun sendMockRequest() {
        makeToast("Generating creative")

        val queue = Volley.newRequestQueue(this)
        val url =
            "https://afternoon-waters-50114.herokuapp.com/mock/1?path=154341540-f37a238b-dd36-4b80-865f-d370f2ec745a.mp4"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
                val json = JSONObject(it)
                val utfDecoder: CharsetDecoder = Charset.forName("UTF-8").newDecoder()
                val data: ByteBuffer =
                    ByteBuffer.wrap(json.getString("video").toByteArray(charset("ISO-8859-1")))
                val decodedData: CharBuffer = utfDecoder.decode(data)
                val isoEncoder: CharsetEncoder = Charset.forName("ISO-8859-1").newEncoder()
                val encodedData: ByteBuffer = isoEncoder.encode(decodedData)
                val s = encodedData.array()

                val filepath = "samplefile.mp4"
                val file = File(getExternalFilesDir(null).toString(), filepath)
                if (file.exists()) {
                    file.delete()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }
                FileOutputStream(file).use { fos -> fos.write(s) }

                val f =
                    supportFragmentManager.findFragmentByTag(VideoShowFragment.TAG) as VideoShowFragment
                f.setVideoView(this@MainActivity, file)

                makeToast("Creative is generated")
            },
            {
                makeToast("Generating creative, please wait")
            }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
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

}