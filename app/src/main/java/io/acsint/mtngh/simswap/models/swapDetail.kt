package io.acsint.mtngh.simswap.models

import java.io.Serializable

class simDetails {
    var id: Int = 0
    var name: String = ""
    var email: String = ""

    constructor(name:String,email:String){
        this.name=name
        this.email=email
    }
}