package com.example.appblock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Statistics extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    
    private long lastDay1;
    private long lastDay2;
    private long lastDay3;
    private long lastDay4;
    private long lastDay5;
    private long lastDay6;
    private long lastDay7;

    private GraphView graph;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.VISIBLE);

        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();

        ListView listView = (ListView) findViewById(R.id.pkg_list);
        mAdapter = new UsageStatsAdapter();
        listView.setAdapter(mAdapter);

        Spinner typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        typeSpinner.setOnItemSelectedListener(this);

        getLastSevenDaysStatics();

    }

    private void getLastSevenDaysStatics(){
        long end = System.currentTimeMillis();
        long start = end-1000*60*60*24;
        lastDay1=getUsageStates(start,end);

        end=start;
        start=start-1000*60*60*24;
         lastDay2=getUsageStates(start,end);

        end=start;
        start=start-1000*60*60*24;
         lastDay3=getUsageStates(start,end);

        end=start;
        start=start-1000*60*60*24;
         lastDay4=getUsageStates(start,end);

        end=start;
        start=start-1000*60*60*24;
         lastDay5=getUsageStates(start,end);

        end=start;
        start=start-1000*60*60*24;
         lastDay6=getUsageStates(start,end);

        end=start;
        start=start-1000*60*60*24;
        lastDay7=getUsageStates(start,end);

        showGraph();
    }

    private void showGraph() {

        long d=60*60;

        try {
            LineGraphSeries<DataPoint> series = new LineGraphSeries < > (new DataPoint[] {
                    new DataPoint(0,(float)lastDay7/d),
                    new DataPoint(1,(float)lastDay6/d),
                    new DataPoint(2,(float)lastDay5/d),
                    new DataPoint(3,(float)lastDay4/d),
                    new DataPoint(4,(float)lastDay3/d),
                    new DataPoint(5,(float)lastDay2/d),
                    new DataPoint(6,(float)lastDay1/d)
            });
            graph.addSeries(series);
        } catch (IllegalArgumentException e) {
            Toast.makeText(Statistics.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // return total screen time in the given time range
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

    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;

    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private PackageManager mPm;
    private long totalTime=0;
    private int count=0;

    private static final int _DISPLAY_ORDER_USAGE_TIME_DAILY = 0;

    private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME_DAILY;

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int) (b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    // View Holder used when displaying views
    static class AppViewHolder {
        TextView pkgName;
        TextView usageTime;
    }

    class UsageStatsAdapter extends BaseAdapter {
        // Constants defining order for display order
        // private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
        // private AppNameComparator mAppLabelComparator;
        private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
        private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

        UsageStatsAdapter() {
            Calendar cal = Calendar.getInstance();
            final List<UsageStats> stats;

            long start=System.currentTimeMillis();
            long end=start-1000*60*60;
            if (mDisplayOrder==_DISPLAY_ORDER_USAGE_TIME_DAILY) {
                cal.add(Calendar.DATE, -1);

                stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                        end, start);
            }else{
                cal.add(Calendar.DATE, -7);
                stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                        end,start);
            }

            if (stats == null) {
                return;
            }

            ArrayMap<String, UsageStats> map = new ArrayMap<>();
            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                final android.app.usage.UsageStats pkgStats = stats.get(i);


                // load application labels for each application
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    String label = appInfo.loadLabel(mPm).toString();
                    mAppLabelMap.put(pkgStats.getPackageName(), label);

                    UsageStats existingStats =
                            map.get(pkgStats.getPackageName());
                    if (existingStats == null) {
                        map.put(pkgStats.getPackageName(), pkgStats);
                    } else {
                        existingStats.add(pkgStats);
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    // This package may be gone.
                }
            }
            mPackageStats.addAll(map.values());

            // Sort list
            // mAppLabelComparator = new AppNameComparator(mAppLabelMap);
            sortList();
        }

        @Override
        public int getCount() {
            return mPackageStats.size();
        }

        @Override
        public Object getItem(int position) {
            return mPackageStats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            AppViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.usage_stats_items, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new AppViewHolder();
                holder.pkgName = (TextView) convertView.findViewById(R.id.app_name);
                holder.usageTime = (TextView) convertView.findViewById(R.id.app_time_usage);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (AppViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder
            UsageStats pkgStats = mPackageStats.get(position);
            if (pkgStats != null) {
                String label = mAppLabelMap.get(pkgStats.getPackageName());
                holder.pkgName.setText(label);

                if (count<6) {
                    totalTime = totalTime + pkgStats.getTotalTimeInForeground() / 1000;
                    count++;
                }
                holder.usageTime.setText(DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }
            return convertView;
        }

        private void sortList() {
            Collections.sort(mPackageStats, mUsageTimeComparator);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mDisplayOrder=position;
        mAdapter.sortList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }


}