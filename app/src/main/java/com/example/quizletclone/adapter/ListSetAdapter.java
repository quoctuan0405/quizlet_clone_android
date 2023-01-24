package com.example.quizletclone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.SetsQuery;
import com.example.quizletclone.R;
import com.example.quizletclone.listener.SetPreviewListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListSetAdapter extends RecyclerView.Adapter<SetPreviewHolder> {
    private final Context context;
    private List<SetsQuery.Set> sets = new ArrayList<>();
    private SetPreviewListener listener;

    public ListSetAdapter(Context context, SetPreviewListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public ListSetAdapter(Context context, List<SetsQuery.Set> sets, SetPreviewListener listener) {
        this.context = context;
        this.sets = sets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SetPreviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SetPreviewHolder(LayoutInflater.from(context).inflate(R.layout.set_preview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SetPreviewHolder holder, int position) {
        SetsQuery.Set set = sets.get(position);

        holder.setNameTextView.setText(set.name);
        holder.authorTextView.setText(set.author.username);

        String termsCount = String.format(Locale.US, "%d %s", set._count.terms, context.getString(R.string.terms));
        holder.termsCountTextView.setText(termsCount);

        holder.setPreviewCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSetPreviewClick(set);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    public void loadSets(List<SetsQuery.Set> sets) {
        this.sets = sets;
        notifyDataSetChanged();
    }
}

class SetPreviewHolder extends RecyclerView.ViewHolder {
    TextView setNameTextView, authorTextView, termsCountTextView;
    CardView setPreviewCardView;

    public SetPreviewHolder(@NonNull View itemView) {
        super(itemView);

        setNameTextView = itemView.findViewById(R.id.set_name_input);
        authorTextView = itemView.findViewById(R.id.author);
        termsCountTextView = itemView.findViewById(R.id.terms_count);
        setPreviewCardView = itemView.findViewById(R.id.set_preview);
    }
}