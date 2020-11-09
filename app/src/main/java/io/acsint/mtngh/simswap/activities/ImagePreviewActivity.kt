package io.acsint.mtngh.simswap.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.utils.*
import kotlinx.android.synthetic.main.activity_image_preview.*

class ImagePreviewActivity : AppCompatActivity() {
    private var currentRotation = 0f;

    override  fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        var callingIntent = intent;
        if (callingIntent.hasExtra(EXTRA_ZOOM_IMAGE_URI_VALUE)) {
            var zoomImageUri = callingIntent.getStringExtra(EXTRA_ZOOM_IMAGE_URI_VALUE)

            val options = RequestOptions()
            options.placeholder(R.drawable.no_image)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.user)
            Glide.with(this).load(zoomImageUri)
                    .apply(options)
                    .into(zoomableImage);
        }


        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
        }

        if (supportActionBar != null) {
            if (callingIntent.hasExtra(EXTRA_SUBSCRIBER_NAME_VALUE)) {
                var callingSubscriberName = callingIntent.getStringExtra(EXTRA_SUBSCRIBER_NAME_VALUE)
                supportActionBar!!.title = callingSubscriberName
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image_preview, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        saveLastActiveDate()
        if (item!!.itemId == R.id.action_rotate) {
            when(currentRotation){
                0f->{
                    currentRotation=90f
                }
                90f->{
                    currentRotation=180f
                }
                180f->{
                    currentRotation=270f
                }
                270f->{
                    currentRotation=0f
                }
            }

            zoomableImage.rotate(currentRotation )

            return  true
        }
        return super.onOptionsItemSelected(item)
    }
}
