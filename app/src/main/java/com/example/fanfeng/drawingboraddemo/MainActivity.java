package com.example.fanfeng.drawingboraddemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private android.widget.Button btnSync;
    private android.widget.Button btnPen;
    private android.widget.Button btnEraser;
    private DrawingBoradView dvCanvas;
    private DrawingBoradView dvCanvasPreview;
    private Button btnClear;
    private android.widget.ImageView ivRed;
    private android.widget.ImageView ivGreen;
    private android.widget.ImageView ivBlue;
    private android.widget.ImageView ivBlack;

    private boolean isSync;
    private List<String> cachePathList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.ivBlack = (ImageView) findViewById(R.id.ivBlack);
        this.ivBlue = (ImageView) findViewById(R.id.ivBlue);
        this.ivGreen = (ImageView) findViewById(R.id.ivGreen);
        this.ivRed = (ImageView) findViewById(R.id.ivRed);
        this.btnClear = (Button) findViewById(R.id.btnClear);
        this.dvCanvas = (DrawingBoradView) findViewById(R.id.dvCanvas);

        //预览窗口
        this.dvCanvasPreview = (DrawingBoradView) findViewById(R.id.dvCanvasPreview);
        this.btnEraser = (Button) findViewById(R.id.btnEraser);
        this.btnPen = (Button) findViewById(R.id.btnPen);
        this.btnSync = (Button) findViewById(R.id.btnSync);

        btnPen.setOnClickListener( v -> dvCanvas.setDrawMode(DrawingBoradView.DRAW_PEN));
        btnEraser.setOnClickListener( v -> dvCanvas.setDrawMode(DrawingBoradView.DRAW_ERASER));
        btnClear.setOnClickListener( v -> dvCanvas.clearCanvas());
        btnSync.setOnClickListener( v -> {

            isSync = !isSync;

            btnSync.setText(isSync ? "关闭同步" : "开启同步" );

        });

        ivRed.setOnClickListener( v -> dvCanvas.setPenColor(Color.parseColor("#FF4444")));
        ivGreen.setOnClickListener( v -> dvCanvas.setPenColor(Color.parseColor("#99cc33")));
        ivBlue.setOnClickListener( v -> dvCanvas.setPenColor(Color.parseColor("#3388FF")));
        ivBlack.setOnClickListener( v -> dvCanvas.setPenColor(Color.parseColor("#000000")));

        //设置绘画监听
        dvCanvas.setOnDrawListener(new DrawingBoradView.OnDrawListener() {
            @Override
            public void onDrawStart() {

            }

            @Override
            public void onDrawing() {
                
            }

            @Override
            public void onDrawEnd(String drawPath, boolean isClear) {
                toSync(drawPath, isClear);
            }
        });

        //禁止预览绘制
        dvCanvasPreview.setPathMode(DrawingBoradView.PATH_LOCK);

    }

    private void toSync(String drawPath, boolean isClear){

        //开启了同步
        if(isSync){

            //如果缓存集合有数据，则推给预览
            if(cachePathList != null && cachePathList.size() > 0){
                
                cachePathList.add(drawPath);
                dvCanvasPreview.setPathFromList(cachePathList);
                cachePathList.clear();

            }else{  //如果暂存集合没有数据 则正常显示给预览
                dvCanvasPreview.setPathFromJson(drawPath);
            }
        }else{  //关闭了同步，把新path加入到暂存集合

            if(cachePathList == null){
                cachePathList = new ArrayList<>();
            }
            //如果当前path为清空画布，则把暂存集合清空（没必要存清空之前的数据）
            if(isClear){
                cachePathList.clear();
            }
            cachePathList.add(drawPath);
        }

        if(cachePathList != null){
            Log.e("xxx","暂存list大小：" + cachePathList.size());
        }

    }

}
