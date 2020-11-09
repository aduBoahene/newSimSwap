package io.acsint.mtngh.simswap.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.acsint.mtngh.simswap.*
import io.acsint.mtngh.simswap.api.SimResponse
import io.acsint.mtngh.simswap.api.SwapListApi
import io.acsint.mtngh.simswap.data.AllSentSimAdapter
import io.acsint.mtngh.simswap.utils.*
import kotlinx.android.synthetic.main.fragment_swap_requests_list.*

class SentRequestsFragment() : Fragment() {

    public lateinit var recyclerView: RecyclerView
    public lateinit var cAdapter: AllSentSimAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_swap_requests_list, container, false)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)

        val LManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = LManager
        cAdapter = AllSentSimAdapter(activity!!.applicationContext)
        PendingRequesterFetcherTask(activity!!.applicationContext).execute()

        recyclerView.adapter = cAdapter

        swipeContainer.setOnRefreshListener {
            if (activity!!.IsTokenExpired()) return@setOnRefreshListener
            activity!!.saveLastActiveDate()

            cAdapter = AllSentSimAdapter(activity!!.applicationContext)
            recyclerView.adapter = null
            PendingRequesterFetcherTask(activity!!.applicationContext).execute()

            recyclerView.adapter = cAdapter
            swipeContainer.isRefreshing = false
        }

        swipeContainer.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark,
                android.R.color.holo_red_light);
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (activity!!.IsTokenExpired()) return true
        activity!!.saveLastActiveDate()
        PendingRequesterFetcherTask(activity!!.applicationContext).execute()
        return false
    }


    inner class PendingRequesterFetcherTask(var context: Context) : AsyncTask<Void, Void, List<SimResponse.SwapRequest>>() {
        lateinit var errorMessage: String

        override fun doInBackground(vararg params: Void?): List<SimResponse.SwapRequest> {
            return showSentSwapSimRequests()
        }

        override fun onPostExecute(result: List<SimResponse.SwapRequest>) {
            super.onPostExecute(result)

            cAdapter.setData(result)
            recyclerView.adapter = null
            recyclerView.adapter = cAdapter

            if (errorMessage != null && errorMessage.length > 0 && (result == null || result.size == 0)) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG);
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        public fun showSentSwapSimRequests(): List<SimResponse.SwapRequest> {
            try {
                errorMessage = "";

                var simRequestResponse = swapList()
                        .swapRequestSynchronous(context.getAuthToken(), context.getAgentId()).execute()
                if (simRequestResponse.isSuccessful) {
                    val swapRequests = simRequestResponse.body()?.swapRequests
                    if (swapRequests != null) {
                        Log.d("swap list", swapRequests.toString())
                        return swapRequests
                    }
                }
            } catch (ex: Exception) {
                errorMessage = "An Error Occured\n" + ex.message
            }
            return emptyList()
        }

        private fun swapList() = mainServerRetrofit.create(SwapListApi::class.java)!!

    }


}