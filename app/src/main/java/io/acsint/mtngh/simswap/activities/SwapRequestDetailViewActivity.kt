package io.acsint.mtngh.simswap.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.EXTRA_SUBSCRIBER_DETAILS
import io.acsint.mtngh.simswap.api.SimResponse
import io.acsint.mtngh.simswap.utils.*

import kotlinx.android.synthetic.main.activity_swap_request_detail_view.*

class SwapRequestDetailViewActivity : AppCompatActivity() {

    override  fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap_request_detail_view)

        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = resources.getString(R.string.title_activity_sim_swap_request_detail_view)
        }

        if (intent.hasExtra(EXTRA_SUBSCRIBER_DETAILS)) {
            var subscriberDetailsString: String = intent.getStringExtra(EXTRA_SUBSCRIBER_DETAILS)
            if (!subscriberDetailsString.isNullOrBlank()) {
                var subscriberDetails = SimResponse.SwapRequest.fromJsonString(subscriberDetailsString)

                phoneNumber.text = subscriberDetails.msisdn
                subscriberIdType.text = subscriberDetails.idType
                clientIdNumber.text = subscriberDetails.idNumber
                swapReason.text = subscriberDetails.reason
                swapComment.text = subscriberDetails.comments
                swapSerialNumber.text = subscriberDetails.newSimSerial
                swapSubscriberName.text = subscriberDetails.fullname
                currentLocationCoordinates.text = "(" + subscriberDetails.latitude + ", " +
                        subscriberDetails.longitude + ")"

                val options = RequestOptions()
                options.placeholder(R.drawable.no_image)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .error(R.drawable.user)

                var idCardImageObject = subscriberDetails.attachment?.idCardImage
                if (idCardImageObject != null && idCardImageObject.fileUrl != null) {
                    Glide.with(this).load(idCardImageObject.fileUrl)
                            .apply(options)
                            .into(idCardImageView);

                    idCardImageView.setOnClickListener(View.OnClickListener {
                        openImageUriInDetail(idCardImageObject.fileUrl, subscriberDetails.fullname, "ID Card")
                    })
                }

                var profileImageObject = subscriberDetails.attachment?.requesterImage
                if (profileImageObject != null && profileImageObject.fileUrl != null) {
                    Glide.with(this).load(profileImageObject.fileUrl)
                            .apply(options)
                            .into(passportPhotoImageView);

                    passportPhotoImageView.setOnClickListener(View.OnClickListener {
                        openImageUriInDetail(profileImageObject.fileUrl, subscriberDetails.fullname, "Profile")
                    })
                }

                currentLocationCoordinates.setOnClickListener(View.OnClickListener {
                    try {
                        openCoordinatesInGoogleMaps(subscriberDetails.latitude, subscriberDetails.longitude, subscriberDetails.fullname!!)
                    } catch (e: Exception) {
                        Log.e("SHW_MAPS", e.message)
                    }
                }
                )
            }
        }
    }

    fun openImageUriInDetail(imageUri: String, callingUserName: String?, imageType: String) {
        var imageDetailIntent = Intent(this, ImagePreviewActivity::class.java)
        imageDetailIntent.putExtra(EXTRA_ZOOM_IMAGE_URI_VALUE, imageUri)
        var previewTitle: String = imageType
        if (!callingUserName.isNullOrBlank()) {
            previewTitle = callingUserName!! + " (" + previewTitle + ")"
        }
        imageDetailIntent.putExtra(EXTRA_SUBSCRIBER_NAME_VALUE, previewTitle)

        startActivity(imageDetailIntent)
    }

}
