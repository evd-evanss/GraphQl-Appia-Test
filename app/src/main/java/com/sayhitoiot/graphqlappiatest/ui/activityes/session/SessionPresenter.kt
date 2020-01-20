package com.sayhitoiot.graphqlappiatest.ui.activityes.session

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.example.graphql.SignInMutation
import okhttp3.OkHttpClient
import java.lang.Exception


class SessionPresenter (val view: ContractSession.View):ContractSession.Presenter{
    private val BASE_URL = "https://appia-api-candidates.herokuapp.com/graphql"
    val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
    var token: String = ""

    override fun isEmailValid(email: String):Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun isPasswordValid(password: String):Boolean {
        if(password.length > 5){
            return true
        }
        return false
    }

    suspend override fun loginClick(email: String, password: String) {

        if(!isEmailValid(email)||email.isEmpty()){
            view.message("Digite um email válido!")
            view.resetProgressBar()
            return
        }
        if(!isPasswordValid(password)||password.isEmpty()){
            view.message("A senha deve conter no mínimo 6 caracteres!")
            view.resetProgressBar()
            return
        }

        token = makeLogin(email, password)
        if(!token.contentEquals("fail")){
            view.initSession(token)
        }
        //signInUser(email, password)
        //view.message("Login Efetuado com sucesso!")

    }

    override fun signInUser(email: String, password: String){

        val apolloClient: ApolloClient = ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .build()

        apolloClient.mutate(
            SignInMutation.builder()
                .email(email)
                .password(password).build()).enqueue(object: ApolloCall.Callback<SignInMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.i("fail", e.toString())
            }

            override fun onResponse(response: Response<SignInMutation.Data>) {
                token = response.data()?.signIn()?.token().toString()

                Log.i("sucess", response.data()?.signIn()?.token().toString())
                view.saveToken(response.data()?.signIn()?.token().toString())
                view.message(token)

            }
        })

    }

    override suspend fun makeLogin(email: String, password: String): String {
        val apolloClient: ApolloClient = ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .build()

        val makeSign = SignInMutation.builder()
            .email(email)
            .password(password).build()

        val signData =
                apolloClient.mutate(makeSign).toDeferred().await()
        // Salvo token em SharedPreferences )

        var token_received = ""
        try {
            token_received = signData.data()?.signIn()?.token()!!
            return token_received
        }catch (e: Exception){
            view.message("Falha no login!")
            view.resetEditTexts()
            view.resetProgressBar()
        }
        return "fail"

    }
}