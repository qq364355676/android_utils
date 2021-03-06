package com.utils.commonutils.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 图片处理相关工具类
 */

public class ImageUtils {

    /**
     * 保存图片的Exif信息至压缩后的图片中
     *
     * @param oldFilePath 原始图片路径
     * @param newFilePath 压缩后图片路径
     * @throws Exception
     */
    public static void saveExif(String oldFilePath, String newFilePath) throws Exception {
        ExifInterface oldExif = new ExifInterface(oldFilePath);
        ExifInterface newExif = new ExifInterface(newFilePath);
        Class<ExifInterface> cls = ExifInterface.class;
        Field[] fields = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].getName();
            if (!TextUtils.isEmpty(fieldName) && fieldName.startsWith("TAG")) {
                String fieldValue = fields[i].get(cls).toString();
                String attribute = oldExif.getAttribute(fieldValue);
                if (attribute != null) {
                    newExif.setAttribute(fieldValue, attribute);
                }
            }
        }
        newExif.saveAttributes();
    }

    /**
     *  把 bitmap 存储为指定路径
     * @param bitmap    bitmap
     * @param outPath   输出的路径
     * @throws FileNotFoundException
     */
    public static void storeImage(Bitmap bitmap, String outPath) throws FileNotFoundException {
        FileOutputStream os = new FileOutputStream(outPath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
    }

    /**
     *
     * 从指定路径获取bitmap
     * @param imgPath 图片路径
     * @return
     */
    public static Bitmap getBitmap(String imgPath) {
        // Get bitmap through image path
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        // Do not compress
        newOpts.inSampleSize = 1;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(imgPath, newOpts);
    }

    /**
     *  按比例压缩后保存到指定路径 (Bitmap)
     * @param bm        bitmap
     * @param outPath   压缩后的图片路径
     * @param pixelW    宽
     * @param pixelH    高
     * @throws FileNotFoundException
     */
    public static void ratioAndGenThumb(Bitmap bm, String outPath, float pixelW, float pixelH) throws FileNotFoundException {
        Bitmap bitmap = ratioCompress(bm, pixelW, pixelH);
        storeImage( bitmap, outPath);
    }

    /**
     * 按比例压缩后保存至指定路径 (路径获取图片)
     *  删除原始图片
     * @param imgPath   原始图片路径
     * @param outPath   压缩后图片路径
     * @param pixelW    宽
     * @param pixelH    高
     * @param needsDelete 是否需要删除原始图片 true 删除  false 不删除
     * @throws FileNotFoundException
     */
    public static void ratioAndGenThumb(String imgPath, String outPath, float pixelW, float pixelH, boolean needsDelete) throws FileNotFoundException {
        Bitmap bitmap = ratioCompress(imgPath, pixelW, pixelH);
        storeImage( bitmap, outPath);

        // Delete original file
        if (needsDelete) {
            File file = new File (imgPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     *  按比例大小压缩 （Bitmap）
     * @param bm  bitmap
     * @param pixelW    宽度
     * @param pixelH    高度
     * @return
     */
    public static Bitmap ratioCompress(Bitmap bm, float pixelW, float pixelH) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
        if( os.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();//重置baos即清空baos
            bm.compress(Bitmap.CompressFormat.JPEG, 50, os);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true后只读取图片宽高等边界信息
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        is = new ByteArrayInputStream(os.toByteArray());
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        return bitmap;
    }

    /**
     *  按比例大小压缩 （路径获取图片）
     * @param imgPath   图片路径
     * @param pixelW    宽
     * @param pixelH    高
     * @return
     */
    public static Bitmap ratioCompress(String imgPath, float pixelW, float pixelH) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        // Get bitmap info, but notice that bitmap is null now
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath,newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 想要缩放的目标尺寸
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        return bitmap;
    }

    /**
     *  质量压缩并保存到指定的路径
     * @param bm        bitmap
     * @param outPath   指定路径
     * @param maxSize   图片最大值
     * @throws IOException
     */
    public static void compressAndGenImage(Bitmap bm, String outPath, int maxSize) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // scale
        int options = 100;
        // Store the bitmap into output stream(no compress)
        bm.compress(Bitmap.CompressFormat.JPEG, options, os);
        // Compress by loop
        while ( os.toByteArray().length / 1024 > maxSize) {
            // Clean up os
            os.reset();
            // interval 10
            options -= 10;
            bm.compress(Bitmap.CompressFormat.JPEG, options, os);
        }

        // Generate compressed image file
        FileOutputStream fos = new FileOutputStream(outPath);
        fos.write(os.toByteArray());
        fos.flush();
        fos.close();
    }

    /**
     *  给图片添加水印  添加水印图片和文字
     * @param bm            原始图片
     * @param waterMarkBm   图片水印
     * @paran title         文字水印
     * @return
     *
     */

    public static Bitmap addWaterMark(Bitmap bm, Bitmap waterMarkBm, String title) {

        // 获取原始图片与水印图片的宽与高
        int w = bm.getWidth();
        int h = bm.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(newBitmap);
        mCanvas.drawBitmap(bm, 0, 0, null);
        if (waterMarkBm != null) {
            int ww = waterMarkBm.getWidth();
            int wh = waterMarkBm.getHeight();
            // 往位图中开始画入src原始图片
            // 在src的右下角添加水印
            Paint paint = new Paint();
            //paint.setAlpha(100);
            mCanvas.drawBitmap(waterMarkBm, w - ww - 5, h - wh - 5, paint);
        }
        // 开始加入文字
        if (null != title) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(26);
            String familyName = "宋体";
            Typeface typeface = Typeface.create(familyName,
                    Typeface.BOLD_ITALIC);
            textPaint.setTypeface(typeface);
            textPaint.setTextAlign(Paint.Align.CENTER);
            float width = textPaint.measureText(title);
            mCanvas.drawText(title, w - width/2, h - 15, textPaint);

        }
        mCanvas.save(Canvas.ALL_SAVE_FLAG);
        mCanvas.restore();
        return newBitmap;
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path
     *            图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getImageDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm 需要旋转的图片
     * @param path 图片路径
     * @return 旋转后的图片
     */
    public static Bitmap rotateImageByDegree(Bitmap bm,String path) {
        Bitmap bitmap = null;
        int degree = getImageDegree(path);
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            return null;
        }
        if (bitmap == null) {
            bitmap = bm;
        }
        if (bm != bitmap) {
            bm.recycle();
        }
        return bitmap;
    }
}
