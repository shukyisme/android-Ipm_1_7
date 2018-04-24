package me.kwik.appsquare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProject;
import me.kwik.listeners.UpdateKwikButtonListener;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;

public class GoodJobActivity extends BaseActivity {

    private final String TAG = GoodJobActivity.class.getSimpleName();
    private Button mOrderSetupButton;
    private String mSender;
    private Application mApp;
    private List<String> addressList = new ArrayList<String>();
    private String mKwikDeviceButtonId;
    private KwikProject mKwikProject;
    private KwikButtonDevice mKwikButton;
    private TextView mSerialNumberTextView;
    private VideoView video_player_view;
    private MediaController media_Controller;
    private DisplayMetrics dm;
    private String mClientId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_job);

        mOrderSetupButton = (Button) findViewById(R.id.good_job_activity_start_button);
        mSerialNumberTextView = (TextView)findViewById(R.id.good_job_serial_number_TextView);

        mActionBarTitle.setText(R.string.good_job_activity_title);
        mApp = (Application) getApplication();

        try {
            mSender = getIntent().getStringExtra("sender");
        } catch (NullPointerException e) {
            Logger.e(TAG, "%s", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mClientId = getIntent().getExtras().getString("client");
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        try {
            mKwikDeviceButtonId = getIntent().getStringExtra("buttonId");
            mSerialNumberTextView.setText("Serial number: " + mKwikDeviceButtonId);
        } catch (NullPointerException e) {
            Logger.e(TAG, "%s", e.getMessage());
        }

//        if (mSender != null && mSender.equals(ButtonSettingsActivity.class.getSimpleName())) {
//            mOrderSetupButton.setText(R.string.finish);
//       }

        if(mApp != null) {
            mKwikButton = mApp.getButton( mKwikDeviceButtonId );
        }
        if(mKwikButton != null) {
            mKwikProject = mApp.getProject( mKwikButton.getProject() );
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        Utils.stopPlaying();
    }

    public void orderSetUpClick(final View orderButton) {
        orderButton.setClickable(false);
        Intent i = new Intent(GoodJobActivity.this, TrapDetailsActivity.class);
        i.putExtra("serial_number",mKwikDeviceButtonId);
        i.putExtra("client",mClientId);
       // i.putExtra("sender", GoodJobActivity.class.getSimpleName());
        startActivity(i);
        finish();

    }

    @Override
    protected void showProgressBar() {
        //mVideoPlayerView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
        //mVideoPlayerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.my_buttons_activity_exit_dialog_title)
                .setMessage(R.string.my_buttons_activity_exit_dialog_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }

                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public void needHelpClick(View view) {
        Intent i = new Intent(GoodJobActivity.this,TroubleshootingActivity.class);
        startActivity(i);
    }
}
