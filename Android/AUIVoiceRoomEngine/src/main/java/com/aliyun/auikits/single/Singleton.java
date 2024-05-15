package com.aliyun.auikits.single;

import java.util.Hashtable;
import java.util.Map;

public class Singleton {
    private static Object mLock = new Object();
    private static Map<String, Single> mObjects = new Hashtable<>();
    public static <T extends Single> T getInstance(Class<T> c) {
        String clsName = c.getName();
        if(!mObjects.containsKey(clsName)){
            synchronized (mLock){
                if(!mObjects.containsKey(clsName)){
                    try {
                        mObjects.put(clsName, c.newInstance());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mObjects.containsKey(clsName) ? (T)mObjects.get(clsName) : null;
    }
}
