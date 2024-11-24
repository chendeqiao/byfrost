package com.intelligence.commonlib.notification;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;

public class NotificationCenter<T> {
    private static NotificationCenter sLaunchDispatchManager = new NotificationCenter();
    private static HashMap<Class<?>, Subscriber> subscriberHashMap = new HashMap<>();

    private NotificationCenter() {
    }

    public static NotificationCenter defaultCenter() {
        return sLaunchDispatchManager;
    }

    public void subscriber(Class<?> cls, Subscriber subscriber) {
        if (!subscriberHashMap.containsKey(cls)) {
            subscriberHashMap.put(cls, subscriber);
        }
    }

    public void unsubscribe(Class<?> cls, Subscriber subscriber) {
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            Object ele = it.next();
            if (ele.equals(cls)) {
                it.remove();
            }
        }
//        for (Map.Entry<Class<?>, Subscriber> item : subscriberHashMap.entrySet()) {
//            Class<?> key = item.getKey();
//
//                subscriberHashMap.remove(item.getKey());
//            }
//        }
    }

    public void publish(final T t) {
        if (subscriberHashMap == null) {
            return;
        }
        try {
            for (Class<?> key : subscriberHashMap.keySet()) {
                final Subscriber subscriber = subscriberHashMap.get(key);
                Type[] type1 = subscriber.getClass().getGenericInterfaces();
                for (Type type : type1) {
                    if (type instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) type;
                        Type claz = pType.getActualTypeArguments()[0];
                        if (claz == t.getClass()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    subscriber.onEvent(t);
                                }
                            });
                        }
                    }
                }
            }
        }catch (Exception e){
        }
    }
}
