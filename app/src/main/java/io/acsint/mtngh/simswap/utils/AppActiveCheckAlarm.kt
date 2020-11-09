package io.acsint.mtngh.simswap.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.PowerManager
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.widget.Toast
import io.acsint.mtngh.simswap.activities.LoginActivity
import io.acsint.mtngh.simswap.activities.VerificationDetailActivity
import io.acsint.mtngh.simswap.api.*
import io.acsint.mtngh.simswap.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import org.jetbrains.anko.support.v4.toast
import java.util.*


class AppActiveCheckAlarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "login_alarm")
        wl.acquire()

        // Put here YOUR code.
        //Toast.makeText(context, "Alarm code reached", Toast.LENGTH_LONG).show()
        checkAndLogOutIfInActive(context)
        wl.release()
    }

    private fun checkAndLogOutIfInActive(context: Context) {
        if (context.isActive()) {
           // Toast.makeText(context, "Active so extending session", Toast.LENGTH_LONG).show()
            getTokenExpiryAPi()
                    .performExtension(context.getAuthToken(), ExtendExpiryParams(context.getAuthToken()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if ( !it.loginData.authToken.isNullOrBlank() ) {
                            context.saveAuthTokenToPrefs(it.loginData.authToken)
                            val simpleDate = SimpleDateFormat("yyyy-MM-dd HH:mm")
                            var dateString = it.loginData.tokenExpiryDate.substring(0, 16).replace("T", " ")
                            var tokenExpiryDate = simpleDate.parse(dateString).time
                            if(tokenExpiryDate>Date().time) {
                                context.saveExpiryDate(tokenExpiryDate)
                            }

                            /*Toast.makeText(context, "Token extented to = {"
                                    + simpleDate.format(expDateStored)
                                    + "}", Toast.LENGTH_LONG).show()*/
                        } else {
                            //Toast.makeText(context, "Token wasnt extented ", Toast.LENGTH_LONG).show()
                        }
                    }, {
                        //Toast.makeText(context, "Token wasnt extented{" + it.message + "}", Toast.LENGTH_LONG).show()
                    })
        }
    }

    fun getTokenExpiryAPi() = mainServerRetrofit.create(ExtendExpiryApi::class.java)!!
    fun getLogoutAPi() = mainServerRetrofit.create(LogoutApi::class.java)!!
}
