package me.kwik.square;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikAddress;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProject;
import me.kwik.listeners.AddAddressToUserListener;
import me.kwik.listeners.GetBillingListener;
import me.kwik.listeners.UpdateKwikButtonListener;
import me.kwik.listeners.UpdateKwikUserAddressListener;
import me.kwik.rest.responses.GetBillingResponse;
import me.kwik.rest.responses.UpdateKwikUserAddressResponse;
import me.kwik.utils.Logger;

public class AddressActivity extends BaseActivity {

    private static final String            TAG = AddressActivity.class.getSimpleName();

    public static final      String            ADD_MODE   = "add";
    public static final      String            EDIT_MODE  = "edit";
    public static final      String            FIRST_FLOW = "firstFlow";

    private         Application                     mApp;
    private         String                          mButtonId;
    private         KwikButtonDevice                mButtonDevice;
    private         TextView                        mAddressText;
    private         KwikAddress                     mNewAddress;
    private         Button                          mNextButton;
    private         KwikProject                     mProject;
    private         EditText                        mApartmentNumberEditText;
    private         EditText                        mEntranceNumberEditText;
    private         EditText                        mStreetNumberEditText;
    private         ProgressBar                     mProgressBar;
    private         ProgressBar                     mGoogleMapProgressBar;
    private         EditText                        mAddANoteEditText;
    private         CheckBox                        mLeaveByTheDoorCheckBox;
    private String mode = ADD_MODE;
    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_address );

        mApp = (Application) getApplication();
        mAddressText = (TextView)findViewById( R.id.add_address_sub_title_TextView );
        mNextButton = (Button)findViewById( R.id.nextButton );
        mApartmentNumberEditText = (EditText)findViewById( R.id.apartment_number_EditText );
        mEntranceNumberEditText = (EditText)findViewById( R.id.entrance_number_EditText );
        mStreetNumberEditText = (EditText)findViewById( R.id.street_number_EditText );
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mGoogleMapProgressBar = (ProgressBar) findViewById(R.id.google_map_progress_bar);
        mAddANoteEditText = (EditText)findViewById( R.id.add_a_note_EditText );
        mLeaveByTheDoorCheckBox = (CheckBox)findViewById( R.id.leave_by_the_door_CheckBox );

        if (mApp == null) {
            return;
        }

        getButtonId();

        toolBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleMapProgressBar.setVisibility( View.GONE );
    }

    //Override for implementing the back button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void toolBar() {
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        try {
            this.mode = getIntent().getStringExtra( "mode" );
        } catch (NullPointerException e) {
            //Do Nothing
        }

        if(this.mode == null){
            this.mode = ADD_MODE;
        }

        if(this.mode.equals( AddressActivity.ADD_MODE )){
            actionBar.setTitle("Add new shipping address");
            mNextButton.setClickable( false );
            mNextButton.setBackgroundColor( getResources().getColor(R.color.kwik_me_smart_network_Gray_Disabled));
        }else if(this.mode.equals( AddressActivity.EDIT_MODE )){
            actionBar.setTitle("Edit shipping address");
            addAddressFields();
        }else if(this.mode.equals( AddressActivity.FIRST_FLOW )){
            actionBar.setTitle("Shipping address");
            mNextButton.setText("CONTINUE");
            mNextButton.setClickable( false );
            mNextButton.setBackgroundColor( getResources().getColor(R.color.kwik_me_smart_network_Gray_Disabled));
        }
        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.show();
    }

    private void addAddressFields() {
        String addressId;
        try {
            addressId = getIntent().getStringExtra( "addressId" );
        } catch (NullPointerException e) {
            return;
        }

        if(addressId == null){
            return;
        }

        List<KwikAddress> addresses = mApp.getUser().getAddresses();
        KwikAddress address = null;

        for(KwikAddress a:addresses){
            if(a.getId().equals( addressId )){
                address = a;
                break;
            }
        }

        if(mNewAddress == null){
            mNewAddress = new KwikAddress();
        }

        if(address != null) {
            mAddressText.setText( address.getDisplayText() );
            mNewAddress.setDisplayText( address.getDisplayText() );
            String apartmentNumber = address.getApt();
            if(apartmentNumber != null){
                mApartmentNumberEditText.setText( apartmentNumber );
                mNewAddress.setApt( apartmentNumber );
            }

            String entranceNumber = address.getEntrance();
            if(entranceNumber != null){
                mEntranceNumberEditText.setText( entranceNumber );
                mNewAddress.setEntrance( entranceNumber );
            }

            String streetNumber = address.getStreetNumber();
            if(streetNumber != null){
                mStreetNumberEditText.setText( streetNumber );
                mNewAddress.setStreetNumber( streetNumber );
            }

            String googlePlaceId = address.getGooglePlaceId();
            if(googlePlaceId != null){
                mNewAddress.setGooglePlaceId( googlePlaceId );
            }

            String note = address.getDeliveryComment();
            if(note != null){
                mAddANoteEditText.setText( note );
            }

            if(address.isLeaveByTheDoor()){
                mLeaveByTheDoorCheckBox.setChecked( true );
            }else{
                mLeaveByTheDoorCheckBox.setChecked( false );
            }
        }

    }

    public void addAddress(View v){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        mGoogleMapProgressBar.setVisibility( View.VISIBLE );

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void sendUpdateButton(KwikButtonDevice button) {
        final String mode = this.mode;
        KwikMe.updateKwikButtonDeviceWithListener(button, new UpdateKwikButtonListener() {
            @Override
            public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                if (mApp.getButton( button.getId() ) == null) {
                    mApp.getButtons().add( button );
                } else {
                    mApp.updateButton( button.getId(), button );
                }

                if(mode.equals( FIRST_FLOW )) {
                    Intent i = null;
                    if (mProject.getForm() != null) {
                        i = new Intent( AddressActivity.this, PersonalDetailsActivity.class );
                    } else if (mProject.getDeliveryTimeSlots() != null && mProject.getDeliveryTimeSlots().size() > 0) {
                        i = new Intent( AddressActivity.this, DeliveryActivity.class );
                    } else if (mProject.isHasPayment()) {
                        paymentMethod();
                        return;
                    } else {
                        i = new Intent( AddressActivity.this, ReorderSettingsActivity.class );
                    }

                    i.putExtra( "buttonId", mButtonId );
                    i.putExtra( "sender", LoginActivity.class.getSimpleName() );
                    startActivity( i );
                    finish();
                }else{
                    finish();
                }
            }

            @Override
            public void updateKwikButtonListenerError(KwikServerError error) {
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
            }
        });
    }

    private void paymentMethod() {
        KwikMe.getBillingUrl(mButtonId, new GetBillingListener() {
            @Override
            public void getBillingDone(GetBillingResponse response) {
                Intent i = new Intent(AddressActivity.this, PaymentWebViewActivity.class);

                String url = response.getBillingUrl();
                if (url == null || url.length() == 0) {
                    Logger.e(TAG, "loading url is null");
                    return;
                }

                String redirectUrl = response.getBillingRedirectUrl();
                if (redirectUrl == null || redirectUrl.length() == 0) {
                    Logger.e(TAG, "redirect url is null");
                    return;
                }

                i.putExtra(PaymentWebViewActivity.LOADING_URL, url);
                i.putExtra(PaymentWebViewActivity.REDIRECT_URL, redirectUrl);
                i.putExtra("buttonId", mButtonId);
                i.putExtra( "sender", SelectProductActivity.class.getSimpleName() );
                startActivity(i);
            }

            @Override
            public void getBillingError(KwikServerError error) {
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
            }
        });
    }

    private void getButtonId() {
        try {
            mButtonId = getIntent().getStringExtra( "buttonId" );
            mButtonDevice = mApp.getButton( mButtonId );
        } catch (NullPointerException e) {
            //Do Nothing
        }

        if (mButtonDevice != null) {
            mProject = mApp.getProject( mButtonDevice.getProject() );
        }
    }

    public void next(View v){
        try{
            String apartment = mApartmentNumberEditText.getText().toString();
            mNewAddress.setApt( apartment );
        }catch (NullPointerException e){

        }

        try{
            String streetNumber = mStreetNumberEditText.getText().toString();
            mNewAddress.setStreetNumber( streetNumber );
        }catch (NullPointerException e){

        }

        try{
            String streetNumber = mStreetNumberEditText.getText().toString();
            mNewAddress.setStreetNumber( streetNumber );
        }catch (NullPointerException e){

        }


        try{
            String entrance = mEntranceNumberEditText.getText().toString();
            mNewAddress.setEntrance( entrance );
        }catch (NullPointerException e){

        }

        try{
            String note = mAddANoteEditText.getText().toString();
            mNewAddress.setDeliveryComment( note );
        }catch (NullPointerException e){

        }

        mNewAddress.setLeaveByTheDoor( mLeaveByTheDoorCheckBox.isChecked());

        if(this.mode.equals( EDIT_MODE )){
            updateUserAddress(mNewAddress);
        }else if(this.mode.equals( ADD_MODE ) || this.mode.equals( FIRST_FLOW )){
            addAddressToUser(mNewAddress);
        }

    }

    private void updateUserAddress(KwikAddress mNewAddress) {

        String addressId;
        try {
            addressId = getIntent().getStringExtra( "addressId" );
        } catch (NullPointerException e) {
            return;
        }

        if(addressId == null){
            return;
        }



        if(mNewAddress == null){
            mNewAddress = new KwikAddress(  );
        }
        mNewAddress.setId( addressId );
        mProgressBar.setVisibility(View.VISIBLE);
        KwikMe.updateKwikUserAddress( mNewAddress, mApp.getUser().getId(), new UpdateKwikUserAddressListener() {
            @Override
            public void updateKwikUserAddressDone(UpdateKwikUserAddressResponse response) {
                KwikAddress updatedAddress = response.getAddressObject();
                int index = 0;
                for(KwikAddress a:mApp.getUser().getAddresses()){

                    if(a.getId().equals( updatedAddress.getId() )){
                        break;
                    }
                    index++;
                }

                mApp.getUser().getAddresses().set( index,updatedAddress );
                mProgressBar.setVisibility(View.GONE);
                finish();
            }

            @Override
            public void updateKwikUserAddressError(KwikServerError error) {
                mProgressBar.setVisibility(View.GONE);
                showOneButtonErrorDialog( getString( R.string.oops ), error.getMessage() );
            }
        } );
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                mAddressText.setText( place.getAddress().toString() );
                mNewAddress = new KwikAddress(place.getId(), place.getName().toString());
                mNextButton.setClickable( true );
                mNextButton.setBackgroundColor( getResources().getColor(R.color.kwik_me_orange));
            }
        }
    }

    private void addAddressToUser(final KwikAddress addressToSend) {
        mProgressBar.setVisibility(View.VISIBLE);
        KwikMe.addAddressToUser( addressToSend, mApp.getUser().getId(), new AddAddressToUserListener() {
                    @Override
                    public void addAddressToUserDone(KwikAddress address) {
                        address.setDisplayText( addressToSend.getDisplayText() );
                        mApp.getUser().getAddresses().add(address);
                        mButtonDevice.setAddress(address.getId());
                        sendUpdateButton( mButtonDevice );
                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void addAddressToUserError(KwikServerError error) {
                        mProgressBar.setVisibility(View.GONE);
                        showOneButtonErrorDialog(getString(R.string.oops),error.getMessage());
                    }
                });
    }
}
