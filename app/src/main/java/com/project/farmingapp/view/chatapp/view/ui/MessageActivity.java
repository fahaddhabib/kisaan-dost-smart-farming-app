package com.project.farmingapp.view.chatapp.view.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.fcmsender.FCMSender;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.farmingapp.R;
import com.project.farmingapp.databinding.ActiMessageBinding;
import com.project.farmingapp.view.chatapp.services.model.Chats;
import com.project.farmingapp.view.chatapp.services.model.Users;
import com.project.farmingapp.view.chatapp.services.notifications.Client;
import com.project.farmingapp.view.chatapp.services.notifications.Data;
import com.project.farmingapp.view.chatapp.services.notifications.MyResponse;
import com.project.farmingapp.view.chatapp.services.notifications.Sender;
import com.project.farmingapp.view.chatapp.services.notifications.Token;
import com.project.farmingapp.view.chatapp.view.adapters.MessageAdapter;
import com.project.farmingapp.view.chatapp.view.fragments.APIService;
import com.project.farmingapp.view.chatapp.view.fragments.BottomSheetProfileDetailUser;
import com.project.farmingapp.view.chatapp.viewModel.DatabaseViewModel;
import com.project.farmingapp.view.chatapp.viewModel.LogInViewModel;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    LogInViewModel logInViewModel;
    DatabaseViewModel databaseViewModel;

    String profileUserNAme;
    String profileImageURL;
    String bio;
    String profileUid;
    FirebaseUser currentFirebaseUser;

    String chat;
    String timeStamp;
    String userId_receiver; // userId of other user who'll receive the text // Or the user id of profile currently opened
    String userId_sender;  // current user id
    String user_status;
    MessageAdapter messageAdapter;
    ArrayList<Chats> chatsArrayList;
    Context context;
    BottomSheetProfileDetailUser bottomSheetProfileDetailUser;

    APIService apiService;
    boolean notify = false;

    private ActiMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActiMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId_receiver = getIntent().getStringExtra("userid");

        init();
        getCurrentFirebaseUser();
        fetchAndSaveCurrentProfileTextAndData();


        binding.ivUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetDetailFragment(profileUserNAme, profileImageURL, bio,profileUid);
            }
        });


        binding.ivSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;

                chat = binding.etChat.getText().toString().trim();
                if (!chat.equals("")) {
                    addChatInDataBase();
                } else {
                    Toast.makeText(MessageActivity.this, "Message can't be empty.", Toast.LENGTH_SHORT).show();
                }
                binding.etChat.setText("");
            }
        });

    }

    private void openBottomSheetDetailFragment(String username, String imageUrl, String bio,String UserUid) {
        bottomSheetProfileDetailUser = new BottomSheetProfileDetailUser(username, imageUrl, bio, context,UserUid);
        assert getSupportActionBar() != null;
        bottomSheetProfileDetailUser.show(getSupportFragmentManager(), "edit");
    }

    private void getCurrentFirebaseUser() {
        logInViewModel.getFirebaseUserLogInStatus();
        logInViewModel.firebaseUserLoginStatus.observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                currentFirebaseUser = firebaseUser;
                userId_sender = currentFirebaseUser.getUid();
            }
        });
    }



    private void fetchAndSaveCurrentProfileTextAndData() {
        if(userId_receiver == null){
            userId_receiver=  getIntent().getStringExtra("userId");
        }
        databaseViewModel.fetchSelectedUserProfileData(userId_receiver);
        databaseViewModel.fetchSelectedProfileUserData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);

                assert user != null;
                profileUserNAme = user.getUsername();
                profileImageURL = user.getImageUrl();
                bio = user.getBio();
                user_status = user.getStatus();
                profileUid = user.getId();

                try {
                    if (user_status.contains("online") && isNetworkConnected()) {
                        binding.ivUserStatusMessageView.setBackgroundResource(R.drawable.online_status);
                    } else {
                        binding.ivUserStatusMessageView.setBackgroundResource(R.drawable.offline_status);
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

                binding.tvProfileUserName.setText(profileUserNAme);
                if (profileImageURL.equals("default")) {
                    binding.ivUserImage.setImageResource(R.drawable.sample_img);
                } else {
                    Glide.with(getApplicationContext()).load(profileImageURL).into(binding.ivUserImage);
                }
                fetchChatFromDatabase(userId_receiver, userId_sender);
            }
        });

        addIsSeen();
    }

    public void addIsSeen() {
        String isSeen = "seen";
        databaseViewModel.fetchChatUser();
        databaseViewModel.fetchedChat.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Chats chats = dataSnapshot1.getValue(Chats.class);
                    assert chats != null;
                    if (chats.getSenderId().equals(userId_receiver) && chats.getReceiverId().equals(userId_sender)) {
                        databaseViewModel.addIsSeenInDatabase(isSeen, dataSnapshot1);
                    }
                }

            }
        });

    }


    public boolean isNetworkConnected() throws InterruptedException, IOException {   //check internet connectivity
        final String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    private void fetchChatFromDatabase(String myId, String senderId) {
        databaseViewModel.fetchChatUser();
        databaseViewModel.fetchedChat.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                chatsArrayList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chats chats = snapshot.getValue(Chats.class);
                    assert chats != null;
                    if (chats.getReceiverId().equals(senderId) && chats.getSenderId().equals(myId) || chats.getReceiverId().equals(myId) && chats.getSenderId().equals(senderId)) {
                        chatsArrayList.add(chats);
                    }

                    messageAdapter = new MessageAdapter(chatsArrayList, context, userId_sender);
                    binding.recyclerViewMessagesRecord.setAdapter(messageAdapter);
                }
            }
        });
    }

    private void addChatInDataBase() {

        long tsLong = System.currentTimeMillis();
        timeStamp = Long.toString(tsLong);
        databaseViewModel.addChatDb(userId_receiver, userId_sender, chat, timeStamp);
        databaseViewModel.successAddChatDb.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    // Toast.makeText(MessageActivity.this, "Sent.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MessageActivity.this, "Message can't be sent.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final String msg = chat;
        databaseViewModel.fetchingUserDataCurrent();
        databaseViewModel.fetchUserCurrentData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                assert users != null;
                if (notify) {
                    sendNotification(userId_receiver, users.getUsername(), msg);

                }
                notify = false;
            }
        });
    }

    private void sendNotification(String userId_receiver, String username, String msg) {
        databaseViewModel.getTokenDatabaseRef();
        databaseViewModel.getTokenRefDb.observe(this, new Observer<DatabaseReference>() {
            @Override
            public void onChanged(DatabaseReference databaseReference) {
                Query query = databaseReference.orderByKey().equalTo(userId_receiver);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Token token = snapshot.getValue(Token.class);
                            Data data = new Data(userId_sender, String.valueOf(R.drawable.ic_baseline_textsms_24), username + ": " + msg, "New Message", userId_receiver);

                            assert token != null;
                            Sender sender = new Sender(data, token.getToken());
                          //      sendPush(context,msg,token.getToken(),userId_receiver);
                            apiService.sendNotification(sender)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.code() == 200) {
                                                assert response.body() != null;
                                                if (response.body().success != 1) {

                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void sendPush(Context context, String body, String token, String to) {
        try {
            JSONObject data= new JSONObject();
            JSONObject pushData=new  JSONObject();
//            data.put("type", type);
            data.put("message_body",body);
            data.put("to",to);
            pushData.put("data",data);
            FCMSender push = new FCMSender.Builder()
                    .serverKey("AAAAknRXD3g:APA91bGWz9o5FIerP6SHEmjkRSAgkyI1J2FSAIJXH0HexlK2QYmxVMOYI6LrvSnF6uYMMvUVJf4_q6FBRTj8hfemR-vbHFRIVf8AVggDphwW3fLzydbflIN3ByDb4ioT417FsWFqy7Hv")
                    .setData(pushData)
                    .toTokenOrTopic(token)
                    .responseListener(new FCMSender.ResponseListener() {
                        @Override
                        public void onSuccess(@NonNull String s) {
                            Log.v("fcm","notification sent successfully to "+token);
                        }

                        @Override
                        public void onFailure(int i, @NonNull String s) {
                            Log.v("fcm","notification sent Failed to $token");
                        }
                    }).build();
            push.sendPush(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        databaseViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(DatabaseViewModel.class);
        logInViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(LogInViewModel.class);
        context = MessageActivity.this;

        apiService = Client.getClient("https://fcm.googleapis.com/v1/projects/farmingapp-beab6/").create(APIService.class);


        binding.ivBackButton.setOnClickListener(v -> {

            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerViewMessagesRecord.setLayoutManager(linearLayoutManager);

        chatsArrayList = new ArrayList<>();


    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }


    private void addStatusInDatabase(String status) {
        databaseViewModel.addStatusInDatabase("status", status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addStatusInDatabase("online");
        currentUser(userId_receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        addStatusInDatabase("offline");
        currentUser("none");
    }


}
