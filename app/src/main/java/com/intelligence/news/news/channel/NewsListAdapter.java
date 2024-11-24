/*
 * Copyright (c) 2016 咖枯 <kaku201313@163.com | 3772304@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.intelligence.news.news.channel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.intelligence.news.news.cards.BaseCardView;
import com.intelligence.news.news.cards.NewsItemView;
import com.intelligence.news.news.mode.NewsData;

public class NewsListAdapter extends ArrayListAdapter<NewsData> {

    public NewsListAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemViewType(int position) {
        NewsData newData = mList.get(position);
        int type = 1;
        switch (newData.cardType) {
            case CardFactory.CARD_TEXT:
                type = 0;
                break;
            case CardFactory.CARD_TEXT_IMAGE:
                type = 1;
                break;
            case CardFactory.CARD_IMAGE:
                type = 2;
                break;
            case CardFactory.CARD_HOTWORD:
                type = 3;
                break;
            case CardFactory.CARD_ADVERT:
                type = 4;
                break;
        }
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return 5;// 有10种卡片，加可运营卡片19张,加RN卡片的预置数目
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final NewsData newsData = mList.get(position);
        int type = getItemViewType(position);
        if (type == CardFactory.CARD_ADVERT) {

        }
        NewsItemView item = null;
        BaseCardView cardView;
        if (convertView == null) {
            item = new NewsItemView(mContext);
            cardView = CardFactory.createCardView(mContext,newsData);
            if (cardView != null) {
                item.setBaseCardView(cardView);
            }
        } else {
            item = ((NewsItemView) convertView);
            cardView = item.getBaseCardView();
            convertView.setVisibility(View.VISIBLE);
        }
        cardView.updateData(newsData);
        convertView = item;
        return convertView;
    }
}
