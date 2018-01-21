package me.kwik.square;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.IpmClient;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProduct;
import me.kwik.data.KwikProject;
import me.kwik.data.KwikSelectedProduct;
import me.kwik.data.MobileDevice;
import me.kwik.listeners.GetClientsListener;
import me.kwik.listeners.GetKwikDevicesListener;
import me.kwik.rest.responses.GetClientsResponse;
import me.kwik.rest.responses.GetKwikDevicesResponse;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;


public class ClientsActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Application mApp;
    private String TAG = ClientsActivity.class.getSimpleName();
    private NavigationView mNavigationView;
    private boolean registering = false;
    android.support.v7.app.ActionBar actionBar;
    int i = 0;

    @BindView(R.id.clients_activity_clients_ListView) ListView mClientsList;
    @BindView(R.id.clients_activity_clients_list_header_TextView) TextView mClientsHeaderTextView;
    @BindView(R.id.clients_activity_overview_total_traps_TextView) TextView mTotalTrapsTextView;
    @BindView(R.id.clients_activity_alert_traps_TextView) TextView mTotalAlertTrapsTextView;


    private ArrayAdapter<IpmClient> mClientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_my_kwik_buttons );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        ButterKnife.bind(this);




        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.my_kwik_buttons_activity_drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.add, R.string.add );
        drawer.setDrawerListener( toggle );
        toggle.syncState();


        mApp = (Application) getApplication();

        mNavigationView = (NavigationView) findViewById( R.id.nav_view );
        mNavigationView.setItemIconTintList( null );
        mNavigationView.setNavigationItemSelectedListener( this );

        actionBar = getSupportActionBar();
        actionBar.setTitle( getResources().getString( R.string.clients_activity_title) );
        actionBar.show();

        View headerView = mNavigationView.getHeaderView( 0 );
        TextView user = (TextView) headerView.findViewById( R.id.nav_header_user_name_text_view );
        user.setText( mApp.getUser().getFirstName() + " " + mApp.getUser().getLastName() );

        TextView phone = (TextView) headerView.findViewById( R.id.nav_header_user_phone_text_view );
        phone.setText( mApp.getUser().getPhone() );


        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        customNavigationView( navigationView );
    }

    public void  addNewTrapClick(View v){
        if (Utils.checkVolumeLevel( ClientsActivity.this ) != null) {
            return;
        }
        Intent i = new Intent( ClientsActivity.this, AddNewTrapActivity.class );
        i.putExtra( "sender", ClientsActivity.class.getSimpleName() );
        i.putExtra("name", mApp.getUser().getFirstName());
        startActivity( i );
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateClientsList();

        updateOverView();


        //updateList();
    }

    private void updateOverView() {
        updateTotalTrapsHeader();
        updateTrapsAlertValue();
    }

    private void updateTrapsAlertValue() {

        KwikMe.getKwikDevices(null, null, null, KwikDevice.STATUS_ALERT, new GetKwikDevicesListener() {
            @Override
            public void getKwikDevicesListenerDone(GetKwikDevicesResponse response) {
                hideProgressBar();
                int totalTraps = 0;
                try {
                    totalTraps = response.getPaging().getTotal();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                mTotalAlertTrapsTextView.setText(totalTraps + " Trap Alerts");
            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog("",error.getMessage());
            }
        });
    }

    private void updateTotalTrapsHeader() {
        KwikMe.getKwikDevices(null, null, null,null, new GetKwikDevicesListener() {
            @Override
            public void getKwikDevicesListenerDone(GetKwikDevicesResponse response) {
                hideProgressBar();
                int totalTraps = 0;
                try {
                    totalTraps = response.getPaging().getTotal();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                mTotalTrapsTextView.setText("Total Traps: (" + totalTraps + ")");
            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog("",error.getMessage());
            }
        });
    }

    private void updateClientsList() {
        showProgressBar();
        KwikMe.getClients(null, new GetClientsListener() {
            @Override
            public void getClientsDone(GetClientsResponse res) {

                if(res == null || res.getClients() == null){
                    return;
                }
                mApp.setmClients(res.getClients());

                mClientsHeaderTextView.setText("Active clients: (" + res.getClients().size() + ")");
                mClientsAdapter = new ClientsArrayAdapter(ClientsActivity.this,res.getClients() );

                mClientsList.setAdapter(mClientsAdapter);
            }

            @Override
            public void getClientsError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog("",error.getMessage());

            }
        });
    }

    private void customNavigationView(NavigationView navigationView) {
        final Menu menu = navigationView.getMenu();
        int i = 0;
        for (; i < menu.size(); i++) {
            if (menu.getItem( i ).getTitle().equals( getString( R.string.my_buttons_activity_nav_menu_communicate ) )) {
                SpannableString spanString = new SpannableString( menu.getItem( i ).getTitle().toString() );
                spanString.setSpan( new ForegroundColorSpan( getResources().getColor( R.color.kwik_me_menu_group_item_text_color ) ), 0, spanString.length(), 0 );
                menu.getItem( i ).setTitle( spanString );
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.stopPlaying();
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.my_kwik_buttons_activity_drawer_layout );
        if (drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder( this, android.R.style.Theme_Material_Light_Dialog_Alert );
            } else {
                builder = new AlertDialog.Builder( this );
            }

            builder
                    .setIcon( android.R.drawable.ic_dialog_alert )
                    .setTitle( R.string.my_buttons_activity_exit_dialog_title )
                    .setMessage( R.string.my_buttons_activity_exit_dialog_message )
                    .setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }

                    } )
                    .setNegativeButton( android.R.string.no, null )
                    .show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        String selectedOption = null;

        String googleAnalyticsUserAction = "Side menu click ";
        if (id == R.id.nav_personal_details) {
            selectedOption = "Personal Details";
            //Intent i = new Intent( this, UpdatePersonalDetailsActivity.class );
            //startActivity( i );
        } else if (id == R.id.nav_order_history) {
            googleAnalyticsUserAction += "Order History";
            selectedOption = getString( R.string.my_buttons_activity_nav_menu_order_history );

        } else if (id == R.id.nav_change_language) {
            googleAnalyticsUserAction += "Change Language";
            selectedOption = "Change Language";
//            AlertDialog.Builder adb;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                adb = new AlertDialog.Builder( this, android.R.style.Theme_Material_Light_Dialog_Alert );
//            } else {
//                adb = new AlertDialog.Builder( this );
//            }
//            CharSequence items[] = new CharSequence[]{getString( R.string.english ), getString( R.string.hebrew )};//, getString( R.string.russian ), getString( R.string.french ), getString( R.string.japanese ), getString( R.string.portuguese )};
//            int selected = 0;
//            if (KwikMe.LOCAL.equals( "he_IL" )) {
//                selected = 1;
//            } else if (KwikMe.LOCAL.equals( "ru_RU" )) {
//                selected = 2;
//            } else if (KwikMe.LOCAL.equals( "fr_FR" )) {
//                selected = 3;
//            } else if (KwikMe.LOCAL.equals( "ja_JP" )) {
//                selected = 4;
//            } else if (KwikMe.LOCAL.equals( "pt_BR" )) {
//                selected = 5;
//            }
//
//            adb.setSingleChoiceItems( items, selected, new DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface d, int n) {
//                    mApp.setPref( Application.PrefType.LOCALE, String.valueOf( n ) );
//                    if (n == 5) {
//                        Utils.setLocale( getApplicationContext(), "pt" );
//                        KwikMe.LOCAL = "pt_BR";
//                    } else if (n == 4) {
//                        Utils.setLocale( getApplicationContext(), "ja" );
//                        KwikMe.LOCAL = "ja_JP";
//                    } else if (n == 3) {
//                        Utils.setLocale( getApplicationContext(), "fr" );
//                        KwikMe.LOCAL = "fr_FR";
//                    } else if (n == 2) {
//                        Utils.setLocale( getApplicationContext(), "ru" );
//                        KwikMe.LOCAL = "ru_RU";
//                    } else if (n == 1) {
//                        Utils.setLocale( getApplicationContext(), "iw" );
//                        KwikMe.LOCAL = "he_IL";
//                    } else if (n == 0) {
//                        Utils.setLocale( getApplicationContext(), "en" );
//                        KwikMe.LOCAL = "en_US";
//                    }
//
//                    //Update the server
//                    if (mApp.getUser() != null) {
//                        mApp.getUser().setLocale( KwikMe.LOCAL );
//                        KwikMe.updateKwikUser( mApp.getUser(), new UpdateKwikUserListener() {
//                            @Override
//                            public void updateKwikUserDone(KwikUser user) {
//
//
//                            }
//
//                            @Override
//                            public void updateKwikUserError(KwikServerError error) {
//                                showOneButtonErrorDialog( getString( R.string.oops ), error.getMessage() );
//                            }
//                        } );
//                    }
//                }
//
//            } );
//            adb.setNegativeButton( android.R.string.cancel, null );
//            adb.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    recreate();
//                }
//            } );
//            adb.setTitle( R.string.my_buttons_activity_settings_select_language );
//            adb.show();

        } else if (id == R.id.nav_send) {
            googleAnalyticsUserAction += "Feedback";
            openEmailApp();

        } else if (id == R.id.nav_need_help) {
            googleAnalyticsUserAction += "Need Help";
           // Utils.showHelp( this );
            selectedOption = "Need help";
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder( this, android.R.style.Theme_Material_Light_Dialog_Alert );
            } else {
                builder = new AlertDialog.Builder( this );
            }
            builder.setIcon( android.R.drawable.ic_dialog_alert )
                    .setTitle( R.string.my_buttons_activity_exit_dialog_title )
                    .setMessage( R.string.my_buttons_activity_exit_dialog_message )
                    .setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mApp.clearApplicationData();
                            mApp.setButtons( null );
                            finishAffinity();

                        }

                    } )
                    .setNegativeButton( android.R.string.no, null )
                    .show();

        }


        if (selectedOption == null) {

            DrawerLayout drawer = (DrawerLayout) findViewById( R.id.my_kwik_buttons_activity_drawer_layout );
            drawer.closeDrawer( GravityCompat.START );
        } else {
            Toast toast = Toast.makeText( this, selectedOption + " " + getString( R.string.my_buttons_nav_not_supported_yet_message ),
                    Toast.LENGTH_SHORT );
            TextView v = (TextView) toast.getView().findViewById( android.R.id.message );
            if (v != null) v.setGravity( Gravity.CENTER );
            toast.show();
        }

        mApp.getDefaultTracker().send( new HitBuilders.EventBuilder()
                .setCategory( mApp.GOOGLE_ANALYTICS_CATEGORY_USER_ACTION )
                .setAction( googleAnalyticsUserAction )
                .build() );
        return true;
    }

    private void openEmailApp() {
        //get to, subject from the user input and store as string.
        String emailTo = mApp.SUPPORT_EMAIL;
        String emailSubject;
        try {
            emailSubject = getString( R.string.my_buttons_feedback_header ) + " " + mApp.getUser().getFirstName();
        } catch (NullPointerException e) {
            emailSubject = "";
        }
        Intent emailIntent = new Intent( Intent.ACTION_SEND );
        emailIntent.putExtra( Intent.EXTRA_EMAIL, new String[]{emailTo} );
        emailIntent.putExtra( Intent.EXTRA_SUBJECT, emailSubject );

        //need this to prompts email client only
        emailIntent.setType( "message/rfc822" );

        startActivity( Intent.createChooser( emailIntent, getString( R.string.select_email_client ) ) );
    }

    public class MyButtonsArrayAdapter extends ArrayAdapter<KwikButtonDevice> {
        private final Context context;
        private  List<KwikButtonDevice> values;

        public MyButtonsArrayAdapter(Context context, List<KwikButtonDevice> values) {
            super( context, -1, values );
            Logger.e(TAG,this.getClass().getSimpleName() + " MyButtonsArrayAdapter constructor ");
            this.context = context;
            this.values = values;
        }

        public void setValues(List<KwikButtonDevice> values) {
            this.values = values;
        }


        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Logger.e(TAG,this.getClass().getSimpleName() + " "  + new Object(){}.getClass().getEnclosingMethod().getName());
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            final View rowView = inflater.inflate( R.layout.my_kwik_buttons_list_item, parent, false );
            final ViewGroup transitionsContainer = (ViewGroup) rowView.findViewById( R.id.kwik_button_item );
            KwikButtonDevice buttonTemp = null;
            try {
                buttonTemp = mApp.getButton(values.get(position).getId());
            }catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }

            final KwikButtonDevice button = buttonTemp;


            if (button == null) {
                return new View(ClientsActivity.this);
            }

            final KwikProject project = mApp.getProject( button.getProject() );


            //Popup menu
            final ImageView threeDots = (ImageView) rowView.findViewById( R.id.three_dots_image_view );
            threeDots.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonSettings( project, button );
                }
            } );

            //Project Title
            TextView title = (TextView) rowView.findViewById( R.id.title );
            if (project != null && project.getName() != null) {
                //if button not registered
                TextView notRegisteredTextMessage = (TextView) rowView.findViewById( R.id.button_settings_tap_to_setup_text_view );
                if (button.getStatus().equals( KwikButtonDevice.STATUS_ATTACHED )) {
                    notRegisteredTextMessage.setVisibility( View.VISIBLE );
                    title.setText( R.string.no_actions );
                    title.setTextColor( Color.parseColor( "#9b9999" ) );
                    rowView.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            registerNonRegisteredButton( button, project );
                        }
                    } );
                } else if (button.getStatus().equals( KwikButtonDevice.STATUS_REGISTERED )) {
                    notRegisteredTextMessage.setVisibility( View.GONE );
                    String titleText = "";
                    if(button.getName() != null){
                        titleText += button.getName();
                    }
                    if(button.getDescription() != null){
                        titleText += "\n" + button.getDescription();
                    }
                    if(button.getId() != null){
                        titleText += "-" + button.getId();
                    }
                    title.setText( titleText);
                    title.setTextColor( Color.parseColor( "#256ba7" ) );
                }
            }

            //Price
            TextView priceTextView = (TextView) findViewById( R.id.price );
            try {
                List<KwikSelectedProduct> selectedProducts = button.getProducts();

                try {
                    for (Iterator<KwikSelectedProduct> iter = selectedProducts.listIterator(); iter.hasNext(); ) {
                        KwikSelectedProduct a = iter.next();
                        if (getKwikProductById( project, a.getId() ) == null) {
                            iter.remove();
                        }
                    }
                } catch (NullPointerException e) {

                }

                //String price;
                double price = 0;
                for (KwikSelectedProduct p : selectedProducts) {
                    double c = p.getCount() * getKwikProductById( project, p.getId() ).getPrice().getRetail();
                    Logger.i( TAG, "Product %s amount %d price %f", p.getId(), p.getCount(), getKwikProductById( project, p.getId() ).getPrice().getRetail() );
                    price += c;
                }
                price = Math.round( price * 100.0 ) / 100.0;
                if (price == Math.round( price )) {
                    priceTextView.setText( getKwikProductById( project, selectedProducts.get( 0 ).getId() ).getPrice().getCurrencySymbol() + Math.round( price ) );
                } else {
                    priceTextView.setText( getKwikProductById( project, selectedProducts.get( 0 ).getId() ).getPrice().getCurrencySymbol() + price );
                }

            } catch (Exception e) {
            }

            String pattern = Pattern.quote( System.getProperty( "file.separator" ) );

            //Check if project includes Logo.
            //Brand Image
            final ImageView brandView = (ImageView) rowView.findViewById( R.id.my_buttons_item_button_image_view );
            if (button.getStatus().equals( KwikButtonDevice.STATUS_ATTACHED )) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation( 0 );
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter( matrix );
                brandView.setColorFilter( filter );
            }


            try {
                String[] splittedFileName = project.getLogosButton().get( MobileDevice.DENSITY ).split( pattern );

                String fileName = Application.IMAGES_FOLDER_NAME + "/" + splittedFileName[splittedFileName.length - 1];
                File sdCardDirectory = Environment.getExternalStorageDirectory();
                File image = new File( sdCardDirectory, fileName );
                Bitmap mIcon11 = null;
                if (image.exists()) {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    mIcon11 = BitmapFactory.decodeFile( image.getAbsolutePath(), bmOptions );
                    if (button.getStatus().equals( KwikButtonDevice.STATUS_ATTACHED )) {
                        ColorMatrix matrix = new ColorMatrix();
                        matrix.setSaturation( 0 );

                        ColorMatrixColorFilter filter = new ColorMatrixColorFilter( matrix );
                        brandView.setImageBitmap( mIcon11 );
                        brandView.setColorFilter( filter );

                    } else {
                        if(button.getTriggerType() == null){
                            brandView.setImageDrawable(getResources().getDrawable(R.drawable.ipm_square_yellow));
                        }else {
                            if (button.getTriggerType().equalsIgnoreCase("trigger1") || button.getTriggerType().equals("click")) {
                                brandView.setImageDrawable(getResources().getDrawable(R.drawable.ipm_button_ready_icon));
                            } else {
                                brandView.setImageDrawable(getResources().getDrawable(R.drawable.ipm_good_job_logo));
                            }
                        }
                    }
                } else {
                    try {
                        boolean registered = false;
                        if (button.getStatus().equals( KwikButtonDevice.STATUS_REGISTERED )) {
                            registered = true;
                        }
                        new DownloadImageTask( brandView, project, registered ).execute( project.getLogosButton().get( MobileDevice.DENSITY ) );
                    } catch (Exception e) {
                        Logger.e( TAG, "MyButtonsArrayAdapter.getView Exception %s", e.getMessage() );
                    }
                }
            } catch (NullPointerException e) {
                Logger.e( TAG, "Exception Project does not include logo" );
            }

            return rowView;
        }

        @Override
        public int getCount() {
            return mApp.getButtons().size();
        }
    }

    private void buttonSettings(KwikProject project, final KwikButtonDevice button) {

        Intent i = new Intent( ClientsActivity.this, ButtonSettingsActivity.class );
        if (button != null)
            i.putExtra( "buttonId", button.getId() );
        else {
            Toast.makeText( ClientsActivity.this, "Button is not valid ", Toast.LENGTH_LONG ).show();
            return;
        }
        startActivity( i );
    }

    private KwikProduct getKwikProductById(KwikProject project, String id) {
        List<KwikProduct> projectProducts;
        projectProducts = project.getProducts();
        for (KwikProduct product : projectProducts) {
            if (product.getId().equals( id )) {
                return product;
            }
        }
        return null;
    }

    /**
     * Show error dialog with one button
     *
     * @param header  error header
     * @param message error message
     */
    protected void showOneButtonErrorDialog(String header, String message) {
        mApp.getDefaultTracker().send( new HitBuilders.EventBuilder()
                .setCategory( mApp.GOOGLE_ANALYTICS_CATEGORY_POPUP_FOR_USER )
                .setAction( message )
                .build() );
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder( this, android.R.style.Theme_Material_Light_Dialog_Alert );
        } else {
            builder = new AlertDialog.Builder( this );
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle( header );
        alertDialog.setCancelable( false );
        alertDialog.setMessage( Html.fromHtml( message ) );
        alertDialog.setButton( AlertDialog.BUTTON_NEUTRAL, getString( android.R.string.ok ),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ClientsActivity.this.onResume();
                        dialog.dismiss();
                    }
                } );
        if (!ClientsActivity.this.isFinishing()) {
            alertDialog.show();
        }
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        KwikProject project;
        boolean registered;

        public DownloadImageTask(ImageView bmImage, KwikProject project, boolean registered) {
            this.bmImage = bmImage;
            this.project = project;
            this.registered = registered;
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
            String pattern = Pattern.quote( System.getProperty( "file.separator" ) );
            String[] splittedFileName = project.getLogosButton().get( MobileDevice.DENSITY ).split( pattern );

            String fileName = Application.IMAGES_FOLDER_NAME + "/" + splittedFileName[splittedFileName.length - 1];
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
            if (this.registered) {
                bmImage.setImageBitmap( result );
            } else {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation( 0 );

                ColorMatrixColorFilter filter = new ColorMatrixColorFilter( matrix );
                bmImage.setImageBitmap( result );
                bmImage.setColorFilter( filter );
            }
        }
    }

    private void registerNonRegisteredButton(KwikButtonDevice button, KwikProject project) {
        if (button.getStatus().equals( KwikButtonDevice.STATUS_ATTACHED )) {
            if (!registering) {
                mProgressBar.setVisibility( View.VISIBLE );
                registerButtonAndGetProject( button );
            }

        }
    }


    private void registerButtonAndGetProject(KwikButtonDevice button) {
        if (!button.getStatus().equals( KwikButtonDevice.STATUS_REGISTERED )) {

                Intent i = new Intent( ClientsActivity.this, IpmLoginActivity.class );
                i.putExtra( WebviewActivity.LOADING_URL, mApp.getProject( button.getProject() ).getLoginUri() );
                i.putExtra( WebviewActivity.LOGIN_REDIRECT_URI, mApp.getProject( button.getProject() ).getLoginRedirectUri() );
                i.putExtra( WebviewActivity.PAGE_TITLE, "Register" );
                i.putExtra( "sender", LoginActivity.class.getSimpleName() );
                i.putExtra( "buttonId", button.getId() );
                startActivity( i );
                mProgressBar.setVisibility( View.GONE );
        } else {
            mProgressBar.setVisibility( View.GONE );
        }
    }

    public class ClientsArrayAdapter extends ArrayAdapter<IpmClient> {
        private final Context context;
        private  List<IpmClient> values;

        public ClientsArrayAdapter(Context context, List<IpmClient> values) {
            super( context, -1, values );
            this.context = context;
            this.values = values;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Logger.e(TAG,this.getClass().getSimpleName() + " "  + new Object(){}.getClass().getEnclosingMethod().getName());
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            final View rowView = inflater.inflate( R.layout.client_list_item, parent, false );
            TextView clientName = (TextView)rowView.findViewById(R.id.client_name_TextView);
            clientName.setText(values.get(position).getName());
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ClientsActivity.this,ClientOverviewActivity.class);
                    i.putExtra("client",values.get(position).getId());
                    startActivity(i);
                }
            });
            return rowView;
        }

    }

}
