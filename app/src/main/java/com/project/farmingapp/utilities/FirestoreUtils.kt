package com.project.farmingapp.utilities

import com.google.firebase.firestore.FirebaseFirestore

fun checkUserExists(phoneNumber: String, callback: (Boolean) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val usersCollection = firestore.collection("users")

    usersCollection
        .whereEqualTo("phone", phoneNumber)
        .get()
        .addOnSuccessListener { querySnapshot ->
            val exists = !querySnapshot.isEmpty
            callback(exists)
        }
        .addOnFailureListener { exception ->
            callback(false)
        }
}