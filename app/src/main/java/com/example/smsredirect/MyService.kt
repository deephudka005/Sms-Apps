package com.example.smsredirect

import android.Manifest
import android.app.*
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build

import android.os.IBinder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smsredirect.Constant.CHANNEL_ID
import com.example.smsredirect.Constant.SMS_NOTIFICATION_ID
import kotlinx.android.synthetic.main.activity_main.*

class MyService: Service(){


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        //you can ignore initsms function
        initSms()
    }

    private fun initSms() {
       //val obj=MainActivity()
        //LocalBroadcastManager.getInstance(this).registerReceiver(obj, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
        //obj.receiveMsg()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()


        return START_STICKY

    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification() {
        val notificationIntent = Intent(this,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,0,notificationIntent,0
        )
        val notification = Notification
                .Builder(this,CHANNEL_ID)
                .setContentText("SMS REDIRECT")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(SMS_NOTIFICATION_ID, notification)
    }



    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "My Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager= getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun intitSms(){

    }
}


