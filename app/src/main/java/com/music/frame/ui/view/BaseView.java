package com.music.frame.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public abstract class BaseView extends View {

    private Paint paint;
    private Canvas canvas;

    public BaseView(Context context) {
        super(context);
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 文字居中绘制(内容不变的情况)
     * @param centerX
     * @param centerY
     * @param content
     */
    public void drawCenterTextOne(int centerX,int centerY, String content){
        Rect bounds=new Rect();
        paint.getTextBounds(content,0,content.length(),bounds);
        float offSet=(bounds.top+bounds.bottom)/2;
        canvas.drawText("text",centerX,centerY-offSet,paint);
    }

    /**
     * 文字居中绘制(内容动态变的情况)
     * @param centerX
     * @param centerY
     * @param content
     */
    public void drawCenterTextTwo(int centerX,int centerY, String content){
        Paint.FontMetrics fontMetrics=new Paint.FontMetrics();
        paint.getFontMetrics(fontMetrics);
        float offset=(fontMetrics.descent+fontMetrics.ascent)/2;
        canvas.drawText(content,centerX,centerY-offset,paint);
    }
}
