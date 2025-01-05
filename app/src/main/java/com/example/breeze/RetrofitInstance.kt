package com.example.breeze

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//  api_2 = 4c48fd296amsh4ebd6fe8d72915dp1de3c7jsnf61e78770e40
// api_1 ="9f3be0ac51msh3cb70aff9b2a468p12941ajsn4a14566a4f47"
// api_3 = fcfc92a7b5mshb20024ab92505a3p109edfjsn7870b299d1ba

const val base_url="https://news-api14.p.rapidapi.com/"
const val API_KEY ="9f3be0ac51msh3cb70aff9b2a468p12941ajsn4a14566a4f47"
// /v2/search/articles?query=nasa&language=en
object RetrofitInstance {
    val newsInstance : API_interface
    init {

        val retrofit= Retrofit.Builder()
            .baseUrl(base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        newsInstance = retrofit.create(API_interface::class.java)
    }

}