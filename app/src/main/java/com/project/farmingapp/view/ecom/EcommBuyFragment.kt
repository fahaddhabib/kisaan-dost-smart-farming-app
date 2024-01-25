package com.project.farmingapp.view.ecom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.farmingapp.R
import com.project.farmingapp.adapter.EcommPostListAdapter
import com.project.farmingapp.databinding.FragmentEcomBuyBinding
import com.project.farmingapp.viewmodel.SocialMediaViewModel

class EcommBuyFragment : Fragment() {

    private lateinit var binding: FragmentEcomBuyBinding

    private lateinit var viewModel: SocialMediaViewModel

    lateinit var ecomSellFrag: EcomSellFragment
    lateinit var ecommextendedbuyfragment: EcommExtendedBuyFragment
    private var adapter : EcommPostListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity())
            .get<SocialMediaViewModel>(SocialMediaViewModel::class.java)

//        viewModel.loadPosts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEcomBuyBinding.inflate(inflater, container, false)
        return binding.root

    }

    fun getData() {
        val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("posts").orderBy("timeStamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener {
                Log.d("Posts data", it.documents.toString())
                adapter = EcommPostListAdapter(requireActivity().applicationContext, requireActivity(), it.documents)
                binding.postsRecycler.adapter = adapter
                binding.postsRecycler.layoutManager = LinearLayoutManager(requireActivity().applicationContext)
            }
            .addOnFailureListener {

            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "E-Commerce"

        // Set up OnClickListener for the button
        ecommextendedbuyfragment = EcommExtendedBuyFragment()

        getData()
        ecomSellFrag = EcomSellFragment()
        binding.createPostFloating.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, ecomSellFrag, "ecomSell")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("ecomSell")
                .commit()
        }
    }


}