package io.acsint.mtngh.simswap.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.ClearSessionsApi
import io.acsint.mtngh.simswap.api.ClearSessionsParams
import io.acsint.mtngh.simswap.api.LoginParams
import io.acsint.mtngh.simswap.utils.getAuthToken
import io.acsint.mtngh.simswap.utils.mainServerRetrofit
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_clear_all_sessions.*

class ClearAllSessionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clear_all_sessions)

        clear_active_session.setOnClickListener {
            val inputUserName = username.text.toString().trim()
            val inputPassword = password.text.toString().trim()

            getClearSessionApi()
                    .performClearSession(ClearSessionsParams(inputUserName, inputPassword))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if(it.success){
                            Toast.makeText(this,"You have succesfully logged out of all devices",Toast.LENGTH_LONG).show()
                        }
                    },{
                        Toast.makeText(this,"Error Connecting to API server: {" + it.message + "}",Toast.LENGTH_LONG).show()
                    })
        }
    }

    private fun getClearSessionApi() = mainServerRetrofit.create(ClearSessionsApi::class.java)!!


}
