package io.acsint.mtngh.simswap.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST


class SubmitRequestResponse {
    var exception:String? = null
    var success = false
    var message = ""
}

class SubmitRequestParams(val msisdn: String, val newsimserial: String, val idtype: String, val idnumber:String,
                          val reason:String, val comment:String)

interface SubmitRequestApi {
    @POST("SimSwap/SubmitRequest")
    fun submitRequest(@Body params: SubmitRequestParams): Single<SubmitRequestResponse>
}


