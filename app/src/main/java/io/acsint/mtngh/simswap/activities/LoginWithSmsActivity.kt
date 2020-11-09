package io.acsint.mtngh.simswap.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.View
//import com.example.pat.simswap.R
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.LoginWithPhoneParams
import io.acsint.mtngh.simswap.api.PhoneLoginApi
import io.acsint.mtngh.simswap.utils.AGENT_ID
import io.acsint.mtngh.simswap.utils.KEY_AUTH_TOKEN
import io.acsint.mtngh.simswap.utils.mainServerRetrofit
import io.reactivex.android.schedulers.AndroidSchedulers
import org.jetbrains.anko.indeterminateProgressDialog
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_login_with_sms.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import io.acsint.mtngh.simswap.utils.InternationalizePhoneNumber
import android.widget.Toast

class LoginWithSmsActivity : AppCompatActivity() {

    val KEY_TOKEN_EXPIRY: String = "KEY_TOKEN_EXPIRY"
    lateinit var msisdnToBeSent: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_with_sms)

        //set controls to invisible
        pin.visibility = View.INVISIBLE
        loginwithpin.visibility = View.INVISIBLE

        requestpin.setOnClickListener {

            var p  = indeterminateProgressDialog("Loading…")
            p.show()
            //do http call
            msisdnToBeSent = msisdn.text.toString()
            if (msisdnToBeSent.isNotBlank() && msisdnToBeSent.length == 10) {
                msisdnToBeSent = msisdn.text.toString().InternationalizePhoneNumber()
                getLogInWithSmsApi()
                        .phoneLogin(msisdnToBeSent)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            p.dismiss()
                            var firstTime:Boolean=true
                                if (it.success) {
                                    //show hidden controls
                                    pin.visibility = View.VISIBLE
                                    loginwithpin.visibility = View.VISIBLE
                                } else {
                                    toast("Error:" + it.message)
                                }

                        }, {
                            p.dismiss()
                            toast("An Error Occured")
                        })
            }

        }

        loginwithpin.setOnClickListener {
            var p  = indeterminateProgressDialog("Loading…")
            p.show()
            val pin = pin.text.toString()
            msisdnToBeSent=msisdnToBeSent.InternationalizePhoneNumber()
            try {
                var request = LoginWithPhoneParams(msisdnToBeSent, pin)
                var api = getLogInWithSmsApi()
                api.performPinLogin(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            p.dismiss()
                            try {
                                if (it.success && it.loginData !=null) {
                                    //save auth in shared preference
                                   // toast(it!!.loginData!!.tokenExpiryDate!!)
                                    Toast.makeText(this,it!!.loginData!!.tokenExpiryDate!!,Toast.LENGTH_LONG).show()
                                    val simpleDate = SimpleDateFormat("yyyy-MM-dd HH:mm")
                                    var dateString = it.loginData!!.tokenExpiryDate!!.substring(0, 16).replace("T", " ")
                                    var tokenExpiryDate = simpleDate.parse(dateString).time
                                    var UserId = it.loginData!!.userId
                                    saveAuthTokenToPrefs(it.loginData!!.authToken!!)
                                    saveAgentIdToPrefs(it.loginData!!.userId)
                                    saveExpiryDate(tokenExpiryDate)
                                    var mainIntent = Intent(this@LoginWithSmsActivity, MainActivity::class.java)
                                    mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(mainIntent)
                                    //toast("Login Succesfull")
                                } else {
                                    toast("Error:" + it.message)
                                }
                            } catch (ex: Exception) {
                                Log.e("error1",ex.toString())
                            }
                        }, {
                            p.dismiss()
                            toast("An Error occured" + it.message)
                        })
            } catch (ex: Exception) {
                p.dismiss()
                Log.e("error", ex.toString())
            }


        }
    }

    private fun saveAuthTokenToPrefs(authToken: String) {
        defaultSharedPreferences.edit().putString(KEY_AUTH_TOKEN, authToken).apply()
    }

    private fun saveAgentIdToPrefs(agentId: Int) {
        defaultSharedPreferences.edit().putInt(AGENT_ID, agentId).apply()
    }


    private fun saveExpiryDate(tokenExpiry: Long) {
        defaultSharedPreferences.edit().putLong(KEY_TOKEN_EXPIRY, tokenExpiry).apply()
    }


    private fun getLogInWithSmsApi() = mainServerRetrofit.create(PhoneLoginApi::class.java)!!

}
