package com.galvez.videomeeting.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.galvez.videomeeting.Network.ApiClient;
import com.galvez.videomeeting.Network.ApiService;
import com.galvez.videomeeting.R;
import com.galvez.videomeeting.Utilities.Constants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);

        ImageView imageMeetingType=findViewById(R.id.imageMeetingType);
        String meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        
        if (meetingType!=null){
            if (meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }
        }
        TextView textFirstChar = findViewById(R.id.textFirstChar);
        TextView textUserName = findViewById(R.id.textUserName);
        TextView textEmail = findViewById(R.id.textEmail);

        String firstName = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if (firstName!=null){
            textFirstChar.setText(firstName.substring(0,1));
        }

        textUserName.setText(String.format(
                "%s %s",
                firstName,
                getIntent().getStringExtra(Constants.KEY_LAST_NAME)
        ));
        textEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        ImageView imageAcceptedInvitation= findViewById(R.id.imageAcceptedInvitation);
        imageAcceptedInvitation.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

        ImageView imageRejectedInvitation = findViewById(R.id.imageRejectInvitation);
        imageRejectedInvitation.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));
    }

    private void sendInvitationResponse(String type, String receiverToken){

        try {
            JSONArray tokens= new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,type);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

            sendRemoteMessage(body.toString(),type);

        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                        
                        try {
                            URL serverURL = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions conferenceOptions =
                                    new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(serverURL)
                                    .setWelcomePageEnabled(false)
                                    .setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))
                                    .build();
                            JitsiMeetActivity.launch(IncomingInvitationActivity.this,conferenceOptions);
                            finish();
                        }catch (Exception e){
                            Toast.makeText(IncomingInvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();

                        }
                        
                    }else {
                        Toast.makeText(IncomingInvitationActivity.this, "Invitacion rechazada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else {
                    Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}