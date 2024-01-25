package com.project.farmingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.project.farmingapp.databinding.UserProfileFarmSingleBinding
import com.project.farmingapp.utilities.CellClickListener2

class FarmListUserProfileAdapter(
    private val context: Context,
    private val activity: FragmentActivity,
    private var listData: ArrayList<DocumentSnapshot>,
    private val cellClickListener2: CellClickListener2
) : RecyclerView.Adapter<FarmListUserProfileAdapter.FarmListUserProfileViewHolder>() {

    inner class FarmListUserProfileViewHolder(private val binding: UserProfileFarmSingleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(farmData: DocumentSnapshot) {
            binding.userFarmTitleUserProfileFrag.text = farmData.get("f_title").toString()
            binding.userFarmAreaUserProfileFrag.text = farmData.get("f_area").toString()

            binding.userFarmProfileCard.setOnClickListener {
                cellClickListener2.onCellClickListener2(farmData.id)
            }

            if (!farmData.get("imageUrl").toString().isNullOrEmpty()) {
                Glide.with(context)
                    .load(farmData.getString("imageUrl"))
                    .into(binding.userFarmImageUserProfileFrag)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmListUserProfileViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = UserProfileFarmSingleBinding.inflate(inflater, parent, false)
        return FarmListUserProfileViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: FarmListUserProfileViewHolder, position: Int) {
        val currentData = listData[position]
        holder.bind(currentData)
    }
}