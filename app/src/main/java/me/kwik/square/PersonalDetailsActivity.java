package me.kwik.square;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.kwik.bl.KwikMe;
import me.kwik.bl.KwikServerError;
import me.kwik.data.DetailsElement;
import me.kwik.data.KwikButtonDevice;
import me.kwik.data.KwikProject;
import me.kwik.listeners.UpdateKwikButtonListener;
import me.kwik.utils.Logger;


public class PersonalDetailsActivity extends BaseActivity {

    private static final String TAG = PersonalDetailsActivity.class.getSimpleName();
    private Application mApp;
    public static enum FieldType  {STRING,NUMBER,BOOLEAN,CHECKBOX};
    public  List<DetailsElement>  elements= new ArrayList<DetailsElement>();
    private Map<String,Object> data = new HashMap<String, Object>();
    String mFormText = null;
    private KwikButtonDevice mKwikButton;
    private String mKwikButtonSerialNumber;
    private KwikProject mKwikProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);
        mActionBarTitle.setText(R.string.personal_details_activity_title);

        mApp = (Application)(getApplicationContext());
        init();
    }

    private void init() {
        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.personal_details_linear_lay_out);

        mKwikButtonSerialNumber = getIntent().getStringExtra("buttonId");
        if(mKwikButtonSerialNumber == null){
            return;
        }
        mKwikButton = mApp.getButton(mKwikButtonSerialNumber);
        mKwikProject = mApp.getProject(mKwikButton.getProject());

        if(mKwikProject != null){
            String pageTitle = mKwikProject.getFormTitle();
            if(pageTitle != null){
                mActionBarTitle.setText(pageTitle);
            }
        }

        try {
            Object formJsonObj = mKwikProject.getForm();

            Gson gson = new Gson();
            mFormText = gson.toJson(formJsonObj);

        }catch(IndexOutOfBoundsException e){
            //Do nothing
        }

        try {
            parse( mFormText );
        }catch (IllegalStateException e){
            Logger.e(TAG,"Exception, oncreate %s",e.getMessage());
            Intent i = new Intent(PersonalDetailsActivity.this, ReorderSettingsActivity.class);
            if(mKwikButtonSerialNumber != null){
                i.putExtra( "buttonId",mKwikButtonSerialNumber );
            }
            startActivity(i);
            this.finish();
        }


        int i = 0 ;
        for(final DetailsElement element: elements) {

            TextInputLayout v = new TextInputLayout(this);
            v.setHint(element.getName());




            final EditText editText = new EditText(this);
            editText.setSingleLine(false);
            editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            if(element.getValue() != null){
                editText.setText(element.getValue());
            }

            if(!element.isEditable()){
                editText.setKeyListener(null);
            }

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    element.setValue(editText.getText().toString());
                }
            });
            v.addView(editText);
            mLinearLayout.addView(v);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean checkValidation(List<DetailsElement> elements) {
        for (DetailsElement element: elements){
            if(element.isValueValid() != null){
                showOneButtonErrorDialog(getString(R.string.oops),element.isValueValid());
                hideNextSpinner();
                return false;
            }
        }

        return true;
    }

    public void parse(String jsonLine) {
        JsonElement jelement = new JsonParser().parse(jsonLine);
        JsonObject jobject = jelement.getAsJsonObject();
        Set<Map.Entry<String,JsonElement>> entrySet=jobject.entrySet();
        Map<String,Object> personalDetails = null;
        for(Map.Entry<String,JsonElement> entry:entrySet){
            DetailsElement elemnt = new DetailsElement();
            JsonElement jsonElemnt = entry.getValue();
            JsonObject ob = jsonElemnt.getAsJsonObject();

            if(entry.getKey() != null) {
                elemnt.setKey(entry.getKey());
                try {
                    HashMap<String,Object> triggers = mKwikButton.getTriggers();
                    personalDetails = (Map<String, Object>) triggers.get("click");
                    Iterator it = personalDetails.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        if (entry.getKey().equals(pair.getKey())) {
                            elemnt.setValue(String.valueOf(pair.getValue()));
                        }
                    }
                }catch(NullPointerException e){
                    Logger.e(TAG,"parse %s",e.getMessage());
                }
            }

            if(ob.get("name") != null){
                elemnt.setName(ob.get("name").getAsString());
            }
            if(ob.get("isEditable") != null){
                if(Boolean.valueOf(ob.get("isEditable").toString())){
                    elemnt.setIsEditable(true);
                }else{
                    elemnt.setIsEditable(false);
                }
            }

            if(ob.get("maxLength") != null) {
                elemnt.setMaxLength(ob.get("maxLength").getAsInt());
            }

            if(ob.get("minLength") != null) {
                elemnt.setMinLength(ob.get("minLength").getAsInt());
            }
            elements.add(elemnt);
        }

    }

    @Override
    protected void clickNext() {
        super.clickNext();
        if(!checkValidation(elements)){
            return;
        }
        for (DetailsElement element:elements){
            data.put(element.getKey(),element.getValue());
        }

        //mKwikButton.setData(data);

        try {
            HashMap<String, Object> triggers = mKwikButton.getTriggers();
            triggers.put("click",data);
            mKwikButton.setTriggers(triggers);
        }catch (Exception e){
            e.printStackTrace();
        }


        if(mKwikButton.getDeliveryTimeSlots() != null && mKwikButton.getDeliveryTimeSlots().getHourDescription() == ""){
            mKwikButton.setDeliveryTimeSlots(null);
        }


        mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                .setAction("request")
                .setLabel("Update kwik button")
                .build());
        KwikMe.updateKwikButtonDeviceWithListener(mKwikButton, new UpdateKwikButtonListener() {
            @Override
            public void updateKwikButtonListenerDone(KwikButtonDevice button) {
                if(mApp.getButton(button.getId()) == null) {
                    mApp.getButtons().add(button);
                }else{
                    mApp.updateButton(button.getId(),button);
                }
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Update kwik button")
                        .setValue(0)
                        .build());

                Intent i = null;
                if(mKwikProject.getDeliveryTimeSlots() != null && mKwikProject.getDeliveryTimeSlots().size() > 0){
                    i = new Intent(PersonalDetailsActivity.this, DeliveryActivity.class);
                }
                else if (mKwikProject.isHasCatalog() == false) {
                    i = new Intent(PersonalDetailsActivity.this, AllSetUpActivity.class);
                }else{
                    i = new Intent(PersonalDetailsActivity.this, ReorderSettingsActivity.class);
                }
                i.putExtra("buttonId",button.getId());
                startActivity(i);
            }

            @Override
            public void updateKwikButtonListenerError(KwikServerError error) {
                mApp.getDefaultTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(mApp.GOOGLE_ANALYTICS_CATEGORY_SERVER_ACTION)
                        .setAction("response")
                        .setLabel("Update kwik button")
                        .setValue(1)
                        .build());
                showOneButtonErrorDialog(getString(R.string.oops), error.getMessage());
                hideNextSpinner();
            }
        });


    }
}
