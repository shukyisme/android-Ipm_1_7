package me.kwik.square;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.listeners.GetMultipleOrderListener;
import me.kwik.rest.responses.GetMultipleOrderResponse;

/**
 * Created by Farid Abu Salih on 18/10/17.
 * farid@kwik.me
 */
public class ButtonStatusUpdate extends AsyncTask<String, Void, Drawable> {
    ImageView bmImage;
    String buttonId;
    boolean registered;
    Bitmap bitmap = null;
    Context context;
    Drawable drawable = null;

    public ButtonStatusUpdate(ImageView bmImage, String buttonId, boolean registered,Context context) {
        this.bmImage = bmImage;
        this.buttonId = buttonId;
        this.registered = registered;
        this.context = context;

    }

    protected Drawable doInBackground(String... urls) {
        KwikMe.getMultipleOrder(buttonId, new GetMultipleOrderListener() {
            @Override
            public void getMultipleOrderDone(GetMultipleOrderResponse response) {

                if (response.getOrders().get(0).getTriggerType().equalsIgnoreCase("trigger1") || response.getOrders().get(0).getTriggerType().equals("click")){
                    drawable = context.getResources().getDrawable(R.drawable.ipm_button_ready_icon);
                }else{
                    drawable = context.getResources().getDrawable(R.drawable.ipm_good_job_logo);
                }


//                if (drawable instanceof BitmapDrawable) {
//                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//                    if(bitmapDrawable.getBitmap() != null) {
//                        bitmap =  bitmapDrawable.getBitmap();
//                    }
//                }
//
//                if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
//                    bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
//                } else {
//                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//                }
//
//                Canvas canvas = new Canvas(bitmap);
//                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//                drawable.draw(canvas);
//                bmImage.setImageBitmap( bitmap );
            }

            @Override
            public void getMultipleOrderError(KwikServerError error) {
                //showOneButtonErrorDialog(getString(R.string.oops),error.getMessage());
            }
        });


        return drawable;

    }

    protected void onPostExecute(Drawable result) {
        bmImage.setImageDrawable(result);
        drawable = null;
    }
}
