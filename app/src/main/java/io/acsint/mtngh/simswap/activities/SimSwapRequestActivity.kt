package io.acsint.mtngh.simswap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.util.Log
import android.widget.*
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.*
import io.acsint.mtngh.simswap.utils.*
import kotlinx.android.synthetic.main.activity_sim_swap_request.*


class SimSwapRequestActivity : AppCompatActivity() {
    lateinit var idDetailsPassport: PassportResponse
    lateinit var idDetailsVoters: VoterResponse

    lateinit var idDetailsDriver: DriverResponse

    lateinit var idTypeDropdown: Spinner

    override  fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sim_swap_request)

        val subscriberDetails = SubscriberDetails.fromJsonString(intent.getStringExtra(EXTRA_SUBSCRIBER_DETAILS))
        val subscriberMsisdn = intent.getStringExtra(EXTRA_SUBSCRIBER_MSISDN_VALUE)

        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = resources.getString(R.string.title_activity_sim_swap_request)
        }

        val phoneNumberEditText = findViewById<TextView>(R.id.phone)
        phoneNumberEditText.setText(subscriberMsisdn)

        idTypeDropdown = findViewById(R.id.idtype) as Spinner
        var options = arrayOf("NHIS", "PASSPORT", "VOTERS", "NATIONAL ID", "DRIVER'S LICENSE")
        idTypeDropdown.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options)

        idDetailsPassport= PassportResponse()
        idDetailsVoters= VoterResponse()

        if(intent.hasExtra(ID_DETAILS_PASSPORT)){
            var id_json_passport=intent.getStringExtra(ID_DETAILS_PASSPORT)
            idDetailsPassport = PassportResponse.fromJsonString(id_json_passport)
            idNumber.setText(idDetailsPassport.passportNumber)
            idTypeDropdown.setSelection(1)
            idTypeDropdown.isEnabled=false
            idNumber.isEnabled=false
            proceed.isEnabled=true
        }
        else if(intent.hasExtra(ID_DETAILS_VOTER)){
            var id_json_voter=intent.getStringExtra(ID_DETAILS_VOTER)
            idDetailsVoters = VoterResponse.fromJsonString(id_json_voter)
            idNumber.setText(idDetailsVoters.voterIDNumber)
            idTypeDropdown.setSelection(2)
            idTypeDropdown.isEnabled=false
            idNumber.isEnabled=false
            proceed.isEnabled=true
        }
        else if(intent.hasExtra(ID_DETAILS_DRIVER)){
            var id_json_driver=intent.getStringExtra(ID_DETAILS_DRIVER)
            idDetailsDriver = DriverResponse.fromJsonString(id_json_driver)
            idNumber.setText(idDetailsDriver.certificateOfCompetence)
            idTypeDropdown.setSelection(4)
            idTypeDropdown.isEnabled=false
            idNumber.isEnabled=false
            proceed.isEnabled=true
        }
        else{
            proceed.isEnabled=false
        }

        PromptForPermissions()

        proceed.setOnClickListener {
            saveLastActiveDate()
            val phoneNumber = findViewById(R.id.phone) as TextView
            val serial = findViewById<TextInputEditText>(R.id.serialNumber)
            val serialConfirm = findViewById<TextInputEditText>(R.id.confirmSerialNumber)
            val idNumber = findViewById<TextInputEditText>(R.id.idNumber)
            val reason = findViewById<TextInputEditText>(R.id.reason)
            val comment = findViewById<TextInputEditText>(R.id.comment)
            var idType = idTypeDropdown.selectedItem.toString()

            var validationResponse = isValidRequest(phoneNumber.text.toString(),
                    serial.text.toString(), serialConfirm.text.toString(),
                    idType, idNumber.text.toString(), comment.text.toString(), reason.text.toString())
            if (validationResponse == 0L) {
                val intent = Intent(this@SimSwapRequestActivity, SimSwapImageAttachmentActivity::class.java)
                Log.d("msisdn", phoneNumber.text.toString())
                intent.putExtra("User Phone", phoneNumber.text.toString())
                intent.putExtra("serial", serial.text.toString())
                intent.putExtra("idNumber", idNumber.text.toString())
                intent.putExtra("reason", reason.text.toString())
                intent.putExtra("comment", comment.text.toString())
                intent.putExtra("idtype", idType)
                intent.putExtra(EXTRA_SUBSCRIBER_DETAILS, subscriberDetails.toJsonString())
                intent.putExtra(EXTRA_SUBSCRIBER_MSISDN_VALUE, subscriberMsisdn)

                //clear all fields
                phoneNumber.setText("")
                serial.setText("")
                idNumber.setText("")
                reason.setText("")
                comment.setText("")
                serialConfirm.setText((""))

                startActivity(intent)
            } else {
                //if (validationResponse == 1L) {
                    //Toast.makeText(applicationContext, "Invalid " + idType, Toast.LENGTH_LONG).show()
               // } else
                if (validationResponse == 2L) {
                    Toast.makeText(applicationContext, "Subscriber Phone Number must be 10 digits", Toast.LENGTH_LONG).show()
                } else if (validationResponse == 3L) {
                    Toast.makeText(applicationContext, "SIM Card Serial Number must be 12 digits", Toast.LENGTH_LONG).show()
                } else if (validationResponse == 4L) {
                    Toast.makeText(applicationContext, "SIM Card Serial Numbers Do not Match", Toast.LENGTH_LONG).show()
                }else if (validationResponse == 5L) {
                    Toast.makeText(applicationContext, "Invalid Comments", Toast.LENGTH_LONG).show()
                }else if (validationResponse == 6L) {
                    Toast.makeText(applicationContext, "Invalid Reason", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun isValidRequest(msisdn: String, serial: String, serialConfirm: String, idType: String, idNum: String,
                               comment: String, reason:String): Long {

        if (!verifyMsisdn(msisdn)) {
            return 2
        }

        if (!verifySerialNumber(serial, serialConfirm)) {
            return 3
        }

        if (!serial.equals(serialConfirm)) {
            return 4
        }

        if (!verifyIdNumber(idType, idNum)) {
            return 1
        }

        if (!verifyComment(comment)) {
            return 5
        }

        if (!verifyComment(reason)) {
            return 6
        }

        return 0
    }

    private fun verifyIdNumber(idType: String, idNumber: String): Boolean {
        if (idType.isNullOrBlank() || idType.length < 2 || idType.length > 30) {
            return false
        }

        if (idNumber.isNullOrBlank() || idNumber.length < 5 || idNumber.length > 20) {
            return false
        }

        //"NHIS", "PASSPORT", "VOTERS", "NATIONAL ID", "DRIVERS LISCENCE"
        if (idType == "NHIS" && !idNumber.matches(Regex("^\\d{8}"))) {
            return false
        }

        if (idType == "PASSPORT" && !idNumber.matches(Regex("[Gg]\\d{7}|[Hh]\\d{7}"))) {
            return false
        }

        if (idType == "VOTERS" && !idNumber.matches(Regex("^\\d{8}[A-Za-z]{2}\$"))
                && !idNumber.matches(Regex("^\\d{10}\$"))) {
            return false
        }
        if (idType == "NATIONAL ID" && !idNumber.matches(Regex("^[Cc]\\d{12}|[Pp]\\d{12}|[rR]\\d{12}|[A-Za-z]{3}\\-\\d{9}\\-\\d{1}\$"))) {
            return false
        }
        //if (idType == "DRIVER'S LICENSE" ){
            //&& !idNumber.matches(Regex("^[A-Za-z]{4}\\d{10}|[A-Za-z]{4}-\\d{6}-\\d{2}-\\d{2}|[A-Za-z]{3}-\\d{8}-\\d{5}|[A-Za-z]{3}\\d{8}\\d{5}|\\d{6}\$"))) {
          //  return false
        //}

        return true
    }

    private fun verifyMsisdn(msisdn: String): Boolean {
        if (msisdn.isNullOrBlank() || msisdn.length < 9 || msisdn.length > 15) {
            return false
        }

        for (ch in msisdn) {
            if (!Character.isDigit(ch)) {
                return false
            }
        }

        return true
    }

    private fun verifyComment(comment: String): Boolean {
        if (comment.isNullOrBlank() || comment.length < 5 || comment.length > 400) {
            return false
        }

        return true
    }


    private fun verifySerialNumber(serial: String, serialConfirm: String): Boolean {
        if (serial.isNullOrBlank() || serial.length < 12 || serial.length > 12) {
            return false
        }

        for (ch in serial) {
            if (!Character.isDigit(ch)) {
                return false
            }
        }

        return true
    }

}
