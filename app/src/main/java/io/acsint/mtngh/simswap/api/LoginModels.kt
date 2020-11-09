package io.acsint.mtngh.simswap.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

class LoginData {
    var tokenExpiryDate = ""
    var authToken = ""
    var userId = 0
    var username = ""
    var isDefaultPass=false
}

class LoginResponse {
    var exception = null
    var success = false
    var subscriberDetails = null
    var message = ""
    var loginData = LoginData()
}

class LoginParams(val userName: String, val password: String)

interface LoginApi {
    @POST("User/LogIn")
    fun performLogin(@Body params: LoginParams): Single<LoginResponse>
}


