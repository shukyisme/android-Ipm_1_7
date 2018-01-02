package me.kwik.square;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.listeners.UpdateBillingListener;
import me.kwik.rest.responses.UpdateBillingResponse;
import me.kwik.utils.Logger;
import me.kwk.utils.Utils;

public class PaymentWebViewActivity extends BaseActivity {

    private static final String TAG = PaymentWebViewActivity.class.getSimpleName();
    private WebView mWebView;
    private String mLoadingUrl;
    private String mRedirectUri;
    private ProgressBar mProgressBar;
    private Application mApp;
    private String mKwikButtonId;
    private String mSender="";

    public static final String REDIRECT_URL = "me.kwik.me.PaymentWebviewActivity.REDIRECT_URL";
    public static final String LOADING_URL =  "me.kwik.me.PaymentWebviewActivity.LOADING_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_web_view);


        mApp = (Application)getApplication();
        mProgressBar = (ProgressBar)findViewById(R.id.payment_webview_activity_progressBar);

        try{
            mSender = getIntent().getStringExtra("sender");
        }catch (Exception e){
            Logger.e(TAG,"%s",e.getMessage());
        }

        mWebView = (WebView) findViewById(R.id.payment_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mActionBarTitle.setText(R.string.payment_method);

        mLoadingUrl = getIntent().getStringExtra(LOADING_URL);
        if(mLoadingUrl == null || mLoadingUrl.length() == 0){
            Logger.e(TAG,"No load url or empty");
            finish();
        }

        mRedirectUri = getIntent().getStringExtra(REDIRECT_URL);
        if(mRedirectUri == null || mRedirectUri.length() == 0){
            Logger.e(TAG,"No redirect url or empty");
            finish();
        }

        mKwikButtonId = getIntent().getStringExtra("buttonId");
        if(mKwikButtonId == null || mKwikButtonId.length() == 0){
            Logger.e(TAG,"button id is null or empty");
            finish();
        }

        if(KwikMe.USER_ID == null || KwikMe.USER_TOKEN == null){
            Logger.e(TAG,"user id is null or user token is null");
            finish();
        }

        String token = KwikMe.USER_ID + ":" + KwikMe.USER_TOKEN;
        HashMap<String,String> extraHeader = new HashMap<String, String>();
        extraHeader.put("Authorization","Token " + Base64.encodeToString(token.getBytes(), Base64.NO_WRAP));
        extraHeader.put("Accept-Language","en-US");
        mWebView.loadUrl(mLoadingUrl,extraHeader);

        this.mWebView.clearCache(true);
        this.mWebView.clearHistory();
        clearCookies(this);

        this.mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideProgressBar();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                                                    String urlString) {
                hideProgressBar();
                URL url = null;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return false;
                }


                if (urlString != null && urlString.contains(mRedirectUri)) {
                    Map<String, List<String>> urlParams = null;
                    try {
                        urlParams = Utils.splitQuery(url);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return false;
                    }


                    Iterator it = urlParams.entrySet().iterator();
                    HashMap<String, String> params = null;
                    while (it.hasNext()) {
                        Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) it.next();
                        Logger.e(TAG, pair.getKey());
                        if (params != null) {
                            params.put(pair.getKey(), pair.getValue().get(0));
                        } else {
                            params = new HashMap<String, String>();
                            params.put(pair.getKey(), pair.getValue().get(0));
                        }
                        it.remove();
                    }

                    if (params != null) {
                        KwikMe.updateBilling(mKwikButtonId, params, new UpdateBillingListener() {
                            @Override
                            public void updateBillingDone(UpdateBillingResponse response) {
                               if(mSender.equals( SelectProductActivity.class.getSimpleName() )){
                                   Intent i = new Intent( PaymentWebViewActivity.this, ReorderSettingsActivity.class );
                                   i.putExtra( "sender",PaymentWebViewActivity.class.getSimpleName() );
                                   i.putExtra("buttonId", mKwikButtonId);
                                   startActivity( i );
                                   finish();
                               }else{
                                   finish();
                               }
                            }

                            @Override
                            public void updateBillingError(KwikServerError error) {
                                showTwoButtonErrorDialog( getString( R.string.oops ), error.getMessage(), "", getString( R.string.cancel ),


                                        null,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if(mSender.equals( SelectProductActivity.class.getSimpleName() )){
                                                    Intent i = new Intent( PaymentWebViewActivity.this, ReorderSettingsActivity.class );
                                                    i.putExtra( "sender",PaymentWebViewActivity.class.getSimpleName() );
                                                    i.putExtra("buttonId", mKwikButtonId);
                                                    startActivity( i );
                                                    finish();
                                                }else{
                                                    finish();
                                                }
                                            }
                                        }
                                );
                            }
                        });
                    }

                }
                if (!urlString.contains(mRedirectUri)) {
                    //TODO: Remove authorization.
                    String token = KwikMe.USER_ID + ":" + KwikMe.USER_TOKEN;
                    HashMap<String, String> extraHeader = new HashMap<String, String>();
                    extraHeader.put("Authorization", "Token " + Base64.encodeToString(token.getBytes(), Base64.NO_WRAP));
                    extraHeader.put("Accept-Language", "en-US");
                    view.loadUrl(urlString, extraHeader);
                }

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNextImageView.setVisibility(View.INVISIBLE);
    }

    public static void clearCookies(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else{
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}
