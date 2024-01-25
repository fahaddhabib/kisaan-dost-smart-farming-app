package com.project.farmingapp.view.chatapp.view.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.project.farmingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.project.farmingapp.databinding.ActiHomeBinding;
import com.project.farmingapp.view.chatapp.services.model.Users;
import com.project.farmingapp.view.chatapp.view.adapters.ViewPagerAdapter;
import com.project.farmingapp.view.chatapp.view.fragments.ChatFragment;
import com.project.farmingapp.view.chatapp.view.fragments.ProfileFragment;
import com.project.farmingapp.view.chatapp.view.fragments.UserFragment;
import com.project.farmingapp.view.chatapp.viewModel.DatabaseViewModel;
import com.project.farmingapp.view.chatapp.viewModel.LogInViewModel;

public class HomeActivity extends AppCompatActivity {
    LogInViewModel logInViewModel;
    DatabaseViewModel databaseViewModel;

    String username;
    String imageUrl;

    ViewPagerAdapter viewPagerAdapter;

    private ActiHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActiHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        fetchCurrentUserdata();
        setupPagerFragment();

    }

    private void setupPagerFragment() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), ViewPagerAdapter.POSITION_UNCHANGED);

        viewPagerAdapter.addFragment(new ChatFragment(this), "Chats");
        viewPagerAdapter.addFragment(new UserFragment(this), "Users");
        viewPagerAdapter.addFragment(new ProfileFragment(this), "Profile");

        binding.viewPager.setAdapter(viewPagerAdapter);

        binding.tabLayout.setupWithViewPager(binding.viewPager);

    }

    private void fetchCurrentUserdata() {
        databaseViewModel.fetchingUserDataCurrent();
        databaseViewModel.fetchUserCurrentData.observe(this, dataSnapshot -> {
            Users user = dataSnapshot.getValue(Users.class);
            if (user != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.linearLayout.setVisibility(View.VISIBLE);
                username = user.getUsername();
                imageUrl = user.getImageUrl();
                //  Toast.makeText(HomeActivity.this, "Welcome back " + username + ".", Toast.LENGTH_SHORT).show();
                binding.tvUsername.setText(username);
                if (imageUrl.equals("default")) {
                    binding.ivProfileImage.setImageResource(R.drawable.sample_img);
                } else {
                    Glide.with(getApplicationContext()).load(imageUrl).into(binding.ivProfileImage);
                }
            } else {
                Toast.makeText(HomeActivity.this, "User not found..", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void getUserAuthToSignOut() {
        logInViewModel.getFirebaseAuth();
        logInViewModel.firebaseAuthLiveData.observe(this, new Observer<FirebaseAuth>() {
            @Override
            public void onChanged(FirebaseAuth firebaseAuth) {
             /*   firebaseAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();*/
            }
        });
    }
    private void init() {

        logInViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(LogInViewModel.class);


        databaseViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(DatabaseViewModel.class);


        binding.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewPager.setCurrentItem(2); // to go to profile fragment
            }
        });

    }

    private void status(String status){
        databaseViewModel.addStatusInDatabase("status", status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
