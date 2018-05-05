package me.kwik.appsquare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kwik.bl.KwikDevice;
import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.IpmClient;
import me.kwik.data.IpmEvent;
import me.kwik.listeners.GetIpmEventsListener;
import me.kwik.listeners.GetKwikDevicesListener;
import me.kwik.listeners.UpdateEventListener;
import me.kwik.rest.responses.GetIpmEventsResponse;
import me.kwik.rest.responses.GetKwikDevicesResponse;
import me.kwik.rest.responses.UpdateEventResponse;
import me.kwk.utils.DateUtils;

public class CloseTrapActivity extends BaseActivity {

    @BindView(R.id.close_trap_save_button)
    Button mResolveButton;

    @BindView(R.id.close_trap_activity_name_TextView)
    TextView mNameTextView;

    @BindView(R.id.close_trap_activity_technician_name_TextView)
    TextView mTechnicianNameTextView;

    @BindView(R.id.close_trap_activity_time_and_date_TextView)
    TextView mDateAndTimeTextView;

    @BindView(R.id.close_trap_activity_description_EditText)
    TextView mCloseTrapEditText;

    @BindView(R.id.close_trap_activity_rodent_Spinner)
    Spinner mRodentTrapedSpinner;

    @BindView(R.id.close_trap_activity_rodent_trapped)
    TextView mRodentTrappedTextView;



    private IpmEvent mEvent;
    private String mSerial;
    private KwikDevice mTrap;
    private Application mApp;
    private IpmClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_trap);
        mActionBarTitle.setText(R.string.close_trap_activity_close_trap_alert);

        ButterKnife.bind(this);

        mApp = (Application) getApplication();

        SpannableStringBuilder builder = new SpannableStringBuilder();

        String red = "*";
        SpannableString redSpannable= new SpannableString(red);
        redSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, red.length(), 0);
        builder.append(redSpannable);

        String white = getString(R.string.close_trap_activity_rodent_trapped);
        SpannableString whiteSpannable= new SpannableString(white);
        whiteSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this,R.color.manatee_2)), 0, white.length(), 0);
        builder.append(whiteSpannable);

        mRodentTrappedTextView.setText(builder, TextView.BufferType.SPANNABLE);


        Integer[] items = new Integer[]{0,1,2,3,4};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, items);
        mRodentTrapedSpinner.setAdapter(adapter);

        mResolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  if(mEvent == null){
                      showOneButtonErrorDialog("",getString(R.string.close_trap_activity_sorry_no_event_was_founded));
                     return;
                 }

                mEvent.setStatus(IpmEvent.Status.RESOLVED.toString());

                String description = null;

                try {
                    description = mCloseTrapEditText.getText().toString();
                }catch (NullPointerException e){

                }

                if(description != null){
                    mEvent.setMessage((description));
                }


                int rodentNumber = Integer.valueOf(mRodentTrapedSpinner.getSelectedItem().toString());
                mEvent.setRodentCount(rodentNumber);


                KwikMe.updateEvent(mEvent, new UpdateEventListener() {
                    @Override
                    public void updateEventDone(UpdateEventResponse res) {
                        if(res != null && res.getEventObject() != null){
                            if(res.getEventObject().getStatus().equals(IpmEvent.Status.RESOLVED.toString())){
                                finish();
                            }
                        }
                    }

                    @Override
                    public void updateEventError(KwikServerError error) {
                        showErrorDialog(error);
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
            showOneButtonErrorDialog("",getString(R.string.close_trap_activity_unkown_error));
            return;
        }
        showProgressBar();
        KwikMe.getKwikDevices(mSerial,null, null,null, "status",1,new GetKwikDevicesListener() {
            @Override
            public void getKwikDevicesListenerDone(GetKwikDevicesResponse response) {
                hideProgressBar();
                List<KwikDevice> buttons = response.getButtons();
                if (buttons == null || buttons.size() == 0) {
                    showOneButtonErrorDialog("",getString(R.string.close_trap_activity_button_was_not_found));
                    return;
                }
                mTrap = buttons.get(0);
                if(mTrap.getName() != null) {
                    mNameTextView.setText(mTrap.getName() + "\n" + getString(R.string.close_trap_activity_serial_number) + mTrap.getId());
                }
                getEventFromServer();
                mClient = mApp.getClient(mTrap.getClient());
                mDateAndTimeTextView.setText(DateUtils.formatDate(new Date(), "MM/dd/yyyy, h:mm a", mClient.getTimezone()));
            }

            @Override
            public void getKwikDevicesListenerError(KwikServerError error) {
                hideProgressBar();
                showErrorDialog(error);
            }
        });

        mTechnicianNameTextView.setText(mApp.getUser().getFirstName() + " " + mApp.getUser().getLastName());
        mDateAndTimeTextView.setText(DateUtils.formatDate(new Date(), "MM/dd/yyyy, h:mm a", null));

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
                            }
                        }
                    }
                }

                @Override
                public void getIpmEventsError(KwikServerError error) {
                    showErrorDialog(error);
                }
            });
        }
    }
}
