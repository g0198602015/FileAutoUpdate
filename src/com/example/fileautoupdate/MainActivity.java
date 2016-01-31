package com.example.fileautoupdate;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

	private AudioManager audioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AutoStartReceiver a = new AutoStartReceiver();
        //a.onReceive(this, null);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    }
    private boolean mIsLongPress = false;
    @Override 
    public boolean onKeyLongPress(int keyCode, KeyEvent event) 
    {
    	Log.i("Test", "onKeyLongPress");
    	mIsLongPress = true;
        return super.onKeyLongPress(keyCode, event);
    } 
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
    	if (audioManager != null)
    		audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
    	
    	if (mIsLongPress)
    		return false;
    	Log.i("Test", "onKeyDown");
    	event.startTracking();	
    		//blabla
    		
    	return true;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) 
    {
    	mIsLongPress = false;
    	Log.i("Test", "onKeyUp");
//    	if (audioManager != null)
//    		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    	return true;
    }
    
}
