package com.project.farmingapp.view.chatapp.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.project.farmingapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.farmingapp.view.user.UserFarmFragment;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BottomSheetProfileDetailUser extends BottomSheetDialogFragment {
    String username;
    String imageURL;
    String bio;
    String userUid;
    Context context;

    CircleImageView iv_profile_bottom_sheet_profile_image;
    TextView tv_profile__bottom_sheet_fragment_username;
    TextView tv_profile_bottom_sheet_fragment_bio;

    public BottomSheetProfileDetailUser(String username, String imageURL, String bio, Context context, String userUid) {
        this.username = username;
        this.imageURL = imageURL;
        this.bio = bio;
        this.context = context;
        this.userUid = userUid;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bottom_sheet_show_profile, container, false);
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        UserFarmFragment farmFragment =  UserFarmFragment.newInstance(userUid);
        fragmentTransaction.add(R.id.farmFrame,farmFragment);
        fragmentTransaction.addToBackStack("farm");
        fragmentTransaction.commit();
        init(view);
        setDetails(username, imageURL, bio,userUid);
        return view;
    }



    private void setDetails(String username, String imageURL, String bio, String userUid) {
        tv_profile__bottom_sheet_fragment_username.setText(username);
        tv_profile_bottom_sheet_fragment_bio.setText(bio);

        if (imageURL.equals("default")) {
            iv_profile_bottom_sheet_profile_image.setImageResource(R.drawable.sample_img);
        } else {
            Glide.with(context).load(imageURL).into(iv_profile_bottom_sheet_profile_image);
        }

    }

    private void init(View view) {
        iv_profile_bottom_sheet_profile_image = view.findViewById(R.id.iv_profile_bottom_sheet);
        tv_profile__bottom_sheet_fragment_username = view.findViewById(R.id.tv_profile__bottom_sheet_fragment_username);
        tv_profile_bottom_sheet_fragment_bio = view.findViewById(R.id.tv_profile_bottom_sheet_fragment_bio);
    }

}
