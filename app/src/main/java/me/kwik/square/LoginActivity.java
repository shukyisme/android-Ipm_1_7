package me.kwik.square;

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

public class LoginActivity extends BaseActivity {
    private AutoCompleteTextView mPhoneNumberPrefix;
    private EditText mPhoneNumberEditText;
    private Application mApp;
    private Button mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mApp = (Application)getApplication();
        mPhoneNumberEditText = (EditText)findViewById(R.id.login_activity_phone_edit_text);
        mPhoneNumberPrefix = (AutoCompleteTextView)findViewById(R.id.login_activity_phone_prefix);
        mNextButton = (Button)findViewById(R.id.log_in_activity_next_button);

        mPhoneNumberPrefix = Utils.setAutoCompleteTextViewAdapter(mPhoneNumberPrefix, this);

        mActionBarTitle.setText(R.string.log_in_activity_title);

        mPhoneNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    clickNext();
                }
                return false;
            }
        });

        mPhoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() >3 ){
                    mNextButton.setBackground(getDrawable(R.drawable.start_button_selector));
                }else{
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

        mPrevTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //If this called after click change phone number at VerifyPhoneNumber screen
        if(getIntent().getStringExtra("sender")!= null && getIntent().getStringExtra("sender").equals(VerifyPhoneActivity.class.getSimpleName())){
            if(getIntent().getStringExtra("phoneNumber") != null) {
                mPhoneNumberEditText.setText(getIntent().getStringExtra("phoneNumber"));
            }
        }
    }

    @Override
    protected void clickNext() {
        super.clickNext();

        if(isEditTextsEmpty(new EditText[]{mPhoneNumberEditText},new String[]{getString(R.string.login_activity_phone_number_is_empty)})){
            hideNextSpinner();
            //TODO: Remove this after removing mNextImageView in base activity
          //  mNextImageView.setVisibility(View.INVISIBLE);
            return;
        }

        if(mPhoneNumberPrefix.getText().toString().trim().length() == 0){
            mPhoneNumberPrefix.setError(getString(R.string.please_enter_country_code));
            hideNextSpinner();
            //TODO: Remove this after removing mNextImageView in base activity
           // mNextImageView.setVisibility(View.INVISIBLE);
            return;
        }

        final String phoneNumber;

        //TODO: Change this, Check phone number validation - works for Israeli numbers only for now
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

            phoneNumber =  new StringBuilder(reversedPhoneNumber).reverse().toString();
        }else{
            phoneNumber =   mPhoneNumberEditText.getText().toString().replace(" ", "");
        }

        sendPhoneNumber(mPhoneNumberPrefix.getText().toString().trim(),phoneNumber, "null");
    }

    private void sendPhoneNumber(final String phoneNumberPrefix,final String phoneNumber,final String name) {

        String phone = phoneNumberPrefix + phoneNumber;
        KwikMe.sendValidationCode(phone, true, new SendValidationCodeListener() {
            @Override
            public void sendValidationCodeDone() {
                Intent i = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
                i.putExtra("sender", LoginActivity.class.getSimpleName());
                i.putExtra("phoneNumber", phoneNumber);
                i.putExtra("phoneNumberPrefix",phoneNumberPrefix);
                i.putExtra("name", name);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }

            @Override
            public void sendValidationCodeError(KwikServerError kwikServerError) {
                if (kwikServerError.getValue() == 404) {
                    hideNextSpinner();
                    showTwoButtonErrorDialog( getString( R.string.oops ), kwikServerError.getMessage(),
                            getResources().getString( R.string.sign_up_activity_title ),
                            getString( R.string.cancel ),

                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                                    i.putExtra("sender", LoginActivity.class.getSimpleName());
                                    i.putExtra("phoneNumber", phoneNumber);
                                    i.putExtra("phoneNumberPrefix",phoneNumberPrefix);
                                    startActivity(i);
                                    LoginActivity.this.finish();

                                }
                            },null);
                } else {
                    showOneButtonErrorDialog(getString(R.string.oops), kwikServerError.getMessage());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(LoginActivity.this,SplashActivity.class);
        startActivity(i);
        finish();
    }
}
