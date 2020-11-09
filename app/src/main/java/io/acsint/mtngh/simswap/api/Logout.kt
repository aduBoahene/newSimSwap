package io.acsint.mtngh.simswap.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

class LogoutResponse {
}

class LogoutParams(val authToken: String)

interface LogoutApi {
    @POST("user/logout")
    fun performLogout(@Header("Authorization") authorization: String): Single<String>
}


