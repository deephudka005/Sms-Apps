package com.example.smsredirect

import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.net.ConnectivityManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService.setOnClickListener {
            startstopService()
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS),
                111
            )
        }else{
            receiveMsg()
        }

    }

    private fun startstopService() {
        if(isMyServiceRunning(MyService::class.java)){

            Toast.makeText(this,"Service Stopped",Toast.LENGTH_LONG).show()
            stopService(Intent(this,MyService::class.java))
        }else{
            Toast.makeText(this,"Service Started",Toast.LENGTH_LONG).show()
            startService(Intent(this,MyService::class.java))
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            receiveMsg()
        }

    }
    fun receiveMsg() {
        val br = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for (sms in Telephony.Sms.Intents.getMessagesFromIntent(p1)) {
                        if (sms.originatingAddress != null && sms.displayMessageBody != null){
                            phone_rec_sms.setText(sms.originatingAddress)
                            sms_body.setText(sms.displayMessageBody)
                            sendMsg()
                    }
                    }
                }
            }

        }

        LocalBroadcastManager.getInstance(this).registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
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





    private fun sendMsg() {
        val sms = SmsManager.getDefault()
        if (sendNumber.text.toString() != "") {
            sms.sendTextMessage(
                sendNumber.text.toString(),
                "ME",
                sms_body.text.toString(),
                null,
                null
            )
        }else{
            sms.sendTextMessage(
                phone_rec_sms.text.toString(),
                "ME",
                sms_body.text.toString(),
                null,
                null
            )
        }
    }

}

