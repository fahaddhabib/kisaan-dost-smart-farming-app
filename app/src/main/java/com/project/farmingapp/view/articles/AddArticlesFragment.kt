package com.project.farmingapp.view.articles

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.project.farmingapp.databinding.FragmentAddArticlesBinding
import com.project.farmingapp.view.ecom.EcomSellFragment
import com.project.farmingapp.viewmodel.UserDataViewModel
import java.io.IOException
import java.util.*

class AddArticlesFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var authUser: FirebaseAuth? = null
    private var postID: UUID? = null
    private var bitmap: Bitmap? = null
    private lateinit var binding: FragmentAddArticlesBinding
    private lateinit var userDataViewModel: UserDataViewModel
    private val db = FirebaseFirestore.getInstance()
    private val data2 = HashMap<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageReference = FirebaseStorage.getInstance().reference
        authUser = FirebaseAuth.getInstance()
        firebaseStore = FirebaseStorage.getInstance()

        userDataViewModel = ViewModelProviders.of(requireActivity())
            .get(UserDataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SMCreatePostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EcomSellFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Add Article"

        binding.progressCreatePost.visibility = View.GONE
        binding.progressTitle.visibility = View.GONE

        data2["uploadType"] = ""
        binding.chooseArticleImgFloat.setOnClickListener {
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

        binding.addArticleBtn.setOnClickListener {

            if (binding.articleTitle.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter title",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.articleDescription.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter price",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
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
            binding.uploadImageArticle.setImageURI(filePath)
            try {
                val lastIndex = filePath.toString().length - 1
                val type =
                    filePath.toString().slice((filePath.toString().lastIndexOf(".") + 1)..lastIndex)

                Log.d("File Type", filePath.toString())

                if (filePath.toString().contains("png") || filePath.toString().contains("jpg") || filePath.toString().contains("jpeg") || filePath.toString().contains("image") || filePath.toString().contains("images")) {
                    data2["uploadType"] = "image"
                } else if (filePath.toString().contains("videos") || filePath.toString().contains("video") || filePath.toString().contains("mp4")) {
                    data2["uploadType"] = "video"
                }

                Log.d("File Type 3", data2["uploadType"].toString())
                bitmap =
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, filePath)

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
            val ref = storageReference?.child("articles/" + postID.toString())
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

    private fun addUploadRecordWithImageToDb(uri: String?, postID: UUID?) {

        if (!uri.isNullOrEmpty()) {
            data2["imageUrl"] = uri.toString()
            data2["imageID"] = postID.toString()
        }

        val postTimeStamp = System.currentTimeMillis()

        data2["userID"] = authUser!!.currentUser!!.uid.toString()
        data2["timeStamp"] = postTimeStamp
        data2["title"] = binding.articleTitle.text.toString()
        data2["description"] = binding.articleDescription.text.toString()
        data2["likes"] = 0
        data2["likedByUser"] = false

        db.collection("articles")
            .add(data2)
            .addOnSuccessListener { documentReference ->

                val data = HashMap<String, Any>()
                val posts = arrayListOf<String>()
                val postRecordID = documentReference.id.toString()

                posts.add(postRecordID)
                data["articles"] = posts

                db.collection("users")
                    .document("${authUser!!.currentUser!!.uid.toString()}")
                    .update("articles", FieldValue.arrayUnion(postRecordID))
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Article Created",
                            Toast.LENGTH_LONG
                        ).show()

                        binding.progressCreatePost.visibility = View.GONE
                        binding.progressTitle.visibility = View.GONE
                        userDataViewModel.getUserData(authUser!!.currentUser!!.uid.toString())
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
}


private fun Any.setImageBitmap(bitmap: Bitmap?) {

}