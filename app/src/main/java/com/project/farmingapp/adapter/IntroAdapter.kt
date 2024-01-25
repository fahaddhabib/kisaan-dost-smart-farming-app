package com.project.farmingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.farmingapp.databinding.SingleSliderScreenBinding
import com.project.farmingapp.model.data.IntroData

class IntroAdapter(private val introSlides: List<IntroData>) : RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {

    inner class IntroViewHolder(private val binding: SingleSliderScreenBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(introSlider: IntroData) {
            binding.sliderTitle.text = introSlider.title
            binding.sliderDescription.text = introSlider.description
            binding.imageSlider.setImageResource(introSlider.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SingleSliderScreenBinding.inflate(inflater, parent, false)
        return IntroViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return introSlides.size
    }

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        holder.bind(introSlides[position])
    }
}
