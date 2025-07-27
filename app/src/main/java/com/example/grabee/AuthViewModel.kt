package com.example.grabee


import android.widget.Toast
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

fun registerUser(email: String, password: String, role: String, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->
            val uid = result.user?.uid ?: return@addOnSuccessListener
            val user = hashMapOf(
                "email" to email,
                "role" to role
            )
            db.collection("users").document(uid).set(user)
                .addOnSuccessListener {
                    Toast.makeText(context, "Registered as $role", Toast.LENGTH_SHORT).show()
                }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Register failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}

fun loginUser(email: String, password: String, context: Context, onSuccess: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    auth.signInWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->
            val uid = result.user?.uid ?: return@addOnSuccessListener
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role") ?: "Unknown"
                    Toast.makeText(context, "Logged in as $role", Toast.LENGTH_SHORT).show()
                    onSuccess(role)
                }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}
