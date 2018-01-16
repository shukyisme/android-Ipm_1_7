package me.kwik.data;

import java.util.List;

/**
 * Created by Farid Abu Salih on 15/01/18.
 * farid@kwik.me
 */

public class TroubleshootingItem {
    private String question;
    List<PossibleCase> cases;


    private class PossibleCase {
        private String posibleCaseText;
        private String whatToDo;
    }
}
