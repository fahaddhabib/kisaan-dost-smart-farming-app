package com.project.farmingapp.view.auth

import android.app.DatePickerDialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.farmingapp.databinding.ActivityUpdateUserDataBinding
import com.project.farmingapp.viewmodel.UserDataViewModel
import java.util.*

class UpdateUserData : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateUserDataBinding

    private lateinit var userDataViewModel: UserDataViewModel
    private lateinit var registeredPhoneNo: String

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var storageReference: StorageReference? = null

    private lateinit var genderList: ArrayList<String>
    private lateinit var occupationList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateUserDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        registeredPhoneNo = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()

        userDataViewModel = ViewModelProviders.of(this)
            .get<UserDataViewModel>(UserDataViewModel::class.java)

        setupViews()
        getLoggedInUser()
    }

    private fun setupViews() {
        binding.apply {
            binding.phoneNumberUp.isEnabled = false
            binding.phoneNumberUp.isFocusable = false
            binding.phoneNumberUp.isCursorVisible = false

            binding.progressBarUp.visibility = View.GONE

            binding.btnUp.setOnClickListener {
                registerUser()
            }

            binding.dobUp.setOnClickListener {
                openDatePicker()
            }

            genderList = getGenderList()
            occupationList = getOccupationList()

            val genderAdapter = ArrayAdapter(
                applicationContext,
                android.R.layout.select_dialog_singlechoice,
                genderList
            )

            val occupationAdapter = ArrayAdapter(
                applicationContext,
                android.R.layout.select_dialog_singlechoice,
                occupationList
            )

            binding.genderAutoCompleteUp.setAdapter(genderAdapter)
            binding.occupationAutoCompleteUp.setAdapter(occupationAdapter)
        }
    }

    private fun getLoggedInUser() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        firestore.collection("users")
            .document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.data

                    binding.apply {
                        binding.txtFullNameUp.setText(userData?.get("name") as? String)
                        binding.dobUp.setText(userData?.get("dob") as? String)
                        binding.cityUp.setText(userData?.get("city") as? String)
                        binding.experienceUp.setText(userData?.get("experience") as? String)
                        binding.phoneNumberUp.setText(userData?.get("phone") as? String)
                        binding.emailUp.setText(userData?.get("email") as? String)
                        binding.aboutUp.setText(userData?.get("about") as? String)
                        binding.genderAutoCompleteUp.setText(userData?.get("gender") as? String)
                        binding.occupationAutoCompleteUp.setText(userData?.get("occupation") as? String)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle the error appropriately
            }
    }

    private fun registerUser() {
        val postTimeStamp = System.currentTimeMillis()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val name = binding.txtFullNameUp.text.toString().trim()
        val dob = binding.dobUp.text.toString().trim()
        val city = binding.cityUp.text.toString().trim()
        val exp = binding.experienceUp.text.toString().trim()
        val email = binding.emailUp.text.toString().trim()
        val about = binding.aboutUp.text.toString().trim()
        val genderStr = binding.genderAutoCompleteUp.text.toString().trim()
        val occupationStr = binding.occupationAutoCompleteUp.text.toString().trim()

        if (userId != null && name.isNotEmpty()) {
            val updatedUserData = hashMapOf<String, Any>(
                "name" to name,
                "dob" to dob,
                "city" to city,
                "experience" to exp,
                "phone" to registeredPhoneNo,
                "email" to email,
                "about" to about,
                "gender" to genderStr,
                "occupation" to occupationStr
            )

            firestore.collection("users")
                .document(userId)
                .set(updatedUserData)
                .addOnSuccessListener {
                    Toast.makeText(
                        applicationContext,
                        "User data updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        applicationContext,
                        "Failed to update user data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(
                applicationContext,
                "Please enter a valid name",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.dobUp.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun getGenderList(): ArrayList<String> {
        return arrayListOf("Male", "Female", "Other")
    }

    private fun getOccupationList(): ArrayList<String> {
        return arrayListOf(
            "Farmer",
            "Non-Farmer",
            "Expert In Farming"
        )
    }
}

private fun Any.setImageBitmap(bitmap: Bitmap?) {

}