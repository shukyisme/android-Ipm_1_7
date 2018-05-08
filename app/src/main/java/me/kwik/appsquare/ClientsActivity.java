package me.kwik.appsquare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.IpmClient;
import me.kwik.data.KwikProduct;
import me.kwik.data.KwikProject;
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
    private boolean hasError;

    @BindView(R.id.clients_activity_clients_ListView) ListView mClientsList;
    @BindView(R.id.clients_activity_clients_list_header_TextView) TextView mClientsHeaderTextView;
    @BindView(R.id.clients_activity_overview_total_traps_TextView) TextView mTotalTrapsTextView;
    @BindView(R.id.clients_activity_alert_traps_TextView) TextView mTotalAlertTrapsTextView;
    @BindView(R.id.client_activity_clients_listView_bottom_background_View) View mListViewBottomBackgroundView;


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

        mTotalAlertTrapsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ClientsActivity.this,TrapsActivity.class);
                i.putExtra("client",(String)null);
                startActivity(i);
            }
        });
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

        hasError = false;
        updateClientsList();

        updateOverView();

        //updateList();
    }

    private void updateOverView() {
        updateTotalTrapsHeader();

    }

    private void updateTrapsAlertValue() {

        KwikMe.getKwikDevices(null, null, null, KwikDevice.STATUS_ALERT,"status",1, new GetKwikDevicesListener() {
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
                if(!hasError) {
                    showErrorDialog(error);
                }
                hasError = true;
            }
        });
    }

    private void updateTotalTrapsHeader() {
        KwikMe.getKwikDevices(null, null, null,null, "status",1,new GetKwikDevicesListener() {
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
                updateTrapsAlertValue();
            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                if(!hasError) {
                    showErrorDialog(error);
                }
                hasError = true;
            }
        });
    }

    private void updateClientsList() {
        showProgressBar();
        KwikMe.getClients(null, new GetClientsListener() {
            @Override
            public void getClientsDone(GetClientsResponse res) {


                if(res == null){
                    return;
                }

                if(res.getClients() != null) {
                    mClientsAdapter = new ClientsArrayAdapter(ClientsActivity.this, res.getClients());
                    mApp.setmClients(res.getClients());
                    mClientsList.setAdapter(mClientsAdapter);
                }

                int activeClientsNumber = 0;
                if(res.getPaging() != null){
                    activeClientsNumber = res.getPaging().getTotal();
                }
                mClientsHeaderTextView.setText("Active clients: (" + activeClientsNumber + ")");
            }

            @Override
            public void getClientsError(KwikServerError error) {
                hasError = true;
                hideProgressBar();
                showErrorDialog(error);
            }
        });
    }

//    private void customNavigationView(NavigationView navigationView) {
//        final Menu menu = navigationView.getMenu();
//        int i = 0;
//        for (; i < menu.size(); i++) {
//            if (menu.getItem( i ).getTitle().equals( getString( R.string.my_buttons_activity_nav_menu_communicate ) )) {
//                SpannableString spanString = new SpannableString( menu.getItem( i ).getTitle().toString() );
//                spanString.setSpan( new ForegroundColorSpan( getResources().getColor( R.color.kwik_me_menu_group_item_text_color ) ), 0, spanString.length(), 0 );
//                menu.getItem( i ).setTitle( spanString );
//                break;
//            }
//        }
//    }

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

        if (id == R.id.nav_send) {
            googleAnalyticsUserAction += "Feedback";
            openEmailApp();
        } else if (id == R.id.nav_need_help) {
            googleAnalyticsUserAction += "Need Help";
           // Utils.showHelp( this );
            selectedOption = "Need help";
        } else if (id == R.id.nav_faq) {
            googleAnalyticsUserAction += "F.A.Q";
            String faqUrl = getString(R.string.my_buttons_nav_menu_faq_url);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(faqUrl));
            startActivity(browserIntent);
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
                            finish();
                            Intent i= new Intent(ClientsActivity.this,SignInActivity.class);
                            startActivity(i);

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
                        //ClientsActivity.this.onResume();
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


    public class ClientsArrayAdapter extends ArrayAdapter<IpmClient> {
        private final Context context;
        private  List<IpmClient> values;

        public ClientsArrayAdapter(Context context, List<IpmClient> values) {
            super( context, -1, values );
            this.context = context;
            this.values = values;
            if(this.values.size() > 5){
                mListViewBottomBackgroundView.setVisibility(View.VISIBLE);
            }else{
                mListViewBottomBackgroundView.setVisibility(View.GONE);
            }
        }



        public View getView(final int position, View convertView, ViewGroup parent) {
            Logger.e(TAG,this.getClass().getSimpleName() + " "  + new Object(){}.getClass().getEnclosingMethod().getName());
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            final View rowView = inflater.inflate( R.layout.client_list_item, parent, false );
            TextView clientName = (TextView)rowView.findViewById(R.id.client_name_TextView);
            ImageView statusImageView = (ImageView)rowView.findViewById(R.id.client_status_ImageView);
            clientName.setText(values.get(position).getName());
            try {
            if(values.get(position).getStatus().equalsIgnoreCase("alert")){
                statusImageView.setImageDrawable(ContextCompat.getDrawable(ClientsActivity.this,R.drawable.client_red_alert));
            }else if(values.get(position).getStatus().equalsIgnoreCase("ready")){
                statusImageView.setImageDrawable(ContextCompat.getDrawable(ClientsActivity.this,R.drawable.client_green_alert));
            }else{
                statusImageView.setImageDrawable(ContextCompat.getDrawable(ClientsActivity.this,R.drawable.client_grey_alert));
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

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
