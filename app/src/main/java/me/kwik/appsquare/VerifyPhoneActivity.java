package me.kwik.appsquare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.TimeZone;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikLocation;
import me.kwik.data.KwikUser;
import me.kwik.listeners.CreateUserListener;
import me.kwik.listeners.GetKwikUserListener;
import me.kwik.listeners.LoginByListener;
import me.kwik.listeners.SendValidationCodeListener;
import me.kwik.rest.responses.CreateUserResponse;
import me.kwik.rest.responses.LoginResponse;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;


public class VerifyPhoneActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText mValidationCodeEditText;
    private String name;
    private String mLastName;
    private String mEmail;
    private String phoneNumber;
    private String phoneNumberPrefix;
    private String sender;
    private BroadcastReceiver mSmsBroadcastReceiver;
    private Application mApp;
    private String method;
    private IntentFilter mIntentFilter = new IntentFilter();
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private Button mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        TextView sentMessage = (TextView) findViewById(R.id.verify_phone_activity_message_sent_text_view);
        mValidationCodeEditText = (EditText) findViewById(R.id.verify_phone_activity_code_edit_text);
        mNextButton = (Button) findViewById(R.id.verify_phone_activity_next_button);
        mApp = (Application) getApplication();
        mValidationCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    clickNext();
                }
                return false;
            }
        });

        mValidationCodeEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() == 4) {
                    clickNext();
                }

            }
        });

        name = getIntent().getStringExtra("mName");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        phoneNumberPrefix = getIntent().getStringExtra("phoneNumberPrefix");
        mLastName = getIntent().getStringExtra(SignupActivity.LAST_NAME_EXTRA_INTENT);
        mEmail = getIntent().getStringExtra(SignupActivity.EMAIL_EXTRA_INTENT);

        sender = getIntent().getStringExtra("sender");

        mActionBarTitle.setText(R.string.verify_phone_activity_title);
        if (KwikMe.LOCAL == Application.LOCAL_HE_IL) {
            sentMessage.setText(getResources().getString(R.string.verify_phone_activity_enter_the_code_we_sent_to) + " " + phoneNumberPrefix.substring(1) + phoneNumber + "+");
        } else {
            sentMessage.setText(getResources().getString(R.string.verify_phone_activity_enter_the_code_we_sent_to) + " " +  phoneNumberPrefix + phoneNumber);
        }

        mPrevTextView.setVisibility(View.INVISIBLE);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNext();
            }
        });
    }

    // Click resend sms link
    public void resendSms(View resendTextView) {
        if (phoneNumber != null) {
            if (sender.equals(SignupActivity.class.getSimpleName())) {

                sendPhoneNumber(phoneNumberPrefix + phoneNumber, false, null);
            } else if (sender.equals(LoginActivity.class.getSimpleName())) {
                sendPhoneNumber(phoneNumberPrefix + phoneNumber, true, null);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.stopPlaying();


        Utils.playAudioFile(this, "sms_verification",0,5);

        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

        mSmsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                Bundle bundle = intent.getExtras();

                if (bundle != null) {

                    Object[] pdus = (Object[]) intent.getExtras().get("pdus");
                    SmsMessage shortMessage;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = bundle.getString("format");
                        shortMessage = SmsMessage.createFromPdu((byte[]) pdus[0], format);
                    } else {
                        shortMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
                    }

                    final String s = shortMessage.getDisplayMessageBody();    // has the actual message

                    Logger.d("TAG", "Sms : %s", s);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mValidationCodeEditText.setText(s.substring(0, 4));
                        }
                    });

                }
            }
        };
        registerReceiver(mSmsBroadcastReceiver, mIntentFilter);
       // mNextImageView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSmsBroadcastReceiver != null) {
            try {
                unregisterReceiver(mSmsBroadcastReceiver);
            } catch (Exception e) {
                //Do nothing
            }

        }
        Utils.stopPlaying();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    // Click change phone number link
    public void changePhoneNumber(View changePhoneNumberTextView) {
        Class a;

        if (name == null || name.isEmpty()) {
            a = LoginActivity.class;
        } else {
            a = SignupActivity.class;
        }

        Intent i = new Intent(VerifyPhoneActivity.this, a);
        i.putExtra("sender", VerifyPhoneActivity.class.getSimpleName());
        i.putExtra("name", name);
        i.putExtra("phoneNumber", phoneNumber);
        i.putExtra(SignupActivity.LAST_NAME_EXTRA_INTENT,mLastName);
        i.putExtra(SignupActivity.EMAIL_EXTRA_INTENT,mEmail);
        startActivity(i);
        this.finish();
    }

    @Override
    protected void clickNext() {
        super.clickNext();
        method = "phone";
        if (isEditTextsEmpty(new EditText[]{mValidationCodeEditText}, new String[]{getString(R.string.verify_phone_activity_please_enter_the_code)})) {
            return;
        }
        String validationCode = mValidationCodeEditText.getText().toString();

        if (sender.equals(SignupActivity.class.getSimpleName())) {
            KwikUser user = new KwikUser();
            user.setFirstName(name);
            user.setLastName(mLastName);
            user.setEmail(mEmail);
            user.setTimezone( TimeZone.getDefault().getID());


            if (KwikMe.LOCAL != null) {
                user.setLocale(KwikMe.LOCAL);
            }
            mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                    .setAction("request")
                    .setLabel("Create user")
                    .build());
            KwikLocation location = null;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
              //  return;
            }else {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            if (mLastLocation != null) {
                location = new KwikLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAccuracy());
            }
            KwikMe.createUser(phoneNumberPrefix + phoneNumber, validationCode, method,user ,location, new CreateUserListener() {
                @Override
                public void createUserDone(CreateUserResponse res) {
                    Intent i = new Intent(VerifyPhoneActivity.this, WiFiSelectionActivity.class);
                    i.putExtra("name", name);
                    ((Application)getApplication()).getUser().setId(res.getUser().getId());
                    ((Application)getApplication()).getUser().setFirstName(name);
                    ((Application)getApplication()).getUser().setLastName(mLastName);
                    ((Application)getApplication()).getUser().setEmail(mEmail);
                    ((Application)getApplication()).getUser().setPhone(phoneNumberPrefix +phoneNumber);
                    ((Application)getApplication()).getUser().save();
                    startActivity(i);
                    VerifyPhoneActivity.this.finish();
                }

                @Override
                public void createUserError(KwikServerError error) {
                    mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                            .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                            .setAction("response")
                            .setLabel("Create user")
                            .setValue(1)
                            .build());
                    showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
                }
            });

        }else if(sender.equals(LoginActivity.class.getSimpleName())){
            KwikMe.loginByValidationCode(validationCode,phoneNumberPrefix + phoneNumber,method, new LoginByListener() {
                @Override
                public void loginByDone(LoginResponse res) {
                    KwikMe.getKwikUser(res.getUser().getId(), new GetKwikUserListener() {
                        @Override
                        public void getKwikUserDone(KwikUser user) {
                            Intent i = new Intent(VerifyPhoneActivity.this, ClientsActivity.class);
                            ((Application) getApplication()).setUser(user);
                            startActivity(i);
                            finish();
                            return;
                        }

                        @Override
                        public void getKwikUserError(KwikServerError error) {
                            showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
                            return;

                        }
                    });
                }

                @Override
                public void loginByError(KwikServerError error) {
                    showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
                }
            });

        }

    }

    private void sendPhoneNumber(final String phoneNumber, final boolean isUserExist,final String name) {

        showProgressBar();
        KwikMe.sendValidationCode(phoneNumber, isUserExist, new SendValidationCodeListener() {
            @Override
            public void sendValidationCodeDone() {
                hideProgressBar();
            }

            @Override
            public void sendValidationCodeError(KwikServerError kwikServerError) {
                hideProgressBar();
                showOneButtonErrorDialog(getString(R.string.oops), kwikServerError.getMessage());

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
