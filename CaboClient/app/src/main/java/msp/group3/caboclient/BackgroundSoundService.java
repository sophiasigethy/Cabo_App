package msp.group3.caboclient;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class BackgroundSoundService extends Service {
    private static final String TAG = null;
    MediaPlayer player;


    public IBinder onBind(Intent arg0) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true); // Set looping
        player.setVolume(100, 100);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras().containsKey("song")) {
            player.stop();
            player.release();
            switch (intent.getExtras().getInt("song")) {
                case 1:
                    player = MediaPlayer.create(this, R.raw.music);
                    break;
                case 2:
                    player = MediaPlayer.create(this, R.raw.ingame_music);
                    break;
            }
            player.setLooping(true); // Set looping
            player.setVolume(80, 80);

        }
        player.start();
        return Service.START_STICKY;
    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public void onStop() {
        player.stop();
    }

    public void onPause() {
        player.pause();
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
    }

    @Override
    public void onLowMemory() {

    }
}