package com.example.smsredirect

import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

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
                val bundle = p1?.extras

                //val slot = bundle!!.getInt("slot", -1)
                var slot = -1
                try {
                    if (bundle != null) {
                        val keySet = bundle.keySet()
                        for (key in keySet) {
                            when (key) {
                                "slot" -> slot = bundle.getInt("slot", -1)
                                "simId" -> slot = bundle.getInt("simId", -1)
                                "simSlot" -> slot = bundle.getInt("simSlot", -1)
                                "slot_id" -> slot = bundle.getInt("slot_id", -1)
                                "simnum" -> slot = bundle.getInt("simnum", -1)
                                "slotId" -> slot = bundle.getInt("slotId", -1)
                                "slotIdx" -> slot = bundle.getInt("slotIdx", -1)
                                else -> if (key.lowercase(Locale.getDefault())
                                        .contains("slot") or key.lowercase(
                                        Locale.getDefault()
                                    )
                                        .contains("sim")
                                ) {
                                    val value = bundle.getString(key, "-1")
                                    if ((value == "0") or (value == "1") or (value == "2")) {
                                        slot = bundle.getInt(key, -1)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Exception=>$e")
                }
                //Toast.makeText(applicationContext, "slot== " + slot, Toast.LENGTH_SHORT).show()
                var sim = 0
                if (slot == 0) {
                    sim = 1
                }
                else if(slot==1){
                    sim = 2
                }
                if (sim.toString() == simcardNumber.text.toString()) {
                    for (sms in Telephony.Sms.Intents.getMessagesFromIntent(p1)) {
                        if (sms.originatingAddress != null && sms.displayMessageBody != null) {
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
        val str=sendNumber.text.toString()
        val list: List<String> = str.split(",").toList()
        for (i in list.indices) {
            if (list[i] != "") {
                sms.sendTextMessage(
                    list[i],
                    "ME",
                    sms_body.text.toString(),
                    null,
                    null
                )
            }
        }/*else{
            sms.sendTextMessage(
                phone_rec_sms.text.toString(),
                "ME",
                sms_body.text.toString(),
                null,
                null
            )
        }*/
    }

}

