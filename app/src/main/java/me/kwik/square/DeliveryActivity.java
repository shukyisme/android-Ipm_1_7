package me.kwik.square;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikDeliveryTimeSlots;
import me.kwik.data.KwikProject;
import me.kwik.listeners.UpdateKwikButtonListener;

public class DeliveryActivity extends BaseActivity {


    private final String TAG = DeliveryActivity.class.getSimpleName();
    private ListView mTimeSlotsListView;
    private Application mApp;
    private int mSelectedTimeSlotIndex = -1;
    private final int NO_ITEM_SELECTED = -1;
    private final int ITEM_DELETED = -2;
    private String mKwikButtonId;
    private KwikButtonDevice mButton;
    private KwikProject mProject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        mApp = (Application)getApplication();
        mTimeSlotsListView = (ListView)findViewById(R.id.delivery_activity_time_slots_list_view);

        mActionBarTitle.setText(getResources().getString(R.string.delivery_activity_title));
        mKwikButtonId = getIntent().getStringExtra("buttonId");
        if(mKwikButtonId == null){
            this.finish();
            return;
        }
        mButton = mApp.getButton(mKwikButtonId);
        mProject = mApp.getProject(mButton.getProject());
        displayTimeSlots();

        //Adding header to the product list
        TextView textView = new TextView(this);
        textView.setText(R.string.delivery_activity_time_slots_title);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 0, 0, 20);
        mTimeSlotsListView.addHeaderView(textView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mKwikButtonId = getIntent().getStringExtra("buttonId");
        if(mKwikButtonId == null){
            this.finish();
            return;
        }
        mButton = mApp.getButton(mKwikButtonId);
        mProject = mApp.getProject(mButton.getProject());
    }

    private void displayTimeSlots() {
        mTimeSlotsListView.setAdapter(new DeliveryTimeSlotsAdapter(this,mProject.getDeliveryTimeSlots()));

    }

    @Override
    protected void clickNext() {
        super.clickNext();
        //Collect inputs and validate
        if(!validation()){
            mNextImageView.setClickable(true);
            return;
        }



        //Add data to button
        if(mSelectedTimeSlotIndex != NO_ITEM_SELECTED &&  mSelectedTimeSlotIndex != ITEM_DELETED) {
            mButton.setDeliveryTimeSlots(mProject.getDeliveryTimeSlots().get(mSelectedTimeSlotIndex));
        }else if(mSelectedTimeSlotIndex == ITEM_DELETED){
            mButton.setDeliveryTimeSlots(null);
        }

        //Update button api
        KwikMe.updateKwikButtonDeviceWithListener(mButton, new UpdateKwikButtonListener() {
            @Override
            public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                if(mApp.getButton(button.getId()) == null) {
                    mApp.getButtons().add(button);
                }else{
                    mApp.updateButton(button.getId(),button);
                }
                Intent i = new Intent(DeliveryActivity.this,ReorderSettingsActivity.class);
                i.putExtra("buttonId",button.getId());
                startActivity(i);
            }

            @Override
            public void updateKwikButtonListenerError(KwikServerError error) {
                showOneButtonErrorDialog(getString(R.string.oops),error.getMessage());
                mNextImageView.setClickable(true);
            }
        });



    }

    private boolean validation() {

        return true;
    }

    public class DeliveryTimeSlotsAdapter extends ArrayAdapter<KwikDeliveryTimeSlots> {
        private final Context context;
        private final List<KwikDeliveryTimeSlots> timeSlots;

        public DeliveryTimeSlotsAdapter(Context context, List<KwikDeliveryTimeSlots> timeSlots) {
            super(context, -1, timeSlots);
            this.context = context;
            this.timeSlots = timeSlots;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.time_slots_list_item_layout, parent, false);


            TextView timeDescription = (TextView)rowView.findViewById(R.id.time_slots_list_item_description_text_view);
            timeDescription.setText(timeSlots.get(position).getHourDescription());

            CheckBox deliveryTimeSlot = (CheckBox)rowView.findViewById(R.id.time_slots_list_item_check_box);


            //No item selected
            if(mSelectedTimeSlotIndex == NO_ITEM_SELECTED) {

                //Check if item was selected on the button
                if (mButton.getDeliveryTimeSlots() != null) {

                    if (timeSlots.get(position).getId().equals(mButton.getDeliveryTimeSlots().getId())) {
                        mSelectedTimeSlotIndex = position;
                        deliveryTimeSlot.setChecked(true);
                    } else {
                        deliveryTimeSlot.setChecked(false);
                    }
                } else {
                    deliveryTimeSlot.setChecked(false);
                }
            }else{
                //Item was selected
                if(mSelectedTimeSlotIndex == position){
                    deliveryTimeSlot.setChecked(true);
                }else{
                    deliveryTimeSlot.setChecked(false);
                }
            }

            //Select option
            deliveryTimeSlot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        mSelectedTimeSlotIndex = position;
                    }else{
                        //If the user delete the last selection
                        if(mSelectedTimeSlotIndex == position){
                            mSelectedTimeSlotIndex = ITEM_DELETED;
                        }
                    }
                    notifyDataSetChanged();
                }
            });

            return rowView;
        }
    }
}
