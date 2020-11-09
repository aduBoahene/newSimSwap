package io.acsint.mtngh.simswap.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.EXTRA_SUBSCRIBER_DETAILS
import io.acsint.mtngh.simswap.api.SubscriberDetails
import kotlinx.android.synthetic.main.activity_sim_swap_attachment.*
import org.jetbrains.anko.toast
import android.content.ContentValues
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.acsint.mtngh.simswap.utils.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton


class SimSwapImageAttachmentActivity : AppCompatActivity() {

    val CAMERA_REQUEST_CODE_ID_CARD = 0
    val CAMERA_REQUEST_CODE_PROFILE = 2

    lateinit var phoneMsisdn: String
    lateinit var serial: String
    lateinit var idNumber: String
    lateinit var idType: String
    lateinit var reason: String
    lateinit var comment: String

    private var idCardLoaded = false;
    private var passportLoaded = false;
    private var passportImageUri: Uri? = null
    private var idCardImageUri: Uri? = null

    private var options: RequestOptions? = null
    private var overrideNoFace = false;
    private var faceDected = false;

    override fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sim_swap_attachment)

        if (supportActionBar != null) {
            supportActionBar!!.title = resources.getString(R.string.title_activity_sim_swap_attachment)
        }

        options = RequestOptions()
        options!!.placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)

        val subscriberDetails = SubscriberDetails.fromJsonString(intent.getStringExtra(EXTRA_SUBSCRIBER_DETAILS))
        val subscriberMsisdn = intent.getStringExtra(EXTRA_SUBSCRIBER_MSISDN_VALUE)

        if (savedInstanceState != null) {
            var idCardUriString = savedInstanceState.getString(EXTRA_ID_CARD_IMAGE_VALUE)
            if (!idCardUriString.isNullOrBlank()) {
                idCardImageUri = Uri.parse(idCardUriString)

                Glide.with(this)
                        .load(idCardImageUri)
                        .apply(options!!)
                        .into(findViewById<ImageView>(R.id.idCardImageView));
                idCardLoaded = savedInstanceState.getBoolean(EXTRA_ID_CARD_LOADED_VALUE)
            }

            var passportUriString = savedInstanceState.getString(EXTRA_PASSPORT_IMAGE_VALUE)
            if (!passportUriString.isNullOrBlank()) {
                passportImageUri = Uri.parse(passportUriString)

                Glide.with(this)
                        .load(passportImageUri)
                        .apply(options!!)
                        .into(findViewById<ImageView>(R.id.passportPhotoImageView));
                passportLoaded = savedInstanceState.getBoolean(EXTRA_PASSPORT_LOADED_VALUE)
            }
        }
        idCardPhotoCaptureButton.setOnClickListener {
            saveLastActiveDate()

            val callCameraIntent = prepareImageCapture(CAMERA_REQUEST_CODE_ID_CARD,
                    subscriberDetails.msisdn + "_id_card", subscriberDetails.fullName)
            if (callCameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE_ID_CARD)
            }
        }

        passportPhotoCaptureButton.setOnClickListener {
            saveLastActiveDate()
            val callCameraIntent = prepareImageCapture(CAMERA_REQUEST_CODE_PROFILE,
                    subscriberDetails.msisdn + "_user_profile", subscriberDetails.fullName)
            if (callCameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE_PROFILE)
            }
        }

        previous.setOnClickListener {
            saveLastActiveDate()
            startActivity(Intent(this@SimSwapImageAttachmentActivity, SimSwapRequestActivity::class.java))
        }

        preview.setOnClickListener {
            saveLastActiveDate()
            if (!idCardLoaded || !passportLoaded) {
                Toast.makeText(applicationContext, "Select or capture both ID Card and Subscriber Photos", Toast.LENGTH_LONG).show()

            } else if (!faceDected /*&& !overrideNoFace */) {
                toast("Profile Picture is invalid. Correct in order to continue")
                overrideNoFace = true
            } else {
                val intent = prepareSummaryAcitivityLaunchIntent(subscriberDetails, subscriberMsisdn)

                startActivity(intent)
            }
        }


        setupPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        setupPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        setupPermissions(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_PERMISSION_REQUEST_CODE)
        setupPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_PERMISSION_REQUEST_CODE)
        setupPermissions(Manifest.permission.CAMERA, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)

    }

    fun prepareSummaryAcitivityLaunchIntent(subscriberDetails: SubscriberDetails, subscriberMsisdn: String?): Intent {
        phoneMsisdn = intent!!.getStringExtra("User Phone")
        serial = intent!!.getStringExtra("serial")
        idNumber = intent!!.getStringExtra("idNumber")
        idType = intent!!.getStringExtra("idtype")
        reason = intent!!.getStringExtra("reason")
        comment = intent!!.getStringExtra("comment")

        val intent = Intent(this@SimSwapImageAttachmentActivity, SimRequestSummaryActivity::class.java)

        intent.putExtra("UserPhone", phoneMsisdn)
        intent.putExtra("serial", serial)
        intent.putExtra("idNumber", idNumber)
        intent.putExtra("reason", reason)
        intent.putExtra("comment", comment)
        intent.putExtra("idtype", idType)

        intent.putExtra("passportPhoto", passportImageUri.toString())
        intent.putExtra("idPhoto", idCardImageUri.toString())

        intent.putExtra(EXTRA_SUBSCRIBER_DETAILS, subscriberDetails.toJsonString())
        intent.putExtra(EXTRA_SUBSCRIBER_MSISDN_VALUE, subscriberMsisdn)
        return intent
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            super.onActivityResult(requestCode, resultCode, data)

            //toast("resultCode=" + resultCode.toString())
            when (requestCode) {
                CAMERA_REQUEST_CODE_ID_CARD -> {
                    processIdCardCaptureResultBeingReady(resultCode)
                }
                CAMERA_REQUEST_CODE_PROFILE -> {
                    processProfileCaptureResultBeingReady(resultCode)
                }
                else -> {
                    Toast.makeText(this, "Unrecognised request code", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("image_capture", e.toString());
        }
    }

    fun processIdCardCaptureResultBeingReady(resultCode: Int) {
        if ((resultCode == Activity.RESULT_OK || idCardImageUri != null)) {

            val realFilePath = getRealPathFromURI(idCardImageUri!!)
            if (realFilePath.isNullOrBlank()) {
                toast("Error Capturing ID Card Photo.")
                return
            }

            Glide.with(this)
                    .load(idCardImageUri)
                    .apply(options!!)
                    .into(findViewById<ImageView>(R.id.idCardImageView));

            idCardLoaded = true
        }
    }

    fun processProfileCaptureResultBeingReady(resultCode: Int) {
        if ((resultCode == Activity.RESULT_OK || passportImageUri != null)) {

            val realFilePath = getRealPathFromURI(passportImageUri!!)
            if (realFilePath.isNullOrBlank()) {
                toast("Error Capturing Profile Photo.")
                return
            }

            faceDected = ImageFaceDetector(this).detectHasSingleFace(passportImageUri)

            if (!faceDected) {
                //toast("Invalid Profile Photo. Should have a single person's profile picture.")
                alert("Make sure to capture enough facial feautures under enough lightening") {
                    title = "Face detection failed"
                    yesButton {  }
                }.show()
            }


            Glide.with(this)
                    .load(passportImageUri)
                    .apply(options!!)
                    .into(findViewById<ImageView>(R.id.passportPhotoImageView));


            passportLoaded = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState == null) return

        if (idCardImageUri != null) {
            outState.putString(EXTRA_ID_CARD_IMAGE_VALUE, idCardImageUri.toString())
        }

        if (passportImageUri != null) {
            outState.putString(EXTRA_PASSPORT_IMAGE_VALUE, passportImageUri.toString())
        }

        outState.putBoolean(EXTRA_ID_CARD_LOADED_VALUE, idCardLoaded)
        outState.putBoolean(EXTRA_PASSPORT_LOADED_VALUE, passportLoaded)
    }


    fun prepareImageCapture(requestCode: Int, title: String, description: String): Intent {
        var values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DESCRIPTION, description)
        val imageUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (requestCode == CAMERA_REQUEST_CODE_ID_CARD) {
            idCardImageUri = imageUri
        } else {
            passportImageUri = imageUri
        }

        return intent
    }

    class IdCardLoaderTask : AsyncTask<String, Void, Uri>() {
        override fun doInBackground(vararg params: String?): Uri {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}