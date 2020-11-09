package io.acsint.mtngh.simswap.activities

import android.Manifest
import android.content.Intent
import android.location.Location
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.acsint.mtngh.simswap.*
import io.acsint.mtngh.simswap.api.LogoutApi
import io.acsint.mtngh.simswap.api.SubscriberDetails
import io.acsint.mtngh.simswap.fragments.CompletedRequestsFragment
import io.acsint.mtngh.simswap.fragments.FailedRequestsFragment
import io.acsint.mtngh.simswap.fragments.SentRequestsFragment
import io.acsint.mtngh.simswap.utils.*

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    fun toJsonString() = gson.toJson(this)!!

    companion object {
        fun fromJsonString(jsonStr: String) = gson.fromJson(jsonStr, SubscriberDetails::class.java)!!
    }

    private var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var checkActiveHandler: Handler = Handler()
    private val checkActiveRunnable = object : Runnable {

        override fun run() {
            runOnUiThread({
                checkIsActiveAndLogoutIfNot()
                if (!getAuthToken().isNullOrBlank()) {
                    checkActiveHandler!!.postDelayed(this, 1000)
                }
            })
        }
    }

    public override fun onPause() {
        super.onPause()
        checkActiveHandler.removeCallbacks(checkActiveRunnable)
    }

    public override fun onResume() {
        super.onResume()
        checkActiveRunnable.run()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setSupportActionBar(toolbar)
        //toolbar.setNavigationIcon(R.drawable.logo);

        if (this.IsTokenExpired()) return

        if (supportActionBar != null) {
            supportActionBar!!.title = resources.getString(R.string.title_activity_main)
        }

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        PromptForPermissions()

        var fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            saveLastActiveDate()
            if (this.IsTokenExpired()) return@setOnClickListener

           if (setupPermissions(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_PERMISSION_REQUEST_CODE)) {
                if (setupPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_PERMISSION_REQUEST_CODE)) {
                    getCurrentLocation()
                    if (currentLocation == null) {
                        getCurrentLocation()
                    }
                    val intent=Intent(this, IDVerificationActivity::class.java)
                    //val intent=Intent(this, SimRequestSummaryActivity::class.java)
                    startActivity(intent)
                }
            }



        }

        container.addOnPageChangeListener(this)
    }

    fun getCurrentLocation() {
        try {
            setupPermissions(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_PERMISSION_REQUEST_CODE)
            setupPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_PERMISSION_REQUEST_CODE)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        currentLocation = location;
                    }
        } catch (ex: SecurityException) {
            Log.d("SIMSW_LOC", "Security Exception, no location available");
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_logout) {
            var authToken = getAuthToken()
            //Expire session from server
            getLogoutAPi()
                    .performLogout(authToken)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("logout", "Logout successfull")

                        saveExpiryDate(0)
                        saveAuthTokenToPrefs("")
                        //Exit after 1 second -- enough time for changes to be saved
                        Handler().postDelayed(
                                {
                                    System.exit(0);
                                },
                                1000 // value in milliseconds
                        )
                    }, {
                        Log.d("logout", "Error Logout {" + it.message + "} ["
                            + authToken + "]")
                    })


            return true
        }

        return false
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> {
                    saveLastActiveDate()
                    return SentRequestsFragment()
                }
                1 -> {
                    saveLastActiveDate()
                    return CompletedRequestsFragment()
                }
                2 -> {
                    saveLastActiveDate()
                    return FailedRequestsFragment()
                }
                else -> return null
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {

            when (position) {
                0 -> return "SENT"
                1 -> return "SUCCESSFUL"
                2 -> return "FAILED"
            }
            return null
        }

    }


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (position == 0) {
            fab.show()
        } else {
            fab.hide()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }


    fun getLogoutAPi() = mainServerRetrofit.create(LogoutApi::class.java)!!


}
