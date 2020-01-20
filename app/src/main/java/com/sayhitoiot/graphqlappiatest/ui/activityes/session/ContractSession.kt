package com.sayhitoiot.graphqlappiatest.ui.activityes.session

interface ContractSession {
    interface View{
        fun message(msg: String)
        fun initSession(token: String)
        fun saveToken(token: String)
        fun resetEditTexts()
        fun resetProgressBar()
    }

    interface Presenter{
        fun isEmailValid(email: String): Boolean
        fun isPasswordValid(password: String): Boolean
        fun signInUser(email: String, password: String)
        suspend fun loginClick(email: String, password: String)
        suspend fun makeLogin(email: String, password: String): String
    }
}