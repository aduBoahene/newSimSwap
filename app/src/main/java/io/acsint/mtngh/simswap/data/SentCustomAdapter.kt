package io.acsint.mtngh.simswap.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.activities.SwapRequestDetailViewActivity
import io.acsint.mtngh.simswap.api.EXTRA_SUBSCRIBER_DETAILS
import io.acsint.mtngh.simswap.api.SimResponse
import org.jetbrains.anko.find


class AllSentSimAdapter(var context: Context) : RecyclerView.Adapter<AllSentSimAdapter.ViewHolder>()
        , SwapItemClickListener {

    var swappedSimsList: List<SimResponse.SwapRequest> = emptyList()

    override fun clicked(clickedItemPosition: Int) {
        try {
            var currentItem = swappedSimsList[clickedItemPosition]

            var swapDetailIntent = Intent(context, SwapRequestDetailViewActivity::class.java)
            swapDetailIntent.putExtra(EXTRA_SUBSCRIBER_DETAILS, currentItem.toJsonString())
            swapDetailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(swapDetailIntent)
        } catch (e: Exception)
        {
            Log.e("ListItemClicked", e.message)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.swap_request_list_item_row, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return swappedSimsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            var currentSwapData = swappedSimsList[position]
            var requesterImageObject = currentSwapData.attachment?.requesterImage
            if (requesterImageObject != null && requesterImageObject.fileUrl != null) {
                val options = RequestOptions()
                options.placeholder(R.drawable.no_image)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .error(R.drawable.user)
                        .transform(CenterCrop())
                        .transform(RoundedCorners(20))

                Glide.with(context)
                        .load(requesterImageObject.fileUrl)
                        .apply(options)
                        .into(holder.displayImage);
            }
            holder.msisdnTextView.text = currentSwapData.msisdn.trim()
            holder.idNumberTextView.text = currentSwapData.idNumber.trim()
            holder.idTypeTextView.text = currentSwapData.idType.trim()
            holder.serialNumberTextView.text = currentSwapData.newSimSerial.trim()
            holder.reasonTextView.text = currentSwapData.reason
            holder.fullNameTextView.text = currentSwapData.fullname

            holder.setItemClickListener(this)
        } catch (e: Exception) {
        }
    }

    public fun setData(newData: List<SimResponse.SwapRequest>) {
        swappedSimsList = newData
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val displayImage = itemView.find<ImageView>(R.id.dp)
        val serialNumberTextView = itemView.find<TextView>(R.id.serial)
        val msisdnTextView = itemView.find<TextView>(R.id.msisdn)
        val idTypeTextView = itemView.find<TextView>(R.id.idtype)
        val idNumberTextView = itemView.find<TextView>(R.id.idNumber)
        val reasonTextView = itemView.find<TextView>(R.id.reason)
        val fullNameTextView = itemView.find<TextView>(R.id.fullNameTextView)
        lateinit var swapItemClickListener: SwapItemClickListener

        init {
            itemView.setOnClickListener {
                swapItemClickListener!!.clicked(adapterPosition)
            }
        }

        fun setItemClickListener(listener: SwapItemClickListener) {
            swapItemClickListener = listener
        }
    }
}

interface SwapItemClickListener {
    fun clicked(clickedItemPosition: Int)
}