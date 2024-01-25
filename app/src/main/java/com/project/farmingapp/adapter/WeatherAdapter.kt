package com.project.farmingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.project.farmingapp.R
import com.project.farmingapp.databinding.WeatherDailyListItemBinding
import com.project.farmingapp.model.WeatherModel
import java.util.*

class WeatherAdapter(private val items: List<WeatherModel>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: WeatherDailyListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: WeatherModel) {
            val generator = ColorGenerator.MATERIAL

            // generate random color
            val color = generator.randomColor
            binding.cvListWeather.setCardBackgroundColor(color)

            binding.tvNameDay.text = data.timeNow
            binding.tvTemp.text = String.format(Locale.getDefault(), "%.0f°C", data.currentTemp)
            binding.tvTempMin.text = String.format(Locale.getDefault(), "%.0f°C", data.tempMin)
            binding.tvTempMax.text = String.format(Locale.getDefault(), "%.0f°C", data.tempMax)

            val animationResId = when (data.descWeather) {
                "broken clouds" -> R.raw.broken_clouds
                "light rain" -> R.raw.light_rain
                "overcast clouds" -> R.raw.overcast_clouds
                "moderate rain" -> R.raw.moderate_rain
                "few clouds" -> R.raw.few_clouds
                "heavy intensity rain" -> R.raw.heavy_intentsity
                "clear sky" -> R.raw.clear_sky
                "scattered clouds" -> R.raw.scattered_clouds
                else -> R.raw.unknown
            }
            binding.iconTemp.setAnimation(animationResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = WeatherDailyListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}