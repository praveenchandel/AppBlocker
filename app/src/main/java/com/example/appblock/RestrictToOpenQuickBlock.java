package com.example.appblock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class RestrictToOpenQuickBlock extends AppCompatActivity {

    // creating a DatabaseHelper class object
    DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restrict_to_open_quick_block);

        // initializing database
        myDb=new DatabaseHelper(this);

        checkIsAlreadyHasPassword();
    }

    // if in the database we have already password
    private void checkIsAlreadyHasPassword() {
        String password=viewAllData();
        if(password.equals("")){
            Intent intent=new Intent(RestrictToOpenQuickBlock.this,QuickBlockActivity.class);
           // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

}
