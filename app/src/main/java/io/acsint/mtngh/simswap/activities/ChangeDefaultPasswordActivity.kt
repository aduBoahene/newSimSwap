package io.acsint.mtngh.simswap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.ChangeDefaultPassParams
import io.acsint.mtngh.simswap.api.ChangeDefaultPasswordApi
import io.acsint.mtngh.simswap.utils.getUserName
import io.acsint.mtngh.simswap.utils.mainServerRetrofit
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_first_time_login.*
import io.acsint.mtngh.simswap.utils.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast


class ChangeDefaultPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time_login)



       sendBtn.setOnClickListener {
           val newPassword = new_password.text.toString().trim()
           val confirmNewPasssword = confirm_new_password.text.toString().trim()
           val oldPasssword = old_password.text.toString().trim()


           if(oldPasssword.isNullOrBlank()){
               toast("Please provide old password")
               return@setOnClickListener
           }

           if(newPassword.isNullOrBlank()){
               toast("Please provide new password")
               return@setOnClickListener
           }

           if(confirmNewPasssword.isNullOrBlank()){
               toast("Please provide confirm password")
               return@setOnClickListener
           }

           if(newPassword!=confirmNewPasssword){
               toast("Passwords do not match")
               return@setOnClickListener
           }

           if(newPassword.length<8){
               toast("Password should have more than eight characters")
               return@setOnClickListener
           }

           if(! "^(?=.*\\d)(?=.*[a-zA-Z])([a-zA-Z0-9]{8})\$".toRegex().matches(newPassword)){
               toast("Password should be eight alphanumeric characters")
               return@setOnClickListener
           }

           if(oldPasssword==newPassword){
               toast("New password should not be equal to old password")
               return@setOnClickListener
           }

           getResetPasswordApi()
                   .performPassReset(getAuthToken(),ChangeDefaultPassParams(getUserName(),newPassword,oldPasssword))
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe({
                       if(it.success){
                           storeIsDefaultPass(false)
                           Handler().postDelayed({
                               var mainIntent = Intent(this@ChangeDefaultPasswordActivity, MainActivity::class.java)
                               //saveExpiryDate(tokenExpiryDate)
                               mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                               startActivity(mainIntent)
                           }, 500)



                       }else {
                           toast("Username or password invalid {" + it.message + "}")
                       }
                   },{
                       toast("Enter a username or password missing {" + it.message + "}")
                   })
       }



    }



    private fun saveAgentIdToPrefs(agentId: Int) {
        defaultSharedPreferences.edit().putInt(AGENT_ID, agentId).apply()
    }

    private fun saveExpiryDate(tokenExpiry: Long) {
        defaultSharedPreferences.edit().putLong(KEY_TOKEN_EXPIRY, tokenExpiry).apply()
    }

    private fun getResetPasswordApi() = mainServerRetrofit.create(ChangeDefaultPasswordApi::class.java)!!


}
