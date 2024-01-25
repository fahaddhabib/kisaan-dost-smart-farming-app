package com.project.farmingapp.view.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.farmingapp.R
import com.project.farmingapp.adapter.ArticleListUserProfileAdapter
import com.project.farmingapp.adapter.ImageSliderAdapter
import com.project.farmingapp.adapter.WeatherAdapter
import com.project.farmingapp.databinding.FragmentDashboardBinding
import com.project.farmingapp.model.UserProfileArticleViewModel
import com.project.farmingapp.model.WeatherModel
import com.project.farmingapp.model.data.ImageData
import com.project.farmingapp.networking.ApiEndpoint
import com.project.farmingapp.view.chatapp.view.ui.HomeActivity
import com.smarteist.autoimageslider.SliderView
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class DashboardFragment : Fragment(), LocationListener {

    // admin articles
    private lateinit var viewModelArticles: UserProfileArticleViewModel

    private lateinit var binding: FragmentDashboardBinding

    val firebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    private val collectionRef = firebaseFirestore.collection("slider")
    private val imageDataList = ArrayList<ImageData>()

    //weather today
    private var lat: Double? = null
    private var lng: Double? = null
    private var hariIni: String? = null
    private var mProgressBar: AlertDialog? = null
    private var mainAdapter: WeatherAdapter? = null
    private val modelMain: MutableList<WeatherModel> = ArrayList()
    private val permissionArrays = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModelArticles = ViewModelProviders.of(requireActivity())
            .get(UserProfileArticleViewModel::class.java)

        val dateNow = Calendar.getInstance().time
        hariIni = DateFormat.format("EEE", dateNow) as String

        mProgressBar = AlertDialog.Builder(requireContext())
            .setTitle("Please wait")
            .setMessage("Currently displaying data...")
            .setCancelable(false)
            .create()

        mainAdapter = WeatherAdapter(modelMain)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        //slider
        val sliderView = view.findViewById<SliderView>(R.id.carousel)
        val adapter = ImageSliderAdapter(imageDataList)
        sliderView.setSliderAdapter(adapter)

        collectionRef.get().addOnSuccessListener { querySnapshot ->

            imageDataList.clear() // Clear the list before adding new images

            for (document in querySnapshot) {
                val imageUrl = document.getString("sliderurl")
                val imageData = ImageData(imageUrl.toString())
                imageDataList.add(imageData)
            }

            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            // Handle any errors that occur during the data retrieval
            // Log or display an error message
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvListWeather.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            adapter = mainAdapter
        }

        // Request Permission
        val myVersion = Build.VERSION.SDK_INT
        if (myVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission() && checkIfAlreadyhavePermission2()) {
            } else {
                requestPermissions(permissionArrays, 101)
            }
        }

        // Get Today's Date
        getToday()
        // Get Latitude and Longitude
        getLatlong()

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Kisaan Dost"

        getData()

        binding.btnChat.setOnClickListener {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            requireContext().startActivity(intent)
        }

        binding.refreshWeather.setOnClickListener {

            modelMain.clear() // Clear the list before adding new images

            // Get Today's Date
            getToday()
            // Get Latitude and Longitude
            getLatlong()

        }

    }

    override fun onStop() {
        super.onStop()

    }

    fun getData() {
        val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("articles").orderBy("timeStamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener {
                Log.d("Posts data", it.documents.toString())
                val adapter = ArticleListUserProfileAdapter(requireActivity().applicationContext, requireActivity(), it.documents)
                binding.articleRV.adapter = adapter
                binding.articleRV.layoutManager = LinearLayoutManager(requireActivity().applicationContext, LinearLayoutManager.HORIZONTAL, false)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {

            }
    }

    //weather today all functions
    private fun getToday() {
        val date = Calendar.getInstance().time
        val tanggal = DateFormat.format("d MMM yyyy", date) as String
        val formatDate = "$hariIni, $tanggal"
        binding.tvDate.text = formatDate
    }

    private fun getLatlong() {
        if (!checkGPSEnabled()) {
            return
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, true)
            val location = locationManager.getLastKnownLocation(provider.toString())
            if (location != null) {
                onLocationChanged(location)
            } else {
                locationManager.requestLocationUpdates(provider.toString(), 20000, 0f, this)
            }
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                115
            )
        }
    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun showAlert() {
        val dialog = android.app.AlertDialog.Builder(context)
        dialog.setTitle("Enable Location")
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to use this app!")
            .setPositiveButton("Location Settings") { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            .setNegativeButton("Cancel") { _, _ -> }
        dialog.show()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude

        // Get Weather Data
        getCurrentWeather()
        getListWeather()
    }


    private fun getCurrentWeather() {
        Log.d("weatherLink",""+ ApiEndpoint.BASEURL + ApiEndpoint.CurrentWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid
        )
        AndroidNetworking.get(
            ApiEndpoint.BASEURL + ApiEndpoint.CurrentWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid
        )
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject) {
                    try {
                        val jsonArrayOne = response.getJSONArray("weather")
                        val jsonObjectOne = jsonArrayOne.getJSONObject(0)
                        val jsonObjectTwo = response.getJSONObject("main")
                        val jsonObjectThree = response.getJSONObject("wind")
                        val strWeather = jsonObjectOne.getString("main")
                        val strDescWeather = jsonObjectOne.getString("description")
                        val strKecepatanAngin = jsonObjectThree.getString("speed")
                        val strKelembaban = jsonObjectTwo.getString("humidity")
                        val strNamaKota = response.getString("name")
                        val dblTemperatur = jsonObjectTwo.getDouble("temp")

                        when (strDescWeather) {
                            "broken clouds" -> {
                                binding.iconTemp.setAnimation(R.raw.broken_clouds)
                                binding.tvWeather.text = getString(R.string.scattered_clouds)
                            }
                            "light rain" -> {
                                binding.iconTemp.setAnimation(R.raw.light_rain)
                                binding.tvWeather.text = getString(R.string.drizzling)
                            }
                            "haze" -> {
                                binding.iconTemp.setAnimation(R.raw.broken_clouds)
                                binding.tvWeather.text = getString(R.string.foggy)
                            }
                            "overcast clouds" -> {
                                binding.iconTemp.setAnimation(R.raw.overcast_clouds)
                                binding.tvWeather.text = getString(R.string.cloudy_sky)
                            }
                            "moderate rain" -> {
                                binding.iconTemp.setAnimation(R.raw.moderate_rain)
                                binding.tvWeather.text = getString(R.string.cloudy_sky)
                            }
                            "few clouds" -> {
                                binding.iconTemp.setAnimation(R.raw.few_clouds)
                                binding.tvWeather.text = getString(R.string.cloudy)
                            }
                            "heavy intensity rain" -> {
                                binding.iconTemp.setAnimation(R.raw.heavy_intentsity)
                                binding.tvWeather.text = getString(R.string.heavy_rain)
                            }
                            "clear sky" -> {
                                binding.iconTemp.setAnimation(R.raw.clear_sky)
                                binding.tvWeather.text = getString(R.string.bright)
                            }
                            "scattered clouds" -> {
                                binding.iconTemp.setAnimation(R.raw.scattered_clouds)
                                binding.tvWeather.text = getString(R.string.scattered_clouds)
                            }
                            else -> {
                                binding.iconTemp.setAnimation(R.raw.unknown)
                                binding.tvWeather.text = strWeather
                            }
                        }

                        binding.tvNamaKota.text = strNamaKota
                        binding.tvTempeatur.text = String.format(Locale.getDefault(), "%.0f°C", dblTemperatur)
                        binding.tvKecepatanAngin.text = "Wind velocity $strKecepatanAngin km/h"
                        binding.tvKelembaban.text = "Humidity $strKelembaban %"
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(context, "Failed to display header data!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(
                        context,
                        "No internet network!" + anError.errorBody,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("weather error:", "" + anError.errorCode)
                }
            })
    }

    private fun getListWeather() {
        mProgressBar?.show()
        AndroidNetworking.get(
            ApiEndpoint.BASEURL + ApiEndpoint.ListWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid
        )
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        mProgressBar?.dismiss()
                        val jsonArray = response.getJSONArray("list")
                        for (i in 0..6) {
                            val dataApi = WeatherModel()
                            val objectList = jsonArray.getJSONObject(i)
                            val jsonObjectOne = objectList.getJSONObject("main")
                            val jsonArrayOne = objectList.getJSONArray("weather")
                            val jsonObjectTwo = jsonArrayOne.getJSONObject(0)
                            var timeNow = objectList.getString("dt_txt")
                            val formatDefault = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val formatTimeCustom = SimpleDateFormat("kk:mm")

                            try {
                                val timesFormat = formatDefault.parse(timeNow)
                                timeNow = formatTimeCustom.format(timesFormat!!)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                            dataApi.timeNow = timeNow
                            dataApi.currentTemp = jsonObjectOne.getDouble("temp")
                            dataApi.descWeather = jsonObjectTwo.getString("description")
                            dataApi.tempMin = jsonObjectOne.getDouble("temp_min")
                            dataApi.tempMax = jsonObjectOne.getDouble("temp_max")
                            modelMain.add(dataApi)
                        }
                        mainAdapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            "Failed to display header data!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(anError: ANError) {
                    mProgressBar?.dismiss()
                    Toast.makeText(context, "No internet network!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyhavePermission2(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 115) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with image picking
                getLatlong()
            } else {
                // Permission denied, handle accordingly
                // ...
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {
    }
    override fun onProviderDisabled(s: String) {}

    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

}