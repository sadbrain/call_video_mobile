package com.example.callvideowithjavawebrtc.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.callvideowithjavawebrtc.R;
import com.example.callvideowithjavawebrtc.databinding.ActivityLoginBinding;
import com.example.callvideowithjavawebrtc.repository.MainRepository;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding views;
    private MainRepository mainRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());
        init();
    }

    private void init() {
        mainRepository = MainRepository.getInstance();
        views.enterBtn.setOnClickListener(v -> {
            //login to firebase here
            mainRepository.login(
                    views.username.getText().toString(),
                    getApplicationContext(),
                    () -> {
                         //if success then we want to move to call activity
                        startActivity(new Intent(LoginActivity.this, CallActivity.class));
                    });

        });
    }
}