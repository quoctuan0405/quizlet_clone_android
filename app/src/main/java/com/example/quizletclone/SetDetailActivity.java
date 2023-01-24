package com.example.quizletclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apollographql.apollo3.ApolloCall;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.rx3.Rx3Apollo;
import com.example.quizapp.SetQuery;
import com.example.quizletclone.adapter.ListTermAdapter;

import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class SetDetailActivity extends AppCompatActivity {
    ApolloClient apolloClient;
    TextView setNameTextView, currentCardCountTextView, flashcardContentTextView, authorTextView, termInThisSetTextView;
    Button previousTermButton, nextTermButton, learnButton, editButton;
    CardView flashcard;
    RecyclerView listTermRecyclerView;
    ListTermAdapter listTermAdapter;
    String setId;
    SetQuery.Set set;
    int currentTermIndex;
    boolean showAnswer = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_detail);

        // Apollo client
        apolloClient = ApolloClientManager.getApolloClient(this, getString(R.string.base_url));

        // Get set id from previous screen
        setId = getIntent().getStringExtra("setId");

        // Get view element
        setNameTextView = findViewById(R.id.set_name_input);
        flashcard = findViewById(R.id.flashcard);
        currentCardCountTextView = findViewById(R.id.current_card_count);
        flashcardContentTextView = findViewById(R.id.flashcard_content);
        authorTextView = findViewById(R.id.author);
        termInThisSetTextView = findViewById(R.id.term_in_this_set);

        previousTermButton = findViewById(R.id.previous_term_button);
        nextTermButton = findViewById(R.id.next_term_button);
        learnButton = findViewById(R.id.learn_button);
        editButton = findViewById(R.id.edit_button);

        listTermRecyclerView = findViewById(R.id.list_term);
        listTermRecyclerView.setHasFixedSize(true);
        listTermRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        listTermAdapter = new ListTermAdapter(this);
        listTermRecyclerView.setAdapter(listTermAdapter);

        // Set on click listener
        previousTermButton.setOnClickListener(new PreviousTermButtonListener());
        nextTermButton.setOnClickListener(new NextTermButtonListener());
        learnButton.setOnClickListener(new LearningButtonListener());
        editButton.setOnClickListener(new EditButtonListener());
        flashcard.setOnClickListener(new FlipFlashcardListener());

        // Load data and fill it to screen
        loadData();
    }

    private void loadData() {
        ApolloCall<SetQuery.Data> queryCall = apolloClient.query(new SetQuery(setId));
        Single<ApolloResponse<SetQuery.Data>> queryResponse = Rx3Apollo.single(queryCall);

        queryResponse
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.data != null && response.data.set != null && !response.hasErrors()) {
                                set = response.data.set;
                                loadDataToScreen();
                            }
                        },
                        Throwable::printStackTrace
                );
    }

    private void loadDataToScreen() {
        setNameTextView.setText(set.name);

        showFlashcard();

        authorTextView.setText(set.author.username);

        String termInThisSetText = String.format(Locale.US, "%s (%d)", getString(R.string.term_in_this_set), set.terms.size());
        termInThisSetTextView.setText(termInThisSetText);

        if (set.author.id.equals(User.getUserId(SetDetailActivity.this))) {
            editButton.setVisibility(View.VISIBLE);
        }

        listTermAdapter.loadTerms(set.terms);
    }

    private void showFlashcard() {
        if (currentTermIndex < set.terms.size()) {
            String currentCardCountText = String.format(Locale.US, "%d/%d", currentTermIndex + 1, set.terms.size());
            currentCardCountTextView.setText(currentCardCountText);

            if (showAnswer) {
                flashcardContentTextView.setText(set.terms.get(currentTermIndex).question);
            } else {
                flashcardContentTextView.setText(set.terms.get(currentTermIndex).answer);
            }

        } else {
            currentCardCountTextView.setText("0/0");
        }
    }

    class FlipFlashcardListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showAnswer = !showAnswer;
            showFlashcard();
        }
    }

    class NextTermButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (currentTermIndex < set.terms.size() - 1) {
                currentTermIndex++;
            } else {
                currentTermIndex = 0;
            }

            showFlashcard();
        }
    }

    class PreviousTermButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (currentTermIndex > 0) {
                currentTermIndex--;
            } else {
                currentTermIndex = set.terms.size() - 1;
            }

            showFlashcard();
        }
    }

    class LearningButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SetDetailActivity.this, LearningActivity.class)
                    .putExtra("setId", set.id);
            startActivity(intent);
        }
    }

    class EditButtonListener implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SetDetailActivity.this, UpdateSetActivity.class)
                    .putExtra("setId", set.id);
            startActivity(intent);
        }
    }
}