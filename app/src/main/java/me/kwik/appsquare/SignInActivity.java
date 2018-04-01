package me.kwik.appsquare;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.listeners.LoginListener;
import me.kwik.rest.responses.LoginResponse;
import me.kwk.utils.Utils;

public class SignInActivity extends BaseActivity {

    private static int PASSWORD_MIN_LENGTH = 6;
    private View.OnClickListener mOnSignInClickListener;
    private String mEmailString;
    private String mPasswordString;

    @BindView(R.id.signin_user_name_edit_text)EditText mEmailEditText;
    @BindView(R.id.signin_password_edit_text) EditText mPasswordEditText;
    @BindView(R.id.sigin_sign_in_button)Button mLoginButton;

    private Application mApp;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        mApp = (Application) getApplication();
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mOnSignInClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEmailString == null || mEmailString.length() == 0){
                    mEmailEditText.setError("Please fill the required fields");
                    return;
                }


                if(!Utils.isValidEmail(mEmailString)){
                    mEmailEditText.setError("Not Valid Email");
                    return;
                }

                if(mPasswordString == null || mPasswordString.length() < PASSWORD_MIN_LENGTH){
                    if(mPasswordString == null || mPasswordString.length() == 0){
                        mPasswordEditText.setError("Please fill the required fields");
                    }else {
                        mPasswordEditText.setError("Not Valid Password");
                    }
                    return;
                }
                showProgressBar();
                KwikMe.login(mEmailString, mPasswordString, new LoginListener() {

                    @Override
                    public void onLoginCompleted(LoginResponse response) {
                        hideProgressBar();
                        mApp.setUser(response.getUser());
                        Intent i = new Intent(SignInActivity.this,ClientsActivity.class);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onLoginError(KwikServerError error) {
                        hideProgressBar();
                        showErrorDialog(error);
                    }
                });


            }
        };

        mLoginButton.setOnClickListener(mOnSignInClickListener);

        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mEmailString = s.toString().trim();
            }
        });

        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mPasswordString = s.toString().trim();
            }
        });
    }
}
