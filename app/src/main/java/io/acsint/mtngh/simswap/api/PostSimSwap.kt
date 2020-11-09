package io.acsint.mtngh.simswap.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


class PostSwapResponse {
    var exception = null
    var success = false
    var successID = 0
    var message = ""
}


    /*

     "UserId": "2",
    "Longitude": "4.8976",
    "Latitude": "-0.5647",
    "Fullname": "Pablo Guzman"

     */

class PostParams(val Msisdn: String,
                 val NewSimSerial:String,
                 val idType:String,
                 val idNumber:String,
                 val Reason:String,
                 val Comments:String,
                 val IdImage:String,
                 val RequesterImage:String,
                 val UserId:Int,
                 val Longitude:String,
                 val Latitude:String,
                 val Fullname:String
                 )

interface PostSwapApi {
    @POST("simswap/SubmitSimSwapRequest")
    fun postSimSwap(@Header( "Authorization") authorization: String,@Body params: PostParams): Single<PostSwapResponse>
}



