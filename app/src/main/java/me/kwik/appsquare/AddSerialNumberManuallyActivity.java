package me.kwik.appsquare;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikLocation;
import me.kwik.listeners.CreateClientButtonListener;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;

public class AddSerialNumberManuallyActivity extends BaseActivity  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private Application mApp;
    private KwikDevice mButton;
    private String mClientId;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 03;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_serial_number_manualy);

        mActionBarTitle.setText(R.string.add_serial_manually_activity_title);
        mApp = (Application)getApplication();

        Intent i = getIntent();
        mButton = (KwikDevice) i.getParcelableExtra("button");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        try {
            mClientId = getIntent().getExtras().getString("client");
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mGoogleApiClient.disconnect();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.playAudioFile(AddSerialNumberManuallyActivity.this, "enter_the_serial_number", 0, 5);
    }

    public void enterSerialNumberClicked(View enterSerialNumber){
        AlertDialog.Builder alert = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        final EditText edittext = new EditText(this);
        edittext.setKeyListener(new DigitsKeyListener());
        edittext.setHint(R.string.enter_serial_number_dialog_edit_text_hint);

        alert.setTitle(R.string.enter_serial_number_dialog_title);

        alert.setView(edittext);

        alert.setPositiveButton(R.string.enter_serial_number_dialog_submit_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String serialNumber = edittext.getText().toString();
                if(serialNumber.length() != 7){
                    showOneButtonErrorDialog(getString(R.string.enter_serial_number_dialog_error_header), getString(R.string.enter_serial_number_error_length_error));
                    dialog.dismiss();
                    return;
                }

                try {
                    double d = Double.parseDouble(serialNumber);
                }
                catch(NumberFormatException nfe) {
                    showOneButtonErrorDialog(getString(R.string.enter_serial_number_dialog_error_header), getString(R.string.enter_serial_number_error_numbers_only_error));
                    dialog.dismiss();
                    return;
                }

                mButton.setId(serialNumber);
                mButton.setSerial(serialNumber);

                if(mLastLocation != null) {
                    KwikLocation location = new KwikLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAccuracy());
                    mButton.setCoordinate(location);
                }

                KwikMe.createClientButton(mButton.getClient(), mButton, new CreateClientButtonListener() {
                    @Override
                    public void createClientButtonListenerDone(KwikDevice device) {
                        Intent i = new Intent(AddSerialNumberManuallyActivity.this, GoodJobActivity.class);
                        i.putExtra("buttonId",serialNumber);
                        i.putExtra("client",mClientId);
                        startActivity(i);
                        finish();
                    }
                    @Override
                    public void createClientButtonListenerError(KwikServerError error) {
                        showErrorDialog(error);
                    }
                });

            }
        });

        alert.setNegativeButton(R.string.enter_serial_number_error_cancel_button, null);

        final AlertDialog dialog = alert.create();

        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                                      @Override
                                      public void onShow(DialogInterface arg0) {
                                          dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.kwik_me_orange));
                                          dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.kwik_me_orange));

                                      }
                                  });

        dialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddSerialNumberManuallyActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
