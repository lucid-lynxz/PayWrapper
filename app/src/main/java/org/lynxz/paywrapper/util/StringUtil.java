package org.lynxz.paywrapper.util;


import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lynxz.wechatpaywrapper.util.LoggerUtil;

import java.lang.reflect.Type;

public class StringUtil {
    private static final String TAG = "StringUtil";
    private static Gson mGson = new Gson();
    private static Gson mFormatGson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String src) {
        return src == null || src.length() == 0;
    }

    /**
     * 格式化json字符串再输出
     */
    @NotNull
    public static String toPrettyJson(Object obj) {
        return toJsonInternal(mFormatGson, obj);
    }

    /**
     * 直接序列化(不做格式缩进)
     */
    @NotNull
    public static String toJson(Object obj) {
        return toJsonInternal(mGson, obj);
    }

    /**
     * 解析json字符串为指定的普通对象(非list)
     */
    @Nullable
    public static <T> T parseJson(String json, Class<? extends T> cls) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return mGson.fromJson(json, cls);
        } catch (Exception e) {
            LoggerUtil.w(TAG, "parseJson() fail:" + json + "\n" + e.getMessage());
            return null;
        }
    }

    /**
     * 对于转换成 List<Integer> 等整数的, Gson会将其解析成科学计数法,会被视为Double,导致报错
     * 建议解析成Double, 再自行进行转换
     * 参考: https://juejin.im/post/5cbb3c5af265da03ab23258c
     */
    public static <T> T parseListJson(String json) {
        Type type = new TypeToken<T>() {
        }.getType();

        return mGson.fromJson(json, type);
    }


    @NotNull
    private static String toJsonInternal(Gson gson, @Nullable Object obj) {
        try {
            return gson.toJson(obj);
        } catch (Exception e) {
            LoggerUtil.e(TAG, "toJson() fail:" + e.getMessage());
            return obj == null ? "" : obj.toString();
        }
    }

    /**
     * 对用户名做马赛克处理, 目前保留收尾明文, 中间部分用特定符号替代
     *
     * @param userName        明文
     * @param placeholderFlag 要使用的占位符, 默认为 *
     * @return
     */
    public static String userNameMosaic(String userName, String placeholderFlag) {
        if (TextUtils.isEmpty(placeholderFlag)) {
            placeholderFlag = "*";
        }
        int len = userName == null ? 0 : userName.length();
        if (len <= 1) {
            return "*";
        }

        String result = "";
        if (len >= 3) {
            StringBuilder sb = new StringBuilder();
            sb.append(userName.charAt(0));
            for (int i = 1; i < len - 1; i++) {
                sb.append(placeholderFlag);
            }
            sb.append(userName.charAt(len - 1));
            result = sb.toString();
        } else {
            result = userName.charAt(0) + placeholderFlag;
        }
        return result;
    }
}
