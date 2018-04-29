package deaddevs.com.studentcompanion.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import deaddevs.com.studentcompanion.CoreActivity;
public class MusicCompletionReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String musicName=intent.getStringExtra(MusicService.MUSICNAME);
		core.updateName(musicName);
	}

	CoreActivity core;
	public MusicCompletionReceiver(CoreActivity core) {
		this.core=core;
	}
}