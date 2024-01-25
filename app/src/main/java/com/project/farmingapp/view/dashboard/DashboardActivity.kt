package com.project.farmingapp.view.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.project.farmingapp.R
import com.project.farmingapp.databinding.ActivityDashboardBinding
import com.project.farmingapp.view.articles.AddArticlesFragment
import com.project.farmingapp.view.auth.PhoneAuthActivity
import com.project.farmingapp.view.chatapp.view.ui.HomeActivity
import com.project.farmingapp.view.ecom.EcommBuyFragment
import com.project.farmingapp.view.expenseapp.ActivityExpenseMain
import com.project.farmingapp.view.user.UserFragment
import com.project.farmingapp.viewmodel.UserDataViewModel
import com.project.farmingapp.viewmodel.UserProfilePostsViewModel

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dashboardFragment: DashboardFragment
    private lateinit var ecommBuyFragment: EcommBuyFragment
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var userFragment: UserFragment
    private lateinit var addArticlesFragment: AddArticlesFragment
    private lateinit var viewModel: UserDataViewModel
    private lateinit var viewModel2: UserProfilePostsViewModel

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var userName = ""
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)
        binding.userDataViewModel = viewModel

        // Navigation view clicks
        navViewclicks()
        // Custom app bar
        initToolbar()

        viewModel2 = ViewModelProvider(this)
            .get(UserProfilePostsViewModel::class.java)

        viewModel.getUserData(firebaseAuth.currentUser!!.uid)

        dashboardFragment = DashboardFragment()

        supportFragmentManager
            .beginTransaction()
            .replace(binding.frameLayout.id, dashboardFragment, "dashFrag")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setReorderingAllowed(true)
            .commit()

        binding.bottomNav.setItemSelected(R.id.bottomNavHome)

        if (dashboardFragment.isVisible) {
            binding.bottomNav.setItemSelected(R.id.bottomNavHome)
        }

        ecommBuyFragment = EcommBuyFragment()

        binding.bottomNav.setOnItemSelectedListener { id ->
            when (id) {
                R.id.bottomNavHome -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(binding.frameLayout.id, dashboardFragment, "dashFrag")
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        setReorderingAllowed(true)
                        addToBackStack("dashFrag")
                        commit()
                    }
                }
                R.id.bottomNavEcommerce -> {
                    ecommBuyFragment = EcommBuyFragment()

                    supportFragmentManager.beginTransaction().apply {
                        replace(binding.frameLayout.id, ecommBuyFragment, "ecomFrag")
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        setReorderingAllowed(true)
                        addToBackStack("ecommFrag")
                        commit()
                    }
                }
                R.id.bottomNavProfile -> {
                    userFragment = UserFragment()
                    supportFragmentManager.beginTransaction().apply {
                        replace(binding.frameLayout.id, userFragment, "userFrag")
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        setReorderingAllowed(true)
                        addToBackStack("userFrag")
                        commit()
                    }
                }
                R.id.bottomNavArticles -> {
                    addArticlesFragment = AddArticlesFragment()
                    supportFragmentManager.beginTransaction().apply {
                        replace(binding.frameLayout.id, addArticlesFragment, "articlesfrag")
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        setReorderingAllowed(true)
                        addToBackStack("articlesfrag")
                        commit()
                    }
                }
            }
        }

        viewModel.userliveData.observe(this, Observer { data ->
            try {
                userName = data.get("name").toString()
                userEmail = data.get("email").toString()

                binding.navbarUserName.text = userName
                binding.navbarUserEmail.text = userEmail
                Glide.with(this).load(data.get("imageurl")).into(binding.navbarUserImage)

                Log.d("User Data from VM", data.getString("name")!!)
            } catch (e: Exception) {
                Log.e("User Data E: ", e.message!!)
            }
        })
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.customNavigationIcon.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
                binding.drawerLayout.closeDrawer(binding.navView)
            } else {
                binding.drawerLayout.openDrawer(binding.navView)
            }
        }
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        toggle.isDrawerIndicatorEnabled = false
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun navViewclicks() {
        binding.ecomnavlay.setOnClickListener { view ->
            ecommBuyFragment = EcommBuyFragment()
            binding.bottomNav.setItemSelected(R.id.bottomNavEcommerce)
            supportFragmentManager.beginTransaction().apply {
                replace(binding.frameLayout.id, ecommBuyFragment, "ecomFrag")
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                setReorderingAllowed(true)
                addToBackStack("ecommFrag")
                commit()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.expertnavlay.setOnClickListener {
            val intent = Intent(applicationContext, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext.startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.expensenavlay.setOnClickListener {
            val intent = Intent(applicationContext, ActivityExpenseMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext.startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.logoutnavlay.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Log Out")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    firebaseAuth.signOut()
                    Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show()
                    Intent(this, PhoneAuthActivity::class.java).also {
                        startActivity(it)
                    }
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.frameLayout.id, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            setReorderingAllowed(true)
            addToBackStack("name")
            commit()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toggle.syncState()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (dashboardFragment.isVisible) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }
}