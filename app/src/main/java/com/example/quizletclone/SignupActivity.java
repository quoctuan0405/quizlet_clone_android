package com.example.quizletclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apollographql.apollo3.ApolloCall;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.rx3.Rx3Apollo;
import com.example.quizapp.LoginMutation;
import com.example.quizapp.SignupMutation;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class SignupActivity extends AppCompatActivity {
    Button signupButton;
    EditText usernameInput, passwordInput;
    TextView loginIntent, errorTextView;
    ApolloClient apolloClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        apolloClient = ApolloClientManager.getApolloClient(this, getString(R.string.base_url));

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        errorTextView = findViewById(R.id.error);
        signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new SignupButtonListener());
        loginIntent = findViewById(R.id.login_intent);
        loginIntent.setOnClickListener(new SignupActivity.LoginIntentListener());
    }

    class LoginIntentListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    class SignupButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            ApolloCall<SignupMutation.Data> mutationCall = apolloClient.mutation(new SignupMutation(username, password));
            Single<ApolloResponse<SignupMutation.Data>> mutationResponse = Rx3Apollo.single(mutationCall);

            mutationResponse
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                if (response.data != null && response.data.signup != null && !response.hasErrors()) {
                                    User.setToken(SignupActivity.this, response.data.signup.accessToken);
                                    User.setUserId(SignupActivity.this, response.data.signup.id);

                                    Intent intent = new Intent(SignupActivity.this, ListSetActivity.class);
                                    startActivity(intent);

                                } else {
                                    errorTextView.setVisibility(View.VISIBLE);
                                }
                            },
                            throwable -> {
                                throwable.printStackTrace();
                                errorTextView.setVisibility(View.VISIBLE);
                            }
                    );
        }
    }
}