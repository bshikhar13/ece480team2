package com.iDocent;

import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class VolumeBar extends SeekBar {

	private TextToSpeech tts;
	AudioManager am;

	public VolumeBar(iDocent iDocent, TextToSpeech tts, AudioManager am) {
		super(iDocent);
		this.tts = tts;
		this.am = am;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
            double curVol = getProgress();
            double maxVol = getMax();
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 
           		 (int) (curVol/maxVol*am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
			tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
			tts.speak("i docent", TextToSpeech.QUEUE_FLUSH, null);
		}
		return super.onTouchEvent(event);
	}
}
