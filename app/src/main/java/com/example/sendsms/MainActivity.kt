package com.example.sendsms

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {
    private val PICK_PDF_FILE = 202
    private lateinit var data: String
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS),
                111
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // start runtime permission
            val hasPermission =
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
            }
        }
        Send.setOnClickListener {
            startstopService()
            sendMsg(data)
        }
        import_file.setOnClickListener {
            openFile()
        }
    }



    fun openFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType("*/*")
                //type = "application/pdf"
                // Optionally, specify a URI for the file that should appear in the
                // system file picker when it loads.
                //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, PICK_PDF_FILE)
    }


    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 202
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri->
                val path=uri
                readTextFromUri(path)

            }
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri) {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
          data= stringBuilder.toString()
    }

    private fun startstopService() {
        if(isMyServiceRunning(MyService::class.java)){

            Toast.makeText(this,"Service Stopped", Toast.LENGTH_SHORT).show()
            stopService(Intent(this,MyService::class.java))
        }else{
            Toast.makeText(this,"Service Started", Toast.LENGTH_SHORT).show()
            startService(Intent(this,MyService::class.java))
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun isMyServiceRunning(mClass: Class<MyService>): Boolean {
        val manager: ActivityManager = getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager

        for(service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE)){
            if(mClass.name.equals(service.service.className)){
                return true
            }
        }
        return false
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun sendMsg(sendNumber: String) {
        val sms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(SmsManager::class.java)
        }else{
            TODO()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sms.createForSubscriptionId(2)
        } // uses default simcard registered for sending sms
        val list: List<String> = sendNumber.split(",").toList()
        for (i in list.indices) {
            if (list[i] != "") {
                try{sms.sendTextMessage(
                    list[i],
                    "ME",
                    message_body.text.toString(),
                    null,
                    null
                )
                }catch (e:Exception){
                    Toast.makeText(this, e.message + "!\n" + "Failed to send SMS", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
        Toast.makeText(this, "Message Sent to All valid Contacts", Toast.LENGTH_LONG).show()
        startstopService()
    }
}