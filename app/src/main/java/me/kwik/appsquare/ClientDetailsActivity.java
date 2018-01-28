package me.kwik.appsquare;

import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.data.IpmClient;

public class ClientDetailsActivity extends BaseActivity {

    @BindView(R.id.client_details_activity_client_name_TextView)    TextView mClientNameTextView;
    @BindView(R.id.client_details_activity_client_email_TextView)   TextView mClientEmailTextView;
    @BindView(R.id.client_details_activity_address_TextView)        TextView mClientAddressTextView;
    @BindView(R.id.client_details_activity_contact_person_TextView) TextView mContactPersonTextView;
    @BindView(R.id.client_details_activity_phone_number_TextView)   TextView mPhoneNumberTextView;
    @BindView(R.id.client_details_activity_description_TextView)    TextView mDescriptionTextView;

    private Application mApp;
    private String                  mClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_details);
        mActionBarTitle.setText("Client details");
        ButterKnife.bind(this);
        mApp = (Application)getApplication();
        mClientId = getIntent().getStringExtra("client");

        if(mClientId == null || mClientId.trim().length() == 0){
            showOneButtonErrorDialog("","Unknown Error, please go back and try again");
        }else {

            IpmClient client = mApp.getClient(mClientId);

            mClientNameTextView.setText(client.getName());
            mClientEmailTextView.setText(client.getEmail());
            mClientAddressTextView.setText("No address for now");
            mContactPersonTextView.setText(client.getContactName());
            mPhoneNumberTextView.setText(client.getPhone());
            mDescriptionTextView.setText(client.getDescription());
        }
    }
}
