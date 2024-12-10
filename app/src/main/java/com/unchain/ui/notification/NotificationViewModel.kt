package com.unchain.ui.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unchain.data.model.Notification

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    init {
        // TODO: Load notifications from your data source
        loadDummyNotifications()
    }

    private fun loadDummyNotifications() {
        // This is just dummy data for testing
        val dummyNotifications = listOf(
            Notification(
                "1",
                "Welcome to UnChain Beta!",
                "Thank you for being an early user of UnChain. We're constantly improving the app based on user feedback.",
                System.currentTimeMillis() - 3600000 // 1 hour ago
            ),
            Notification(
                "2",
                "App Under Development",
                "We're working on exciting new features! Some features might be limited as we're still in beta. Stay tuned for updates!",
                System.currentTimeMillis() - 7200000 // 2 hours ago
            ),
            Notification(
                "3",
                "Coming Soon: Enhanced Features",
                "We're developing advanced sugar tracking, personalized recommendations, and more detailed analytics. Your feedback helps us prioritize what to build next!",
                System.currentTimeMillis() - 10800000 // 3 hours ago
            )
        )
        _notifications.value = dummyNotifications
    }
}
