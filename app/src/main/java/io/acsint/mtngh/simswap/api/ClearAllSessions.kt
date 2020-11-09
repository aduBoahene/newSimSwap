package io.acsint.mtngh.simswap.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

class ClearSessionsResponse {
    var exception = null
    var loginData = null
    var success = false
    var subscriberDetails = null
    var message = ""
}

class ClearSessionsParams(val username: String,val password:String)

interface ClearSessionsApi {
    @POST("user/Clearallactivesessions")
    fun performClearSession(@Body params: ClearSessionsParams): Single<ClearSessionsResponse>
}


