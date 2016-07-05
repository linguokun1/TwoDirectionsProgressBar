package com.lgk.twodirectionprogressbar.customedview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;

import com.lgk.twodirectionprogressbar.R;


/**
 * @author linguokun
 * @packageName com.example.linguokun.myapplication.customedview
 * @description
 * @date 16/2/18
 */
/**双向进度条*/
public class TwoDirectionsProgressBar extends ProgressBar {
    /**进度条左边前景色*/
    private static final int DEFAULT_COLOR_LEFT_REACHED_COLOR = 0XFF49b5e1;//深蓝色
    private static final int DEFAULT_COLOR_LEFT2_REACHED_COLOR = 0XFF6cd7f0;//浅蓝色
    /**进度条右边前景色*/
    private static final int DEFAULT_COLOR_RIGHT_REACHED_COLOR = 0XFFfc501e;//红色
    private static final int DEFAULT_COLOR_RIGHT2_REACHED_COLOR = 0XFFfe8840;//橙色
    /**进度条背景色*/
    private static final int DEFAULT_COLOR_UNREACHED_COLOR = 0XFFf5f5f5;
    /**进度条前景高度*/
    private static final int DEFAULT_HEIGHT_REACHED_PROGRESS_BAR = 6;
    /**进度条背景高度*/
    private static final int DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR = 6;
    /**右边进度条切割路径 圆角的半径，依次为左上角xy半径，右上角，右下角，左下角*/
    private float[] right_rids = {0.0f, 0.0f, 10.0f, 10.0f, 10.0f, 10.0f, 0.0f, 0.0f};
    /**左边进度条切割路径 圆角的半径，依次为左上角xy半径，右上角，右下角，左下角*/
    private float[] left_rids = {10.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10.0f, 10.0f};

    /**画笔对象*/
    protected Paint mPaint = new Paint();

    /**进度条前景高度*/
    protected int mReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_REACHED_PROGRESS_BAR);
    /**进度左边条前景色*/
    protected int mReachedLeftBarColor = DEFAULT_COLOR_LEFT_REACHED_COLOR;
    /**进度右边条前景色*/
    protected int mReachedRightBarColor = DEFAULT_COLOR_RIGHT_REACHED_COLOR;
    /**进度条背景色*/
    protected int mUnReachedBarColor = DEFAULT_COLOR_UNREACHED_COLOR;
    /**进度条背景高度*/
    protected int mUnReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR);
    /**本控件除去内边距后进度条的实际宽度*/
    protected int mRealWidth;

    /**本控件除去内边距后进度条的实际宽度*/
    protected int mProgressLeft;
    private RectF mRectF;
    private RectF mRectF2;
    private LinearGradient mLgRight;
    private LinearGradient mLgLeft;

    public TwoDirectionsProgressBar(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public TwoDirectionsProgressBar(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        setHorizontalScrollBarEnabled(true);
        obtainStyledAttributes(attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//设置软件绘制
        mRectF = new RectF();
        mRectF2 = new RectF();
        //渐变色
        mLgRight = new LinearGradient(0, -6, 0, 6, new int[] {DEFAULT_COLOR_RIGHT2_REACHED_COLOR, DEFAULT_COLOR_RIGHT_REACHED_COLOR }, null, Shader.TileMode.CLAMP);
        mLgLeft = new LinearGradient(0, -6, 0, 6, new int[] {DEFAULT_COLOR_LEFT2_REACHED_COLOR, DEFAULT_COLOR_LEFT_REACHED_COLOR }, null, Shader.TileMode.CLAMP);
    }

    /**
     * 获取并初始化各种自定义属性值
     * @param attrs xml文件中定义的属性
     */
    private void obtainStyledAttributes(AttributeSet attrs){
        final TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.TwoDirectionsProgressBar);
        mReachedRightBarColor = attributes.getColor(R.styleable.TwoDirectionsProgressBar_progress_right_reached_color, mReachedRightBarColor);
        mReachedLeftBarColor = attributes.getColor(R.styleable.TwoDirectionsProgressBar_progress_left_reached_color, mReachedLeftBarColor);
        mUnReachedBarColor = attributes.getColor(R.styleable.TwoDirectionsProgressBar_progress_unreached_color, DEFAULT_COLOR_UNREACHED_COLOR);
        mReachedProgressBarHeight = (int) attributes.getDimension(R.styleable.TwoDirectionsProgressBar_progress_reached_bar_height, mReachedProgressBarHeight);
        mUnReachedProgressBarHeight = (int) attributes.getDimension(R.styleable.TwoDirectionsProgressBar_progress_unreached_bar_height, mUnReachedProgressBarHeight);
        mProgressLeft = (int) attributes.getFloat(R.styleable.TwoDirectionsProgressBar_progress_left, 0.0f);
        attributes.recycle();
    }

    /**这里做的就是当控件的不是wrap或者match的话,那么就按我期待的结果去测量高度*/
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            //如果高度没有设定具体的值 就按下面来设定控件的高度
        if (heightMode != MeasureSpec.EXACTLY){
            //我期望测量出来的高度 = 顶部内边距+底部内边距+文字与进度条之间的较大值
            int expectHeight = getPaddingTop() + getPaddingBottom() + Math.max(mReachedProgressBarHeight, mUnReachedProgressBarHeight);
            //按我期望测量出来的值去测量这个控件 测量模式只要设置为精确, 那么最终的结果当然也是我期待的
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(expectHeight,MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**在这个方法中获取本控件进度条的实际宽度*/
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mRealWidth = w - getPaddingRight() - getPaddingLeft();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas){
        canvas.save();
        //画笔平移到指定paddingLeft， getHeight() / 2位置，注意以后坐标都为以此为0，0
        canvas.translate(getPaddingLeft(), getHeight() / 2);
        boolean noNeedBg = false;
        //当前进度和总值的比例
        float radio = getProgress() * 1.0f / getMax();
        //已到达的宽度
        float progressPosX = (int) (mRealWidth/2 * radio +mRealWidth/2);

        //如果到达最后，则未到达的进度条不需要绘制
        if (progressPosX > mRealWidth){
            progressPosX = mRealWidth;
            noNeedBg = true;
        }
        // 绘制未到达的进度条, 即灰色背景进度
        if (!noNeedBg){
            float start = 0;
            mPaint.setColor(mUnReachedBarColor);
            mPaint.setStrokeWidth(mUnReachedProgressBarHeight);
//            mRectF = new RectF(start, dp2px(-3), mRealWidth, dp2px(3));
            mRectF.left = start;
            mRectF.top = dp2px(-3);
            mRectF.right = mRealWidth;
            mRectF.bottom = dp2px(3);
            canvas.drawRoundRect(mRectF, 10, 10, mPaint);
        }
        //右边进度条的到达位置
        float rightX = progressPosX;
        //左边进度条的到达位置
        if(mProgressLeft>100){
            mProgressLeft = 100;
        }
        //左边进度条的终点x坐标
        float leftX = mRealWidth / 2 - ((mProgressLeft* 1.0f / getMax())*mRealWidth / 2);
        if(leftX==mRealWidth){
            leftX = 0;
        }
        //绘制下面圆角进度条也可以通过RoundRectShape实现
        /**绘制右边进度条*/
        if (rightX >= 0){
            mPaint.setShader(mLgRight);
            mPaint.setColor(mReachedRightBarColor);
            mPaint.setStrokeWidth(mReachedProgressBarHeight);
            //创建一个将要给右边进度条填充的矩形范围
            mRectF.left = mRealWidth/2;
            mRectF.top = dp2px(-3);
            mRectF.right = rightX;
            mRectF.bottom = dp2px(3);
            //右边进度条进度不等于0时(也就是rightX不在中间位置时)才切割右上角和右下角
            if(rightX!=mRealWidth/2) {
                //rectF2是切割的范围
                Path path = getClipPath(rightX, leftX, right_rids);
                canvas.clipPath(path);//切割画布
            }
            canvas.drawRoundRect(mRectF, 0, 0, mPaint);
        }

        /**绘制左边进度条*/
        if (leftX >= 0){
            mPaint.setShader(mLgLeft);
            mPaint.setColor(mReachedLeftBarColor);
            mPaint.setStrokeWidth(mReachedProgressBarHeight);
            //创建一个将要给左边进度条填充的矩形范围
//            mRectF = new RectF(leftX, dp2px(-3), mRealWidth/2, dp2px(3));
            mRectF.left = leftX;//leftX
            mRectF.top = dp2px(-3);
            mRectF.right = mRealWidth/2;
            mRectF.bottom = dp2px(3);
            Path path = getClipPath(rightX, leftX, left_rids);
            canvas.clipPath(path);
            canvas.drawRoundRect(mRectF, 0, 0, mPaint);

        }
        canvas.restore();
    }

    /**获取要剪切的画布路径*/
    @NonNull
    private Path getClipPath(float rightX, float leftX, float[] rids) {
        //创建一个将要被切割的矩形范围
        //rectF2是切割的范围

        mRectF2.left = leftX;//leftX
        mRectF2.top = dp2px(-3);
        mRectF2.right = rightX;
        mRectF2.bottom = dp2px(3);
        //给画布按指定的路径切割
        Path path = new Path();
        //向路径中添加圆角矩形。
        path.addRoundRect(mRectF2, rids, Path.Direction.CW);
        return path;
    }

    /**设置左边进度*/
    public void setLeftProgress(int progressLeft){
        this.mProgressLeft = progressLeft;
    }

    /**
     * dp 2 px
     * @param dpVal 要转换的dp值
     */
    protected int dp2px(int dpVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

}
