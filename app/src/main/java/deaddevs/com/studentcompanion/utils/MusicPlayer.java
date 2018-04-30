package deaddevs.com.studentcompanion.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;

import deaddevs.com.studentcompanion.R;

public class MusicPlayer implements MediaPlayer.OnCompletionListener {
	static final String[] MUSICNAME = new String[]{
			"Shiloh",
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
			player = MediaPlayer.create(musicService, R.raw.shiloh);
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