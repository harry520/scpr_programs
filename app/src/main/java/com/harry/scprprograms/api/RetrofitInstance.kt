package com.harry.scprprograms.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private const val BASE_URL =
            "https://omny.fm/api/orgs/acc8cc57-ff7c-44c5-9bd6-ab0900fbdc43/"
        private val retrofit by lazy {
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val api: ConsumerApi by lazy { retrofit.create(ConsumerApi::class.java) }
    }
}