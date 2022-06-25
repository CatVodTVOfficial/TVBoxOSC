package xyz.doikki.videoplayer.exo;

import android.content.Context;

import xyz.doikki.videoplayer.player.PlayerFactory;

public class ExoMediaPlayerFactory extends PlayerFactory<ExoMediaPlayer> {

    public static ExoMediaPlayerFactory create() {
        return new ExoMediaPlayerFactory();
    }

    @Override
    public ExoMediaPlayer createPlayer(Context context) {
        return new ExoMediaPlayer(context);
    }
}
