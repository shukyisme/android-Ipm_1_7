package me.kwik.square;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikLocation;
import me.kwik.listeners.HandShakeListener;
import me.kwik.listeners.InitialHandShakeListener;
import me.kwik.listeners.LoginListener;
import me.kwik.rest.responses.InitialHandShakeResponse;
import me.kwik.rest.responses.LoginResponse;
import me.kwik.utils.Logger;

public class SplashActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 03;
    private static final int REQUEST_ID_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 04;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String TAG = SplashActivity.class.getSimpleName();
    private Application mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mApp = (Application) getApplication();
        getSupportActionBar().hide();
        askForStoragePermission();
    }

    private void askForStoragePermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SplashActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_ID_WRITE_EXTERNAL_STORAGE_PERMISSIONS);

        }else {
            // Create an instance of GoogleAPIClient.
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder( this )
                        .addConnectionCallbacks( this )
                        .addOnConnectionFailedListener( this )
                        .addApi( LocationServices.API )
                        .build();
                mGoogleApiClient.connect();
            }
        }
    }

    private void init() {
        KwikLocation location = null;
        if (mLastLocation != null) {
            location = new KwikLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAccuracy());
        }
        String version="";
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            if(version.length() > 1){
                version = version.substring(0,version.length() - 1);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        if (KwikMe.USER_EMAIL != null || KwikMe.USER_PASSWROD != null) {
            KwikMe.login(KwikMe.USER_EMAIL, KwikMe.USER_PASSWROD, new LoginListener() {
                @Override
                public void onLoginCompleted(LoginResponse response) {
                    mApp.setUser(response.getUser());
                    Intent i = new Intent(SplashActivity.this,ClientsActivity.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onLoginError(KwikServerError error) {
                    Intent i = new Intent(SplashActivity.this, SignInActivity.class);
                    startActivity(i);
                    finish();
                }
            });

        } else {
            Intent i = new Intent(SplashActivity.this, SignInActivity.class);
            startActivity(i);
            finish();
        }

//        if (KwikMe.USER_TOKEN != null && KwikMe.USER_ID != null){
//            handShake(location, version);
//        }else{
//            initialHandShake(location, version);
//        }
    }

    private void initialHandShake(KwikLocation location, String version) {
        KwikMe.initialHandShake(version, FirebaseInstanceId.getInstance().getToken(), location, getPackageName() , new InitialHandShakeListener() {
            @Override
            public void initialHandShakeDone(InitialHandShakeResponse.Message message) {
                if (message != null) {
                    showOneButtonErrorDialog(message.getTitle(), message.getMessage());
                } else {
                    if (KwikMe.USER_EMAIL != null || KwikMe.USER_PASSWROD != null) {
                        KwikMe.login(KwikMe.USER_EMAIL, KwikMe.USER_PASSWROD, new LoginListener() {
                            @Override
                            public void onLoginCompleted(LoginResponse response) {
                                Intent i = new Intent(SplashActivity.this,ClientsActivity.class);
                                startActivity(i);
                                finish();
                            }

                            @Override
                            public void onLoginError(KwikServerError error) {
                                Intent i = new Intent(SplashActivity.this, SignInActivity.class);
                                startActivity(i);
                                finish();
                            }
                        });

                    } else {
                        Intent i = new Intent(SplashActivity.this, SignInActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
            }

            @Override
            public void initialHandShakeError(KwikServerError kwikServerError) {
                if (kwikServerError != null) {
                    showOneButtonErrorDialog(getString(R.string.oops), kwikServerError.getMessage());
                }

            }
        });
    }

    private void handShake(KwikLocation location, String version) {
        KwikMe.handShake(version, FirebaseInstanceId.getInstance().getToken() , getPackageName(), location, new HandShakeListener() {
            @Override
            public void handShakeDone(InitialHandShakeResponse.Message message) {
                if (message != null) {
                    showOneButtonErrorDialog(message.getTitle(), message.getMessage());
                } else {
                    if (KwikMe.USER_TOKEN != null || KwikMe.USER_ID != null) {
                        Intent i = new Intent(SplashActivity.this,ClientsActivity.class);
                        startActivity(i);
                        finish();
//                        KwikMe.getKwikUser(KwikMe.USER_ID, new GetKwikUserListener() {
//                            @Override
//                            public void getKwikUserDone(KwikUser user) {
//                                Intent i = new Intent(SplashActivity.this, ClientsActivity.class);
//                                ((Application) getApplication()).setUser(user);
//                                startActivity(i);
//                                finish();
//                                return;
//                            }
//
//                            @Override
//                            public void getKwikUserError(KwikServerError error) {
//                                //If user token was expired or user deleted from the server
//                                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
//                                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
//                                        .setAction("response")
//                                        .setLabel("Get kwik user")
//                                        .setValue(1)
//                                        .build());
//                                Intent i = new Intent(SplashActivity.this, StartActivity.class);
//                                startActivity(i);
//                                finish();
//                            }
//                        });

                    } else {
                        Intent i = new Intent(SplashActivity.this, StartActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
            }

            @Override
            public void handShakeError(KwikServerError error) {
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Initial handShake")
                        .setValue(1)
                        .build());
                if (error != null) {
                    if(error.getValue() == 401){
                        Intent i = new Intent(SplashActivity.this, StartActivity.class);
                        startActivity(i);
                        finish();
                    }else {
                        showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean showRationale = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            showRationale = shouldShowRequestPermissionRationale( permissions[0] );
        }
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        Logger.d(TAG, "Location lat = %s", String.valueOf(mLastLocation.getLatitude()));
                        Logger.d(TAG, "Location long = %s", String.valueOf(mLastLocation.getLongitude()));
                        Logger.d(TAG, "Location acc = %s", String.valueOf(mLastLocation.getAccuracy()));

                    }
                    init();

                } else {

                    init();
                }
                return;
            }
            case REQUEST_ID_WRITE_EXTERNAL_STORAGE_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mGoogleApiClient == null) {
                        mGoogleApiClient = new GoogleApiClient.Builder( this )
                                .addConnectionCallbacks( this )
                                .addOnConnectionFailedListener( this )
                                .addApi( LocationServices.API )
                                .build();
                        mGoogleApiClient.connect();
                    }

                }else{
                    final boolean finalShowRationale = showRationale;
                    if (!finalShowRationale) {
                        if (mGoogleApiClient == null) {
                            mGoogleApiClient = new GoogleApiClient.Builder( this )
                                    .addConnectionCallbacks( this )
                                    .addOnConnectionFailedListener( this )
                                    .addApi( LocationServices.API )
                                    .build();
                            mGoogleApiClient.connect();
                        }
                        return;
                    }
                    showTwoButtonErrorDialog( getString( R.string.oops ), getString( R.string.ask_for_storage_permission_message), getString( R.string.kwik_continue), "", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            askForStoragePermission();
                        }
                    }, null );
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        //   mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        try {
            mGoogleApiClient.disconnect();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,  Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                Logger.d(TAG,"Location lat = %s",String.valueOf(mLastLocation.getLatitude()));
                Logger.d(TAG,"Location long = %s", String.valueOf(mLastLocation.getLongitude()));
                Logger.d(TAG, "Location acc = %s", String.valueOf(mLastLocation.getAccuracy()));

            }
            init();
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        init();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        init();
    }
}
