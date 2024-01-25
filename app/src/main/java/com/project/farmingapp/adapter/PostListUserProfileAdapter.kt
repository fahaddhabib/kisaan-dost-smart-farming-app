package com.project.farmingapp.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.project.farmingapp.databinding.UserProfilePostsSingleBinding
import com.project.farmingapp.utilities.CellClickListener

class PostListUserProfileAdapter(
    private val context: Context,
    private var listData: ArrayList<DocumentSnapshot>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<PostListUserProfileAdapter.PostListUserProfileViewHolder>() {

    inner class PostListUserProfileViewHolder(private val binding: UserProfilePostsSingleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(postData: DocumentSnapshot) {
            binding.userPostTitleUserProfileFrag.text = postData.get("title").toString()
            binding.userPostUploadTimeUserProfileFrag.text =
                DateUtils.getRelativeTimeSpanString(postData.get("timeStamp") as Long)
            binding.userPostDescriptionProfileFrag.text = postData.get("description").toString()

            binding.userPostProfileCard.setOnClickListener {
                cellClickListener.onCellClickListener(postData.id)
            }

            if (!postData.get("imageUrl").toString().isNullOrEmpty()) {
                Glide.with(context)
                    .load(postData.getString("imageUrl"))
                    .into(binding.userPostImageUserProfileFrag)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostListUserProfileViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = UserProfilePostsSingleBinding.inflate(inflater, parent, false)
        return PostListUserProfileViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: PostListUserProfileViewHolder, position: Int) {
        val currentData = listData[position]
        holder.bind(currentData)
    }
}