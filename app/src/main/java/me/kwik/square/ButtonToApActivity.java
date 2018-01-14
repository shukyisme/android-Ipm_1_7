package me.kwik.square;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;

import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProject;
import me.kwik.listeners.AttachUserListener;
import me.kwik.listeners.CreateClientButtonListener;
import me.kwik.listeners.GetProjectListener;
import me.kwik.utils.Logger;
import me.kwik.utils.NetworkUtil;
import me.kwik.wifi.TeachWifiCredentials;
import me.kwk.utils.Utils;


public class ButtonToApActivity extends BaseActivity  {

    private String TAG = ButtonToApActivity.class.getSimpleName();
    private TeachButtonWifiReceiver mTeachButtonWifiReceiver;
    private String ssid;
    private String password;
    private TeachWifiCredentials mTeachWifiCredentials;
    private static String mKwikButtonSerial;
    private Handler mHandler = new Handler();
    private Button mNextButton;
    private AlertDialog alertDialog;

    //For Animation
    private VideoView mVideoPlayerView;
    private DisplayMetrics mDisplayMetrics;
    private MediaController mMediaController_;


    private LinearLayout mConnectingAnimationLayout;
    private TextView mConnectingMessageTextView;
    private FrameLayout mAnimationFrameLayout;
    private String mSender;
    private Application mApp;
    private TextView mTitleTextView;
    private WiFiCheckerReceiver mWifiStatusBroadcastReceiver;
    private int pStatus = 0;
    private Handler handler = new Handler();
    private boolean noError = true;
    private MediaPlayer mGoToSettingsAudioInstructionPlayer;
    final Handler mPlaySecondAudiohandler = new Handler();

    private KwikDevice mButton;


    private Runnable playClickNextAudio = new Runnable() {
        @Override
        public void run() {
            mNextButton.setVisibility(View.VISIBLE);
            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
            mNextButton.startAnimation(shake);
            Utils.playAudioFile(ButtonToApActivity.this, "once_it_blinks_please_click_next", 0, 5);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_to_ap);

        mApp = (Application)getApplication();
        mActionBarTitle.setText(getResources().getString(R.string.button_to_ap_activity_title));
        mAnimationFrameLayout = (FrameLayout)findViewById(R.id.button_to_ap_animation_frame_layout);
        mWifiStatusBroadcastReceiver = new WiFiCheckerReceiver();

        mConnectingAnimationLayout = (LinearLayout)findViewById(R.id.button_to_ap_activity_connecting_linear_layout);
        mTeachButtonWifiReceiver = new TeachButtonWifiReceiver();
        mConnectingMessageTextView = (TextView)findViewById(R.id.button_to_ap_activity_connecting_text_view);
        mTitleTextView = (TextView)findViewById(R.id.button_to_ap_activity_title);
        mNextButton = (Button) findViewById(R.id.button_to_ap_activity_start_button);

        ssid = getIntent().getStringExtra("selectedWiFiSsid");
        password = getIntent().getStringExtra("password");
        mButton = (KwikDevice) getIntent().getParcelableExtra("button");

        mVideoPlayerView = (VideoView) findViewById(R.id.video_player_view);
        mMediaController_ = new MediaController(this);
        mDisplayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int height = mDisplayMetrics.heightPixels;
        int width = mDisplayMetrics.widthPixels;
        mVideoPlayerView.setMinimumWidth(width);
        mVideoPlayerView.setMinimumHeight(height);
        mVideoPlayerView.setMediaController(mMediaController_);
        mMediaController_.setVisibility(View.GONE);
        mVideoPlayerView.setZOrderOnTop(true);
        mVideoPlayerView.setVideoPath("android.resource://" + getPackageName() + "/"
                + R.raw.button_to_ap_animation_07);
        mVideoPlayerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mVideoPlayerView.start();

            }
        });


        if(!NetworkUtil.getWifiName(this).contains("kwik_button_") && !NetworkUtil.getWifiName(this).contains("ipm_button_")) {
            Utils.playAudioFile(this,"press_and_hold",0,5);
        }
    }

    private void pause() {
        if(mTeachWifiCredentials != null) {
            mTeachWifiCredentials.stopRepeatingTask();
        }
        hideProgressBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlaySecondAudiohandler.removeCallbacks(playClickNextAudio);
        Utils.stopPlaying();
    }

    @Override
    public Context createDisplayContext(Display display) {
        return super.createDisplayContext(display);
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(null);
    }

    public void startClicked(final View startButton) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        changeUiToTeachMode();

        Utils.stopPlaying();

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        alertDialog = builder.create();

        if(!ButtonToApActivity.this.isFinishing()) {
            alertDialog.show();

            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
            lp.dimAmount = 0.5f;
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Build.VERSION.RELEASE.startsWith("7.0")) {
                alertDialog.setContentView( R.layout.wifi_teach_manual_mode_dialog_android_7_layout );
            }else{
                alertDialog.setContentView( R.layout.wifi_teach_manual_mode_dialog_layout );
            }
            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    ButtonToApActivity.this.finish();
                }
            });
        }



        Utils.playAudioFile(this,"lets_connect_the_kwik_button",0,5);

        registerReceiver(mWifiStatusBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(mWifiStatusBroadcastReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        onOffButton(false,mNextButton);
    }

    private void changeUiToTeachMode() {
        mConnectingAnimationLayout.setVisibility(View.VISIBLE);
        mVideoPlayerView.setVisibility(View.INVISIBLE);
        mAnimationFrameLayout.setVisibility(View.INVISIBLE);
        mTitleTextView.setText(R.string.button_to_ap_activity_welcome_back);
        Drawable myDrawable = ContextCompat.getDrawable(ButtonToApActivity.this, R.drawable.good_job_icon);
        mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null,myDrawable,null,null);
    }

    public void closeDialog(){
        if(alertDialog != null){
            alertDialog.dismiss();
        }
    }

    public void goToSettings(View v){
        if (KwikMe.LOCAL.equals(Application.LOCAL_HE_IL) || KwikMe.LOCAL.equals(Application.LOCAL_IW_IL)) {
            playGoToSettingsAudioFile(this,"kwik_button_network_he.mp3",0,5);
        } else if (KwikMe.LOCAL.startsWith( "en" )){
            playGoToSettingsAudioFile(this,"kwik_button_network_en.wav",0,5);
        }
        showAdvancedWifiIfAvailable(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mTeachButtonWifiReceiver, new IntentFilter(KwikMe.WIFI_TEACH_COMPLETED_INTENT));

        if(mConnectingAnimationLayout.getVisibility() != View.VISIBLE) {
            mPlaySecondAudiohandler.postDelayed(playClickNextAudio, 10000);
        }

        //mNextTextView.setVisibility(View.INVISIBLE);
        if(mVideoPlayerView.isPlaying()){
            View placeholder =  findViewById(R.id.placeholder);

            placeholder.setVisibility(View.GONE);
            mVideoPlayerView.setZOrderOnTop(false);
        }

        try{
            mSender = getIntent().getStringExtra("sender");
        }catch (NullPointerException e){
            Logger.e(TAG,"%s",e.getMessage());
        }

        String networkName = NetworkUtil.getWifiName(this);

        if( networkName.contains("kwik_button") || networkName.contains("ipm_button")){

            closeDialog();
            stopGoToSettingsPlaying();
            mTeachWifiCredentials = new TeachWifiCredentials(ButtonToApActivity.this, ssid, password);
            mTeachWifiCredentials.startManualMode();
            mConnectingMessageTextView.setText(getString(R.string.button_to_ap_activity_connecting_your_button));

//            String serialNumber = null;
//            if(networkName.startsWith("kwik")){
//                serialNumber = networkName.substring(12,networkName.length());
//            }else {
//                serialNumber = networkName.substring(11,networkName.length());
//            }
//
//            mButton.setId(serialNumber);
//            mButton.setSerial(serialNumber);
//
//            KwikMe.createClientButton(mButton.getClient(), mButton, new CreateClientButtonListener() {
//                @Override
//                public void createClientButtonListenerDone(KwikDevice device) {
//                    mTeachWifiCredentials = new TeachWifiCredentials(ButtonToApActivity.this, ssid, password);
//                    mTeachWifiCredentials.startManualMode();
//                    mConnectingMessageTextView.setText(getString(R.string.button_to_ap_activity_connecting_your_button));
//                }
//                @Override
//                public void createClientButtonListenerError(KwikServerError error) {
//                    showOneButtonErrorDialog("",error.getMessage());
//                }
//            });

        }

    }

    private void onOffButton(boolean clickable, Button b) {
        if(clickable) {
            b.setAlpha(1f);
            b.setClickable(true);
        }else{
            b.setAlpha(.5f);
            b.setClickable(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
        stopRepeatingTask();
        try{
            unregisterReceiver(mTeachButtonWifiReceiver);
        }catch(Exception e){
            //Do nothing
        }
    }

    public void showAdvancedWifiIfAvailable(Context ctx)  {
        final Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (activityExists(ctx, i)) {
            ctx.startActivity(i);
        }
    }

    public static boolean activityExists(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        final ResolveInfo info = mgr.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return (info != null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ButtonToApActivity.this.finish();
    }

    class TeachButtonWifiReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            boolean teach = intent.getBooleanExtra(KwikMe.WIFI_TEACH_COMPLETED_INTENT_SUCCESS_EXTRA,false);
            mKwikButtonSerial = intent.getStringExtra(KwikMe.WIFI_TEACH_COMPLETED_INTENT_SUCCESS_KWIK_ID_EXTRA);
            String projectId = intent.getStringExtra(KwikMe.WIFI_TEACH_COMPLETED_PROJECT_ID);

            if(projectId != null) {
                mApp.addProject(projectId);
            }
            Logger.d(TAG, "Teach wifi response = %s", teach);

            if(teach){
                pause();
                noError = true;
                mButton.setSerial(mKwikButtonSerial);
                mButton.setId(mKwikButtonSerial);
                KwikMe.createClientButton(mButton.getClient(), mButton, new CreateClientButtonListener() {
                    @Override
                    public void createClientButtonListenerDone(KwikDevice device) {

                        if(mSender!= null && mSender.equals(ButtonSettingsActivity.class.getSimpleName())){
                            Intent i = new Intent(ButtonToApActivity.this,GoodJobActivity.class);
                            i.putExtra("sender",mSender);
                            i.putExtra("buttonId",mKwikButtonSerial);
                            startActivity(i);
                            finish();
                            return;
                        }

                        KwikButtonDevice button = new KwikButtonDevice();

                        if(mKwikButtonSerial.startsWith("kwik_")) {
                            button.setId(mKwikButtonSerial.substring(5));
                        }else if(mKwikButtonSerial.startsWith("ipm_")) {
                            button.setId(mKwikButtonSerial.substring(4));
                        }else{
                            button.setId(mKwikButtonSerial);
                        }
                        button.setUser(KwikMe.USER_ID);


                        if(mApp.getButton(mKwikButtonSerial) != null) {
                            Intent i = new Intent(ButtonToApActivity.this, GoodJobActivity.class);
                            i.putExtra("buttonId",mKwikButtonSerial);
                            startActivity(i);
                            finish();
                            return;
                        }
                        mConnectingMessageTextView.setText(getString(R.string.button_to_ap_activity_connecting_button_to_user));
                    }
                    @Override
                    public void createClientButtonListenerError(KwikServerError error) {
                        showOneButtonErrorDialog("",error.getMessage());
                    }
                });

            }else{
                pause();
                noError = false;
                showTwoButtonErrorDialog( getString( R.string.oops ), getString( R.string.button_to_ap_activity_teach_fail_message ), getString( android.R.string.ok ),
                        "",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ButtonToApActivity.this.finish();
                            }
                        }, null);

            }
        }
    }

    private void  playGoToSettingsAudioFile(final Context context,String fileName,final int numberOfRepeats, int interval) {
        AssetFileDescriptor afd = null;
        try {
            if(mGoToSettingsAudioInstructionPlayer != null){
                mGoToSettingsAudioInstructionPlayer.stop();
                mGoToSettingsAudioInstructionPlayer.release();
            }
            afd = context.getAssets().openFd(fileName);
            mGoToSettingsAudioInstructionPlayer = new MediaPlayer();
            mGoToSettingsAudioInstructionPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mGoToSettingsAudioInstructionPlayer.prepare();
            try {
                mGoToSettingsAudioInstructionPlayer.start();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopGoToSettingsPlaying() {
        if (mGoToSettingsAudioInstructionPlayer != null) {
            try {
                mGoToSettingsAudioInstructionPlayer.stop();
                mGoToSettingsAudioInstructionPlayer.release();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
    }
}
