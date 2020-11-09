package io.acsint.mtngh.simswap.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.*
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.PostParams
import io.acsint.mtngh.simswap.api.PostSwapApi
import io.acsint.mtngh.simswap.api.SubscriberDetails
import io.acsint.mtngh.simswap.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sim_request_summary.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import java.util.*
import android.provider.Settings
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.acsint.mtngh.simswap.api.EXTRA_SUBSCRIBER_DETAILS
import java.text.SimpleDateFormat


class SimRequestSummaryActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_LOCATION = 10


    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var phoneMsisdn: String
    lateinit var serial: String
    lateinit var idNumber: String
    lateinit var idType: String
    lateinit var reason: String
    lateinit var comment: String

    private var currentLocation: Location? = null

    private var options: RequestOptions? = null

    lateinit var idCardImageUri: Uri
    lateinit var passportImageUri: Uri
    var saveOngoing = false
    var isProceedBtnClicked = false
    lateinit var subscriberDetails:SubscriberDetails


    override fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sim_request_summary)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        startLocationUpdates()
         subscriberDetails = SubscriberDetails.fromJsonString(intent.getStringExtra(EXTRA_SUBSCRIBER_DETAILS))
        val subscriberMsisdn = intent.getStringExtra(EXTRA_SUBSCRIBER_MSISDN_VALUE)

        if (supportActionBar != null) {
            supportActionBar!!.title = resources.getString(R.string.title_activity_sim_swap_confirmation)
        }

        options = RequestOptions()
        options!!.placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)

        phoneMsisdn = intent.getStringExtra("UserPhone")
        serial = intent.getStringExtra("serial")
        idNumber = intent.getStringExtra("idNumber")
        idType = intent.getStringExtra("idtype")
        reason = intent.getStringExtra("reason")
        comment = intent.getStringExtra("comment")

        //set imageViews with bitmaps
        idCardImageUri = Uri.parse(intent.getStringExtra("idPhoto"))
        passportImageUri = Uri.parse(intent.getStringExtra("passportPhoto"))

        Glide.with(this)
                .load(idCardImageUri)
                .apply(options!!)
                .into(findViewById<ImageView>(R.id.imageView1));

        Glide.with(this)
                .load(passportImageUri)
                .apply(options!!)
                .into(findViewById<ImageView>(R.id.imageView2));

        //populate textviews
        phoneNumber.text = phoneMsisdn
        swapSerialNumber.text = serial
        subscriberIdType.text = idType
        clientIdNumber.text = idNumber
        swapReason.text = reason
        swapComment.text = comment

        sendrequest.setOnClickListener {
            saveLastActiveDate()
            isProceedBtnClicked=true;
            startLocationUpdates()
            //doSaveSwapRequestToServer(subscriberDetails)
        }

    }

    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            , 11)
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.cancel()
                    finish()
                }
        val alert: AlertDialog = builder.create()
        alert.show()

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)

        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY)

    }

    protected fun startLocationUpdates() {


        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest!!.setInterval(5000.toLong())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val locationList = locationResult?.locations
                onLocationChanged(locationResult!!.lastLocation)
                /*if (locationResult == null) {
                    return
                }*/

            }
        }

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback,
                Looper.myLooper())
    }


    public override fun onPause() {
        super.onPause()
        stoplocationUpdates()
    }

    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED){
                true
            }else{
                // Show the permission request
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }
    fun onLocationChanged(location: Location) {
        // New location has now been determined
        //doSaveSwapRequestToServer(subscriberDetails)
        currentLocation = location
        if(isProceedBtnClicked){
            isProceedBtnClicked=false
            Toast.makeText(this,currentLocation!!.latitude.toString(),Toast.LENGTH_LONG).show()
            doSaveSwapRequestToServer(subscriberDetails)
        }

    }

    private fun stoplocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    fun doSaveSwapRequestToServer(subscriberDetails: SubscriberDetails) {
        if (!saveOngoing) {
            saveOngoing = true
            if (currentLocation == null) {
                saveOngoing = false
                toast("Location not retrieved: turn on GPS and try again")
                return
            }
            isProceedBtnClicked=false
            sendrequest.isEnabled = false

            toast("Sending request to server ... ")
            val idPhotoString = encodeFromFile(getRealPathFromURI(idCardImageUri))
            val passportPhotoString = encodeFromFile(getRealPathFromURI(passportImageUri))

            var tok = getAuthToken()
            var saveRequest = PostParams(phoneMsisdn, serial, idType, idNumber,
                    reason, comment, idPhotoString, passportPhotoString
                    , getAgentId(), currentLocation?.longitude.toString(),
                    currentLocation?.latitude.toString(), subscriberDetails.fullName)

            var p = indeterminateProgressDialog("Loading…")
            p.show()

            getPostSimSwapApi()
                    .postSimSwap(getAuthToken(), saveRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        p.dismiss()
                        if (it == null || !it.success) {
                            toast("Error! {" + it.message + "}")
                            sendrequest.isEnabled = true
                        } else {
                            sendrequest.isEnabled = true
                            Log.d("success Post sim", it.toString())
                            toast("Data Saved Succesfully")

                            var mainIntent = Intent(this@SimRequestSummaryActivity, MainActivity::class.java)
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                            startActivity(mainIntent)
                        }
                        saveOngoing = false
                    }, {
                        p.dismiss()
                        sendrequest.isEnabled = true
                        toast("Error in post {" + it.message + "}")
                        saveOngoing = false
                    })
        }
    }

    fun decodeImageString(imgString: String): Bitmap {

        val decodedBytes = Base64.decode(imgString, 0)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    private fun getPostSimSwapApi() = mainServerRetrofit.create(PostSwapApi::class.java)!!


}


