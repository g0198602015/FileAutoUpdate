/*
 * @Title DownloadService.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description��
 * @author Yann
 * @date 2015-8-7 ����10:03:42
 * @version 1.0
 */
package com.example.fileautoupdate.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.fileautoupdate.AutoStartReceiver;
import com.example.fileautoupdate.entities.FileInfo;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView.FindListener;

public class DownloadService extends Service
{
	public static String DOWNLOAD_PATH = 
			Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/downloads/";
	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final String ACTION_FINISHED = "ACTION_FINISHED";
	public static final int MSG_INIT = 0;
	private Map<Integer, DownloadTask> mTasks = 
			new LinkedHashMap<Integer, DownloadTask>();
	
	/**
	 * @see Service#onStartCommand(Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (ACTION_START.equals(intent.getAction()))
		{
			try
			{
				List<FileInfo> fileInfoList = (List<FileInfo>) intent.getSerializableExtra("fileInfo");
				for (int index = 0 ;index < fileInfoList.size() ;index++)
				{
					FileInfo fileInfo = fileInfoList.get(index);
					Log.i(AutoStartReceiver.TAG , "Start:" + fileInfo.toString());
					new InitThread(fileInfo).start();
				}
			}
			catch(Exception ex)
			{
				FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
				Log.i(AutoStartReceiver.TAG , "Start:" + fileInfo.toString());
				new InitThread(fileInfo).start();		
			}
		}
		else if (ACTION_STOP.equals(intent.getAction()))
		{
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.i(AutoStartReceiver.TAG , "Stop:" + fileInfo.toString());

			DownloadTask task = mTasks.get(fileInfo.getId());
			if (task != null)
			{
				task.isPause = true;
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.what)
			{
				case MSG_INIT:
					FileInfo fileInfo = (FileInfo) msg.obj;
					Log.i(AutoStartReceiver.TAG, "Init:" + fileInfo);
					DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 1);
					task.downLoad();
					mTasks.put(fileInfo.getId(), task);
					break;

				default:
					break;
			}
		};
	};
	
	private class InitThread extends Thread
	{
		private FileInfo mFileInfo = null;
		
		public InitThread(FileInfo mFileInfo)
		{
			this.mFileInfo = mFileInfo;
		}
		
		/**
		 * @see Thread#run()
		 */
		@Override
		public void run()
		{
			HttpURLConnection connection = null;
			RandomAccessFile raf = null;
			
			try
			{
				URL url = new URL(mFileInfo.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				int length = -1;
				int responseCode = connection.getResponseCode();
				if (responseCode == 200)// HttpStatus.SC_OK
				{
					length = connection.getContentLength();
				}
				
				if (length < 0)
				{
					Log.i(AutoStartReceiver.TAG, "(DownloadService )Error: the "+mFileInfo.getFileName()+" http response code is "+ responseCode);
					return;
				}
				
				File dir = new File(DOWNLOAD_PATH);
				if (!dir.exists())
				{
					dir.mkdir();
				}

				File file = new File(dir, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.setLength(length);
				mFileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (connection != null)
				{
					connection.disconnect();
				}
				if (raf != null)
				{
					try
					{
						raf.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * @see Service#onBind(Intent)
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

}
