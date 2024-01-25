package com.project.farmingapp.view.auth

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.project.farmingapp.databinding.ActivityUserSignUpFormBinding
import com.project.farmingapp.view.dashboard.DashboardActivity
import com.project.farmingapp.viewmodel.UserDataViewModel
import java.util.*

class UserSignUpForm : AppCompatActivity() {

    private lateinit var binding: ActivityUserSignUpFormBinding

    private var bitmap: Bitmap? = null
    private var selectedImageUri: Uri? = null
    private var postID: UUID? = null
    private val PICK_IMAGE_REQUEST = 71


    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var storageReference: StorageReference? = null

    lateinit var userDataViewModel : UserDataViewModel

    private lateinit var registeredPhoneNo: String
    private var imageurl: String ="imageurl"
    private var imageid: String ="imageid"

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // Handle the image data here
            if (data != null && data.data != null) {
                 selectedImageUri = data.data!!
                // Process the selected image URI
                // ...
                // Set the selected image URI to the Fresco DraweeView
                Toast.makeText(applicationContext,selectedImageUri.toString(), Toast.LENGTH_SHORT).show();
                binding.profileImagee.setImageURI(selectedImageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSignUpFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        registeredPhoneNo = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()

        userDataViewModel = ViewModelProviders.of(this)
            .get<UserDataViewModel>(UserDataViewModel::class.java)

        binding.progressBar.visibility = View.GONE

        binding.phoneNumber.isEnabled = false
        binding.phoneNumber.isFocusable = false
        binding.phoneNumber.isCursorVisible = false
        binding.phoneNumber.text = registeredPhoneNo

        binding.dob.setOnClickListener {
            openDatePicker()
        }


        binding.btnSave.setOnClickListener() {

            uploadImage()

        }

        dropDownInit()
// Set up click listener on the ImageView
        binding.btnChooseImage.setOnClickListener {
            //pickImageFromGallery()

            // Request runtime permission if needed
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                // Permission is already granted, proceed with image picking
                pickImage()
            }
        }
    }


    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with image picking
                pickImage()
            } else {
                // Permission denied, handle accordingly
                // ...
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 71
    }

    private fun registerUser(uri: String?, postID: UUID?) {

        if (!uri.isNullOrEmpty()) {

             imageurl = uri.toString()
             imageid = postID.toString()

            }

        val postTimeStamp = System.currentTimeMillis()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val name = binding.txtFullName.text.toString().trim()
        val dob = binding.dob.text.toString().trim()
        val city = binding.city.text.toString().trim()
        val exp = binding.experience.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val about = binding.about.text.toString().trim()
        val genderStr = binding.genderAutoComplete.text.toString().trim()
        val occupationStr = binding.occupationAutoComplete.text.toString().trim()

        if (userId != null && name.isNotEmpty()) {
            val user = hashMapOf(
                "name" to name,
                "dob" to dob,
                "city" to city,
                "experience" to exp,
                "phone" to registeredPhoneNo,
                "email" to email,
                "about" to about,
                "gender" to genderStr,
                "occupation" to occupationStr,
                "imageurl" to imageurl,
                "imageid" to imageid,
                "timestamp" to postTimeStamp
            )

            firestore.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener {
                    Log.d("signUpform", "User added to Firestore")
                    Toast.makeText(applicationContext,"User Created Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(applicationContext,"Error adding user to Firestore", Toast.LENGTH_SHORT).show()

                    Log.e("signUpform", "Error adding user to Firestore", e)
                }


            val hashMap = HashMap<String, String>()
            hashMap["id"] = userId
            hashMap["username"] = name
            hashMap["emailId"] = email
            hashMap["timestamp"] = postTimeStamp.toString()
            hashMap["imageUrl"] = imageurl
            hashMap["bio"] = "Hey there!"
            hashMap["status"] = "offline"
            hashMap["search"] = name.toLowerCase()

            FirebaseDatabase.getInstance().getReference("Users").child(userId).setValue(hashMap)
                .addOnCompleteListener(
                    OnCompleteListener<Void?> {
                        //    successAddUserDb.setValue(true)
                        Log.d("signUpform", "User added to Firestore")
                        Toast.makeText(applicationContext,"User Created Successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                    })
                .addOnFailureListener(
                    OnFailureListener {
                        // successAddUserDb.setValue(false)
                    })

        }

        Log.d("signUpform", "User signed in with phone number")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK ) {
            if (data == null || data.data == null) {
                return
            }

            selectedImageUri = data.data
            // Set the selected image URI to the Fresco DraweeView
            binding.profileImagee.setImageURI(selectedImageUri)

        }
    }

    //date picker
    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.dob.setText(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    //image picker
    private fun uploadImage() {
        binding.progressBar.visibility = View.VISIBLE

        Log.d("selectedEInage:", ""+selectedImageUri)
        if (selectedImageUri != null) {
            postID = UUID.randomUUID()
            val ref = storageReference?.child("users/" + postID.toString())
            val uploadTask = ref?.putFile(selectedImageUri!!)

            val urlTask =
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result

                        Log.d("selectedEInage:", ""+downloadUri)
                        registerUser(downloadUri.toString(), postID!!)
//                        binding.progressBar.visibility = View.GONE
                    } else {
                        // Handle failures
                        binding.progressBar.visibility = View.GONE
                    }
                }?.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext.applicationContext, it.message, Toast.LENGTH_LONG).show()
                }
        } else {
            registerUser(null, null)
            Log.d("File Type 2", "Null")
        }
    }


    private fun dropDownInit() {


        val genderList: ArrayList<String> = getGenderList()
        val soilTypeACTList: ArrayList<String> = getOccupationList()


        val genderAdapter = ArrayAdapter(
            applicationContext,
            android.R.layout.select_dialog_singlechoice,
            genderList
        )

        val occupationTypeAdapter = ArrayAdapter(
            applicationContext,
            android.R.layout.select_dialog_singlechoice,
            soilTypeACTList
        )

        //Set adapters
        binding.genderAutoComplete.setAdapter(genderAdapter)
        binding.occupationAutoComplete.setAdapter(occupationTypeAdapter)
    }
}

private fun getGenderList(): ArrayList<String> {
    val genderList: ArrayList<String> = ArrayList()
    genderList.add("Male")
    genderList.add("Female")
    genderList.add("Other")
    return genderList
}

private fun getOccupationList(): ArrayList<String> {
    val occupationList: ArrayList<String> = ArrayList()
    occupationList.add("Farmer")
    occupationList.add("Non-Farmer")
    occupationList.add("Expert In Farming")
    return occupationList
}

private fun Any.setImageBitmap(bitmap: Bitmap?) {

}