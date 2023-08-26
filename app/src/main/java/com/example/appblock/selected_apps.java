package com.example.appblock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yarolegovich.lovelydialog.LovelyStandardDialog;

public class selected_apps extends AppCompatActivity {

    static Context appcontext1;
    //TextView
    TextView title;
    //Database
    DB_Helper db;
    //Variable to store app drawable in
    Drawable Aicon;
    //Variable to save version number of an app
    String version = "";
    String result;
    //Variable for app name
    String AppName = "";

    //App Icons array
    Drawable[] images;

    //App Names Array
    String[] name;

    //App Version Array
    String[] versionNumber;

    ListView lView;

    SelectedAppsAdapter lAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_apps);

        // Does everyThing
        init();
    }

    //Initialize OnCreate()
    public void init() {
        //TextView
        title = findViewById(R.id.selected_apps_title);

        //DataBase Handler
        db = new DB_Helper(this);
        appcontext1 = getApplicationContext();
        name = new String[(int) db.getAppsCount()];
        images = new Drawable[(int) db.getAppsCount()];
        versionNumber = new String[(int) db.getAppsCount()];
        title.setText(getString(R.string.custom_list_title) + "(" + String.valueOf(db.getAppsCount() + ")"));

        getIcons();
        getNames();
        getVersions();
        populate();
    }

    //Fill the ListView
    public void populate() {

        lView = (ListView) findViewById(R.id.selected_apps_list);

        lAdapter = new SelectedAppsAdapter(selected_apps.this, name, versionNumber, images);

        lView.setAdapter(lAdapter);
    }

    public Drawable[] getIcons() {
        int i;
        for (i = 0; i < images.length; i++) {
            try {
                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(db.get_app(i + 1).get_PKG(), PackageManager.GET_META_DATA);
                Aicon = applicationInfo.loadIcon(getPackageManager());
                images[i] = Aicon;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return images;
    }

    public String[] getNames() {
        int i;
        for (i = 0; i < name.length; i++) {
            name[i] = db.get_app_PKG(i + 1);
            try {
                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(db.get_app(i + 1).get_PKG(), PackageManager.GET_META_DATA);
                AppName = applicationInfo.loadLabel(getPackageManager()).toString();
                name[i] = AppName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    public String[] getVersions() {
        int i;
        for (i = 0; i < versionNumber.length; i++) {
            versionNumber[i] = db.get_app(i + 1).get_PKG();
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(db.get_app(i + 1).get_PKG(), PackageManager.GET_META_DATA);
                version = pInfo.versionName;
                versionNumber[i] = version;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return versionNumber;
    }
}