package io.acsint.mtngh.simswap.api

import io.acsint.mtngh.simswap.utils.gson
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

class PassportResponse {
    var passportNumber=""
    var firstName=""
    var lastName=""
    var middleName=""
    var dateOfBirth=""
    var picture:String=""

    fun toJsonString() = gson.toJson(this)!!
    companion object {
        fun fromJsonString(jsonStr: String) = gson.fromJson(jsonStr, PassportResponse::class.java)!!
    }
}

/*
 "voterIDNumber": "4281010308",
    "fullname": "ASARE SETH MIREKU",
    "dateOfBirth": "16/03/1989",
    "picture"
 */

class VoterResponse {
    var voterIDNumber=""
    var fullname=""
    var dateOfBirth=""
    var picture=""

    fun toJsonString() = gson.toJson(this)!!
    companion object {
        fun fromJsonString(jsonStr: String) = gson.fromJson(jsonStr, VoterResponse::class.java)!!
    }
}

/*
"pin": null,
    "firstName": "DANIEL",
    "lastName": "BOATENG",
    "picture"
 */

class DriverResponse {
    var pin=""
    var name=""
    var picture=""
    var certificateOfCompetence=""
    var dob = ""

    fun toJsonString() = gson.toJson(this)!!
    companion object {
        fun fromJsonString(jsonStr: String) = gson.fromJson(jsonStr, DriverResponse::class.java)!!
    }
}


interface GetDetailsFromGVIVEApi {
    @GET("SimSwap/GetPassportInformation")
    fun getPassportDetails(@Header("Authorization") authorization: String,
                           @Query("passportNumber")passportNumber:String): Single<PassportResponse>


    @GET("SimSwap/GetVoterInformation")
    fun getVoterDetails(@Header("Authorization") authorization: String,
                        @Query("voterNumber")voterNumber:String): Single<VoterResponse>

    @GET("SimSwap/GetDriverInformation")
    fun getDriverDetails(@Header("Authorization") authorization: String,
                        @Query("certificateOfCompetence")certificateOfCompetence:String, @Query("dob")dob:String)
                        : Single<DriverResponse>


}


