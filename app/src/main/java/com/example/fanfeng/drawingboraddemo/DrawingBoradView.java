package com.example.fanfeng.drawingboraddemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * 绘画板
 * Created by fan.feng on 2018/10/29.
 */

public class DrawingBoradView extends View {

    //笔
    public static final int DRAW_PEN = 0;
    //橡皮
    public static final int DRAW_ERASER = 1;

    //默认最大笔点数
    private static final int MAX_STROKE_NUMBER = 1000 * 7;

    //锁定绘图
    public static final int PATH_LOCK = 1;
    //可绘图
    public static final int PATH_UNLOCK = 2;
    //只认笔绘图
    public static final int PATH_PAN_ONLY = 3;

    //画笔大小
    private int penSize = 5;
    //橡皮擦大小
    private int eraserSize = 5;

    //上一次的触摸点x坐标
    private float preX = 0.0f;
    //上一次触摸点y坐标
    private float preY = 0.0f;

    //path路径
    private Path mBufferPath;

    //路径模式
    private int pathMode = PATH_UNLOCK;

    //画笔
    private Paint mPaint;
    //画笔颜色
    private int mPenColor = Color.BLACK;

    //编辑模式
    private int drawMode = DRAW_PEN;

    //把当前绘制的内容用mBufferCanvas画到mBufferBitmap上，然后把mBufferBitmap画到主画布上
    private Bitmap mBufferBitmap;
    private Canvas mBufferCanvas;

    private PathBean mPathBean;

    private Gson mGson;

    private OnDrawListener onDrawListener;

    //是否绘制中（主要用于绘画中按清除按钮）
    private boolean isDrawing = false;


    public DrawingBoradView(Context context) {
        this(context, null);
    }

    public DrawingBoradView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingBoradView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {

        //绘制的path路径
        mBufferPath = new Path();

        mPaint = createPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mBufferBitmap == null){
            getBufferCanvas();
        }

        //画出缓存bitmap的内容
        canvas.drawBitmap(mBufferBitmap,0f,0f,null);
    }

    private Canvas getBufferCanvas(){
        if(mBufferCanvas == null){
            mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            //canvas绘制的内容，将会在这个mBufferBitmap内
            mBufferCanvas = new Canvas(mBufferBitmap);
        }

        return mBufferCanvas;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (pathMode == PATH_LOCK) {
            return super.onTouchEvent(event);
        } else if (pathMode == PATH_PAN_ONLY
                && event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS) {
            return super.onTouchEvent(event);
        }

        getParent().requestDisallowInterceptTouchEvent(true);

        if(mPathBean == null){
            mPathBean = new PathBean();
        }

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                if(onDrawListener != null){
                    onDrawListener.onDrawStart();
                }

                isDrawing = true;

                float downX = ((int) (event.getX() * 10)) * 0.1f;
                float downY = ((int) (event.getY() * 10)) * 0.1f;

                touchDown(downX, downY);

                break;
            case MotionEvent.ACTION_MOVE:

                float moveX = ((int) (event.getX() * 10)) * 0.1f;
                float moveY = ((int) (event.getY() * 10)) * 0.1f;

                if (Math.abs(moveX - preX) > 0 || Math.abs(moveY - preY) > 0) {
                    touchMove(moveX, moveY);
                }

                break;
            case MotionEvent.ACTION_UP:
                touchUp();

                isDrawing = false;

                break;
        }

        return true;
    }

    private void touchDown(float x, float y){
        mBufferPath.reset();
        mBufferPath.moveTo(x, y);
        //记录上次触摸的坐标，注意ACTION_DOWN方法只会执行一次
        preX = x;
        preY = y;

        mPathBean = new PathBean();
        mPathBean.canvasWidth = getWidth();
        mPathBean.canvasHeight = getHeight();
        mPathBean.mode = drawMode;
        mPathBean.color = formatColorToStr(mPaint.getColor());
        mPathBean.width = mPaint.getStrokeWidth();
        mPathBean.points = new ArrayList<>();
        mPathBean.isClear = false;
        PointBean mPoint = new PointBean();
        mPoint.x = x;
        mPoint.y = y;
        mPathBean.points.add(mPoint);
    }

    private void touchMove(float x, float y){
        //绘制贝塞尔曲线,使线条圆滑
        mBufferPath.quadTo(preX, preY, x, y);

        //在缓存里面绘制
        getBufferCanvas().drawPath(mBufferPath, mPaint);
        //重新绘制，会调用onDraw方法
        invalidate();

        preX = x;
        preY = y;

        if(mPathBean != null && mPathBean.points != null){
            PointBean mPoint = new PointBean();
            mPoint.x = x;
            mPoint.y = y;
            mPathBean.points.add(mPoint);

//            //绘画中
//            if(onDrawListener != null){
//                onDrawListener.onDrawing();
//            }
        }
    }

    private void touchUp(){

        mBufferPath.reset();

        if(onDrawListener != null && mPathBean != null && mPathBean.points != null && mPathBean.points.size() > 0){
            onDrawListener.onDrawEnd(toJson(mPathBean), false);
        }
    }

    /**
     * 设置绘制模式
     * @param mode
     */
    public void setDrawMode(int mode){

        //绘制中拦截
        if(isDrawing){
            return;
        }

        drawMode = mode;
        //清除路径的内容
        mBufferPath.reset();

        switch (mode){
            case DRAW_PEN:  //笔

                mPaint.setXfermode(null);
                mPaint.setStrokeWidth(penSize);
                mPaint.setColor(mPenColor);

                break;

            case DRAW_ERASER:   //橡皮

                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                mPaint.setStrokeWidth(eraserSize);
                mPaint.setColor(Color.TRANSPARENT);

                break;
        }
    }

    /**
     * 设置路径模式
     * @param mode
     */
    public void setPathMode(int mode) {

        //绘制中拦截
        if(isDrawing){
            return;
        }

        pathMode = mode;
    }

    /**
     * 设置笔颜色
     * @param color
     */
    public void setPenColor(int color){

        //绘制中拦截
        if(isDrawing){
            return;
        }

        this.mPenColor = color;

        if(drawMode == DRAW_PEN){
            mPaint.setColor(color);
            //清除路径的内容,否则之前的路径也会变色
            mBufferPath.reset();
        }
    }

    /**
     * 清理画布
     */
    public void clearCanvas(){

        //绘制中拦截
        if(isDrawing){
            return;
        }

        clear();

        mPathBean = new PathBean();
        mPathBean.isClear = true;

        if(onDrawListener != null){
            onDrawListener.onDrawEnd(toJson(mPathBean), true);
        }
    }

    /**
     * 清空画布
     */
    private void clear() {
        
        mBufferPath.reset();

        getBufferCanvas().drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    /**
     * 设置Json格式路径
     *
     *   注意！！！json格式必须为PathBean.class！！！
     *
     * @param pathJson
     */
    public void setPathFromJson(String pathJson){
        if(!TextUtils.isEmpty(pathJson)){
            setPathFromBean(fromJson(pathJson, PathBean.class));
        }
    }

    /**
     * 设置路径
     * @param pathBean
     */
    public void setPathFromBean(PathBean pathBean){
        if(pathBean == null){
            return;
        }

        //如果是清屏
        if(pathBean.isClear){
            clear();
            return;
        }

        if(pathBean.points == null || pathBean.points.size() <= 0){
            return;
        }

        int pointSize = pathBean.points.size();

        if(pointSize <= 0){
            return;
        }

        //来源宽度和当前画布的宽比例
        float widthRate = getWidth() / pathBean.canvasWidth;
        //来源高度和当前画布的高比例
        float heightRate = getHeight() / pathBean.canvasHeight;

        //取最小的比例（为了能完全显示）
        float rate = widthRate < heightRate ? widthRate : heightRate ;

        //创建临时画笔
        Paint tempPaint = createPaint();
        //设置颜色
        tempPaint.setColor(Color.parseColor(pathBean.color));
        //设置笔的粗细
        tempPaint.setStrokeWidth(pathBean.width * rate);

        //设置模式（笔or橡皮擦）
        if(pathBean.mode == DRAW_ERASER){
            tempPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }else{
            tempPaint.setXfermode(null);
        }

        //创建路径
        Path tempPath = new Path();
        float tempStartX = 0f;
        float tempStartY = 0f;

        for (int i = 0; i < pointSize; i++) {
            PointBean bean = pathBean.points.get(i);

            if(i == 0){ //模拟按下   --> ACTION_DOWN
                tempPath.reset();

                tempStartX = bean.x * rate;
                tempStartY = bean.y * rate;

                tempPath.moveTo(tempStartX, tempStartY);

            }else if(i == pointSize - 1){   //模拟当前路径结束  --> ACTION_UP
                tempPath.reset();
                //重新绘制，会调用onDraw方法
                invalidate();

            }else{  //模拟移动  --> ACTION_MOVE

                float tempMoveX = bean.x * rate;
                float tempMoveY = bean.y * rate;

                //绘制贝塞尔曲线,使线条圆滑
                tempPath.quadTo(tempStartX, tempStartY, tempMoveX, tempMoveY);

                //在缓存里面绘制
                getBufferCanvas().drawPath(tempPath, tempPaint);

                tempStartX = tempMoveX;
                tempStartY = tempMoveY;
            }
        }
    }

    public void setPathFromList(List<String> pathList){

        if(pathList == null){
            return;
        }

        int listSize = pathList.size();

        if(listSize <= 0){
            return;
        }

        for (int i = 0; i < listSize; i++) {
            setPathFromJson(pathList.get(i));
        }
    }

    /**
     * 创建一个画笔
     * @return
     */
    private Paint createPaint(){
        Paint mTempPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        //抖动
        mTempPaint.setDither(true);
        mTempPaint.setStyle(Paint.Style.STROKE);
        //新加入的笔触为圆
        mTempPaint.setStrokeJoin(Paint.Join.ROUND);
        //颜色
        mTempPaint.setColor(mPenColor);
        //笔触为圆形
        mTempPaint.setStrokeCap(Paint.Cap.ROUND);
        //画笔大小
        mTempPaint.setStrokeWidth(penSize);
        //抗锯齿
        mTempPaint.setAntiAlias(true);

        return mTempPaint;
    }

    /**
     * 转换颜色值为十六进制
     * @param color
     * @return
     */
    private String formatColorToStr(int color) {
        if (color == Color.TRANSPARENT) {
            return "#00000000";
        }
        String str = "#" + Integer.toHexString(color);
        return str;
    }

    /**
     * 转换为json
     * @param obj
     * @return
     */
    private String toJson(Object obj){
        if(mGson == null){
            mGson = new Gson();
        }

        return mGson.toJson(obj);
    }

    /**
     * 解析json
     * @param json
     * @param classOfT
     * @param <T>
     * @return
     */
    private <T> T fromJson(String json, Class<T> classOfT){
        if(mGson == null){
            mGson = new Gson();
        }

        return mGson.fromJson(json, classOfT);
    }

    /**
     * 设置绘画监听
     * @param onDrawListener
     */
    public void setOnDrawListener(OnDrawListener onDrawListener){
        this.onDrawListener = onDrawListener;
    }

    public interface OnDrawListener{
        /**
         * 绘画开始
         */
        void onDrawStart();

        /**
         * 绘画中
         */
        void onDrawing();
        /**
         * 绘画结束
         * @param drawPath
         * @param drawPath 是否清空画布
         */
        void onDrawEnd(String drawPath, boolean isClear);
    }

}
