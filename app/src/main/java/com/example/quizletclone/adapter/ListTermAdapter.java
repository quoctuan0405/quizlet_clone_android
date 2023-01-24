package com.example.quizletclone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.SetQuery;
import com.example.quizletclone.R;

import java.util.ArrayList;
import java.util.List;

public class ListTermAdapter extends RecyclerView.Adapter<TermDetailHolder> {
    private final Context context;
    private List<SetQuery.Term> terms = new ArrayList<>();

    public ListTermAdapter(Context context) {
        this.context = context;
    }

    public ListTermAdapter(Context context, List<SetQuery.Term> terms) {
        this.context = context;
        this.terms = terms;
    }

    @NonNull
    @Override
    public TermDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TermDetailHolder(LayoutInflater.from(context).inflate(R.layout.term_detail, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TermDetailHolder holder, int position) {
        SetQuery.Term term = terms.get(position);

        holder.questionTextView.setText(term.question);
        holder.answerTextView.setText(term.answer);
    }

    @Override
    public int getItemCount() {
        return terms.size();
    }

    public void loadTerms(List<SetQuery.Term> terms) {
        this.terms = terms;
        notifyDataSetChanged();
    }
}

class TermDetailHolder extends RecyclerView.ViewHolder {
    TextView questionTextView, answerTextView;

    public TermDetailHolder(@NonNull View itemView) {
        super(itemView);

        questionTextView = itemView.findViewById(R.id.question);
        answerTextView = itemView.findViewById(R.id.answer);
    }
}