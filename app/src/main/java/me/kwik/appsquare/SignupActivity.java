package me.kwik.appsquare;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.util.regex.Pattern;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.listeners.SendValidationCodeListener;
import me.kwk.utils.Utils;


public class SignupActivity extends BaseActivity {
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private AutoCompleteTextView mPhoneNumberPrefix;
    private EditText mPhoneNumberEditText;
    private EditText mEmailEditText;
    boolean cancelWaitForSMSTimeout = false;
    private String TAG = SignupActivity.class.getSimpleName();
    private String mName;
    private Application mApp;
    private Button mNextButton;
    public static final String LAST_NAME_EXTRA_INTENT = "me.kwik.me.sign.up.last.name.extra.intent";
    public static final String FIRST_NAME_EXTRA_INTENT = "mName";
    public static final String PHONE_NUMBER_EXTRA_INTENT = "phoneNumber";
    public static final String EMAIL_EXTRA_INTENT = "me.kwik.me.email.extra.intent";
    private boolean validFirstName = false;
    private boolean validLastName = false;
    private boolean validPhoneNumber = false;
    private boolean validEmail = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        mFirstNameEditText = (EditText) findViewById(R.id.sign_up_activity_name_edit_text);
        mLastNameEditText = (EditText)findViewById(R.id.sign_up_activity_last_name_edit_text);
        mPhoneNumberEditText = (EditText) findViewById(R.id.sign_up_activity_phone_edit_text);
        mPhoneNumberPrefix = (AutoCompleteTextView)findViewById(R.id.sign_up_activity_phone_prefix);
        mNextButton = (Button)findViewById(R.id.sign_up_activity_next_button);
        mEmailEditText = (EditText)findViewById(R.id.sign_up_activity_email_edit_text);

        mActionBarTitle.setText(R.string.setup_activity_title);
        mApp = (Application)getApplication();


        mPhoneNumberPrefix = Utils.setAutoCompleteTextViewAdapter(mPhoneNumberPrefix, this);

        mPrevTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        TextView.OnEditorActionListener listener = new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    clickNext();
                }
                return false;
            }
        };

        mPhoneNumberEditText.setOnEditorActionListener(listener);
        mFirstNameEditText.setOnEditorActionListener(listener);
        mLastNameEditText.setOnEditorActionListener(listener);
        mEmailEditText.setOnEditorActionListener(listener);

        mPhoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 5 ){
                    validPhoneNumber = true;
                    if(validFirstName && validLastName && validEmail){
                        mNextButton.setBackground(getDrawable(R.drawable.start_button_selector));
                    }
                }else{
                    validPhoneNumber = false;
                    mNextButton.setBackground(getDrawable(R.drawable.start_button_selector_disabled));
                }

            }
        });

        mFirstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.length() > 0 ){
                    validFirstName = true;
                    if(validPhoneNumber && validLastName && validEmail){
                        mNextButton.setBackground(getDrawable(R.drawable.start_button_selector));
                    }
                }else{
                    validFirstName = false;
                    mNextButton.setBackground(getDrawable(R.drawable.start_button_selector_disabled));
                }
            }
        });

        mLastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0 ){
                    validLastName = true;
                    if(validPhoneNumber && validFirstName && validEmail){
                        mNextButton.setBackground(getDrawable(R.drawable.start_button_selector));
                    }
                }else{
                    validLastName = false;
                    mNextButton.setBackground(getDrawable(R.drawable.start_button_selector_disabled));
                }
            }
        });

        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Utils.isValidEmail(s.toString()) ){
                    validEmail = true;
                    if(validPhoneNumber && validFirstName && validLastName){
                        mNextButton.setBackground(getDrawable(R.drawable.start_button_selector));
                    }
                }else{
                    validEmail = false;
                    mNextButton.setBackground(getDrawable(R.drawable.start_button_selector_disabled));
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNext();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //If this called after click change phone number at VerifyPhoneNumber screen
        if (getIntent().getStringExtra("sender") != null && getIntent().getStringExtra("sender").equals(VerifyPhoneActivity.class.getSimpleName())) {
            if (getIntent().getStringExtra("phoneNumber") != null) {
                mPhoneNumberEditText.setText(getIntent().getStringExtra("phoneNumber"));
            }

            if (getIntent().getStringExtra("name") != null) {
                mFirstNameEditText.setText(getIntent().getStringExtra("name"));
            }
        }

        cancelWaitForSMSTimeout = false;

        if (getIntent().getStringExtra("sender")!= null && getIntent().getStringExtra("sender").equals(LoginActivity.class.getSimpleName())) {
            if (getIntent().getStringExtra("phoneNumber") != null) {
                mPhoneNumberEditText.setText(getIntent().getStringExtra("phoneNumber"));
            }
        }
        //mNextImageView.setVisibility(View.INVISIBLE);
        Utils.playAudioFile(this, "lets_get_started",0,5);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.stopPlaying();
    }


    @Override
    protected void clickNext() {
        super.clickNext();

        if (isEditTextsEmpty(new EditText[]{mFirstNameEditText, mPhoneNumberEditText,mLastNameEditText}, new String[]{getString(R.string.sign_up_activity_name_is_empty), getString(R.string.phone_is_empty),getString(R.string.last_name_is_empty)})) {
            mNextImageView.setClickable(true);
            Utils.playAudioFile(this, "missed_field",1,5);
            hideNextSpinner();
            //TODO: Remove this after removing mNextImageView in base activity
            //mNextImageView.setVisibility(View.INVISIBLE);
            return;
        }

        if(mPhoneNumberPrefix.getText().toString().trim().length() == 0){
            mPhoneNumberPrefix.setError(getString(R.string.please_enter_country_code));
            hideNextSpinner();
            //TODO: Remove this after removing mNextImageView in base activity
            //mNextImageView.setVisibility(View.INVISIBLE);
            return;
        }

        final String phoneNumber;
        //TODO: Change this, Check phone number validation - works for Israely numbers only for now
        if(mPhoneNumberPrefix.getText().toString().startsWith("+972")) {
            String phoneRegex = "^(\\+\\d{1,3})(0?)(5[012345678]){1}(\\d{7})$";

            Pattern pattern = Pattern.compile(phoneRegex);

            if (!pattern.matcher(mPhoneNumberPrefix.getText().toString().trim() + mPhoneNumberEditText.getText().toString().replace(" ", "")).matches()) {
                mPhoneNumberEditText.setError(getString(R.string.log_in_activity_phone_number_edit_text_invalid_error));
                hideNextSpinner();
                //TODO: Remove this after removing mNextImageView in base activity
                //mNextImageView.setVisibility(View.INVISIBLE);
                return;
            }

            String reversedPhoneNumber = new StringBuilder(mPhoneNumberEditText.getText().toString().replace(" ", "")).reverse().toString();

            if (reversedPhoneNumber.length() == 10 && reversedPhoneNumber.charAt(9) == '0')
                reversedPhoneNumber = reversedPhoneNumber.substring(0, 9) + reversedPhoneNumber.substring(9 + 1);

            phoneNumber =   new StringBuilder(reversedPhoneNumber).reverse().toString();
        }else{
            phoneNumber =  mPhoneNumberEditText.getText().toString().replace(" ", "");
        }

        if(!Utils.isValidEmail(mEmailEditText.getText().toString())){
            mEmailEditText.setError(getString(R.string.sign_up_activity_invalid_email));
            return;
        }

        mName = mFirstNameEditText.getText().toString();
        sendPhoneNumber(mPhoneNumberPrefix.getText().toString().trim(), phoneNumber.replace(" ", ""), false, mName);
    }

    private void sendPhoneNumber(final String phoneNumberPrefix,final String phoneNumber, final boolean isUserExist, final String name) {

        String phone = phoneNumberPrefix + phoneNumber;
        KwikMe.sendValidationCode(phone, isUserExist, new SendValidationCodeListener() {
            @Override
            public void sendValidationCodeDone() {
                Intent i = new Intent(SignupActivity.this, VerifyPhoneActivity.class);
                if (isUserExist) {
                    i.putExtra("sender", LoginActivity.class.getSimpleName());
                } else {
                    i.putExtra("sender", SignupActivity.class.getSimpleName());
                }
                i.putExtra(PHONE_NUMBER_EXTRA_INTENT, phoneNumber);
                i.putExtra("phoneNumberPrefix",phoneNumberPrefix);
                i.putExtra(FIRST_NAME_EXTRA_INTENT, name);
                i.putExtra(LAST_NAME_EXTRA_INTENT,mLastNameEditText.getText().toString());
                i.putExtra(EMAIL_EXTRA_INTENT,mEmailEditText.getText().toString());
                startActivity(i);
            }

            @Override
            public void sendValidationCodeError(KwikServerError kwikServerError) {
                hideNextSpinner();
                if (kwikServerError.getValue() == 409) {
                    showTwoButtonErrorDialog( "", kwikServerError.getMessage(),
                            getString( R.string.sign_up_activity_login ),
                            getString( R.string.sign_up_activity_change ),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showProgressBar();
                                    sendPhoneNumber(mPhoneNumberPrefix.getText().toString().trim(),phoneNumber.replace(" ", ""), true, name);

                                }
                            },null);

                } else {
                    showOneButtonErrorDialog(getString(R.string.oops), kwikServerError.getMessage());
                }
            }
        });
    }
}
