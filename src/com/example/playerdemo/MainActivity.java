package com.example.playerdemo;

import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnCompletionListener {

	private final String TAG = "PlayerDemo";
	private int playerState;
	private boolean playInForeground;
	private Button startBtn, stopBtn;
	private String sourcePath;
	private MediaPlayer player;
	private AudioManager am;
	
	
	//private CallStateReceiver callStateReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startBtn = (Button) findViewById(R.id.button1);
		stopBtn = (Button) findViewById(R.id.button2);
		
		startBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(player.isPlaying()){
					pause();
					startBtn.setText("Start");
				}else{
					start();
					startBtn.setText("Pause");
				}
				Log.d(TAG, "PlayerState: " + playerState);
			}
			
		});
		
		stopBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stop();
				startBtn.setText("Start");
				Log.d(TAG, "PlayerState: " + playerState);
			}
			
		});
		
		playInForeground = false;
		playerState = 0;		
		boolean gotFocus = init();
		if(gotFocus){
			prepare();
			Log.d(TAG, "PlayerState: " + playerState);		
			//registerCallStateMonitor();
		}
		
	}
	
	private boolean init(){
		sourcePath = Environment.getExternalStorageDirectory().getPath() + "/Music/Andre_Rieu_The_Skaters_Waltz.mp3";
		player = new MediaPlayer();
		player.setOnCompletionListener(this);
		try {
			player.setDataSource(sourcePath);
		} catch (IllegalArgumentException e) {
			Log.d(TAG, "Source Exception: " + e.getMessage());
		} catch (SecurityException e) {
			Log.d(TAG, "Source Exception: " + e.getMessage());
		} catch (IllegalStateException e) {
			Log.d(TAG, "Source Exception: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Source Exception: " + e.getMessage());
		}
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		int result = am.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
		    return true;
		} else return false;
		
	}
	
	private void prepare(){
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG, "Prepare Exception: " + e.getMessage());
			player.release();
		} catch (IOException e) {
			Log.d(TAG, "Prepare Exception: " + e.getMessage());
			player.release();
		}
		playerState = 1;
	}
	
	private OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		private boolean flag = false;
	    public void onAudioFocusChange(int focusChange) {
	    	switch(focusChange){
	    	case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	    		// Pause playback
	    		Toast.makeText(MainActivity.this, "Focus loss transient", Toast.LENGTH_LONG).show();
	        	if(player != null && playerState == 2){
    				Pause();
    				flag = true;
    			}
		        Log.d(TAG, "PlayerState: " + playerState);
	    		break;
	    	case AudioManager.AUDIOFOCUS_LOSS:
	    		// Stop playback: focus taken by other music services
	    		Toast.makeText(MainActivity.this, "Focus loss permanent", Toast.LENGTH_LONG).show();
	    		am.abandonAudioFocus(afChangeListener);
	    		if(player != null && playerState == 2){
    				stop();
    			}
	    		player.release();  
	        	break;
	    	case AudioManager.AUDIOFOCUS_GAIN:
	    		// Resume playback 
	        	Toast.makeText(MainActivity.this, "Focus gain", Toast.LENGTH_LONG).show();
	        	if(player != null && flag && playerState == 4){
    				start();
    				flag = false;
    			}
    			Log.d(TAG, "PlayerState: " + playerState);
	    		break;
	    	}
	        
	    }
	};
	
	private void start(){
		if(player == null) return;
		player.start();
		playerState = 2;
		
	}
	
	// by user
	private void pause(){
		if(player == null) return;
		player.pause();		
		playerState = 3;
	}
	
	// by lifecycle
	private void Pause(){
		if(player == null) return;
		player.pause();	
		playerState = 4;
	}
	
	private void stop(){
		player.stop();
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG, "Prepare Exception: " + e.getMessage());
			player.release();
		} catch (IOException e) {
			Log.d(TAG, "Prepare Exception: " + e.getMessage());
			player.release();
		}
		player.seekTo(0);
		playerState = 1;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mp.stop();
		try {
			mp.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG, "Prepare Exception: " + e.getMessage());
			mp.release();
		} catch (IOException e) {
			Log.d(TAG, "Prepare Exception: " + e.getMessage());
			mp.release();
		}
		mp.seekTo(0);
		startBtn.setText("Start");
		playerState = 1;
	}

	@Override
	public void onResume(){
		super.onResume();
		if(player != null){
			if(playInForeground && playerState == 4){
				start();
			}
		}		
		Log.d(TAG, "PlayerState: " + playerState);
	}
	
	@Override
	public void onPause(){
		if(player != null) {
			if(playInForeground && player.isPlaying()){
				Pause();
			}
		}		
		Log.d(TAG, "PlayerState: " + playerState);
		super.onPause();
	}
	
	@Override
	public void onStop(){
		if(player != null) {
			if(playInForeground && player.isPlaying()){
				Pause();
			}
		}	
		Log.d(TAG, "PlayerState: " + playerState);
		super.onStop();
	}
	
	@Override
	public void onDestroy(){
		//unregisterCallStateMonitor();
		// Abandon audio focus when playback complete    
		am.abandonAudioFocus(afChangeListener);
		player.release();
		super.onDestroy();
	}
	
	
/*	private class CallStateReceiver extends BroadcastReceiver {
		private boolean flag; // flag for player in state 2 interrupted
		private int status; // 1:incoming, 2:incoming answered, 3:outgoing, 0:undetermined 
	    public CallStateReceiver() {
	    	flag = false;
	    	status = 0;
	    }

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	
	    	if(TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)){
	    		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
	    		
	    		if(TelephonyManager.EXTRA_STATE_RINGING.equals(state)){
	    			// Incoming call rings
	    			Toast.makeText(context, "Incoming Ringing", Toast.LENGTH_LONG).show();
	    			status = 1;
	    			if(player != null && playerState == 2){
	    				Pause();
	    				flag = true;
	    			}
	    			Log.d(TAG, "PlayerState: " + playerState);
	    		}else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)){
	    			// Call off-hook
	    			if(status == 1){
	    				Toast.makeText(context, "Incoming Answered", Toast.LENGTH_LONG).show();
	    				status = 2;
	    			}else if(status == 0){
	    				// Outgoing call dialed
	    				Toast.makeText(context, "Outgoing Dialed", Toast.LENGTH_LONG).show();
	    				status = 3;
	    				if(player != null && playerState == 2){
	        				Pause();
	        				flag = true;
	        			}
	    		        Log.d(TAG, "PlayerState: " + playerState);
	    			}
	    		}else if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)){
	    			// Incomming/Outgoing Call ends
	    			//Toast.makeText(context, state, Toast.LENGTH_LONG).show();
	    			if(status == 1){
	    				// Incoming Missed or Rejected
	    				Toast.makeText(context, "Incoming Missed or Rejected", Toast.LENGTH_LONG).show();
	    			}else if(status == 2){
	    				// Incoming Answer Ended
	    				Toast.makeText(context, "Incoming Answer Ended", Toast.LENGTH_LONG).show();
	    			}else if(status == 3){
	    				// Outgoing Ended
	    				Toast.makeText(context, "Outgoing Ended", Toast.LENGTH_LONG).show();
	    			}
	    			if(player != null && flag && playerState == 4){
	    				start();
	    				flag = false;
	    			}
	    			status = 0;
	    			Log.d(TAG, "PlayerState: " + playerState);
	    		}
	    	}else if(Intent.ACTION_NEW_OUTGOING_CALL.equals(action)){	
	    		// Outgoing call dialed
		        Toast.makeText(context, "Outgoing Dialed", Toast.LENGTH_LONG).show();
    			status = 3;
		        if(player != null && playerState == 2){
    				Pause();
    				flag = true;
    			}
		        Log.d(TAG, "PlayerState: " + playerState);
	    	}
	        
	    }
  
	}	
	
	private void registerCallStateMonitor(){
		callStateReceiver = new CallStateReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		this.registerReceiver(callStateReceiver, intentFilter);
	}
		
	private void unregisterCallStateMonitor(){
		try{
			this.unregisterReceiver(callStateReceiver);
		}catch (IllegalArgumentException e){
			Toast.makeText(this, "Focus ungot.", Toast.LENGTH_LONG).show();
		}
		
	}*/

}
