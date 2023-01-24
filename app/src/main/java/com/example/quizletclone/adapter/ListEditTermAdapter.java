package com.example.quizletclone.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.SetQuery;
import com.example.quizletclone.R;

import java.util.ArrayList;
import java.util.Locale;

public class ListEditTermAdapter extends RecyclerView.Adapter<EditTermHolder> {
    private final Context context;
    private SetQuery.Set set;

    public ListEditTermAdapter(Context context) {
        this.context = context;
    }

    public ListEditTermAdapter(Context context, SetQuery.Set set) {
        this.context = context;
        this.set = set;
    }

    @NonNull
    @Override
    public EditTermHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EditTermHolder(LayoutInflater.from(context).inflate(R.layout.edit_term_detail, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EditTermHolder holder, int position) {
        if (set != null && set.terms != null && set.terms.size() != 0) {
            SetQuery.Term term = set.terms.get(position);

            String termTitleText = String.format(Locale.US, "%s %d", context.getString(R.string.term), position + 1);
            holder.termTitle.setText(termTitleText);
            holder.questionInput.setText(term.question);
            holder.answerInput.setText(term.answer);
            holder.explanationInput.setText(term.explanation);

            holder.questionInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    term.question = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            holder.answerInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    term.answer = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            holder.explanationInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    term.explanation = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (set != null && set.terms != null) {
            return set.terms.size();
        } else {
            return 0;
        }
    }

    public void loadSet(SetQuery.Set set) {
        this.set = set;
        notifyDataSetChanged();
    }

    public void addEmptyTerm() {
        SetQuery.Term term = new SetQuery.Term("", "", "", "", new ArrayList<>(), 0, false);

        if (set.terms == null) {
            set.terms = new ArrayList<>();
        }
        set.terms.add(term);
        notifyDataSetChanged();
    }
}

class EditTermHolder extends RecyclerView.ViewHolder {
    TextView termTitle;
    EditText questionInput, answerInput, explanationInput;

    public EditTermHolder(@NonNull View itemView) {
        super(itemView);

        termTitle = itemView.findViewById(R.id.term_title);
        questionInput = itemView.findViewById(R.id.question_edit_text);
        answerInput = itemView.findViewById(R.id.answer_edit_text);
        explanationInput = itemView.findViewById(R.id.explanation_edit_text);
    }
}
