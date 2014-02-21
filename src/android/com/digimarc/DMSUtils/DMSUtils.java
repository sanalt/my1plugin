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
 conveys no license under the foregoing patents, nor under any of DigimarcÕs other patent, trademark,
 or copyright rights.
 
 This software comprises CONFIDENTIAL INFORMATION, including TRADE SECRETS, of Digimarc Corporation,
 USA, and is protected by a license agreement and/or non-disclosure agreement with Digimarc.  It is
 important that this software be used, copied and/or disclosed only in accordance with such
 agreements.
 
 © Copyright, Digimarc Corporation, USA.  All Rights Reserved.
 
***************************************************************************************************/package com.digimarc.DMSUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Spanned;

public class DMSUtils
{
	public static void ToggleScreenDimTimeoutValue(String caller, Context context)
	{
		try
		{
			if((context != null) && (caller != null) && (caller.isEmpty() == false))
			{
				DMSDebugLog.Write("Utils.ToggleScreenDimTimeoutValue - toggling SCREEN_OFF_TIMEOUT on behalf of \"" + caller + "\"");
				ContentResolver cr = context.getContentResolver();
				if(cr != null)
				{
					String previousValue = Settings.System.getString(cr, Settings.System.SCREEN_OFF_TIMEOUT);
					Settings.System.putString(cr, Settings.System.SCREEN_OFF_TIMEOUT, "-1");
					Settings.System.putString(cr, Settings.System.SCREEN_OFF_TIMEOUT, previousValue);
				}
			}
		}
		catch(Exception e)
		{
			DMSDebugLog.Write("Utils.ToggleScreenDimTimeoutValue", e);
		}
	}

	@SuppressWarnings("deprecation")
	public static void resetScreenTimeout(String caller, Context context)
	{
		PowerManager.WakeLock wakelock = null;

		try
		{
			if((context != null) && (caller != null) && (caller.isEmpty() == false))
			{
				DMSDebugLog.Write("Utils.resetScreenTimeout - touching wakelock on behalf of \"" + caller + "\"");
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

				if(pm != null)
				{
					wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "reset");

					wakelock.acquire();
				}
			}
		}
		catch(Exception e)
		{
			DMSDebugLog.Write("Utils.ToggleScreenDimTimeoutValue", e);
		}
		finally
		{
			if ( wakelock != null )
				wakelock.release();
		}
	}

	public static
		String
		GetASCIISize(
			long value)
	{
		StringBuilder sb = new StringBuilder();
		try
		{
			double dvalue = (double) value;
			if (dvalue > DMSConstants.ONE_GIG)
			{
				dvalue = (dvalue / (double) DMSConstants.ONE_GIG);
				sb.append(String.format(
					"%.2f",
					dvalue) + " Gig");
			}
			else if (dvalue > DMSConstants.ONE_MEG)
			{
				dvalue = (dvalue / (double) DMSConstants.ONE_MEG);
				sb.append(String.format(
					"%.2f",
					dvalue) + " Meg");
			}
			else if (dvalue > DMSConstants.ONE_K)
			{
				dvalue = (dvalue / (double) DMSConstants.ONE_K);
				sb.append(String.format(
					"%.2f",
					dvalue) + " K");
			}
			else
			{
				sb.append("1 K");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();// since DMSDebugLog uses this method during his
								// constructor, we can't send exceptions to him
		}
		return (sb.toString());
	}

	public static
		long
		GetTotalMemory()
	// Activity activity)
	{
		long result = 0L;
		try
		{
			// API Level 16 and onward...
			// if (activity != null)
			// {
			// ActivityManager actManager = (ActivityManager)
			// activity.getSystemService(Context.ACTIVITY_SERVICE);
			// MemoryInfo memInfo = new ActivityManager.MemoryInfo();
			// actManager.getMemoryInfo(memInfo);
			// result = memInfo.totalMem;
			// }

			// pre API level 16...
			File file = new File(
				"/proc/meminfo");
			if (file.exists() == true)
			{
				FileReader fileReader = new FileReader(
					file);
				BufferedReader bufferedReader = new BufferedReader(
					fileReader,
					8192);
				String temp = bufferedReader.readLine();
				bufferedReader.close();
				bufferedReader = null;
				fileReader.close();
				fileReader = null;
				if ((temp != null) && (temp.isEmpty() == false))
				{
					String[] arrayOfString = temp.split("\\s+");
					result = (Integer.valueOf(
						arrayOfString[1]).intValue() * DMSConstants.ONE_K);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();// since DMSDebugLog uses this method during his
								// constructor, we can't send exceptions to him
		}
		return (result);
	}

	public static
		long
		GetUsedMemorySize()
	{
		long result = 0L;
		try
		{
			Runtime info = Runtime.getRuntime();
			long freeSize = info.freeMemory();
			long totalSize = info.totalMemory();
			result = totalSize - freeSize;
		}
		catch (Exception e)
		{
			e.printStackTrace();// since DMSDebugLog uses this method during his
								// constructor, we can't send exceptions to him
		}
		return (result);
	}

	public static
		String
		GetCPUInfo()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			File file = new File(
				"/proc/cpuinfo");
			if (file.exists() == true)
			{
				sb.append("CPU info =\r\n");
				FileReader fileReader = new FileReader(
					file);
				BufferedReader bufferedReader = new BufferedReader(
					fileReader);
				String line = null;
				while ((line = bufferedReader.readLine()) != null)
				{
					sb.append("    " + line + "\r\n");
				}
				bufferedReader.close();
				bufferedReader = null;
				fileReader.close();
				fileReader = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();// since DMSDebugLog uses this method during his
								// constructor, we can't send exceptions to him
		}
		return (sb.toString());
	}

	public static
		void
		SendEMail(
			Activity activity,
			List<String> emailTo,
			List<String> emailCC,
			String subject,
			String emailText,
			List<String> filenames)
	{
		SendEMail(
			activity,
			emailTo,
			emailCC,
			subject,
			emailText,
			null,
			filenames);
	}

	public static
		void
		SendEMail(
			Activity activity,
			List<String> emailTo,
			List<String> emailCC,
			String subject,
			Spanned spannedText,
			List<String> filenames)
	{
		SendEMail(
			activity,
			emailTo,
			emailCC,
			subject,
			null,
			spannedText,
			filenames);
	}

	public static
		void
		SendEMail(
			Activity activity,
			List<String> emailTo,
			List<String> emailCC,
			String subject,
			String emailText,
			Spanned spannedText,
			List<String> filenames)
	{
		if ((activity != null) && (emailTo != null) && (emailTo.size() > 0) && (subject != null) && (subject.isEmpty() == false))
		{
			try
			{
				String type = "";
				if ((emailText != null) && (emailText.isEmpty() == false))
				{
					type = "text/plain";
				}
				else if ((spannedText != null) && (spannedText.length() > 0))
				{
					type = "text/html";
				}
				if ((type != null) && (type.isEmpty() == false))
				{
					Intent emailIntent = FindSendIntent(
						activity,
						"mail");
					// Intent emailIntent = new
					// Intent(android.content.Intent.ACTION_SEND);
					if (emailIntent != null)
					{
						emailIntent.setType(type);

						emailIntent.putExtra(
							android.content.Intent.EXTRA_EMAIL,
							emailTo.toArray(new String[emailTo.size()]));

						if ((emailCC != null) && (emailCC.size() > 0))
						{
							emailIntent.putExtra(
								android.content.Intent.EXTRA_CC,
								emailCC.toArray(new String[emailCC.size()]));
						}

						emailIntent.putExtra(
							Intent.EXTRA_SUBJECT,
							subject);

						if ((emailText != null) && (emailText.isEmpty() == false))
						{
							emailIntent.putExtra(
								Intent.EXTRA_TEXT,
								emailText);
						}
						else if ((spannedText != null) && (spannedText.length() > 0))
						{
							emailIntent.putExtra(
								Intent.EXTRA_TEXT,
								spannedText);
						}

						if ((filenames != null) && (filenames.size() > 0))
						{
							ArrayList<Uri> uris = new ArrayList<Uri>();
							for (String filename : filenames)
							{
								File file = new File(
									filename);
								Uri uri = Uri.fromFile(file);
								uris.add(uri);
							}
							emailIntent.putParcelableArrayListExtra(
								Intent.EXTRA_STREAM,
								uris);
						}

						// digimarcandroid@gmail.com
						// D1g1marc2013
						activity.startActivity(Intent.createChooser(
							emailIntent,
							"Send mail..."));
					}
				}
			}
			catch (Exception e)
			{
				DMSDebugLog.Write(
					"Utils.SendEMail",
					e);
			}
		}
	}

	public static
		Intent
		FindSendIntent(
			Activity activity,
			String type)
	{
		Intent intent = null;
		if ((activity != null) && (type != null) && (type.isEmpty() == false))
		{
			PackageManager mgr = activity.getPackageManager();
			if (mgr != null)
			{
				boolean found = false;
				type = type.toLowerCase();
				intent = new Intent(
					android.content.Intent.ACTION_SEND_MULTIPLE);
				intent.setType("image/jpeg");
				List<ResolveInfo> resInfo = mgr.queryIntentActivities(
					intent,
					0);
				if ((resInfo != null) && (resInfo.isEmpty() == false))
				{
					for (ResolveInfo info : resInfo)
					{
						if ((info.activityInfo.packageName.toLowerCase().contains(type)) || (info.activityInfo.name.toLowerCase().contains(type)))
						{
							intent.setPackage(info.activityInfo.packageName);
							found = true;
							break;
						}
					}
				}
				if (found == false)
				{
					intent = null;
				}
			}
		}
		return (intent);
	}

	public static
		long
		ComputeBuildDateAndTime(
			Context context)
	{
		long time = 0;
		try
		{
			if (context != null)
			{
				ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
					context.getPackageName(),
					0);
				ZipFile zf = new ZipFile(
					ai.sourceDir);
				ZipEntry ze = zf.getEntry("classes.dex");
				time = ze.getTime();
				zf.close();
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"Utils.ComputeBuildDateAndTime",
				e);
		}
		return (time);
	}

	public static
		String
		ComputeBuildDateAndTimeString(
			Context context)
	{
		StringBuilder sb = new StringBuilder();
		try
		{
			if (context != null)
			{
				long ldatetime = ComputeBuildDateAndTime(context);
				if (ldatetime > 0)
				{
					Date dtime = new Date(
						ldatetime);
					sb.append(dtime.toString());
					sb.append("\r\n");
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"Utils.ComputeBuildDateAndTimeString",
				e);
		}
		return (sb.toString());
	}

	public static
		String
		ValidateString(
			String text)
	{
		if (text == null)
		{
			text = "";
		}
		return (text);
	}
}
