package com.example.gymsbond

import android.app.Notification
import android.provider.SyncStateContract
import com.example.gymsbond.Constants.Constants
import com.example.gymsbond.Constants.Constants.Companion.BASE_URL
import com.example.gymsbond.`interface`.NotificationApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class RetrofitInstance {

    companion object{
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api by lazy {
            retrofit.create(NotificationApi::class.java)
        }
    }
}