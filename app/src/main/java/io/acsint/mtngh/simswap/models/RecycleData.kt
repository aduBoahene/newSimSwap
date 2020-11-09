package io.acsint.mtngh.simswap.models

class SData() {
    var imageUrl: String = ""
    var msisdn: String = ""
    var idType: String = ""
    var idNumber: String = ""
    var serial: String = ""

    constructor(msisdn:String,idtype:String,idnumber:String,serial:String) : this() {
        this.msisdn = msisdn
        this.idType = idtype
        this.idNumber = idnumber
        this.serial = serial
    }


}