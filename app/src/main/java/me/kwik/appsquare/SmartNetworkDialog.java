package me.kwik.appsquare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by Farid Abu Salih on 08/05/16.
 * farid@kwik.me
 */
public class SmartNetworkDialog extends BaseDialog {
    private Context mContext;;
    private Button mNextButton;

    @SuppressLint("NewApi") public SmartNetworkDialog(Context context) {
        super(context);
        setContentView(R.layout.smart_network_dialog_layout);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.dialogContent);
        //TODO: Farid: 1950 is match xxhdpi screens, check xhdp and hdpi screens
        rl.getLayoutParams().height = 1950;

        closeBtn.setVisibility(View.GONE);
        mContext = context;
        rl.setBackground(mContext.getResources().getDrawable(R.drawable.smart_network_dialog_bg));

        mNextButton = (Button)findViewById(R.id.smartNetworkDialogNextButton);
        mNextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showAdvancedWifiIfAvailable(mContext);
            }
        });
    }



    public static void showAdvancedWifiIfAvailable(Context ctx) {
        final Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (activityExists(ctx, i)) {
            ctx.startActivity(i);
        }
    }
    public static boolean activityExists(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        final ResolveInfo info = mgr.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return (info != null);
    }
}
