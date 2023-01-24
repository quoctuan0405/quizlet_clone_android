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
import com.example.quizapp.CreateSetMutation;
import com.example.quizapp.SetQuery;
import com.example.quizapp.type.CreateSetInput;
import com.example.quizapp.type.CreateTermInput;
import com.example.quizapp.type.UpsertOption;
import com.example.quizletclone.adapter.ListEditTermAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class CreateSetActivity extends AppCompatActivity {
    ApolloClient apolloClient;
    EditText setNameInput;
    Button addTermButton, saveButton;
    RecyclerView editListTermRecyclerView;
    SetQuery.Set set;
    ListEditTermAdapter listEditTermAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_set);

        // Apollo client
        apolloClient = ApolloClientManager.getApolloClient(this, getString(R.string.base_url));

        setNameInput = findViewById(R.id.set_name_input);
        addTermButton = findViewById(R.id.add_term_button);
        saveButton = findViewById(R.id.save_button);

        // Create new empty set
        set = new SetQuery.Set("", "Set name", new SetQuery.Author("", ""), new ArrayList<>());

        // Recycler view
        editListTermRecyclerView = findViewById(R.id.edit_list_term);
        editListTermRecyclerView.setHasFixedSize(true);
        editListTermRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        listEditTermAdapter = new ListEditTermAdapter(this, set);
        editListTermRecyclerView.setAdapter(listEditTermAdapter);
        addSwipeToDelete(editListTermRecyclerView);


        // Click event listener
        addTermButton.setOnClickListener(new CreateSetActivity.AddTermButtonListener());
        saveButton.setOnClickListener(new CreateSetActivity.SaveButtonListener());
    }

    private void addSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Toast.makeText(CreateSetActivity.this, getString(R.string.term_deleted), Toast.LENGTH_SHORT).show();

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
            List<CreateTermInput> listCreateTermInput = new ArrayList<>();
            for (int i = 0; i < set.terms.size(); i++) {
                SetQuery.Term term = set.terms.get(i);

                List<UpsertOption> listUpsertOption = new ArrayList<>();
                for (int j = 0; j < term.options.size(); j++) {
                    Optional<String> optionId = Optional.present(term.options.get(j).id);
                    UpsertOption option = new UpsertOption(optionId, term.options.get(j).option);

                    listUpsertOption.add(option);
                }

                Optional<String> explanation = Optional.present(term.explanation);
                Optional<List<UpsertOption>> listOptionalUpsertOption = Optional.present(listUpsertOption);

                CreateTermInput updateTermInput = new CreateTermInput(term.question, term.answer, listOptionalUpsertOption, explanation);
                listCreateTermInput.add(updateTermInput);
            }

            CreateSetInput createSetInput = new CreateSetInput(setNameInput.getText().toString(), listCreateTermInput);

            ApolloCall<CreateSetMutation.Data> mutationCall = apolloClient.mutation(new CreateSetMutation(createSetInput));
            Single<ApolloResponse<CreateSetMutation.Data>> mutationResponse = Rx3Apollo.single(mutationCall);

            mutationResponse
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                if (response.data != null && !response.hasErrors()) {
                                    Intent intent = new Intent(CreateSetActivity.this, SetDetailActivity.class)
                                            .putExtra("setId", response.data.createSet.id);
                                    startActivity(intent);
                                }
                            },
                            Throwable::printStackTrace
                    );
        }
    }
}