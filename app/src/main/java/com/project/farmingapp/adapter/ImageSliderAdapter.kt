package com.project.farmingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.project.farmingapp.databinding.ItemImageSliderBinding
import com.project.farmingapp.model.data.ImageData
import com.smarteist.autoimageslider.SliderViewAdapter

class ImageSliderAdapter(private val imageDataList: List<ImageData>) :
    SliderViewAdapter<ImageSliderAdapter.ImageSliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): ImageSliderViewHolder {
        val binding = ItemImageSliderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageSliderViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ImageSliderViewHolder, position: Int) {
        val imageData = imageDataList[position]
        Glide.with(viewHolder.itemView)
            .load(imageData.url)
            .into(viewHolder.imageView)
    }

    override fun getCount(): Int {
        return imageDataList.size
    }

    inner class ImageSliderViewHolder(private val binding: ItemImageSliderBinding) :
        SliderViewAdapter.ViewHolder(binding.root) {
        val imageView: ImageView = binding.sliderImageView
    }
}
