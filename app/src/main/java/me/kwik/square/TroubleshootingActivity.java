package me.kwik.square;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.data.TroubleshootingItem;

public class TroubleshootingActivity extends BaseActivity {


    @BindView(R.id.troubleshooting_activity_questions_ListView)
    ListView mQuestionsListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_troubleshooting);
        mActionBarTitle.setText("Troubleshooting");
        ButterKnife.bind(this);

       // mQuestionsListView.setAdapter(new QuestionsArrayAdapter(this,null));

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
            final ImageButton blueRightArrow = (ImageButton)rowView.findViewById(R.id.troubleshooting_list_item_arrow_ImageButton);
            blueRightArrow.setOnClickListener(new View.OnClickListener() {
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
            });

            return rowView;
        }

        @Override
        public int getCount() {
            return  5;
        }

    }


}
