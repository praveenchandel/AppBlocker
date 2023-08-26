package com.example.appblock;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.facebook.stetho.Stetho;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.scottyab.rootbeer.RootBeer;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuickBlockActivity extends AppCompatActivity{

    static Context appContext;
    //------------Google related------------
    public String h_value = "";
    public String c_value = "";
    long millisNow;

    //Database
    DB_Helper db;

    //intents
    Intent lockIntent;
    //Array lists for Apps
    ArrayList<Integer> preselectedApps = new ArrayList<>();
    ArrayList<MultiSelectModel> listOfApps = new ArrayList<>();
    List<String> LS;
    //Multi-Choice-Selector
    MultiSelectDialog multiSelectDialog;
    //strings
    String package_name;
    //RootBeer Root Checker
    RootBeer rootbeer;
    Button Lock;
    TextView title_timer;
    private TextView addOrRemoveApps;
    private Spinner spinner_time;
    private Spinner spinner_state;
    private String selected_time="";
    private String selected_state="";

    //------------Service related------------
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String str_time = intent.getStringExtra("time");
            if (Integer.valueOf(db.get_Hours(1)) > 10) {
                title_timer.setText(getString(R.string.time_left1)
                        + str_time
                        + "\n" + getString(R.string.selected_time) + db.get_Hours(1)
                        + getString(R.string.minutes) + "\n" + getString(R.string.selected_state) + db.get_StateTitle(1));
            } else {
                title_timer.setText(getString(R.string.time_left1)
                        + str_time
                        + "\n" + getString(R.string.selected_time) + db.get_Hours(1)
                        + getString(R.string.Hours_v2) + "\n" + getString(R.string.selected_state) + db.get_StateTitle(1));
            }

            if (db.get_TimerFinish(1) == 1) {
                title_timer.setText(getString(R.string.not_running));
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_block);

        init();

        addOrRemoveApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawer_App_Selector();
            }
        });

        Lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_time != "" && selected_state != "") {

                    // if the timer is running
                    if (db.get_Running(1).equals("N")) {
                        if (db.get_Selected(1) == 1) {

                            new LovelyStandardDialog(QuickBlockActivity.this)
                                    .setTopColorRes(R.color.blue)
                                    .setIcon(R.drawable.ic_baseline_lock_24)
                                    .setTitle(getString(R.string.lockBT_dialog_title))
                                    .setMessage(getString(R.string.lockBT_dialog_message))
                                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            String toastMessage = "";
                                            if (db.get_Running(1).equals("N")) {

                                                h_value = selected_time;
                                                start_timer(h_value);
                                                db.set_LockTime(h_value);
                                                toastMessage += getString(R.string.hours) + "your apps is locked for :" + h_value;
                                            } else if (db.get_Running(1).equals("Y")) {
                                                toastMessage += getString(R.string.toast_already_running);
                                            }


                                            c_value = selected_state;
                                            switch (c_value) {
                                                case "Wifi Only":
                                                    db.set_StateTable(1);
                                                    db.set_on_off(1);
                                                    db.set_StateTitle("Wifi Only");
                                                    toastMessage += getString(R.string.state) + "Wifi Only";
                                                    break;
                                                case "Full(wifi + mobile data)":
                                                    if (rootbeer.isRooted()) {
                                                        db.set_StateTable(2);
                                                        db.set_on_off(1);
                                                        db.set_StateTitle("Full(wifi + mobile data)");
                                                        toastMessage += getString(R.string.state) + "Full(wifi + mobile data)";
                                                    } else {
                                                        db.set_StateTitle("None");
                                                        toastMessage += getString(R.string.swipe_root_alert);
                                                        break;
                                                    }

                                            }
                                            if (DefaultSettings.getCb1(QuickBlockActivity.this)) {
                                                //Timer Start/End notifications enabled
                                                notification_update();
                                            }
                                            Toast.makeText(QuickBlockActivity.this, toastMessage, Toast.LENGTH_SHORT).show();

                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    finish();
                                                }
                                            }, 3000);

                                        }


                                    }).setNegativeButton(android.R.string.no, null)
                                    .show();

                        } else if (db.get_Selected(1) == 0) {

                            Toast.makeText(QuickBlockActivity.this, getString(R.string.cafebar_error2), Toast.LENGTH_SHORT).show();
                        }

                    } else if (db.get_Running(1).equals("Y")) {

                        stopService(new Intent(getApplicationContext(), Timer_Service.class));

                        Toast.makeText(QuickBlockActivity.this, "timer is stopped and apps unlocked now you can use them ", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }

    //All declarations and variable initializations
    public void init() {

        //------------Variables & Declarations------------
        //String
        package_name = getPackageName();

        //DataBase Handler
        db = new DB_Helper(this);
        appContext = getApplicationContext();
        //Locked_intent
        lockIntent = new Intent(this, locked.class);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        //Title Timer
        title_timer = findViewById(R.id.title_timer);
        //Lock Button
        Lock = findViewById(R.id.sendButton);
        addOrRemoveApps=(Button)findViewById(R.id.add_or_remove_apps);

        spinner_time=(Spinner)findViewById(R.id.spinner_time);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.time_in_hours,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_time.setAdapter(adapter);

        spinner_state=(Spinner)findViewById(R.id.spinner_desire_state);
        ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this,R.array.state_type,android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state.setAdapter(adapter1);

        spinner_time.setOnItemSelectedListener(new spinnerTimerClass());
        spinner_state.setOnItemSelectedListener(new spinnerStatesClass());

        //------------Method Calls------------
        rootbeer = new RootBeer(this);
      //  new_drawer();
        permission_check();
        isIgnoringBattery();
        first_Boot_check();
        try {
            LS = getInstalledComponentList();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //selection_all();
        PreSelect();
        //Main Title text change
        if (db.get_Running(1).equals("N")) {
            title_timer.setText(getString(R.string.not_running));
        }
        Stetho.initializeWithDefaults(this);
    }

    class spinnerTimerClass implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selected_time=parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class spinnerStatesClass implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selected_state=parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


    //Drawer App Selector
    public void Drawer_App_Selector() {
//        Checking if the lock is running or not
        if (db.get_Running(1).equals("N")) {

            //Creating multi dialog
            multiSelectDialog = new MultiSelectDialog()
                    .title(getString(R.string.app_selector_title)) //setting title for dialog
                    .titleSize(20) //setting textSize
                    .positiveText(getString(R.string.app_selector_apply)) //setting Submit text
                    .negativeText(getString(R.string.app_selector_cancel)) //setting Cancel text
                    //.clearText(getString(R.string.app_selector_clear))
                    .preSelectIDsList(preselectedApps) //List of ids that you need to be selected
                    .multiSelectList(listOfApps) // the multi select model list with ids and name
                    .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {

                        @Override
                        public void onSelected(ArrayList<Integer> ids, ArrayList<String> arrayList1, String s) {

                            db.deleteAll();
                            if (ids.size() >= 1) {
                                //set_Selected means there is at least 1 item selected, set to true
                                db.set_Selected(1);
                            } else if (ids.size() < 1) {
                                //set to false
                                db.set_Selected(0);
                            }
                            //Adding IDs of selections and the respective PKG name
                            for (int i = 0; i < ids.size(); i++) {
                                db.add_apps(new apps(LS.get(ids.get(i) - 1),ids.get(i)));
                                //Toast.makeText(Main.this,"Selected Ids : " + ids.get(i),Toast.LENGTH_SHORT).show();
                            }

                            Toast.makeText(QuickBlockActivity.this, getString(R.string.selected_apps) + ids.size(), Toast.LENGTH_SHORT).show();

                        }

                        //onCancel do nothing
                        @Override
                        public void onCancel() {
                            Log.e("onCancel", "Dialog Dismissed without selection");
                        }

                    });
            multiSelectDialog.show(getSupportFragmentManager(), "multiSelectDialog");
        } else {

            stopService(new Intent(getApplicationContext(), Timer_Service.class));

            Toast.makeText(appContext, getString(R.string.cafebar_error5), Toast.LENGTH_SHORT).show();

        }
    }

    //Initialize all values on First Boot ever
    public void first_Boot_check() {
        //First launch and update check
        if (db.getFirstBootCount() == 0) {
            db.set_AllTimerData("", "N", 1, "", 0, "");
            db.set_defaultStateTable(0, 0, "None", 0);
            db.set_FirstBoot("N");
            db.set_defaultUsage("XXX");
        }
    }

    //Show dialog if usage access permission not given
    public void permission_check() {
        //Usage Permission
        if (!isAccessGranted()) {
            new LovelyStandardDialog(QuickBlockActivity.this)
                    .setTopColorRes(R.color.blue)
                    .setIcon(R.drawable.ic_baseline_perm_device_information_24)
                    .setTitle(getString(R.string.permission_check_title))
                    .setMessage(getString(R.string.permission_check_message))
                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }


    //Starts Timer_Service
    public void start_timer(String hours) {
        millisNow = System.currentTimeMillis();
        db.set_Data(millisNow);
        db.set_Hours(hours.replaceAll("[\\D]", ""));
        db.set_LockTime(hours);
        db.set_Running("Y");
        db.set_once(1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dnd_toggle();
        }

        Intent intent_service = new Intent(getApplicationContext(), Timer_Service.class);
        startService(intent_service);
    }

    //Control DND Mode
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void dnd_toggle() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (DefaultSettings.getCb2(this)) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        }

    }

    //Check if app usage access is granted
    public boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode;
            assert appOpsManager != null;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //Check if battery permissions given
    public void isIgnoringBattery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(package_name)) {
                new LovelyStandardDialog(QuickBlockActivity.this)
                        .setTopColorRes(R.color.blue)
                        .setIcon(R.drawable.ic_baseline_perm_device_information_24)
                        .setTitle(getString(R.string.battery_dialog_title))
                        .setMessage(getString(R.string.battery_dialog_message))
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//                                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                //intent.setData(Uri.parse("package:" + package_name));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();

            }

        }
    }

    //Preselect the apps that are saved in DB when opening app selector
    public void PreSelect() {
        if ((int) db.getAppsCount() != 0) {
            int count = (int) db.getAppsCount();
            for (int i = 1; i <= count; ++i) {
                preselectedApps.add(db.get_app(i).getS_id());
            }
        }
    }

    //Get list of all apps, put List<String> if u uncomment return
    private List<String> getInstalledComponentList()
            throws PackageManager.NameNotFoundException {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ril = getPackageManager().queryIntentActivities(mainIntent, 0);
        List<String> componentList = new ArrayList<String>();
        String name;
        String pkg = "";
        int i = 0;
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                Resources res = getPackageManager().getResourcesForApplication(ri.activityInfo.applicationInfo);
                if (ri.activityInfo.labelRes != 0) {
                    name = res.getString(ri.activityInfo.labelRes);
                } else {
                    name = ri.activityInfo.applicationInfo.loadLabel(
                            getPackageManager()).toString();
                }
                pkg = ri.activityInfo.packageName;
                componentList.add(pkg);
                i++;
                listOfApps.add(new MultiSelectModel(i, name));
            }
        }
        return componentList;
    }



    //Everything related to Notifications
    public void notification_update() {
        Intent intent = new Intent(this, QuickBlockActivity.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(); // just use a counter in some util class...

        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("notification_1", "Timer_Notification", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0, 500,});
            notificationChannel.enableVibration(true);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        if (Integer.valueOf(db.get_Hours(1)) > 10) {
            builder = new NotificationCompat.Builder(this, "notification_1");
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT) //HIGH, MAX, FULL_SCREEN and setDefaults(Notification.DEFAULT_ALL) will make it a Heads Up Display Style
                    //.setDefaults(Notification.) // also requires VIBRATE permission
                    .setSmallIcon(R.mipmap.ic_launcher) // Required!
                    .setContentTitle(getString(R.string.notification_title1))
                    .setContentText(getString(R.string.time_chosen) + db.get_Hours(1) + getString(R.string.minutes) + ", " + getString(R.string.app_open1) + db.get_StateTitle(1))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.time_chosen) + db.get_Hours(1) + getString(R.string.minutes) + "\n" + getString(R.string.app_open2) + db.get_StateTitle(1)))
                    .setVibrate(new long[]{0, 500})
                    //.setAutoCancel(true)
                    .setContentIntent(pIntent);
        } else {
            builder = new NotificationCompat.Builder(this, "notification_1");
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT) //HIGH, MAX, FULL_SCREEN and setDefaults(Notification.DEFAULT_ALL) will make it a Heads Up Display Style
                    //.setDefaults(Notification.) // also requires VIBRATE permission
                    .setSmallIcon(R.mipmap.ic_launcher) // Required!
                    .setContentTitle(getString(R.string.notification_title1))
                    .setContentText(getString(R.string.time_chosen) + db.get_Hours(1) + getString(R.string.Hours_v2) + ", " + getString(R.string.app_open1) + db.get_StateTitle(1))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.time_chosen) + db.get_Hours(1) + getString(R.string.Hours_v2) + "\n" + getString(R.string.app_open2) + db.get_StateTitle(1)))
                    .setVibrate(new long[]{0, 500})
                    //.setAutoCancel(true)
                    .setContentIntent(pIntent);
        }


        // Builds the notification and issues it.
        assert notificationManager != null;
        notificationManager.notify(313, builder.build());
    }


    @Override
    public void finish() {
        super.finishAndRemoveTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Timer_Service.str_receiver));
        Log.e("Lock_Time", db.get_LockTime(1));
        Log.e("IsRunning?", db.get_Running(1));
        Log.e("Timer_Finish", String.valueOf(db.get_TimerFinish(1)));
        Log.e("Hours", "H: " + db.get_Hours(1));
        Log.e("Data", "D: " + db.get_Data(1));
        //Saved
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        Log.e("Lock_Time", db.get_LockTime(1));
        Log.e("IsRunning?", db.get_Running(1));
        Log.e("Timer_Finish", String.valueOf(db.get_TimerFinish(1)));
        Log.e("Hours", "H: " + db.get_Hours(1));
        Log.e("Data", "D: " + db.get_Data(1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}