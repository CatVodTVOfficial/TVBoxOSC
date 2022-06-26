package xyz.doikki.videoplayer.render;

import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;

import xyz.doikki.videoplayer.player.AbstractPlayer;

public interface IRenderView {

    /**
     * 关联AbstractPlayer
     */
    void attachToPlayer(@NonNull AbstractPlayer player);

    /**
     * 设置视频宽高
     * @param videoWidth 宽
     * @param videoHeight 高
     */
    void setVideoSize(int videoWidth, int videoHeight);

    /**
     * 设置视频旋转角度
     * @param degree 角度值
     */
    void setVideoRotation(int degree);

    /**
     * 设置screen scale type
     * @param scaleType 类型
     */
    void setScaleType(int scaleType);

    /**
     * 获取真实的RenderView
     */
    View getView();

    /**
     * 截图
     */
    Bitmap doScreenShot();

    /**
     * 释放资源
     */
    void release();

}