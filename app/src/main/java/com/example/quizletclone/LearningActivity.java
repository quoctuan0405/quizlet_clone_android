package com.example.quizletclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apollographql.apollo3.ApolloCall;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.rx3.Rx3Apollo;
import com.example.quizapp.ReportUserLearningTermsMutation;
import com.example.quizapp.SetQuery;
import com.example.quizapp.SetUserLearningSetMutation;
import com.example.quizapp.type.TermReport;
import com.example.quizletclone.adapter.ListChoiceAdapter;
import com.example.quizletclone.listener.ChoiceListener;
import com.example.quizletclone.model.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class LearningActivity extends AppCompatActivity {
    ApolloClient apolloClient;
    TextView countTermRemainingTextView, questionTextView, resultTextView, explanationTextView;
    RecyclerView listChoiceRecycleView;
    String setId;
    Button nextQuestionButton;
    ListChoiceAdapter listChoiceAdapter;

    SetQuery.Set set;
    List<SetQuery.Term> learningTermsPool = new ArrayList<>();
    List<String> answersPool = new ArrayList<>();
    int currentLearningTermIndex;
    Quiz currentQuiz; // Our own model of term

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        // Apollo client
        apolloClient = ApolloClientManager.getApolloClient(this, getString(R.string.base_url));

        // Get set id from previous screen
        setId = getIntent().getStringExtra("setId");

        // Get view element
        countTermRemainingTextView = findViewById(R.id.count_term_remaining);
        questionTextView = findViewById(R.id.question);
        resultTextView = findViewById(R.id.result);
        explanationTextView = findViewById(R.id.explanation);

        listChoiceRecycleView = findViewById(R.id.list_choice);
        listChoiceRecycleView.setHasFixedSize(true);
        listChoiceRecycleView.setLayoutManager(new GridLayoutManager(this, 1));
        listChoiceAdapter = new ListChoiceAdapter(this, new ChoiceOnClickListener());
        listChoiceRecycleView.setAdapter(listChoiceAdapter);

        nextQuestionButton = findViewById(R.id.next_question_button);

        // Set on click listener
        nextQuestionButton.setOnClickListener(new NextQuestionOnClickListener());

        // Load data and fill it to screen
        loadData();
    }

    private void loadData() {
        ApolloCall<SetUserLearningSetMutation.Data> mutationCall = apolloClient.mutation(new SetUserLearningSetMutation(setId));
        ApolloCall<SetQuery.Data> queryCall = apolloClient.query(new SetQuery(setId));

        // https://levelup.gitconnected.com/easily-handle-advanced-requests-on-android-with-graphql-and-rxjava-dca2cc0cecee
        Rx3Apollo.single(mutationCall)
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap((dataApolloResponse -> Rx3Apollo.single(queryCall).observeOn(AndroidSchedulers.mainThread())))
                .subscribe(
                        response -> {
                            if (response.data != null && response.data.set != null && !response.hasErrors()) {
                                set = response.data.set;
                                generateLearningTermPool();
                                pickRandomTerm();
                                loadCurrentTermToScreen();
                            }

                            if (learningTermsPool.size() == 0) {
                                Intent intent = new Intent(LearningActivity.this, CongratulationActivity.class);
                                startActivity(intent);
                            }
                        },
                        Throwable::printStackTrace
                );
    }

    private void reportUserLearningTerm(TermReport termReport) {
        List<TermReport> listTermReport = new ArrayList<>();
        listTermReport.add(termReport);

        ApolloCall<ReportUserLearningTermsMutation.Data> mutationCall = apolloClient.mutation(new ReportUserLearningTermsMutation(listTermReport));
        Rx3Apollo.single(mutationCall)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.data != null && response.data.setUserLearningTerms != null && !response.hasErrors()) {
                                // Update learning pool
                                for (int i = 0; i < learningTermsPool.size(); i++) {
                                    for (int j = 0; j < response.data.setUserLearningTerms.size(); j++) {
                                        if (learningTermsPool.get(i).id.equals(response.data.setUserLearningTerms.get(j).id)) {
                                            if (response.data.setUserLearningTerms.get(j).remained != null && response.data.setUserLearningTerms.get(j).remained != 0) {
                                                learningTermsPool.get(i).remained = response.data.setUserLearningTerms.get(j).remained;
                                            } else {
                                                learningTermsPool.remove(i);
                                            }
                                        }
                                    }
                                }
                            }

                            if (learningTermsPool.size() == 0) {
                                Intent intent = new Intent(LearningActivity.this, CongratulationActivity.class);
                                startActivity(intent);
                            }
                        },
                        Throwable::printStackTrace
                );
    }

    private void generateLearningTermPool() {
        for (int i = 0; i < set.terms.size(); i++) {
            SetQuery.Term term = set.terms.get(i);

            if (term.remained != null && term.remained > 0) {
                learningTermsPool.add(term);
            }

            answersPool.add(term.answer);
        }
    }

    private void pickRandomTerm() {
        if (learningTermsPool.size() > 0) {
            Random rand = new Random();
            currentLearningTermIndex = rand.nextInt(learningTermsPool.size());

            SetQuery.Term currentLearningTerm = learningTermsPool.get(currentLearningTermIndex);
            currentQuiz = new Quiz(currentLearningTerm.question, currentLearningTerm.answer, currentLearningTerm.options, answersPool);
        }
    }

    private void loadCurrentTermToScreen() {
        if (currentLearningTermIndex < learningTermsPool.size()) {
            SetQuery.Term currentLearningTerm = learningTermsPool.get(currentLearningTermIndex);

            String countTermRemainingText = String.format(Locale.US, "%d %s", learningTermsPool.size(), getString(R.string.terms_remaining));
            countTermRemainingTextView.setText(countTermRemainingText);
            questionTextView.setText(currentLearningTerm.question);

            if (currentLearningTerm.explanation != null) {
                explanationTextView.setText(currentLearningTerm.explanation);
            }

            listChoiceAdapter.loadQuiz(currentQuiz);
        }
    }

    class ChoiceOnClickListener implements ChoiceListener {
        @Override
        public void onSetPreviewClick(int index, String choice) {
            // If correct
            boolean correct = index == currentQuiz.getCorrectChoiceIndex();

            // Handling UI
            resultTextView.setVisibility(View.VISIBLE);
            if (correct) {
                resultTextView.setText(R.string.correct);
                resultTextView.setTextColor(Color.GREEN);

            } else {
                resultTextView.setText(R.string.incorrect);
                resultTextView.setTextColor(Color.RED);
            }

            SetQuery.Term currentLearningTerm = learningTermsPool.get(currentLearningTermIndex);
            if (currentLearningTerm.explanation != null) {
                explanationTextView.setVisibility(View.VISIBLE);
                explanationTextView.setText(currentLearningTerm.explanation);
            }

            nextQuestionButton.setVisibility(View.VISIBLE);

            // Report user's learning progress
            TermReport termReport = new TermReport(currentLearningTerm.id, correct);
            reportUserLearningTerm(termReport);
        }
    }

    class NextQuestionOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            pickRandomTerm();
            loadCurrentTermToScreen();

            resultTextView.setVisibility(View.INVISIBLE);
            explanationTextView.setVisibility(View.INVISIBLE);
            nextQuestionButton.setVisibility(View.INVISIBLE);
        }
    }
}