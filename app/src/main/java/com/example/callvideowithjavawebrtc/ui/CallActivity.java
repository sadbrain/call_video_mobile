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

public class CallActivity extends AppCompatActivity {

    private ActivityCallBinding views;
    private MainRepository mainRepository;
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
        mainRepository.subscribeForLatestEvent(data->{
            if(data.getType() == DataModelType.StartCall){
                runOnUiThread(()->{
                    views.incomingNameTV.setText(data.getSender()+" is calling you");
                    views.incomingCallLayout.setVisibility(View.VISIBLE);
                    views.acceptButton.setOnClickListener(v->{
                        //star the call here
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                    views.rejectButton.setOnClickListener(v->{
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                });
            }
        });
    }
}