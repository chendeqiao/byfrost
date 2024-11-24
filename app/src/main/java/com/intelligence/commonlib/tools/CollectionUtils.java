package com.intelligence.commonlib.tools;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CollectionUtils<T extends Comparable>{

    /**判断集合是否为空*/
    public static  boolean isEmpty(List list){
        return list == null || list.size() == 0;
    }

    /**从集合2中查找与集合1重复的数据集合*/
    public List<T> findRepeatedItemList(List<T> list1, List<T> list2){
        if(list1 == null || list2 == null){
            return null;
        }
        List<T> newList = new ArrayList();
        newList.addAll(list2);
        T min = null,max =null,t = null;
        //找出最小数组里的最大和最小的
        for(int i = 1; i < newList.size();i++){
            if(i == 1){
                max = min = newList.get(0);
            }
            t = newList.get(i);
            if(t.compareTo(min) < 0){
                min = t;
            }
            if(t.compareTo(max) > 0){
                max = t;
            }
        }

        //找出最大数组里介于最小数组里最大数据和最小数据之前的数据（重复数据在这段区间）
        List<T> repeatList = null;
        T  t2 = null;
        for(int i = 0;i<list1.size();i++){
            t = list1.get(i);
            if(t.compareTo(min) < 0 || t.compareTo(max) > 0){
                continue;
            }else {
                for(int j = 0;j<list2.size();j++){
                   t2 = list2.get(j);
                    if(t.compareTo(t2) == 0){
                        if(repeatList == null){
                            repeatList   = new ArrayList();
                        }
                        repeatList.add(t2);
                    }
                }
            }
        }
        return repeatList;
    }

    /**查找某单个集合中重复的数据集合*/
    public List<T> findRepeatedItemListInSingleList(List<T> list){
        if (list.size() == 1) {
            return list;
        }
        List<T> repeatList = null;
        //要保持原有集合不变,新生成一个集合.
        List<T> newList = new ArrayList();
        newList.addAll(list);
        //对新集合进行排序
        Collections.sort(newList);
        //遍历新集合
        T t1 , t2;
        int N = newList.size();
        for(int i = 0;i < N;){
            t1 = newList.get(i);
            int index = i+1;
            while(index < N -1){
                t2 = newList.get(index);
                if(t1.compareTo(t2) == 0){
                    //如果没有集合对象,生成集合（第一次找到重复数据时创建）
                   if(repeatList == null) {
                       repeatList = new LinkedList<>();
                   }
                   //将每次重复数据第一个数据放到重复集合里，后面的做删除处理
                   if(!repeatList.contains(t1)) {
                       repeatList.add(t1);
                   }
                   //索引位置后移，查看下一数据.
                   index++;
                   i = index+1;
                }else{

                    //如果没有重复,将从未重复的位置进行遍历
                    i = index;
                    //清空t1
                    t1 = null;
                    t2 = null;
                    break;
                }
            }

            if(index == N-1){
                //扫描结束
                break;
            }
        }

        return repeatList;
    }
}
