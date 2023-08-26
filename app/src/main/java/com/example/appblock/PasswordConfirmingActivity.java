package com.example.appblock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordConfirmingActivity extends AppCompatActivity {

    private EditText enteredPassword;
    private Button submitButton;

    // creating a DatabaseHelper class object
    DatabaseHelper myDb;

    private static final int ACTIVATION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_confirming);

        // initializing database
        myDb=new DatabaseHelper(this);

        enteredPassword=(EditText)findViewById(R.id.password_confirmation_enter_password);
        submitButton=(Button)findViewById(R.id.password_confirmation_submit_button);

        checkIsAlreadyHasPassword();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass=enteredPassword.getText().toString();
                if (pass.isEmpty()){
                    Toast.makeText(PasswordConfirmingActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                }else if (pass.equals("")){
                    Toast.makeText(PasswordConfirmingActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                }else {
                    String password=viewAllData();

                    if (pass.equals(password)){
                        myDb.deleteData();
                        makeAdmin("disable");
                        Intent intent=new Intent(PasswordConfirmingActivity.this,StrictMode.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(PasswordConfirmingActivity.this, "Password didn't matched", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // if in the database we have already password
    private void checkIsAlreadyHasPassword() {
        String password=viewAllData();
        if(password.equals("")){
            Intent intent=new Intent(PasswordConfirmingActivity.this,StrictMode.class);
            startActivity(intent);
            finish();
        }
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
}