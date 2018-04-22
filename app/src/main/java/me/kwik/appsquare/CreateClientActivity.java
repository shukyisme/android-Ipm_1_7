package me.kwik.appsquare;

import android.app.Activity;
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
import me.kwik.data.IpmClient;
import me.kwik.listeners.CreateNewClientListener;
import me.kwk.utils.Utils;

public class CreateClientActivity extends BaseActivity {

    private static int CLIENT_NAME_MIN_LENGTH = 2;

    private String mClientNameString;
    private String mEmailString;
    private String mClientAddress;
    private String mContactPerson;
    private String mPhoneNumber;
    private String mDescription;


    @BindView(R.id.create_client_activity_client_name_EditText)     EditText mClientNameEditText;
    @BindView(R.id.create_client_activity_client_email_EditText)    EditText mClientEmailEditText;
    @BindView(R.id.create_client_activity_client_address_EditText)  EditText mClientAddressEditText;
    @BindView(R.id.create_client_activity_contact_person_EditText)  EditText mContactPersonEditText;
    @BindView(R.id.create_client_activity_phone_number_EditText)    EditText mPhoneNumberEditText;
    @BindView(R.id.create_client_activity_description_EditText)     EditText mDescriptionEditText;
    @BindView(R.id.create_client_activity_create_button_Button)     Button mCreateClientButton;


    private Application mApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_client);
        ButterKnife.bind(this);
        mApp = (Application) getApplication();
        mActionBarTitle.setText("Create New Client");

        mPhoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPhoneNumber = s.toString().trim();
            }
        });
        mDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mDescription = s.toString().trim();
            }
        });
        mContactPersonEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mContactPerson = s.toString().trim();
            }
        });

        mClientNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mClientNameString = s.toString().trim();
            }
        });

        mClientEmailEditText.addTextChangedListener(new TextWatcher() {
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

        mCreateClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClientNameEditText.setBackgroundResource(R.drawable.rectangle_2_vd);
                mClientEmailEditText.setBackgroundResource(R.drawable.rectangle_2_vd);
                boolean hasErrors = false;
                if(mClientNameString == null || mClientNameString.length() == 0){
                    mClientNameEditText.setError(getString(R.string.create_client_activity_msg_required));
                    mClientNameEditText.setBackgroundResource(R.drawable.rectangle_2_vd_red);
                    mClientNameEditText.requestFocus();
                    hasErrors = true;
                } else if(mClientNameString.length() < CLIENT_NAME_MIN_LENGTH) {
                    mClientNameEditText.setError(getString(R.string.create_client_activity_msg_name_invalid));
                    mClientNameEditText.setBackgroundResource(R.drawable.rectangle_2_vd_red);
                    mClientNameEditText.requestFocus();
                    hasErrors = true;
                }

                if(mEmailString == null || mEmailString.length() == 0){
                    mClientEmailEditText.setError(getString(R.string.create_client_activity_msg_required));
                    mClientEmailEditText.setBackgroundResource(R.drawable.rectangle_2_vd_red);
                    if(!hasErrors) {
                        mClientEmailEditText.requestFocus();
                    }
                    hasErrors = true;
                } else if(!Utils.isValidEmail(mEmailString)){
                    mClientEmailEditText.setError(getString(R.string.create_client_activity_msg_email_invalid));
                    mClientEmailEditText.setBackgroundResource(R.drawable.rectangle_2_vd_red);
                    if(!hasErrors) {
                        mClientEmailEditText.requestFocus();
                    }
                    hasErrors = true;
                }

                if(hasErrors) {
                    return;
                }

                showProgressBar();
                KwikMe.createNewClient(mApp.getUser().getCompany(), mClientNameString, mEmailString,mClientAddress,mContactPerson,mPhoneNumber,mDescription, new CreateNewClientListener() {
                    @Override
                    public void createNewClientDone(IpmClient client) {
                        hideProgressBar();
                        mApp.getmClients().add(client);
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("client",client.getId());
                        returnIntent.putExtra("name",mClientNameString);

                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }

                    @Override
                    public void createNewClientError(KwikServerError error) {
                        hideProgressBar();
                        showErrorDialog(error);
                    }
                });

            }
        });
    }
}
