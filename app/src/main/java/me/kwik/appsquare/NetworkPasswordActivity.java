package me.kwik.appsquare;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.List;

import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.RushSearch;
import me.kwik.bl.KwikDevice;
import me.kwik.data.KwikNetwork;
import me.kwik.utils.Logger;
import me.kwik.utils.NetworkUtil;
import me.kwk.utils.Utils;

public class NetworkPasswordActivity extends BaseActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 02;
    private TextView hideShowTextView;
    private EditText mWifiPasswordEditText;
    private EditText mWifiNetworkSsidEditText;
    private String mWifiNetworkSsid;
    private static boolean BUTTON_TO_AP_ACTIVITY_FIRST_RUN = true;
    private TextView mNeedHelpTextView;
    private String mSender;
    private String TAG = NetworkPasswordActivity.class.getSimpleName();
    private SmartNetworkDialog smartNetworkDialog;
    private CheckBox mSavePasswordCheckBox;
    private RelativeLayout relativeLayout;
    private Button mNextButton;
    private KwikDevice mButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_password);

        relativeLayout = (RelativeLayout) findViewById(R.id.network_password_activity_main_layout);

        mWifiPasswordEditText = (EditText) findViewById(R.id.network_password_activity_password_edit_text);
        mSavePasswordCheckBox = (CheckBox) findViewById(R.id.network_password_activity_save_password_check_box);
        mNeedHelpTextView = (TextView) findViewById(R.id.network_password_activity_help_link_text_view);
        mWifiNetworkSsidEditText = (EditText) findViewById(R.id.network_password_activity_ssid_edit_text);
        hideShowTextView = (TextView) findViewById(R.id.choose_network_hide_show_text_view);
        mNextButton = (Button)findViewById(R.id.network_password_activity_next_button);

        hideShowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = hideShowTextView.getText().toString();
                if (status.equals(getResources().getString(R.string.network_password_activity_hide))) {
                    hideShowTextView.setText(R.string.network_password_activity_show);
                    mWifiPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mWifiPasswordEditText.setSelection(mWifiPasswordEditText.getText().length());
                } else {
                    hideShowTextView.setText(R.string.network_password_activity_hide);
                    mWifiPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    mWifiPasswordEditText.setSelection(mWifiPasswordEditText.getText().length());
                }
            }
        });

        mWifiPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    clickNext();
                }
                return false;
            }
        });
        mActionBarTitle.setText(R.string.network_password_activity_title);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion == Build.VERSION_CODES.M) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NetworkPasswordActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                }
            }
        }

        Intent i = getIntent();
        mButton = (KwikDevice) i.getParcelableExtra("button");
    }

    private void buildAlertMessageNoGps() {
        final android.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new android.app.AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.your_gps_seems_to_be_disabled)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mSender = getIntent().getStringExtra("sender");
        } catch (NullPointerException e) {
            Logger.e(TAG, "%s", e.getMessage());
        }
        mWifiNetworkSsid = NetworkUtil.getWifiName(NetworkPasswordActivity.this);


        if (mWifiPasswordEditText != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mWifiPasswordEditText.getWindowToken(), 0);
        }
        try {
            if (mWifiNetworkSsidEditText != null) {
                mWifiNetworkSsidEditText.setText(mWifiNetworkSsid);
                List<KwikNetwork> savedNetwors = new RushSearch().find(KwikNetwork.class);
                if (savedNetwors != null) {
                    for (KwikNetwork network : savedNetwors) {
                        if (network.getSsid() != null && network.getSsid().equals(mWifiNetworkSsid)) {
                            if (network.getPassword() != null) {
                                mWifiPasswordEditText.setText(network.getPassword());
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            Logger.e(TAG, "Exception %s", e.getMessage());
        }


        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNext();
            }
        });

        if (mWifiNetworkSsid == null || mWifiNetworkSsid.isEmpty()) {
            showTwoButtonErrorDialog( getString( R.string.network_password_activity_title ), getString( R.string.network_password_activity_no_wifi_error_message ),
                    getString( R.string.network_password_activity_open_wifi_settings ), getString( R.string.network_password_activity_continue ), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity( new Intent( Settings.ACTION_WIFI_SETTINGS ) );
                            hideTwoButtonsErrorsDialog();
                        }
                    },
                    null);
            return;
        }else{
            hideTwoButtonsErrorsDialog();
        }
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        List<ScanResult> results = null;
        try {
            results = wifi.getScanResults();
        }catch (SecurityException e){
            e.printStackTrace();
        }

        if (results!= null  && results.size() == 0) {
            String last4Characters = "";
            if (mWifiNetworkSsid != null) {
                if (mWifiNetworkSsid.length() <= 4) {
                    last4Characters = mWifiNetworkSsid;
                } else {
                    last4Characters = mWifiNetworkSsid.substring(mWifiNetworkSsid.length() - 4);
                }

                if (last4Characters.contains("5")) {
                    Utils.playAudioFile(this, "5ghz_error_message",0,5);
                    showTwoButtonErrorDialog( getString( R.string.network_password_activity_title ), getString( R.string.network_password_activity_wifi_5_g_error ),
                            getString( R.string.network_password_activity_open_wifi_settings ), getString( R.string.network_password_activity_continue ),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                    hideTwoButtonsErrorsDialog();
                                }
                            },
                            null);
                    return;
                }
            }
        } else if(results!= null) {
            for (ScanResult s : results) {
                if (s.SSID.equals(mWifiNetworkSsid)) {
                    if (s.frequency > 4000) {

                        for (ScanResult j : results) {
                            if(j.SSID.equals( s.SSID ) && j.frequency < 4000){
                                return;
                            }
                        }

                        Utils.playAudioFile(this, "5ghz_error_message",0,5);
                        showTwoButtonErrorDialog( getString( R.string.network_password_activity_title ), getString( R.string.network_password_activity_wifi_5_g_error ),
                                getString( R.string.network_password_activity_open_wifi_settings ), getString( R.string.network_password_activity_continue ),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                        hideTwoButtonsErrorsDialog();
                                    }
                                },
                                null);
                        return;
                    }else{
                        hideTwoButtonsErrorsDialog();
                        Utils.playAudioFile(this,"wifi_credentials",0,5);
                    }
                }
            }
        }

        mWifiNetworkSsidEditText.setFocusable(false);
        mWifiNetworkSsidEditText.setFocusableInTouchMode(false);
        mWifiNetworkSsidEditText.setClickable(false);
        BUTTON_TO_AP_ACTIVITY_FIRST_RUN = true;
       // mNextImageView.setVisibility(View.INVISIBLE);


    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.stopPlaying();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    } else {

                    }

                    final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();
                    }
                } else {


                }
                return;
            }
        }
    }

    @Override
    protected void clickNext() {
        if (mWifiPasswordEditText.getText().toString().trim().isEmpty()) {
            android.app.AlertDialog.Builder builder;

            builder = new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);

            android.app.AlertDialog alertDialog = builder.create();

            alertDialog.setMessage(getString(R.string.password_is_empty));

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.continue_without_password), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    startButtonToApActivity();

                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.type_a_password), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    dialog.dismiss();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInputFromWindow(relativeLayout.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);

                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.change_network), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    showAdvancedWifiIfAvailable(NetworkPasswordActivity.this);
                    dialog.dismiss();

                }
            });
            alertDialog.show();

        } else {
            super.clickNext();
            startButtonToApActivity();
        }
    }

    private void startButtonToApActivity() {
        Intent i = new Intent(NetworkPasswordActivity.this, ButtonToApActivity.class);
        i.putExtra("selectedWiFiSsid", mWifiNetworkSsid);
        i.putExtra("password", mWifiPasswordEditText.getText().toString().trim());
        i.putExtra("button",mButton);
        if (mSender != null) {
            i.putExtra("sender", mSender);
        }
        KwikNetwork networkToSave = new KwikNetwork(mWifiNetworkSsid, mWifiPasswordEditText.getText().toString().trim());
        networkToSave.save();
        if (!mSavePasswordCheckBox.isChecked()) {
            mWifiPasswordEditText.getText().clear();
            RushCore.getInstance().deleteAll(KwikNetwork.class);
        }

        startActivity(i);
    }

    private static void showAdvancedWifiIfAvailable(Context ctx) {
        final Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (activityExists(ctx, i)) {
            ctx.startActivity(i);
        }
    }

    private static boolean activityExists(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        final ResolveInfo info = mgr.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return (info != null);
    }

    public void helpClicked(View view) {
        Utils.showHelp(this);
    }

}
