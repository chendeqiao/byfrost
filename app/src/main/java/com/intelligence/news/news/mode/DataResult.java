package com.intelligence.news.news.mode;

import com.intelligence.news.websites.bean.AllWebSiteData;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;

public class DataResult {
    public int returnCode = 0;
    public ArrayList newsList = new ArrayList();
    public ArrayList<WebSiteData> appsites = new ArrayList();
    public ArrayList<WebSiteData> lablesites = new ArrayList();
    public ArrayList<WebSiteData> toolssites = new ArrayList();
    public ArrayList<WebSiteData> browserhistory = new ArrayList();
    public ArrayList<WebSiteData> websites = new ArrayList();
    public ArrayList<AllWebSiteData> allWebSite = new ArrayList();
}
