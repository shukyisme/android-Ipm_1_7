package me.kwik.square;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProduct;
import me.kwik.data.KwikProject;
import me.kwik.data.KwikSelectedProduct;
import me.kwik.data.MobileDevice;
import me.kwik.listeners.GetProductCatalogListener;
import me.kwik.utils.Logger;
import me.kwk.utils.DownloadImageTask;

public class ReorderSettingsActivity extends BaseActivity {

    private ListView mProductsListView;
    private SelectedProductsArrayAdapter mSelectedProductsAddapter ;
    private List<KwikSelectedProduct> mSelectedProducts = new ArrayList<KwikSelectedProduct>();
    private static String TAG = ReorderSettingsActivity.class.getSimpleName();
    private Application mApp;
    private TextView mTotalPrice;
    private String mKwikButtonId;
    private KwikProject mProject;
    private KwikButtonDevice mButton;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reorder_settings);
        mActionBarTitle.setText(R.string.reorder_settings_activity_screen_title);
        mPrevTextView.setText(getString(R.string.edit));
        mProductsListView = (ListView)findViewById(R.id.reorder_settings_activity_products_list_view);
        mTotalPrice = (TextView)findViewById(R.id.reorder_settings_activity_total_price_text_view);
        mProgressBar = (ProgressBar)findViewById(R.id.reorder_settings_activity_progress_bar);


        mApp = (Application)(getApplicationContext());

    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.VISIBLE);
        mKwikButtonId = getIntent().getStringExtra("buttonId");
        if(mKwikButtonId !=  null) {
            mButton = mApp.getButton(mKwikButtonId);
            mProject = mApp.getProject(mButton.getProject());
            getProductCatalog(mKwikButtonId);
        }
    }

    private void initProductList() {

        mPrevTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.GONE);
                Intent i = new Intent(ReorderSettingsActivity.this,SelectProductActivity.class);
                i.putExtra("sender",ReorderSettingsActivity.class.getSimpleName());
                i.putExtra("buttonId",mKwikButtonId);
                startActivity(i);
                ReorderSettingsActivity.this.finish();
            }
        });

        ImageView logoImageView = (ImageView) findViewById(R.id.reorder_settings_activity_title_image_view);
        try {
            new DownloadImageTask(logoImageView).execute(mProject.getLogos().get(MobileDevice.DENSITY));
        }catch (Exception e){
            Logger.e(TAG,"Exception, ReorderSettingsActivity.oncreate %s",e.getMessage());
        }

        try {
            mSelectedProducts = mButton.getProducts();

            try {
                for (Iterator<KwikSelectedProduct> iter = mSelectedProducts.listIterator(); iter.hasNext(); ) {
                    KwikSelectedProduct a = iter.next();
                    if (getKwikProductById(a.getId()) == null) {
                        iter.remove();
                    }
                }
            }catch (NullPointerException e){
                Logger.e(TAG,"Exception, ReorderSettingsActivity.oncreate %s",e.getMessage());
            }

            //String price;
            double price = 0;
            for (KwikSelectedProduct p :mSelectedProducts){
                double c = p.getCount() * getKwikProductById(p.getId()).getPrice().getRetail();
                Logger.i(TAG,"Product %s amount %d price %f",p.getId(),p.getCount(),getKwikProductById(p.getId()).getPrice().getRetail());
                price += c;
            }
            price = Math.round(price* 100.0) /100.0;
            if(price == Math.round(price)){
                mTotalPrice.setText(getKwikProductById(mSelectedProducts.get(0).getId()).getPrice().getCurrencySymbol() + Math.round(price));
            }else{
                mTotalPrice.setText(getKwikProductById(mSelectedProducts.get(0).getId()).getPrice().getCurrencySymbol() + price);
            }

        }catch (Exception e){
            Logger.e(TAG,"Exception, ReorderSettingsActivity.oncreate %s",e.getMessage());
        }
        mProgressBar.setVisibility(View.GONE);
        if(mSelectedProducts != null && mSelectedProducts.size() > 0) {
            mSelectedProductsAddapter = new SelectedProductsArrayAdapter(ReorderSettingsActivity.this, mSelectedProducts);
            mProductsListView.setAdapter(mSelectedProductsAddapter);
        }
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

                for (KwikProduct product : products) {

                    ImageView v = new ImageView(ReorderSettingsActivity.this);
                    if (product.getImages() != null) {
                        new DownloadImageTask(v).execute(product.getImages().get(MobileDevice.DENSITY));
                    }else{
                        new DownloadImageTask(v).execute("");
                    }
                }

                initProductList();
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
                showOneButtonErrorDialog(getString(R.string.oops),error.getMessage());
                mNextImageView.setClickable(true);
            }
        });
    }


    @Override
    protected void clickNext() {
        super.clickNext();
        Intent i;
        if (mApp.getBooleanPref(Application.PrefType.FIRST_BUTTON_IN_THE_APP)) {
            i = new Intent(ReorderSettingsActivity.this, AllSetUpActivity.class);
        }else {
            i = new Intent(ReorderSettingsActivity.this, ClientsActivity.class);
        }
        i.putExtra("buttonId",mKwikButtonId);
        i.putExtra("sender", ReorderSettingsActivity.class.getSimpleName());
        startActivity(i);
        this.finish();
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

    public void doneClicked(View view) {
        clickNext();
    }

    public class SelectedProductsArrayAdapter extends ArrayAdapter<KwikSelectedProduct> {
        private final Context context;
        private final List<KwikSelectedProduct> values;
        private final List<KwikProduct> projectProducts;

        public SelectedProductsArrayAdapter(Context context, List<KwikSelectedProduct> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;

            projectProducts = mProject.getProducts();
        }

        private KwikProduct getKwikProductById(String id){
            for(KwikProduct product:projectProducts){
                if(product.getId().equals(id)){
                    return product;
                }
            }
            return null;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.selected_products_list_item_layout, parent, false);


            //Product image
            Bitmap bitmap = null;
            try {
                String urldisplay = getKwikProductById(values.get(position).getId()).getImages().get(MobileDevice.DENSITY);
                String pattern = Pattern.quote(System.getProperty("file.separator"));
                String[] splittedFileName = urldisplay.split(pattern);

                String fileName = Application.IMAGES_FOLDER_NAME + "/" + splittedFileName[splittedFileName.length - 1];
                File sd = Environment.getExternalStorageDirectory();
                File image = new File(sd, fileName);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            }catch (Exception e){
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.button_to_ap_animation_frame1);
            }


            ImageView imageView = (ImageView) rowView.findViewById(R.id.selected_products_list_item_product_image_view);
            imageView.setImageBitmap(bitmap);

            final LinearLayout textLayout = (LinearLayout)rowView.findViewById(R.id.selected_products_list_item_second_line);
            //Product name
            try {
                final TextView textViewName = (TextView) rowView.findViewById(R.id.selected_products_list_item_name_text_view);
                textViewName.setText(getKwikProductById(values.get(position).getId()).getName());
            }catch (NullPointerException e){
                Logger.e(TAG,"%s",e.getMessage());
            }

            try {
                //Product quantity
                final TextView textViewQuantity = (TextView) rowView.findViewById(R.id.selected_products_list_item_quantity_text_view);
                String udata=getString(R.string.quantity);
                SpannableString content = new SpannableString(udata);
                content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
                textViewQuantity.setText(content);

                TextView quantityNumber = (TextView)rowView.findViewById(R.id.selected_products_list_item_quantity_number_text_view);
                quantityNumber.setText(" " + String.valueOf(values.get(position).getCount()) + " ");

                //Product price
                final TextView textViewPrice = (TextView) rowView.findViewById(R.id.selected_products_list_item_price_text_view);
                udata=getString(R.string.price);
                content = new SpannableString(udata);
                content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
                textViewPrice.setText(content);

                TextView priceNumber = (TextView)rowView.findViewById(R.id.selected_products_list_item_price_number_text_view);
                double p = Math.round((Double.valueOf(values.get(position).getCount()) * getKwikProductById(values.get(position).getId()).getPrice().getRetail()) * 100.0) /100.0;
                if(p==Math.round(p)){
                    priceNumber.setText(" " +getKwikProductById(values.get(position).getId()).getPrice().getCurrencySymbol() + Math.round(p));
                }else {
                    priceNumber.setText(" " + getKwikProductById(values.get(position).getId()).getPrice().getCurrencySymbol() + p);
                }


            }catch (Exception e){
                Logger.e(TAG,"%s",e.getMessage());
            }
            return rowView;
        }
    }

    @Override
    public void onBackPressed() {
        //Do nothing
    }
}
