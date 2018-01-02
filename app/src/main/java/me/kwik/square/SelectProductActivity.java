package me.kwik.square;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikOrder;
import me.kwik.data.KwikProduct;
import me.kwik.data.KwikProject;
import me.kwik.data.KwikSelectedProduct;
import me.kwik.data.MobileDevice;
import me.kwik.listeners.GetBillingListener;
import me.kwik.listeners.GetProductCatalogListener;
import me.kwik.listeners.UpdateKwikButtonListener;
import me.kwik.listeners.UpdateKwikOrderListener;
import me.kwik.rest.responses.GetBillingResponse;
import me.kwik.utils.Logger;

public class SelectProductActivity extends BaseActivity {

    private ListView mProductsListView;
    private List<ImageView> mProductsImages = new ArrayList<ImageView>();
    private ProductsArrayAdapter mProductsAddapter ;
    private List<KwikSelectedProduct> mSelectedProducts = new ArrayList<KwikSelectedProduct>();
    private ProgressBar mBar;
    private String TAG = SelectProductActivity.class.getSimpleName();
    private Application mApp;
    private String mSender="";
    private KwikButtonDevice mButtonBeforeChanges;
    private List<KwikSelectedProduct> mProductsBeforeChanges = new ArrayList<KwikSelectedProduct>();
    private String mKwikButtonId;
    private KwikButtonDevice mButton;
    private KwikProject mProject;
    private String mKwikOrderId;
    private Button mModifyOrder;
    private ProgressBar mUpdateProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product);

        mKwikOrderId = getIntent().getStringExtra( "order" );
        mModifyOrder =  (Button)findViewById( R.id.modify_order_button );
        mUpdateProgressBar = (ProgressBar)findViewById( R.id.select_product_activity_update_progress_bar );


        //If the app opened by sms edit click, change the screen ui
        if (Application.LAUNCH_MODE == Application.LAUNCH_MODE_EDIT){
            showTwoButtonErrorDialog( "",getString( R.string.update_order_message),getString(android.R.string.ok),"",null,null);
            mActionBarTitle.setText(R.string.select_products_activity_edit_title);
            mModifyOrder.setVisibility( View.VISIBLE );
            mModifyOrder.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUpdateProgressBar.setVisibility( View.VISIBLE );
                    mModifyOrder.setText( R.string.modifying );
                    inhirtedClickNext();
                }
            } );

        }else {
            mActionBarTitle.setText(R.string.select_products_activity_title);
            mModifyOrder.setVisibility( View.INVISIBLE );
        }
        mPrevTextView.setVisibility(View.INVISIBLE);
        mProductsListView = (ListView)findViewById(R.id.select_product_activity_products_list_view);
        mBar = (ProgressBar)findViewById(R.id.select_product_activity_progress_bar);
        mApp = (Application) getApplication();
        mBar.setVisibility(View.VISIBLE);

        //Adding header to the product list
        TextView textView = new TextView(this);
        textView.setText(R.string.select_products_activity_choose);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 0, 0, 20);
        mProductsListView.addHeaderView(textView);



    }

    @Override
    protected void onResume() {
        super.onResume();

        mKwikButtonId = getIntent().getStringExtra("buttonId");
        if(mKwikButtonId == null){
            return;
        }
        mButton = mApp.getButton(mKwikButtonId);
        mProject = mApp.getProject(mButton.getProject());

        if (projectIcon()) return;

        getProductCatalog(mKwikButtonId);


        if(mButtonBeforeChanges == null && mApp.getButtons() != null && mApp.getButtons().size() > 0){

            mButtonBeforeChanges = new KwikButtonDevice(mButton);
            if(mButtonBeforeChanges != null && mButtonBeforeChanges.getProducts() != null) {
                for (KwikSelectedProduct p : mButtonBeforeChanges.getProducts()) {
                    mProductsBeforeChanges.add(new KwikSelectedProduct(p));
                }
            }
        }
        List<KwikSelectedProduct> products = null;
        try {
            products = mButton.getProducts();
        }catch (Exception e){
            Logger.e(TAG,"onResume : %s", e.getMessage());
        }

        try{
            mSender = getIntent().getStringExtra("sender");
        }catch (Exception e){
            Logger.e(TAG,"%s",e.getMessage());
        }

        if(products != null) {
            mSelectedProducts = products;
        }

        if (Application.LAUNCH_MODE == Application.LAUNCH_MODE_EDIT) {
            mPrevTextView.setVisibility(View.VISIBLE);
            mPrevTextView.setText( "" );
            mPrevTextView.setCompoundDrawablesWithIntrinsicBounds( android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
            mPrevTextView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Application.LAUNCH_MODE = Application.LAUNCH_MODE_NORMAL;
                    Intent i = new Intent( SelectProductActivity.this, ClientsActivity.class );
                    startActivity( i );
                    SelectProductActivity.this.finish();
                }
            } );
            mNextImageView.setVisibility( View.INVISIBLE );
        }else{
            mPrevTextView.setVisibility(View.INVISIBLE);
            mNextImageView.setVisibility( View.VISIBLE );
        }

    }

    private boolean projectIcon() {
        ImageView logoImageView = (ImageView) findViewById(R.id.select_product_title_image_view);
        try{
            new DownloadImageTask(logoImageView).execute(mProject.getLogos().get(MobileDevice.DENSITY));
        }catch (Exception e){
            Logger.e(TAG,"MyButtonsArrayAdapter.getView Exception %s",e.getMessage());
            return true;
        }
        return false;
    }

    private void getProductCatalog(final String buttonId) {
        mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                .setAction("request")
                .setLabel("Get product catalog")
                .build());
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
                mProject.setProducts(products);
                mBar.setVisibility(View.GONE);
                for (KwikProduct product : products) {

                    ImageView v = new ImageView(SelectProductActivity.this);
                    if (product.getImages() != null) {
                        new DownloadImageTask(v).execute(product.getImages().get(MobileDevice.DENSITY));
                    }else{
                        new DownloadImageTask(v).execute("");
                    }
                    mProductsImages.add(v);
                }
                mProductsAddapter = new ProductsArrayAdapter(SelectProductActivity.this, products);
            }

            @Override
            public void getProductCatalogError(KwikServerError error) {
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Get product catalog")
                        .setValue(1)
                        .build());
                showOneButtonErrorDialog(getString(R.string.oops),error.getMessage());
                mNextImageView.setClickable(true);
            }
        });
    }

    @Override
    protected void clickNext() {
        super.clickNext();
        inhirtedClickNext();
    }

    private void inhirtedClickNext() {
        double totalPrice = 0;

        if(mSelectedProducts!= null ) {
            if (mSelectedProducts.size() == 0) {
                showOneButtonErrorDialog("",getString( R.string.select_products_activity_no_productd_choosen));
                hideNextSpinner();
                return;
            }else{
                List<KwikSelectedProduct> searchedProduct = new ArrayList<>();
                for(KwikSelectedProduct product:mSelectedProducts){
                    if(getKwikProductById(product.getId())!= null) {
                        double c = getKwikProductById(product.getId()).getAmount() * getKwikProductById(product.getId()).getPrice().getRetail();
                        totalPrice += c;
                        if (product.getCount() == 0) {
                            searchedProduct.add(product);
                        }
                    }
                }
                if (searchedProduct.size() == 0) {
                   // chooseNextScreen();
                }else{
                    for(KwikSelectedProduct product:searchedProduct){
                        mSelectedProducts.remove(product);
                    }
                }
            }
        }else{
            showOneButtonErrorDialog("",getString(R.string.select_products_activity_no_productd_choosen));
            hideNextSpinner();
            mUpdateProgressBar.setVisibility( View.GONE );
            mModifyOrder.setText( R.string.modify_order );
            return;
        }

        if (mSelectedProducts.size() == 0) {
            showOneButtonErrorDialog("", getString(R.string.select_products_activity_no_productd_choosen));
            mUpdateProgressBar.setVisibility( View.GONE );
            mModifyOrder.setText( R.string.modify_order );
            return;
        }
        mButton.setProducts(mSelectedProducts);
        //TODO: Bug fix, Need to be fixed int the server, The server return empty timeSlots after wifi teach
        if(mButton.getDeliveryTimeSlots() != null && mButton.getDeliveryTimeSlots().getHourDescription() == ""){
            mApp.getButton(mKwikButtonId).setDeliveryTimeSlots(null);
        }

        if(totalPrice < mProject.getMinimumPrice()){
            double x =  mProject.getMinimumPrice();
            if(x==Math.round(x)){
                showOneButtonErrorDialog("", getString(R.string.select_products_activity_minimum_price_prefix) + Math.round(x) + " " + getKwikProductById(mSelectedProducts.get(0).getId()).getPrice().getCurrencySymbol() + getString(R.string.select_products_activity_minimum_price_suffix));
            }else {
                showOneButtonErrorDialog("", getString(R.string.select_products_activity_minimum_price_prefix) + x + " " + getKwikProductById(mSelectedProducts.get(0).getId()).getPrice().getCurrencySymbol() + getString(R.string.select_products_activity_minimum_price_suffix));
            }
            hideNextSpinner();
            mUpdateProgressBar.setVisibility( View.GONE );
            mModifyOrder.setText( R.string.modify_order );
            return;
        }

        mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                .setAction("request")
                .setLabel("Update kwik button")
                .build());
        KwikMe.updateKwikButtonDeviceWithListener(mButton, new UpdateKwikButtonListener() {
            @Override
            public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                mUpdateProgressBar.setVisibility( View.GONE );
                mModifyOrder.setText( R.string.modify_order );
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Update kwik button")
                        .setValue(0)
                        .build());
                if (Application.LAUNCH_MODE == Application.LAUNCH_MODE_EDIT){
                    OrderClicked();
                    return;
                }
                if(mApp.getButton(button.getId()) == null) {
                    mApp.getButtons().add(button);
                }else{
                    mApp.updateButton(button.getId(),button);
                }
                chooseNextScreen();
            }

            @Override
            public void updateKwikButtonListenerError(KwikServerError error) {
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Update kwik button")
                        .setValue(1)
                        .build());
                mUpdateProgressBar.setVisibility( View.GONE );
                mModifyOrder.setText( R.string.modify_order );
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
            }
        });
    }

    private void OrderClicked() {
        Application.LAUNCH_MODE = Application.LAUNCH_MODE_NORMAL;
        mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                .setAction("request")
                .setLabel("Create order")
                .build());

        if(mKwikOrderId != null) {
                   KwikMe.updateOrder( mKwikOrderId, null, new UpdateKwikOrderListener() {
                        @Override
                        public void updateKwikOrderDone(KwikOrder order) {
                            mUpdateProgressBar.setVisibility( View.GONE );
                            mModifyOrder.setText( R.string.modify_order );
                            if(order != null) {
                                showTwoButtonErrorDialog( order.getMessage(), getString( R.string.select_products_activity_save_changes ), getString( R.string.yes ), getString( R.string.no ),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent i = new Intent( SelectProductActivity.this, ClientsActivity.class );
                                                startActivity( i );
                                                SelectProductActivity.this.finish();
                                            }
                                        },

                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mButton.setProducts( mProductsBeforeChanges );
                                                KwikMe.updateKwikButtonDeviceWithListener( mButton, new UpdateKwikButtonListener() {
                                                    @Override
                                                    public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                                                        if (mApp.getButton( button.getId() ) == null) {
                                                            mApp.getButtons().add( button );
                                                        } else {
                                                            mApp.updateButton( button.getId(), button );
                                                        }
                                                        Intent i = new Intent( SelectProductActivity.this, ClientsActivity.class );
                                                        startActivity( i );
                                                        SelectProductActivity.this.finish();
                                                    }

                                                    @Override
                                                    public void updateKwikButtonListenerError(KwikServerError error) {

                                                    }
                                                } );
                                            }
                                        } );
                            }
                        }

                        @Override
                        public void updateKwikOrderError(KwikServerError error) {
                            mUpdateProgressBar.setVisibility( View.GONE );
                            mModifyOrder.setText( R.string.modify_order );
                            Logger.e(TAG, error.getMessage());
                            showOneButtonErrorDialog(getString(R.string.oops),error.getMessage());
                        }
                    } );
        }
    }

    private KwikProduct getKwikProductById(String id){
        List<KwikProduct> projectProducts;
        projectProducts = mProject.getProducts();
        for(KwikProduct product:projectProducts){
            if(product.getId().equals(id)){
                return product;
            }
        }
        return null;
    }

    private int getProductIndexById(String id){
        List<KwikProduct> projectProducts;
        projectProducts = mProject.getProducts();

        for(int i =0; i < projectProducts.size(); i++){
            if(projectProducts.get(i).getId().equals(id)){
                return i;
            }
        }
        return -1;
    }

    private void chooseNextScreen() {
        Intent i;
        if(mSender == null){
            finish();
            return;
        }
        if(mProject.getAdditionalData() != null && mProject.getAdditionalData().size() >0 ){

            i = new Intent(SelectProductActivity.this,WebviewActivity.class);
            String url = mProject.getAdditionalData().get(0).getUri().replace("{{button.id}}",mKwikButtonId);
            Logger.e(TAG,"Url = ",url);
            i.putExtra(WebviewActivity.LOADING_URL,url);
            i.putExtra(WebviewActivity.LOGIN_REDIRECT_URI,mProject.getAdditionalData().get(0).getRedirectUri());
        }else {
            if (mSender.equals(ReorderSettingsActivity.class.getSimpleName())) {
                i = new Intent(SelectProductActivity.this, ReorderSettingsActivity.class);
            }else if(mProject.isHasAddress()) {
                i = new Intent(SelectProductActivity.this,AddressActivity.class);
                i.putExtra("mode",AddressActivity.FIRST_FLOW);
            }else if (mProject.getForm() != null) {
                i = new Intent(SelectProductActivity.this, PersonalDetailsActivity.class);
            }else if (mProject.getDeliveryTimeSlots() != null && mProject.getDeliveryTimeSlots().size() > 0) {
                i = new Intent( SelectProductActivity.this, DeliveryActivity.class );
            }else if(mProject.isHasPayment()){
                paymentMethod();
                return;
            }else {
                i = new Intent(SelectProductActivity.this, ReorderSettingsActivity.class);
            }
        }
        i.putExtra("buttonId",mKwikButtonId);
        i.putExtra("sender", LoginActivity.class.getSimpleName());
        startActivity(i);
    }

    private void paymentMethod() {
        KwikMe.getBillingUrl(mKwikButtonId, new GetBillingListener() {
            @Override
            public void getBillingDone(GetBillingResponse response) {
                Intent i = new Intent(SelectProductActivity.this, PaymentWebViewActivity.class);

                String url = response.getBillingUrl();
                if (url == null || url.length() == 0) {
                    Logger.e(TAG, "loading url is null");
                    return;
                }

                String redirectUrl = response.getBillingRedirectUrl();
                if (redirectUrl == null || redirectUrl.length() == 0) {
                    Logger.e(TAG, "redirect url is null");
                    return;
                }

                i.putExtra(PaymentWebViewActivity.LOADING_URL, url);
                i.putExtra(PaymentWebViewActivity.REDIRECT_URL, redirectUrl);
                i.putExtra("buttonId", mKwikButtonId);
                i.putExtra( "sender", SelectProductActivity.class.getSimpleName() );
                startActivity(i);
            }

            @Override
            public void getBillingError(KwikServerError error) {
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
            }
        });
    }

    public class ProductsArrayAdapter extends ArrayAdapter<KwikProduct> {
        private final Context context;
        private  List<KwikProduct> values;

        public ProductsArrayAdapter(Context context, List<KwikProduct> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;

            for(int i = 0; i < values.size(); i++) {
                if (mSelectedProducts != null) {
                    for (KwikSelectedProduct p : mSelectedProducts) {
                        if (p.getId().equals(values.get(i).getId())) {
                            this.values.get(i).setAmount(p.getCount());
                            this.values.get(i).setSelected(true);
                        }
                    }
                }
            }

           // sortProducts(values);


        }

        private void sortProducts(List<KwikProduct> values) {
            //Sort products and make selected ones in the top of the list
            List<KwikProduct> tempValues = new ArrayList<KwikProduct>();
            List<ImageView> tempImageViews = new ArrayList<ImageView>();
            tempImageViews.clear();

            //int i =0;
            if(mSelectedProducts != null){
                for(KwikSelectedProduct p : mSelectedProducts){
                    if(getProductIndexById(p.getId()) != -1) {
                        tempValues.add(getKwikProductById(p.getId()));
//                        tempImageViews.add(mProductsImages.get(getProductIndexById(p.getId())));
             //           i++;
                    }
                }
            }

            //i=0;
            if(values != null && values.size() > 0){
                for(KwikProduct p: values){
                    if(!isSelectedProduct(p)){
                        tempValues.add(getKwikProductById(p.getId()));
  //                      tempImageViews.add(mProductsImages.get(getProductIndexById(p.getId())));

                        //tempImageViews.add(mProductsImages.get(p));
                    }
              //      i++;
                }
            }
            this.values = tempValues;
            for(KwikProduct p: tempValues){
                tempImageViews.add(mProductsImages.get(getProductIndexById(p.getId())));
            }
            mProductsImages = tempImageViews;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.product_list_item_layout, parent, false);

            //Product image
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

            Drawable drawable = mProductsImages.get(position).getDrawable();
            if(drawable!= null) {
                imageView.setImageDrawable(drawable);
            }

            final LinearLayout textLayout = (LinearLayout)rowView.findViewById(R.id.secondLine);
            //Product name
            final TextView textViewName = (TextView) rowView.findViewById(R.id.product_list_item_product_name_text_view);
            textViewName.setText(values.get(position).getName());
            if(values.get(position).isSelected()){
                textViewName.setMaxLines(3);
            }else{
                textViewName.setMaxLines(1);
            }

            //Product description
            final TextView textViewDescription = (TextView) rowView.findViewById(R.id.product_list_item_product_description_text_view);
            if(values.get(position).getDescription() == null || values.get(position).getDescription().isEmpty()){
                textViewDescription.setVisibility(View.GONE);
            }else {
                textViewDescription.setVisibility(View.VISIBLE);
                textViewDescription.setText(values.get(position).getDescription());
            }

            //Product price
            final TextView textViewPrice = (TextView) rowView.findViewById(R.id.product_list_item_product_price_text_view);

            double price  = 0;
            if(values.get(position).getAmountMin() >= values.get(position).getAmount() && values.get(position).isSelected()) {
                price = values.get(position).getPrice().getRetail() * values.get(position).getAmountMin();
            }else {
                price = values.get(position).getPrice().getRetail() * values.get(position).getAmount();
            }

            price = Math.round(price * 100.0) / 100.0;
            if(values.get(position).isSelected()){
                textViewPrice.setVisibility( View.VISIBLE );
            }else{
                textViewPrice.setVisibility( View.GONE );
            }
            textViewPrice.setText(values.get(position).getPrice().getCurrencySymbol() + price);

            LinearLayout linearLayoutProductAmount = (LinearLayout)rowView.findViewById(R.id.product_list_item_product_amount_lay_out);
            ImageButton plusIcon = (ImageButton)rowView.findViewById(R.id.product_list_item_plus_icon_image_button);
            ImageButton minusIcon = (ImageButton)rowView.findViewById(R.id.product_list_item_minus_icon_image_button);
            final TextView amount = (TextView)rowView.findViewById(R.id.product_list_item_product_amount_text_view);

            if(values.get(position).getAmount() < values.get(position).getAmountMin()){
                amount.setText(String.valueOf(Math.round(values.get(position).getAmountMin())));
            }else {
                amount.setText(String.valueOf(Math.round(values.get(position).getAmount())));
            }
            plusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(values.get(position).isSelected()){

                        int prev = Integer.valueOf(amount.getText().toString());
                        if(prev < values.get(position).getAmountMax()) {
                            values.get(position).setAmount(prev + 1);
                            if(mSelectedProducts != null){
                                if(mSelectedProducts.size() == 0){
                                    mSelectedProducts.add(new KwikSelectedProduct(values.get(position).getId(), 1));
                                    notifyDataSetChanged();
                                }
                                for(KwikSelectedProduct p : mSelectedProducts){
                                    if(p.getId().equals(values.get(position).getId())){
                                        p.setCount(prev + 1);
                                    }
                                }
                            }
                        }

                    }
                    notifyDataSetChanged();
                }
            });

            minusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(values.get(position).isSelected()){

                        int prev = Integer.valueOf(amount.getText().toString());
                        if(prev > values.get(position).getAmountMin()) {
                            values.get(position).setAmount(prev - 1);
                            if(mSelectedProducts != null){
                                for(KwikSelectedProduct p : mSelectedProducts){
                                    if(p.getId().equals(values.get(position).getId())){
                                        p.setCount(prev - 1);
                                    }
                                }
                            }
                        }
                    }
                    notifyDataSetChanged();
                }
            });
            //Product selector
            ImageView selectorImageView = (ImageView) rowView.findViewById(R.id.product_list_item_selector);
            if(Math.round(values.get(position).getAmountMax()) == 0){
                selectorImageView.setVisibility(View.INVISIBLE);
                linearLayoutProductAmount.setVisibility(View.GONE);
                textViewPrice.setVisibility(View.INVISIBLE);

            }else {
                if (values.get(position).isSelected()) {
                    selectorImageView.setImageResource(R.drawable.product_select__selected);
                    selectorImageView.setSelected(true);
                    textLayout.setSelected(true);
                    textViewName.setSelected(true);
                    textViewDescription.setSelected(true);
                    textViewPrice.setSelected(true);
                    linearLayoutProductAmount.setVisibility(View.VISIBLE);


                } else {
                    selectorImageView.setImageResource(R.drawable.product_select__deselected);
                    selectorImageView.setSelected(false);
                    textLayout.setSelected(false);
                    textViewName.setSelected(false);
                    textViewDescription.setSelected(false);
                    textViewPrice.setSelected(false);
                    linearLayoutProductAmount.setVisibility(View.GONE);

                }

            }
            selectorImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!values.get(position).isSelected()){
                        values.get(position).setSelected(true);
                        if(Math.round(values.get(position).getAmountMin()) >0) {
                            mSelectedProducts.add(new KwikSelectedProduct(values.get(position).getId(), Math.round(values.get(position).getAmountMin())));
                            values.get(position).setAmount(Math.round(values.get(position).getAmountMin()));
                        }else {
                            mSelectedProducts.add(new KwikSelectedProduct(values.get(position).getId(), 1));
                        }
                    }else{
                        values.get(position).setSelected(false);
                        values.get(position).setAmount(1);
                        removeProduct(values.get(position).getId());
                    }
                    notifyDataSetChanged();
                }
            });

            return rowView;
        }
    }

    private boolean isSelectedProduct(KwikProduct p) {
        for(KwikSelectedProduct product:mSelectedProducts){
            if(product.getId().equals(p.getId())){
                return true;
            }
        }
        return false;
    }

    private void removeProduct(String id) {
        KwikSelectedProduct searchedProduct = null;
        if(mSelectedProducts == null || mSelectedProducts.isEmpty()){
            return;
        }

        for(KwikSelectedProduct product:mSelectedProducts){
            if(product.getId().equals(id)){
                searchedProduct = product;
               break;
            }
        }

        mSelectedProducts.remove(searchedProduct);
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String fileName;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            if(urldisplay.equals("")){
                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.button_to_ap_animation_frame1);
                return icon;
            }
            String pattern = Pattern.quote(System.getProperty("file.separator"));
            String[] splittedFileName = urldisplay.split(pattern);

            File folder = new File(Environment.getExternalStorageDirectory() + "/" + Application.IMAGES_FOLDER_NAME);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (!success) {
                return null;
            }

            fileName = Application.IMAGES_FOLDER_NAME + "/" + splittedFileName[splittedFileName.length - 1];
            File sdCardDirectory = Environment.getExternalStorageDirectory();
            File image = new File(sdCardDirectory, fileName);
            Bitmap mIcon11 = null;
            if(image.exists()) {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                mIcon11 = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

            }else{

                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Logger.d(TAG, "save file %s", fileName);

                FileOutputStream outStream;
                try {

                    outStream = new FileOutputStream(image);
                    mIcon11.compress(Bitmap.CompressFormat.PNG, 100, outStream);

                    outStream.flush();
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

            bmImage.setImageBitmap(result);
            mProductsListView.setAdapter(mProductsAddapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Application.LAUNCH_MODE == Application.LAUNCH_MODE_EDIT){
            this.finishAffinity();
        }else {
            Intent i = new Intent(SelectProductActivity.this, ClientsActivity.class);
            startActivity(i);
            this.finish();
        }
    }
}
