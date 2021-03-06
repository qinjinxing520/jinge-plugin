package com.caih.kinggrid_lib.view.sign.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * 图像操作工具类
 *
 * @author king
 * @since 2018/07/05
 */
public class BitmapUtil {

    private static int LETTERS_DEFAULT = 14;  // 每行默认字数 14个
    private static int LINE_NUM = 3;    // 行数3 行

    /**
     * 逐行扫描 清除边界空白
     *
     * @param blank 边距留多少个像素
     * @param color 背景色限定
     * @return 清除边界后的Bitmap
     */
    public static Bitmap clearBlank(Bitmap mBitmap, int blank, int color) {
        if (mBitmap != null) {
            int height = mBitmap.getHeight();
            int width = mBitmap.getWidth();
            int top = 0, left = 0, right = 0, bottom = 0;
            int[] widthPixels = new int[width];
            boolean isStop;
            for (int y = 0; y < height; y++) {
                mBitmap.getPixels(widthPixels, 0, width, 0, y, width, 1);
                isStop = false;
                for (int pix : widthPixels) {
                    if (pix != color) {

                        top = y;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int y = height - 1; y >= 0; y--) {
                mBitmap.getPixels(widthPixels, 0, width, 0, y, width, 1);
                isStop = false;
                for (int pix : widthPixels) {
                    if (pix != color) {
                        bottom = y;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            widthPixels = new int[height];
            for (int x = 0; x < width; x++) {
                mBitmap.getPixels(widthPixels, 0, 1, x, 0, 1, height);
                isStop = false;
                for (int pix : widthPixels) {
                    if (pix != color) {
                        left = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int x = width - 1; x > 0; x--) {
                mBitmap.getPixels(widthPixels, 0, 1, x, 0, 1, height);
                isStop = false;
                for (int pix : widthPixels) {
                    if (pix != color) {
                        right = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            if (blank < 0) {
                blank = 0;
            }
            left = left - blank > 0 ? left - blank : 0;
            top = top - blank > 0 ? top - blank : 0;
            right = right + blank > width - 1 ? width - 1 : right + blank;
            bottom = bottom + blank > height - 1 ? height - 1 : bottom + blank;
            return Bitmap.createBitmap(mBitmap, left, top, right - left, bottom - top);
        } else {
            return null;
        }
    }

    /**
     * 清除bitmap左右边界空白
     *
     * @param mBitmap 源图
     * @param blank   边距留多少个像素
     * @param color   背景色限定
     * @return 清除后的bitmap
     */
    public static Bitmap clearLRBlank(Bitmap mBitmap, int blank, int color) {
        if (mBitmap != null) {
            int height = mBitmap.getHeight();
            int width = mBitmap.getWidth();
            int left = 0, right = 0;
            int[] pixs = new int[height];
            boolean isStop;
            for (int x = 0; x < width; x++) {
                mBitmap.getPixels(pixs, 0, 1, x, 0, 1, height);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != color) {
                        left = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int x = width - 1; x > 0; x--) {
                mBitmap.getPixels(pixs, 0, 1, x, 0, 1, height);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != color) {
                        right = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            if (blank < 0) {
                blank = 0;
            }
            left = left - blank > 0 ? left - blank : 0;
            right = right + blank > width - 1 ? width - 1 : right + blank;
            return Bitmap.createBitmap(mBitmap, left, 0, right - left, height);
        } else {
            return null;
        }
    }

    /**
     * 给Bitmap添加背景色
     *
     * @param srcBitmap 源图
     * @param color     背景颜色
     * @return 修改背景后的bitmap
     */
    public static Bitmap drawBgToBitmap(Bitmap srcBitmap, int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), paint);
        canvas.drawBitmap(srcBitmap, 0, 0, paint);
        return bitmap;
    }

    /**
     * 保存图像到本地
     *
     * @param bmp     源图
     * @return 保存后的图片地址
     */
    public static String saveImage(String filePath, Bitmap bmp) {
        if (bmp == null) {
            return null;
        }
        FileOutputStream fos = null;
        try {
            File file = new File(filePath);
            file.delete();
            Bitmap outB=bmp.copy(Bitmap.Config.ARGB_8888,true);
            Canvas canvas=new Canvas(outB);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bmp, 0, 0, null);
            fos = new FileOutputStream(file);
            outB.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean saveBitmap2File(String filePath, Bitmap bitmap){
        File file = new File(filePath);
        File fileDirs = new File(file.getParent());
        if (!fileDirs.exists()) {
            fileDirs.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    public static boolean saveBitmap2File(String filePath, Bitmap bitmap, Bitmap.CompressFormat format){
//        File file = new File(filePath);
//        File fileDirs = new File(file.getParent());
//        if (!fileDirs.exists()) {
//            fileDirs.mkdirs();
//        }
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//        Bitmap outB = null;
//        if (format == Bitmap.CompressFormat.JPEG) {
//            outB=bitmap.copy(Bitmap.Config.ARGB_8888,true);
//            Canvas canvas=new Canvas(outB);
//            canvas.drawColor(Color.WHITE);
//            canvas.drawBitmap(bitmap, 0, 0, null);
//        }
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            if (outB != null) {
//                outB.compress(format, 75, fos);
//            } else {
//                bitmap.compress(format, 75, fos);
//            }
//            fos.close();
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    /**
     * 根据宽度缩放图片，高度等比例
     *
     * @param bm       源图
     * @param newWidth 新宽度
     * @return 缩放后的bitmap
     */
    public static Bitmap zoomImg(Bitmap bm, int newWidth) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float ratio = ((float) newWidth) / width;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    /**
     * 缩放图片至指定宽高
     *
     * @param bm        源图
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @return 缩放后的bitmap
     */
    public static Bitmap zoomImage(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    /**
     * 根据宽高之中最大缩放比缩放图片
     *
     * @param bitmap    源图
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @return 缩放后的bitmap
     */
    public static Bitmap zoomImg(Bitmap bitmap, int newWidth,
                                 int newHeight) {
        // 获取这个图片的宽和高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        float ratio = Math.max(scaleWidth, scaleHeight);
        // 缩放图片动作
        matrix.postScale(ratio, ratio);
        return Bitmap.createBitmap(bitmap, 0, 0, width,
                height, matrix, true);
    }


    /**
     * 给图片右下角添加水印
     *
     * @param src       源图
     * @param watermark 水印图
     * @param bgColor   背景色
     * @param fixed     源图是否固定大小，固定则在源图上绘制印章，不固定则动态改变图片大小
     * @return 添加水印后的图片
     */
    public static Bitmap addWaterMask(Bitmap src, Bitmap watermark, int bgColor, boolean fixed) {
        int w = src.getWidth();
        int h = src.getHeight();
        //获取原始水印图片的宽、高
        int w2 = watermark.getWidth();
        int h2 = watermark.getHeight();

        //合理控制水印大小
        Matrix matrix1 = new Matrix();
        float ratio;

        ratio = (float) w2 / w;
        if (ratio > 1.0f && ratio <= 2.0f) {
            ratio = 0.7f;
        } else if (ratio > 2.0f) {
            ratio = 0.5f;
        } else if (ratio <= 0.2f) {
            ratio = 2.0f;
        } else if (ratio < 0.3f) {
            ratio = 1.5f;
        } else if (ratio <= 0.4f) {
            ratio = 1.2f;
        } else if (ratio < 1.0f) {
            ratio = 1.0f;
        }
        matrix1.postScale(ratio, ratio);
        watermark = Bitmap.createBitmap(watermark, 0, 0, w2, h2, matrix1, true);

        //获取新的水印图片的宽、高
        w2 = watermark.getWidth();
        h2 = watermark.getHeight();
        if (!fixed) {
            if (w < 1.5 * w2) {
                w = w + w2;
            }
            if (h < 2 * h2) {
                h = h + h2;
            }
        }
        // 创建一个新的和SRC长度宽度一样的位图
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas cv = new Canvas(result);
        cv.drawColor(bgColor);
        //在canvas上绘制原图和新的水印图
        cv.drawBitmap(src, 0, 0, null);
        //水印图绘制在画布的右下角，距离右边和底部都为20
        cv.drawBitmap(watermark, w - w2 - 20, h - h2 - 20, null);
        cv.save();
        cv.restore();
        return result;
    }

    /**
     * 修改图片颜色
     *
     * @param inBitmap 源图
     * @param color    颜色
     * @return 修改颜色后的图片
     */
    public static Bitmap changeBitmapColor(Bitmap inBitmap, int color) {
        if (inBitmap == null) {
            return null;
        }
        Bitmap outBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), inBitmap.getConfig());
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(inBitmap, 0, 0, paint);
        return outBitmap;
    }

    /**
     * 设置ImageView的图片，支持改变图片颜色
     * @param iv
     * @param id
     * @param color
     */
    public static void setImage(ImageView iv, int id, int color) {
        Bitmap bitmap = BitmapFactory.decodeResource(iv.getResources(), id);
        iv.setImageBitmap(BitmapUtil.changeBitmapColor(bitmap, color));
    }

    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {

        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            Log.i("BitmapUtil", "backBitmap=" + backBitmap + ";frontBitmap=" + frontBitmap);
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        Rect baseRect  = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        Log.i("BitmapUtil", "baseRect=" + baseRect.width()+" "+baseRect.height() + ";frontRect=" + frontRect.width()+" "+frontRect.height());
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null);
        return bitmap;
    }

    /**
     * 使用Matrix
     * @param bitmap 原始的Bitmap
     * @return 缩放后的Bitmap
     */
    public static Bitmap scale(Bitmap bitmap, int width, int height){
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//创建和目标相同大小的空Bitmap
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        Bitmap temp = bitmap;

        //针对绘制bitmap添加抗锯齿
        PaintFlagsDrawFilter pfd= new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        paint.setFilterBitmap(true); //对Bitmap进行滤波处理
        paint.setAntiAlias(true);//设置抗锯齿
        canvas.setDrawFilter(pfd);
        Rect rect = new Rect(0, 0, width, height);
        canvas.drawBitmap(temp, null, rect, paint);
        return newBitmap;
    }

    /**
     * 文字转批注+签名+日期
     * @param text
     * @return
     */
    public static Bitmap text2AnnotBitmap(String text, Context context){
        // 将文字转换为图片
        // 从本地读取签名图片
        // 拼接当前日期
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String date = year+"."+month+"."+day;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int defaultPicSize = dm.widthPixels/9*4;

        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        float textSize;
        int lineLetters = LETTERS_DEFAULT;
        // 一行14个字，3行就是42个字
        if(text.length() <= LETTERS_DEFAULT*3){
            textSize = defaultPicSize / lineLetters;
        } else {
            lineLetters = text.length() / LINE_NUM;
            textSize = defaultPicSize / lineLetters;
        }
        paint.setTextSize(textSize);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fangsong.ttf");
        paint.setTypeface(typeface);
        Log.i("BitmapUtil", "textSize " +textSize);
        // 生成批注文字图片
        Bitmap annotBitmap = Bitmap.createBitmap(defaultPicSize, defaultPicSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(annotBitmap);
        canvas.drawBitmap(annotBitmap, 0, 0, null);
        StaticLayout sl = new StaticLayout(text, paint, defaultPicSize, Layout.Alignment.ALIGN_NORMAL, 1.0f ,1.0f, false);
        sl.draw(canvas);
        Log.i("BitmapUtil", "StaticLayout "+sl.getWidth() +" "+sl.getHeight());
        // 此时staticLayout的高度才是文字的真实高度 需要对原有的txtBitmap进行截取
        annotBitmap = Bitmap.createBitmap(annotBitmap, 0, 0, sl.getWidth(), sl.getHeight());
        int annotWidth = annotBitmap.getWidth();
        int annotHeight = annotBitmap.getHeight();
        Log.i("BitmapUtil", "截取后的annotBitmap 宽高 "+annotWidth+" "+annotHeight);
        int tabSpace = annotHeight;
        // 1.5倍行间距 暂定以日期高度的一半 作为签批内容和签名的间距
        int letterSpace = (int)(annotHeight*0.5);
        // 生成日期图片
        Bitmap dateBitmap = Bitmap.createBitmap(defaultPicSize, defaultPicSize, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(dateBitmap);
        canvas.drawBitmap(dateBitmap, 0, 0, null);
        sl = new StaticLayout(date, paint, defaultPicSize, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1.0f, false);
        sl.draw(canvas);
        dateBitmap = clearBlank(dateBitmap, 4, Color.TRANSPARENT);
        int dateWidth = dateBitmap.getWidth();
        int dateHeight = dateBitmap.getHeight();
        Log.i("BitmapUtil", "日期文字宽高 "+dateWidth +" "+dateHeight);
        // 签名图片
        String signPath = SignUtils.getSignPath(context);
        try {
            FileInputStream fis = new FileInputStream(signPath);
            Log.i("BitmapUtil", "开始读取签名图片");
            Bitmap signBitmap = BitmapFactory.decodeStream(fis);
            // 先把签名和日期拼成一行
            // 签名的高为 日期高度的2.5倍
            int signHeight = (int)(dateBitmap.getHeight()*2.5);
            int signWidth = signBitmap.getWidth()*signHeight/signBitmap.getHeight();
            Log.i("BitmapUtil", "签名宽高 "+signWidth + " "+ signHeight);
            signBitmap = scale(signBitmap, signWidth, signHeight);
            int baseHeight = annotHeight + signHeight + letterSpace;
            int baseWidth = calculateAnnotWith(defaultPicSize, text, textSize, dateWidth, signWidth, tabSpace, letterSpace);
            Log.i("BitmapUtil", "baseWidth "+baseWidth+" baseHeight "+baseHeight);
            // 底图宽高
            Bitmap signDatebitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(signDatebitmap);
            canvas.drawColor(Color.TRANSPARENT);
            // 先叠加批注内容
            Rect baseAnnotRect = new Rect(0, 0, annotWidth, annotHeight);
            Rect annotRect = new Rect(0, 0, annotWidth, annotHeight);
            canvas.drawBitmap(annotBitmap, annotRect, baseAnnotRect, null);
            // 先叠加签名
            Rect baseSignRect  = new Rect(baseWidth-dateWidth -letterSpace-signWidth, annotHeight + letterSpace , baseWidth-dateWidth -letterSpace, baseHeight);
            Rect signRect = new Rect(0, 0, signWidth, signHeight);
            canvas.drawBitmap(signBitmap, signRect, baseSignRect, null);
            // 再叠加日期
            Rect baseDateRect  = new Rect(baseWidth-dateWidth , annotHeight +letterSpace + (signHeight-dateHeight), baseWidth, baseHeight);
            Rect dateRect = new Rect(0, 0, dateWidth, dateHeight);
            canvas.drawBitmap(dateBitmap, dateRect, baseDateRect, null);
            String savePath = SignUtils.getAnnotPath(context);
            saveImage(savePath, signDatebitmap);
            return signDatebitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 开始绘文字
        return dateBitmap;
    }

    /**
     * 获取批注图片合适的宽度
     * @param defaultPicSize    预设的图片最大宽度
     * @param text  批注内容
     * @param textSize  文字大小
     * @param dateWidth 日期文字的长度
     * @param signWidth 签名的长度
     * @param letterSpace 字间距
     * @return
     */
    public static int calculateAnnotWith(int defaultPicSize, String text, float textSize, int dateWidth, int signWidth, int tabSpace, int letterSpace){
        if(text.length() >= LETTERS_DEFAULT){   // 批注字数大于一行的字数
            return defaultPicSize;
        }else {
            // 不够一行 批注宽度为字数 * 字号
            float totalAnnotWidth = textSize * text.length();
            // 签名 + 日期的宽度为：tab间隔 + 日期宽度 + 字间距 + 签名宽度
            float signDateWidth = dateWidth +letterSpace+ signWidth + tabSpace;
            if(totalAnnotWidth > signDateWidth) {
                return (int) totalAnnotWidth;
            }else {
                return (int) signDateWidth;
            }
        }
    }

    public static Bitmap white2transparent(Bitmap bitmap){
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for(int h = 0;h<height; h++){
            for(int w = 0;w<width; w++){
                int color = bitmap.getPixel(w,h);
                int g = Color.green(color);
                int r = Color.red(color);
                int b = Color.blue(color);
                int a = Color.alpha(color);
                if(g>=250 && r >=250 && b >=250){
                    a = 0;
                }
                color = Color.argb(a, r, g, b);
                createBitmap.setPixel(w, h, color);
            }
        }
        return createBitmap;
    }

}
