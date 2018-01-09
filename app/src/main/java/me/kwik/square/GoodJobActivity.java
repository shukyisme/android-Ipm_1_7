package me.kwik.square;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikApplicationClient;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProduct;
import me.kwik.data.KwikProject;
import me.kwik.data.MobileDevice;
import me.kwik.listeners.GetProductCatalogListener;
import me.kwik.listeners.RegisterUserListener;
import me.kwik.listeners.UpdateKwikButtonListener;
import me.kwik.utils.Logger;
import me.kwk.utils.DownloadImageTask;
import me.kwk.utils.Utils;

import static me.kwik.square.R.id.video_player_view;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_job);

        mOrderSetupButton = (Button) findViewById(R.id.good_job_activity_start_button);
        mSerialNumberTextView = (TextView)findViewById(R.id.good_job_serial_number_TextView);

        mActionBarTitle.setText(R.string.good_job_activity_title);
        mApp = (Application) getApplication();

        Utils.playAudioFile(this, "great_job",0,5);

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
            mKwikDeviceButtonId = getIntent().getStringExtra("buttonId");
            mSerialNumberTextView.setText("Serial number: " + mKwikDeviceButtonId);
        } catch (NullPointerException e) {
            Logger.e(TAG, "%s", e.getMessage());
        }

        if (mSender != null && mSender.equals(ButtonSettingsActivity.class.getSimpleName())) {
            mOrderSetupButton.setText(R.string.finish);
       }

        if(mApp != null) {
            mKwikButton = mApp.getButton( mKwikDeviceButtonId );
        }
        if(mKwikButton != null) {
            mKwikProject = mApp.getProject( mKwikButton.getProject() );
        }

    }

    private void getProductCatalog(final String buttonId, final View orderButton) {
        KwikMe.getProductCatalogWithListener(buttonId, null, null, null, new GetProductCatalogListener() {
            @Override
            public void getProductCatalogDone(List<KwikProduct> products) {
                //Download products images
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Get product catalog")
                        .setValue(0)
                        .build());
                mKwikProject.setProducts(products);
                mProgressBar.setVisibility(View.GONE);
                for (KwikProduct product : products) {

                    ImageView v = new ImageView(GoodJobActivity.this);
                    if (product.getImages() != null) {
                        new DownloadImageTask(v).execute(product.getImages().get(MobileDevice.DENSITY));
                    } else {
                        new DownloadImageTask(v).execute("");
                    }
                }

              //  if (mKwikProject.isHasAddress()) {
              //      getUserAfterRegister(orderButton);
              //  } else {
                    goToNextPage(mKwikDeviceButtonId);
              //  }
            }

            @Override
            public void getProductCatalogError(KwikServerError error) {
                mProgressBar.setVisibility(View.GONE);
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Get product catalog")
                        .setValue(1)
                        .build());
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
                mNextImageView.setClickable(true);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.stopPlaying();
    }

    public void orderSetUpClick(final View orderButton) {
        orderButton.setClickable(false);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "progress", 0, 500);
        animation.setDuration (5000); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();
        if (mSender == null || !mSender.equals(ButtonSettingsActivity.class.getSimpleName())) {

            if (mKwikProject != null && mKwikProject.getClient() != null && mKwikProject.getClient().getType().equals( KwikApplicationClient.WEB_APPLICATION )) {
                String newUserUrl = mKwikProject.getClient().getNewUserUrl();
                String redirectUrl = mKwikProject.getClient().getRedirectUrl();
                if (newUserUrl != null && redirectUrl != null) {
                    Intent i = new Intent( GoodJobActivity.this, ProductsWebviewActivity.class );
                    i.putExtra( ProductsWebviewActivity.LOADING_URL, newUserUrl );
                    i.putExtra( ProductsWebviewActivity.LOGIN_REDIRECT_URI, redirectUrl );
                    i.putExtra( "buttonId", mKwikButton.getId() );
                    i.putExtra( "sender", ClientsActivity.class.getSimpleName() );
                    startActivity( i );
                    finish();
                    return;
                } else {
                    //showOneButtonErrorDialog( getString( R.string.oops ), getString( R.string.unknown_error ) );
                    finish();
                }
            }
        }

        Intent i = null;
        if (mKwikButton != null && mKwikButton.getStatus() != null && mKwikButton.getStatus().equals(KwikButtonDevice.STATUS_REGISTERED)) {
            if (mKwikButton.getUser().equals(KwikMe.USER_ID)) {
                i = new Intent(GoodJobActivity.this, ClientsActivity.class);
                i.putExtra("sender", GoodJobActivity.class.getSimpleName());
                startActivity(i);
                finish();
            } else {
                hideProgressBar();
                showOneButtonErrorDialog(getString(R.string.oops), "This button is registered to another user");
                orderButton.setClickable(true);
                return;
            }

        }

        if (mKwikButton != null && mKwikButton.getStatus() != null && mKwikButton.getStatus().equals(KwikButtonDevice.STATUS_REGISTERED)) {
            i = new Intent(GoodJobActivity.this, ButtonSettingsActivity.class);
            i.putExtra("sender", LoginActivity.class.getSimpleName());
            i.putExtra("buttonId", mKwikButton.getId());
            startActivity(i);
            GoodJobActivity.this.finish();
        } else {
            if (mKwikProject!= null && mKwikProject.getLoginUri() != null) {
                i = new Intent(GoodJobActivity.this, IpmLoginActivity.class);
                i.putExtra(WebviewActivity.LOADING_URL, mKwikProject.getLoginUri());
                i.putExtra(WebviewActivity.LOGIN_REDIRECT_URI, mKwikProject.getLoginRedirectUri());
                i.putExtra(WebviewActivity.PAGE_TITLE, "Register");
                i.putExtra("sender", LoginActivity.class.getSimpleName());
                i.putExtra("buttonId", mKwikButton.getId());
                i.putExtra("Authorization",false);
                startActivity(i);
                GoodJobActivity.this.finish();
            } else if(mKwikButton != null && mKwikButton.getStatus().equals(KwikButtonDevice.STATUS_ATTACHED)){

                if (mKwikProject != null && mKwikProject.getClient() != null && mKwikProject.getClient().getType().equals( KwikApplicationClient.WEB_APPLICATION )) {
                    String newUserUrl = mKwikProject.getClient().getNewUserUrl();
                    String redirectUrl = mKwikProject.getClient().getRedirectUrl();
                    if (newUserUrl != null && redirectUrl != null) {
                        i = new Intent( GoodJobActivity.this, ProductsWebviewActivity.class );
                        i.putExtra( ProductsWebviewActivity.LOADING_URL, newUserUrl );
                        i.putExtra( ProductsWebviewActivity.LOGIN_REDIRECT_URI, redirectUrl );
                        i.putExtra( "buttonId", mKwikButton.getId() );
                        i.putExtra( "sender", ClientsActivity.class.getSimpleName() );
                        startActivity( i );
                        finish();
                        return;
                    } else {
                        //showOneButtonErrorDialog( getString( R.string.oops ), getString( R.string.unknown_error ) );
                        finish();
                    }
                }else {

                    KwikMe.registerUser( mKwikButton, new RegisterUserListener() {
                        @Override
                        public void registerUserDone(KwikButtonDevice button) {
                            if (mApp.getButton( button.getId() ) == null) {
                                mApp.getButtons().add( button );
                            } else {
                                mApp.updateButton( button.getId(), button );
                            }

                            if (mKwikProject.isHasCatalog()) {
                                getProductCatalog( button.getId(), orderButton );
                            } else {
                               // if (mKwikProject.isHasAddress()) {
                               //     getUserAfterRegister( orderButton );
                               // } else {
                                    goToNextPage( mKwikDeviceButtonId );
                               // }
                            }

                        }

                        @Override
                        public void registerUserError(KwikServerError error) {
                            hideProgressBar();
                            showTwoButtonErrorDialog( getString( R.string.oops ), error.getMessage(), "", getString( R.string.cancel ), null,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent( GoodJobActivity.this, ClientsActivity.class );
                                            startActivity( i );
                                            GoodJobActivity.this.finish();
                                        }
                                    });
                            orderButton.setClickable( true );
                        }
                    } );
                }
            }
        }
    }

    private void sendUpdateButton(KwikButtonDevice button, final View orderButton) {
        KwikMe.updateKwikButtonDeviceWithListener(button, new UpdateKwikButtonListener() {
            @Override
            public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                if (mApp.getButton(button.getId()) == null) {
                    mApp.getButtons().add(button);
                } else {
                    mApp.updateButton(button.getId(), button);
                }
                goToNextPage(button.getId());
            }

            @Override
            public void updateKwikButtonListenerError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
                orderButton.setClickable(true);
            }
        });
    }

    private void goToNextPage(String id) {
        Intent i = null;
        List<KwikProject.AdditionalDataItem> additionalData = mKwikProject.getAdditionalData();
        if (mKwikProject.getProducts() != null && mKwikProject.getProducts().size() > 0) {
            i = new Intent(GoodJobActivity.this, SelectProductActivity.class);
        } else if (mKwikProject.getForm() != null) {
            i = new Intent(GoodJobActivity.this, PersonalDetailsActivity.class);
        } else if(additionalData != null && additionalData.size() >0){
            i = new Intent(GoodJobActivity.this, IpmLoginActivity.class);
            String url = mKwikProject.getAdditionalData().get(0).getUri().replace("{{button.id}}", id);
            Logger.e(TAG, "Url = ", url);
            i.putExtra(WebviewActivity.LOADING_URL, url);
            i.putExtra(WebviewActivity.LOGIN_REDIRECT_URI, mKwikProject.getAdditionalData().get(0).getRedirectUri());
            i.putExtra(WebviewActivity.PAGE_TITLE, mKwikProject.getAdditionalData().get(0).getTitle());
            i.putExtra("buttonId", id);
        }else {
            i = new Intent(GoodJobActivity.this, AllSetUpActivity.class);
        }
        i.putExtra("buttonId", id);
        i.putExtra("sender", LoginActivity.class.getSimpleName());
        startActivity(i);
        finish();
    }

    private void alertDialogView(final View orderButton) {
        final CharSequence[] items = addressList.toArray(new CharSequence[addressList.size()]);


        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.please_select_address);
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mKwikButton.setAddress(mApp.getUser().getAddresses().get(item).getId());
                    }
                });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendUpdateButton(mKwikButton, orderButton);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                hideProgressBar();
                orderButton.setClickable(true);
            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
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
}
