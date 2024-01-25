package com.project.farmingapp.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.project.farmingapp.R
import com.project.farmingapp.databinding.UserProfileArticleSingleBinding
import com.project.farmingapp.view.articles.ViewArticleFragment

class ArticleListUserProfileAdapter(
    private val context: Context,
    private val activity: FragmentActivity,
    private var listData: List<DocumentSnapshot>
) : RecyclerView.Adapter<ArticleListUserProfileAdapter.ArticleListUserProfileViewHolder>() {

    inner class ArticleListUserProfileViewHolder(private val binding: UserProfileArticleSingleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(articleData: DocumentSnapshot) {
            binding.userArticleTitleUserProfileFrag.text = articleData.get("title").toString()

            val articlePath = articleData.reference.path // get the path of the document snapshot as a string
            val bundle = Bundle().apply {
                putString("articlePath", articlePath)
            }
            val viewArticleFragment = ViewArticleFragment().apply {
                arguments = bundle
            }

            binding.root.setOnClickListener {
                activity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, viewArticleFragment, "viewarticle")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("viewarticle")
                    .commit()
            }

            if (!articleData.get("imageUrl").toString().isNullOrEmpty()) {
                Glide.with(context)
                    .load(articleData.getString("imageUrl"))
                    .into(binding.userArticleImageUserProfileFrag)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleListUserProfileViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = UserProfileArticleSingleBinding.inflate(inflater, parent, false)
        return ArticleListUserProfileViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: ArticleListUserProfileViewHolder, position: Int) {
        val currentData = listData[position]
        holder.bind(currentData)
    }
}
