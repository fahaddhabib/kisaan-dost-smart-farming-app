package com.project.farmingapp.view.ecom

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
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.project.farmingapp.R
import com.project.farmingapp.databinding.FragmentEcomSellBinding
import com.project.farmingapp.viewmodel.UserDataViewModel
import java.io.IOException
import java.util.*

class EcomSellFragment : Fragment() {

    private lateinit var binding: FragmentEcomSellBinding

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var authUser: FirebaseAuth? = null
    private var postID: UUID? = null
    private var bitmap: Bitmap? = null
    private lateinit var registeredPhoneNo: String
    lateinit var ecomBuyFrag: EcommBuyFragment
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
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEcomSellBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registeredPhoneNo = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()

        binding.postContact.isEnabled = false
        binding.postContact.isFocusable = false
        binding.postContact.isCursorVisible = false
        binding.postContact.text = registeredPhoneNo

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "E-Commerce"

        dropDownInit()

        binding.progressCreatePost.visibility = View.GONE
        binding.progressTitle.visibility = View.GONE

        data2["uploadType"] = ""
        binding.chooseImgFloat.setOnClickListener {
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

        binding.createPostBtnSM.setOnClickListener {

            if (binding.postTitleSM.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter title",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.postPrice.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter price",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.postUnit.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter unit",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.postQuantity.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter quantity",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.postQuality.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter quality",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.postContact.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter contact number",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (binding.postLocation.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter location",
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
            binding.uploadImagePreview.setImageURI(filePath)
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
        binding.progressCreatePost.visibility = View.VISIBLE
        binding.progressTitle.visibility = View.VISIBLE
        if (filePath != null) {
            postID = UUID.randomUUID()
            val ref = storageReference?.child("posts/" + postID.toString())
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask =
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            binding.progressCreatePost.visibility = View.GONE
                            binding.progressTitle.visibility = View.GONE
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        addUploadRecordWithImageToDb(downloadUri.toString(), postID!!)
//                        binding.progressCreatePost.visibility = View.GONE
//                        binding.progressTitle.visibility = View.GONE
                    } else {
                        // Handle failures
                        binding.progressCreatePost.visibility = View.GONE
                        binding.progressTitle.visibility = View.GONE
                    }
                }?.addOnFailureListener {
                    binding.progressCreatePost.visibility = View.GONE
                    binding.progressTitle.visibility = View.GONE
                    Toast.makeText(requireActivity().applicationContext, it.message, Toast.LENGTH_LONG).show()
                }
        } else {
            data2["uploadType"] = ""
            addUploadRecordWithImageToDb(null, null)
            Log.d("File Type 2", "Null")
        }
    }

/*    private fun addUploadRecordWithTextToDb() {
        addUploadRecordWithImageToDb(null, null)
    }*/

    private fun addUploadRecordWithImageToDb(uri: String?, postID: UUID?) {

        if (!uri.isNullOrEmpty()) {
            data2["imageUrl"] = uri.toString()
            data2["imageID"] = postID.toString()

        }

        //val data3 = HashMap<String, Any>()
        val postTimeStamp = System.currentTimeMillis()

        data2["userID"] = authUser!!.currentUser!!.uid.toString()
        data2["timeStamp"] = postTimeStamp
        data2["title"] = binding.postTitleSM.text.toString()
        data2["price"] = binding.postPrice.text.toString()
        data2["unit"] = binding.postUnit.text.toString()
        data2["quantity"] = binding.postQuantity.text.toString()
        data2["quality"] = binding.postQuality.text.toString()
        data2["contact"] = binding.postContact.text.toString()
        data2["location"] = binding.postLocation.text.toString()
        data2["description"] = binding.descPostSM.text.toString()
        data2["likes"] = 0
        data2["likedByUser"] = false

        db.collection("posts")
            .add(data2)
            .addOnSuccessListener { documentReference ->

                val data = HashMap<String, Any>()
                val posts = arrayListOf<String>()
                val postRecordID = documentReference.id.toString()

                posts.add(postRecordID)
                data["posts"] = posts

                db.collection("users")
                    .document("${authUser!!.currentUser!!.uid.toString()}")
                    .update("posts", FieldValue.arrayUnion(postRecordID))
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Post Created",
                            Toast.LENGTH_LONG
                        ).show()

                        binding.progressCreatePost.visibility = View.GONE
                        binding.progressTitle.visibility = View.GONE
                        userDataViewModel.getUserData(authUser!!.currentUser!!.uid.toString())
                        ecomBuyFrag = EcommBuyFragment()
                        val transaction = requireActivity().supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, ecomBuyFrag, "smPostList")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .setReorderingAllowed(true)
                            .addToBackStack("smPostList")
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Error saving to DB",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.progressCreatePost.visibility = View.GONE
                        binding.progressTitle.visibility = View.GONE
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Error saving to DB",
                    Toast.LENGTH_LONG
                ).show()
                binding.progressCreatePost.visibility = View.GONE
                binding.progressTitle.visibility = View.GONE
            }
    }

    private fun dropDownInit() {


        val qualityList: ArrayList<String> = getQualityList()

        //Create qualityList adapter
        val topographyAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_singlechoice,
            qualityList
        )

        val unitList: ArrayList<String> = getUnitList()

        //Create qualityList adapter
        val unitAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_singlechoice,
            unitList
        )

        //Set adapters
        binding.postQuality.setAdapter(topographyAdapter)
        binding.postUnit.setAdapter(unitAdapter)


    }

}
private fun getQualityList(): ArrayList<String> {
    val qualityList: ArrayList<String> = ArrayList()
    qualityList.add("A")
    qualityList.add("B")
    qualityList.add("C")

    return qualityList
}

private fun getUnitList(): ArrayList<String> {
    val unitList: ArrayList<String> = ArrayList()
    unitList.add("KG")
    unitList.add("Unit")
    unitList.add("Liter")

    return unitList
}


private fun Any.setImageBitmap(bitmap: Bitmap?) {

}