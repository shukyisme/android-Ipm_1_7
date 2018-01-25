package me.kwik.square;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.IpmEvent;
import me.kwik.listeners.GetIpmEventsListener;
import me.kwik.listeners.GetKwikDevicesListener;
import me.kwik.listeners.UpdateEventListener;
import me.kwik.rest.responses.GetIpmEventsResponse;
import me.kwik.rest.responses.GetKwikDevicesResponse;
import me.kwik.rest.responses.UpdateEventResponse;

public class TrapDetailsActivity extends BaseActivity {

    @BindView(R.id.trap_details_activity_edit_ImageButton)
    ImageButton mEditImageButton;

    @BindView(R.id.trap_details_activity_name_EditText)
    EditText mNameEditText;

    @BindView(R.id.trap_details_activity_name_TextView)
    TextView mNameTextView;

    @BindView(R.id.trap_details_activity_description_edit_ImageButton)
    ImageButton mEditDescriptionImageButton;

    @BindView(R.id.trap_details_activity_edit_description_EditText)
    EditText mDescriptionEditText;

    @BindView(R.id.trap_details_activity_edit_description_TextView)
    TextView mDescriptionTextView;

    @BindView(R.id.trap_details_activity_status_value_TextView)
    TextView mStatusValueTextView;

    @BindView(R.id.trap_details_activity_alert_time_TextView)
    TextView mAlertTimeTextView;

    @BindView(R.id.trap_details_activity_last_communication_TextView)
    TextView mLastCommunicationTextView;

    @BindView(R.id.trap_details_activity_battery_level_TextView)
    TextView mBatteryLevelTextView;

    @BindView(R.id.trap_details_activity_setup_date_TextView)
    TextView mSetupDateTextView;

    @BindView(R.id.trap_details_activity_button_icon_ImageView)
    ImageView mButtonIconImageView;

    @BindView(R.id.trap_details_activity_resolve_Button)
    Button mResolveButton;

    private static final int EDITING_STATUS = 0;
    private static final int EDITED_STATUS  = 1;
    private static int NAME_STATUS = EDITED_STATUS;
    private static int DESCRIPTION_STATUS = EDITED_STATUS;

    private String mSerial;
    private KwikDevice mTrap;
    private IpmEvent    mEvent;

    private final String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trap_details);
        mActionBarTitle.setText("Trap details");
        ButterKnife.bind(this);

        mResolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEvent == null){
                    showOneButtonErrorDialog("","Sorry, no event was founded");
                    return;
                }

                mEvent.setStatus(IpmEvent.Status.RESOLVED.toString());
                KwikMe.updateEvent(mEvent, new UpdateEventListener() {
                    @Override
                    public void updateEventDone(UpdateEventResponse res) {
                        if(res != null && res.getEventObject() != null){
                            if(res.getEventObject().getStatus().equals(IpmEvent.Status.RESOLVED.toString())){
                                finish();
                                startActivity(getIntent());
                            }
                        }
                    }

                    @Override
                    public void updateEventError(KwikServerError error) {
                        showOneButtonErrorDialog("",error.getMessage());
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSerial = getIntent().getStringExtra("serial_number");
        if(mSerial == null){
            showOneButtonErrorDialog("","UnKnown error, Please go back and try again");
            return;
        }
        showProgressBar();
        KwikMe.getKwikDevices(mSerial,null, null,null, new GetKwikDevicesListener() {
            @Override
            public void getKwikDevicesListenerDone(GetKwikDevicesResponse response) {
                hideProgressBar();
                List<KwikDevice> buttons = response.getButtons();
                mTrap = buttons.get(0);
                if (buttons == null || buttons.size() == 0) {
                    showOneButtonErrorDialog("","Button was not found");
                    return;
                }
                update();
            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                showOneButtonErrorDialog("",error.getMessage());
            }
        });
    }

    private void update() {
        if(mTrap == null) {
            return;
        }
        if(mTrap.getName() != null) {
            mNameEditText.setText(mTrap.getName());
            mNameTextView.setText(mTrap.getName() + "\nSerial number: " + mTrap.getId());
        }

        if(mTrap.getDescription() != null){
            mDescriptionTextView.setText(mTrap.getDescription());
            mDescriptionEditText.setText(mTrap.getDescription());
        }

        String na = "-";
        String status = mTrap.getStatus();
        mAlertTimeTextView.setText(na);
        mStatusValueTextView.setTextColor(ContextCompat.getColor(this,R.color.outerspace));
        if(status != null) {
            mResolveButton.setVisibility(View.GONE);
            if (status.equals(KwikDevice.STATUS_NOT_AVAILABLE)) {
                mStatusValueTextView.setText(na);
                mButtonIconImageView.setImageResource(R.drawable.grey_button);
            } else {
                mStatusValueTextView.setText(status);
                if(status.equals(KwikDevice.STATUS_ALERT)){
                    mStatusValueTextView.setTextColor(ContextCompat.getColor(this,R.color.deepcarminepink));
                    mButtonIconImageView.setImageResource(R.drawable.kwik_orange);
                    mResolveButton.setVisibility(View.VISIBLE);
                    getEventFromServer();
                }else if(status.equals(KwikDevice.STATUS_READY) ||
                        status.equals(KwikDevice.STATUS_AVAILABLE)){
                    mButtonIconImageView.setImageResource(R.drawable.ipm_button_ready_icon);
                }else if(status.equals(KwikDevice.STATUS_DISABLED)){
                    mButtonIconImageView.setImageResource(R.drawable.grey_button);
                }
            }
        }





        if(mTrap.getPingAt() != null){
            mLastCommunicationTextView.setText(mTrap.getPingAt());
        }else {
            mLastCommunicationTextView.setText(na);
        }

        if(mTrap.getBatteryStatus() != null){
            mBatteryLevelTextView.setText(mTrap.getBatteryStatus());
            if(mTrap.getBatteryStatus().equalsIgnoreCase("replace")){
                mBatteryLevelTextView.setTextColor(ContextCompat.getColor(this,R.color.deepcarminepink));
            }else{
                mBatteryLevelTextView.setTextColor(ContextCompat.getColor(this,R.color.outerspace));
            }
        }else {
            mBatteryLevelTextView.setText(na);
        }

        if(mTrap.getManufacturedAt() != null){
            mSetupDateTextView.setText(mTrap.getManufacturedAt());
        }else {
            mSetupDateTextView.setText(na);
        }

    }

    private void getEventFromServer() {
        if(mTrap != null && mTrap.getClient() != null) {
            KwikMe.getIpmEvents(IpmEvent.Status.ACTIVE, mTrap.getClient(), new GetIpmEventsListener() {
                @Override
                public void getIpmEventsDone(GetIpmEventsResponse res) {
                    if(res != null && res.getEvents() != null) {
                        for (IpmEvent event : res.getEvents()) {
                            if (event.getButton().equals(mTrap.getId())) {
                                mEvent = event;
                                mAlertTimeTextView.setText(mEvent.getTriggerAt());
                            }
                        }
                    }
                }

                @Override
                public void getIpmEventsError(KwikServerError error) {
                    showOneButtonErrorDialog("",error.getMessage());
                }
            });
        }
    }

    public void editClicked(View view) {
        if(NAME_STATUS == EDITED_STATUS){
            NAME_STATUS = EDITING_STATUS;
            mNameTextView.setVisibility(View.INVISIBLE);
            mNameEditText.setVisibility(View.VISIBLE);
            if(mTrap != null && mTrap.getName() != null) {
                mNameEditText.setText(mTrap.getName());
            }
        }else if(NAME_STATUS == EDITING_STATUS){
            NAME_STATUS = EDITED_STATUS;
            mNameTextView.setVisibility(View.VISIBLE);
            mNameEditText.setVisibility(View.INVISIBLE);
            if(mTrap!= null && mTrap.getId() != null) {
                mNameTextView.setText(mNameEditText.getText().toString() + "\nSerial number:" + mTrap.getId());
            }
        }
    }

    public void editDescriptionClicked(View view) {
        if(DESCRIPTION_STATUS == EDITED_STATUS){
            DESCRIPTION_STATUS = EDITING_STATUS;
            mDescriptionTextView.setVisibility(View.INVISIBLE);
            mDescriptionEditText.setVisibility(View.VISIBLE);
            mDescriptionEditText.setText(mDescriptionTextView.getText().toString());
        }else if(DESCRIPTION_STATUS == EDITING_STATUS){
            DESCRIPTION_STATUS = EDITED_STATUS;
            mDescriptionTextView.setVisibility(View.VISIBLE);
            mDescriptionEditText.setVisibility(View.INVISIBLE);
            mDescriptionTextView.setText(mDescriptionEditText.getText().toString());
        }
    }
}
