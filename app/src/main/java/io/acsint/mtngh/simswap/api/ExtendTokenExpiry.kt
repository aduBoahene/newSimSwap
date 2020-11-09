package io.acsint.mtngh.simswap.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

class ExtendExpiryResponse {
    var loginData = tokenExpiryData()
}

class tokenExpiryData {
    var tokenExpiryDate = ""
    var authToken = ""
    var userId = 0
    var username = ""
    var isDefaultPass=false
}

class ExtendExpiryParams(val authToken: String)

interface ExtendExpiryApi {
    @POST("user/ExtendActiveSession")
    fun performExtension(@Header("Authorization") authorization: String, @Body params: ExtendExpiryParams): Single<ExtendExpiryResponse>
}
