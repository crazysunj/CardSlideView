package com.crazysunj.cardslide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;


/**
 * 线和圆交互的指示器
 *
 * @author sunjian
 * @date 2020-01-09 16:21
 */
public class CircleLineIndicator extends View {

    private int position;
    private int lineWidth;
    private int radius;
    private int space;
    private int size;
    private float offsetPercent;
    private boolean isLeft;
    private Paint selectedPaint;
    private Paint unselectedPaint;
    private RectF leftRectF;
    private RectF rightRectF;
    private float firstLeft;

    public CircleLineIndicator(Context context) {
        super(context);
        initView(context, null);
    }

    public CircleLineIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public CircleLineIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        int colorSelected, colorUnselected;
        if (attrs == null) {
            lineWidth = dp2px(context, 12f);
            space = dp2px(context, 8f);
            radius = dp2px(context, 4f);
            colorSelected = Color.parseColor("#FFC057");
            colorUnselected = Color.parseColor("#D8D8D8");
        } else {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleLineIndicator);
            lineWidth = ta.getDimensionPixelSize(R.styleable.CircleLineIndicator_circle_line_indicator_line_width, dp2px(context, 12f));
            space = ta.getDimensionPixelSize(R.styleable.CircleLineIndicator_circle_line_indicator_space, dp2px(context, 8f));
            radius = ta.getDimensionPixelSize(R.styleable.CircleLineIndicator_circle_line_indicator_radius, dp2px(context, 4f));
            colorSelected = ta.getColor(R.styleable.CircleLineIndicator_circle_line_indicator_color_selected, Color.parseColor("#FFC057"));
            colorUnselected = ta.getColor(R.styleable.CircleLineIndicator_circle_line_indicator_color_unselected, Color.parseColor("#D8D8D8"));
            ta.recycle();
        }

        selectedPaint = new Paint();
        selectedPaint.setAntiAlias(true);
        selectedPaint.setStyle(Paint.Style.FILL);
        selectedPaint.setColor(colorSelected);

        unselectedPaint = new Paint();
        unselectedPaint.setAntiAlias(true);
        unselectedPaint.setStyle(Paint.Style.FILL);
        unselectedPaint.setColor(colorUnselected);

        leftRectF = new RectF();
        rightRectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (size < 1) {
            return;
        }
        canvas.translate(getMeasuredWidth() / 2.f, getMeasuredHeight() / 2.f);
        if (position == size - 1) {
            float firstRight = -firstLeft + 2 * radius + lineWidth * offsetPercent;
            float lastLeft = firstLeft - 2 * radius - (1 - offsetPercent) * lineWidth;
            leftRectF.set(-firstLeft, -radius, firstRight, radius);
            canvas.drawRoundRect(leftRectF, radius, radius, isLeft ? unselectedPaint : selectedPaint);
            for (int i = 1; i < size - 1; i++) {
                canvas.drawCircle(firstRight + space + radius + (2 * radius + space) * (i - 1), 0, radius, unselectedPaint);
            }
            rightRectF.set(lastLeft, -radius, firstLeft, radius);
            canvas.drawRoundRect(rightRectF, radius, radius, isLeft ? selectedPaint : unselectedPaint);
        } else {
            for (int i = 0; i < position; i++) {
                canvas.drawCircle(radius - firstLeft + (2 * radius + space) * i, 0, radius, unselectedPaint);
            }
            float leftLeft = -firstLeft + (2 * radius + space) * position;
            float leftRight = leftLeft + 2 * radius + (1 - offsetPercent) * lineWidth;
            leftRectF.set(leftLeft, -radius, leftRight, radius);
            canvas.drawRoundRect(leftRectF, radius, radius, isLeft ? selectedPaint : unselectedPaint);
            float rightLeft = leftRight + space;
            float rightRight = rightLeft + 2 * radius + offsetPercent * lineWidth;
            rightRectF.set(rightLeft, -radius, rightRight, radius);
            canvas.drawRoundRect(rightRectF, radius, radius, isLeft ? unselectedPaint : selectedPaint);
            for (int i = position + 1; i < size - 1; i++) {
                canvas.drawCircle(rightRight + radius + space + (2 * radius + space) * (i - position - 1), 0, radius, unselectedPaint);
            }
        }
    }

    public void setSize(int size) {
        this.size = size;
        firstLeft = (lineWidth + 2 * radius + (2 * radius + space) * (size - 1)) / 2.f;
    }

    public void transform(boolean isLeft, int position, float offsetPercent) {
        this.isLeft = isLeft;
        this.position = position;
        this.offsetPercent = offsetPercent;
        invalidate();
    }

    private static int dp2px(Context context, float dp) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
