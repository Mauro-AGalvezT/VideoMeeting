package com.galvez.videomeeting.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.galvez.videomeeting.Models.User;
import com.galvez.videomeeting.R;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users;

    public UsersAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_user,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();

    }

    static class UserViewHolder extends RecyclerView.ViewHolder{

        TextView textFirstChar,textUsername,textEmail;
        ImageView imageAudioMeeting,imageVideoMeeting;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textFirstChar=itemView.findViewById(R.id.textFirstChar);
            textUsername=itemView.findViewById(R.id.textUsername);
            textEmail=itemView.findViewById(R.id.textEmail);
            imageAudioMeeting=itemView.findViewById(R.id.imageAudioMeeting);
            imageVideoMeeting=itemView.findViewById(R.id.imageViewoMeeting);
        }

        void setUserData(User user){
            textFirstChar.setText(user.fistName.substring(0,1));
            textUsername.setText(String.format("%s %s",user.fistName,user.lastName));
            textEmail.setText(user.email);
        }

    }
}
