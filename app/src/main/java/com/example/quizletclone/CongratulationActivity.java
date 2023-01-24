package com.example.quizletclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CongratulationActivity extends AppCompatActivity {
    Button continueLearningButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulation);

        continueLearningButton = findViewById(R.id.continue_learning);
        continueLearningButton.setOnClickListener(new ContinueLearningButtonListener());
    }

    class ContinueLearningButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(CongratulationActivity.this, ListSetActivity.class);
            startActivity(intent);
        }
    }
}