package io.acsint.mtngh.simswap.models

data class SimSummary(
        val Msisdn:String,

        val NewSimSerial: String,

        val IdType: String,

        val IdNumber: String,

        val Reason:  String,

        val Comments: String,

        val IdImage: String,

        val RequesterImage: String
    )