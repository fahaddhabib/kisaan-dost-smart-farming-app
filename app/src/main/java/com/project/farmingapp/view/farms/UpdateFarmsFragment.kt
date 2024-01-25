package com.project.farmingapp.view.farms

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.project.farmingapp.R
import com.project.farmingapp.databinding.FragmentUpdateFarmsBinding
import com.project.farmingapp.view.user.UserFragment
import com.project.farmingapp.viewmodel.UserDataViewModel
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class UpdateFarmsFragment : Fragment() {

    private lateinit var binding: FragmentUpdateFarmsBinding

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var authUser: FirebaseAuth? = null
    private var postID: UUID? = null
    private var bitmap: Bitmap? = null
    lateinit var userFrag: UserFragment
    var farmId : String = ""
    var imageUrl : String = ""
    lateinit var userDataViewModel : UserDataViewModel
    val db = FirebaseFirestore.getInstance()
    val data2 = HashMap<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageReference = FirebaseStorage.getInstance().reference
        authUser = FirebaseAuth.getInstance()
        firebaseStore = FirebaseStorage.getInstance()

        userDataViewModel = ViewModelProviders.of(requireActivity())
            .get<UserDataViewModel>(UserDataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUpdateFarmsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "UPDATE FARMS"

        farmId = arguments?.getString("farmID").toString()

        val documentReference = FirebaseFirestore.getInstance().collection("farms").document(farmId)

        documentReference.get().addOnSuccessListener { currentFarm ->

            Toast.makeText(context, ""+ currentFarm.get("f_title").toString(), Toast.LENGTH_SHORT).show()

            binding.farmTitleUp.setText(currentFarm.get("f_title").toString())
            imageUrl = currentFarm.get("imageUrl").toString()
            Glide.with(this).load(imageUrl).into(binding.uploadImagePreviewUp)
            binding.farmLocationUp.setText(currentFarm.get("f_location").toString())
            binding.farmCityUp.setText(currentFarm.get("f_city").toString())
            binding.farmAreaAcreUp.setText(currentFarm.get("f_area").toString())
            binding.topographyAutoCompleteUp.setText(currentFarm.get("f_topography").toString())
            binding.farmSoilTypeAutoCompleteUp.setText(currentFarm.get("f_soiltype").toString())
            binding.farmIssueAutoCompleteUp.setText(currentFarm.get("f_soilissues").toString())
            binding.farmIrrigSourceAutoCompleteUp.setText(currentFarm.get("f_irrigsrc").toString())
            binding.cropsnvegitablesAutoCompleteUp.setText(currentFarm.get("f_cropveg").toString())


        }

        dropDownInit()

        binding.progressUpdateFarm.visibility = View.GONE
        binding.progressTitleUpdateFarm.visibility = View.GONE

        data2["uploadType"] = ""
        binding.chooseImgFloatUp.setOnClickListener {
            val intent = Intent()
            intent.type = "image/* video/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }

        val googleLoggedUser = authUser!!.currentUser!!.displayName
        if (googleLoggedUser.isNullOrEmpty()) {
            db.collection("users").document(authUser!!.currentUser!!.uid!!)
                .get()
                .addOnCompleteListener {
                    val data = it.result
                    data2["name"] = data!!.getString("name").toString()
                    Log.d("Google User", data!!.getString("name")!!)
                }
        } else {
            data2["name"] = googleLoggedUser.toString()
            Log.d("Normal User", googleLoggedUser)
        }

        binding.updatefarmBtn.setOnClickListener {

            if (binding.farmTitleUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter farm title",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.farmLocationUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter farm location",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.farmCityUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter farm city",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.farmAreaAcreUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter farm area",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.topographyAutoCompleteUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please select topography",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.farmSoilTypeAutoCompleteUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please select Soil Type",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.farmIssueAutoCompleteUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please select soil issues",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.farmIrrigSourceAutoCompleteUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please select farm Irrigation Source",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.cropsnvegitablesAutoCompleteUp.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please select crop type",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else {
                uploadImage().setImageBitmap(bitmap)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }

            filePath = data.data
            binding.uploadImagePreviewUp.setImageURI(filePath)
            try {
                val lastIndex = filePath.toString().length - 1
                val type =
                    filePath.toString().slice((filePath.toString().lastIndexOf(".") + 1)..lastIndex)

                Log.d("File Type", filePath.toString())

                if (filePath.toString().contains("png") || filePath.toString().contains("jpg") || filePath.toString().contains("jpeg") || filePath.toString().contains("image") || filePath.toString().contains("images")){
                    data2["uploadType"] = "image"
                } else if(filePath.toString().contains("videos") || filePath.toString().contains("video") || filePath.toString().contains("mp4")){
                    data2["uploadType"] = "video"
                }

                Log.d("File Type 3", data2["uploadType"].toString())
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, filePath)

//                uploadImage().setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        binding.progressUpdateFarm.visibility = View.VISIBLE
        binding.progressTitleUpdateFarm.visibility = View.VISIBLE
        if (filePath != null) {
            postID = UUID.randomUUID()
            val ref = storageReference?.child("farms/" + postID.toString())
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask =
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            binding.progressUpdateFarm.visibility = View.GONE
                            binding.progressTitleUpdateFarm.visibility = View.GONE
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result

                        userDataViewModel.updateUserFarm(requireActivity().applicationContext, farmId,
                            binding.farmTitleUp.text.toString(), downloadUri.toString(), binding.farmLocationUp.text.toString()
                            , binding.farmCityUp.text.toString(), binding.farmAreaAcreUp.text.toString(), binding.topographyAutoCompleteUp.text.toString()
                            , binding.farmSoilTypeAutoCompleteUp.text.toString(), binding.farmIssueAutoCompleteUp.text.toString(), binding.farmIrrigSourceAutoCompleteUp.text.toString()
                            , binding.cropsnvegitablesAutoCompleteUp.text.toString(),authUser!!.currentUser!!.uid.toString())

                        userDataViewModel.getUserData(authUser!!.currentUser!!.uid.toString())

                       // addUploadRecordWithImageToDb(downloadUri.toString(), postID!!)

                        binding.progressUpdateFarm.visibility = View.GONE
                        binding.progressTitleUpdateFarm.visibility = View.GONE

                        userFrag = UserFragment()
                        val transaction = requireActivity().supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, userFrag, "userfarm")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .setReorderingAllowed(true)
                            .addToBackStack("userfarm")
                            .commit()

                    } else {
                        // Handle failures
                        binding.progressUpdateFarm.visibility = View.GONE
                        binding.progressTitleUpdateFarm.visibility = View.GONE
                    }
                }?.addOnFailureListener {
                    binding.progressUpdateFarm.visibility = View.GONE
                    binding.progressTitleUpdateFarm.visibility = View.GONE
                    Toast.makeText(requireActivity().applicationContext, it.message, Toast.LENGTH_LONG).show()
                }
        } else {
            data2["uploadType"] = ""

          //    addUploadRecordWithImageToDb(null, null)
            userDataViewModel.updateUserFarm(requireActivity().applicationContext, farmId,
                binding.farmTitleUp.text.toString(), null, binding.farmLocationUp.text.toString()
                , binding.farmCityUp.text.toString(), binding.farmAreaAcreUp.text.toString(), binding.topographyAutoCompleteUp.text.toString()
                , binding.farmSoilTypeAutoCompleteUp.text.toString(), binding.farmIssueAutoCompleteUp.text.toString(), binding.farmIrrigSourceAutoCompleteUp.text.toString()
                , binding.cropsnvegitablesAutoCompleteUp.text.toString(),authUser!!.currentUser!!.uid.toString())

           userDataViewModel.getUserData(authUser!!.currentUser!!.uid.toString())
            userFrag = UserFragment()
            val transaction = requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, userFrag, "userfarm")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("userfarm")
                .commit()

            binding.progressUpdateFarm.visibility = View.GONE
            binding.progressTitleUpdateFarm.visibility = View.GONE

            Log.d("File Type 2", "Null")
        }
    }

    private fun addUploadRecordWithImageToDb(uri: String?, postID: UUID?) {

        if (!uri.isNullOrEmpty()) {
            data2["imageUrl"] = uri.toString()
            data2["imageID"] = postID.toString()
        }

        val postTimeStamp = System.currentTimeMillis()

        data2["userID"] = authUser!!.currentUser!!.uid.toString()
        data2["timeStamp"] = postTimeStamp
        data2["f_title"] = binding.farmTitleUp.text.toString()
        data2["f_location"] = binding.farmLocationUp.text.toString()
        data2["f_city"] = binding.farmCityUp.text.toString()
        data2["f_area"] = binding.farmAreaAcreUp.text.toString()
        data2["f_topography"] = binding.topographyAutoCompleteUp.text.toString()
        data2["f_soiltype"] = binding.farmSoilTypeAutoCompleteUp.text.toString()
        data2["f_soilissues"] = binding.farmIssueAutoCompleteUp.text.toString()
        data2["f_irrigsrc"] = binding.farmIrrigSourceAutoCompleteUp.text.toString()
        data2["f_cropveg"] = binding.cropsnvegitablesAutoCompleteUp.text.toString()


        db.collection("farms")
            .add(data2)
            .addOnSuccessListener { documentReference ->

                val data = HashMap<String, Any>()
                val posts = arrayListOf<String>()
                val postRecordID = documentReference.id

                posts.add(postRecordID)
                data["farms"] = posts

                db.collection("users")
                    .document(authUser!!.currentUser!!.uid.toString())
                    .update("farms", FieldValue.arrayUnion(postRecordID))
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Farm Added",
                            Toast.LENGTH_LONG
                        ).show()

                        binding.progressUpdateFarm.visibility = View.GONE
                        binding.progressTitleUpdateFarm.visibility = View.GONE
                        userDataViewModel.getUserData(authUser!!.currentUser!!.uid.toString())
                        userFrag = UserFragment()
                        val transaction = requireActivity().supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, userFrag, "userfarm")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .setReorderingAllowed(true)
                            .addToBackStack("userfarm")
                            .commit()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Error saving to DB",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.progressUpdateFarm.visibility = View.GONE
                        binding.progressTitleUpdateFarm.visibility = View.GONE
                    }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Error saving to DB",
                    Toast.LENGTH_LONG
                ).show()
                binding.progressUpdateFarm.visibility = View.GONE
                binding.progressTitleUpdateFarm.visibility = View.GONE
            }
    }


    private fun dropDownInit() {


        val topographyList: ArrayList<String> = getTopographyList()
        val soilTypeACTList: ArrayList<String> = getSoilTypeACTList()
        val soilIssuesACTList: ArrayList<String> = getSoilIssuesACTList()
        val irrigationSrcACTList: ArrayList<String> = getIrrigationSrcACTList()
        val cropsnvegeACTList: ArrayList<String> = getCropsnvegeACTList()

        //Create topographyList adapter
        val topographyAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_singlechoice,
            topographyList
        )

        //Create soilTypeACTList adapter
        val soilTypeAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_singlechoice,
            soilTypeACTList
        )

        //Create soilIssuesACTList adapter
        val soilIssuesAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_singlechoice,
            soilIssuesACTList
        )

        //Create IrrigationSrcACTList adapter
        val irrigationSrcAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_singlechoice,
            irrigationSrcACTList
        )

        //Create cropsnvegeACTList adapter
        val cropsnvegeAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_singlechoice,
            cropsnvegeACTList
        )



        //Set adapters
        binding.topographyAutoCompleteUp.setAdapter(topographyAdapter)
        binding.farmSoilTypeAutoCompleteUp.setAdapter(soilTypeAdapter)
        binding.farmIssueAutoCompleteUp.setAdapter(soilIssuesAdapter)
        binding.farmIrrigSourceAutoCompleteUp.setAdapter(irrigationSrcAdapter)
        binding.cropsnvegitablesAutoCompleteUp.setAdapter(cropsnvegeAdapter)

    }


}

//topographyArrayList
private fun getTopographyList(): ArrayList<String> {
    val topographyList: ArrayList<String> = ArrayList()
    topographyList.add("Arid")
    topographyList.add("Irrigated Plains")
    topographyList.add("Semi Arid")
    topographyList.add("Sandy Deserts")
    topographyList.add("Mountainous")
    topographyList.add("Sub Mountainous")
    topographyList.add("Coastal Areas")
    topographyList.add("Piedmonts")
    topographyList.add("Snowy Mountains")
    return topographyList
}

//SoilTypeACTList
private fun getSoilTypeACTList(): ArrayList<String> {
    val soilTypeACTList: ArrayList<String> = ArrayList()
    soilTypeACTList.add("Highly Fertile")
    soilTypeACTList.add("Low Fertile Soil")
    soilTypeACTList.add("Sandy Soil")
    soilTypeACTList.add("Fertile Soil")
    soilTypeACTList.add("Medium Fertile Soil")
    soilTypeACTList.add("Clayey Soil")
    soilTypeACTList.add("Sandy Loam Soil")
    soilTypeACTList.add("Loamy Soil")
    return soilTypeACTList
}

//SoilIssueACTList
private fun getSoilIssuesACTList(): ArrayList<String> {
    val soilIssuesACTList: ArrayList<String> = ArrayList()
    soilIssuesACTList.add("Saline Soil")
    soilIssuesACTList.add("Sodic Soil")
    soilIssuesACTList.add("Water Logged")
    soilIssuesACTList.add("Normal Soil")
    return soilIssuesACTList
}

//irrigationSrcACTList
private fun getIrrigationSrcACTList(): ArrayList<String> {
    val irrigationSrcACTList: ArrayList<String> = ArrayList()
    irrigationSrcACTList.add("Canal")
    irrigationSrcACTList.add("Tube Well")
    irrigationSrcACTList.add("Rain Water")
    irrigationSrcACTList.add("Pond")
    irrigationSrcACTList.add("DAM")
    irrigationSrcACTList.add("Drip Irrigation")
    return irrigationSrcACTList
}

//cropsnvegeACTList
private fun getCropsnvegeACTList(): ArrayList<String> {
    val cropsnvegeACTList: ArrayList<String> = ArrayList()
    cropsnvegeACTList.add("Crops")
    cropsnvegeACTList.add("Fruits")
    cropsnvegeACTList.add("Vegetable")
    return cropsnvegeACTList
}

private fun Any.setImageBitmap(bitmap: Bitmap?) {

}