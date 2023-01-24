package com.example.quizletclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.apollographql.apollo3.ApolloCall;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.api.Optional;
import com.apollographql.apollo3.rx3.Rx3Apollo;
import com.example.quizapp.SetQuery;
import com.example.quizapp.UpdateSetMutation;
import com.example.quizapp.type.UpdateSetInput;
import com.example.quizapp.type.UpdateTermInput;
import com.example.quizapp.type.UpsertOption;
import com.example.quizletclone.adapter.ListEditTermAdapter;
import com.example.quizletclone.listener.TermEditListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class UpdateSetActivity extends AppCompatActivity {
    ApolloClient apolloClient;
    EditText setNameInput;
    Button addTermButton, saveButton;
    RecyclerView editListTermRecyclerView;
    ListEditTermAdapter listEditTermAdapter;
    String setId;
    SetQuery.Set set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_set);

        // Apollo client
        apolloClient = ApolloClientManager.getApolloClient(this, getString(R.string.base_url));

        // Get set id from previous screen
        setId = getIntent().getStringExtra("setId");

        setNameInput = findViewById(R.id.set_name_input);
        addTermButton = findViewById(R.id.add_term_button);
        saveButton = findViewById(R.id.save_button);

        editListTermRecyclerView = findViewById(R.id.edit_list_term);
        editListTermRecyclerView.setHasFixedSize(true);
        editListTermRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        listEditTermAdapter = new ListEditTermAdapter(this);
        editListTermRecyclerView.setAdapter(listEditTermAdapter);
        addSwipeToDelete(editListTermRecyclerView);

        // Click event listener
        addTermButton.setOnClickListener(new AddTermButtonListener());
        saveButton.setOnClickListener(new SaveButtonListener());

        // Load data
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
        setNameInput.setText(set.name);
        listEditTermAdapter.loadSet(set);
    }

    private void addSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Toast.makeText(UpdateSetActivity.this, getString(R.string.term_deleted), Toast.LENGTH_SHORT).show();

                set.terms.remove(viewHolder.getAdapterPosition());
                listEditTermAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

    class AddTermButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            listEditTermAdapter.addEmptyTerm();
        }
    }

    class SaveButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            List<UpdateTermInput> listUpdateTermInput = new ArrayList<>();
            for (int i = 0; i < set.terms.size(); i++) {
                SetQuery.Term term = set.terms.get(i);

                List<UpsertOption> listUpsertOption = new ArrayList<>();
                for (int j = 0; j < term.options.size(); j++) {
                    Optional<String> optionId = Optional.present(term.options.get(j).id);
                    UpsertOption option = new UpsertOption(optionId, term.options.get(j).option);

                    listUpsertOption.add(option);
                }

                Optional<String> termId = Optional.present(term.id);
                Optional<String> explanation = Optional.present(term.explanation);
                Optional<List<UpsertOption>> listOptionalUpsertOption = Optional.present(listUpsertOption);

                UpdateTermInput updateTermInput = new UpdateTermInput(termId, term.question, term.answer, listOptionalUpsertOption, explanation);
                listUpdateTermInput.add(updateTermInput);
            }

            UpdateSetInput updateSetInput = new UpdateSetInput(set.id, setNameInput.getText().toString(), listUpdateTermInput);

            ApolloCall<UpdateSetMutation.Data> mutationCall = apolloClient.mutation(new UpdateSetMutation(updateSetInput));
            Single<ApolloResponse<UpdateSetMutation.Data>> mutationResponse = Rx3Apollo.single(mutationCall);

            mutationResponse
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                if (response.data != null && !response.hasErrors()) {
                                    Intent intent = new Intent(UpdateSetActivity.this, SetDetailActivity.class)
                                            .putExtra("setId", setId);
                                    startActivity(intent);
                                }
                            },
                            Throwable::printStackTrace
                    );
        }
    }
}