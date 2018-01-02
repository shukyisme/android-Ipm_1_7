package me.kwik.square;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikAddress;
import me.kwik.data.KwikUser;
import me.kwik.listeners.GetKwikUserListener;
import me.kwik.listeners.UpdateKwikUserListener;


public class UpdatePersonalDetailsActivity extends BaseActivity {

    private ListView mAddressListView;
    private AddressArrayAdapter mAddressArrayAdapter;
    private Application mApp;
    private int PLACE_PICKER_REQUEST = 0 ;
    private String userFirstNameOnPageLoading;
    private String userLastNameOnPageLoading;
    private String userMailOnPageLoading;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mEmailEditText;
    private EditText mPhoneEditText;
    private static boolean CHANGE_ADDRESS_TEMPORARY = false;
//    private Place place;
    private String entrance;
    private String apt;
    private String mEditedFirstName;
    private String mEditedLastName;

    private String mEditedEmail;
    private  int i = 0;
    //Address List in loading the screen, To Check changes after leaving the screen
    private  List<KwikAddress> mAddressesOnPageLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_personal_details);

        mApp = (Application)getApplication();
       // mNextImageView.setText(getString(R.string.save));


        mFirstNameEditText = (EditText)findViewById(R.id.update_personal_details_activity_name_edit_text);
        mLastNameEditText = (EditText)findViewById(R.id.update_personal_details_activity_last_name_edit_text);
        mPhoneEditText = (EditText)findViewById(R.id.update_personal_details_activity_phone_edit_text);
        mEmailEditText = (EditText)findViewById(R.id.update_personal_details_activity_email_edit_text);

        EditText buttonsEditText = (EditText)findViewById(R.id.update_personal_details_activity_buttons_edit_text);

        String buttonsId = "";
        if(mApp.getButtons() != null) {
            for (int i = 0; i < mApp.getButtons().size(); i++) {


                if(i ==  mApp.getButtons().size() -1){
                    buttonsId += mApp.getButtons().get(i).getId() + " ";
                }else{
                    buttonsId += mApp.getButtons().get(i).getId() + ", ";
                }
            }
            buttonsEditText.setText(buttonsId);
        }

        mLastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s != null){
                    mEditedLastName = s.toString();
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
                if(s != null){
                    mEditedFirstName = s.toString();
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
                if(s != null) {
                    mEditedEmail = s.toString();
                }
            }
        });

        mActionBarTitle.setText(R.string.update_personal_details_title);
        mAddressListView = (ListView) findViewById(R.id.update_personal_details_activity_address_list_view);
    }

    @Override
    protected void clickNext() {

        String name = null;
        String email = null;
        String lastName = null;
        try {
             name = mFirstNameEditText.getText().toString();
             email = mEmailEditText.getText().toString();
             lastName = mLastNameEditText.getText().toString();

        }catch (NullPointerException e){
            //Do nothing
        }



        if( (userFirstNameOnPageLoading != null  && !userFirstNameOnPageLoading.equals(name))
                || (email != null && !email.equals(userMailOnPageLoading))
                || (lastName != null && !lastName.equals(userLastNameOnPageLoading))){
            super.clickNext();
            mApp.getUser().setFirstName(name);
            mApp.getUser().setLastName(lastName);
            mApp.getUser().setEmail(email);
            KwikMe.updateKwikUser(mApp.getUser(), new UpdateKwikUserListener() {
                @Override
                public void updateKwikUserDone(KwikUser user) {
                    hideNextSpinner();
                    changeAddress();
                }

                @Override
                public void updateKwikUserError(KwikServerError error) {
                    showOneButtonErrorDialog(getString(R.string.oops),error.getMessage());
                    hideNextSpinner();
                }
            });
        }else {
            changeAddress();
        }
    }


    private void changeAddress() {
//        //Send change user address only if there is any address changed.
//        if (mAddressesOnPageLoading != null && !mAddressesOnPageLoading.equals(mApp.getUser().getAddresses())) {
//            i=0;
//            for(final KwikAddress address:mApp.getUser().getAddresses()) {
//                super.clickNext();
//
//                if(!mAddressesOnPageLoading.contains(address)) {
//                    KwikMe.updateKwikUserAddress(address, mApp.getUser().getId(), new UpdateKwikUserAddressListener() {
//                        @Override
//                        public void updateKwikUserAddressDone() {
//                            hideNextSpinner();
//                            i++;
//                        }
//
//                        @Override
//                        public void updateKwikUserAddressError(KwikServerError error) {
//                            hideNextSpinner();
//                            showOneButtonErrorDialog(getString(R.string.oops), getString(R.string.address) + " " + (++i) + ": \n" + error.getMessage());
//                        }
//                    });
//                }
//            }
//        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                place = PlacePicker.getPlace(this, data);
//
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        showProgressBar();
        if (KwikMe.USER_TOKEN != null || KwikMe.USER_ID != null) {
            KwikMe.getKwikUser(KwikMe.USER_ID, new GetKwikUserListener() {
                @Override
                public void getKwikUserDone(KwikUser user) {
                    hideProgressBar();
                    if(user == null){
                        return;
                    }
                    mApp.setUser(user);
                    if(mApp.getUser().getAddresses() != null && mApp.getUser().getAddresses().size() > 0) {

                        //Init the list of addreess before changes, To compare it with the changes after clicking next
                        mAddressesOnPageLoading = new ArrayList<KwikAddress>(mApp.getUser().getAddresses().size());
                        for(KwikAddress address: mApp.getUser().getAddresses()) {
                            if(!mAddressesOnPageLoading.contains(address)) {
                                KwikAddress a = new KwikAddress(address);
                                mAddressesOnPageLoading.add(a);
                            }
                        }
                        //Adding header to the product list
                        TextView textView = new TextView(UpdatePersonalDetailsActivity.this);
                        textView.setText(R.string.addresses);
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextSize(16);
                        textView.setTypeface(null, Typeface.BOLD);
                        textView.setPadding(0, 0, 0, 30);
                        if(mAddressListView.getHeaderViewsCount() == 0) {
                            mAddressListView.addHeaderView(textView);
                        }


                        mAddressArrayAdapter = new AddressArrayAdapter(UpdatePersonalDetailsActivity.this, mApp.getUser().getAddresses());
                    }
                    try {
                        mAddressListView.setAdapter(mAddressArrayAdapter);
                    }catch (NullPointerException e){
                        //Do nothing
                    }
                    try {
                        if(mEditedFirstName != null){
                            mFirstNameEditText.setText(mEditedFirstName);
                        }else {
                            mFirstNameEditText.setText(mApp.getUser().getFirstName());
                        }
                    }catch (NullPointerException e){
                        //Do nothing
                    }

                    try {
                        if(mEditedLastName != null){
                            mLastNameEditText.setText(mEditedLastName);
                        }else {
                            mLastNameEditText.setText(mApp.getUser().getLastName());
                        }
                    }catch (NullPointerException e){
                        //Do nothing
                    }

                    try {
                        mPhoneEditText.setText(mApp.getUser().getPhone());
                    }catch (NullPointerException e){
                        // Do nothing
                    }
                    try {
                        if(mEditedEmail != null){
                            mEmailEditText.setText(mEditedEmail);
                        }else {
                            mEmailEditText.setText(mApp.getUser().getEmail());
                        }
                    }catch (NullPointerException e){
                        //Do nothing
                    }

                    try {
                        userFirstNameOnPageLoading = mFirstNameEditText.getText().toString();
                        userMailOnPageLoading = mEmailEditText.getText().toString();
                        userLastNameOnPageLoading = mLastNameEditText.getText().toString();
                    }catch (NullPointerException e){
                        //Do nothing
                    }

                }

                @Override
                public void getKwikUserError(KwikServerError error) {
                    hideProgressBar();
                }
            });
        }

    }

    public class AddressArrayAdapter extends ArrayAdapter<KwikAddress>{

        private Context context;
        private List<KwikAddress> address;
        public AddressArrayAdapter(Context context, List<KwikAddress> address){
            super(context, -1, address);
            this.context = context;
            this.address = address;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.address_list_view_item, parent, false);



            EditText addressString = (EditText)rowView.findViewById(R.id.update_personal_details_activity_address_string_edit_text);

            TextView label = (TextView)rowView.findViewById(R.id.update_personal_details_activity_address_label_text_view);

            label.setText(getString(R.string.address) + " " + (position +1) + ":");


//            addressString.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//                    place = null;
//                    PLACE_PICKER_REQUEST = position;
//                    try {
//                        startActivityForResult(builder.build(UpdatePersonalDetailsActivity.this), PLACE_PICKER_REQUEST);
//                    } catch (GooglePlayServicesRepairableException e) {
//                        e.printStackTrace();
//                    } catch (GooglePlayServicesNotAvailableException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });

            EditText entranceEditText = (EditText) rowView.findViewById(R.id.update_personal_details_activity_entrance_edit_text);
            EditText apartmentEditText = (EditText)rowView.findViewById(R.id.update_personal_details_activity_apartment_edit_text);

            String message = "";
            KwikAddress address = null;

            try {
                address = this.address.get(position);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            if (address != null) {

                if (address.getDisplayText() != null) {
                    message += address.getDisplayText();
                } else {

                    if (address.getState() != null) {
                        message += address.getState();
                    }
                    if (address.getCity() != null) {
                        message += ", " + address.getCity();
                    }
                    if (address.getStreet() != null) {
                        message += ", " + address.getStreet();
                    }
                    if (address.getStreetNumber() != null) {
                        message += ", " + address.getStreetNumber();
                    }
                }
            }
            addressString.setText(message);


            final KwikAddress finalAddress = address;
            entranceEditText.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    finalAddress.setEntrance(s.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if(s != null) {
                        entrance = s.toString();
                    }

                }
            });

            apartmentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    finalAddress.setApt(s.toString());
                    if(s != null) {
                        apt = s.toString();
                    }
                }
            });



            if (address.getEntrance() != null) {
                entranceEditText.setText(address.getEntrance());
            }
            if (address.getApt() != null) {
               apartmentEditText.setText(address.getApt());
            }

            if(entrance!= null){
                entranceEditText.setText(entrance);
                entrance= "";

            }

            if(apt != null){
                apartmentEditText.setText(apt);
                apt = "";
            }

            return rowView;
        }
    }
}
