package me.kwik.appsquare;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikMe;
import me.kwik.data.Troubleshooting;
import me.kwik.data.TroubleshootingItem;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;

public class TroubleshootingActivity extends BaseActivity {


    @BindView(R.id.troubleshooting_activity_questions_ListView)
    ListView mQuestionsListView;

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_troubleshooting);
        mActionBarTitle.setText("Troubleshooting");
        ButterKnife.bind(this);

        Troubleshooting t;
        String response = Utils.loadJSONFromAsset(this, "troubleshooting_questions");

        if(KwikMe.LOCAL.equals("cs")){
            response = Utils.loadJSONFromAsset(this, "troubleshooting_questions_cs");
        }

        t = new Gson().fromJson(response,Troubleshooting.class);
        Logger.e(TAG,t.toString());
        mQuestionsListView.setAdapter(new QuestionsArrayAdapter(this,t.getQuestions()));

    }

    public class QuestionsArrayAdapter extends ArrayAdapter<TroubleshootingItem> {
        private final Context context;
        private List<TroubleshootingItem> values;

        public QuestionsArrayAdapter(Context context, List<TroubleshootingItem> values) {
            super( context, -1, values );
            this.context = context;
            this.values = values;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService( Context.LAYOUT_INFLATER_SERVICE );

            final View rowView = inflater.inflate( R.layout.troubleshooting_list_item, parent, false );
            TextView question = (TextView)rowView.findViewById(R.id.troubleshooting_list_item_question_TextView);
            question.setText(values.get(position).getQuestion());
            final ImageButton blueRightArrow = (ImageButton)rowView.findViewById(R.id.troubleshooting_list_item_arrow_ImageButton);
            LinearLayout casesLayout = (LinearLayout)rowView.findViewById(R.id.troubleshooting_list_item_possibles_LinearLayout);
            Character a = 0x41;
            for (TroubleshootingItem.PossibleCase c:values.get(position).getPosible_cases()){

                TextView possibleCase = new TextView(TroubleshootingActivity.this);
                possibleCase.setText(a + ". Possible cause");
                a++;

                possibleCase.setId(5);
                possibleCase.setTypeface(null, Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(40,0,40,0);
                possibleCase.setLayoutParams(params);

                possibleCase.setBackground(ContextCompat.getDrawable(TroubleshootingActivity.this,R.drawable.troubleshooting_upper_rounded_corners_bakground));
                casesLayout.addView(possibleCase);

                TextView valueTV = new TextView(TroubleshootingActivity.this);

                valueTV.setText(c.getPosibleCaseText());
                valueTV.setId(5);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params1.setMargins(40,0,40,0);
                valueTV.setLayoutParams(params1);
                valueTV.setBackground(ContextCompat.getDrawable(TroubleshootingActivity.this,R.drawable.troubleshooting_rectangle_background));
                casesLayout.addView(valueTV);

                String whatToDo = "";

                TextView whatToDoTextView = new TextView(TroubleshootingActivity.this);
                whatToDoTextView.setText("What to do?");

                whatToDoTextView.setId(5);
                whatToDoTextView.setTypeface(null, Typeface.BOLD);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params2.setMargins(40,0,40,0);
                whatToDoTextView.setLayoutParams(params2);
                whatToDoTextView.setBackground(ContextCompat.getDrawable(TroubleshootingActivity.this,R.drawable.troubleshooting_rectangle_background));
                casesLayout.addView(whatToDoTextView);


                for(String w:c.getWhatToDo()){
                    whatToDo += w + "\n\n";
                }

                TextView whatToDoList = new TextView(TroubleshootingActivity.this);

                whatToDoList.setText(whatToDo);
                whatToDoList.setId(5);
                LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params3.setMargins(40,0,40,0);
                whatToDoList.setLayoutParams(params3);
                whatToDoList.setBackground(ContextCompat.getDrawable(TroubleshootingActivity.this,R.drawable.troubleshooting_bottom_rounded_corners_background));
                casesLayout.addView(whatToDoList);

                View spaceView = new View(TroubleshootingActivity.this);
                spaceView.setId(5);
                spaceView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        40));
                casesLayout.addView(spaceView);
            }

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout possibleCausesLinearLayout = (LinearLayout)rowView.findViewById(R.id.troubleshooting_list_item_possibles_LinearLayout);
                    if(possibleCausesLinearLayout.getVisibility() == View.VISIBLE){
                        ObjectAnimator anim = ObjectAnimator.ofFloat( blueRightArrow, "rotation", 90, 0 );
                        anim.setDuration( 100 );
                        anim.start();
                        possibleCausesLinearLayout.setVisibility(View.GONE);
                    }else{
                        ObjectAnimator anim = ObjectAnimator.ofFloat( blueRightArrow, "rotation", 0,  +90);
                        anim.setDuration( 100 );
                        anim.start();
                        possibleCausesLinearLayout.setVisibility(View.VISIBLE);
                    }
                }
            };


            blueRightArrow.setOnClickListener(listener);
            question.setOnClickListener(listener);

            return rowView;
        }

        @Override
        public int getCount() {
            return  values.size();
        }

    }




}
