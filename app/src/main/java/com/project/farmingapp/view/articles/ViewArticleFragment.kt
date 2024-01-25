package com.project.farmingapp.view.articles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.project.farmingapp.R
import com.project.farmingapp.databinding.FragmentViewArticleBinding

class ViewArticleFragment : Fragment() {

    var articlepath : String = ""
    var imageUrl : String = ""

    private lateinit var binding: FragmentViewArticleBinding
    private lateinit var articleRef: DocumentReference
    private var currentLikes: Long = 0
    private var isLiked: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentViewArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articlepath = arguments?.getString("articlePath").toString()

        val documentReference = FirebaseFirestore.getInstance().document(articlepath)

        documentReference.get().addOnSuccessListener { currentArticle ->
            binding.articletitle.setText(currentArticle.get("title").toString())
            imageUrl = currentArticle.get("imageUrl").toString()
            Glide.with(this).load(imageUrl).into(binding.articleimage)
            binding.articleDescrip.setText(currentArticle.get("description").toString())

            currentLikes = currentArticle.getLong("likes") ?: 0
            isLiked = currentArticle.getBoolean("likedByUser") ?: false
            updateLikeButtonState()

            // Update like count and display toast when like button is clicked
            binding.btnArticleLike.setOnClickListener {
                val newLikes = if (isLiked) currentLikes - 1 else currentLikes + 1
                isLiked = !isLiked

                documentReference.update("likes", newLikes, "likedByUser", isLiked)
                    .addOnSuccessListener {
                        currentLikes = newLikes
                        updateLikeButtonState()

                        val toastMessage = if (isLiked) "Liked!" else "Unliked!"
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        // Handle the failure case
                        Toast.makeText(context, "Failed to perform action", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }

    private fun updateLikeButtonState() {
        binding.btnArticleLike.setImageResource(if (isLiked) R.drawable.ic_liked_by_user else R.drawable.ic_baseline_favorite_border_24)
        binding.txtArticleLikes.text = currentLikes.toString()
    }
}