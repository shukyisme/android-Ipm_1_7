package me.kwik.square;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;

import java.util.ArrayList;
import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwk.utils.FontsOverride;
import me.kwk.utils.Utils;


public class StartActivity extends BaseActivity {

    private TextView mTermsAndConditionsTextView;
    private TextView mLoginTextView;

    private DisplayMetrics mDisplayMetrics;
    private Application mApp;
    private CallbackManager callbackManager;
    private String TAG = StartActivity.class.getSimpleName();
    private int mDebugCounter = 0;


    public static final String BUTTON_CLICKED = "me.kwik.me.start.activity.button.clicked";
    public static final String LOG_IN_BUTTON = "me.kwik.me.start.activity.login.button.clicked";
    public static final String SIGN_UP_BUTTON = "me.kwik.me.start.activity.sign.up.button.clicked";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getSupportActionBar().hide();
        FontsOverride.setDefaultFont(this, "MONOSPACE", "HelveticaNeueLTStd-Lt.otf");


        mTermsAndConditionsTextView = (TextView) findViewById(R.id.start_activity_terms_and_conditions_text_view);
        mTermsAndConditionsTextView.setText(Html.fromHtml(getString(R.string.start_activity_terms_and_conditions)));

        mLoginTextView = (TextView) findViewById(R.id.start_activity_alredy_registered_text_view);
        mLoginTextView.setText(Html.fromHtml(getString(R.string.start_activity_already_registered)));

        mApp = (Application) getApplication();
        mDisplayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);


       // Utils.playAudioFile(this,"welcome_to_kwik_button_set_up",0,5);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void termsAndConditions(View v) {

        String url = Application.SERVER_URL + "/static/kwikmeTerms_en.html";
        if (KwikMe.LOCAL == Application.LOCAL_HE_IL) {
            url = Application.SERVER_URL + "/static/kwikmeTerms_he.html";
        }
        mDebugCounter++;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.stopPlaying();
    }

    public void signupClicked(View sigupButton) {

        if(Utils.checkVolumeLevel(StartActivity.this) != null){
            return;
        }

        Intent i = new Intent(StartActivity.this, SignupActivity.class);
        startActivity(i);
    }

    public void loginClicked(View loginButton) {
        if (mDebugCounter == 3) {
            List<String> urlList = new ArrayList<String>();
            urlList.add("https://devinternal.kwik.me/api");
            urlList.add("https://integration.kwik.me/api");
            urlList.add("https://api.kwik.me");
            urlList.add("Custom url");

            final CharSequence[] items = urlList.toArray(new CharSequence[urlList.size()]);

            android.app.AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new android.app.AlertDialog.Builder(this);
            }
            builder.setTitle(R.string.please_select_address);
            builder.setSingleChoiceItems(items, -1,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (item < 3) {
                                KwikMe.setCustomUrl(items[item].toString());
                            } else if (3 == item) {
                                openCustomUrlEditTextDialog();
                                dialog.dismiss();
                            }
                        }
                    });

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            AlertDialog alert = builder.create();
            alert.setCancelable(false);
            alert.show();
        } else {
            Intent i = null;
            i = new Intent(StartActivity.this, SignInActivity.class);
            startActivity(i);
        }
    }

    private void openCustomUrlEditTextDialog() {
        android.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new android.app.AlertDialog.Builder(this);
        }

        builder.setTitle("URL");
        builder.setMessage("Custom Url");

        final EditText input = new EditText(StartActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setIcon(android.R.drawable.ic_menu_add);

        builder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String url = input.getText().toString();
                        KwikMe.setCustomUrl(url);
                    }
                });

        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }


}
