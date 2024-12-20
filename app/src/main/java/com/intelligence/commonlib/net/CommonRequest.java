package com.intelligence.commonlib.net;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CommonRequest {

  public static Request createGetRequest(String url, RequestParams params) {
    StringBuilder urlBuilder = new StringBuilder(url).append("?");

    if (params != null) {
      for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
        urlBuilder
            .append(entry.getKey())
            .append("=")
            .append(entry.getValue())
            .append("&");
      }
    }

    return new Request.Builder().url(urlBuilder.substring(0, urlBuilder.length() - 1))
        .get().build();
  }

  public static Request createPostRequest(String url, RequestParams params) {
    FormBody.Builder mFromBodyBuilder = new FormBody.Builder();

    //将请求参数逐一遍历添加到我们的请求构建类中
    for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
      mFromBodyBuilder.add(entry.getKey(), entry.getValue());
    }

    //通过请求构建类的build方法获取到真正的请求体对象
    FormBody mFormBody = mFromBodyBuilder.build();
    Request request = new Request.Builder()
        .url(url)
        .post(mFormBody)
        .build();

    return request;
  }


  /**
   * 混合form和图片
   * @return 返回一个创建好的Request对象
   */
  public static Request createMultipartRequest(String url, RequestParams params, List<File> files) {

    //构建多部件builder
    MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
    //获取参数并放到请求体中
    try {
      if (params != null) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
          //将请求参数逐一遍历添加到我们的请求构建类中
          bodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
          jsonObject.put(entry.getKey(), entry.getValue());
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    //添加图片集合放到请求体中
    if (files != null) {
      for (File f : files) {
        bodyBuilder.addFormDataPart("files", f.getName(),
            RequestBody.create(MediaType.parse("image/png"), f));
      }
    }

    Request request = new Request.Builder()
        .url(url)
        .post(bodyBuilder.build())
        .build();

    return request;
  }


}
