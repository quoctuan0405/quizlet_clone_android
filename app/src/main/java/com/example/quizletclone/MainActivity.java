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

public class MainActivity extends AppCompatActivity {
    ApolloClient apolloClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apolloClient = ApolloClientManager.getApolloClient(this, getString(R.string.base_url));

        switchActivityIfLoggedIn();
    }

    private void switchActivityIfLoggedIn() {
        ApolloCall<MeQuery.Data> queryCall = apolloClient.query(new MeQuery());
        Single<ApolloResponse<MeQuery.Data>> queryResponse = Rx3Apollo.single(queryCall);

        queryResponse
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.data != null && response.data.me != null && !response.hasErrors()) {
                                Intent intent = new Intent(MainActivity.this, ListSetActivity.class);
                                startActivity(intent);

                            } else {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        },
                        Throwable::printStackTrace
                );
    }
}