package org.fresheed.theremin;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;

public class SoundTest extends Activity {
	public static final String tag="SOUND";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_test);
//		createAndPlay();
		generateTone(150, 1000);
	}
	
	void createAndPlay(){
		final int SAMPLE_RATE=44100;
		final int DURATION=2;
		final int AMOUNT_OF_POINTS=SAMPLE_RATE*DURATION;
		final int FREQUENCY=440;

		double[] calcs=new double[AMOUNT_OF_POINTS];
		final double pi=Math.PI;
		for (int i=0; i<AMOUNT_OF_POINTS; i++){
			calcs[i]=Math.sin(2*pi* (i/(SAMPLE_RATE/(double)FREQUENCY)));
		}
		
		// convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
		final byte generatedSnd[] = new byte[2 * AMOUNT_OF_POINTS];
        int idx = 0;
        for (final double dVal : calcs) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 0x7FFF));
            if (idx==1000) Log.d(tag, "1000::: "+Integer.toBinaryString(val));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >> 8);
//            generatedSnd[idx++]=(byte)val;
//            generatedSnd[idx++]=(byte)val;
        }
        Log.d(tag, "1000, 1001: "+Integer.toBinaryString(generatedSnd[1000])+" "+Integer.toBinaryString(generatedSnd[1001]));
        
        Log.d(tag, "SOUND finished generation");
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//        		SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
        		SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO,
//                AudioFormat.ENCODING_PCM_16BIT, AMOUNT_OF_POINTS,
        		AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length*Byte.SIZE/8,
                AudioTrack.MODE_STATIC);
        int result=audioTrack.write(generatedSnd, 0, generatedSnd.length);
        if (result==AudioTrack.ERROR_INVALID_OPERATION || result==AudioTrack.ERROR_BAD_VALUE || result==AudioManager.ERROR_DEAD_OBJECT) {
        	Log.d(tag, "SOUND error value");
        } else {
        	Log.d(tag, "SOUND success: "+result);
        }   
        audioTrack.play();
        Log.d(tag, "SOUND Play");
	}
	
	private void generateTone(double freqHz, int durationMs){
//		int count = (int)(44100.0 * 2.0 * (durationMs / 1000.0));
//		short[] samples = new short[count];
//		for(int i = 0; i < count; i += 2){
//			short sample = (short)(Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF);
//			samples[i + 0] = sample;
//			samples[i + 1] = sample;
//		}
//		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
//			AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
//			count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
//		track.write(samples, 0, count);
//		track.play();
		int count = (int)(44100.0 * (durationMs / 1000.0));
		short[] samples = new short[count];
		for(int i = 0; i < count; i ++){
			short sample = (short)(Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF);
			samples[i + 0] = sample;
		}
		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
			AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
			count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
		track.write(samples, 0, count);
//		long b=System.currentTimeMillis();
		track.play();
//		long e=System.currentTimeMillis();
//		Log.d(tag, "time: "+(e-b));
	}

}
