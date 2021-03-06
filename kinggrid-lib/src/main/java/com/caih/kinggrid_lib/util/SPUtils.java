package com.caih.kinggrid_lib.util;

import android.content.Context;
import android.content.SharedPreferences;


import com.caih.kinggrid_lib.view.ConstantValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Author: wmy
 * Date: 2020/8/3 16:21
 */
public class SPUtils {

    public static final String FILE_NAME = "caih.com";
    public static final String KEY_HANDWRITE_MODE = "KEY_HAND_WRITE_MODE";
    public static final String KEY_PENCIL_WIDTH = "KEY_PENCIL_PATH";


    public static void put(Context context, String key, Object object) {

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        SharedPreferencesCompat.apply(editor);
    }



    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */

    public static Object get(Context context, String key, Object defaultObject) {
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            if (defaultObject instanceof String) {
                return sp.getString(key, (String) defaultObject);
            } else if (defaultObject instanceof Integer) {
                return sp.getInt(key, (Integer) defaultObject);
            } else if (defaultObject instanceof Boolean) {
                return sp.getBoolean(key, (Boolean) defaultObject);
            } else if (defaultObject instanceof Float) {
                return sp.getFloat(key, (Float) defaultObject);
            } else if (defaultObject instanceof Long) {
                return sp.getLong(key, (Long) defaultObject);
            }
        }
        return null;

    }



    /**
     * 移除某个key值已经对应的
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getAll();
    }

    /**
     * 解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }
            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            editor.commit();
            if (true) {
                return;
            }
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }

    }


    public static int getHandwriteMode(Context context){
        return (int)get(context, KEY_HANDWRITE_MODE, ConstantValue.WRITE_MODE_PEN);
    }

    public static void setHandwriteMode(Context context, int handwriteMode){
        put(context, KEY_HANDWRITE_MODE, handwriteMode);
    }

    public static int getPencilWidth(Context context){
        return (int)get(context, KEY_PENCIL_WIDTH, ConstantValue.DOCUMENT_PENCIL_DEFAULT_WIDTH);
    }

    public static void setPencilWidth(Context context, int pencilWidth){
        put(context, KEY_PENCIL_WIDTH, pencilWidth);
    }
}
