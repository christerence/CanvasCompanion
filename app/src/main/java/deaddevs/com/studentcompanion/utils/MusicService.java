package deaddevs.com.studentcompanion.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class MusicService extends Service {
	public class MyBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}
	private final IBinder iBinder=new MyBinder();
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return iBinder;
	}
	MusicPlayer musicPlayer;
	@Override
	public void onCreate() { super.onCreate(); musicPlayer = new MusicPlayer(this); }
	public void startMusic() { musicPlayer.playMusic(); }
	public void pauseMusic() { musicPlayer.pauseMusic(); }
	public void resumeMusic() { musicPlayer.resumeMusic(); }
	public int getPlayingStatus() { return musicPlayer.getMusicStatus(); }
	public String getMusicName() { return musicPlayer.getMusicName(); }

	public static final String COMPLETE_INTENT = "complete intent";
	public static final String MUSICNAME = "music name";

	public void onUpdateMusicName(String musicname) {
		Intent intent = new Intent(COMPLETE_INTENT);
		intent.putExtra(MUSICNAME, musicname);
		sendBroadcast(intent);
	}

}