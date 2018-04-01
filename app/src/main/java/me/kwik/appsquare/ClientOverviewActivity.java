package me.kwik.appsquare;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.listeners.GetKwikDevicesListener;
import me.kwik.rest.responses.GetKwikDevicesResponse;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;

public class ClientOverviewActivity extends BaseActivity {

    @BindView(R.id.client_overview_activity_traps_ListView)
    ListView mTrapsList;

    @BindView(R.id.client_overview_activity_add_new_trap_LinearLayout)
    LinearLayout mAddNewTrapLinearLayout;

    @BindView(R.id.client_overview_activity_client_details_three_dots_TextView)
    TextView mClientDetailsThreeDotsTextView;

    @BindView(R.id.client_overview_activity_total_traps_TextView)
    TextView mTotalTrapsTextView;

    @BindView(R.id.client_overview_activity_trap_alerts_TextView)
    TextView mTotalAlertTrapsTextView;


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
        mPrevTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ClientOverviewActivity.this,ClientsActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateTrapAlertsValue() {
            KwikMe.getKwikDevices(null, mClientId, null, KwikDevice.STATUS_ALERT,"status",1, new GetKwikDevicesListener() {
                @Override
                public void getKwikDevicesListenerDone(GetKwikDevicesResponse response) {
                    hideProgressBar();
                    int totalTraps = 0;
                    try {
                        totalTraps = response.getPaging().getTotal();
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    mTotalAlertTrapsTextView.setText(totalTraps + " Trap Alerts");
                }

                @Override
                public void getKwikDevicesListenerError(KwikServerError error) {
                    hideProgressBar();
                    showErrorDialog(error);
                }
            });
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
                trapSerialNumber.setText("SN: " + values.get(position).getId());
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            try {
                Drawable dr = null;
                if (values.get(position).getStatus().equals(KwikDevice.STATUS_ALERT)) {
                     dr = ContextCompat.getDrawable(ClientOverviewActivity.this, R.drawable.kwik_orange);
                }else if(values.get(position).getStatus().equals(KwikDevice.STATUS_READY)) {
                    dr = ContextCompat.getDrawable(ClientOverviewActivity.this, R.drawable.ipm_button_ready_icon);
                }else if(values.get(position).getStatus().equals(KwikDevice.STATUS_AVAILABLE)) {
                    dr = ContextCompat.getDrawable(ClientOverviewActivity.this, R.drawable.ipm_button_ready_icon);
                }else if(values.get(position).getStatus().equals(KwikDevice.STATUS_NOT_AVAILABLE)) {
                    dr = ContextCompat.getDrawable(ClientOverviewActivity.this, R.drawable.grey_button);
                }else if(values.get(position).getStatus().equals(KwikDevice.STATUS_DISABLED)) {
                    dr = ContextCompat.getDrawable(ClientOverviewActivity.this, R.drawable.grey_button);
                }

                if(dr != null){
                    trapImageView.setImageDrawable(dr);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            if(values.get(position).getBatteryStatus() != null && values.get(position).getBatteryStatus().equalsIgnoreCase("Replace")){
                lowBatteryImageView.setVisibility(View.VISIBLE);
            }else{
                lowBatteryImageView.setVisibility(View.INVISIBLE);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ClientOverviewActivity.this,TrapDetailsActivity.class);
                    i.putExtra("serial_number",values.get(position).getId());
                    i.putExtra("client",mClientId);
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
        KwikMe.getKwikDevices(null,mClientId, null,null,"status",1, new GetKwikDevicesListener() {
            @Override
            public void getKwikDevicesListenerDone(GetKwikDevicesResponse response) {
                hideProgressBar();
                mTraps = response.getButtons();
                if (mTraps == null ) {
                    mTraps = new ArrayList<KwikDevice>();
                }
                int totalTraps = 0;
                try {
                    totalTraps = response.getPaging().getTotal();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                mTotalTrapsTextView.setText("Total Traps: (" + totalTraps + ")");
                mTrapsAdapter = new TrapsArrayAdapter(ClientOverviewActivity.this, mTraps);
                mTrapsList.setAdapter(mTrapsAdapter);

                updateTrapAlertsValue();
            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                showErrorDialog(error);
            }
        });
    }
}
