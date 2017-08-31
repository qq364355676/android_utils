package com.utils.commonutils.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/8/31.
 */

public class T {
    /**
     * 短时间显示吐司
     * @param context   上下文
     * @param message   显示文本
     */
    public static void showShort(Context context,CharSequence message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间显示吐司
     * @param context   上下文
     * @param message   显示文本
     */
    public static void showLong(Context context,CharSequence message){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    /**
     * 自定义显示吐司时间
     * @param context   上下文
     * @param message   显示文本
     * @param duration  显示时间（毫秒）
     */
    public static void customShow(Context context,CharSequence message,int duration){
        Toast.makeText(context,message,duration).show();
    }
}
