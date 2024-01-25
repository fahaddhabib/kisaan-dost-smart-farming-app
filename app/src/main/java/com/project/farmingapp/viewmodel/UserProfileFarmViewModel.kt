package com.project.farmingapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFarmViewModel : ViewModel() {

    val userProfileFarmLiveData = MutableLiveData<ArrayList<HashMap<String, Any>>>()
    val userProfileFarmLiveData2 = MutableLiveData<List<String>>()

    val liveData1 = MutableLiveData<List<String>>()
    val liveData2 = MutableLiveData<ArrayList<DocumentSnapshot>>()
    val liveData3 = MutableLiveData<ArrayList<DocumentSnapshot>>()


    fun getUserFarms(userID: String?) {
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val firebaseFirestore2 = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("users").document("$userID")
            .get()
            .addOnSuccessListener { it ->
                val allPostsIDs: List<String> = it.get("farms") as List<String>

                userProfileFarmLiveData2.value = allPostsIDs
                Log.d("User All Posts 3", userProfileFarmLiveData2.value.toString())

                val localPostList : ArrayList<HashMap<String, Any>>?= null

                for (i in allPostsIDs) {
                    firebaseFirestore2.collection("farms").document(i)
                        .get()
                        .addOnSuccessListener {
                            Log.d("User All Posts", it.data.toString())
//                            localPostList?.add(it)


//                            var localData = it.data


                            val someData = it.data as HashMap<String, Any>
                            localPostList?.add(someData)
                            if(localPostList!=null){
                                Log.d("User All Posts 2", localPostList.toString())
                            }

                            Log.d("User All Posts 4", localPostList?.size.toString())
                            userProfileFarmLiveData.value?.add(it.data as HashMap<String, Any>)
                        }
                        .addOnFailureListener {
                            Log.d("User All Posts", "Some Failure Occured while fetching user farms")
                        }
                }

            }.addOnFailureListener {
                Log.d("User All Posts 2", "Some Failure Occured while fetching user farms")
            }

    }

    fun getUserFarmsIDs(userID: String?){
        val firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("users").document(userID!!)
            .get()
            .addOnSuccessListener {
                liveData1.value = it.get("farms") as List<String>
            }
    }

    fun getAllFarmsOfUser(listOfIDs : List<String>){
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val someList = ArrayList<DocumentSnapshot>()
        val i = 0
        val j = listOfIDs.size
        for (i in 0 until j){
            firebaseFirestore.collection("farms").document(listOfIDs[i])
                .get()
                .addOnSuccessListener {
                    someList.add(it)
                    Log.d("LiveData2 - 2", it.toString())
                    Log.d("LiveData2 - 2", someList.toString())
//                        liveData2.value?.add(it!!)

                }
        }
        if (i == j){
            liveData2.value = someList
            Log.d("LiveData2 - 1", liveData2.value.toString())
        }

    }

    fun getAllFarms(userId: String?){
        val firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("farms").whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener {
                liveData3.value = it.documents as ArrayList<DocumentSnapshot>
                Log.d("UserPrlPostsViewModel", "Updated data")
            }
            .addOnFailureListener {
                Log.d("Error", "Error in all docs")
            }
    }
}