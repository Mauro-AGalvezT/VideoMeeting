package com.galvez.videomeeting.Listeners;

import com.galvez.videomeeting.Models.User;

public interface UserListener {

    void initiateVideoMeeting(User user);

    void initiateAudioMeeting (User user);
}
