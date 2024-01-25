package com.project.farmingapp.view.user

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.project.farmingapp.adapter.FarmListUserProfileAdapter
import com.project.farmingapp.utilities.CellClickListener2
import com.project.farmingapp.viewmodel.UserDataViewModel
import com.project.farmingapp.viewmodel.UserProfileFarmViewModel
import androidx.lifecycle.Observer
import com.project.farmingapp.databinding.FragmentUserFarmBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UserFarmFragment() : Fragment(), CellClickListener2 {
    private lateinit var binding: FragmentUserFarmBinding

    // TODO: Rename and change types of parameters
    private var userUid: String? = null
    // users
    private lateinit var userDataViewModel: UserDataViewModel
    // user farms
    private lateinit var viewModelFarms: UserProfileFarmViewModel

    val firebaseAuth = FirebaseAuth.getInstance()
    private val observer = Observer<List<String>> { data ->
        // Handle the data update here
        // This code will be executed when the LiveData changes
        if (data != null) {
            viewModelFarms.getAllFarmsOfUser(data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userUid = it.getString(ARG_PARAM1)
            //        param2 = it.getString(ARG_PARAM2)
        }

        viewModelFarms = ViewModelProviders.of(requireActivity())
            .get(UserProfileFarmViewModel::class.java)

        userDataViewModel = ViewModelProviders.of(requireActivity())
            .get(UserDataViewModel::class.java)

        if (userUid == "") {
            viewModelFarms.getAllFarms(firebaseAuth.currentUser!!.uid)
        }
        else{
            viewModelFarms.getAllFarms(userUid)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        addLiveDataObserver()

        binding = FragmentUserFarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun addLiveDataObserver() {
        // Add observer using viewLifecycleOwner

        viewModelFarms.liveData1.observe(viewLifecycleOwner,observer)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModelFarms.liveData1.removeObserver(observer)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param userUid Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserFarmFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(userUid: String) =
            UserFarmFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, userUid)
                    // putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Profile"
    }


    override fun onCellClickListener2(farmId: String) {
        val dialog = AlertDialog.Builder(activity)

        dialog.setTitle("Your Farm")
            .setMessage("Do you want to View your ?")
            .setPositiveButton("View") { dialogInterface, i ->

                Toast.makeText(requireActivity().applicationContext, "...", Toast.LENGTH_SHORT).show()

/*                val bundle = Bundle()
                bundle.putString("farmID", farmId)
                val updateFarmsFragment = UpdateFarmsFragment()
                updateFarmsFragment.arguments = bundle


                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, updateFarmsFragment, "updatefarms")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("updatefarms")
                    .commit()*/


            }
         /*   .setNegativeButton("Delete") { dialogInterface, i ->
                userDataViewModel.deleteUserFarm(firebaseAuth.currentUser!!.uid!!, farmId)
                userDataViewModel.getUserData(firebaseAuth.currentUser!!.uid.toString())
                viewModelFarms.getAllFarms(firebaseAuth.currentUser!!.uid)
            }*/
            .setNeutralButton("Cancel"){
                    dialogInterface, i ->

            }
            .show()

        Toast.makeText(requireActivity().applicationContext, "You Clicked$farmId", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        viewModelFarms.liveData3.observe(viewLifecycleOwner) {
            Log.d("All Posts", it.toString())
            val adapter = FarmListUserProfileAdapter(
                requireActivity().applicationContext,
                requireActivity(),
                it,
                this
            )
            binding.userProfileFarmsRecycler.adapter = adapter

            binding.userProfileFarmsRecycler.layoutManager =
                LinearLayoutManager(
                    requireActivity().applicationContext,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            adapter.notifyDataSetChanged()

        }

    }
}
