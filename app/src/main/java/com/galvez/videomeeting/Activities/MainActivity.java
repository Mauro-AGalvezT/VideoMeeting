package com.galvez.videomeeting.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.galvez.videomeeting.Adapters.UsersAdapter;
import com.galvez.videomeeting.Listeners.UserListener;
import com.galvez.videomeeting.Models.User;
import com.galvez.videomeeting.R;
import com.galvez.videomeeting.Utilities.Constants;
import com.galvez.videomeeting.Utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserListener {

    private PreferenceManager preferenceManager;
    private List<User> users;
    private UsersAdapter usersAdapter;
    private TextView textErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = new PreferenceManager(getApplicationContext());

        TextView textTitle=findViewById(R.id.textTitle);
        textTitle.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        findViewById(R.id.textSignOut).setOnClickListener(v -> signOut());

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult()!=null){
                sendFCMTokentoDatabase(task.getResult().getToken());
            }
        });

        RecyclerView usersRecyclerView = findViewById(R.id.usersRecyclerView);

        textErrorMessage = findViewById(R.id.textErrorMessage);

        users = new ArrayList<>();
        usersAdapter=new UsersAdapter(users,this);
        usersRecyclerView.setAdapter(usersAdapter);

        swipeRefreshLayout=findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);
        getUsers();

    }

    private void getUsers(){
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    String myUserId=preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful()&& task.getResult()!=null){
                        users.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if (myUserId.equals(documentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.fistName=documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                            user.lastName=documentSnapshot.getString(Constants.KEY_LAST_NAME);
                            user.email=documentSnapshot.getString(Constants.KEY_EMAIL );
                            user.token=documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if (users.size()>0){
                            usersAdapter.notifyDataSetChanged();
                        }
                        else{
                            textErrorMessage.setText(String.format("%s","No users available"));
                            textErrorMessage.setVisibility(View.VISIBLE);
                        }
                    }else {
                        textErrorMessage.setText(String.format("%s","No users available"));
                        textErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void sendFCMTokentoDatabase(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "No se envio el token:"+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void signOut(){
        Toast.makeText(this, "Cerrando sesión", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates=new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPreferences();
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "No se pudo cerrar sesión", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void initiateVideoMeeting(User user) {
        if(user.token==null || user.token.trim().isEmpty()){
            Toast.makeText(this, user.fistName+" "+user.lastName+" no se puede conectar", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(User user) {
        if(user.token==null || user.token.trim().isEmpty()){
            Toast.makeText(this, user.fistName+" "+user.lastName+" no se puede conectar", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","audio");
            startActivity(intent);
        }
    }
}