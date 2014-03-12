package com.example.playerdemo;

import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
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
	
	private CallStateReceiver callStateReceiver;
	
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
		init();
		Log.d(TAG, "PlayerState: " + playerState);
		
		registerCallStateMonitor();
		
	}
	
	private void init(){
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
		mp.release();
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
		if(player == null) {
			if(playInForeground && player.isPlaying()){
				Pause();
			}
		}		
		Log.d(TAG, "PlayerState: " + playerState);
		super.onPause();
	}
	
	@Override
	public void onStop(){
		if(player == null) {
			if(playInForeground && player.isPlaying()){
				Pause();
			}
		}	
		Log.d(TAG, "PlayerState: " + playerState);
		super.onStop();
	}
	
	@Override
	public void onDestroy(){
		unregisterCallStateMonitor();
		super.onDestroy();
	}
	
	
	private class CallStateReceiver extends BroadcastReceiver {
		private boolean flag;
	    public CallStateReceiver() {
	    	flag = false;
	    }

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	
	    	if(TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)){
	    		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
	    		Toast.makeText(context, state, Toast.LENGTH_LONG).show();
	    		if(TelephonyManager.EXTRA_STATE_RINGING.equals(state)){
	    			// Incoming call
	    			if(player != null && playerState == 2){
	    				Pause();
	    				flag = true;
	    			}
	    			Log.d(TAG, "PlayerState: " + playerState);
	    		}else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)){
	    			// Call offhook
	    		}else if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)){
	    			// Incomming/Outgoing Call ends
	    			if(player != null && flag && playerState == 4){
	    				start();
	    				flag = false;
	    			}
	    			Log.d(TAG, "PlayerState: " + playerState);
	    		}
	    	}else if(Intent.ACTION_NEW_OUTGOING_CALL.equals(action)){	
	    		// Outgoing call
		        Toast.makeText(context, "Outgoing", Toast.LENGTH_LONG).show();
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
		this.unregisterReceiver(callStateReceiver);
	}

}
