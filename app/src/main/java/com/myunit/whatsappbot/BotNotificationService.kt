package com.myunit.whatsappbot

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.RemoteInput
import android.content.Intent

class BotNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        val packageName = sbn?.packageName ?: return
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getString(Notification.EXTRA_TEXT) ?: ""

        if (text.isBlank()) return

        val prefs = getSharedPreferences("BotSettings", Context.MODE_PRIVATE)
        val allowPrivate = prefs.getBoolean("allow_private", true)
        val allowGroup = prefs.getBoolean("allow_group", false)

        val isGroup = title.contains("(@") || sbn.notification.category == Notification.CATEGORY_SOCIAL

        if (isGroup && !allowGroup) return
        if (!isGroup && !allowPrivate) return

        if (text.contains("السلام عليكم", ignoreCase = true)) {
            sendReply(sbn.notification, "وعليكم السلام ورحمة الله وبركاته، تم الاستلام بنجاح عبر البوت الآلي ✅")
        }
    }

    private fun sendReply(notification: Notification, replyText: String) {
        val wearableExtender = Notification.WearableExtender(notification)
        for (action in wearableExtender.actions) {
            if (action.remoteInputs != null) {
                for (remoteInput in action.remoteInputs) {
                    val intent = Intent()
                    val bundle = android.os.Bundle()
                    bundle.putCharSequence(remoteInput.resultKey, replyText)
                    RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
                    try {
                        action.actionIntent.send(this, 0, intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
