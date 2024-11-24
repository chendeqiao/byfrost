package com.intelligence.news.news.channel;

import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.news.news.mode.NewsData;

import java.util.ArrayList;
import java.util.List;

public class InsertHelper {

    private List<NewsData> insertCache = new ArrayList<>();

    public void addInsertCache(NewsData entity) {
        insertCache.add(entity);
    }

    public void removeOldInsertEntity(List<NewsData> targetList) {
        if (CollectionUtils.isEmpty(insertCache) || CollectionUtils.isEmpty(targetList)) {
            return;
        }
        targetList.removeAll(insertCache);
        insertCache.clear();
    }

    public void insert(List<NewsData> insertList, List<NewsData> targetList) {
        if (CollectionUtils.isEmpty(insertList) || CollectionUtils.isEmpty(targetList)) {
            return;
        }
//        removeOldInsertEntity(targetList);
        for (NewsData entity : insertList) {
            if (entity == null) {
                continue;
            }
            int position =entity.position - 1;
            if (position < 0 || position >= targetList.size()) {
                continue;
            }
            targetList.add(position, entity);
//            insertCache.add(entity);
        }
    }

    private List<NewsData> obtainDeleteList(List<NewsData> insertList) {
        List<NewsData> deleteList = new ArrayList<>();
        if (CollectionUtils.isEmpty(insertList)) {
            return deleteList;
        }
        for (NewsData entity : insertList) {
            if (entity == null) {
                continue;
            }
            if (insertCache.contains(entity)) {
                NewsData deleteEntity = obtainDeleteItem(entity);
                if (deleteEntity != null) {
                    deleteList.add(deleteEntity);
                }
            }
        }
        return deleteList;
    }

    //在已插入的数据中查找和当前数据重复，需要删除的数据
    private NewsData obtainDeleteItem(NewsData entity) {
        if (entity == null || insertCache.isEmpty()) {//判空处理
            return null;
        }
        for (NewsData item : insertCache) {
            //如果相同的数据
            if (compareBaseNewsEntity(item, entity) == 0) {
                //将数据从insertCache中移除
                insertCache.remove(item);
                //返回需要删除的数据
                return item;
            }
        }
        return null;
    }

    private int compareBaseNewsEntity(NewsData lhs, NewsData rhs) {
        if (lhs == null || rhs == null) {
            return 0;
        }
        return 1;
    }
}
