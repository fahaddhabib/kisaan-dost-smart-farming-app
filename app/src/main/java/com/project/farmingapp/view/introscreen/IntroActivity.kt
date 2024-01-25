package com.project.farmingapp.view.introscreen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.project.farmingapp.R
import com.project.farmingapp.adapter.IntroAdapter
import com.project.farmingapp.databinding.ActivityIntroBinding
import com.project.farmingapp.model.data.IntroData
import com.project.farmingapp.view.auth.PhoneAuthActivity

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private val introSliderAdapter = IntroAdapter(
        listOf(
            IntroData(
                "Welcome to the\n\bKISAAN DOST\b",
                "Best Guide and Helper for any Farmer. Provides various features at one place!",
                R.drawable.img_wizard_1
            ),
            IntroData(
                "Read Articles",
                "Read Online articles related to Farming Concepts, Technologies and other useful knowledge.",
                R.drawable.img_wizard_2
            ),
            IntroData(
                "E-Commerce",
                "Buy / Sell Agriculture related products",
                R.drawable.img_wizard_3
            ),
            IntroData(
                "Weather Forecast",
                "Check Weather Forecast Conditions. 24x7 Data",
                R.drawable.img_wizard_4
            ),
            IntroData(
                "Expert Opinion",
                "Get Expert Opinion From Our Expert Farmers",
                R.drawable.img_wizard_5
            ),
            IntroData(
                "Let's Grow Together",
                "- KISAAN DOST",
                R.drawable.appiconpng
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sliderViewPager.adapter = introSliderAdapter
        setupIndicators()
        setCurrentIndicator(0)
        binding.sliderViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        if (binding.sliderViewPager.currentItem + 1 == introSliderAdapter.itemCount) {
            binding.nextBtn.text = "Get Started"
        } else {
            binding.nextBtn.text = "Next"
        }

        binding.nextBtn.setOnClickListener {
            if (binding.sliderViewPager.currentItem + 1 < introSliderAdapter.itemCount) {
                binding.sliderViewPager.currentItem += 1
                binding.nextBtn.text = "Next"
                if (binding.sliderViewPager.currentItem + 1 == introSliderAdapter.itemCount) {
                    binding.nextBtn.text = "Get Started"
                }
            } else {
                Intent(this, PhoneAuthActivity::class.java).also {
                    startActivity(it)
                }
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("firstTime", false)
                editor.apply()
                finish()
            }
        }

        binding.skipIntro.setOnClickListener {
            Intent(this, PhoneAuthActivity::class.java).also {
                startActivity(it)
            }
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("firstTime", false)
            editor.apply()
            finish()
        }
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            binding.sliderballsContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.sliderballsContainer.childCount
        for (i in 0 until childCount) {
            val imageView = binding.sliderballsContainer.get(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }

        if (index == introSliderAdapter.itemCount - 1) {
            binding.nextBtn.text = "Get Started"
        } else {
            binding.nextBtn.text = "Next"
        }
    }
}