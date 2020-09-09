package com.matrix159.gamerjammer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.matrix159.gamerjammer.api.service.DiscordService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.text.Charsets.US_ASCII

class MainActivity : AppCompatActivity() {
    
    private val TAG: String = MainActivity::class.java.simpleName
    
    val db = Firebase.firestore

    val clientId = "752191831738941531"
    val redirectURI = "matrix159.gamerjammer://callback"

    private lateinit var verifier: String
    lateinit var service: DiscordService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging) // <-- this is the important line!

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://discord.com/api/")
            .addConverterFactory(MoshiConverterFactory.create().asLenient())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(httpClient.build())
            .build()

       service= retrofit.create(DiscordService::class.java)

        val storedVerifier = getPreferences(Context.MODE_PRIVATE).getString("VERIFIER", null)
        verifier = if (storedVerifier == null) {
            val secureRandom = SecureRandom()
            val code = ByteArray(32)
            secureRandom.nextBytes(code)
            Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        } else {
            storedVerifier
        }

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString("VERIFIER", verifier)
            apply()
        }
    }

    override fun onResume() {
        super.onResume()

        val bytes: ByteArray = verifier.toByteArray(US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(bytes, 0, bytes.size)
        val digest = messageDigest.digest()
        val challenge = Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

        val uri = intent.data

        if (uri != null && uri.toString().startsWith(redirectURI) && uri.toString().contains("code")) {
            val code = uri.getQueryParameter("code")
            code?.let {
                service.getToken(clientId, "authorization_code", code, redirectURI, "identify email connections", verifier)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({
                        Log.d(TAG, "Call worked")
                    }, {
                        Log.e(TAG, it.message ?: "")
                    })
            }

            Toast.makeText(this, "yay!", Toast.LENGTH_SHORT).show()
        } else {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/api/oauth2/authorize?client_id=${clientId}&redirect_uri=${redirectURI}" +
                    "&response_type=code&scope=identify%20email%20connections&code_challenge=${challenge}&code_challenge_method=S256"))
            startActivity(intent)
        }
    }
}