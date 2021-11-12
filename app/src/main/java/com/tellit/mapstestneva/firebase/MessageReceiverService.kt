package com.tellit.mapstestneva.firebase

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessageReceiverService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.i("Token", "token: ${remoteMessage.from}")

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i("New Token", "onNewToken: $p0")
    }
}
