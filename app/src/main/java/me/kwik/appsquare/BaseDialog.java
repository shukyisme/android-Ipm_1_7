package me.kwik.appsquare;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * Created by Farid Abu Salih on 08/05/16.
 * farid@kwik.me
 */
public class BaseDialog extends Dialog{

    protected ImageView closeBtn;
    public BaseDialog(Context context) {
        super(context,android.R.style.Theme_Translucent_NoTitleBar);
        View view = getLayoutInflater().inflate(R.layout.base_dailog_layout, null);
        super.setContentView(view);

        closeBtn = (ImageView)findViewById(R.id.closeIcon);
        closeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

    }

    @Override
    public void setContentView(int layoutResID) {
        Context context = getContext();
        RelativeLayout rootLayout = (RelativeLayout)findViewById(R.id.dialogContent);
        View.inflate(context, layoutResID, rootLayout);

    }
}

