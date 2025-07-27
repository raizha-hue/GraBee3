package com.example.grabee

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.grabee.ui.theme.GraBeeTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.firestore.ktx.firestore


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

        if (BuildConfig.DEBUG) {
            Firebase.storage.useEmulator("10.0.2.2", 9199)
            Firebase.firestore.useEmulator("10.0.2.2", 8080)
        }




        setContent {
            GraBeeTheme {
                MainNavigation() // ← Changed from AppNavigation() to MainNavigation()
            }
        }
    }
}