package com.intelligence.browser.ui.home.headerview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intelligence.browser.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeController {

    private TextView mTimeTv1, mTimeTv2, mTimeTv3;
    private TextView mDateTv;
    private Context context;

    private BroadcastReceiver timeTickReceiver;


    public TimeController(Context context) {
        this.context = context;
    }

    public void apply(ViewGroup v) {

        mTimeTv1 = v.findViewById(R.id.tv_time1);
        mTimeTv2 = v.findViewById(R.id.tv_time2);
        mTimeTv3 = v.findViewById(R.id.tv_time3);
        mDateTv = v.findViewById(R.id.tv_date);
        setTimeData();
    }


    private void setTimeData(){
        mDateTv.postDelayed(new Runnable() {
            @Override
            public void run() {
                setTimeAndData(context, mTimeTv1, mTimeTv2, mDateTv);
                setTimeData();
            }
        },2000);
    }
    /* 时间View信息显示 */
    public void setTimeInfo() {
       setTimeAndData(context, mTimeTv1, mTimeTv2, mDateTv);
        if (Build.VERSION.SDK_INT < 16 || mTimeTv1.getTag() != null)
            return;
        try {
            mTimeTv1.setTag("");
//            mTimeTv1.getPaint().setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
//            mTimeTv2.getPaint().setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
//            mTimeTv3.getPaint().setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
            mDateTv.getPaint().setTypeface(Typeface.create("sans-serif-thin", Typeface.BOLD));
        } catch (Exception e) {
        }
    }

    public static void setTimeAndData(Context context, TextView timeTv1, TextView timeTv2, TextView dateTv) {
        //time
        String timeStr = new SimpleDateFormat("HH_mm", Locale.getDefault()).format(new Date());
        String[] times = timeStr.split("_");
        timeTv1.setText(times[0]);
        timeTv2.setText(times[1]);
        // month string
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int monthInt = calendar.get(Calendar.MONTH);
        String monthStr = monthInt + 1 + "";
        if (monthStr.length() < 2)
            monthStr = "0" + monthStr;
        // day string
        int dayInt = calendar.get(Calendar.DAY_OF_MONTH);
        String dayStr = dayInt + "";
        if (dayStr.length() == 1) dayStr = "0" + dayStr;
        //week string
//        boolean isFirstSunday = (calendar.getFirstDayOfWeek() == Calendar.SUNDAY);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
//        if(isFirstSunday){
//            weekDay = weekDay - 1;
//            if(weekDay == 0)
//                weekDay = 7;
//        }
        weekDay = weekDay - 2;
        if (weekDay < 0) {
            weekDay = 6;
        }
//        String[] weekArray = context.getResources().getStringArray(R.array.screenlock_week_array);
//        String weekStr = weekArray[weekDay];
//        // setting
//        dateTv.setText("");
//        dateTv.append(dayStr);
//        dateTv.append("/");
//        dateTv.append(monthStr);
//        dateTv.append("  ");
//        dateTv.append(weekStr);
    }
}
