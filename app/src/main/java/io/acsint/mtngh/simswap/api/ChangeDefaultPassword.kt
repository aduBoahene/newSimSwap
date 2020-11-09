package io.acsint.mtngh.simswap.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

class ResetPassResponse {
    var exception = null
    var loginData = null
    var success = false
    var subscriberDetails = null
    var message = ""

}

class ChangeDefaultPassParams(val username: String, val newpassword: String, val oldpassword:String)

interface ChangeDefaultPasswordApi {
    @POST("simswap/changepassword")
    fun performPassReset(@Header("Authorization") authorization: String, @Body params: ChangeDefaultPassParams): Single<ResetPassResponse>
}


