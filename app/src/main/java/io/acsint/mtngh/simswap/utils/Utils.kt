package io.acsint.mtngh.simswap.utils

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jetbrains.anko.defaultSharedPreferences
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalStateException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import io.acsint.mtngh.simswap.activities.LoginActivity
import io.acsint.mtngh.simswap.api.LogoutApi
import io.acsint.mtngh.simswap.api.LogoutParams
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.jetbrains.anko.runOnUiThread
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val BASE_URL = "https://agentsimswap.mtn.com.gh"
const val KEY_AUTH_TOKEN = "KEY_AUTH_TOKEN"
const val USER_NAME = "USER_NAME"
const val KEY_TOKEN_EXPIRY = "KEY_TOKEN_EXPIRY"
const val AGENT_ID = "AGENT_ID"
const val LAST_ACTIVE_DATE = "LAST_ACTIVE_DATE"

const val EXTRA_SUBSCRIBER_NAME_VALUE = "EXTRA_SUBSCRIBER_NAME_VALUE"
const val EXTRA_SUBSCRIBER_MSISDN_VALUE = "EXTRA_SUBSCRIBER_MSISDN_VALUE"

//ID_DETAILS
const val ID_DETAILS_PASSPORT = "ID_DETAILS_PASSPORT"
const val ID_DETAILS_VOTER = "ID_DETAILS_VOTER"
const val ID_DETAILS_DRIVER = "ID_DETAILS_DRIVER"

const val EXTRA_ID_CARD_IMAGE_VALUE = "EXTRA_ID_CARD_IMAGE_VALUE"
const val EXTRA_PASSPORT_IMAGE_VALUE = "EXTRA_PASSPORT_IMAGE_VALUE"

const val EXTRA_ID_CARD_LOADED_VALUE = "EXTRA_ID_CARD_LOADED_VALUE"
const val EXTRA_PASSPORT_LOADED_VALUE = "EXTRA_PASSPORT_LOADED_VALUE"
const val IS_DEFAULT_PASS = "IS_DEFAULT_PASS"

const val FINE_LOCATION_PERMISSION_REQUEST_CODE: Int = 987
const val COARSE_LOCATION_PERMISSION_REQUEST_CODE: Int = 965
const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 954
const val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 943//

const val EXTRA_ZOOM_IMAGE_URI_VALUE = "EXTRA_ZOOM_IMAGE_URI_VALUE"

val mainServerRetrofit: Retrofit
    get() {
        val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build()
    }

val gson: Gson = GsonBuilder().setLenient().create()

fun Context.getAuthToken(): String {
    val authToken = defaultSharedPreferences.getString(KEY_AUTH_TOKEN, "")
    return if (authToken.isNullOrBlank()) {
        return ""
    } else {
        authToken
    }
}

fun Context.getUserName(): String {
    val userName = defaultSharedPreferences.getString(USER_NAME, "")
    return if (userName.isNullOrBlank()) {
        return ""
    } else {
        userName
    }
}


fun Context.getAgentId() = defaultSharedPreferences.getInt(AGENT_ID, 0)


fun Context.getTokenExpiry(): Long {
    val tokenExpiry = defaultSharedPreferences.getLong(KEY_TOKEN_EXPIRY, -1)
    return tokenExpiry
}

fun Context.openCoordinatesInGoogleMaps(latitude: String, longitude: String, label: String) {
    val uri = String.format(Locale.ENGLISH, "geo:%s,%s?q=%s,%s(%s)", latitude, longitude, latitude, longitude, label)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    startActivity(intent)
}

fun attemptDateFormatting(input: String): String {
    try {
        if (input.length >= 10) {
            var sdfInput = SimpleDateFormat("yyyy-MM-dd")
            var inputDate = sdfInput.parse(input.substring(0, 10))
            var sdfOutput = SimpleDateFormat("dd MMM, YYYY")
            return sdfOutput.format(inputDate)
        }
    } catch (e: Exception) {

    }

    return input;
}

fun Context.getIsDefaultPass(): Boolean {
    val isDefaultPass = defaultSharedPreferences.getBoolean(IS_DEFAULT_PASS, false)
    return isDefaultPass
}

fun Context.IsTokenExpired(): Boolean {
    val tokenExpiry = getTokenExpiry()
    if (tokenExpiry == 1L || Date().time >= tokenExpiry) {
        var mainIntent = Intent(this, LoginActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        //this.toast("Login Expired, Provide crendentials to continue")
        //startActivity(mainIntent)
        return true
    }
    return false
}

fun Context.saveExpiryDate(tokenExpiry: Long) {
    defaultSharedPreferences.edit().putLong(KEY_TOKEN_EXPIRY, tokenExpiry).apply()
}

fun Context.saveLastActiveDate() {
    val lastActiveDate = Date().time
    defaultSharedPreferences.edit().putLong(LAST_ACTIVE_DATE, lastActiveDate).apply()
}

fun Context.isActive(): Boolean {
    val lastActiveDate = defaultSharedPreferences.getLong(LAST_ACTIVE_DATE, -1)
    val minutesSinceActive = (Date().time-lastActiveDate)/(1000.0*60)
    if (minutesSinceActive>0 && minutesSinceActive < 9.0) {
        return true
    }

    return false
}

fun Context.saveAuthTokenToPrefs(authToken: String) {
    defaultSharedPreferences.edit().putString(KEY_AUTH_TOKEN, authToken).apply()
}

fun Context.storeIsDefaultPass(isDefaultPass: Boolean) {
    defaultSharedPreferences.edit().putBoolean(IS_DEFAULT_PASS, isDefaultPass).apply()
}



fun Activity.setupPermissions(permissionName: String, requestCode: Int): Boolean {
    val permission = ContextCompat.checkSelfPermission(this,
            permissionName)

    if (permission != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
                arrayOf(permissionName),
                requestCode)

        return false
    }
    return true
}


fun Activity.encodeFromString(bm: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.PNG, 100, baos) //bm is the bitmap object
    val b = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}

fun Activity.encodeFromFile(fileUrl: String): String {

    val bm = getBitmapFromFile(fileUrl)!!
    val bOut = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.JPEG, 100, bOut)
    val base64Image = Base64.encodeToString(bOut.toByteArray(), Base64.DEFAULT)
    return base64Image

}

fun Activity.getBitmapFromString(imageBase64: String): Bitmap? {
    try{
        val decodedString = Base64.decode(imageBase64, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        return decodedByte
    }
    catch(ex:Exception){}


    return null
}

fun Activity.getBitmapFromFile(fileUrl: String): Bitmap? {
    val imgFile = File(fileUrl)
    if (imgFile.exists() && imgFile.length() > 0) {
        val bm = BitmapFactory.decodeFile(fileUrl)
        val bOut = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bOut)

        val compressedBm = BitmapFactory.decodeByteArray(bOut.toByteArray(), 0, bOut.size())
        var finalBm = compressedBm
        when (getImageOrientation()) {
            90 -> {
                finalBm = rotateImage(compressedBm, 90f);
            }
            180 -> {
                finalBm = rotateImage(compressedBm, 180f);
            }
            270 -> {
                finalBm = rotateImage(compressedBm, 270f);
            }
        }

        return finalBm
    }

    return null
}

fun Activity.getRealPathFromURI(contentUri: Uri): String {
    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = managedQuery(contentUri, proj, null, null, null)
    val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor.moveToFirst()
    return cursor.getString(column_index)
}

fun Context.rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix,
            true)
}

private fun Context.getImageOrientation(): Int {
    val imageColumns = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION)
    val imageOrderBy = MediaStore.Images.Media._ID + " DESC"
    val cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageColumns, null, null, imageOrderBy)

    if (cursor.moveToFirst()) {
        val orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION))
        cursor.close()
        return orientation
    } else {
        return 0
    }
}

fun ImageView.rotate(angle: Float) {
    this.rotation = angle;
}

fun String.InternationalizePhoneNumber(): String {
    if (this.length == 10) {
        return "233" + this.substring(1)
    } else if (this.length == 9) {
        return "233" + this
    }
    return this
}

fun String.LocalizePhoneNumber(): String {
    if (this.length == 12) {
        return "0" + this.substring(4)
    } else if (this.length == 9) {
        return "0" + this
    }
    return this
}

fun isAlphanumeric(str: String): Boolean {
    val charArray = str.toCharArray()
    for (c in charArray) {
        if (!Character.isLetterOrDigit(c))
            return false
    }
    return true
}



fun setAlarm(context: Context) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val i = Intent(context, AppActiveCheckAlarm::class.java)
    val pi = PendingIntent.getBroadcast(context, 0, i, 0)
    am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (1000 * 60 * 1).toLong(), pi) // Millisec * Second * Minute
}

fun cancelAlarm(context: Context) {
    val intent = Intent(context, AppActiveCheckAlarm::class.java)
    val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(sender)
}


public fun Activity.checkIsActiveAndLogoutIfNot() {

    val authToken = getAuthToken()
    if (!isActive() && !authToken.isNullOrBlank()) {
        cancelAlarm(this)

        getLogoutAPi()
                .performLogout(authToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    saveExpiryDate(0)
                    saveAuthTokenToPrefs("")
                    Log.i("logout", "successfull")
                    finishAffinity()
                }, {
                    Log.i("logout", "failed {" + it.message + "}")

                    saveExpiryDate(0)
                    saveAuthTokenToPrefs("")
                    finishAffinity()
                })

    }
}

fun Activity.PromptForPermissions() {
    setupPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
    setupPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
    setupPermissions(Manifest.permission.CAMERA, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
    setupPermissions(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_PERMISSION_REQUEST_CODE)
    setupPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION_PERMISSION_REQUEST_CODE)
}


fun getLogoutAPi() = mainServerRetrofit.create(LogoutApi::class.java)!!