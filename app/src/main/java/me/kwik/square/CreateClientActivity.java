package me.kwik.square;

import android.os.Bundle;
import android.view.View;

public class CreateClientActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_client);
        mActionBarTitle.setText("Create New Client");
    }

    public void onCreateClientClick(View view) {
    }
}
