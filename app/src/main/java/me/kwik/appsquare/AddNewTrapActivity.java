package me.kwik.appsquare;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;


import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.IpmClient;
import me.kwik.data.IpmClientSite;
import me.kwik.listeners.CreateNewClientSiteListener;
import me.kwik.listeners.GetClientsListener;
import me.kwik.rest.responses.GetClientsResponse;

public class AddNewTrapActivity extends BaseActivity {

    private final String TAG = this.getClass().getSimpleName();
    @BindView(R.id.client_name_AutoCompleteTextView)
    AutoCompleteTextView mClientNameAutoCompleteTextView;

    @BindView(R.id.add_new_trap_activity_new_site_AutoCompleteTextView)
    AutoCompleteTextView mSiteAutoCompleteTextView;

    @BindView(R.id.add_new_trap_activity_new_site_TextView)
    TextView mAddNewSiteTextView;

    @BindView(R.id.add_new_trap_activity_trap_name_EditText)
    EditText mTrapNameEditText;

    @BindView(R.id.add_new_trap_activity_trap_description_EditText)
    EditText mTrapDescriptionEditText;




    private IpmClient       mSelectedClient;
    private IpmClientSite   mSelectedSite;
    private String          mTrapNameString;
    private String          mTrapDescriptionString;

    private Application mApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_trap);
        ButterKnife.bind(this);
        mApp = (Application) getApplication();
        mActionBarTitle.setText("Add new trap");

        mClientNameAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mClientNameAutoCompleteTextView.setError(null);
                    ((AutoCompleteTextView) v).showDropDown();
                }catch (NullPointerException e){

                }
            }
        });

        mSiteAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((AutoCompleteTextView)v).showDropDown();
                }catch (NullPointerException e){

                }

            }
        });



        mClientNameAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) {
                    // on focus off
                    String str = mClientNameAutoCompleteTextView.getText().toString();

                    ListAdapter listAdapter = mClientNameAutoCompleteTextView.getAdapter();
                    if(listAdapter != null) {

                        try {
                            for (int i = 0; i < listAdapter.getCount(); i++) {
                                String temp = listAdapter.getItem(i).toString();
                                if (str.compareTo(temp) == 0) {
                                    return;
                                }
                            }
                        }catch (NullPointerException e){

                        }
                    }

                    mClientNameAutoCompleteTextView.setText("");
                    mSiteAutoCompleteTextView.setText("");
                    changeAddNewSiteTextView(false);
                }
            }
        });

        mSiteAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) {
                    // on focus off
                    String str = mSiteAutoCompleteTextView.getText().toString();

                    ListAdapter listAdapter = mSiteAutoCompleteTextView.getAdapter();
                    if(listAdapter != null) {
                        try {


                            for (int i = 0; i < listAdapter.getCount(); i++) {
                                String temp = listAdapter.getItem(i).toString();
                                if (str.compareTo(temp) == 0) {
                                    return;
                                }
                            }
                        }catch (NullPointerException e){

                        }
                    }
                    mSiteAutoCompleteTextView.setText("");
                }else{
                    ((AutoCompleteTextView)view).showDropDown();
                }
            }
        });



        mClientNameAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedClient = (IpmClient)mClientNameAutoCompleteTextView.getAdapter().getItem(position);
                if(mSelectedClient == null){
                    return;
                }

                IpmClientSite[] sitesArray = mSelectedClient.getSites();
                if(sitesArray != null && sitesArray.length >0) {
                    ArrayAdapter<IpmClientSite> clientSitesAdapter = new ArrayAdapter<IpmClientSite>(AddNewTrapActivity.this, android.R.layout.simple_dropdown_item_1line, sitesArray);
                    mSiteAutoCompleteTextView.setAdapter(clientSitesAdapter);
                }
                changeAddNewSiteTextView(true);
            }
        });

        mSiteAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedSite = (IpmClientSite)mSiteAutoCompleteTextView.getAdapter().getItem(position);
            }
        });


        mAddNewSiteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final NewSiteDialog dialog = new NewSiteDialog(AddNewTrapActivity.this);
                dialog.show();
                Window window = dialog.getWindow();
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        });


        mTrapNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTrapNameString = s.toString();
            }
        });

        mTrapDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTrapDescriptionString = s.toString().trim();
            }
        });


    }

    private void changeAddNewSiteTextView(Boolean active){

        mAddNewSiteTextView.setClickable(active);
        if(active){
            mAddNewSiteTextView.setTextColor(ContextCompat.getColor(this,R.color.lapislazuli));
            mAddNewSiteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_client,0,0,0);
        }else{

            mAddNewSiteTextView.setTextColor(ContextCompat.getColor(this,R.color.silver));
            mAddNewSiteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_client1,0,0,0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        showProgressBar();

        KwikMe.getClients(null, new GetClientsListener() {
            @Override
            public void getClientsDone(GetClientsResponse res) {
                ArrayAdapter<IpmClient> clientsAdapter = new ArrayAdapter<IpmClient>(AddNewTrapActivity.this, android.R.layout.simple_dropdown_item_1line, res.getClients());
                mClientNameAutoCompleteTextView.setAdapter(clientsAdapter);
                hideProgressBar();
                String client = getIntent().getStringExtra("client");
                if( client != null){
                    mSelectedClient = mApp.getClient(client);
                    mClientNameAutoCompleteTextView.setText(mSelectedClient.getName());
                    mClientNameAutoCompleteTextView.dismissDropDown();
                    IpmClientSite[] sitesArray = mSelectedClient.getSites();
                    if(sitesArray != null && sitesArray.length >0) {
                        ArrayAdapter<IpmClientSite> clientSitesAdapter = new ArrayAdapter<IpmClientSite>(AddNewTrapActivity.this, android.R.layout.simple_dropdown_item_1line, sitesArray);
                        mSiteAutoCompleteTextView.setAdapter(clientSitesAdapter);
                    }
                    changeAddNewSiteTextView(true);
                }
            }

            @Override
            public void getClientsError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog("",error.getMessage());
            }
        });
    }

    public void onNextClick(View v){
        if(mSelectedClient == null){
            mClientNameAutoCompleteTextView.setError("Select client");
            return;
        }

        if(mTrapNameString == null || mTrapNameString.trim().length() == 0){
            mTrapNameEditText.setError("Trap name is mandatory");
            return;
        }

        KwikDevice button = new KwikDevice();
        button.setClient(mSelectedClient.getId());
        button.setName(mTrapNameString);
        if(mSelectedSite != null){
            button.setSite(mSelectedSite.getId());
        }
        if(mTrapDescriptionString != null){
            button.setDescription(mTrapDescriptionString);
        }



        Intent i = new Intent(AddNewTrapActivity.this,WiFiSelectionActivity.class);
        i.putExtra("button",button);
        startActivity(i);
    }

    public void createNewClientClick(View view) {
        Intent i = new Intent(AddNewTrapActivity.this,CreateClientActivity.class);
        startActivityForResult(i,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("result");
                mSelectedClient = mApp.getClient(result);
                String name=data.getStringExtra("name");
                mClientNameAutoCompleteTextView.setText(name);
                mClientNameAutoCompleteTextView.dismissDropDown();
                changeAddNewSiteTextView(true);
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public void needHelpClick(View view) {
        Intent i = new Intent(AddNewTrapActivity.this,TroubleshootingActivity.class);
        startActivity(i);
    }

    public class NewSiteDialog extends Dialog{

        @BindView(R.id.new_site_pop_up_client_name_TextView)
        TextView mClientTextView;

        @BindView(R.id.new_site_pop_up_add_new_TextView)
        TextView mAddNewTextView;

        @BindView(R.id.new_site_pop_up_cancel_TextView)
        TextView mCancelTextView;

        @BindView(R.id.new_site_pop_up_site_name_EditText)
        EditText mSiteNameEditText;

        private String mSiteNameString;

        public NewSiteDialog(Context context) {
            super(context);
            this.setContentView(R.layout.add_new_site_pop_up);
            ButterKnife.bind(this);

            mClientTextView.setText(mSelectedClient.getName());

            mCancelTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewSiteDialog.this.dismiss();
                }
            });

            mAddNewTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSelectedClient == null || mSelectedClient.getName() == null || mSelectedClient.getName().length() == 0){
                        showOneButtonErrorDialog("","Client name is not valid");
                        return;
                    }
                    if(mSiteNameString == null || mSiteNameString.trim().length() == 0){
                        mSiteNameEditText.setError("Site name is mandatory");
                        return;
                    }
                    showProgressBar();
                    KwikMe.createNewClientSite(mSelectedClient.getId(), mSiteNameString, "Without description", new CreateNewClientSiteListener() {
                    @Override
                    public void createNewClientSiteDone(IpmClientSite site) {

                        mSelectedSite = site;
                        mSiteAutoCompleteTextView.setText(mSelectedSite.getName());
                        KwikMe.getClients(null, new GetClientsListener() {
                            @Override
                            public void getClientsDone(GetClientsResponse res) {
                                hideProgressBar();
                                ArrayAdapter<IpmClient> clientsAdapter = new ArrayAdapter<IpmClient>(AddNewTrapActivity.this, android.R.layout.simple_dropdown_item_1line, res.getClients());
                                mClientNameAutoCompleteTextView.setAdapter(clientsAdapter);
                                hideProgressBar();
                                NewSiteDialog.this.dismiss();
                            }

                            @Override
                            public void getClientsError(KwikServerError error) {
                                hideProgressBar();
                                showOneButtonErrorDialog("",error.getMessage());
                            }
                        });


                    }

                    @Override
                    public void createNewClientSiteError(KwikServerError error) {
                        hideProgressBar();
                        showOneButtonErrorDialog("",error.getMessage());
                    }
                });
                }
            });

            mSiteNameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mSiteNameString = s.toString();
                }
            });
        }
    }
}
