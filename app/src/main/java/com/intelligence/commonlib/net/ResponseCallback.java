package com.intelligence.commonlib.net;

/**
 * 创建： PengJunShan
 * 描述：回调接口
 */

public interface ResponseCallback<T> {

  //请求成功回调事件处理
  void onSuccess(T responseObj);

  //请求失败回调事件处理
  void onFailure(OkHttpException failuer);

}
