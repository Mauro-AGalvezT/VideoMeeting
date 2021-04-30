package com.galvez.videomeeting.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.galvez.videomeeting.Models.User;
import com.galvez.videomeeting.Network.ApiClient;
import com.galvez.videomeeting.Network.ApiService;
import com.galvez.videomeeting.R;
import com.galvez.videomeeting.Utilities.Constants;
import com.galvez.videomeeting.Utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    String meetingRoom=null;
    private String inviterToken = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        preferenceManager=new PreferenceManager(getApplicationContext());

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
        imageStopInvitation.setOnClickListener(v -> {
            if (user!=null){
                cancelInvitation(user.token);
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful()&&task.getResult()!=null){
                inviterToken=task.getResult().getToken();
                if(meetringType!=null && user !=null){
                    initiateMeeting(meetringType,user.token);
                }
            }
        });


    }

    private void initiateMeeting(String meetingType, String receiverToken){
        try {

            JSONArray tokens= new JSONArray();
            tokens.put(receiverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME,preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME,preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL,preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN,inviterToken);

            meetingRoom=
                    preferenceManager.getString(Constants.KEY_USER_ID)+" "+
                    UUID.randomUUID().toString().substring(0,5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM,meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeards(),remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                        TextView textSendingInvitation = findViewById(R.id.textSendingInvitation);
                        textSendingInvitation.setText("Conectando...");
                    }else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitacion cancelada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else {
                    Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken){

        try {
            JSONArray tokens= new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION_CANCELLED);

        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type!=null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    try{
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions conferenceOptions =
                                new JitsiMeetConferenceOptions.Builder()
                                        .setServerURL(serverURL)
                                        .setWelcomePageEnabled(false)
                                        .setRoom(meetingRoom)
                                        .build();
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this,conferenceOptions);
                        finish();
                    }catch (Exception e){
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    Toast.makeText(context, "Invitacion rechazada", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}