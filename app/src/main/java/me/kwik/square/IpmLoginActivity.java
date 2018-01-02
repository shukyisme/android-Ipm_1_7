package me.kwik.square;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.Client;
import me.kwik.data.IpmClientsResponse;
import me.kwik.data.IpmStandeAloneClient;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.Site;
import me.kwik.listeners.RegisterUserListener;
import me.kwik.utils.Logger;

public class IpmLoginActivity extends BaseActivity {

    public Application mApp;
    private String mKwikButtonId;
    private HashMap<String,Object> loginParams;
    private AutoCompleteTextView mClientNameAutoCompleteTextView;
    private AutoCompleteTextView mSiteAutoCompleteTextView;
    private AutoCompleteTextView mCoordinatorAutoCompleteTextView;
    private EditText mTrapNameEditText;
    private EditText mTrapDescriptionEditText;
    private List<Client> clients = new ArrayList<>();
    private List<Site> sites = new ArrayList<>();
    private int selectedClientIndex = -1;
    private int selectedSiteIndex = -1;
    private int selectedCoordinatorIndex = -1;
    private static final String[] CLIENT_NAME = new String[] {
    };
    private static final String[] SITE = new String[] {

    };
    private static final String[] COORDINATOR = new String[] {

    };

    private final String url = KwikMe.ipmServerUrl;
    private final String TAG = this.getClass().getSimpleName();

    String[] validWords = new String[]{"", "snowboard", "bobsleigh", "slalom"};

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipm_login);
        mActionBarTitle.setText(R.string.ipm_login_activity_title);

        mClientNameAutoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.client_name_autoCompleteTextView);
        mSiteAutoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.site_autoCompleteTextView);
        mCoordinatorAutoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.coordinator_autoCompleteTextView);
        mTrapNameEditText = (EditText)findViewById(R.id.trap_name_editText);
        mTrapDescriptionEditText = (EditText)findViewById(R.id.trap_description_editText);

        mApp = (Application) getApplication();
        mKwikButtonId = getIntent().getStringExtra("buttonId");

        mClientNameAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AutoCompleteTextView)v).showDropDown();
            }
        });



        if(mApp.getProject(mApp.getButton(mKwikButtonId).getProject()).getLoginUri().contains("ipm=standalone")){
            LinearLayout siteLayout = (LinearLayout)findViewById(R.id.site_layout);
            LinearLayout coordinatorLayout = (LinearLayout)findViewById(R.id.coordinator_layout);

            siteLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.GONE);


            List<IpmStandeAloneClient> clients = null;
            try {
                clients = (mApp.getProject(mApp.getButton(mKwikButtonId).getProject())).getProperties().getClients();
            }catch (NullPointerException e){
                e.printStackTrace();
                return;
            }


            ArrayAdapter<IpmStandeAloneClient> clientsAdapter = new ArrayAdapter<IpmStandeAloneClient>(this, android.R.layout.simple_dropdown_item_1line, clients);
            mClientNameAutoCompleteTextView.setAdapter(clientsAdapter);
            mClientNameAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                        long id) {
                    selectedClientIndex = pos;
                    mTrapNameEditText.requestFocus();

                }
            });

            mClientNameAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(!b) {
                        // on focus off
                        String str = mClientNameAutoCompleteTextView.getText().toString();

                        ListAdapter listAdapter = mClientNameAutoCompleteTextView.getAdapter();
                        for(int i = 0; i < listAdapter.getCount(); i++) {
                            String temp = listAdapter.getItem(i).toString();
                            if(str.compareTo(temp) == 0) {
                                return;
                            }
                        }
                        mClientNameAutoCompleteTextView.setText("");
                        selectedClientIndex = -1;
                    }
                }
            });
            return;
        }

        getIpmClients();

        mSiteAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AutoCompleteTextView)v).showDropDown();
            }
        });

        mClientNameAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                selectedClientIndex = pos;
                sites = clients.get(pos).getSites();
                ArrayAdapter<Site> mSitesAdapter = new ArrayAdapter<Site>(IpmLoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, sites);
                mSiteAutoCompleteTextView.setText("");
                mCoordinatorAutoCompleteTextView.setText("");
                mSiteAutoCompleteTextView.setAdapter(mSitesAdapter);
                mSiteAutoCompleteTextView.requestFocus();

            }
        });

        mSiteAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSiteIndex = position;

                ArrayAdapter<String> mCoordinatorAdapter = new ArrayAdapter<String>(IpmLoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, sites.get(position).getCoordinators());
                mCoordinatorAutoCompleteTextView.setText("");
                mCoordinatorAutoCompleteTextView.setAdapter(mCoordinatorAdapter);
                mCoordinatorAutoCompleteTextView.requestFocus();

            }
        });

        mCoordinatorAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AutoCompleteTextView)v).showDropDown();
            }
        });

        mCoordinatorAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCoordinatorIndex = position;
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

                        for (int i = 0; i < listAdapter.getCount(); i++) {
                            String temp = listAdapter.getItem(i).toString();
                            if (str.compareTo(temp) == 0) {
                                return;
                            }
                        }
                    }

                    mClientNameAutoCompleteTextView.setText("");
                    mSiteAutoCompleteTextView.setText("");
                    mCoordinatorAutoCompleteTextView.setText("");
                    selectedClientIndex = -1;
                    selectedSiteIndex = -1;
                    selectedCoordinatorIndex = -1;

                }
            }
        });


        mSiteAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) {
                    String str = mSiteAutoCompleteTextView.getText().toString();

                    ListAdapter listAdapter = mSiteAutoCompleteTextView.getAdapter();
                    if(listAdapter != null) {
                        for (int i = 0; i < listAdapter.getCount(); i++) {
                            String temp = listAdapter.getItem(i).toString();
                            if (str.compareTo(temp) == 0) {
                                return;
                            }
                        }
                    }

                    mSiteAutoCompleteTextView.setText("");
                    mCoordinatorAutoCompleteTextView.setText("");
                    selectedSiteIndex = -1;
                    selectedCoordinatorIndex = -1;

                }
            }
        });


    }

    private void getIpmClients() {
        try {
            new CallAPI().execute(url,mApp.getButton(mKwikButtonId).getProject(),mApp.getUser().getPhone(),"123456798766");
        } catch (Exception e) {
            Logger.e( TAG, e.getMessage() );
        }
    }

    public void onNextClick(View next){

        if(selectedClientIndex == -1){
          showOneButtonErrorDialog(getString(R.string.oops),getString(R.string.ipm_login_activity_error_enter_name));
            return;
        }

        if(mClientNameAutoCompleteTextView.getText().toString() == null ){
            showOneButtonErrorDialog(getString(R.string.oops),getString(R.string.ipm_login_activity_error_enter_name));
            return;
        }

        if(mClientNameAutoCompleteTextView.getText().toString().length() == 0 ){
            showOneButtonErrorDialog(getString(R.string.oops),getString(R.string.ipm_login_activity_error_enter_name));
            return;
        }

        KwikButtonDevice button = mApp.getButton(mKwikButtonId);
        loginParams = new HashMap<>();

        loginParams.put("clientName", mClientNameAutoCompleteTextView.getText().toString());
        try {
            loginParams.put("description", mTrapDescriptionEditText.getText().toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        loginParams.put("clientId", KwikMe.USER_ID);
        try {
            loginParams.put("buttonName", mTrapNameEditText.getText().toString());
        } catch (NullPointerException e) {
            showOneButtonErrorDialog(getString(R.string.oops), getString(R.string.ipm_login_activity_trap_name_error));
            return;
        }
        if (mTrapNameEditText.getText().toString().trim().length() == 0) {
            showOneButtonErrorDialog(getString(R.string.oops), getString(R.string.ipm_login_activity_trap_name_error));
            return;
        }
        loginParams.put("clientPhoneNumber", mApp.getUser().getPhone());
        loginParams.put("managerPhoneNumber", mApp.getUser().getPhone());

        if(!(mApp.getProject(mApp.getButton(mKwikButtonId).getProject()).getLoginUri().contains("ipm=standalone"))) {

            if (selectedSiteIndex == -1) {
                showOneButtonErrorDialog(getString(R.string.oops), getString(R.string.ipm_login_activity_site_error));
                return;
            }

            if (selectedCoordinatorIndex == -1) {
                showOneButtonErrorDialog(getString(R.string.oops), getString(R.string.ipm_login_activity_coordinator_error));
                return;
            }



            loginParams.put("locationName", mSiteAutoCompleteTextView.getText().toString());
            loginParams.put("locationId", sites.get(selectedSiteIndex).getId());
            loginParams.put("coordinator", mCoordinatorAutoCompleteTextView.getText().toString());

        }


        //Logger.i(TAG,loginParams.toString());
        button.setLoginParams(loginParams);


        //register button

        KwikMe.registerUser(button, new RegisterUserListener() {
            @Override
            public void registerUserDone(KwikButtonDevice button) {
                //registerRequestSent = false;
                if (mApp.getButton(button.getId()) == null) {
                    mApp.getButtons().add(button);
                } else {
                    mApp.updateButton(button.getId(), button);
                }

                if (mApp.getProject(button.getProject()).isHasAddress()) {
                    //getUserAfterRegister(button.getId());
                } else {
                    //selectNextScreen(button);

                }

                Intent i = new Intent(IpmLoginActivity.this, ClientsActivity.class);
                i.putExtra("sender", WebviewActivity.class.getSimpleName());
                startActivity(i);
                finish();
            }

            @Override
            public void registerUserError(KwikServerError error) {
                //registerRequestSent = false;
                showTwoButtonErrorDialog( getString( R.string.oops ), error.getMessage(), getString( R.string.register_later ), getString( R.string.cancel ),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(IpmLoginActivity.this, ClientsActivity.class);
                                i.putExtra("sender", WebviewActivity.class.getSimpleName());
                                startActivity(i);
                                finish();
                            }
                        }, null);
            }
        });
    }

    public class CallAPI extends AsyncTask<String, String, IpmClientsResponse> {

        public CallAPI(){
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected IpmClientsResponse doInBackground(String... params) {

            String urlString = params[0]; // URL to call

            JSONObject orderObject=new JSONObject();
            JSONObject eventData=new JSONObject();
            JSONObject parent=new JSONObject();
            JSONObject userObject=new JSONObject();
            try {
                orderObject.put("id", params[3]);
                orderObject.put("time", Calendar.getInstance().getTime());
                orderObject.put("triggerType", "GetCoordinators");
                eventData.put("project", params[1]);
                userObject.put("phone", params[2]);
                eventData.put("userObject", userObject);
                eventData.put("orderObject", orderObject);
                parent.put("eventData", eventData);

            }catch (JSONException e){
                e.printStackTrace();
            }

            String data = parent.toString();

            OutputStream out = null;
            try {

                URL url = new URL(urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                out = new BufferedOutputStream(urlConnection.getOutputStream());

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                writer.write(data);

                writer.flush();

                writer.close();

                out.close();

                //Read
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                String result = sb.toString();

                Gson gson = new Gson();
                IpmClientsResponse respnse = gson.fromJson(result, IpmClientsResponse.class);
                urlConnection.connect();
                int httpResponse =  urlConnection.getResponseCode();
                Logger.e(TAG, "sendToServer Response from server = %d", httpResponse);

                if(respnse != null){
                    return respnse;
                }


            } catch (Exception e) {

                System.out.println(e.getMessage());



            }
            return null;
        }

        @Override
        protected void onPostExecute(IpmClientsResponse s) {
            if(s == null || s.getClients() == null || s.getClients().size() == 0){
                showTwoButtonErrorDialog(getString(R.string.oops), getString(R.string.ipm_login_activity_error_get_ipm_clients), "Retry", "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getIpmClients();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(IpmLoginActivity.this, ClientsActivity.class);
                        i.putExtra("sender", WebviewActivity.class.getSimpleName());
                        startActivity(i);
                        finish();
                    }
                });

                return;
            }
            clients = s.getClients();
            ArrayAdapter<Client> mClientsAdapter = new ArrayAdapter<Client>(IpmLoginActivity.this,
                    android.R.layout.simple_dropdown_item_1line, s.getClients());
            mClientNameAutoCompleteTextView.setAdapter(mClientsAdapter);

        }
    }

}
