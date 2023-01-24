package com.example.quizletclone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizletclone.R;
import com.example.quizletclone.listener.ChoiceListener;
import com.example.quizletclone.model.Quiz;

import java.util.List;

public class ListChoiceAdapter extends RecyclerView.Adapter<ChoiceHolder> {
    private final Context context;
    private Quiz quiz;
    private ChoiceListener listener;

    public ListChoiceAdapter(Context context, ChoiceListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public ListChoiceAdapter(Context context, Quiz quiz, ChoiceListener listener) {
        this.context = context;
        this.quiz = quiz;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChoiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChoiceHolder(LayoutInflater.from(context).inflate(R.layout.choice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChoiceHolder holder, int position) {
        String choice = quiz.getChoices().get(position);

        if (choice != null) {
            holder.choiceButton.setText(choice);
        }
        holder.choiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSetPreviewClick(holder.getAdapterPosition(), choice);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (quiz != null) {
            return quiz.getChoices().size();
        } else {
            return 0;
        }
    }

    public void loadQuiz(Quiz quiz) {
        this.quiz = quiz;
        notifyDataSetChanged();
    }
}

class ChoiceHolder extends RecyclerView.ViewHolder {
    Button choiceButton;

    public ChoiceHolder(@NonNull View itemView) {
        super(itemView);

        choiceButton = itemView.findViewById(R.id.choice);
    }
}
