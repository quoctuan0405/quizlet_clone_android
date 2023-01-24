package com.example.quizletclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingActivity extends AppCompatActivity {
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new LogoutButtonListener());
    }

    class LogoutButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            User.removeToken(SettingActivity.this);
            User.removeUserId(SettingActivity.this);

            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}