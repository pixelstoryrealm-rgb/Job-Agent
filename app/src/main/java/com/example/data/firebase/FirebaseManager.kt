package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Manages Firebase initialization state and service references.
 * Implements graceful fallback when google-services.json has not yet been placed in the project.
 */
class FirebaseManager(private val context: Context) {

    val isFirebaseConfigured: Boolean by lazy {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                true
            } else {
                val app = FirebaseApp.initializeApp(context)
                app != null
            }
        } catch (e: Exception) {
            Log.w("FirebaseManager", "Firebase not yet initialized. Operating in local-resilient mode: ${e.message}")
            false
        }
    }

    val auth: FirebaseAuth?
        get() = if (isFirebaseConfigured) {
            try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        } else null

    val firestore: FirebaseFirestore?
        get() = if (isFirebaseConfigured) {
            try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
        } else null

    val storage: FirebaseStorage?
        get() = if (isFirebaseConfigured) {
            try { FirebaseStorage.getInstance() } catch (e: Exception) { null }
        } else null

    companion object {
        const val SETUP_GUIDE = """
To enable cloud synchronization with Firebase:
1. Create a project in the Firebase Console (console.firebase.google.com).
2. Add an Android app with package name: com.aistudio.aijobassistant.asifqk
3. Download google-services.json and upload it to the /app directory.
4. In Firebase Console, enable Authentication (Email/Password & Google), Cloud Firestore, and Cloud Storage.
5. Deploy firestore.rules and storage.rules.
"""
    }
}
