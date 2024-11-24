package com.intelligence.ad;
public class AdUtils {

public static final String NET_KEY = "1d58b4dddd11133f7b2f86d3a9859feb";

//  public static final String HOT_WROD_API = "http://api.tianapi.com/txapi/";
  public static final String HOST = "http://api.tianapi.com/";
  public static final String URL_HOT_WROD = HOST+"txapi/";
  public static final String URL_WEATHER = HOST+"txapi/tianqi/index";

  public static final String HOT_WORD_BAIDU = URL_HOT_WROD+"nethot/index";
  public static final String HOT_WORD_WEIBO = URL_HOT_WROD+"weibohot/index";
  public static final String HOT_WORD_DOUYIN = URL_HOT_WROD+"douyinhot/index";

  public static final String URL_NEWS_INDEX = "index";
  public static final String URL_NEWS_BULLETIN = HOST+"bulletin/"+URL_NEWS_INDEX;
  public static final String URL_NEWS_WORD = HOST+"world/"+URL_NEWS_INDEX;

  public static final int PAGE_BAIDU = 0;
  public static final int PAGE_DOUYIN = 1;
  public static final int PAGE_WEIBO = 2;


  public static final int NEWS_BULLETIN = 0;
  public static final int NEWS_WORLD = 1;
  public static final int NEWS_ALL = 2;


  //bmob 配置文件
  public static final String BMOB_APPLICATION_ID = "c8a151b7e9e52dbd3212a20d75f64faa";
  public static final String BMOB_REST_KEY = "4edb1ca43cb956b1c7d20e58bfc0f309";

}
