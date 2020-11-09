package io.acsint.mtngh.simswap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.DriverResponse
import io.acsint.mtngh.simswap.api.PassportResponse
import io.acsint.mtngh.simswap.api.VoterResponse
import io.acsint.mtngh.simswap.utils.*
import kotlinx.android.synthetic.main.activity_idverification_result.*

class IDVerificationResultActivity : AppCompatActivity() {

    lateinit var idDetailsPassport:PassportResponse
    lateinit var idDetailsVoters:VoterResponse
    lateinit var idDetailsDriver:DriverResponse


    override  fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idverification_result)

        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = resources.getString(R.string.title_activity_id_verification_result)
        }

        idDetailsPassport= PassportResponse()
        idDetailsVoters= VoterResponse()
        idDetailsDriver= DriverResponse()


        val options = RequestOptions()
        options.placeholder(R.drawable.no_image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(R.drawable.user)

        if(intent.hasExtra(ID_DETAILS_PASSPORT)){
            var id_json_passport=intent.getStringExtra(ID_DETAILS_PASSPORT)
            idDetailsPassport = PassportResponse.fromJsonString(id_json_passport)

            txt_fullname.text  = idDetailsPassport.firstName + " "+ idDetailsPassport.lastName
            txt_dob.text = idDetailsPassport.dateOfBirth
            txt_pinValue.text = "N/A"
            txt_idNumber.text = idDetailsPassport.passportNumber
            var photoImage = getBitmapFromString(idDetailsPassport.picture)
            if(photoImage!=null){
                Glide.with(this).load(photoImage)
                        .apply(options)
                        .into(user_photo);
            }


            proceed_to_swap.isEnabled=true
        }

        else if(intent.hasExtra(ID_DETAILS_VOTER)){
            var id_json_voter=intent.getStringExtra(ID_DETAILS_VOTER)
            idDetailsVoters = VoterResponse.fromJsonString(id_json_voter)

            txt_fullname.text = idDetailsVoters.fullname
            txt_dob.text = idDetailsVoters.dateOfBirth
            txt_idNumber.text= idDetailsVoters.voterIDNumber
            txt_pinValue.text = "N/A"

            var photoImage = getBitmapFromString(idDetailsVoters.picture)
            if(photoImage!=null){
                Glide.with(this).load(photoImage)
                        .apply(options)
                        .into(user_photo);
            }

            proceed_to_swap.isEnabled=true
        }
        else if(intent.hasExtra(ID_DETAILS_DRIVER)){
            var id_json_driver=intent.getStringExtra(ID_DETAILS_DRIVER)
            idDetailsDriver = DriverResponse.fromJsonString(id_json_driver)

            txt_fullname.text = idDetailsDriver.name
            txt_dob.text = idDetailsDriver.dob
            txt_idNumber.text= idDetailsDriver.certificateOfCompetence
            txt_pinValue.text = idDetailsDriver.pin

            var photoImage = getBitmapFromString(idDetailsDriver.picture)
            if(photoImage!=null){
                Glide.with(this).load(photoImage)
                        .apply(options)
                        .into(user_photo);
            }

            proceed_to_swap.isEnabled=true
        }
        else{
            proceed_to_swap.isEnabled=false
        }

        PromptForPermissions()

        proceed_to_swap.setOnClickListener {
            val intent= Intent(this@IDVerificationResultActivity,VerifyNumberActivity::class.java)
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
