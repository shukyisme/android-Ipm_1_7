package me.kwik.square;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.DetailsElement;
import me.kwik.data.KwikAddress;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProject;
import me.kwik.data.MobileDevice;
import me.kwik.listeners.DetachUserListener;
import me.kwik.listeners.UpdateKwikButtonListener;
import me.kwik.utils.Logger;
import me.kwk.utils.ExpandableListAdapter;
import me.kwk.utils.Utils;


public class ButtonSettingsActivity extends BaseActivity {

    private final   String                          TAG = ButtonSettingsActivity.class.getSimpleName();
    private         String                          mButtonId;
    private         KwikButtonDevice                mButtonDevice;
    private         Application                     mApp;
    private         ExpandableListView              mPreferredDeliveryTimeListView;
    private         KwikProject                     mProject;
    private         Map<String,Object>              data = new HashMap<String, Object>();
    public          List<DetailsElement>            elements = new ArrayList<DetailsElement>();

    private         ExpandableListAdapter           listAdapter;
    private         List<String>                    listDataHeader;
    private         HashMap<String, List<String>>   listDataChild;
    private         ImageView                       brandImageView;
    private         EditText                        mLastFocusedEditText;
    private         ListView                        mAddressListView;
    private         ImageView                       mArrowIcon;
    private         int                             rotationAngle = 0;
    private         AddressAdapter                  addressesAdapter;
    private         ImageView                       mFinishEditingAddressImageView;
    private         ImageView                       threeDots;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_button_settings );

        mApp = (Application) getApplication();

        if (mApp == null) {
            return;
        }

        //Tool bar
        toolBar();

        //Get button id and show it, and init the button
        getButtonIdAndShowIt();

        //Get Project
        if (getTrapAndShowTrapName()) return;

        //ButtonImage
        buttonImage();



        //Check if project includes schema
        LinearLayout root = schema();

        //add delete button
        addDeleteButtonOption(root);


        //Hide keyboard
        this.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //name
        name();

        //description
        description();
        //addresses
        if(mProject.isHasAddress()){
            addAddresses();
            RelativeLayout addressLayout = (RelativeLayout)findViewById( R.id.address_RelativeLayout );
            addressLayout.setVisibility( View.VISIBLE );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.e(TAG,this.getClass().getSimpleName() + " "  + new Object(){}.getClass().getEnclosingMethod().getName());
    }

    private void addAddresses() {
        mArrowIcon = (ImageView)findViewById( R.id.address_list_view_arrow_imageButton );
        mFinishEditingAddressImageView = (ImageView)findViewById( R.id.finish_editing_image_view );
        mArrowIcon.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandList( v ,null);
            }
        } );

        mFinishEditingAddressImageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandList( null ,null);
            }
        } );

        if(addressesAdapter != null){
            addressesAdapter.notifyDataSetChanged();
        }

        if(mApp.getUser().getAddresses().size() <= 1){
            mArrowIcon.setVisibility( View.INVISIBLE );
        }else{
            mArrowIcon.setVisibility( View.VISIBLE );
        }

        //Popup menu
        threeDots = (ImageView) findViewById(R.id.three_dots_image_view);
        threeDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //custom dialog
                final Dialog dialog = new Dialog(ButtonSettingsActivity.this,R.style.PauseDialog);
                dialog.requestWindowFeature( Window.FEATURE_NO_TITLE);
                WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

                //Position
                wmlp.gravity = Gravity.TOP | Gravity.START;
                int[] location = new int[2];
                threeDots.getLocationInWindow(location);
                if(KwikMe.LOCAL.equals("he_IL")){
                    wmlp.x = location[0];
                }else{
                    wmlp.x = location[0] -300;
                }
                wmlp.y = location[1] -130 ;
                dialog.setContentView(R.layout.popupmenue_edit_add_address);


                LinearLayout buttonSettings = (LinearLayout)dialog.findViewById( R.id.button_settings_layout );
                buttonSettings.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       Intent i = new Intent( ButtonSettingsActivity.this,AddressActivity.class );
                        i.putExtra( "buttonId", mButtonId);
                        i.putExtra( "mode",AddressActivity.ADD_MODE );
                        startActivity( i );
                        dialog.dismiss();
                    }
                } );

                LinearLayout editReorder = (LinearLayout)dialog.findViewById( R.id.edit_reorder_layout );
                editReorder.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addressesAdapter.setShowEditIcon( true );
                        addressesAdapter.notifyDataSetChanged();
                        expandList( mArrowIcon, threeDots );
                        dialog.dismiss();
                    }
                } );


                dialog.show();
            }
        });



        if(addressesAdapter == null) {
            mAddressListView = (ListView) findViewById( R.id.address_ListView );
            mAddressListView.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });


            if(mApp.getUser().getAddresses().size() > 1 && mButtonDevice.getAddress() != null){
                int i =0;
                for(KwikAddress a:mApp.getUser().getAddresses()){
                    if(a.getId().equals( mButtonDevice.getAddress())){
                        if(i==0){
                            break;
                        }
                        KwikAddress temp = mApp.getUser().getAddresses().get( 0 );
                        mApp.getUser().getAddresses().set(0,a);
                        mApp.getUser().getAddresses().set( i,temp );

                    }
                    i++;
                }
            }
            addressesAdapter = new AddressAdapter( this, mApp.getUser().getAddresses(), false );
            mAddressListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final KwikAddress address = mApp.getUser().getAddresses().get( position );
                    mApp.getUser().getAddresses().remove( address );
                    mButtonDevice.setAddress( address.getId() );

                    KwikMe.updateKwikButtonDeviceWithListener( mButtonDevice, new UpdateKwikButtonListener() {
                        @Override
                        public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                            if (mApp.getButton( button.getId() ) == null) {
                                mApp.getButtons().add( button );
                            } else {
                                mApp.updateButton( button.getId(), button );
                            }
                            mApp.getUser().getAddresses().add( 0, address );
                            addressesAdapter.notifyDataSetChanged();
                            expandList( mArrowIcon,null );
                        }

                        @Override
                        public void updateKwikButtonListenerError(KwikServerError error) {
                            showOneButtonErrorDialog( getString( R.string.oops ), error.getMessage() );
                        }
                    } );
                }
            } );
            mAddressListView.setAdapter( addressesAdapter );
        }
    }

    private void expandList(View v, ImageView threeDotsImageView) {
        ObjectAnimator anim = ObjectAnimator.ofFloat( mArrowIcon, "rotation", rotationAngle, rotationAngle + 180 );
        anim.setDuration( 100 );
        anim.start();

        rotationAngle += 180;
        rotationAngle = rotationAngle%360;

        RelativeLayout rl = (RelativeLayout)findViewById( R.id.address_RelativeLayout );
        ViewGroup.LayoutParams params = rl.getLayoutParams();
        if(addressesAdapter != null){
            addressesAdapter.notifyDataSetChanged();
        }

        if(threeDotsImageView == null && v == null) {
            threeDots.setVisibility( View.VISIBLE );
            mFinishEditingAddressImageView.setVisibility( View.INVISIBLE );
            addressesAdapter.setShowEditIcon( false );
            params.height = Math.round(getResources().getDimension( R.dimen. address_tab_height));
        }else if(rotationAngle == 180 || (v != null && threeDotsImageView != null)) {
            if(threeDotsImageView != null) {
                threeDots.setVisibility( View.INVISIBLE );
                mFinishEditingAddressImageView.setVisibility( View.VISIBLE );
            }
            params.height = Math.round(getResources().getDimension( R.dimen. address_item_height)) +( (mApp.getUser().getAddresses().size()) * Math.round(getResources().getDimension( R.dimen. address_item_height)));
        }else{
            threeDots.setVisibility( View.VISIBLE );
            mFinishEditingAddressImageView.setVisibility( View.INVISIBLE );
            addressesAdapter.setShowEditIcon( false );
            params.height = Math.round(getResources().getDimension( R.dimen. address_tab_height));
        }

        rl.setLayoutParams(params);
    }

    private void name() {
            RelativeLayout paymentMethodRelativeLayout = (RelativeLayout)findViewById( R.id.paymentMethodRelativeLayout );
            TextView nameTextView = (TextView)findViewById( R.id.credit_card_details_TextView );
            if(mButtonDevice !=null && mButtonDevice.getName() != null){
                nameTextView.setVisibility( View.VISIBLE );
                String name = mButtonDevice.getName();
                nameTextView.setText( name );

            }else{
                nameTextView.setVisibility( View.GONE );
            }
            paymentMethodRelativeLayout.setVisibility( View.VISIBLE );
    }

    private void description() {
        RelativeLayout paymentMethodRelativeLayout = (RelativeLayout)findViewById( R.id.descriptionRelativeLayout );
        TextView descriptionTextView = (TextView)findViewById( R.id.description_TextView );
        if(mButtonDevice !=null && mButtonDevice.getName() != null){
            descriptionTextView.setVisibility( View.VISIBLE );
            String description = mButtonDevice.getDescription();
            descriptionTextView.setText( description );

        }else{
            descriptionTextView.setVisibility( View.GONE );
        }
        paymentMethodRelativeLayout.setVisibility( View.VISIBLE );
        TextView activateButtonComment = (TextView)findViewById(R.id.activate_button);
        if(mButtonDevice.getTriggerType() == null){
            activateButtonComment.setVisibility(View.VISIBLE);
        }else{
            activateButtonComment.setVisibility(View.GONE);
        }

    }

    public void editDescription(View v){
        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        final EditText edittext = new EditText(this);
        edittext.setSingleLine(false);
        edittext.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        edittext.setText(mButtonDevice.getDescription());
        alert.setTitle(R.string.button_settings_activity_description_dialog);

        alert.setView(edittext);

        alert.setPositiveButton(R.string.enter_serial_number_dialog_submit_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String newDescription = edittext.getText().toString();
                mButtonDevice.setDescription(newDescription);
                updateButton();
            }
        });

        alert.setNegativeButton(R.string.enter_serial_number_error_cancel_button, null);

        alert.show();

        //call update button
    }

    public void editName(View v){
        //get Name
        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        final EditText edittext = new EditText(this);
        edittext.setSingleLine(false);
        edittext.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        edittext.setText(mButtonDevice.getName());
        alert.setTitle(R.string.button_settings_activity_edit_name_dialog);

        alert.setView(edittext);

        alert.setPositiveButton(R.string.enter_serial_number_dialog_submit_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String newName = edittext.getText().toString();
                mButtonDevice.setName(newName);
                updateButton();

            }
        });

        alert.setNegativeButton(R.string.enter_serial_number_error_cancel_button, null);
        alert.show();

        //call update button
    }

    private LinearLayout schema() {
        Object formJsonObj = mProject.getForm();
        LinearLayout root = (LinearLayout) findViewById( R.id.content_button_settings2 );

        if (formJsonObj != null) {

            //Parse Json to elements
            List<DetailsElement> elements = null;

            String formText = null;
            try {
                Gson gson = new Gson();
                formText = gson.toJson( formJsonObj );
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            try {
                elements = parse( formText );
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            //Show elements
            for (DetailsElement element : elements) {

                // Creating a new RelativeLayout
                if (element != null && element.getType() != null) {
                    if (element.getType().equals ( DetailsElement.TYPE_STRING) || element.getType().equals( DetailsElement.TYPE_NUMBER)) {
                        createTextElementUI( root, element );
                    }else if(element.getType().equals ( DetailsElement.TYPE_BOOLEAN)){
                        createBooleanElementUI( root, element );
                    }
                }
            }

            //configure elements
            if(elements != null )
                this.elements = elements;
        }

        return root;
    }

    private void createBooleanElementUI(LinearLayout root, final DetailsElement element) {
        RelativeLayout relativeLayout = new RelativeLayout( this );

        // Defining the RelativeLayout layout parameters.
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                340);

        final int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackgroundDrawable( getResources().getDrawable( R.drawable.ripple_background ) );
        } else {
            relativeLayout.setBackground( getResources().getDrawable( R.drawable.ripple_background ) );
        }

        final CheckBox checkBox = addCheckBoxtAndValueIfExist( element );


        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT );
        if(element.getValue() != null){
            lp.addRule( RelativeLayout.ALIGN_PARENT_TOP );
            lp.setMargins(0, Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ),0,0 );
        }else {
            lp.addRule( RelativeLayout.CENTER_VERTICAL );
        }
        lp.setMarginStart( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );

        checkBox.setLayoutParams( lp );

        checkBox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked && element.isEditable()){

                    if(isChecked) {
                        element.setValue("true");
                    }else {
                        element.setValue("false");
                    }

                    for (DetailsElement element:elements){
                        data.put(element.getKey(),element.getValue());
                    }

                    try {
                        HashMap<String, Object> triggers = mButtonDevice.getTriggers();
                        triggers.put("click",data);
                        mButtonDevice.setTriggers(triggers);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    if(mButtonDevice.getDeliveryTimeSlots() != null && mButtonDevice.getDeliveryTimeSlots().getHourDescription() == ""){
                        mButtonDevice.setDeliveryTimeSlots(null);
                    }

                    updateButton();


                }
            }

        } );

        relativeLayout.addView( checkBox );

        // Setting the RelativeLayout as our content view
        root.addView( relativeLayout, rlp );

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams();
        params.topMargin = 30;
    }

    private void updateButton() {
        KwikMe.updateKwikButtonDeviceWithListener(mButtonDevice, new UpdateKwikButtonListener() {
            @Override
            public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                if(mApp.getButton(button.getId()) == null) {
                    mApp.getButtons().add(button);
                }else{
                    mApp.updateButton(button.getId(),button);
                }
                getTrapAndShowTrapName();
                //name
                name();

                //description
                description();
            }

            @Override
            public void updateKwikButtonListenerError(KwikServerError error) {
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Update kwik button")
                        .setValue(1)
                        .build());
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
                hideNextSpinner();
            }
        });
    }

    private CheckBox addCheckBoxtAndValueIfExist(DetailsElement element) {
        final CheckBox checkBox = new CheckBox(this);
        if(element.getValue() != null){
            if(element.getValue().equals( "true" )){
                checkBox.setChecked( true );
            }else{
                checkBox.setChecked( false );
            }
        }
        checkBox.setText( element.getName());
        checkBox.setTextSize( 18.0f );
        checkBox.setTextColor( Color.parseColor( "#747474" ) );
        return checkBox;
    }

    private void buttonImage() {
        brandImageView = (ImageView) findViewById( R.id.my_buttons_item_button_image_view );
        if(mButtonDevice == null){
            return;
        }
        if(mButtonDevice.getTriggerType() == null){
            brandImageView.setImageDrawable(getResources().getDrawable(R.drawable.ipm_square_yellow));
        }else {
            if (mButtonDevice.getTriggerType().equalsIgnoreCase("trigger1") || mButtonDevice.getTriggerType().equals("click")) {
                brandImageView.setImageDrawable(getResources().getDrawable(R.drawable.ipm_button_ready_icon));
            } else {
                brandImageView.setImageDrawable(getResources().getDrawable(R.drawable.ipm_good_job_logo));
            }
        }
    }

    private boolean getTrapAndShowTrapName() {

        if(mButtonId == null || mApp.getButton( mButtonId ) == null){
            return false;
        }

        mProject = mApp.getProject( (mApp.getButton( mButtonId )).getProject() );

        if (mProject == null) {
            return true;
        }

        //Get trap name and show it
        {
            String trapName = null;
            try {
                trapName = mApp.getButton( mButtonId ).getName();
            } catch (NullPointerException e) {
                //Do Nothing
            }
            TextView projectNameTextView = (TextView) findViewById( R.id.title );
            if (trapName != null) {

                projectNameTextView.setText( trapName );
            }else{
                projectNameTextView.setText("");
            }
        }
        return false;
    }

    private void getButtonIdAndShowIt() {
        try {
            mButtonId = getIntent().getStringExtra( "buttonId" );
            mButtonDevice = mApp.getButton( mButtonId );
        } catch (NullPointerException e) {
            //Do Nothing
        }

        if (mButtonId != null) {
            TextView buttonIdTextView = (TextView) findViewById( R.id.serial_number );
            buttonIdTextView.setText( getString( R.string.sereal_number ) + mButtonId );
        }
    }

    private void toolBar() {
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle( getResources().getString( R.string.button_settings_activity_main_label ) );
        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.show();
    }

    private void addDeleteButtonOption(LinearLayout root) {
        RelativeLayout relativeLayout = new RelativeLayout( this );

        // Defining the RelativeLayout layout parameters.
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                Math.round( Utils.convertDpToPixel( 90.0f, ButtonSettingsActivity.this ) ) );

        final int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackgroundDrawable( getResources().getDrawable( R.drawable.ripple_background ) );
        } else {
            relativeLayout.setBackground( getResources().getDrawable( R.drawable.ripple_background ) );
        }

        relativeLayout.setClickable( true );
        relativeLayout.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deleteButton( );
                return false;
            }
        } );

        //Creating icon
        ImageView iv = new ImageView( this );
        iv.setImageResource( R.drawable.delete_icon );
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT );
        lp.addRule( RelativeLayout.CENTER_VERTICAL );
        lp.setMarginStart( Math.round( Utils.convertDpToPixel( 20.0f, ButtonSettingsActivity.this ) ) );
        iv.setLayoutParams( lp );
        iv.getLayoutParams().height = 60;
        iv.getLayoutParams().width = 60;
        iv.setId( 1000 );
        relativeLayout.addView( iv );

        // Creating a new TextView
        TextView tv = new TextView( this );
        tv.setText( R.string.delete_button );
        tv.setTextSize( 18.0f );
        tv.setTextColor( Color.parseColor( "#747474" ) );

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT );
        lp1.addRule( RelativeLayout.CENTER_VERTICAL );
        lp1.addRule( RelativeLayout.END_OF, iv.getId() );
        lp1.setMarginStart( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );
        lp1.setMarginEnd( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );
        lp1.addRule( RelativeLayout.END_OF, iv.getId() );

        // Setting the parameters on the TextView
        tv.setLayoutParams( lp1 );

        // Adding the TextView to the RelativeLayout as a child
        relativeLayout.addView( tv );

        //add element value

        final TextView longClickTextView = new TextView(this);
        longClickTextView.setText( R.string.long_press_to_delete_the_button );
        longClickTextView.setTextSize( 16.0f );
        longClickTextView.setTextColor( Color.parseColor( "#A1AEBA" ) );

        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT );
        lp2.addRule( RelativeLayout.END_OF, iv.getId() );
        lp2.addRule( RelativeLayout.BELOW, iv.getId() );
        lp2.setMarginStart( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );
        lp2.setMarginEnd( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );
        lp2.setMargins(0, Math.round( Utils.convertDpToPixel( 5.0f, ButtonSettingsActivity.this ) ),0,0 );

        // Setting the parameters on the TextView
        longClickTextView.setLayoutParams( lp2 );

        relativeLayout.addView(longClickTextView);

        // Setting the RelativeLayout as our content view
        root.addView( relativeLayout, rlp );

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams();
        params.topMargin = 30;

    }

    private void createTextElementUI(LinearLayout root, final DetailsElement element) {
        RelativeLayout relativeLayout = new RelativeLayout( this );

        // Defining the RelativeLayout layout parameters.
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                340);

        final int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            relativeLayout.setBackgroundDrawable( getResources().getDrawable( R.drawable.ripple_background ) );
        } else {
            relativeLayout.setBackground( getResources().getDrawable( R.drawable.ripple_background ) );
        }

        //Creating icon
        ImageView iv = createIcon( element, relativeLayout );

        // Creating a new TextView
        createLabel( element, relativeLayout, iv );


        //add element value
        final EditText editText = addEditTextAndValueIfExist( element );

        editElementValue( element, iv, editText );

        //If element is not editable add lock icon
        addLockIconForNonEditableFields( element, relativeLayout, editText );

        // Setting the RelativeLayout as our content view
        root.addView( relativeLayout, rlp );

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams();
        params.topMargin = 30;

    }

    private void addLockIconForNonEditableFields(DetailsElement element, RelativeLayout relativeLayout, EditText editText) {
        if(!element.isEditable()){
            Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.lock, null);
            img.setBounds( 0, 0, 50, 60 );
            editText.setCompoundDrawablesRelative( img,null,null,null );

        }
        relativeLayout.addView(editText);
    }

    private void editElementValue(final DetailsElement element, ImageView iv, final EditText editText) {
        editText.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && element.isEditable()){
                    if(element.isValueValid() != null){
                        showOneButtonErrorDialog(getString( R.string.oops),element.isValueValid());
                        hideNextSpinner();
                        return;
                    }
                    for (DetailsElement element:elements){
                        data.put(element.getKey(),element.getValue());
                    }

                    try {
                        HashMap<String, Object> triggers = mButtonDevice.getTriggers();
                        triggers.put("click",data);
                        mButtonDevice.setTriggers(triggers);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    if(mButtonDevice.getDeliveryTimeSlots() != null && mButtonDevice.getDeliveryTimeSlots().getHourDescription() == ""){
                        mButtonDevice.setDeliveryTimeSlots(null);
                    }

                    updateButton();


                } else if(hasFocus && element.isEditable()){
                    mLastFocusedEditText = editText;
                }
            }
        } );


        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT );
        lp2.addRule( RelativeLayout.END_OF, iv.getId() );
        lp2.addRule( RelativeLayout.BELOW, iv.getId() );
        lp2.setMarginStart( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );
        lp2.setMarginEnd( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );
        lp2.setMargins(0, Math.round( Utils.convertDpToPixel( 10.0f, ButtonSettingsActivity.this ) ),0,0 );

        // Setting the parameters on the TextView
        editText.setLayoutParams( lp2 );
    }

    @NonNull
    private EditText addEditTextAndValueIfExist(final DetailsElement element) {
        final EditText editText = new EditText(this);
        editText.setSingleLine(false);
        editText.setImeOptions( EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        if(element.getValue() != null){
            if(element.getType().equals( DetailsElement.TYPE_NUMBER )){
                try {
                    if (Math.round( Float.valueOf( element.getValue() ) ) == Float.valueOf( element.getValue() )) {
                        editText.setText( "" + Math.round( Float.valueOf( element.getValue() ) ) );
                    } else {
                        editText.setText( element.getValue() );
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }

            }else {
                editText.setText( element.getValue() );
            }
        }

        if(!element.isEditable()){
            editText.setKeyListener(null);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                element.setValue(editText.getText().toString());
            }
        });
        return editText;
    }

    private void createLabel(DetailsElement element, RelativeLayout relativeLayout, ImageView iv) {
        TextView tv = new TextView( this );
        tv.setText( element.getName() );
        tv.setTextSize( 18.0f );
        tv.setTextColor( Color.parseColor( "#747474" ) );


        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT );
        if(element.getValue() != null){
            lp1.addRule( RelativeLayout.ALIGN_PARENT_TOP );
            lp1.setMargins(0, Math.round( Utils.convertDpToPixel( 14.0f, ButtonSettingsActivity.this ) ),0,0 );
        }else {
            lp1.addRule( RelativeLayout.CENTER_VERTICAL );
        }
        lp1.addRule( RelativeLayout.END_OF, iv.getId() );
        lp1.setMarginStart( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );
        lp1.setMarginEnd( Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ) );

        lp1.addRule( RelativeLayout.END_OF, iv.getId() );

        // Setting the parameters on the TextView
        tv.setLayoutParams( lp1 );

        // Adding the TextView to the RelativeLayout as a child
        relativeLayout.addView( tv );
    }

    @NonNull
    private ImageView createIcon(DetailsElement element, RelativeLayout relativeLayout) {
        ImageView iv = new ImageView( this );
        iv.setImageResource( R.drawable.order_history_drawer_icon );
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT );
        if(element.getValue() != null){
            lp.addRule( RelativeLayout.ALIGN_PARENT_TOP );
            lp.setMargins(0, Math.round( Utils.convertDpToPixel( 18.0f, ButtonSettingsActivity.this ) ),0,0 );
        }else {
            lp.addRule( RelativeLayout.CENTER_VERTICAL );
        }
        lp.setMarginStart( Math.round( Utils.convertDpToPixel( 20.0f, ButtonSettingsActivity.this ) ) );

        iv.setLayoutParams( lp );

        iv.getLayoutParams().height = 60;
        iv.getLayoutParams().width = 60;
        iv.setId( 1001 );

        relativeLayout.addView( iv );
        return iv;
    }

    private List<DetailsElement> parse(String jsonLine) {
        List<DetailsElement> elements = new ArrayList<DetailsElement>();
        JsonElement jelement = new JsonParser().parse( jsonLine );
        JsonObject jobject = jelement.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = jobject.entrySet();
        Map<String, Object> personalDetails = null;
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            DetailsElement elemnt = new DetailsElement();
            JsonElement jsonElemnt = entry.getValue();
            JsonObject ob = jsonElemnt.getAsJsonObject();

            if (entry.getKey() != null) {
                elemnt.setKey( entry.getKey() );
                try {
                    HashMap<String, Object> triggers = mButtonDevice.getTriggers();
                    personalDetails = (Map<String, Object>) triggers.get( "click" );
                    Iterator it = personalDetails.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        if (entry.getKey().equals( pair.getKey() )) {
                            elemnt.setValue( String.valueOf( pair.getValue() ) );
                        }
                    }
                } catch (NullPointerException e) {
                    Logger.e( TAG, "parse %s", e.getMessage() );
                }
            }

            if (ob.get( "name" ) != null) {
                elemnt.setName( ob.get( "name" ).getAsString() );
            }
            if (ob.get( "isEditable" ) != null) {
                if (Boolean.valueOf( ob.get( "isEditable" ).toString() )) {
                    elemnt.setIsEditable( true );
                } else {
                    elemnt.setIsEditable( false );
                }
            }

            if (ob.get( "maxLength" ) != null) {
                elemnt.setMaxLength( ob.get( "maxLength" ).getAsInt() );
            }

            if (ob.get( "minLength" ) != null) {
                elemnt.setMinLength( ob.get( "minLength" ).getAsInt() );
            }

            if (ob.get("type") != null){
                elemnt.setType( ob.get ("type").getAsString() );
            }
            elements.add( elemnt );
        }

        return elements;
    }

    //Override for implementing the back button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(mLastFocusedEditText != null) {
                    mLastFocusedEditText.clearFocus();
                    brandImageView.requestFocus();
                }
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }
    }

    //Add new network to button
    public void addNewNetwork(View addNetWorkLayout) {
        if(Utils.checkVolumeLevel(ButtonSettingsActivity.this) != null){
            return;
        }
        Intent i = new Intent( ButtonSettingsActivity.this, NetworkPasswordActivity.class );
        i.putExtra( "sender", ButtonSettingsActivity.class.getSimpleName() );
        startActivity( i );
    }

    //User asked to delete a button
    public void deleteButton() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder( ButtonSettingsActivity.this, R.style.KwikMeDialogTheme );
        } else {
            builder = new AlertDialog.Builder( ButtonSettingsActivity.this );
        }
        builder.setTitle( R.string.remove_kwik_button )
                .setMessage( R.string.remove_kwik_button_message )
                .setNegativeButton( getString( R.string.yes ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeButton( mButtonId );
                    }

                } )
                .setPositiveButton( getString( R.string.no ) + "!", null )
                .show();
    }

    private void removeButton(final String buttonId) {
        KwikMe.detachUser( buttonId, new DetachUserListener() {
            @Override
            public void detachUserDone() {
                mApp.removeButton( buttonId );
                finish();
            }

            @Override
            public void detachUserError(KwikServerError error) {

            }
        } );
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        KwikProject project;

        public DownloadImageTask(ImageView bmImage, KwikProject project) {
            this.bmImage = bmImage;
            this.project = project;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;

            try {
                InputStream in = new java.net.URL( urldisplay ).openStream();
                mIcon11 = BitmapFactory.decodeStream( in );
            } catch (Exception e) {
                e.printStackTrace();
            }

            String fileName = Application.IMAGES_FOLDER_NAME + "/" + project.getLogos().get( MobileDevice.DENSITY );
            File sdCardDirectory = Environment.getExternalStorageDirectory();
            File image = new File( sdCardDirectory, fileName );
            FileOutputStream outStream;
            try {

                outStream = new FileOutputStream( image );
                mIcon11.compress( Bitmap.CompressFormat.PNG, 100, outStream );

                outStream.flush();
                outStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap( result );
        }
    }
}
