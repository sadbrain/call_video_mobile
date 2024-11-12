package com.example.callvideowithjavawebrtc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.callvideowithjavawebrtc.databinding.ActivityLoginBinding;
import com.example.callvideowithjavawebrtc.repository.MainRepository;
import com.permissionx.guolindev.PermissionX;

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
            PermissionX.init(this)
                    .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            mainRepository.login(views.username.getText().toString(), getApplicationContext(), () -> {
                                // Sau khi đăng nhập thành công, chuyển đến màn hình gọi
                                startActivity(new Intent(LoginActivity.this, CallActivity.class));
                            });
                        } else {
                            Toast.makeText(this, "Lỗi: Cần cấp quyền camera và ghi âm", Toast.LENGTH_SHORT).show();
                        }
                    });



        });
    }
}