package me.kwik.appsquare;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import me.kwk.utils.Utils;

public class SetupActivity extends BaseActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();
    private DiscreteSeekBar volumeSeekbar = null;
    private AudioManager audioManager = null;
    private int mVolumeLevel;
    private Button mNextButton;
    private TextView mFirstCommentTextView;
    private TextView mSecondCommentTextView;
    private String mClickedThatOpenedThisPage;
    private boolean audioIsRunning = false;
    private int mPrevProgress = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_setup);
        mActionBarTitle.setText(R.string.setup_activity_title);
        initControls();
        mNextButton = (Button)findViewById(R.id.setup_activity_next_button);
        mFirstCommentTextView = (TextView)findViewById(R.id.setup_activity_please_comment_text_view);
        mSecondCommentTextView = (TextView)findViewById(R.id.setup_activity_increase_comment_text_view);
        Utils.playAudioFile(this,"increase_volume",0,5);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mClickedThatOpenedThisPage = getIntent().getStringExtra(StartActivity.BUTTON_CLICKED);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        final String sender =  getIntent().getStringExtra("sender");
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =  new Intent(SetupActivity.this,WiFiSelectionActivity.class);
                startActivity(i);
                finish();
            }
        });

        mVolumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mPrevProgress = mVolumeLevel;
        changeUiAfterVolumeChange(mVolumeLevel);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                //Raise the Volume Bar on the Screen
                volumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //Adjust the Volume
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                //Lower the Volume Bar on the Screen
                volumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                );
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            default:
                return false;
        }
    }

    private void initControls(){
        try{
            volumeSeekbar = (org.adw.library.widgets.discreteseekbar.DiscreteSeekBar) findViewById(R.id.seekBar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, AudioManager.FLAG_PLAY_SOUND);
                    changeUiAfterVolumeChange(value);
                }

                @Override
                public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

                }
            });

//            volumeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//                @Override
//                public void onStopTrackingTouch(SeekBar arg0){
//                }
//
//                @Override
//                public void onStartTrackingTouch(SeekBar arg0){
//                }
//
//                @Override
//                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2){
//
//                }
//            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void changeUiAfterVolumeChange(int progress) {

        LinearLayout volumeLevelBorder = (LinearLayout)findViewById(R.id.setup_activity_volume_level_border_layout);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
        AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;

        if(mPrevProgress > Application.MINIMUM_AUDIO_LEVEL && progress <= Application.MINIMUM_AUDIO_LEVEL) {
            //mNextButton.setAlpha(.5f);
           // mNextButton.setClickable(false);
            mFirstCommentTextView.setText(R.string.setup_activity_please_turn_up);
            mFirstCommentTextView.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
            mSecondCommentTextView.setVisibility(View.VISIBLE);
            mFirstCommentTextView.startAnimation(fadeIn);
            mSecondCommentTextView.startAnimation(fadeIn);
            volumeLevelBorder.setVisibility(View.VISIBLE);
            fadeIn.setDuration(400);
            fadeIn.setFillAfter(true);
        }   else if(mPrevProgress <= Application.MINIMUM_AUDIO_LEVEL && progress > Application.MINIMUM_AUDIO_LEVEL){
            //mNextButton.setAlpha(1f);
            //mNextButton.setClickable(true);
            mFirstCommentTextView.setText(R.string.setup_activity_great_your_volume_is_set);
            Drawable myDrawable = ContextCompat.getDrawable(SetupActivity.this, R.drawable.good_job_icon);
            mFirstCommentTextView.setCompoundDrawablesWithIntrinsicBounds(null,myDrawable,null,null);
            mFirstCommentTextView.startAnimation(fadeIn);

            mSecondCommentTextView.startAnimation(fadeOut);
            fadeIn.setDuration(400);
            fadeIn.setFillAfter(true);
            fadeOut.setDuration(400);
            fadeOut.setFillAfter(true);
            mSecondCommentTextView.setVisibility(View.INVISIBLE);

            volumeLevelBorder.setVisibility(View.INVISIBLE);
            if(!audioIsRunning) {
                Utils.playAudioFile(this, "great_click_next_button",1,5);
                audioIsRunning = true;
            }
        }

        mPrevProgress = progress;
    }
}
