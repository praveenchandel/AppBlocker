package com.example.appblock;

import android.app.Notification;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        checkCanDrawOverOtherApps();

        long end = System.currentTimeMillis();
        long start = end-1000*60*60*24;
        long time=getUsageStates(start,end);

        int  minutes = (int) ((time / ( 60)) % 60);

        int hours = (int) ((time / ( 60 * 60)) % 24);

        TextView hourText=(findViewById(R.id.main_hour_text));
        TextView hourDigit=(TextView)findViewById(R.id.main_digit_hour);
        TextView minuteDigit=(TextView)findViewById(R.id.main_digit_minute);
        ProgressBar timeUsage=(ProgressBar)findViewById(R.id.main_last_24hour_usage_progressBar);

        CardView activityCardView;
        CardView quickBlockShortCut;
        CardView strictModeCardView;


        activityCardView=(CardView)findViewById(R.id.main_last_24hour_usage_time);
        quickBlockShortCut=(CardView)findViewById(R.id.main_quick_block_shortCut);
        strictModeCardView=(CardView)findViewById(R.id.main_Strict_Mode_shortcut);

        if(hours==0){
            hourDigit.setVisibility(View.GONE);
            hourText.setVisibility(View.GONE);
        }else {
            hourDigit.setText(String.valueOf(hours));
        }

        minuteDigit.setText(String.valueOf(minutes));

        // if user usage the phone less than 8 hours
        if(hours<8) {
            int n= 60*60*8;
            time*=100;
            n= (int) (time/n);
            timeUsage.setProgress(n);
        } else {
            timeUsage.setProgress(100);
        }

        activityCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Statistics.class);
                startActivity(intent);
            }
        });

        quickBlockShortCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,RestrictToOpenQuickBlock.class);
                startActivity(intent);
            }
        });

        strictModeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),PasswordConfirmingActivity.class);
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkCanDrawOverOtherApps() {
        if (!Settings.canDrawOverlays(this)) {
            new LovelyStandardDialog(MainActivity.this)
                    .setTopColorRes(R.color.blue)
                    .setIcon(R.drawable.ic_baseline_perm_device_information_24)
                    .setTitle("Do you want to give draw over other apps permission")
                    .setMessage(getString(R.string.permission_check_message))
                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 0);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_quickBlock)
        {
            Intent intent=new Intent(MainActivity.this,RestrictToOpenQuickBlock.class);
            startActivity(intent);
        }
        else if (id==R.id.nav_Selected){
            Intent intent=new Intent(MainActivity.this,selected_apps.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_statistics)
        {
            Intent intent=new Intent(MainActivity.this,Statistics.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_notification)
        {
           Intent intent=new Intent(getApplicationContext(), NotificationActivity.class);
           startActivity(intent);
        }
        else if (id == R.id.nav_settings)
        {
            Intent intent=new Intent(getApplicationContext(),settings.class);
            startActivity(intent);
        }else if (id == R.id.nav_strict_mode){
            Intent intent=new Intent(getApplicationContext(),PasswordConfirmingActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // return total screen time of last 24 hours
    private long getUsageStates(long start,long end){

        long TimeInforground = 0;

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);

        long totaltime=0;
        ArrayList<Long> timeList=new ArrayList<Long>();

        if (stats != null) {

            for (UsageStats usageStats : stats) {

                TimeInforground = usageStats.getTotalTimeInForeground();

                if (TimeInforground>1000*60*2)
                timeList.add(TimeInforground/1000);
            }

            Collections.sort(timeList);
            long fiTi=0;
            for(int i=timeList.size()-1;i>timeList.size()-6 && i>=0;i--){
                fiTi=fiTi+ timeList.get(i);
            }
            return fiTi;
        }
        return totaltime;
    }

}