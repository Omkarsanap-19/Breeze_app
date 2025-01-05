package com.example.breeze

import NewsResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface API_interface {

    @GET("v2/trendings?rapidapi-key=$API_KEY")
    fun getHeadlines(
        @Query("language") language: String = "en",
        @Query("country") country: String ,
        @Query("topic") topic : String ,
        @Query("page") page: Int = 1
    ): Call<NewsResponse>

    @GET("v2/search/articles?rapidapi-key=$API_KEY")
    fun getSearch(
        @Query("query") query:String,
        @Query("language") language: String = "en"

    ): Call<NewsResponse>

}