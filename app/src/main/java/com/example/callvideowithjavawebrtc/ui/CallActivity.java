package com.example.callvideowithjavawebrtc.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.callvideowithjavawebrtc.R;
import com.example.callvideowithjavawebrtc.databinding.ActivityCallBinding;
import com.example.callvideowithjavawebrtc.databinding.ActivityLoginBinding;
import com.example.callvideowithjavawebrtc.repository.MainRepository;
import com.example.callvideowithjavawebrtc.utils.DataModelType;

public class CallActivity extends AppCompatActivity implements MainRepository.Listener{

    private ActivityCallBinding views;
    private MainRepository mainRepository;
    private Boolean isMicrophoneMuted = false;
    private Boolean isCameraMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());
        init();
    }

    private void init() {
        mainRepository = MainRepository.getInstance();
        views.callBtn.setOnClickListener(v -> {
            mainRepository.sendCallRequest(
                    views.targetUserNameEt.getText().toString(),
                    ()->{
                        Toast.makeText(this, "couldnt find the target", Toast.LENGTH_SHORT).show();
                    });
        });
        mainRepository.initLocalView(views.localView);
        mainRepository.initRemoteView(views.remoteView);
        mainRepository.listener = this;
        mainRepository.subscribeForLatestEvent(data->{
            if(data.getType() == DataModelType.StartCall){
                runOnUiThread(()->{
                    views.incomingNameTV.setText(data.getSender()+" is calling you");
                    views.incomingCallLayout.setVisibility(View.VISIBLE);
                    views.acceptButton.setOnClickListener(v->{
                        //star the call here
                        mainRepository.startCall(data.getSender());
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                    views.rejectButton.setOnClickListener(v->{
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                });
            }
        });
        views.switchCameraButton.setOnClickListener(v -> {
            mainRepository.switchCamera();
        });
        views.micButton.setOnClickListener(v -> {
            if (isMicrophoneMuted){
                views.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
            }else {
                views.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
            }
            mainRepository.toggleAudio(isMicrophoneMuted);
            isMicrophoneMuted=!isMicrophoneMuted;
        });
        views.videoButton.setOnClickListener(v->{
            if (isCameraMuted){
                views.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
            }else {
                views.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
            }
            mainRepository.toggleVideo(isCameraMuted);
            isCameraMuted=!isCameraMuted;
        });
        views.endCallButton.setOnClickListener(v->{
            mainRepository.endCall();
            finish();
        });
    }

    @Override
    public void webrtcConnected() {
        runOnUiThread(()->{
            views.incomingCallLayout.setVisibility(View.GONE);
            views.whoToCallLayout.setVisibility(View.GONE);
            views.callLayout.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void webrtcClosed() {
        runOnUiThread(this::finish);
    }
}