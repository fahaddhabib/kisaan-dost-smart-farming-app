package com.project.farmingapp.view.ecom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.project.farmingapp.databinding.FragmentEcommExtendedBuyBinding

class EcommExtendedBuyFragment : Fragment() {

    private lateinit var binding: FragmentEcommExtendedBuyBinding

    lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var priceString: String
    private lateinit var unitString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val path = arguments?.getString("documentPath")
        val documentReference = FirebaseFirestore.getInstance().document(path.toString())
        documentReference.get().addOnSuccessListener { currentPost ->

            binding.userNamePostSM.text = currentPost.get("name").toString()
            binding.userPostUploadTime.text = currentPost.get("timeStamp").toString()
            binding.userPostTitleValue.text = currentPost.get("title").toString()
            binding.userPostDescValue.text = currentPost.get("description").toString()
            // Getting 2 strings to concatenate price/unit
            priceString = currentPost.get("price").toString()
            unitString = currentPost.get("unit").toString()
            binding.valPostPrice.text = "$priceString/$unitString"

            binding.valPostQuan.text = currentPost.get("quantity").toString()
            binding.valPostQual.text = currentPost.get("quality").toString()
            //postVideoSM.loadUrl(currentPost.get("imageUrl").toString())
            Glide.with(requireContext()).load(currentPost.get("imageUrl")).into(binding.postImageSM)


            firebaseFirestore = FirebaseFirestore.getInstance()
            firebaseFirestore.collection("users").document("${currentPost.get("userID")}").get()
                .addOnSuccessListener {

                    // to send user for contact
                    val mobileNumber = it.get("mobNo").toString()
                    binding.txtContactButton.setOnClickListener {

                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobileNumber"))
                        startActivity(dialIntent)

                    }
                    // to get profile image
                    val profileImage = it.get("imageurl").toString()
                    if (!profileImage.isNullOrEmpty()){
                        Glide.with(requireContext()).load(it.get("imageurl").toString()).into(binding.userProfileImagePost)
                    }
                }

        }.addOnFailureListener { exception ->
            // Handle the failure here
            // ...
        }

        binding = FragmentEcommExtendedBuyBinding.inflate(inflater, container, false)
        return binding.root
    }


}