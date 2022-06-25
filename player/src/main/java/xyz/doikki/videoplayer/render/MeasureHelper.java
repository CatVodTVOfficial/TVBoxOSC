package xyz.doikki.videoplayer.render;

import android.view.View;

import xyz.doikki.videoplayer.player.VideoView;

public class MeasureHelper {

    private int mVideoWidth;

    private int mVideoHeight;

    private int mCurrentScreenScale;

    private int mVideoRotationDegree;

    public void setVideoRotation(int videoRotationDegree) {
        mVideoRotationDegree = videoRotationDegree;
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    public void setScreenScale(int screenScale) {
        mCurrentScreenScale = screenScale;
    }

    /**
     * 注意：VideoView的宽高一定要定死，否者以下算法不成立
     */
    public int[] doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) { // 软解码时处理旋转信息，交换宽高
            widthMeasureSpec = widthMeasureSpec + heightMeasureSpec;
            heightMeasureSpec = widthMeasureSpec - heightMeasureSpec;
            widthMeasureSpec = widthMeasureSpec - heightMeasureSpec;
        }

        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        if (mVideoHeight == 0 || mVideoWidth == 0) {
            return new int[]{width, height};
        }

        //如果设置了比例
        switch (mCurrentScreenScale) {
            case VideoView.SCREEN_SCALE_DEFAULT:
            default:
                if (mVideoWidth * height < width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    height = width * mVideoHeight / mVideoWidth;
                }
                break;
            case VideoView.SCREEN_SCALE_ORIGINAL:
                width = mVideoWidth;
                height = mVideoHeight;
                break;
            case VideoView.SCREEN_SCALE_16_9:
                if (height > width / 16 * 9) {
                    height = width / 16 * 9;
                } else {
                    width = height / 9 * 16;
                }
                break;
            case VideoView.SCREEN_SCALE_4_3:
                if (height > width / 4 * 3) {
                    height = width / 4 * 3;
                } else {
                    width = height / 3 * 4;
                }
                break;
            case VideoView.SCREEN_SCALE_MATCH_PARENT:
                width = widthMeasureSpec;
                height = heightMeasureSpec;
                break;
            case VideoView.SCREEN_SCALE_CENTER_CROP:
                if (mVideoWidth * height > width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight;
                } else {
                    height = width * mVideoHeight / mVideoWidth;
                }
                break;
        }
        return new int[]{width, height};
    }
}
