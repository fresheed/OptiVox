package org.fresheed.theremin;

import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class SoundPlayer {
	final static int DURATION_MS=200, RATE=8000;
	int buffer_size;
//	final static int freq1=440, freq2=880;
//	short[] samples1, samples2;
	final int FREQ_INTERVAL=320/7;
	float[] freqs={261.6F, 293.7F, 329.6F, 349.2F, 392, 440, 494};
	short[][] samples=new short[7][];
	ByteBuffer[] bbuffers=new ByteBuffer[7];
	AudioTrack now_playing, stream;
	Integer current_frequency;
	
	public SoundPlayer(){
		int count=(int)(RATE * (DURATION_MS / 1000F));
		Log.d("Tag", "got count "+count);
		buffer_size=count*2;
		for (int s=0; s<7; s++){
			samples[s] = new short[count];
//			bbuffers[s]=ByteBuffer.allocate(buffer_size);
			for(int i = 0; i < samples[s].length; i ++){
				short sample = (short)((Math.cos(2 * Math.PI * i / (RATE / (freqs[s])))) * Short.MAX_VALUE);
				samples[s][i] = sample;
//				bbuffers[s].put(2*i, (byte) ((sample & 0xFF00)>>2));
//				bbuffers[s].put(2*i+1, (byte) ((sample & 0xFF)>>2));
			}
		}
		
		stream= new AudioTrack(AudioManager.STREAM_MUSIC, RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffer_size,
                AudioTrack.MODE_STREAM); 
		stream.setPlaybackRate(RATE);
//		stream.setStereoVolume(1F, 1F);
		stream.play();
		
		current_frequency=100;
	}
	
	void setFrequency(int freq){
		synchronized (current_frequency) {
			current_frequency=freq;
		}
	}
	
	
	boolean is_playing=false;
	Thread play_thread=new Thread(){
		@Override
		public void run(){
			while (is_playing){
				int freq_num;
				synchronized (current_frequency) {
					freq_num=current_frequency/FREQ_INTERVAL;
				}
//				stream.pause();
//				stream.flush();
//				Log.d("tag", "before write: "+stream.getPlaybackHeadPosition());
				stream.write(samples[freq_num], 0, buffer_size/2); //1/2 of buffer
//				stream.write(bbuffers[freq_num], bbuffers[freq_num].capacity()/2, AudioTrack.WRITE_BLOCKING);
				//stream.play();
			}
		}
	};
	
	void playInBackground(){
		is_playing=true;
		play_thread.setPriority(Thread.MIN_PRIORITY);
		play_thread.setDaemon(true);
		play_thread.start();
		Log.d("tag", "thread started");
	}
	
	void stop(){
		Log.d("Tag", "sound stopped");
		is_playing=false;
		stream.stop();
	}
}
