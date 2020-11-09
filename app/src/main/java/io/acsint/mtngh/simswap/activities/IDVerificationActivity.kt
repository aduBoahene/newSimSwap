package io.acsint.mtngh.simswap.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import io.acsint.mtngh.simswap.R
import io.acsint.mtngh.simswap.api.ClearSessionsApi
import io.acsint.mtngh.simswap.api.ClearSessionsParams
import io.acsint.mtngh.simswap.api.GetDetailsFromGVIVEApi
import io.acsint.mtngh.simswap.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_idverification.*
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.indeterminateProgressDialog
import java.util.*
import android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK
import android.graphics.Color


class IDVerificationActivity : AppCompatActivity() {

    lateinit var idTypeDropdown: Spinner//txt_idNumber
    var driverDob:String = ""//txt_idNumber
    lateinit var idType:String//txt_idNumber


    override  fun onResume() {
        super.onResume()
        saveLastActiveDate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idverification)

        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = resources.getString(R.string.title_activity_id_verification_request)
        }

        idTypeDropdown = findViewById(R.id.idTypeDropdown) as Spinner
        var options = arrayOf("IDTYPE","VOTERS", "PASSPORT","DRIVERS LICENCE")//, "NHIS", "NATIONAL ID", "DRIVER'S LICENSE")
        idTypeDropdown.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options)

        idTypeDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //idtype.selectedItem.equals("ID Type")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(idTypeDropdown.selectedItem.toString().equals("DRIVERS LICENCE")){
                    txt_dob.visibility=View.VISIBLE
                }else{
                    txt_dob.visibility=View.INVISIBLE
                }

                if(idTypeDropdown.selectedItem.toString().equals("VOTERS")){
                    txt_idNumber.hint = "Enter Voters ID"
                }

                if(idTypeDropdown.selectedItem.toString().equals("PASSPORT")){
                    txt_idNumber.hint = "Enter passport Number"
                }

                if(idTypeDropdown.selectedItem.toString().equals("DRIVERS LICENCE")){
                    txt_idNumber.hint = "Enter certificate of competence"
                }

            }

        }


        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH + 1)
        val day = c.get(Calendar.DAY_OF_MONTH)

        //val c = Calender.getInstance
        txt_dob.setOnClickListener {


            val dob = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                txt_dob.setText(""+year + "-" + (month+1) +"-"+ dayOfMonth)
                driverDob = txt_dob.text.toString()
                //driverDob = txt_dob.text.toString().replace("/","-")

                Toast.makeText(this,driverDob, Toast.LENGTH_LONG).show()

            }, year,month,day)

            dob.show()
            dob.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.YELLOW)
            dob.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.YELLOW)
            //datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);
        }


        PromptForPermissions()

        verify_id_number.setOnClickListener {

            //TODO:make api call to verifytxt_dob.visibility = View.VISIBLE

            var idType = idTypeDropdown.selectedItem.toString()
            val searchTerm = txt_idNumber.text.toString().trim()


            if(searchTerm.isNullOrBlank()){
                Toast.makeText(this,"Please enter a valid ID Number and try again",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(idType.equals("IDTYPE")&&!searchTerm.isNullOrBlank()){
                Toast.makeText(this,"Select ID TYPE to be verified",Toast.LENGTH_LONG).show()
            }

            if(idType.equals("PASSPORT")&&!searchTerm.isNullOrBlank()){
                var p  = indeterminateProgressDialog("Loading…")
                p.show()
                getGetPassportDetailsApi()
                        .getPassportDetails(getAuthToken(),searchTerm)
                        //.getPassportDetails(getAuthToken(),"G1263099")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            p.dismiss()
                            if(!it.firstName.isNullOrBlank()){
                                Log.d("passport response",it.toString())
                                val intent = Intent(this@IDVerificationActivity, IDVerificationResultActivity::class.java)
                                txt_idNumber.setText("")
                                intent.putExtra(ID_DETAILS_PASSPORT,it.toJsonString())
                                startActivity(intent)

                            }else{
                                Toast.makeText(this,"Check Passport Number and try again",Toast.LENGTH_LONG).show()
                                p.dismiss()
                            }

                        },{})
            }else{
                //Toast.makeText(this,"An error occured, Please try again",Toast.LENGTH_LONG).show()
                //p.dismiss()
            }

            if(idType.equals("VOTERS")&&!searchTerm.isNullOrBlank()){
                var p  = indeterminateProgressDialog("Loading…")
                p.show()
                getGetVoterDetailsApi()
                        .getVoterDetails(getAuthToken(),searchTerm)
                        //.getVoterDetails(getAuthToken(),"4281010308")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            p.dismiss()
                            if(!it.fullname.isNullOrBlank()){
                                Log.d("voter response",it.toString())
                                val intent = Intent(this@IDVerificationActivity, IDVerificationResultActivity::class.java)
                                intent.putExtra(ID_DETAILS_VOTER,it.toJsonString())
                                startActivity(intent)
                            }else{
                                Toast.makeText(this,"Check Voter Number and try again",Toast.LENGTH_LONG).show()
                                p.dismiss()
                            }

                        },{})
            }else{
                //Toast.makeText(this,"An error occured, Please try again",Toast.LENGTH_LONG).show()
                //p.dismiss()
            }

            if(idType.equals("DRIVERS LICENCE")&&!searchTerm.isNullOrBlank()){

                if(searchTerm.length<4 || searchTerm.length>12){
                    Toast.makeText(this,"Verify Competence number length",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if(driverDob.isNullOrBlank()){
                    Toast.makeText(this,"Provide driver date of birth",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                var p  = indeterminateProgressDialog("Loading…")
                p.show()
                getGetDriverDetailsApi()
                        .getDriverDetails(getAuthToken(),searchTerm,driverDob)//1984-06-22
                        //.getDriverDetails(getAuthToken(),"18003886H1","1984-06-22")
                        //.getDriverDetails(getAuthToken(),"108357","1983-06-09")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            p.dismiss()
                            if(!it.name.isNullOrBlank()){
                                Log.d("driver response",it.toString())
                                val intent = Intent(this@IDVerificationActivity, IDVerificationResultActivity::class.java)
                                it.dob = driverDob
                                intent.putExtra(ID_DETAILS_DRIVER,it.toJsonString())
                                startActivity(intent)
                            }else{
                                Toast.makeText(this,"Validation failed. Kindly check  number and try again",Toast.LENGTH_LONG).show()
                                p.dismiss()
                            }

                        },{
                            Toast.makeText(this,"An error occured, Please try again",Toast.LENGTH_LONG).show()
                            p.dismiss()
                        })
            }else{
                //Toast.makeText(this,"An error occured, Please try again",Toast.LENGTH_LONG).show()
                //p.dismiss()
            }



        }
    }

    private fun getGetPassportDetailsApi() = mainServerRetrofit.create(GetDetailsFromGVIVEApi::class.java)!!
    private fun getGetVoterDetailsApi() = mainServerRetrofit.create(GetDetailsFromGVIVEApi::class.java)!!
    private fun getGetDriverDetailsApi() = mainServerRetrofit.create(GetDetailsFromGVIVEApi::class.java)!!
}
