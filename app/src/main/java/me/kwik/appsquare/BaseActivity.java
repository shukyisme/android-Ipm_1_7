package me.kwik.appsquare;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import me.kwik.bl.KwikServerError;
import me.kwk.utils.Utils;


public class BaseActivity extends AppCompatActivity{

    protected static final int ERROR_DIALOG_DEFAULT = 0;
    protected static final int ERROR_DIALOG_NO_CONNECTIVITY = 1;
    protected static final int ERROR_CODE_NO_CONNECTIVITY = 5000;

    protected TextView mActionBarTitle;
    protected TextView mPrevTextView;
    //TODO: Should remove this field after changing select products to web views
    protected ImageView mNextImageView;
    protected ProgressBar mProgressBar;
    protected ProgressBar mNextProgressBar;
    private Application mApp;
    protected int mVolumeLevel;
    protected AudioManager audioManager;
    private AlertDialog alertDialog;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        mApp = (Application)getApplication();
        mProgressBar = (ProgressBar)findViewById(R.id.base_activity_progressBar);
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);

            actionBar.setCustomView(R.layout.custom_action_bar);

            mActionBarTitle = (TextView) findViewById(R.id.base_activity_action_bar_title);


            //Change the action bar text font
            SpannableString s = new SpannableString(mActionBarTitle.getText().toString());
            s.setSpan(new TypefaceSpan("OpenSans.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);

            //Action bar - click back
            mPrevTextView = (TextView) findViewById(R.id.base_activity_action_bar_prev_text_view);
            mPrevTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseActivity.this.finish();
                }
            });

            //Action bar - click next
            mNextImageView = (ImageView) findViewById(R.id.base_activity_action_bar_right_logo);
            mNextImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // clickNext();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVolumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mNextProgressBar = (ProgressBar)findViewById(R.id.base_activity_action_bar_next_progress_bar);

        if(mNextImageView != null) {
            mNextImageView.setClickable(true);
            mNextImageView.setVisibility(View.VISIBLE);
        }
        if(mNextProgressBar != null)
            mNextProgressBar.setVisibility(View.GONE);
        if(mPrevTextView != null)
            mPrevTextView.setClickable(true);
    }

    protected void clickNext() {
        //mNextImageView.setClickable(false);
        //mNextImageView.setVisibility(View.INVISIBLE);
        //if(mNextProgressBar != null)
         //   mNextProgressBar.setVisibility(View.VISIBLE);

    }

    protected void hideNextSpinner(){
//        try {
//            mNextImageView.setClickable( true );
//            mNextImageView.setVisibility( View.VISIBLE );
//            mNextProgressBar.setVisibility(View.INVISIBLE);
//        }catch (NullPointerException e){
//
//        }

    }

    protected void clickPrev(){
        mPrevTextView.setClickable(false);
    }
    /**
     * Show progress bar
     */
    protected void showProgressBar(){

        if (mProgressDialog != null && mProgressDialog.isShowing())
            hideProgressBar();
        mProgressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);

        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setMessage("Loading...");

        mProgressDialog.show();


    }

    /**
     * Hide progress bar
     */
    protected void hideProgressBar() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * validate if all input edit texts if any is empty
     * @param fields : edit texts array
     * @param errors : errors array for each edit text
     * @return true if any of the edit texts is empty
     */
    protected boolean isEditTextsEmpty(EditText[] fields, String[] errors){
        boolean empty = false;
        for(int i=0; i<fields.length;i++){
            EditText currentField = fields[i];
            if(currentField.getText() == null || currentField.getText().toString().trim().length() <=0){
                currentField.setError(errors[i]);
                currentField.requestFocus();
                empty = true;
            }
        }

        return empty;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.stopPlaying();
    }

    /**
     * Show error dialog with one button
     * @param header error header
     * @param message error message
     */
    protected void showOneButtonErrorDialog(String header,String message){

        try {
            mNextImageView.setClickable( true );
            mPrevTextView.setClickable( true );
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);


        builder.setCancelable(false);
        if(!BaseActivity.this.isFinishing()) {
            if(header != null) {
                builder.setTitle( header );
            }
            if(message != null) {
                builder.setMessage( Html.fromHtml(message.replace("\n","<br />")) );
            }
            builder.setNegativeButton( getString(R.string.cancel),null);
            alertDialog = builder.create();
            alertDialog.show();
        }

    }

    protected void hideTwoButtonsErrorsDialog(){
        if(alertDialog != null) {
            alertDialog.dismiss();
        }
    }


    /**
     * Show error dialog with two buttons
     * @param header
     * @param message
     * @param okClicked
     * @param cancelClicked
     */
    protected void showTwoButtonErrorDialog(int type,
                                            String header,String message,
                                            String okString,
                                            String cancelString,
                                            DialogInterface.OnClickListener okClicked,
                                            DialogInterface.OnClickListener cancelClicked){

        Utils.stopPlaying();

        if(mNextImageView != null) {
            mNextImageView.setClickable( true );
        }
        if(mPrevTextView != null) {
            mPrevTextView.setClickable( true );
        }

        AlertDialog.Builder builder;
        switch (type) {
            case ERROR_DIALOG_NO_CONNECTIVITY:
                builder = new AlertDialog.Builder(this, R.style.IPMErrorDialogNoConnectivityStyle);
                break;

            default:
                builder = new AlertDialog.Builder(this);
                break;
        }


        builder.setCancelable(false);

        if(!BaseActivity.this.isFinishing()) {


            if(header != null) {
               builder.setTitle( header );
            }

            if(message != null) {
                builder.setMessage( Html.fromHtml(message.replace("\n","<br />")));
            }

            if(okString != null){
                builder.setPositiveButton( okString, okClicked );
            }

            if(cancelString != null){
                builder.setNegativeButton( cancelString, cancelClicked );
            }
            alertDialog = builder.create();
            alertDialog.show();
        }

    }

    /**
     * Show error dialog with two buttons
     * @param header
     * @param message
     * @param okClicked
     * @param cancelClicked
     */
    protected void showTwoButtonErrorDialog(String header,String message,
                                            String okString,
                                            String cancelString,
                                            DialogInterface.OnClickListener okClicked,
                                            DialogInterface.OnClickListener cancelClicked){
        showTwoButtonErrorDialog(ERROR_DIALOG_DEFAULT, header, message, okString, cancelString, okClicked, cancelClicked);

    }

    /**
     * Show error dialog for No Connectivity
     *
     */
    protected void showNoConnectivityErrorDialog(){
        showTwoButtonErrorDialog(ERROR_DIALOG_NO_CONNECTIVITY,
                getString(R.string.err_dialog_no_connectivity_title),
                getString(R.string.err_dialog_no_connectivity_body),
                getString(R.string.err_dialog_no_connectivity_try_again),
                getString(R.string.err_dialog_no_connectivity_wifi_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onResume();

                    }
                },

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

                    }
                });

    }

    protected void showErrorDialog(KwikServerError error) {
        if(error.getValue() == ERROR_CODE_NO_CONNECTIVITY) {
            showNoConnectivityErrorDialog();
        } else {
            showOneButtonErrorDialog("", error.getMessage());
        }
    }
}
