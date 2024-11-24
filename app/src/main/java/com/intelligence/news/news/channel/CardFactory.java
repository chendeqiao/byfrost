package com.intelligence.news.news.channel;

import android.content.Context;

import com.intelligence.news.news.cards.BaseCardView;
import com.intelligence.news.news.cards.HotWordCardView;
import com.intelligence.news.news.cards.ImageCardView;
import com.intelligence.news.news.cards.TextCardView;
import com.intelligence.news.news.cards.TextImageCardView;
import com.intelligence.news.news.mode.NewsData;

public final class CardFactory {
    //头条
    public static final int CARD_TEXT = 1001;
    //左文右图
    public static final int CARD_TEXT_IMAGE = 1002;
    //热搜词卡片
    public static final int CARD_HOTWORD = 1003;
    //大图
    public static final int CARD_IMAGE = 1004;
    //广告
    public static final int CARD_ADVERT = 1005;

    CardFactory() {
    }

    public static BaseCardView createCardView(Context context, NewsData data) {
        BaseCardView cardView;
        switch (data.cardType) {
            case CARD_TEXT:
                 cardView = new TextCardView(context);
                break;
            case CARD_TEXT_IMAGE:
                 cardView = new TextImageCardView(context);
                break;
            case CARD_HOTWORD:
                cardView = new HotWordCardView(context);
                break;
            case CARD_IMAGE:
                cardView = new ImageCardView(context);
                break;
            case CARD_ADVERT:
                cardView = new TextCardView(context);
                break;
            default:
                cardView = new TextCardView(context);
                break;
        }
        return cardView;
    }
}
