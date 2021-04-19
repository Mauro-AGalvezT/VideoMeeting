package com.galvez.videomeeting.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.galvez.videomeeting.Models.User;
import com.galvez.videomeeting.R;

public class OutgoingInvitationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        String meetringType = getIntent().getStringExtra("type");

        if(meetringType!=null){
            if(meetringType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }
        }
        TextView textFirstChar = findViewById(R.id.textFirstCharO);
        TextView textUserName = findViewById(R.id.textUserNameO);
        TextView textEmail = findViewById(R.id.textEmailO);

        User user= (User) getIntent().getSerializableExtra("user");
        if (user!=null){
            textFirstChar.setText(user.fistName.substring(0,1));
            textUserName.setText(String.format("%s %s",user.fistName,user.lastName));
            textEmail.setText(user.email);
        }

        ImageView imageStopInvitation = findViewById(R.id.imageStopInvitation);
        imageStopInvitation.setOnClickListener(v -> onBackPressed());

    }
}