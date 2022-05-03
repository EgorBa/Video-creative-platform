package com.example.panelcreama.controllers

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.panelcreama.MainActivity
import com.example.panelcreama.R
import com.example.panelcreama.fragments.CompanySettingsFragment
import com.example.panelcreama.fragments.VideoCreativeSettingsFragment
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

class NetworkController(
    private val activity: Activity,
    private val onLogin: (() -> Unit),
    private val onSuccessesUpload: ((file: File) -> Unit)
) {

    private val database = FirebaseDatabase.getInstance().reference.root
    private var checkListIds = arrayListOf<Int>()
    private var orderListIds = arrayListOf<Int>()
    private var colors = ""

    init {
        val auth = FirebaseAuth.getInstance()
        if (auth.uid == null) {
            auth.signInAnonymously().addOnCompleteListener {
                Log.d("result", "auth is done")
                MainActivity.username =
                    activity.getSharedPreferences("username", 0).getString("username", "") ?: ""
                onLogin.invoke()
            }
        } else {
            MainActivity.username =
                activity.getSharedPreferences("username", 0).getString("username", "") ?: ""
            onLogin.invoke()
        }
    }

    fun getColors(
        videoSettingsFragment: CompanySettingsFragment,
        videoCreateSettingsFragment: VideoCreativeSettingsFragment
    ) {
        if (videoSettingsFragment.getStringLogo() != "") {
            val id = (1..10000000).random()
            database.child("logos").child(id.toString()).child("image")
                .setValue(videoSettingsFragment.getStringLogo())
                .addOnCompleteListener {
                    sendExtractColors(videoCreateSettingsFragment, videoSettingsFragment, id = id)
                }
        } else {
            sendExtractColors(
                videoCreateSettingsFragment,
                videoSettingsFragment,
                site = videoSettingsFragment.getSiteURL()
            )
        }
    }

    private fun sendExtractColors(
        videoCreateSettingsFragment: VideoCreativeSettingsFragment,
        videoSettingsFragment: CompanySettingsFragment,
        site: String = "",
        id: Int = 0
    ) {
        makeToast(activity.resources.getString(R.string.start_extracting_colors))
        val queue = Volley.newRequestQueue(activity)
        val url = "https://afternoon-waters-50114.herokuapp.com/logo/1?logo_id=$id&site=$site"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
                val json = JSONObject(it)
                colors = json["colors"].toString()
                makeToast(activity.resources.getString(R.string.colors_extracted))
                addImagesToDB(videoCreateSettingsFragment, videoSettingsFragment)
            },
            {
                makeToast(activity.resources.getString(R.string.colors_extracting_error))
            }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    private fun addImagesToDB(
        videoCreateSettingsFragment: VideoCreativeSettingsFragment,
        videoSettingsFragment: CompanySettingsFragment
    ) {
        val id0 = (1..10000000).random()
        val id1 = (1..10000000).random()
        val id2 = (1..10000000).random()
        val id3 = (1..10000000).random()
        addImageToDB(
            id0,
            videoCreateSettingsFragment.getSpinner1Value(),
            videoCreateSettingsFragment.getToggle1Value(),
            videoCreateSettingsFragment.getSale1String(),
            videoCreateSettingsFragment.getImage1String(),
            videoCreateSettingsFragment.getDescr1String()
        )
        addImageToDB(
            id1,
            videoCreateSettingsFragment.getSpinner2Value(),
            videoCreateSettingsFragment.getToggle2Value(),
            videoCreateSettingsFragment.getSale2String(),
            videoCreateSettingsFragment.getImage2String(),
            videoCreateSettingsFragment.getDescr2String()
        )
        addImageToDB(
            id2,
            videoCreateSettingsFragment.getSpinner3Value(),
            videoCreateSettingsFragment.getToggle3Value(),
            videoCreateSettingsFragment.getSale3String(),
            videoCreateSettingsFragment.getImage3String(),
            videoCreateSettingsFragment.getDescr3String()
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

    private fun sendGenerateVideo(
        id: Int,
        animationType: String,
        clearBg: Boolean,
        sale: String,
        useTypeText: Boolean
    ) {
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

        val queue = Volley.newRequestQueue(activity)
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
            { makeToast(activity.resources.getString(R.string.part_generated)) },
            { makeToast(activity.resources.getString(R.string.part_generating_wait)) }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    private fun sendGenerateCreative() {
        makeToast(activity.resources.getString(R.string.generating_creative))
        val allIds =
            orderListIds[0].toString() + orderListIds[1].toString() + orderListIds[2].toString() + orderListIds[3].toString()
        database.child("videos").child(allIds).child("video")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        val s = dataSnapshot.value.toString().toByteArray(charset("ISO-8859-1"))

                        val filepath = "samplefile.mp4"
                        val file = File(activity.getExternalFilesDir(null).toString(), filepath)
                        if (file.exists()) {
                            file.delete()
                        }
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        FileOutputStream(file).use { fos -> fos.write(s) }

                        onSuccessesUpload.invoke(file)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        val queue = Volley.newRequestQueue(activity)
        val url =
            "https://afternoon-waters-50114.herokuapp.com/videos/1?id0=${orderListIds[0]}&id1=${orderListIds[1]}&id2=${orderListIds[2]}&id3=${orderListIds[3]}"
        checkListIds.clear()
        orderListIds.clear()
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { makeToast(activity.resources.getString(R.string.creative_generated)) },
            { makeToast(activity.resources.getString(R.string.creative_generating_wait)) }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    fun sendMockRequest() {
        makeToast(activity.resources.getString(R.string.generating_creative))

        val queue = Volley.newRequestQueue(activity)
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
                val file = File(activity.getExternalFilesDir(null).toString(), filepath)
                if (file.exists()) {
                    file.delete()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }
                FileOutputStream(file).use { fos -> fos.write(s) }

                onSuccessesUpload.invoke(file)

                makeToast(activity.resources.getString(R.string.creative_generated))
            },
            { makeToast(activity.resources.getString(R.string.creative_generating_wait)) }
        )
        stringRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    private fun makeToast(text: String) {
        val toast = Toast.makeText(
            activity.applicationContext,
            text,
            Toast.LENGTH_SHORT
        )
        toast.show()
    }

}