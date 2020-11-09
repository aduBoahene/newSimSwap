package io.acsint.mtngh.simswap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.acsint.mtngh.simswap.*
import io.acsint.mtngh.simswap.api.*
import io.acsint.mtngh.simswap.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_verify_number.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast

class VerifyNumberActivity : AppCompatActivity() {

    lateinit var idDetailsPassport: PassportResponse
    lateinit var idDetailsVoters: VoterResponse
    lateinit var idDetailsDriver:DriverResponse

    override  fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_number)

        if(supportActionBar!=null){
            supportActionBar!!.title=resources.getString(R.string.title_activity_sim_swap_validate_request)
        }

        idDetailsPassport= PassportResponse()
        idDetailsVoters= VoterResponse()
        idDetailsDriver= DriverResponse()

        if(intent.hasExtra(ID_DETAILS_PASSPORT)){
            var id_json_passport=intent.getStringExtra(ID_DETAILS_PASSPORT)
            idDetailsPassport = PassportResponse.fromJsonString(id_json_passport)

            verifyBtn.isEnabled=true
        }

        else if(intent.hasExtra(ID_DETAILS_VOTER)){
            var id_json_voter=intent.getStringExtra(ID_DETAILS_VOTER)
            idDetailsVoters = VoterResponse.fromJsonString(id_json_voter)
            verifyBtn.isEnabled=true
        }
        else if(intent.hasExtra(ID_DETAILS_DRIVER)){
            var id_json_driver=intent.getStringExtra(ID_DETAILS_DRIVER)
            idDetailsDriver = DriverResponse.fromJsonString(id_json_driver)
            verifyBtn.isEnabled=true
        }
        else{
            verifyBtn.isEnabled=false
        }

        PromptForPermissions()

       verifyBtn.setOnClickListener {
           saveLastActiveDate()

           val msisdnToBeVerified = verifyMsisdn.text.toString().trim()
           if (msisdnToBeVerified.isNotBlank()){
               var p  = indeterminateProgressDialog("Loading…")
               p.show()

               getVerificationApi()
                       .verifyNumber(getAuthToken(), msisdnToBeVerified)
                       .subscribeOn(Schedulers.io())
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribe({
                           p.dismiss()
                           if (it.success) {
                               //
                               p.dismiss()
                               val intent = Intent(this@VerifyNumberActivity, VerificationDetailActivity::class.java)
                               intent.putExtra(EXTRA_SUBSCRIBER_DETAILS,it.subscriberDetails.toJsonString())
                               intent.putExtra(EXTRA_SUBSCRIBER_MSISDN_VALUE,msisdnToBeVerified)

                               //clear fields
                               verifyMsisdn.setText("")

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
                           } else {
                               toast("Error Verifying Number {" + it.message + "}")
                           }
                       },{
                           p.dismiss()
                           toast("An Error Occured")
                       })
           }
       }

    }

    private fun getVerificationApi() = mainServerRetrofit.create(VerificationApi::class.java)!!

}
