package com.example.quizletclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;

import com.apollographql.apollo3.ApolloCall;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.api.Optional;
import com.apollographql.apollo3.rx3.Rx3Apollo;
import com.example.quizapp.FindSetQuery;
import com.example.quizapp.SetsQuery;
import com.example.quizletclone.adapter.ListSetAdapter;
import com.example.quizletclone.listener.SetPreviewListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class ListSetActivity extends AppCompatActivity {
    ApolloClient apolloClient;
    SearchView searchView;
    Button createSetButton;
    RecyclerView listSetRecyclerView;
    ListSetAdapter listSetAdapter;
    ImageView settingIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_set);

        apolloClient = ApolloClientManager.getApolloClient(this, getString(R.string.base_url));

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchViewListener());

        createSetButton = findViewById(R.id.add_set);
        createSetButton.setOnClickListener(new AddSetOnClickListener());

        settingIcon = findViewById(R.id.setting_icon);
        settingIcon.setOnClickListener(new SettingIconListener());

        listSetRecyclerView = findViewById(R.id.list_set);
        listSetRecyclerView.setHasFixedSize(true);
        listSetRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        listSetAdapter = new ListSetAdapter(this, new SetPreviewOnClickListener());
        listSetRecyclerView.setAdapter(listSetAdapter);

        loadSets();
    }

    protected void loadSets() {
        ApolloCall<SetsQuery.Data> queryCall = apolloClient.query(new SetsQuery());
        Single<ApolloResponse<SetsQuery.Data>> queryResponse = Rx3Apollo.single(queryCall);

        queryResponse
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.data != null && response.data.sets != null && !response.hasErrors()) {
                                listSetAdapter.loadSets(response.data.sets);
                            }
                        },
                        Throwable::printStackTrace
                );
    }

    class SetPreviewOnClickListener implements SetPreviewListener {
        @Override
        public void onSetPreviewClick(SetsQuery.Set set) {
            Intent intent = new Intent(ListSetActivity.this, SetDetailActivity.class)
                    .putExtra("setId", set.id);
            startActivity(intent);
        }
    }

    class AddSetOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ListSetActivity.this, CreateSetActivity.class);
            startActivity(intent);
        }
    }

    class SearchViewListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (query != null && !query.isEmpty()) {
                ApolloCall<FindSetQuery.Data> queryCall = apolloClient.query(new FindSetQuery(Optional.present(query)));
                Single<ApolloResponse<FindSetQuery.Data>> queryResponse = Rx3Apollo.single(queryCall);

                queryResponse
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.data != null && response.data.findSet != null && !response.hasErrors()) {
                                        List<SetsQuery.Set> sets = new ArrayList<>();
                                        for (int i = 0; i < response.data.findSet.size(); i++) {
                                            FindSetQuery.FindSet foundSet = response.data.findSet.get(i);

                                            SetsQuery._Count count = new SetsQuery._Count(foundSet._count.terms);
                                            SetsQuery.Author author = new SetsQuery.Author(foundSet.author.id, foundSet.author.username);
                                            SetsQuery.Set set = new SetsQuery.Set(foundSet.id, foundSet.name, count, author);
                                            sets.add(set);
                                        }

                                        listSetAdapter.loadSets(sets);
                                    }
                                },
                                Throwable::printStackTrace
                        );

            }

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText.isEmpty()) {
                loadSets();
            }

            return false;
        }
    }

    class SettingIconListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ListSetActivity.this, SettingActivity.class);
            startActivity(intent);
        }
    }
}