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
import com.example.quizapp.MeQuery;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class LoginActivity extends AppCompatActivity {
    Button loginButton;
    EditText usernameInput, passwordInput;
    TextView signupIntent, errorTextView;
    ApolloClient apolloClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apolloClient = ApolloClientManager.getApolloClient(this, getString(R.string.base_url));

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        errorTextView = findViewById(R.id.error);
        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new LoginActivity.LoginButtonListener());
        signupIntent = findViewById(R.id.signup_intent);
        signupIntent.setOnClickListener(new LoginActivity.SignupIntentListener());
    }

    class SignupIntentListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        }
    }

    class LoginButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            ApolloCall<LoginMutation.Data> mutationCall = apolloClient.mutation(new LoginMutation(username, password));
            Single<ApolloResponse<LoginMutation.Data>> mutationResponse = Rx3Apollo.single(mutationCall);

            mutationResponse
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                if (response.data != null && response.data.login != null && !response.hasErrors()) {
                                    User.setToken(LoginActivity.this, response.data.login.accessToken);
                                    User.setUserId(LoginActivity.this, response.data.login.id);

                                    Intent intent = new Intent(LoginActivity.this, ListSetActivity.class);
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