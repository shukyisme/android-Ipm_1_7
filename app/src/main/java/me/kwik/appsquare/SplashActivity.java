package me.kwik.appsquare;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikLocation;
import me.kwik.listeners.InitialHandShakeListener;
import me.kwik.listeners.LoginListener;
import me.kwik.rest.responses.InitialHandShakeResponse;
import me.kwik.rest.responses.LoginResponse;

public class SplashActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

        init();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mGoogleApiClient.disconnect();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void init() {
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
            KwikLocation location = null;
            if(mLastLocation != null) {
                location = new KwikLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAccuracy());
            }
            KwikMe.login(KwikMe.USER_EMAIL, KwikMe.USER_PASSWROD, location, new LoginListener() {
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
        KwikMe.initialHandShake(version, null, location, getPackageName() , new InitialHandShakeListener() {
            @Override
            public void initialHandShakeDone(InitialHandShakeResponse.Message message) {
                if (message != null) {
                    showOneButtonErrorDialog(message.getTitle(), message.getMessage());
                } else {
                    if (KwikMe.USER_EMAIL != null || KwikMe.USER_PASSWROD != null) {
                        KwikLocation location = null;
                        if(mLastLocation != null) {
                            location = new KwikLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAccuracy());
                        }
                        KwikMe.login(KwikMe.USER_EMAIL, KwikMe.USER_PASSWROD, location, new LoginListener() {
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

//    private void handShake(KwikLocation location, String version) {
//        KwikMe.handShake(version, null , getPackageName(), location, new HandShakeListener() {
//            @Override
//            public void handShakeDone(InitialHandShakeResponse.Message message) {
//                if (message != null) {
//                    showOneButtonErrorDialog(message.getTitle(), message.getMessage());
//                } else {
//                    if (KwikMe.USER_TOKEN != null || KwikMe.USER_ID != null) {
//                        Intent i = new Intent(SplashActivity.this,ClientsActivity.class);
//                        startActivity(i);
//                        finish();
//
//                    } else {
//                        Intent i = new Intent(SplashActivity.this, StartActivity.class);
//                        startActivity(i);
//                        finish();
//                    }
//                }
//            }
//
//            @Override
//            public void handShakeError(KwikServerError error) {
//                if (error != null) {
//                    if(error.getValue() == 401){
//                        Intent i = new Intent(SplashActivity.this, StartActivity.class);
//                        startActivity(i);
//                        finish();
//                    }else {
//                        showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
//                    }
//                }
//            }
//        });
//    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                return;
            }
        }
    }


}
