package com.example.quizletclone;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.http.HttpRequest;
import com.apollographql.apollo3.api.http.HttpResponse;
import com.apollographql.apollo3.network.http.HttpInterceptor;
import com.apollographql.apollo3.network.http.HttpInterceptorChain;

import kotlin.coroutines.Continuation;

public class ApolloClientManager {
    private static ApolloClient INSTANCE = null;

    private ApolloClientManager() {
    }

    public static synchronized ApolloClient getApolloClient(Context context, String baseUrl) {
        if (INSTANCE == null) {
            class AuthorizationInterceptor implements HttpInterceptor {
                @Override
                public void dispose() {
                }

                @Nullable
                @Override
                public Object intercept(@NonNull HttpRequest httpRequest, @NonNull HttpInterceptorChain httpInterceptorChain, @NonNull Continuation<? super HttpResponse> continuation) {
                    String accessToken = User.getToken(context);
                    if (accessToken == null) {
                        return httpInterceptorChain.proceed(httpRequest, continuation);

                    } else {
                        HttpRequest newHttpRequest = httpRequest.newBuilder().addHeader("accessToken", accessToken).build();
                        return httpInterceptorChain.proceed(newHttpRequest, continuation);
                    }
                }
            }

            ApolloClient.Builder builder = new ApolloClient.Builder()
                    .serverUrl(baseUrl)
                    .addHttpInterceptor(new AuthorizationInterceptor());
            ApolloClient client = builder.build();

            INSTANCE = client;
        }

        return INSTANCE;
    }
}
