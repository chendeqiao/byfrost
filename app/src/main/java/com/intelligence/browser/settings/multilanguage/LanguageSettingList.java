package com.intelligence.browser.settings.multilanguage;

import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Iterator;

public class LanguageSettingList {
    private static LanguageSettingList instance;
    private ArrayList<LanguageCountry> mListLanguage;

    private LanguageSettingList() {
        initLanguageList();
    }

    public static LanguageSettingList getInstance() {
        if (null == instance) {
            instance = new LanguageSettingList();
        }
        return instance;
    }

    public boolean isCharGlyphAvailable(char... c) {
        Paint paint = new Paint();
        Rect missingCharBounds = new Rect();
        char[] missingCharacter = {'\u2936'};
        paint.getTextBounds(c, 0, 1, missingCharBounds); // takes array as argument, but I need only one char.
        Rect testCharsBounds = new Rect();
        paint.getTextBounds(missingCharacter, 0, 1, testCharsBounds);
        return !testCharsBounds.equals(missingCharBounds);
    }

    private void initLanguageList() {
        mListLanguage = new ArrayList<>();
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_AR));  //Arabic(ar)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_DE));  //German(de)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_EN));  //English(en)
        //Spanish(es-ES)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_ES, LanguageCountry.COUNTRY_OPTION_ES));
        //Spanish(es-LA)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_ES, LanguageCountry.COUNTRY_OPTION_MX));
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_FR)); //French(fr)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_HI)); //Hindi(hi)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_ID)); //Indonesia(id)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_IT)); //italian(it)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_JA)); //japanese(ja)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_KO)); //Korean(ko)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_MS)); //Malay(ms)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_NL)); //dutch(nl)
        //Portuguese(pt-PT)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_PT, LanguageCountry.COUNTRY_OPTION_PT));
        //Brazilian Portuguese(pt-BR)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_PT, LanguageCountry.COUNTRY_OPTION_BR));
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_RU)); //Russian(ru)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_TH)); //thai(th)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_TR)); //turkish(tr)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_VI)); //vietnamese(vi)
        //简体(zh-CN)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_ZH, LanguageCountry.COUNTRY_OPTION_CN));
        //繁体(zh-TW)
        mListLanguage.add(new LanguageCountry(LanguageCountry.LANGUAGE_OPTION_ZH, LanguageCountry.COUNTRY_OPTION_TW));

        // 对显示不了的语言进行过滤
        Iterator<LanguageCountry> iterator = mListLanguage.iterator();
        try {
            while (iterator.hasNext()) {
                LanguageCountry languageCountry = iterator.next();
                if (!isCharGlyphAvailable(languageCountry.getLanguageName(BrowserApplication.getInstance()).toCharArray())) {
                    iterator.remove();
                }
            }
        } catch (Exception e) {
        }
    }

    public ArrayList<LanguageCountry> getLanguageCountryList() {
        return this.mListLanguage;
    }

}
