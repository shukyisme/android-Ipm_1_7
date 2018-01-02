package me.kwik.square;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.IpmClient;
import me.kwik.listeners.CreateNewClientListener;
import me.kwik.listeners.GetClientsListener;
import me.kwik.rest.responses.GetClientsResponse;

public class AddNewTrapActivity extends BaseActivity {


    @BindView(R.id.client_name_AutoCompleteTextView)
    AutoCompleteTextView mClientNameAutoCompleteTextView;

    private Application mApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_trap);
        ButterKnife.bind(this);
        mApp = (Application) getApplication();
        mActionBarTitle.setText("Add new trap");


        mClientNameAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AutoCompleteTextView)v).showDropDown();
            }
        });

        showProgressBar();
        KwikMe.getClients(null, new GetClientsListener() {
            @Override
            public void getClientsDone(GetClientsResponse res) {

                List<String> clients = new ArrayList<>();

                for(IpmClient client: res.getClients()){
                    clients.add(client.getName());
                }

                ArrayAdapter<String> clientsAdapter = new ArrayAdapter<>(AddNewTrapActivity.this, android.R.layout.simple_dropdown_item_1line, clients);
                mClientNameAutoCompleteTextView.setAdapter(clientsAdapter);

                hideProgressBar();
            }

            @Override
            public void getClientsError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog("",error.getMessage());

            }
        });
    }

    public void onNextClick(View v){
        Intent i = new Intent(AddNewTrapActivity.this,WiFiSelectionActivity.class);
        startActivity(i);
    }

    public void createNewClientClick(View view) {
        Intent i = new Intent(AddNewTrapActivity.this,CreateClientActivity.class);
        startActivity(i);
    }
}
