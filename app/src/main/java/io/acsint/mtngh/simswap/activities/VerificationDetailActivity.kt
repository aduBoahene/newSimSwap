package io.acsint.mtngh.simswap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.*
import io.acsint.mtngh.simswap.utils.*
import kotlinx.android.synthetic.main.activity_verification_detail.*

class VerificationDetailActivity : AppCompatActivity() {

    lateinit var idDetailsPassport: PassportResponse
    lateinit var idDetailsVoters: VoterResponse
    lateinit var idDetailsDriver: DriverResponse


    override  fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_detail)

        idDetailsPassport= PassportResponse()
        idDetailsVoters= VoterResponse()
        idDetailsDriver= DriverResponse()

        if(intent.hasExtra(ID_DETAILS_PASSPORT)){
            var id_json_passport=intent.getStringExtra(ID_DETAILS_PASSPORT)
            idDetailsPassport = PassportResponse.fromJsonString(id_json_passport)

            proceedToSwapping.isEnabled=true
        }

        else if(intent.hasExtra(ID_DETAILS_VOTER)){
            var id_json_voter=intent.getStringExtra(ID_DETAILS_VOTER)
            idDetailsVoters = VoterResponse.fromJsonString(id_json_voter)
            proceedToSwapping.isEnabled=true
        }
        else if(intent.hasExtra(ID_DETAILS_DRIVER)){
            var id_json_driver=intent.getStringExtra(ID_DETAILS_DRIVER)
            idDetailsDriver = DriverResponse.fromJsonString(id_json_driver)
            proceedToSwapping.isEnabled=true
        }
        else{
            proceedToSwapping.isEnabled=false
        }

        val subscriberDetails = SubscriberDetails.fromJsonString(intent.getStringExtra(EXTRA_SUBSCRIBER_DETAILS))
        val subscriberMsisdn = intent.getStringExtra(EXTRA_SUBSCRIBER_MSISDN_VALUE)

        subscriberName.text = subscriberDetails.fullName
        subscriberDOB.text = attemptDateFormatting( subscriberDetails.dob)
        subscriberIdNumber.text = subscriberDetails.idNumber
        subscriberIdType.text = subscriberDetails.idType

        Log.d("DOB", subscriberDetails.dob)

        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
        }

        if(supportActionBar!=null){
            supportActionBar!!.title=resources.getString(R.string.title_activity_sim_swap_validate_result)
        }

        PromptForPermissions()

        proceedToSwapping.setOnClickListener {
            saveLastActiveDate()
            var intent = Intent(this, SimSwapRequestActivity::class.java)
            intent.putExtra(EXTRA_SUBSCRIBER_DETAILS,subscriberDetails.toJsonString())
            intent.putExtra(EXTRA_SUBSCRIBER_MSISDN_VALUE,subscriberMsisdn)

            if(!idDetailsPassport.firstName.isNullOrBlank()){
                intent.putExtra(ID_DETAILS_PASSPORT,idDetailsPassport.toJsonString())
            }

            if(!idDetailsVoters.fullname.isNullOrBlank()){
                intent.putExtra(ID_DETAILS_VOTER,idDetailsVoters.toJsonString())
            }

            if(!idDetailsDriver.name.isNullOrBlank()){
                intent.putExtra(ID_DETAILS_DRIVER,idDetailsDriver.toJsonString())
            }

            startActivity(intent)
        }
    }
}
