package com.example.fanfeng.drawingboraddemo;

import java.util.List;

/**
 * path bean
 */

public class PathBean {

    public List<PointBean> points;  //坐标（index = 0  为起始点  index > 0 为移动点 最后一位为结束）
    public String color;            //画笔颜色 格式 -> #ff000000
    public float width;             //画笔/橡皮粗细
    public int mode;                //模式 0：画笔  1 橡皮
    public float canvasWidth;       //原始画布宽
    public float canvasHeight;      //原始画布高
    public boolean isClear = false; //是否为清屏

}
