package com.project.farmingapp.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.project.farmingapp.R
import com.project.farmingapp.view.ecom.EcommExtendedBuyFragment

class EcommPostListAdapter(val context: Context, val activity: FragmentActivity, val postListData : List<DocumentSnapshot>): RecyclerView.Adapter<EcommPostListAdapter.SMPostListViewModel>() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var priceString: String
    private lateinit var unitString: String

    lateinit var ecommextendedbuyfragment: EcommExtendedBuyFragment

    class SMPostListViewModel(itemView: View): RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SMPostListViewModel {
        val view = LayoutInflater.from(context).inflate(R.layout.post_with_image_ecomm, parent, false)

        firebaseAuth = FirebaseAuth.getInstance()

        return EcommPostListAdapter.SMPostListViewModel(view)
    }

    override fun getItemCount(): Int {
        return postListData.size
    }

    override fun onBindViewHolder(holder: SMPostListViewModel, position: Int) {
        val userProfileImagePost = holder.itemView.findViewById<ImageView>(R.id.userProfileImagePost)
        val userNamePostSM = holder.itemView.findViewById<TextView>(R.id.userNamePostSM)
        val userPostUploadTime = holder.itemView.findViewById<TextView>(R.id.userPostUploadTime)
        val userPostTitleValue = holder.itemView.findViewById<TextView>(R.id.userPostTitleValue)
        val userPostDescValue = holder.itemView.findViewById<TextView>(R.id.userPostDescValue)
        val postImageSM = holder.itemView.findViewById<ImageView>(R.id.postImageSM)
        val btnPostLikes = holder.itemView.findViewById<ImageButton>(R.id.btn_post_likes)
        val txtPostLikes = holder.itemView.findViewById<TextView>(R.id.txt_post_likes)
        val btnPostShare = holder.itemView.findViewById<ImageButton>(R.id.btn_post_share)
        val txtContactButton = holder.itemView.findViewById<ImageButton>(R.id.txt_contact_button)
        val txtPostPrice = holder.itemView.findViewById<TextView>(R.id.txt_post_price)
        val post_container = holder.itemView.findViewById<ConstraintLayout>(R.id.post_container)

        val currentPost = postListData[position]
        val path = currentPost.reference.path // get the path of the document snapshot as a string
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(currentPost.id)

        val currentLikes = currentPost.getLong("likes") ?: 0
        txtPostLikes.text = currentLikes.toString()

// Assuming you have access to the current user's ID

        val currentUserId: String = firebaseAuth.currentUser?.uid ?: ""

        val likedByUsersField = currentPost.get("likedByUsers")
        val likedByUsers = if (likedByUsersField is Map<*, *>) likedByUsersField as? Map<String, Boolean> else null
        val isLikedByCurrentUser = likedByUsers?.get(currentUserId) ?: false

        val likeIconResId = if (isLikedByCurrentUser) R.drawable.ic_liked_by_user else R.drawable.ic_baseline_favorite_border_24
        btnPostLikes.setImageResource(likeIconResId)

// Update like count and display toast when like button is clicked
        btnPostLikes.setOnClickListener {
            val newLikes = if (isLikedByCurrentUser) currentLikes - 1 else currentLikes + 1
            val isLikedUpdated = !isLikedByCurrentUser

            val updatedLikedByUsers: MutableMap<String, Boolean> = likedByUsers?.toMutableMap() ?: HashMap()
            updatedLikedByUsers[currentUserId] = isLikedUpdated

            postRef.update("likes", newLikes, "likedByUsers", updatedLikedByUsers)
                .addOnSuccessListener {
                    txtPostLikes.text = newLikes.toString()

                    // Update the like button icon
                    val newLikeIconResId = if (isLikedUpdated) R.drawable.ic_liked_by_user else R.drawable.ic_baseline_favorite_border_24
                    btnPostLikes.setImageResource(newLikeIconResId)

                    val toastMessage = if (isLikedUpdated) "Liked!" else "Unliked!"
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()

                    // Update the currentPost object with the new likes count and likedByUsers map
                    currentPost.reference.update("likes", newLikes)
                    currentPost.reference.update("likedByUsers", updatedLikedByUsers)
                }
                .addOnFailureListener { exception ->
                    // Handle the failure case
                    Toast.makeText(context, "Failed to perform action", Toast.LENGTH_SHORT).show()
                }
        }
        userNamePostSM.text = currentPost.get("name").toString()
        userPostTitleValue.text = currentPost.get("title").toString()
        userPostDescValue.text = currentPost.get("description").toString()
        priceString = currentPost.get("price").toString()
        unitString = currentPost.get("unit").toString()
        txtPostPrice.text = "$priceString $/$unitString"
        userPostUploadTime.text = DateUtils.getRelativeTimeSpanString(currentPost.get("timeStamp") as Long)
        
        val bundle = Bundle()
        bundle.putString("documentPath", path)
        val ecommextendedbuyfragment = EcommExtendedBuyFragment()
        ecommextendedbuyfragment.arguments = bundle

        holder.itemView.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {

                activity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, ecommextendedbuyfragment, "ecomBuy")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("ecomBuy")
                    .commit()
            }
        })

        val imageUrl = currentPost.get("imageUrl")
        Log.d("Post without Image1", imageUrl.toString())

        val uploadType = currentPost.get("uploadType").toString()
        if (uploadType == "video"){

        } else if (uploadType == "image"){
            Glide.with(context).load(currentPost.get("imageUrl")).into(postImageSM)
            //holder.itemView.postVideoSM.visibility = View.GONE

            postImageSM.visibility = View.VISIBLE
            Log.d("Upload Type 2 ", uploadType)
        }else if (uploadType.isEmpty() ){
            Log.d("Post without Image2", imageUrl.toString())
            postImageSM.visibility = View.GONE
            //holder.itemView.postVideoSM.visibility = View.GONE
            Log.d("Upload Type 3 ", uploadType)
        }

        userProfileImagePost.animation = AnimationUtils.loadAnimation(context, R.anim.fade_transition)
        post_container.animation = AnimationUtils.loadAnimation(context, R.anim.fade_transition)


        post_container.animation = AnimationUtils.loadAnimation(context, R.anim.fade_transition)
//        Glide.with(context).load(firebaseAuth.currentUser!!.photoUrl.toString()).into(holder.itemView.userProfileImagePost)
        userPostDescValue.setOnClickListener {
            userPostDescValue.maxLines = Int.MAX_VALUE
        }



        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection("users").document("${currentPost.get("userID")}").get()
            .addOnSuccessListener {

                // to send user for contact
                val mobileNumber = it.get("phone").toString()
                txtContactButton.setOnClickListener {

                    Toast.makeText(context, mobileNumber, Toast.LENGTH_SHORT).show()
/*                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobileNumber"))
                    startActivity(dialIntent)*/
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.data = Uri.parse("tel:$mobileNumber")
                    context.startActivity(intent)
                }

                val profileImage = it.get("imageurl").toString()
                if (!profileImage.isNullOrEmpty()){
                    Glide.with(context).load(it.get("imageurl").toString()).into(userProfileImagePost)
                }
            }
    }
    private fun DocumentSnapshot.getLongOrNull(field: String): Long? {
        return if (contains(field)) getLong(field) else null
    }
}