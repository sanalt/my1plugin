/***************************************************************************************************
 
 The technology detailed in this software is the subject of various pending and issued patents,
 both internationally and in the United States, including one or more of the following patents:
 
 5,636,292 C1; 5,710,834; 5,832,119 C1; 6,286,036; 6,311,214; 6,353,672; 6,381,341; 6,400,827;
 6,516,079; 6,580,808; 6,614,914; 6,647,128; 6,681,029; 6,700,990; 6,704,869; 6,813,366;
 6,879,701; 6,988,202; 7,003,132; 7,013,021; 7,054,465; 7,068,811; 7,068,812; 7,072,487;
 7,116,781; 7,158,654; 7,280,672; 7,349,552; 7,369,678; 7,461,136; 7,564,992; 7,567,686;
 7,590,259; 7,657,057; 7,672,477; 7,720,249; 7,751,588; and EP 1137251 B1; EP 0824821 B1;
 and JP-3949679, all owned by Digimarc Corporation.
 
 Use of such technology requires a license from Digimarc Corporation, USA.  Receipt of this software
 conveys no license under the foregoing patents, nor under any of Digimarc’s other patent, trademark,
 or copyright rights.
 
 This software comprises CONFIDENTIAL INFORMATION, including TRADE SECRETS, of Digimarc Corporation,
 USA, and is protected by a license agreement and/or non-disclosure agreement with Digimarc.  It is
 important that this software be used, copied and/or disclosed only in accordance with such
 agreements.
 
 © Copyright, Digimarc Corporation, USA.  All Rights Reserved.
 
***************************************************************************************************/package com.digimarc.DMSUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.digimarc.DMSUtils.DMSConstants;;

public class DMSDebugLog
{
	public static Context _appContext = null;
	public static String _appName = "unknown";
	private static DMSDebugLog _instance = null;
	private File _file = null;
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat _dateFormat = new SimpleDateFormat(
		"MM/dd/yyyy HH:mm:ss");
	private ReentrantLock _lock = new ReentrantLock();

	private DMSDebugLog()
	{
		Init();
	}

	public static
	DMSDebugLog
		getInstance()
	{
		if (_instance == null)
		{
			_instance = new DMSDebugLog();
			_instance.Init();
		}
		return (_instance);
	}

	public
		boolean
		FileCreated()
	{
		return (_file != null);
	}

	private
		void
		Init()
	{
		try
		{
			_lock.lock();
			if (FileCreated() == false)
			{
				CreateFile();
				AppendDivider();
				if((_appName == null) || (_appName.isEmpty() == true))
				{
					_appName = "unknown";
				}
				Append("Digimarc " + _appName + " app for Android\r\n");
				Append("Build date/time = " + DMSUtils.ComputeBuildDateAndTimeString(_appContext));
				Append("Manufacturer = \"" + Build.MANUFACTURER + "\"\r\n");
				Append("API release = " + Build.VERSION.RELEASE + "\r\n");
				Append("API level = " + Build.VERSION.SDK_INT + "\r\n");
				Append("OS version = " + System.getProperty("os.version") + "\r\n");
				AppendMemoryState();
				AppendCPUInfo();
				Append("Display = \"" + Build.DISPLAY + "\"\r\n");
				Append("Root directory = \"" + Environment.getRootDirectory().getAbsolutePath() + "\"\r\n");
				Append("Data directory = \"" + Environment.getDataDirectory().getAbsolutePath() + "\"\r\n");
				Append("Download cache directory = \"" + Environment.getDownloadCacheDirectory().getAbsolutePath() + "\"\r\n");
				Append("External storage directory = \"" + Environment.getExternalStorageDirectory().getAbsolutePath() + "\"\r\n");
				AppendDivider();
			}
			_lock.unlock();
		}
		catch (Exception e)
		{
			e.printStackTrace();// can't send exceptions to ourself
		}
	}

	public static
		void
		i(
			String tag,
			String text)
	{
		WriteInfo(
			tag,
			text);
	}

	public static
		void
		WriteInfo(
			String tag,
			String text)
	{
		DMSDebugLog.getInstance().Append(
			Log.INFO,
			tag,
			text);
	}

	public static
		void
		e(
			String tag,
			String text)
	{
		WriteError(
			tag,
			text);
	}

	public static
		void
		e(
			String tag,
			String text,
			Exception e)
	{
		Write(
			tag,
			text,
			e);
	}

	public static
		void
		WriteError(
			String tag,
			String text)
	{
		DMSDebugLog.getInstance().Append(
			Log.ERROR,
			tag,
			text);
	}

	public static
		void
		w(
			String tag,
			String text)
	{
		WriteWarning(
			tag,
			text);
	}

	public static
		void
		WriteWarning(
			String tag,
			String text)
	{
		DMSDebugLog.getInstance().Append(
			Log.WARN,
			tag,
			text);
	}

	public static
		void
		d(
			String tag,
			String text)
	{
		WriteDebug(
			tag,
			text);
	}

	public static
		void
		WriteDebug(
			String tag,
			String text)
	{
		DMSDebugLog.getInstance().Append(
			Log.DEBUG,
			tag,
			text);
	}

	public static
		void
		v(
			String tag,
			String text)
	{
		WriteVerbose(
			tag,
			text);
	}

	public static
		void
		WriteVerbose(
			String tag,
			String text)
	{
		DMSDebugLog.getInstance().Append(
			Log.VERBOSE,
			tag,
			text);
	}

	public static
		void
		Write(
			String tag,
			String text)
	{
		DMSDebugLog.getInstance().Append(
			Log.INFO,
			tag,
			text);
	}

	public static
		void
		Write(
			int logLevelValue,
			String tag,
			String text)
	{
		DMSDebugLog.getInstance().Append(
			logLevelValue,
			tag,
			text);
	}

	public static
		void
		Write(
			String text)
	{
		DMSDebugLog.getInstance().Append(
			Log.INFO,
			"",
			text);
	}

	public static
		void
		Write(
			String tag,
			Exception e)
	{
		DMSDebugLog.getInstance().Append(
			tag,
			"",
			e);
	}

	public static
		void
		Write(
			String tag,
			String text,
			Exception e)
	{
		DMSDebugLog.getInstance().Append(
			tag,
			text,
			e);
	}

	public static
		void
		Write(
			Exception e)
	{
		DMSDebugLog.getInstance().Append(
			"",
			"",
			e);
	}

	public
		void
		Append(
			String tag,
			String description,
			Exception exception)
	{
		try
		{
			if (exception != null)
			{
				String text = "\r\n" + exception.getLocalizedMessage();
				String stackTrace = Log.getStackTraceString(exception);
				if((stackTrace != null) && (stackTrace.isEmpty() == false))
				{
					stackTrace = stackTrace.replace("\n", "\r\n");
					text += "\r\n" + stackTrace;
				}
				if ((description != null) && (description.isEmpty() == false))
				{
					tag += "\r\n" + description;
				}
				Append(
					DMSConstants.EXCEPTION_LOG_LEVEL,
					tag,
					text);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();// can't send exceptions to ourself
		}
	}

	public
		void
		Append(
			int logLevelValue,
			String tag,
			String text)
	{
		try
		{
			if ((text != null) && (text.isEmpty() == false))
			{
				if (tag == null)
				{
					tag = "";
				}
				// NOTE: ALWAYS log to the console, even if debug trace logging
				// isn't turned on
				String prefix = null;
				switch (logLevelValue)
				{
					case Log.VERBOSE:
						Log.v(
							tag,
							text);
						break;

					case Log.INFO:
						Log.i(
							tag,
							text);
						break;

					case Log.DEBUG:
						Log.d(
							tag,
							text);
						break;

					case Log.WARN:
						Log.w(
							tag,
							text);
						prefix = "WARNING";
						break;

					case Log.ERROR:
						Log.e(
							tag,
							text);
						prefix = "ERROR";
						break;

					case DMSConstants.EXCEPTION_LOG_LEVEL:
						Log.e(
							tag,
							text);
						prefix = "EXCEPTION";
						break;
				}

				// is debug trace logging on?
				if (Valid() == true)
				{
					// are we verbose, or is it an error or warning or
					// exception?
					//if ((logLevelValue == Log.ERROR) || (logLevelValue == Log.WARN) || (logLevelValue == Constants.EXCEPTION_LOG_LEVEL))
					{
						// write it out to external storage as well
						String msg = "";

						if ((prefix != null) && (prefix.isEmpty() == false))
						{
							msg += "\r\n<< " + prefix + " >>\r\n";
						}

						msg += _dateFormat.format(new Date()) + " - ";

						if (tag.isEmpty() == false)
						{
							msg += tag + " - ";
						}

						msg += text;
						msg += "\r\n";

						Append(msg);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();// can't send exceptions to ourself
		}
	}

	private
		void
		Append(
			String text)
	{
		try
		{
			_lock.lock();
			if ((Valid() == true) && (text != null) && (text.isEmpty() == false))
			{
				FileWriter fileWriter = new FileWriter(
					_file,
					true);
				BufferedWriter bufferedWriter = new BufferedWriter(
					fileWriter);
				bufferedWriter.write(text);
				bufferedWriter.newLine();
				bufferedWriter.flush();
				bufferedWriter.close();
				bufferedWriter = null;
				fileWriter.close();
				fileWriter = null;
			}
			_lock.unlock();
		}
		catch (Exception e)
		{
			e.printStackTrace();// can't send exceptions to ourself
		}
	}

	private
		void
		CreateFile()
	{
		try
		{
			_lock.lock();
			if (FileCreated() == false)
			{
				String subfolder = DMSFileIO.GetDebugSubfolder();
				if (DMSFileIO.Exists(
					subfolder,
					"") == false)
				{
					DMSFileIO.CreateSubfolder(subfolder);
				}
				_file = new File(
					subfolder,
					DMSFileIO.ComputeUniqueFilename("debuglog", DMSConstants.DEBUG_LOG_EXTENSION));
			}
			_lock.unlock();
		}
		catch (Exception e)
		{
			e.printStackTrace();// can't send exceptions to ourself
		}
	}

	public
		boolean
		Valid()
	{
		return (FileCreated() == true);
	}

	public
		void
		AppendMemoryState()
	{
		try
		{
			Append("Total memory = " + DMSUtils.GetASCIISize(DMSUtils.GetTotalMemory()) + "\r\n");
			Append("App [used] memory = " + DMSUtils.GetASCIISize(DMSUtils.GetUsedMemorySize()) + "\r\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();// can't send exceptions to ourself
		}
	}

	public
		void
		AppendCPUInfo()
	{
		try
		{
			Append(DMSUtils.GetCPUInfo());
		}
		catch (Exception e)
		{
			e.printStackTrace();// can't send exceptions to ourself
		}
	}

	private
		void
		AppendDivider()
	{
		Append("________________________________________________________\r\n");
	}
}
