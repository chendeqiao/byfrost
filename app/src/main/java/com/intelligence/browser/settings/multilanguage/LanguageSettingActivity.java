package com.intelligence.browser.settings.multilanguage;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.browser.ui.ActionBarActivity;
import com.intelligence.browser.utils.ActivityUtils;

import java.util.List;


public class LanguageSettingActivity extends ActionBarActivity implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    private LanguageAdapter mLanguageAdapter;
    private List<LanguageCountry> mDatas;
    public static final String SHARED_POSITION = "position" ;

    public static void launch(Activity from){
        ActivityUtils.startActivity(from, LanguageSettingActivity.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_activity_multi_language);
        setBrowserActionBar(this);
        setPageTitle(getResources().getString(R.string.browser_setting_multi_language));
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initView(){
        mRecyclerView = findViewById(R.id.recyclerView_language);
        mLanguageAdapter = new LanguageAdapter();
        mRecyclerView.setAdapter(mLanguageAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
    }

    private void initData() {
        LanguageSettingList mLanguage = LanguageSettingList.getInstance();
        mDatas = mLanguage.getLanguageCountryList();
    }

    @Override
    public void onClick(View v) {
        if (R.id.actionbar_left == v.getId()) {
            finish();
        }
    }

    class LanguageAdapter extends RecyclerView.Adapter<LanguageViewHolder>{
        @Override
        public LanguageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(LanguageSettingActivity.this).inflate(R.layout.browser_item_setting_multi_language,parent,false);
            return new LanguageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LanguageViewHolder holder, int position) {
            holder.tvLanguage.setText( mDatas.get(position).getLanguageName(LanguageSettingActivity.this));
            int positionCB = readCurrentLanguage();
            if (-1 != positionCB){
                if(position == positionCB){
                    holder.cbLanguage.setVisibility(View.VISIBLE);
                }else {
                    holder.cbLanguage.setVisibility(View.GONE);
                }
            }else {
                holder.cbLanguage.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }

    class LanguageViewHolder extends RecyclerView.ViewHolder{
        TextView tvLanguage;
        ImageView cbLanguage;
        public LanguageViewHolder(View itemView) {
            super(itemView);

            tvLanguage = (TextView) itemView.findViewById(R.id.tv_language);
            cbLanguage = (ImageView) itemView.findViewById(R.id.item_multi_language_cb);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    String currentLanguage = mDatas.get(position).getLanguage();
                    String currentCountry = mDatas.get(position).getCountry();

                    cbLanguage.setVisibility(View.VISIBLE);
                    LanguageUtil.toChangeLanguage(currentLanguage,currentCountry,getApplicationContext()); //切换语言
                    storeCurrentLanguage(currentLanguage,currentCountry,position); //保存当前语言
                    BrowserApplication.getInstance().setIsChina(isSystemLanguageChinese(currentLanguage,currentCountry));
                    mLanguageAdapter.notifyDataSetChanged();

                    LocalBroadcastManager.getInstance(itemView.getContext()).sendBroadcast(new Intent(TRANSLATE_PAGE));
//                    ResidentNotification.updateNotify(LanguageSettingActivity.this);
//                    PermanentNoticebarManager.getInstance().startNotification(LanguageSettingActivity.this);
                    itemView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LanguageSettingActivity.this.finish();
                        }
                    },500);
                }
            });
        }
    }

    public boolean isSystemLanguageChinese(String language,String country) {
        return "zh".equals(language) && "CN".equals(country);
    }

    private void storeCurrentLanguage(String language, String country,int position){
        SharedPreferences sharePre = getSharedPreferences(SHARED_FILE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor shareEditor = sharePre.edit();
        shareEditor.putString(SHARED_LANGUAGE,language);
        shareEditor.putString(SHARED_COUNTRY,country);
        shareEditor.putInt(SHARED_POSITION,position);
        shareEditor.commit();
    }

    private int readCurrentLanguage(){
        SharedPreferences sharePre = getSharedPreferences(SHARED_FILE_NAME,MODE_PRIVATE);
        return sharePre.getInt(SHARED_POSITION, -1);
    }



}
