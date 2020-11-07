/*
 * Copyright 2017 Sun Jian
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crazysunj.cardslideview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * @author sunjian
 * @date 2020/11/3 下午8:08
 */
public class BitmapHelper {
    /**
     * 把原始图片转换成倒影图片，该方法比较耗时，如果放在主线程可能会卡顿
     */
    @WorkerThread
    @NonNull
    public static Bitmap convertReflection(@NonNull Bitmap originalImage, int viewWidth, int viewHeight) {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return originalImage;
        }
        final int width = originalImage.getWidth();
        final int height = originalImage.getHeight();
        final int showHeight = (int) (viewHeight / 2.f);
        final float widthRate = viewWidth * 1.f / width;
        final float heightRate = showHeight * 1.f / height;
        final int cropWidth, cropHeight;
        if (widthRate > heightRate) {
            cropWidth = width;
            cropHeight = (int) (showHeight * 1.f / viewWidth * cropWidth);
        } else {
            cropHeight = height;
            cropWidth = (int) (viewWidth * 1.f / showHeight * cropHeight);
        }
        Matrix matrix = new Matrix();
        final float scale = Math.min(viewWidth * 1.f / cropWidth, showHeight * 1.f / cropHeight);
        final int diffX = (int) ((width - cropWidth) / 2.f);
        final int diffY = (int) ((height - cropHeight) / 2.f);
        matrix.postScale(scale, -scale);
        Bitmap reflectionBitmap = Bitmap.createBitmap(originalImage, Math.max(diffX, 0), Math.max(diffY, 0), cropWidth, cropHeight, matrix, true);
        Bitmap totalBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(totalBitmap);
        // 先绘制原图
        matrix.reset();
        matrix.postScale(scale, scale);
        canvas.drawBitmap(Bitmap.createBitmap(originalImage, Math.max(diffX, 0), Math.max(diffY, 0), cropWidth, cropHeight, matrix, true), 0, 0, null);
        // 绘制间隔
        Paint marginPaint = new Paint();
        marginPaint.setColor(Color.BLACK);
        canvas.drawRect(0, showHeight, viewWidth, showHeight, marginPaint);
        // 绘制倒影
        canvas.drawBitmap(alphaBitmap(reflectionBitmap), 0, showHeight, null);
        return totalBitmap;
    }

    private static Bitmap alphaBitmap(Bitmap sourceBitmap) {
        final int width = sourceBitmap.getWidth();
        final int height = sourceBitmap.getHeight();
        int[] argb = new int[width * height];
        // 获得原图的ARGB值
        sourceBitmap.getPixels(argb, 0, width, 0, 0, width, height);
        // 透明百分比0-1，0为完全透明
        float percent = 0.9f;
        // 透明度数值
        float alpha = percent * 255;
        // 图片渐变的范围
        final float range = height * 1.f;
        // 透明度递减常量
        final float decreasingConstant = percent / range;
        // 从上往下
        int start = width * (height - (int) range);
        for (int i = start; i < argb.length; i++) {
            if (i % width == 0) {
                percent = percent - decreasingConstant;
                alpha = (float) (Math.pow(percent, 3) * 255);
            }
            argb[i] = ((int) alpha << 24) | (argb[i] & 0x00FFFFFF);
        }
        return Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888);
    }
}
