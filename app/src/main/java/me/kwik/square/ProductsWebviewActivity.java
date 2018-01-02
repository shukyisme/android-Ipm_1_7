package me.kwik.square;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikAddress;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProject;
import me.kwik.data.KwikUser;
import me.kwik.listeners.GetKwikButtonsListener;
import me.kwik.listeners.GetKwikUserListener;
import me.kwik.listeners.UpdateKwikButtonListener;
import me.kwik.utils.Logger;

public class ProductsWebviewActivity extends BaseActivity {

    private static final String TAG = ProductsWebviewActivity.class.getSimpleName();

    private WebView mWebView;
    private String mLoadingUrl;
    private String mLoginRedirectUri;
    public static final String LOGIN_REDIRECT_URI = "me.kwik.me.WebviewActivity.LOGIN_REDIRECT_URI";
    public static final String LOADING_URL = "me.kwik.me.WebviewActivity.LOADING_URL";
    public static final String PAGE_TITLE = "me.kwik.me.WebviewActivity.PAGE_TITLE";
    public ProgressBar mProgressBar;
    public Application mApp;
    private List<String> addressList = new ArrayList<String>();
    private boolean registerRequestSent = false;
    private String mKwikButtonId;
    private KwikButtonDevice mKwikButton;
    private KwikProject mKwikProject;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mApp = (Application) getApplication();
        mProgressBar = (ProgressBar) findViewById(R.id.webview_activity_progressBar);
        mLoadingUrl = getIntent().getStringExtra(LOADING_URL);
        mLoginRedirectUri = getIntent().getStringExtra(LOGIN_REDIRECT_URI);
        if (getIntent().getStringExtra(PAGE_TITLE) != null) {
            mActionBarTitle.setText(getIntent().getStringExtra(PAGE_TITLE));
        }
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
        showProgressBar();
        HashMap<String, String> extraHeader = new HashMap<String, String>();

        boolean authorization = getIntent().getBooleanExtra("Authorization",true);
        if(authorization) {
            String token = KwikMe.USER_ID + ":" + KwikMe.USER_TOKEN;
            extraHeader.put("Authorization", "Token " + Base64.encodeToString(token.getBytes(), Base64.NO_WRAP));
        }
        extraHeader.put("Accept-Language", KwikMe.LOCAL);
        mWebView.loadUrl(mLoadingUrl, extraHeader);

        this.mWebView.clearCache(true);
        this.mWebView.clearHistory();
        clearCookies(this);


        this.mWebView.setWebViewClient(new WebViewClient() {
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

                try {
                    if (urlString != null && urlString.contains(mLoginRedirectUri)) {
                        KwikMe.getKwikButtons(null,new GetKwikButtonsListener() {
                            @Override
                            public void getKwikButtonsDone(final List<KwikButtonDevice> buttons) {
                                mApp.setButtons(buttons);
                                Intent i = new Intent(ProductsWebviewActivity.this,ClientsActivity.class);
                                i.putExtra("sender",ProductsWebviewActivity.class.getSimpleName());
                                startActivity(i);
                                ProductsWebviewActivity.this.finish();

                            }

                            @Override
                            public void getKwikButtonsError(KwikServerError error) {

                            }
                        });


                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                if (!urlString.contains(mLoginRedirectUri)) {
                    String token = KwikMe.USER_ID + ":" + KwikMe.USER_TOKEN;
                    HashMap<String, String> extraHeader = new HashMap<String, String>();
                    extraHeader.put("Authorization", "Token " + Base64.encodeToString(token.getBytes(), Base64.NO_WRAP));
                    extraHeader.put("Accept-Language", "en-US");
                    view.loadUrl(urlString, extraHeader);
                }

//                KwikMe.getKwikButtons(new GetKwikButtonsListener() {
//                    @Override
//                    public void getKwikButtonsDone(List<KwikButtonDevice> buttons) {
//                        mApp.getButtons().set(0, buttons.get(0));
//                    }
//
//                    @Override
//                    public void getKwikButtonsError(KwikServerError error) {
//
//                    }
//                });
                return true;
            }


        });
    }

    private void selectNextScreen(KwikButtonDevice button) {
        List<KwikProject.AdditionalDataItem> additionalData = mKwikProject.getAdditionalData();

        Intent i = null;
        if (mKwikProject.isHasCatalog()) {
            i = new Intent(ProductsWebviewActivity.this, SelectProductActivity.class);
        } else if (mKwikProject.getForm() != null) {
            i = new Intent(ProductsWebviewActivity.this, PersonalDetailsActivity.class);
        }else if(additionalData != null && additionalData.size() >0) {
            i = new Intent(ProductsWebviewActivity.this, ProductsWebviewActivity.class);
            String url = mKwikProject.getAdditionalData().get(0).getUri().replace("{{button.id}}", button.getId());
            Logger.e(TAG, "Url = ", url);
            i.putExtra(ProductsWebviewActivity.LOADING_URL, url);
            i.putExtra(ProductsWebviewActivity.LOGIN_REDIRECT_URI, mKwikProject.getAdditionalData().get(0).getRedirectUri());
            i.putExtra(ProductsWebviewActivity.PAGE_TITLE, mKwikProject.getAdditionalData().get(0).getTitle());
            i.putExtra("buttonId", button.getId());
        }else {
            i = new Intent(ProductsWebviewActivity.this, AllSetUpActivity.class);
        }
        i.putExtra("buttonId", button.getId());
        i.putExtra("sender", ProductsWebviewActivity.class.getSimpleName());
        startActivity(i);
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

    @Override
    protected void onResume() {
        super.onResume();
        mKwikButtonId = getIntent().getStringExtra("buttonId");
        mKwikButton = mApp.getButton(mKwikButtonId);
        mKwikProject = mApp.getProject(mKwikButton.getProject());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                int color = Color.parseColor(mKwikProject.getClient().getColorBackground());
                if (color != 0)
                    window.setStatusBarColor(color);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    private void getUserAfterRegister(final String buttonId) {
        KwikMe.getKwikUser(KwikMe.USER_ID, new GetKwikUserListener() {
            @Override
            public void getKwikUserDone(KwikUser user) {
                mApp.setUser(user);
                addAddressToButton(buttonId);
            }

            @Override
            public void getKwikUserError(KwikServerError error) {
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
            }
        });

    }

    private void addAddressToButton(String buttonId) {
        List<KwikAddress> addresses = mApp.getUser().getAddresses();
        KwikButtonDevice button = mApp.getButton(buttonId);
        if (addresses == null || addresses.size() == 0) {
            selectNextScreen(button);
            return;
        }

        if (addresses.size() == 1) {
            button.setAddress(addresses.get(0).getId());
            sendUpdateButton(button);
        } else if (addresses.size() > 1) {
            for (KwikAddress a : addresses) {
                String address = null;

                if (a.getDisplayText() != null) address = a.getDisplayText() + "\n";
                if (address == null) {
                    if (a.getState() != null) {
                        address += a.getState();
                    }
                    if (a.getCity() != null) {
                        address += ", " + a.getCity();
                    }
                    if (a.getStreet() != null) {
                        address += ", " + a.getStreet();
                    }
                    if (a.getStreetNumber() != null) {
                        address += ", " + a.getStreetNumber();
                    }
                }
                if (a.getEntrance() != null)
                    address += getString(R.string.entrance) + ": " + a.getEntrance() + "\n";
                if (a.getApt() != null)
                    address += getString(R.string.apartment) + ": " + a.getApt();


                if (address != null) {
                    addressList.add(address);
                }
            }
            alertDialogView(button);
        }
    }

    private void sendUpdateButton(KwikButtonDevice button) {
        KwikMe.updateKwikButtonDeviceWithListener(button, new UpdateKwikButtonListener() {
            @Override
            public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                if (mApp.getButton(button.getId()) == null) {
                    mApp.getButtons().add(button);
                } else {
                    mApp.updateButton(button.getId(), button);
                }

                selectNextScreen(button);
                finish();
            }

            @Override
            public void updateKwikButtonListenerError(KwikServerError error) {
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
            }
        });
    }

    private void alertDialogView(final KwikButtonDevice button) {
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
                        button.setAddress(mApp.getUser().getAddresses().get(item).getId());
                    }
                });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendUpdateButton(button);
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


    @Override
    public void onBackPressed() {
        //Do nothing
    }
}
