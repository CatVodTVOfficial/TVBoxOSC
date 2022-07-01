package xyz.doikki.videoplayer.player;

import android.content.Context;

/**
 * 此接口使用方法：
 * 1.继承{@link AbstractPlayer}扩展自己的播放器。
 * 2.继承此接口并实现{@link #createPlayer(Context)}，返回步骤1中的播放器。
 * 可参照{@link AndroidMediaPlayer}和{@link AndroidMediaPlayerFactory}的实现。
 */
public abstract class PlayerFactory<P extends AbstractPlayer> {

    public abstract P createPlayer(Context context);
}
