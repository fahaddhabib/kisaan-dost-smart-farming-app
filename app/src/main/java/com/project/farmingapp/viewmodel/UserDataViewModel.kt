package com.project.farmingapp.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserDataViewModel : ViewModel() {

    var userliveData = MutableLiveData<DocumentSnapshot>()

    fun getUserData(userId: String) {
        val firebaseFireStore = FirebaseFirestore.getInstance()
Log.e("userIdUpdate: ",userId)
        firebaseFireStore.collection("users").document(userId)
            .get()
            .addOnCompleteListener {
                userliveData.value = it.result
            }
    }

    fun updateUserField(context: Context, userID: String, about: String?, city: String?) {

        if (about !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("users").document("${userID}")
                .update(
                    mapOf(
                        "about" to about
                    )
                )
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "User About Data Updated")
                    getUserData(userID)
                }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update About User Data")
                    Toast.makeText(context, "Failed to Update About. Try Again!", Toast.LENGTH_SHORT).show()
                }
        }

        if (city !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("users").document("${userID}")
                .update(
                    mapOf(
                        "city" to city
                    )
                )
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "User City Data Updated")
                    getUserData(userID)
                }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update City User Data")
                    Toast.makeText(context, "Failed to Update City Try Again!", Toast.LENGTH_SHORT).show()
                }
        }
        Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
    }

    fun updateUserFarm(context: Context, farmID: String, farm_title: String?, image_url: String?, farm_location: String?, farm_city: String?
                       , farm_area: String?, topography: String?, soil_type: String?, soil_issue: String?, irrig: String?, crop_veg: String?, userID: String) {

        if (farm_title !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf(
                        "f_title" to farm_title
                    )
                )
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "User FarmTitle Data Updated")
                    getUserData(userID)
                }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update FarmTitle User Data")
                    Toast.makeText(context, "Failed to Update FarmTitle. Try Again!", Toast.LENGTH_SHORT).show()
                }
        }

        if (image_url !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("imageUrl" to image_url))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm ImageUrl Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm ImageUrl Data")
                    Toast.makeText(context, "Failed to Update Farm ImageUrl Try Again!", Toast.LENGTH_SHORT).show()
                } }
        if (farm_location !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("f_location" to farm_location))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm location Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm location Data")
                    Toast.makeText(context, "Failed to Update Farm location Try Again!", Toast.LENGTH_SHORT).show()
                } }

        if (farm_city !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("f_city" to farm_city))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm city Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm city Data")
                    Toast.makeText(context, "Failed to Update Farm city Try Again!", Toast.LENGTH_SHORT).show()
                } }
        if (farm_area !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("f_area" to farm_area))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm area Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm area Data")
                    Toast.makeText(context, "Failed to Update Farm area Try Again!", Toast.LENGTH_SHORT).show()
                } }
        if (topography !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("f_topography" to topography))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm topography Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm topography Data")
                    Toast.makeText(context, "Failed to Update Farm topography Try Again!", Toast.LENGTH_SHORT).show()
                } }
        if (soil_type !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("f_soiltype" to soil_type))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm soil type Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm soil type Data")
                    Toast.makeText(context, "Failed to Update Farm soil type Try Again!", Toast.LENGTH_SHORT).show()
                } }
        if (soil_issue !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("f_soilissues" to soil_issue))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm soil issues Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm soil issues Data")
                    Toast.makeText(context, "Failed to Update Farm soil issues Try Again!", Toast.LENGTH_SHORT).show()
                } }
        if (irrig !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("f_irrigsrc" to irrig))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm irrigation source Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm irrigation source Data")
                    Toast.makeText(context, "Failed to Update Farm irrigation source Try Again!", Toast.LENGTH_SHORT).show()
                } }
        if (crop_veg !=null) {
            val firebaseFireStore = FirebaseFirestore.getInstance()
            firebaseFireStore.collection("farms").document("${farmID}")
                .update(
                    mapOf("f_cropveg" to crop_veg))
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Farm crops and vege Data Updated")
                    getUserData(userID) }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to Update Farm crops and vege Data")
                    Toast.makeText(context, "Failed to Update Farm crops and vege Try Again!", Toast.LENGTH_SHORT).show()
                } }

        Toast.makeText(context, "Farm Updated", Toast.LENGTH_SHORT).show()
    }

    fun deleteUserPost(userId: String, postId: String){
        val firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.d("User Data View Model", "Post Deleted")
                UserProfilePostsViewModel().getAllPosts(userId)
                firebaseFirestore.collection("users").document(userId).update("posts", FieldValue.arrayRemove("${postId}"))
                    .addOnSuccessListener {
                        Log.d("UserDataViewModel", "Successfully Deleted User Doc Post")
                        getUserData(userId)
                    }
                    .addOnFailureListener{
                        Log.e("UserDataViewModel", "Failed to delete post from User Doc")
                    }
            }
            .addOnFailureListener {
                Log.d("User Data View Model", "Failed to delete post")
            }
    }

    fun deleteUserFarm(userId: String, farmId: String){
        val firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("farms").document(farmId)
            .delete()
            .addOnSuccessListener {
                Log.d("User Data View Model", "Farm Deleted")
                UserProfileFarmViewModel().getAllFarms(userId)
                firebaseFirestore.collection("users").document(userId).update("farms", FieldValue.arrayRemove("${farmId}"))
                    .addOnSuccessListener {
                        Log.d("UserDataViewModel", "Successfully Deleted User Doc Farm")
                        getUserData(userId)
                    }
                    .addOnFailureListener{
                        Log.e("UserDataViewModel", "Failed to delete post from User Doc")
                    }
            }
            .addOnFailureListener {
                Log.d("User Data View Model", "Failed to delete farm")
            }
    }
}