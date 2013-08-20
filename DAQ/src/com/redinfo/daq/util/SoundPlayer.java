package com.redinfo.daq.util;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPlayer {

	SoundPool sp = null;
	/** 存放声音资源 */
	HashMap<State, Integer> res;

	public SoundPlayer() {
		sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		res = new HashMap<State, Integer>();
	}

	public void addSound(Context context, int resId, State state) {
		int r = sp.load(context, resId, 1);
		res.put(state, r);
	}

	/** 播放资源声音 */
	public void play(State state) {
		try {
			int re = res.get(state);
			sp.play(re, 1, 1, 1, 0, 1.0f);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public enum State {//枚举类型
		/** 下拉时 */
		pull,
		/** 刷新完成 */
		refreshed;
	}

}
