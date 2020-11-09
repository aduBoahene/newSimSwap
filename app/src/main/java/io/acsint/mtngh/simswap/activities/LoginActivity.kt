package io.acsint.mtngh.simswap.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.acsint.mtngh.simswap.*
import io.acsint.mtngh.simswap.api.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import io.acsint.mtngh.simswap.utils.*
import java.util.*

class LoginActivity : AppCompatActivity() {
    val KEY_TOKEN_EXPIRY: String = "KEY_TOKEN_EXPIRY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //set font to custom MTN font
        val customFont = Typeface.createFromAsset(assets, "MTNBrighterSans-Bold.ttf")
        //txtMessage.typeface = typeface
        loginwithsms.typeface = customFont
        loginBtn.typeface=customFont

        if (supportActionBar != null) {
            supportActionBar!!.title = resources.getString(R.string.login_activity_label)
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffb300")))
        }

        val tokenExpiry = getTokenExpiry()
        val isDefaultPass=getIsDefaultPass()
       if (tokenExpiry != -1L && Date().time < tokenExpiry) {

            var mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
           if(isDefaultPass){
               mainIntent = Intent(this@LoginActivity, ChangeDefaultPasswordActivity::class.java)
           }
            mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(mainIntent)
            return
        }

        loginBtn.setOnClickListener {
            //TODO:Check if this is first time log in
            val inputUserName = username.text.toString().trim()
            val inputPassword = password.text.toString().trim()

            if (inputPassword.isNotBlank() && inputPassword.isNotBlank()) {
                var p  = indeterminateProgressDialog("Loading…")
                p.show()

                getLoginApi()
                        .performLogin(LoginParams(inputUserName, inputPassword))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            p.dismiss()
                            if(it.success&&it.loginData!=null){
                                saveUserNameToPrefs(it.loginData.username)
                                saveAuthTokenToPrefs(it.loginData.authToken)
                                storeIsDefaultPass(it.loginData.isDefaultPass)
                                val simpleDate = SimpleDateFormat("yyyy-MM-dd HH:mm")
                                var dateString = it.loginData.tokenExpiryDate.substring(0, 16).replace("T", " ")
                                var tokenExpiryDate = simpleDate.parse(dateString).time
                                var UserId = it.loginData.userId
                                saveAgentIdToPrefs(it.loginData.userId)
                                saveExpiryDate(tokenExpiryDate)

                                saveLastActiveDate()

                            }
                            if(it.success&&it.loginData!=null&&it.loginData.isDefaultPass){

                                val intent = Intent(this@LoginActivity, ChangeDefaultPasswordActivity::class.java)
                                val alarm=AppActiveCheckAlarm()
                                setAlarm(this)
                                startActivity(intent)
                            }
                            else if (it.success&&it.loginData!=null && it.loginData.isDefaultPass==false) {
                                    var mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                                    mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK


                                    startActivity(mainIntent)
                            } else {
                                toast("Username or password invalid: {" + it.message + "}")
                            }
                        },{
                            p.dismiss()
                            toast("Error Connecting to API server: {" + it.message + "}")
                        })
            } else {
                toast("Enter a username or password missing")
            }
        }

        loginwithsms.setOnClickListener {
            var intent = Intent(this@LoginActivity, LoginWithSmsActivity::class.java)
            startActivity(intent)
        }

        /*clear_all_sessions.setOnClickListener {
            var intent = Intent(this@LoginActivity, ClearAllSessionsActivity::class.java)
            startActivity(intent)
        }*/


    }

    private fun saveAuthTokenToPrefs(authToken: String) {
        defaultSharedPreferences.edit().putString(KEY_AUTH_TOKEN, authToken).apply()
    }

    private fun saveUserNameToPrefs(userName: String) {
        defaultSharedPreferences.edit().putString(USER_NAME, userName).apply()
    }

    private fun saveAgentIdToPrefs(agentId: Int) {
        defaultSharedPreferences.edit().putInt(AGENT_ID, agentId).apply()
    }


    private fun saveExpiryDate(tokenExpiry: Long) {
        defaultSharedPreferences.edit().putLong(KEY_TOKEN_EXPIRY, tokenExpiry).apply()
    }

    //const val KEY_TOKEN_EXPIRY = "KEY_TOKEN_EXPIRY"

    private fun getLoginApi() = mainServerRetrofit.create(LoginApi::class.java)!!
    //private fun getLogoutApi() = mainServerRetrofit.create(LogoutApi::class.java)!!


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_logout_from_all_devices, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_logout) {
            val inputUserName = username.text.toString().trim()
            val inputPassword = password.text.toString().trim()

            getClearSessionApi()
                    .performClearSession(ClearSessionsParams(inputUserName, inputPassword))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if(it.success){
                            Toast.makeText(this,"You have succesfully logged out of all devices", Toast.LENGTH_LONG).show()
                        }
                    },{
                        Toast.makeText(this,"Error Connecting to API server: {" + it.message + "}", Toast.LENGTH_LONG).show()
                    })

            return true
        }

        return false
    }



    private fun getClearSessionApi() = mainServerRetrofit.create(ClearSessionsApi::class.java)!!

}
