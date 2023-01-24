package com.example.quizletclone.listener;

import com.example.quizapp.SetQuery;

public interface TermEditListener {
    void onTermChange(int index, SetQuery.Term term);
}
