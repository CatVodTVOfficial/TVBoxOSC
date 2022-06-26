package xyz.doikki.videoplayer.controller;

public interface IGestureComponent extends IControlComponent {
    /**
     * 开始滑动
     */
    void onStartSlide();

    /**
     * 结束滑动
     */
    void onStopSlide();

    /**
     * 滑动调整进度
     * @param slidePosition 滑动进度
     * @param currentPosition 当前播放进度
     * @param duration 视频总长度
     */
    void onPositionChange(int slidePosition, int currentPosition, int duration);

    /**
     * 滑动调整亮度
     * @param percent 亮度百分比
     */
    void onBrightnessChange(int percent);

    /**
     * 滑动调整音量
     * @param percent 音量百分比
     */
    void onVolumeChange(int percent);
}
