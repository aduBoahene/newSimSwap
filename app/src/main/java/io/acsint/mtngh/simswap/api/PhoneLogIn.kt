package io.acsint.mtngh.simswap.api

import io.reactivex.Single
import retrofit2.http.*

class PhoneLoginData {
    var tokenExpiryDate:String? = ""
    var authToken:String? = ""
    var userId:Int = 0
    var username:String? = null
}

class LoginWithPhoneResponse {
    var exception = null
    var success = false
    var subscriberDetails = null
    var message :String = ""
    var PhoneLoginData = PhoneLoginData()
    var swapRequests:Any? = null
}

class LoginWithPinResponse {
    var exception:Any?= null
    var success:Boolean = false
    var subscriberDetails:Any? = null
    var message:String? = ""
    var loginData:PhoneLoginData? = null
    var swapRequests:Any? = null
}

class LoginWithPhoneParams(val Msisdn: String, val UserPin: String)

interface PhoneLoginApi{
    @GET("user/RequestLoginTokenForPhone")
    fun phoneLogin(@Query("msisdn") msisdn:String): Single<LoginWithPhoneResponse>

    @POST("user/PinCodeLogIn")
    fun performPinLogin(@Body params: LoginWithPhoneParams): Single<LoginWithPinResponse>
}




