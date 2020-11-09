package io.acsint.mtngh.simswap.api


import io.acsint.mtngh.simswap.models.SimSummary
import io.acsint.mtngh.simswap.models.SwapResponse
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SwapApiClient {


    /* Add new article */
    @Headers("Content-Type: application/json;charset=utf-8")
    @POST("SimSwap/SubmitRequest")
    fun addArticle(@Body article: SimSummary): Observable<SwapResponse>

    companion object {

        fun create(): SwapApiClient {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://agentsimswap.mtn.com.gh/")
                    .build()

            return retrofit.create(SwapApiClient::class.java)

        }
    }

}