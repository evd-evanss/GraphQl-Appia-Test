package com.sayhitoiot.graphqlappiatest.ui.activityes.session

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.sayhitoiot.graphqlappiatest.R
import com.sayhitoiot.graphqlappiatest.ui.activityes.dashboard.DashBoardActivity
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SignupActivity() : AppCompatActivity(), ContractSession.View, CoroutineScope {
    lateinit var sessionPresenter: SessionPresenter
    lateinit var prefs: SharedPreferences
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        prefs = this.getSharedPreferences("my_token_data", Context.MODE_PRIVATE)
        sessionPresenter = SessionPresenter(this)
    }

    fun signInClick(view: View){
        val email: String = et_email.text.toString()
        val password: String = et_password.text.toString()

        launch {
            progressBar.isVisible = true
            sessionPresenter.loginClick(email, password)
        }

    }

    override fun resetEditTexts() {
        et_email?.setText("")
        et_email.requestFocus()
        et_password?.setText("")
    }

    override fun resetProgressBar() {
        progressBar.isVisible = false
    }

    override fun message(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun saveToken(token: String) {
        with (prefs.edit()) {
            putString("token", token)
            commit()
        }
    }

    override fun initSession(token: String) {
        with (prefs.edit()) {
            putString("token", token)
            apply()
        }
        val intent = Intent(this, DashBoardActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val sharedPref = this?.getSharedPreferences("my_token_data",Context.MODE_PRIVATE) ?: return
        val token = sharedPref.getString("token", "")
        if(!token.isNullOrEmpty()){
            initSession(token)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}
