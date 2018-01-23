package me.kwik.data;

import java.util.List;

/**
 * Created by Farid Abu Salih on 15/01/18.
 * farid@kwik.me
 */

public class TroubleshootingItem {
    private String question;
    private List<PossibleCase> posible_cases;


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<PossibleCase> getPosible_cases() {
        return posible_cases;
    }

    public void setPosible_cases(List<PossibleCase> posible_cases) {
        this.posible_cases = posible_cases;
    }

    public class PossibleCase {
        private String posible_case;
        private String[] what_to_do;

        public String getPosibleCaseText() {
            return posible_case;
        }

        public void setPosibleCaseText(String posibleCaseText) {
            this.posible_case = posibleCaseText;
        }

        public String[] getWhatToDo() {
            return what_to_do;
        }

        public void setWhatToDo(String[] whatToDo) {
            this.what_to_do = whatToDo;
        }
    }

}
