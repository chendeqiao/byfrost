package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;

public class BirthdayPicker extends FrameLayout{


    // 存储了每个月份有多少天...
    private static final SparseIntArray mdInfo = new SparseIntArray(12);
    static{
        mdInfo.put(1, 31);mdInfo.put(2, 29);mdInfo.put(3, 31);mdInfo.put(4, 30);mdInfo.put(5, 31);mdInfo.put(6, 30);
        mdInfo.put(7, 31);mdInfo.put(8, 31);mdInfo.put(9, 30);mdInfo.put(10, 31);mdInfo.put(11, 30);mdInfo.put(12, 31);
    }



    private NumberPicker monthPicker;
    private NumberPicker dayPicker;
    private final String[] months;


    public BirthdayPicker(@NonNull Context context) {
        this(context, null);
    }
    public BirthdayPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public BirthdayPicker(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        months = getResources().getStringArray(R.array.lock_birth_months_number);
        LayoutInflater.from(context).inflate(R.layout.browser_birth_picker_view, this, true);
        monthPicker = (NumberPicker) findViewById(R.id.picker_month);
        dayPicker = (NumberPicker) findViewById(R.id.picker_day);
        initMonthPicker();
        setDayPickerByMonth(1);
    }

    /* 初始化设置月份的控件 */
    private void initMonthPicker(){
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(months);
        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setDayPickerByMonth(newVal);
            }});
    }
    /* 根据月份设置日的控件 */
    private void setDayPickerByMonth(int month){
        int day = mdInfo.get(month);
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(day);
        dayPicker.setFormatter(new NumberPicker.Formatter() {
            public String format(int value) {
                return value < 10 ? "0"+value : value+"";
            }});
    }



    /**
     * 返回选择的月
     * @return 数字1-12
     */
    public int getMonth(){
        return monthPicker.getValue();
    }

    /**
     * 返回选择的日
     * @return 数字1-31
     */
    public int getDay(){
        return dayPicker.getValue();
    }


    /**
     * @return a special format string for save
     */
    public String getSelectedString(){
        return monthPicker.getValue()+"_"+dayPicker.getValue();
    }

}
