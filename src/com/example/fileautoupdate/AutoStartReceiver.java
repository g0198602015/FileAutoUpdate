package com.example.fileautoupdate;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.example.fileautoupdate.entities.FileInfo;
import com.example.fileautoupdate.services.DownloadService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.Xml;

public class AutoStartReceiver extends BroadcastReceiver 
{ 
	public static String TAG = "AutoStart";
	private static String TAG_VERSION = "VERSION";
	private String mServerIP = "";
	private static List<String> mFileNames = new ArrayList<String>();
	private static int mServerVersion = -1;
	private static SharedPreferences mSharePreferences = null;
	@Override
    public void onReceive(Context context, Intent arg1) 
    { 
		try
		{
			Log.i(AutoStartReceiver.TAG, "Prepare Pass Config");
			passConfig(context);
			mSharePreferences = context.getSharedPreferences("FileAutoUpdateSettings", 0);
			int currentVersion = mSharePreferences.getInt(TAG_VERSION, -1);
			
			if (mServerVersion > currentVersion)
			{
		        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
		        Log.i(AutoStartReceiver.TAG, "Prepare update ... ServeIP: "+mServerIP);
		        for (int index = 0 ; index < mFileNames.size() ; index++)
		        {
		        	String URL = mServerIP + "/" + mFileNames.get(index);
		        	String fileName = mFileNames.get(index);
			        fileInfoList.add(new FileInfo(index, URL, fileName, 0, 0));
			        Log.i(AutoStartReceiver.TAG, "Prepare update URL: "+URL);
		        }
				Intent intent = new Intent(context, DownloadService.class);
				intent.setAction(DownloadService.ACTION_START);
				intent.putExtra("fileInfo", (Serializable) fileInfoList);
				context.startService(intent);
				Log.i(AutoStartReceiver.TAG, "Prepare startup DownloadService");
			}
			else
			{
				Log.i(AutoStartReceiver.TAG, "Cancel Download .. Current Version:"+currentVersion+", Server Version:"+mServerVersion);
				Log.i(AutoStartReceiver.TAG, "Cancel Download .. the system version is the latest");
			}
		}
		catch(Exception ex)
		{
			Log.e("FileAutoUpdate", ex.toString());
	
		}
        
    }
	
	private void passConfig(Context context)
	{
		AssetManager asset = context.getAssets();
		try {
			InputStream inputStream = asset.open("config.xml");
			XmlPullParser configXML = Xml.newPullParser();
			configXML.setInput(inputStream, "utf-8");
			int eventType = configXML.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) 
			{  
				if(eventType == XmlPullParser.START_TAG) 
				{
					String tagName = configXML.getName();
					configXML.next();
					if (tagName.equalsIgnoreCase("SERVER_IP"))
					{
						mServerIP = configXML.getText();
					}
					else if (tagName.equalsIgnoreCase("SERVER_FILES"))
					{
						configXML.next();
						while(eventType != XmlPullParser.END_TAG)
						{
							if (eventType == XmlPullParser.START_TAG) 
							{
								tagName = configXML.getName();
								if (tagName.equalsIgnoreCase("FILE"))
								{
									String fileName = configXML.nextText();
									mFileNames.add(fileName);
									
								}
							}
							configXML.next();
							eventType = configXML.getEventType();
							//mServerIP = configXML.getText();
						}
					}
					else if (tagName.equalsIgnoreCase("SAVE_FOLDER"))
					{
						DownloadService.DOWNLOAD_PATH = configXML.getText();
					}
					else if (tagName.equalsIgnoreCase("LAST_VERSION"))
					{
						mServerVersion = Integer.parseInt(configXML.getText());
					}
				}
				configXML.next();
				eventType = configXML.getEventType();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	private static int mFinishDownloadCount = 0;
	public static void updateVersion(String path)
	{
		Log.i(AutoStartReceiver.TAG, "Download Finish: "+path);
		mFinishDownloadCount++;
		if (mSharePreferences != null && mFinishDownloadCount == mFileNames.size())
		{
			Log.i(AutoStartReceiver.TAG, "Total download finished and update version code");
			SharedPreferences.Editor editor = mSharePreferences.edit();
			editor.putInt(TAG_VERSION, mServerVersion);
		}
	}

} 
