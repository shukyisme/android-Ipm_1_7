package me.kwik.square;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import me.kwk.utils.Utils;

public class WiFiSelectionActivity extends BaseActivity {

    String mName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_selection);
        mActionBarTitle.setText(R.string.wifi_selection_activity_title);
        try {
            mName = getIntent().getExtras().getString("name");
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.playAudioFile(this, "dedicated_router_screen",0,5);
    }

    public void yesClicked(View yesButton){
        Intent i = new Intent(WiFiSelectionActivity.this, AddSerialNumberManuallyActivity.class);
        i.putExtra("name", mName);
        startActivity(i);
    }

    public void noClicked(View noButton){
        Intent i = new Intent(WiFiSelectionActivity.this, NetworkPasswordActivity.class);
        i.putExtra("name", mName);
        startActivity(i);

    }
}
