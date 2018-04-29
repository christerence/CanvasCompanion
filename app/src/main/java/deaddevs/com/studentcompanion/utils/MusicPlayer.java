package deaddevs.com.studentcompanion.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;

public class MusicPlayer implements MediaPlayer.OnCompletionListener {
	static final String[] MUSICPATH = new String[] {
			"http://people.cs.vt.edu/~shuoniu/mario.mp3",
			"http://people.cs.vt.edu/~shuoniu/tetris.mp3"
	};
	static final String[] MUSICNAME = new String[]{
			"Super Mario",
			"Tetris"
	};

	private MusicService musicService;

	public MusicPlayer(MusicService musicService) {
		this.musicService = musicService;
	}

	MediaPlayer player;
	int currentPosition=0;
	int musicIndex=0;
	private int musicStatus=0;

	public String getMusicName(){
		return MUSICNAME[musicIndex];
	}

	public void playMusic() {
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			player.setDataSource(MUSICPATH[musicIndex]);
			player.prepare();
			player.setOnCompletionListener(this);
			player.start();
			musicService.onUpdateMusicName(getMusicName());
		} catch (Exception ex) {
		}
		musicStatus = 1;
	}

	public void pauseMusic() {
		if (player != null && player.isPlaying()) {
			player.pause();
			currentPosition = player.getCurrentPosition();
			musicStatus = 2;
		}
	}
	public void resumeMusic() {
		if (player != null) {
			player.seekTo(currentPosition);
			player.start();
			musicStatus = 1;
		}
	}

	public int getMusicStatus() {
		return musicStatus;
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		musicIndex = (musicIndex + 1) % MUSICNAME.length;
		player.release();
		player = null;
		playMusic();
	}
}