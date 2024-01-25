package com.project.farmingapp.view.user

import android.app.Activity
import android.app.AlertDialog
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
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.project.farmingapp.R
import com.project.farmingapp.adapter.FarmListUserProfileAdapter
import com.project.farmingapp.adapter.PostListUserProfileAdapter
import com.project.farmingapp.databinding.FragmentUserBinding
import com.project.farmingapp.utilities.CellClickListener
import com.project.farmingapp.utilities.CellClickListener2
import com.project.farmingapp.view.auth.UpdateUserData
import com.project.farmingapp.view.farms.AddFarmsFragment
import com.project.farmingapp.view.farms.UpdateFarmsFragment
import com.project.farmingapp.viewmodel.UserDataViewModel
import com.project.farmingapp.viewmodel.UserProfileFarmViewModel
import com.project.farmingapp.viewmodel.UserProfilePostsViewModel
import java.io.IOException
import java.lang.Exception
import java.util.*

class UserFragment : Fragment(), CellClickListener, CellClickListener2 {

    private lateinit var binding: FragmentUserBinding

    // users
    private lateinit var userDataViewModel: UserDataViewModel
    //user posts
    private lateinit var viewModel: UserProfilePostsViewModel
    // user farms
    private lateinit var viewModelFarms: UserProfileFarmViewModel

    lateinit var addFarmsFragment: AddFarmsFragment
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var postID: UUID? = null
    private var storageReference: StorageReference? = null
    private var bitmap: Bitmap? = null
    private var uploadProfOrBack: Int? = null

    val firebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
  
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel = ViewModelProviders.of(requireActivity())
            .get<UserProfilePostsViewModel>(UserProfilePostsViewModel::class.java)

        viewModelFarms = ViewModelProviders.of(requireActivity())
            .get<UserProfileFarmViewModel>(UserProfileFarmViewModel::class.java)


        userDataViewModel = ViewModelProviders.of(requireActivity())
            .get<UserDataViewModel>(UserDataViewModel::class.java)
      
        storageReference = FirebaseStorage.getInstance().reference

        viewModel.getAllPosts(firebaseAuth.currentUser!!.uid)

        viewModelFarms.getAllFarms(firebaseAuth.currentUser!!.uid)


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        viewModel.liveData1.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.getAllPostsOfUser(it)
            }
        })
        viewModelFarms.liveData1.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModelFarms.getAllFarmsOfUser(it)
            }
        })


        viewModel.liveData2.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d("Live Data In Frag", it.toString())
            }
        })

        viewModelFarms.liveData2.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d("Live Data In Frag", it.toString())
            }
        })



        viewModel.userProfilePostsLiveData2.observe(viewLifecycleOwner, Observer {
            Log.d("Some Part 2", it.toString())
        })

        viewModelFarms.userProfileFarmLiveData2.observe(viewLifecycleOwner, Observer {
            Log.d("Some Part 2", it.toString())
        })

        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Profile"

        binding.cityEditUserProfile.visibility = View.GONE
        binding.cityTitleUserProfileFrag.visibility = View.GONE
        binding.aboutTitleUserProfileFrag.visibility = View.GONE
        binding.aboutValueEditUserProfileFrag.visibility = View.GONE
        binding.saveBtnAboutUserProfileFrag.visibility = View.GONE

        viewModel.userProfilePostsLiveData.observe(viewLifecycleOwner, Observer {

            Log.d("Some Part", it.toString())
        })

        viewModelFarms.userProfileFarmLiveData.observe(viewLifecycleOwner, Observer {

            Log.d("Some Part", it.toString())
        })


        userDataViewModel.userliveData.observe(viewLifecycleOwner, Observer {
            Log.d("User Fragment", it.data.toString())
        })

//        aboutValueUserProfileFrag.setOnClickListener {
//            aboutValueEditUserProfileFrag.visibility = View.VISIBLE
//            addAboutTextUserFrag.visibility = View.GONE
//            aboutValueEditUserProfileFrag.setText(aboutValueUserProfileFrag.text.toString())
//            aboutValueUserProfileFrag.visibility = View.GONE
//            saveBtnAboutUserProfileFrag.visibility = View.VISIBLE
//        }

//        addAboutTextUserFrag.setOnClickListener {
//            aboutValueEditUserProfileFrag.visibility = View.VISIBLE
//            saveBtnAboutUserProfileFrag.visibility = View.VISIBLE
//        }

//        saveBtnAboutUserProfileFrag.setOnClickListener {
//            aboutValueUserProfileFrag.visibility = View.VISIBLE
//            aboutValueUserProfileFrag.text = aboutValueEditUserProfileFrag.text
//            saveBtnAboutUserProfileFrag.visibility = View.GONE
//            userDataViewModel.updateUserField(activity!!.applicationContext, firebaseAuth.currentUser!!.uid.toString() as String, aboutValueEditUserProfileFrag.text.toString() as String, null)
//            aboutValueEditUserProfileFrag.visibility = View.GONE
//        }

        binding.uploadProgressBarProfile.visibility = View.GONE
        binding.uploadBackProgressProfile.visibility = View.GONE

        binding.uploadUserBackgroundImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/* video/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
            uploadProfOrBack = 1

            Toast.makeText(requireActivity().applicationContext, "Uploading Background Image", Toast.LENGTH_SHORT).show()
        }

        addFarmsFragment = AddFarmsFragment()

        binding.addFarms.setOnClickListener {

            val transaction = requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, addFarmsFragment, "addFarms")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("addFarms")
                .commit()

        }

        binding.uploadProfilePictureImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/* video/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
            uploadProfOrBack = 0
            Toast.makeText(requireActivity().applicationContext, "Uploading your Image", Toast.LENGTH_SHORT).show()
        }


        userDataViewModel.userliveData.observe(viewLifecycleOwner, Observer {
            Log.d("User Data in VM Frag", it.get("name").toString())
            Log.d("Data in User", it.toString())
            binding.userNameUserProfileFrag.text = it!!.getString("name")
            val city = it?.getString("city")
            if(city == null){
                binding.userCityUserProfileFrag.text = ""
            } else{
                binding.userCityUserProfileFrag.text = "" + it.getString("city")
            }

            binding.aboutValueUserProfileFrag.text = "" + it.getString("occupation")

            if(it?.get("imageid") == null || it?.getString("imageurl").isNullOrBlank()){
                binding.uploadProfilePictureImage.visibility = View.VISIBLE
            } else{
                binding.uploadProfilePictureImage.visibility = View.GONE
                Glide.with(requireActivity().applicationContext).load(it?.get("imageurl"))
                    .into(binding.userImageUserFrag)
            }


            if(it?.get("backImage") == null || it?.getString("backImage").isNullOrBlank()){
                binding.uploadUserBackgroundImage.visibility = View.VISIBLE
            } else{
                binding.uploadUserBackgroundImage.visibility = View.GONE
                Glide.with(requireActivity().applicationContext).load(it?.getString("backImage"))
                    .into(binding.userBackgroundImage)
            }

            try {
                val posts = "Posts: " + (it.get("posts") as List<String>).size.toString()
                binding.userPostsCountUserProfileFrag.text = posts
                binding.userAboutUserProfileFrag.text = firebaseAuth.currentUser!!.uid
                val about = it.getString("occupation")

                if (about == null || about == "") {
                    binding.aboutValueUserProfileFrag.visibility = View.GONE
                    binding.aboutValueEditUserProfileFrag.visibility = View.GONE
                    binding.aboutTitleUserProfileFrag.visibility = View.GONE
                    binding.saveBtnAboutUserProfileFrag.visibility = View.GONE
                } else {
                    binding.aboutValueUserProfileFrag.visibility = View.VISIBLE
                    binding.aboutValueEditUserProfileFrag.visibility = View.GONE
                    binding.aboutTitleUserProfileFrag.visibility = View.GONE
                    binding.saveBtnAboutUserProfileFrag.visibility = View.GONE
                    binding.aboutValueUserProfileFrag.text = about
                }
            }catch (e: Exception){

                Log.e("User Data E: ", e.message!!)

            }

        })

        binding.imageEdit.setOnClickListener {

            val intent = Intent(requireContext(), UpdateUserData::class.java)
            startActivity(intent)

        /*
            binding.uploadProfilePictureImage.visibility = View.VISIBLE
            binding.uploadUserBackgroundImage.visibility = View.VISIBLE
            imageChecked.visibility = View.VISIBLE
            imageEdit.visibility = View.GONE
            cityEditUserProfile.setText(userCityUserProfileFrag!!.text.toString().removePrefix("City: "))
            cityEditUserProfile.visibility = View.VISIBLE
            cityTitleUserProfileFrag.visibility = View.VISIBLE
            aboutValueEditUserProfileFrag.visibility = View.VISIBLE
            aboutTitleUserProfileFrag.visibility = View.VISIBLE
            aboutValueEditUserProfileFrag.setText(aboutValueUserProfileFrag.text.toString())
            aboutValueUserProfileFrag.visibility = View.GONE
*/
        }

/*        imageChecked.setOnClickListener {
            binding.uploadProfilePictureImage.visibility = View.GONE
            binding.uploadUserBackgroundImage.visibility = View.GONE
            imageEdit.visibility = View.VISIBLE
            cityEditUserProfile.visibility = View.GONE
            cityTitleUserProfileFrag.visibility = View.GONE
            imageChecked.visibility = View.GONE
            userDataViewModel.updateUserField(requireActivity().applicationContext, firebaseAuth.currentUser!!.uid.toString() as String,
                aboutValueEditUserProfileFrag.text.toString(), cityEditUserProfile.text.toString())
            userDataViewModel.getUserData(firebaseAuth.currentUser!!.uid.toString())
            aboutValueEditUserProfileFrag.visibility = View.GONE
            aboutTitleUserProfileFrag.visibility = View.GONE
            aboutValueUserProfileFrag.visibility = View.VISIBLE
        }*/


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }

            filePath = data.data
            try {

                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, filePath)

                if(bitmap!=null){
                    Log.d("UserFragment", bitmap.toString())

                    if(uploadProfOrBack == 0){
                        binding.uploadProgressBarProfile.visibility = View.VISIBLE
                        binding.uploadProfilePictureImage.visibility = View.GONE
                    } else if(uploadProfOrBack == 1){
                        binding.uploadBackProgressProfile.visibility = View.VISIBLE
                        binding.uploadUserBackgroundImage.visibility = View.GONE
                    }

                    uploadImage2().setImageBitmap(bitmap)
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }


    private fun uploadImage2() {
        if (filePath != null) {
            postID = UUID.randomUUID()
            val ref = storageReference?.child("users/" + postID.toString())
            val uploadTask = ref?.putFile(filePath!!)
            Log.d("UserFragment", filePath.toString())
            val urlTask =
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(requireActivity().applicationContext, "Error in Uploading", Toast.LENGTH_SHORT).show()
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        Toast.makeText(requireActivity().applicationContext, "Uploading...", Toast.LENGTH_SHORT).show()
                        uploadUserPhotos(downloadUri.toString(), postID!!)

                    } else {
                        // Handle failures
                        Toast.makeText(requireActivity().applicationContext, "Error", Toast.LENGTH_SHORT).show()
                        binding.uploadProgressBarProfile.visibility = View.GONE
                        binding.uploadBackProgressProfile.visibility = View.GONE
                        binding.uploadUserBackgroundImage.visibility = View.VISIBLE
                        binding.uploadProfilePictureImage.visibility = View.VISIBLE
                    }
                }?.addOnFailureListener {
                    Toast.makeText(requireActivity().applicationContext, "Error2", Toast.LENGTH_SHORT).show()
                    binding.uploadProgressBarProfile.visibility = View.GONE
                    binding.uploadBackProgressProfile.visibility = View.GONE
                    binding.uploadUserBackgroundImage.visibility = View.VISIBLE
                    binding.uploadProfilePictureImage.visibility = View.VISIBLE
                }
        } else {

        }
    }




    fun uploadUserPhotos(uri: String?, postID: UUID?){

        if(uploadProfOrBack == 0){
            firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid!!)
                .update(mapOf(
                    "profileImage" to uri
                ))
                .addOnSuccessListener {
                    Toast.makeText(requireActivity().applicationContext, "Profile Updated", Toast.LENGTH_SHORT).show()
                    binding.uploadProgressBarProfile.visibility = View.GONE
                    binding.imageEdit.visibility = View.VISIBLE
                    //imageChecked.visibility = View.GONE
                    binding.userImageUserFrag.setImageBitmap(bitmap)
                    userDataViewModel.getUserData(firebaseAuth!!.currentUser!!.uid.toString())
                }
                .addOnFailureListener {
                    binding.uploadProgressBarProfile.visibility = View.GONE
                    binding.userImageUserFrag.visibility = View.VISIBLE
                    Toast.makeText(requireActivity().applicationContext, "Failed to Update Profile", Toast.LENGTH_SHORT).show()

                }
        }
        else if(uploadProfOrBack == 1){
            firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid!!)
                .update(mapOf(
                    "backImage" to uri
                ))
                .addOnSuccessListener {
                    Toast.makeText(requireActivity().applicationContext, "Profile Updated 2", Toast.LENGTH_SHORT).show()
                    binding.uploadBackProgressProfile.visibility = View.GONE
                    binding.userBackgroundImage.setImageBitmap(bitmap)
                    binding.imageEdit.visibility = View.VISIBLE
                    //imageChecked.visibility = View.GONE
                    userDataViewModel.getUserData(firebaseAuth!!.currentUser!!.uid.toString())
                }
                .addOnFailureListener {
                    binding.uploadBackProgressProfile.visibility = View.GONE
                    binding.userBackgroundImage.setImageBitmap(bitmap)
                    Toast.makeText(requireActivity().applicationContext, "Failed to Update Profile", Toast.LENGTH_SHORT).show()
                }
        }


    }

    override fun onCellClickListener(name: String) {
        val dialog = AlertDialog.Builder(activity)

        dialog.setTitle("Your Post")
            .setMessage("Do you want to edit your post?")
            /*.setPositiveButton("View") { dialogInterface, i ->

            }*/
            .setNegativeButton("Delete") { dialogInterface, i ->
                userDataViewModel.deleteUserPost(firebaseAuth.currentUser!!.uid!!, name)
                userDataViewModel.getUserData(firebaseAuth.currentUser!!.uid.toString())
                viewModel.getAllPosts(firebaseAuth.currentUser!!.uid)
//                viewModelFarms.getAllFarms(firebaseAuth.currentUser!!.uid)
            }
            .setNeutralButton("Cancel"){
                dialogInterface, i ->

            }
            .show()

        Toast.makeText(requireActivity().applicationContext, "You Clicked" + name, Toast.LENGTH_SHORT).show()
    }

    override fun onCellClickListener2(farmId: String) {
        val dialog = AlertDialog.Builder(activity)

        dialog.setTitle("Your Farm")
            .setMessage("Do you want to edit your farm?")
            .setPositiveButton("Edit") { dialogInterface, i ->

                val bundle = Bundle()
                bundle.putString("farmID", farmId)
                val updateFarmsFragment = UpdateFarmsFragment()
                updateFarmsFragment.arguments = bundle


                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, updateFarmsFragment, "updatefarms")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("updatefarms")
                    .commit()


            }
            .setNegativeButton("Delete") { dialogInterface, i ->
                userDataViewModel.deleteUserFarm(firebaseAuth.currentUser!!.uid!!, farmId)
                userDataViewModel.getUserData(firebaseAuth.currentUser!!.uid.toString())
                viewModelFarms.getAllFarms(firebaseAuth.currentUser!!.uid)
            }
            .setNeutralButton("Cancel"){
                dialogInterface, i ->

            }
            .show()

        Toast.makeText(requireActivity().applicationContext, "You Clicked$farmId", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.liveData3.observe(viewLifecycleOwner, Observer {
            Log.d("All Posts", it.toString())
            val adapter = PostListUserProfileAdapter(requireActivity().applicationContext, it, this)
            binding.userProfilePostsRecycler.adapter = adapter

            binding.userProfilePostsRecycler.layoutManager =
                LinearLayoutManager(requireActivity().applicationContext)
            adapter.notifyDataSetChanged()

        })

        viewModelFarms.liveData3.observe(viewLifecycleOwner, Observer {
            Log.d("All Posts", it.toString())
            val adapter = FarmListUserProfileAdapter(requireActivity().applicationContext, requireActivity(), it, this)
            binding.userProfileFarmsRecycler.adapter = adapter

            binding.userProfileFarmsRecycler.layoutManager =
                LinearLayoutManager(requireActivity().applicationContext, LinearLayoutManager.HORIZONTAL, false)
            adapter.notifyDataSetChanged()

        })

    }
}
private fun Any.setImageBitmap(bitmap: Bitmap?) {

}