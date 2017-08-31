package com.utils.commonutils.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * bitmap互转工具类
 */

public class BitmapUtils {

    /**
     * bitmap转byte[]
     *
     * @param bm bitmap
     * @return
     */
    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * byte[] 转 bitmap
     *
     * @param bytes byte[]
     * @return
     */
    public static Bitmap bytesToBitmap(byte[] bytes) {
        if (bytes.length != 0) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    /**
     * drawable转 bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * drawable 缩放
     */
    public static Drawable zoomDrawable(Resources res,Drawable drawable, int w, int h) {
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                // drawable转换成bitmap
                Bitmap oldbmp = drawableToBitmap(drawable);
                // 创建操作图片用的Matrix对象
                Matrix matrix = new Matrix();
                // 计算缩放比例
                float sx = ((float) w / width);
                 float sy = ((float) h / height);
                 // 设置缩放比例
                 matrix.postScale(sx, sy);
                 // 建立新的bitmap，其内容是对原bitmap的缩放后的图
                 Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,matrix, true);
                 return new BitmapDrawable(res,newbmp);
             }
    /**
     * 文字转换Bitmap
     * @param text
     * @return
     */
    public static Drawable createMapBitMap(Context context, String text) {

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(32);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        float textLength = paint.measureText(text);
        int width = (int) textLength + 10;
        int height = (int) (paint.getTextSize()*2);

        Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas cv = new Canvas(newb);
        cv.drawColor(Color.TRANSPARENT);

        cv.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        cv.drawText(text, width / 2, (height/2 ), paint);

        cv.save(Canvas.ALL_SAVE_FLAG);// 保存
        cv.restore();// 存储

        return new BitmapDrawable(context.getResources(),newb);

    }
    /**
     * 字符串转bitmap
     * @param str
     * @return
     */
    public static Bitmap stringToBitmap(String str){
        try{
            byte[] bytes = Base64.decode(str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }catch(Exception e){
            return null;
        }
    }
    /**
     * bitmap转字符串
     * @param bitmap
     * @return
     */
    public static String bitmapToString(Bitmap bitmap){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            return Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }
}
