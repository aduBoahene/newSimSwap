package io.acsint.mtngh.simswap.api

import io.acsint.mtngh.simswap.utils.gson
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

class SwapRequest {
    var userId = 0
    var msisdn = ""
    var newSimSerial = ""
    var idType = ""
    var idNumber = ""
    var reason = ""
    var comments = ""
    var idImage = ""
    var requesterImage = ""

   constructor()

    constructor(userId:Int,msisdn:String,newSimSerial:String,idType:String,idNumber:String,reason:String
    ,comments:String,idImage:String,requesterImage:String){

        this.userId=userId
        this.msisdn=msisdn
        this.newSimSerial=newSimSerial
        this.idType = idType
        this.idNumber = idNumber
        this.reason = reason
        this.comments = comments
        this.idImage = idImage
        this.requesterImage = requesterImage
    }
}


data class SimResponse(
        var success: Boolean= false,
        var exception:  Any? = null,
        var message: String = "",
        var loginData:  Any? = null,
        var subscriberDetails: Any? = null,
        var swapRequests: List<SwapRequest>?
) {

    data class SwapRequest(
            var id: Int=0,
            var fullname: String?= "",
            var msisdn: String= "",
            var newSimSerial: String="",
            var idType: String="",
            var idNumber: String="",
            var reason: String="",
            var comments: String="",
            var longitude: String="",
            var latitude: String="",
            var attachment: Attachment?
    ) {

        data class Attachment(
                var idCardImage: IdCardImage?,
                var requesterImage: RequesterImage?
        ) {

            data class RequesterImage(
                    var fileName: String="",
                    var fileUrl: String=""
            )


            data class IdCardImage(
                    var fileName: String="",
                    var fileUrl: String=""
            )
        }


        fun toJsonString() = gson.toJson(this)!!

        companion object {
            fun fromJsonString(jsonStr: String) = gson.fromJson(jsonStr, SwapRequest::class.java)!!
        }
    }
}


/*class SimResponse {
    var success = false
    var exception = null
    var message = ""
    var loginData = null
    var subscriberDetails = null
    //var swapRequests: List<SwapRequest> = emptyList()
    var swapRequests: List<SwapRequest> = swapPersonDetails()
}*/

//class swapPersonDetails()

//class LoginParams(val userName: String, val password: String)

interface SwapListApi { //simswap/getsimswaprequests?userid=1
    @GET("simswap/getsimswaprequests?userid={id}")
    fun simswaplist(@Header ("Authorization") authorization: String,@Path("id") id: Int): Single<SimResponse>

    @GET("simswap/GetSimSwapRequestByStatus/{id}?status=Pending")
    fun swapRequestSynchronous(@Header ("Authorization") authorization: String, @Query("userId") userId: Int): Call<SimResponse>

    @GET("simswap/GetSimSwapRequestByStatus/{id}?status=Fulfilled")
    fun completedSimswaplist(@Header ("Authorization") authorization: String, @Query("userId") userId: Int): Call<SimResponse>

    @GET("simswap/GetSimSwapRequestByStatus/{id}?status=Failed")
    fun failedSimswaplist(@Header ("Authorization") authorization: String, @Query("userId") userId: Int): Call<SimResponse>

}


