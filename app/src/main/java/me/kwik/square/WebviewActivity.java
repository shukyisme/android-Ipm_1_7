package me.kwik.square;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikButtonDevice;
import me.kwik.listeners.RegisterUserListener;
import me.kwik.utils.Logger;
import me.kwik.utils.Utils;


public class WebviewActivity extends BaseActivity {

    private static final String TAG = WebviewActivity.class.getSimpleName();

    private WebView mWebView;
    private String mLoadingUrl;
    public static final String LOGIN_REDIRECT_URI = "me.kwik.me.WebviewActivity.LOGIN_REDIRECT_URI";
    public static final String LOADING_URL = "me.kwik.me.WebviewActivity.LOADING_URL";
    public static final String PAGE_TITLE = "me.kwik.me.WebviewActivity.PAGE_TITLE";
    public ProgressBar mProgressBar;
    public Application mApp;
    private List<String> addressList = new ArrayList<String>();
    private HashMap<String,Object> loginParams;
    private String mKwikButtonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        getSupportActionBar().hide();
        mApp = (Application) getApplication();
        mProgressBar = (ProgressBar) findViewById(R.id.webview_activity_progressBar);
        mLoadingUrl = getIntent().getStringExtra(LOADING_URL);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        showProgressBar();
        HashMap<String, String> extraHeader = new HashMap<String, String>();

        boolean authorization = getIntent().getBooleanExtra("Authorization",true);
        if(authorization) {
            String token = KwikMe.USER_ID + ":" + KwikMe.USER_TOKEN;
            extraHeader.put("Authorization", "Token " + Base64.encodeToString(token.getBytes(), Base64.NO_WRAP));
        }
        extraHeader.put("Accept-Language", KwikMe.LOCAL);


        this.mWebView.clearCache(true);
        this.mWebView.clearHistory();
        clearCookies(this);
        mWebView.loadUrl(mLoadingUrl, extraHeader);

        this.mWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                                                    String url) {
                if(url.contains("/#/my_kwik_buttons")) {

                    KwikButtonDevice button = mApp.getButton(mKwikButtonId);
                    //create login params.
                    //'clientName', 'clientId', 'locationName', 'locationId', 'coordinator', 'buttonName', 'description', 'clientPhoneNumber', 'managerPhoneNumber'

                    String[] parts = url.split("\\?");
                    loginParams = (HashMap<String, Object>) Utils.getQueryMap(parts[1]);
                    Logger.i(TAG,loginParams.toString());
                    button.setLoginParams(loginParams);


                    //register button

                    KwikMe.registerUser(button, new RegisterUserListener() {
                        @Override
                        public void registerUserDone(KwikButtonDevice button) {
                            //registerRequestSent = false;
                            if (mApp.getButton(button.getId()) == null) {
                                mApp.getButtons().add(button);
                            } else {
                                mApp.updateButton(button.getId(), button);
                            }

                            if (mApp.getProject(button.getProject()).isHasAddress()) {
                                //getUserAfterRegister(button.getId());
                            } else {
                                //selectNextScreen(button);

                            }

                            Intent i = new Intent(WebviewActivity.this, ClientsActivity.class);
                            i.putExtra("sender", WebviewActivity.class.getSimpleName());
                            startActivity(i);
                            finish();
                        }

                        @Override
                        public void registerUserError(KwikServerError error) {
                            //registerRequestSent = false;
                            showTwoButtonErrorDialog( getString( R.string.oops ), error.getMessage(), getString( R.string.register_later ), getString( R.string.cancel ),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(WebviewActivity.this, ClientsActivity.class);
                                            i.putExtra("sender", WebviewActivity.class.getSimpleName());
                                            startActivity(i);
                                            finish();
                                        }
                                    }, null);
                        }
                    });

                    return true;
                }
                Logger.e(TAG,"shouldOverrideUrlLoading url = " + url);
                if(url.equals("http://127.0.0.1/") || url.contains("/wifi_selection") || url.contains("/connect_button")){
                    Intent i = new Intent(WebviewActivity.this, WiFiSelectionActivity.class);
                    //i.putExtra("buttonId", mKwikButtonId);
                    //i.putExtra("sender", LoginActivity.class.getSimpleName());
                    startActivity(i);
                    return true;
                }else if(url.equals("http://127.0.0.2/")) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder( WebviewActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert );
                    } else {
                        builder = new AlertDialog.Builder( WebviewActivity.this );
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
                    return true;
                }else if(url.equals("http://127.0.0.3/")) {
                    Intent i = new Intent( WebviewActivity.this, NetworkPasswordActivity.class );
                    i.putExtra( "sender", ButtonSettingsActivity.class.getSimpleName() );
                    startActivity( i );
                    return true;
                }else{
                    view.loadUrl(url);
                }

            return true;
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url  = request.getUrl().toString();
                Logger.e(TAG,"shouldOverrideUrlLoading request.getUrl().toString() = " + url);
                if(url.isEmpty()){
                    return super.shouldOverrideUrlLoading(view, request);
                }else{

                    if(url.contains("/#/my_kwik_buttons")) {

                        KwikButtonDevice button = mApp.getButton(mKwikButtonId);
                        //create login params.
                        //'clientName', 'clientId', 'locationName', 'locationId', 'coordinator', 'buttonName', 'description', 'clientPhoneNumber', 'managerPhoneNumber'

                        String[] parts = url.split("\\?");
                        loginParams = (HashMap<String, Object>) Utils.getQueryMap(parts[1]);
                        Logger.i(TAG,loginParams.toString());
                        button.setLoginParams(loginParams);


                        //register button

                        KwikMe.registerUser(button, new RegisterUserListener() {
                            @Override
                            public void registerUserDone(KwikButtonDevice button) {
                                //registerRequestSent = false;
                                if (mApp.getButton(button.getId()) == null) {
                                    mApp.getButtons().add(button);
                                } else {
                                    mApp.updateButton(button.getId(), button);
                                }

                                if (mApp.getProject(button.getProject()).isHasAddress()) {
                                    //getUserAfterRegister(button.getId());
                                } else {
                                    //selectNextScreen(button);

                                }

                                Intent i = new Intent(WebviewActivity.this, ClientsActivity.class);
                                i.putExtra("sender", WebviewActivity.class.getSimpleName());
                                startActivity(i);
                                finish();
                            }

                            @Override
                            public void registerUserError(KwikServerError error) {
                                //registerRequestSent = false;
                                showTwoButtonErrorDialog( getString( R.string.oops ), error.getMessage(), getString( R.string.register_later ), getString( R.string.cancel ),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent i = new Intent(WebviewActivity.this, ClientsActivity.class);
                                                i.putExtra("sender", WebviewActivity.class.getSimpleName());
                                                startActivity(i);
                                                finish();
                                            }
                                        }, null);
                            }
                        });



                        //go to my buttons screen

                        //Don't upload the url
                        return true;
                    }

                    if(url.equals("http://127.0.0.1/")){
                        Intent i = new Intent(WebviewActivity.this, WiFiSelectionActivity.class);
                        //i.putExtra("buttonId", mKwikButtonId);
                        //i.putExtra("sender", LoginActivity.class.getSimpleName());
                        startActivity(i);
                        return true;
                    }else if(url.equals("http://127.0.0.2/")) {
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder( WebviewActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert );
                        } else {
                            builder = new AlertDialog.Builder( WebviewActivity.this );
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
                        return true;
                    }else{
                        return super.shouldOverrideUrlLoading(view, request);
                    }
                }

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //super.onPageFinished(view, url);
                hideProgressBar();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mWebView.getProgress() != 100){
                    mWebView.reload();
                }
            }
        }, 10000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mKwikButtonId = getIntent().getStringExtra("buttonId");
    }



    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private void alertDialogView(final KwikButtonDevice button) {
        final CharSequence[] items = addressList.toArray(new CharSequence[addressList.size()]);

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
                        button.setAddress(mApp.getUser().getAddresses().get(item).getId());
                    }
                });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               // sendUpdateButton(button);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

    @Override
    protected void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
