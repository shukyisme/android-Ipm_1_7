package me.kwik.appsquare;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.IpmClient;
import me.kwik.data.IpmClientSite;
import me.kwik.listeners.GetClientsListener;
import me.kwik.listeners.GetKwikDevicesListener;
import me.kwik.rest.responses.GetClientsResponse;
import me.kwik.rest.responses.GetKwikDevicesResponse;
import me.kwik.utils.Logger;
import me.kwk.utils.CustomAdapter;
import me.kwk.utils.CustomArrayAdapterItem;

public class TrapsActivity extends BaseActivity {

    @BindView(R.id.traps_activity_traps_ListView)
    ListView mTrapsList;

    @BindView(R.id.traps_client_AutoCompleteTextView)
    AutoCompleteTextView mClientNameAutoCompleteTextView;


    private String                  TAG = this.getClass().getSimpleName();
    private Application             mApp;
    private String                  mClientId;
    private List<CustomArrayAdapterItem> mClients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traps);
        ButterKnife.bind(this);

        mApp = (Application)getApplication();
        mClientId = getIntent().getStringExtra("client");

        mActionBarTitle.setText(R.string.traps_alert_title);

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
        KwikMe.getClients(null, new GetClientsListener() {
            @Override
            public void getClientsDone(GetClientsResponse res) {
                mClients = new ArrayList<>();
                CustomArrayAdapterItem item = new CustomArrayAdapterItem(null, getString(R.string.traps_filter_all_clients));
                mClients.add(item);
                CustomArrayAdapterItem selectedItem = item;
                for (IpmClient ipmClient : res.getClients()) {
                    item = new CustomArrayAdapterItem(ipmClient.getId(), ipmClient.getName());
                    mClients.add(item);
                    if(mClientId != null && mClientId.equals(ipmClient.getId())) {
                        selectedItem = item;
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
        KwikMe.getKwikDevices(null,mClientId, null,"alert","status",1, new GetKwikDevicesListener() {
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
