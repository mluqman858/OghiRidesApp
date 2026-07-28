package com.oghrides.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_RIDE = "oghrides_ride_requests"
        private const val CHANNEL_GENERAL = "oghrides_general"
        private const val RIDE_NOTIFICATION_ID = 9999
        private val VIBRATION_PATTERN = longArrayOf(0, 500, 200, 500, 200, 500, 200, 500, 200, 500)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        createNotificationChannels()

        val type = message.data["type"] ?: ""
        val title = message.notification?.title ?: message.data["title"] ?: "Oghi Rides"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        val isRideRequest = type == "ride_request" || type == "new_ride"

        if (isRideRequest) {
            sendRideNotification(title, body, message.data)
        } else {
            sendGeneralNotification(title, body, message.data)
        }
    }

    private fun sendRideNotification(title: String, body: String, data: Map<String, String>) {
        wakeUpScreen()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_driver_dashboard", true)
            data.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, RIDE_NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val notification = NotificationCompat.Builder(this, CHANNEL_RIDE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("\uD83C\uDFCE $title")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(body)
                .setBigContentTitle("\uD83C\uDFCE $title"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setSound(alarmSound, audioAttr)
            .setVibrate(VIBRATION_PATTERN)
            .setLights(0x10b981, 1000, 1000)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLocalOnly(false)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
        manager.notify(RIDE_NOTIFICATION_ID, notification)

        startRideAlarmLoop()
    }

    private fun sendGeneralNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 300, 100, 300))
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun wakeUpScreen() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                "OghiRides:RideRequest"
            )
            wakeLock.acquire(30_000L)
        } catch (e: Exception) {
            Log.e("FCM", "Wake lock failed", e)
        }
    }

    private fun startRideAlarmLoop() {
        Thread {
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
                if (ringtone != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val audioAttr = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        ringtone.audioAttributes = audioAttr
                    }
                    for (i in 1..5) {
                        if (!ringtone.isPlaying) {
                            ringtone.play()
                        }
                        Thread.sleep(1500)
                        if (ringtone.isPlaying) {
                            ringtone.stop()
                        }
                        Thread.sleep(500)
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM", "Alarm loop failed", e)
            }
        }.start()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val rideChannel = NotificationChannel(
                CHANNEL_RIDE,
                "Ride Requests (LOUD)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ride request alerts - maximum volume"
                enableLights(true)
                lightColor = 0x10b981
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
                setBypassDnd(true)
                lockscreenVisibility = NotificationManager.VISIBILITY_PUBLIC
                if (alarmUri != null) {
                    setSound(
                        alarmUri,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                enableLights(true)
                lightColor = 0x10b981
            }

            manager.createNotificationChannel(rideChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }
}
