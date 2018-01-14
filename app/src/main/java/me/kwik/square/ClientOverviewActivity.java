package me.kwik.square;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.listeners.GetKwikDevicesListener;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;

public class ClientOverviewActivity extends BaseActivity {

    @BindView(R.id.client_overview_activity_traps_ListView)
    ListView mTrapsList;

    @BindView(R.id.client_overview_activity_add_new_trap_LinearLayout)
    LinearLayout mAddNewTrapLinearLayout;

//    @BindView(R.id.fab)
//    ImageButton mFab;

    @BindView(R.id.client_overview_activity_client_details_three_dots_TextView)
    TextView mClientDetailsThreeDotsTextView;

    private Application             mApp;
    private String                  mClientName;
    private TrapsArrayAdapter       mTrapsAdapter;
    private String                  mClientId;
    private View.OnClickListener    mOnAddNewTrapClick;
    private String                  TAG = this.getClass().getSimpleName();
    private List<KwikDevice>        mTraps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_overview);
        ButterKnife.bind(this);

        mApp = (Application)getApplication();

        mClientId = getIntent().getStringExtra("client");
        mClientName = mApp.getClient(mClientId).getName();

        mActionBarTitle.setText(mClientName);



        mOnAddNewTrapClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( ClientOverviewActivity.this, AddNewTrapActivity.class );
                i.putExtra( "sender", ClientOverviewActivity.class.getSimpleName() );
                i.putExtra("name", mApp.getUser().getFirstName());
                i.putExtra("client",mClientId);
                startActivity( i );
            }
        };

        mAddNewTrapLinearLayout.setOnClickListener( mOnAddNewTrapClick);

        mClientDetailsThreeDotsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ClientOverviewActivity.this,ClientDetailsActivity.class);
                i.putExtra("client",mClientId);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
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
            TextView siteName = (TextView)rowView.findViewById(R.id.trap_item_site_name_TextView);
            TextView trapSerialNumber = (TextView)rowView.findViewById(R.id.trap_item_sn_TextView);
            ImageView trapImageView = (ImageView)rowView.findViewById(R.id.trap_item_image_ImageView);
            ImageView lowBatteryImageView = (ImageView)rowView.findViewById(R.id.trap_item_low_battery_ImageView);
            trapName.setText(values.get(position).getName());
            siteName.setText(values.get(position).getSiteName());

            try {
                trapSerialNumber.setText(values.get(position).getId());
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            try {
                if (!values.get(position).getStatus().equals("alert")) {
                    trapImageView.setImageDrawable(getResources().getDrawable(R.drawable.ipm_button_ready_icon));
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            if(values.get(position).getBatteryStatus() != null && values.get(position).getBatteryStatus().equals("Good")){
                lowBatteryImageView.setVisibility(View.INVISIBLE);
            }else{
                lowBatteryImageView.setVisibility(View.VISIBLE);
            }

//            if(values.get(position).getBattery() > 5){
//                lowBatteryImageView.setVisibility(View.INVISIBLE);
//            }else{
//                lowBatteryImageView.setVisibility(View.VISIBLE);
//            }


            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ClientOverviewActivity.this,TrapDetailsActivity.class);
                    i.putExtra("serial_number",values.get(position).getId());
                    startActivity(i);
                }
            });
            return rowView;
        }

        @Override
        public int getCount() {
            return  this.values.size();
        }
    }

    private void updateList() {
        showProgressBar();
        KwikMe.getKwikDevices(null,null, null, new GetKwikDevicesListener() {
            @Override
            public void getKwikDevicesListenerDone(List<KwikDevice> buttons) {
                hideProgressBar();
                mTraps = buttons;
                if (buttons == null || buttons.size() == 0) {
                    Utils.playAudioFile( ClientOverviewActivity.this, "add_first_button", 0, 5 );
                    return;
                }
                mTrapsAdapter = new TrapsArrayAdapter(ClientOverviewActivity.this,mTraps );
                mTrapsList.setAdapter(mTrapsAdapter);
            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog("",error.getMessage());
            }
        });
    }
}
