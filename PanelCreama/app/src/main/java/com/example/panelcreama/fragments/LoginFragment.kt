package com.example.panelcreama.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.panelcreama.MainActivity
import com.example.panelcreama.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginFragment : Fragment() {

    companion object {
        const val TAG = "login_fragment"
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    private val database = FirebaseDatabase.getInstance().reference.root
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var signIn: Button
    private var listener: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.login_fragment, container, false)
        username = view.findViewById(R.id.edit_login)
        password = view.findViewById(R.id.edit_password)
        signIn = view.findViewById(R.id.login_btn)
        signIn.setOnClickListener {
            login()
        }
        return view
    }

    fun setCallback(listener: ((String) -> Unit)) {
        this.listener = listener
    }

    private fun login() {
        if (username.text.isBlank() || password.text.isBlank()) {
            makeToast(resources.getString(R.string.empty_error))
        } else {
            database.child("users").child(username.text.toString())
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value != null && password.text.toString() != dataSnapshot.value.toString()) {
                            makeToast(resources.getString(R.string.similar_password_error))
                        } else {
                            database.child("users").child(username.text.toString())
                                .setValue(password.text.toString())
                                .addOnCompleteListener {
                                    MainActivity.username = username.text.toString()
                                    listener?.invoke(username.text.toString())
                                    makeToast(resources.getString(R.string.sign_in_up_success))
                                }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
    }

    private fun makeToast(text: String) {
        val toast = Toast.makeText(
            context,
            text,
            Toast.LENGTH_SHORT
        )
        toast.show()
    }

}