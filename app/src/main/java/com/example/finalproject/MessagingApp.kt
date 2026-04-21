package com.example.finalproject

import android.app.Application
import com.example.finalproject.data.AuthRepository
import com.example.finalproject.work.PriceAlertService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MessagingApp : Application() {

    @Inject lateinit var authRepo: AuthRepository

    override fun onCreate() {
        super.onCreate()
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                PriceAlertService.start(this)
            } else {
                PriceAlertService.stop(this)
            }
        }
    }
}