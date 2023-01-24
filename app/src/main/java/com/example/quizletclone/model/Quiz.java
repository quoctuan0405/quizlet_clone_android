package com.example.quizletclone.model;

import com.example.quizapp.SetQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quiz {
    String question;
    List<String> choices = new ArrayList<>();
    int correctChoiceIndex;

    public Quiz(String question, String answer, List<SetQuery.Option> options, List<String> answers) {
        this.question = question;

        if (options.size() > 0) {
            for (int i = 0; i < options.size(); i++) {
                choices.add(options.get(i).option);
            }

        } else {
            Collections.shuffle(answers);

            int choiceCount = 0;
            for (int i = 0; i < answers.size(); i++) {
                if (!answer.equals(answers.get(i))) {
                    choices.add(answers.get(i));
                    choiceCount++;
                }

                if (choiceCount > 2) {
                    break;
                }
            }
        }

        choices.add(answer);
        Collections.shuffle(choices);

        correctChoiceIndex = choices.indexOf(answer);
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public int getCorrectChoiceIndex() {
        return correctChoiceIndex;
    }

    public void setCorrectChoiceIndex(int correctChoiceIndex) {
        this.correctChoiceIndex = correctChoiceIndex;
    }
}
