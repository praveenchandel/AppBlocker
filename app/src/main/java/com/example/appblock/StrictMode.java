package com.example.appblock;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class StrictMode extends AppCompatActivity {

    // creating a DatabaseHelper class object
    DatabaseHelper myDb;

    private static final int ACTIVATION_REQUEST = 1;

    private TextView blockingLevelTxt;
    private TextView strictModeDeactivationPINTxt;
    private TextView strictModeRights;
    private Button activationButton;

    private CardView blockingCardView;
    private TextView goToActivationTxt;

    private CardView deactivationCardView;
    private EditText enterPassword;
    private EditText ReenterPassword;
    private Button savePassword;

    private CheckBox blockingLevelCheckBox;
    private RadioButton blockingLevelRadioButton;

    private String pass1;
    private String pass2;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strict_mode);

        initializingViews();

        // initializing database
        myDb=new DatabaseHelper(this);

        blockingLevelTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockingCardView.setVisibility(View.VISIBLE);
            }
        });

        goToActivationTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockingCardView.setVisibility(View.GONE);
            }
        });

        strictModeDeactivationPINTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivationCardView.setVisibility(View.VISIBLE);
            }
        });

        savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivationCardView.setVisibility(View.GONE);
            }
        });

        // handling checkbox of blocking level
        blockingLevelCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blockingLevelCheckBox.isChecked()) {
                    blockingLevelRadioButton.setChecked(true);
                }
                else if (!blockingLevelCheckBox.isChecked())
                {
                    blockingLevelRadioButton.setChecked(false);
                }
            }
        });

        savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePassword();
            }
        });

        activationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean ans=validatePassword2();
                if (!ans){
                    Toast.makeText(StrictMode.this, "check password", Toast.LENGTH_SHORT).show();
                    return;
                }
                // if we have to block uninstalling of app
                if (blockingLevelCheckBox.isChecked()) {
                    String pass = viewAllData();

                    if (pass.equals("")) {
                        Toast.makeText(StrictMode.this, "No Password found", Toast.LENGTH_SHORT).show();
                    }else if (pass1.isEmpty()){
                        Toast.makeText(StrictMode.this, "No Password found", Toast.LENGTH_SHORT).show();
                    } else {
                        makeAdmin("enable");
                       // Toast.makeText(StrictMode.this, "Strict Mode is Activated", Toast.LENGTH_SHORT).show();
                    }
                }
                activationButton.setEnabled(false);
                Toast.makeText(StrictMode.this, "Strict Mode is Activated", Toast.LENGTH_SHORT).show();
//                Intent intent=new Intent(StrictMode.this,MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
            }
        });

    }

    // it will validate password
    private void validatePassword() {
        pass1=enterPassword.getText().toString();
        pass2=ReenterPassword.getText().toString();

        if(TextUtils.isEmpty(pass1)){
            enterPassword.setError("password is required ");
            enterPassword.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(pass2)){
            ReenterPassword.setError("password is required ");
            ReenterPassword.requestFocus();
            return;
        }

        if (pass1.equals(pass2)) {
            savePasswordToDataBase();
        }
        else {
            Toast.makeText(StrictMode.this, "password not matched", Toast.LENGTH_SHORT).show();
        }
    }

    // it will validate password
    private boolean validatePassword2() {
        pass1=enterPassword.getText().toString();
        pass2=ReenterPassword.getText().toString();

        if(TextUtils.isEmpty(pass1)){
            return false;
        }
        if(TextUtils.isEmpty(pass2)){
            return false;
        }
        return true;
    }

    // it will save password to database
    private void savePasswordToDataBase() {

            // calling inserting function that will insert the data
            boolean isInserted= myDb.insertData(pass1);

            if (isInserted==true)
                Toast.makeText(StrictMode.this, "Password Saved successfully", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(StrictMode.this, "some error happened ", Toast.LENGTH_SHORT).show();

           deactivationCardView.setVisibility(View.GONE);
        }

    public String viewAllData(){

        Cursor res=myDb.getAllData();
        if (res.getCount()==0){
            // no data available
            return "";
        }

        StringBuffer buffer=new StringBuffer();

        if (res.moveToNext()){
            return res.getString(0);
        }
        return "";
    }

    // this code will make this app a admin app
    private void makeAdmin(String type) {

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName demoDeviceAdmin = new ComponentName(this, DemoDeviceAdmin.class);
        Log.e("DeviceAdminActive==", "" + demoDeviceAdmin);

        Log.e("type is    : ",type);
        if (type.equals("enable")) {

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);// adds new device administrator
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, demoDeviceAdmin);//ComponentName of the administrator component.
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Disable app");//additional explanation
            startActivityForResult(intent, ACTIVATION_REQUEST);

        }else if(type.equals("disable")) {
            devicePolicyManager.removeActiveAdmin(demoDeviceAdmin);
        }
    }

    private void initializingViews() {
        blockingLevelTxt=(TextView)findViewById(R.id.strict_mode_blocking_level);
        strictModeDeactivationPINTxt=(TextView)findViewById(R.id.strict_mode_deactivation_pin);
        strictModeRights=(TextView)findViewById(R.id.strict_mode_rights);
        activationButton=(Button)findViewById(R.id.strict_mode_active_button);
        blockingLevelCheckBox=(CheckBox)findViewById(R.id.strict_mode_blocking_level_checkBox);
        blockingLevelRadioButton=(RadioButton)findViewById(R.id.strict_mode_blocking_level_radio_Button);

        blockingLevelCheckBox.setChecked(false);
        blockingLevelRadioButton.setChecked(false);
        blockingLevelRadioButton.setEnabled(false);

        blockingCardView=(CardView)findViewById(R.id.strict_mode_blocking_level_cardView);
        goToActivationTxt=(TextView)findViewById(R.id.strict_mode_go_to_activation_txt);

        deactivationCardView=(CardView)findViewById(R.id.strict_mode_deactivation_password);
        enterPassword=(EditText)findViewById(R.id.strict_mode_enter_password);
        ReenterPassword=(EditText)findViewById(R.id.strict_mode_reenter_password);
        savePassword=(Button)findViewById(R.id.strict_mode_save_password);
    }
}