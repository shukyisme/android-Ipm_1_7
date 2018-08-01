package me.kwik.appsquare;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.IpmClient;
import me.kwik.listeners.GetClientsListener;
import me.kwik.listeners.GetKwikDevicesListener;
import me.kwik.rest.responses.GetClientsResponse;
import me.kwik.rest.responses.GetKwikDevicesResponse;
import me.kwk.utils.CustomAdapter;
import me.kwk.utils.CustomArrayAdapterItem;

public class TrapsActivity extends BaseActivity {

    public static final int DISPLAY_TRAPS_ALERTS = 1;
    public static final int DISPLAY_TRAPS_NOT_READY = 2;

    @BindView(R.id.traps_activity_traps_ListView)
    ListView mTrapsList;

    @BindView(R.id.traps_client_AutoCompleteTextView)
    AutoCompleteTextView mClientNameAutoCompleteTextView;


    private String                  TAG = this.getClass().getSimpleName();
    private Application             mApp;
    private String                  mClientId;
    private List<CustomArrayAdapterItem> mClients;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traps);
        ButterKnife.bind(this);

        mApp = (Application)getApplication();
        mClientId = getIntent().getStringExtra("client");

        mType = getIntent().getIntExtra("type", DISPLAY_TRAPS_ALERTS);

        if(mType == DISPLAY_TRAPS_ALERTS)
            mActionBarTitle.setText(R.string.traps_alert_title);
        else
            mActionBarTitle.setText(R.string.traps_not_ready_title);

        mClientNameAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((AutoCompleteTextView) v).showDropDown();
                }catch (NullPointerException e){
                }
            }
        });

        mClientNameAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomArrayAdapterItem item = (CustomArrayAdapterItem)mClientNameAutoCompleteTextView.getAdapter().getItem(position);
                mClientId = item.getId();
                mClientNameAutoCompleteTextView.setText(item.getLabel());
                updateTrapList();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mClientNameAutoCompleteTextView.getWindowToken(), 0);
            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        updateClientList();
        //updateTrapList();
    }

    private void updateClientList() {
        showProgressBar();

        String status;
        if(mType == DISPLAY_TRAPS_ALERTS) {
            status = KwikDevice.STATUS_ALERT;
        } else {
            status = KwikDevice.STATUS_NOT_AVAILABLE;
        }


        KwikMe.getClients(null, null, null, "name", 1,false, new GetClientsListener() {
            @Override
            public void getClientsDone(GetClientsResponse res) {
                mClients = new ArrayList<>();
                CustomArrayAdapterItem item = new CustomArrayAdapterItem(null, getString(R.string.traps_filter_all_clients));
                mClients.add(item);
                CustomArrayAdapterItem selectedItem = item;
                if(res != null && res.getClients() != null) {
                    for (IpmClient ipmClient : res.getClients()) {
                        item = new CustomArrayAdapterItem(ipmClient.getId(), ipmClient.getName());
                        mClients.add(item);
                        if (mClientId != null && mClientId.equals(ipmClient.getId())) {
                            selectedItem = item;
                        }
                    }
                }

                ArrayAdapter<CustomArrayAdapterItem> clientsAdapter = new ArrayAdapter<>(TrapsActivity.this, android.R.layout.simple_dropdown_item_1line, mClients);
                mClientNameAutoCompleteTextView.setAdapter(clientsAdapter);
                hideProgressBar();

                mClientNameAutoCompleteTextView.setText(selectedItem.getLabel());
                mClientNameAutoCompleteTextView.dismissDropDown();

                updateTrapList();
            }

            @Override
            public void getClientsError(KwikServerError error) {
                hideProgressBar();
                showErrorDialog(error);
            }
        });

    }

    private void updateTrapList() {
        showProgressBar();
        String status;
        if(mType == DISPLAY_TRAPS_ALERTS) {
            status = KwikDevice.STATUS_ALERT;
        } else {
            status = KwikDevice.STATUS_NOT_AVAILABLE;
        }
        KwikMe.getKwikDevices(null,mClientId, null, status,"status",1, new GetKwikDevicesListener() {
            @Override
            public void getKwikDevicesListenerDone(GetKwikDevicesResponse response) {
                hideProgressBar();
                List<KwikDevice>  mTraps = response.getButtons();
                if (mTraps == null ) {
                    mTraps = new ArrayList<KwikDevice>();
                }

                CustomAdapter mTrapsAdapter = new CustomAdapter(TrapsActivity.this, mTraps, mClients);
                mTrapsList.setAdapter(mTrapsAdapter);

            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                showErrorDialog(error);
            }
        });
    }


}
