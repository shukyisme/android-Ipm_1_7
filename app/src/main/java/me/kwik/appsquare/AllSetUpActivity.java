package me.kwik.appsquare;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class AllSetUpActivity extends BaseActivity {

    private TextView mTitle;
    private VideoView video_player_view;
    private DisplayMetrics dm;
    private MediaController media_Controller;
    private Application mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_set_up);

        mActionBarTitle.setText(getString(R.string.all_set_up_activity_title));

        mTitle = (TextView)findViewById(R.id.all_set_up_activity_title_text_view);
        mTitle.setText(((Application)getApplication()).getUser().getFirstName() + " " + getString(R.string.all_set_up_activity_you_are_all_set_up) );
        mApp= (Application)getApplication();
        video_player_view = (VideoView) findViewById(R.id.video_player_view);
        media_Controller = new MediaController(this);
        dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        video_player_view.setMinimumWidth(width);
        video_player_view.setMinimumHeight(height);
        video_player_view.setMediaController(media_Controller);
        media_Controller.setVisibility(View.GONE);
        video_player_view.setVideoPath("android.resource://" + getPackageName() + "/"
                + R.raw.all_set_up_animation_23);
        video_player_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                video_player_view.start();

            }
        });

        mPrevTextView.setVisibility(View.INVISIBLE);
       // mNextImageView.setText(getString(R.string.all_set_up_activity_done));
    }

    @Override
    protected void clickNext() {
        super.clickNext();
        mApp.setPref(Application.PrefType.FIRST_BUTTON_IN_THE_APP,"false");
        Intent i = new Intent(AllSetUpActivity.this,ClientsActivity.class);
        startActivity(i);
        this.finish();
    }
}
