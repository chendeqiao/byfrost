package com.intelligence.browser.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.utils.ActivityUtils;
import com.intelligence.commonlib.tools.StringUtil;

import java.util.List;
import java.util.Map;


@SuppressLint("ValidFragment")
public class BrowserEditAdBlockRuleFragment extends BaseEditAdBlockRuleFragment {

    private ListView mListView;

    private TextView mPrompt;

    public BrowserEditAdBlockRuleFragment() {
        super(R.string.custom_rules_title);
    }

    @Override
    public List<String> readRules(Context context) {
        return null;
    }

    @Override
    public void writeRules(Context context, List<String> rules) {
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.browser_custom_adblock_rules_list, container, false);
        mListView = rootView.findViewById(R.id.list);
        mPrompt = rootView.findViewById(R.id.null_rules_prompt);
        ArrayMap<String, String> hyperLinkMap = new ArrayMap<>();
//        hyperLinkMap.put(getString(R.string.adblockplus_label), getString(R.string.adblockplus_url));
//        hyperLinkMap.put(getString(R.string.instructions_label), getString(R.string.instructions_url));
//        addHyperlinkToText(mPrompt, getString(R.string.null_rules_prompt), hyperLinkMap);
        return rootView;
    }

    private void addHyperlinkToText(TextView textView, String text, ArrayMap<String, String> hyperLinkMap) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        for (Map.Entry<String, String> hyperLink : hyperLinkMap.entrySet()) {
            String hyperlinkText = hyperLink.getKey();
            final String url = hyperLink.getValue();
            int hyperStartIndex = text.indexOf(hyperlinkText);
            if (hyperStartIndex == -1) {
                textView.setText(text);
                return;
            }
            int hyperlinkEnd = hyperStartIndex + hyperlinkText.length();
            ForegroundColorSpan blueColor = new ForegroundColorSpan(getResources().getColor(R.color.settings_text_enabled));
            builder.setSpan(blueColor, hyperStartIndex, hyperlinkEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new ClickableSpan() {

                @Override
                public void onClick(View widget) {
                    ActivityUtils.openUrl(getActivity(), url);
                }


            }, hyperStartIndex, hyperlinkEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        textView.setText(builder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public ListView getListView() {
        return mListView;
    }

    @Override
    public void addData(List<String> dataList, String newData) {
        dataList.add(0, newData);
    }

    @Override
    public void onDataListChange(List<String> dataList) {
        if (dataList.size() == 0) {
            mPrompt.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mPrompt.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public String getDialogInputHint() {
        return getString(R.string.example_adblock_rule);
    }

    @Override
    protected String getInputFilteredPrompt() {
        return getString(R.string.adblock_rules_error_prompt);
    }

    @Override
    public boolean checkInputLineCanSave(String line) {
        return StringUtil.checkAsciiText(line) && isRuleValid(line) && !isAnnotation(line);
    }

    @Override
    protected boolean checkInputLineIsIllegal(String line) {
        return !isAnnotation(line) && (!StringUtil.checkAsciiText(line) || !isRuleValid(line));
    }

    private boolean isRuleValid(String rule) {
        return !(rule.contains("##") && rule.contains("'"));
    }

    private boolean isAnnotation(String rule) {
        return rule.startsWith("!");
    }


}
