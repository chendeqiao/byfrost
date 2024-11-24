package com.intelligence.browser.settings.multilanguage;

import android.content.Context;

import java.util.ArrayList;

public class LanguageSelectionHelp {
    static private LanguageSelectionHelp instance = null;
    private ArrayList<LanguageCountry> mListLanguage = null;
    private LanguageSelectionHelp(){
        initLanguageList();
    }

    static public LanguageSelectionHelp getInstance(){
        if(null == instance){
            instance = new LanguageSelectionHelp();
        }
        return instance;
    }

    public LanguageCountry get(int position){
        if(position >= 0 && position < mListLanguage.size()){
            return mListLanguage.get(position);
        }
        return null;
    }

    public int getCount(){
        return mListLanguage.size();
    }

    public void clearCheck(){
        for(int i = 0; i < mListLanguage.size(); i++){
            mListLanguage.get(i).setLanguageCheck(false);
        }
    }

    public boolean queryLanguageWithCountry(String language, String country){
        for(int i = 0; i < mListLanguage.size(); i++){
            LanguageCountry item = mListLanguage.get(i);
            if(item.getLanguage().equalsIgnoreCase(language) && item.getCountry().equalsIgnoreCase(country)){
                return true;
            }
        }
        return false;
    }

    private boolean isCNVersion(){
        return false;
    }

    private void initLanguageList(){
        Context context = BrowserApplication.getInstance();
        mListLanguage = new ArrayList<LanguageCountry>();
        if (!isCNVersion()) {
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_DE));
        }
        mListLanguage.add(new LanguageCountry(
                LanguageCountry.LANGUAGE_OPTION_EN));
        if (!isCNVersion()) {
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_ES));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_ES,
                    LanguageCountry.COUNTRY_OPTION_US));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_FR));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_ID));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_IT));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_HU));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_PT));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_PT,
                    LanguageCountry.COUNTRY_OPTION_BR));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_RO));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_SK));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_TH));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_VI));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_TR));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_EL));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_RU));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_UK));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_HE));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_KO));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_MS));
        }
        mListLanguage.add(new LanguageCountry(
                LanguageCountry.LANGUAGE_OPTION_ZH,
                LanguageCountry.COUNTRY_OPTION_CN));
        mListLanguage.add(new LanguageCountry(
                LanguageCountry.LANGUAGE_OPTION_ZH,
                LanguageCountry.COUNTRY_OPTION_TW));
        if (!isCNVersion()) {
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_JA));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_AR));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_NL));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_NB));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_PL));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_HR));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_CS));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_SR));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_BG));
            mListLanguage.add(new LanguageCountry(
                    LanguageCountry.LANGUAGE_OPTION_DA));
        }
    }

    public ArrayList<LanguageCountry> getLanguageCountryList(){
        return this.mListLanguage;
    }
}
