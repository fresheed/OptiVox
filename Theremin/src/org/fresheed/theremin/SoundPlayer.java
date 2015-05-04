package org.fresheed.theremin;

import java.util.Random;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class SoundPlayer {
	final static int DURATION_MS=100, RATE=8000;
	final static int freq1=440, freq2=880;
	AudioTrack tracks[]=new AudioTrack[2];
	short[] samples1, samples2;
	AudioTrack now_playing, stream;
	Integer current_frequency;
	
	public SoundPlayer(){
		int count1 = (int)(RATE * (DURATION_MS / 1000F));
		samples1 = new short[count1];
		for(int i = 0; i < count1; i ++){
			short sample = (short)(Math.sin(2 * Math.PI * i / (RATE / freq1)) * 0x7FFF);
			samples1[i] = sample;
		}
//		AudioTrack track1 = new AudioTrack(AudioManager.STREAM_MUSIC, RATE,
//			AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
//			count1 * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
//		track1.write(samples1, 0, count1);
//		tracks[0]=track1;
		
		int count2 = (int)(RATE * (DURATION_MS / 1000F));
		samples2 = new short[count2];
		for(int i = 0; i < count2; i ++){
			short sample = (short)(Math.sin(2 * Math.PI * i / (RATE / freq2)) * 0x7FFF);
			samples2[i] = sample;
		}
//		AudioTrack track2 = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
//			AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
//			count2 * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
//		track2.write(samples2, 0, count2);
//		tracks[1]=track2;
		
//		int count1 = (int)(44100.0 * (DURATION_MS / 1000.0));
//		short[] samples1 = new short[count1];
//		for(int i = 0; i < count1; i ++){
//			short sample = (short)(Math.sin(2 * Math.PI * i / (44100.0 / freq1)) * 0x7FFF);
//			samples1[i] = sample;
//		}
//		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
//			AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
//			count1 * (Short.SIZE / 8), AudioTrack.MODE_STREAM);
//		track.play();
		
//		stream = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
//				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
//				count1 * (Short.SIZE / 8), AudioTrack.MODE_STREAM);
//		stream.play();
		stream= new AudioTrack(AudioManager.STREAM_MUSIC, RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT, 800 /* 0.1 second buffer */,
                AudioTrack.MODE_STREAM); 
		stream.setPlaybackRate(RATE);
		stream.play();
		
		current_frequency=100;
	}
	
	void setFrequency(int freq){
		synchronized (current_frequency) {
			current_frequency=freq;
		}
	}
	
	
	void playInBackground2(){
		Thread t=new Thread(){
			@Override
			public void run(){
				while (true){
					AudioTrack temp;
					synchronized (current_frequency) {
						if (current_frequency<160) temp=tracks[0];
											  else temp=tracks[1];
					}
					Log.d("Tag", "current: "+now_playing.getPlaybackHeadPosition());
					if (now_playing.getPlaybackHeadPosition()>=22050){
						Log.d("Tag", "position:"+now_playing.getPlaybackHeadPosition());
						now_playing.stop();
						Log.d("Tag", "reset position:"+now_playing.getPlaybackHeadPosition());
						now_playing=temp;
						now_playing.play();
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}
	
	boolean is_playing=false;
	Thread play_thread=new Thread(){
		@Override
		public void run(){
//			Random r=new Random();	
//			byte[] noise=new byte[1000];
			short[] to_write;
			while (is_playing){
				synchronized (current_frequency) {
					if (current_frequency<160) to_write=samples1;
										  else to_write=samples2;
				}
//				r.nextBytes(noise);
				stream.write(to_write, 0, 800);
			}
		}
	};
	
	void playInBackground(){
		is_playing=true;
		play_thread.setPriority(Thread.MIN_PRIORITY);
		play_thread.setDaemon(true);
		play_thread.start();
	}
	
	void stop(){
		is_playing=false;
	}
}
