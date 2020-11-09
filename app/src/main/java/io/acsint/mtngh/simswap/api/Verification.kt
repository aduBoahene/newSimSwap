package io.acsint.mtngh.simswap.api

import io.acsint.mtngh.simswap.utils.gson
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

class SubscriberDetails {
    var msisdn = ""
    var fullName = ""
    var dob = ""
    var idType = ""
    var idNumber = ""

    fun toJsonString() = gson.toJson(this)!!

    companion object {
        fun fromJsonString(jsonStr: String) = gson.fromJson(jsonStr, SubscriberDetails::class.java)!!
    }
}

class VerificationResponse {
    var exception: String? = null
    var success = false
    var message = ""
    var subscriberDetails = SubscriberDetails()
}

class VerificationParams(val Phonenumber: String)

interface VerificationApi {
    //@Headers({"Authorization: e019b1fb-4dce-48e6-99da-9f08a626f67c"})
    @POST("VerifyUser/Verify")
    fun verifyNumber(@Header("Authorization") authorization: String, @Body params: VerificationParams): Single<VerificationResponse>

    @POST("VerifyUser/Verify")
    fun verifyNumber(@Header("Authorization") authorization: String, @Query("phoneNumber") phoneNumber:String): Single<VerificationResponse>
}

const val EXTRA_SUBSCRIBER_DETAILS = "EXTRA_SUBSCRIBER_DETAILS"


