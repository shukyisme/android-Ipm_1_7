package me.kwik.square;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.utils.Logger;

public class ClientOverviewActivity extends BaseActivity {

    @BindView(R.id.client_overview_activity_traps_ListView)
    ListView mTrapsList;

    private String TAG = this.getClass().getSimpleName();
    private TrapsArrayAdapter mTrapsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_overview);
        ButterKnife.bind(this);

        mActionBarTitle.setText(getIntent().getStringExtra("client"));

        mTrapsAdapter = new TrapsArrayAdapter(ClientOverviewActivity.this,null );

        // Assign adapter to ListView
        mTrapsList.setAdapter(mTrapsAdapter);
    }

    public class TrapsArrayAdapter extends ArrayAdapter<KwikDevice> {
        private final Context context;
        private List<KwikDevice> values;

        public TrapsArrayAdapter(Context context, List<KwikDevice> values) {
            super( context, -1, values );
            this.context = context;
            this.values = values;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Logger.e(TAG,this.getClass().getSimpleName() + " "  + new Object(){}.getClass().getEnclosingMethod().getName());
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            final View rowView = inflater.inflate( R.layout.traps_list_item, parent, false );
            TextView trapName = (TextView)rowView.findViewById(R.id.trap_item_name_TextView);
            ImageView trapImageView = (ImageView)rowView.findViewById(R.id.trap_item_image_ImageView);
            ImageView lowBatteryImageView = (ImageView)rowView.findViewById(R.id.trap_item_low_battery_ImageView);
            if(position == 2){
                trapImageView.setImageDrawable(getResources().getDrawable(R.drawable.ipm_button_ready_icon));
                lowBatteryImageView.setVisibility(View.INVISIBLE);
            }

            if(position == 1){
                lowBatteryImageView.setVisibility(View.INVISIBLE);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ClientOverviewActivity.this,TrapDetailsActivity.class);
                    startActivity(i);
                }
            });
            return rowView;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
