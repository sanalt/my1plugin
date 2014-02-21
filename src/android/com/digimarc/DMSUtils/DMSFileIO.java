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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import com.digimarc.DMSUtils.DMSConstants;

public class DMSFileIO
{
	@SuppressLint("SimpleDateFormat")
	private static SimpleDateFormat _dateFormat = new SimpleDateFormat(
		"MM_dd_yyyy_HH_mm_ss");

	public static String mRootSubfolder = DMSConstants.ROOT_SUBFOLDER;

	public static
		String
		ComputeUniqueFilename(
			String suffix,
			String extension)
	{
		String result = "";
		try
		{
			if ((suffix != null) && (suffix.isEmpty() == false) && (extension != null) && (extension.isEmpty() == false))
			{
				String currentDateandTime = _dateFormat.format(new Date());
				result = currentDateandTime + "_" + suffix + "." + extension;
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
		String
		GetExternalFilesDir()
	{
		String result = "";
		try
		{
			File file = Environment.getExternalStorageDirectory();
			result = file.getAbsolutePath();
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
		GetRootSubfolder()
	{
		String result = "";
		try
		{
			if ((mRootSubfolder == null) || (mRootSubfolder.isEmpty() == true))
			{
				mRootSubfolder = DMSConstants.ROOT_SUBFOLDER;
			}
			result = GetExternalFilesDir() + "/" + mRootSubfolder;
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
		GetDebugSubfolder()
	{
		String result = "";
		try
		{
			result = GetRootSubfolder() + "/" + DMSConstants.DEBUG_LOGS_SUBFOLDER;
		}
		catch (Exception e)
		{
			e.printStackTrace();// since DMSDebugLog uses this method during his
								// constructor, we can't send exceptions to him
		}
		return (result);
	}

	public static
		boolean
		Exists(
			String filename)
	{
		return (Exists(
			"",
			filename));
	}

	public static
		boolean
		Exists(
			String folder,
			String filename)
	{
		boolean result = false;
		if ((filename != null) && (filename.isEmpty() == false))
		{
			try
			{
				folder = DMSUtils.ValidateString(folder);
				File file = new File(
					folder,
					filename);
				result = file.exists();
			}
			catch (Exception e)
			{
				e.printStackTrace();// since DMSDebugLog uses this method during
									// his
				// constructor, we can't send exceptions to him
			}
		}
		return (result);
	}

	public static
		boolean
		CreateSubfolder(
			String folder)
	{
		boolean result = false;
		try
		{
			folder = DMSUtils.ValidateString(folder);
			File dir = new File(
				folder);
			if (dir.isDirectory() == false)
			{
				dir.mkdirs();
			}
			result = (dir.isDirectory() == true);
		}
		catch (Exception e)
		{
			e.printStackTrace();// since DMSDebugLog uses this method during his
			// constructor, we can't send exceptions to him
		}
		return (result);
	}

	public static
		File
		WriteList(
			List<String> list,
			String folder,
			String filename)
	{
		File file = null;
		if ((list != null) && (list.size() > 0))
		{
			StringBuilder sb = new StringBuilder();
			for (String text : list)
			{
				if (text != null)
				{
					sb.append(text);
					if (text.endsWith(DMSConstants.CRLF) == false)
					{
						sb.append(DMSConstants.CRLF);
					}
				}
			}
			file = WriteTextFile(
				sb.toString(),
				folder,
				filename);
		}
		return (file);
	}

	public static
		List<String>
		ReadList(
			String folder,
			String filename)
	{
		List<String> list = new ArrayList<String>();
		if ((filename != null) && (filename.isEmpty() == false))
		{
			folder = DMSUtils.ValidateString(folder);
			String text = ReadTextFile(
				folder,
				filename);
			if ((text != null) && (text.isEmpty() == false))
			{
				String[] strings = text.split(DMSConstants.CRLF);
				if (strings != null)
				{
					for (String string : strings)
					{
						if (string != null)
						{
							list.add(string);
						}
					}
				}
			}
		}
		return (list);
	}

	public static
		List<DMSNameValuePair>
		ReadNameValuePairList(
			String folder,
			String filename)
	{
		List<DMSNameValuePair> list = new ArrayList<DMSNameValuePair>();
		try
		{
			List<String> lines = ReadList(
				folder,
				filename);
			if ((lines != null) && (lines.size() > 0))
			{
				for (String line : lines)
				{
					if (line != null)
					{
						line = line.trim();
						if (line.isEmpty() == false)
						{
							String[] strings = line.split(DMSConstants.EQUAL_SIGN);
							if ((strings != null) && (strings.length == 2))
							{
								String name = strings[0];
								name = name.trim();
								String value = strings[1];
								value = value.trim();
								DMSNameValuePair nvp = new DMSNameValuePair(
									name,
									value);
								list.add(nvp);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"FileIO.ReadNameValuePairList",
				e);
		}
		return (list);
	}

	public static
		void
		WriteNameValuePairList(
			String folder,
			String filename,
			List<DMSNameValuePair> list)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			if ((list != null) && (list.size() > 0))
			{
				for (DMSNameValuePair nvp : list)
				{
					if (nvp != null)
					{
						String name = nvp.GetName();
						if ((name != null) && (name.isEmpty() == false))
						{
							String value = nvp.GetValue();
							if (value == null)
							{
								value = "";
							}
							sb.append(name);
							sb.append(DMSConstants.EQUAL_SIGN);
							sb.append(value);
							sb.append(DMSConstants.CRLF);
						}
					}
				}
			}
			WriteTextFile(
				sb.toString(),
				folder,
				filename);
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"FileIO.WriteNameValuePairList",
				e);
		}
	}

	public static
		File
		WriteTextFile(
			String text,
			String folder,
			String filename)
	{
		File file = null;
		try
		{
			if ((text != null) && (filename != null) && (filename.isEmpty() == false))
			{
				folder = DMSUtils.ValidateString(folder);
				file = WriteBinaryFile(
					text.getBytes(),
					folder,
					filename);
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"FileIO.WriteTextFile",
				e);
		}
		return (file);
	}

	public static
		String
		ReadTextFile(
			String folder,
			String filename)
	{
		String result = "";
		if ((filename != null) && (filename.isEmpty() == false))
		{
			try
			{
				folder = DMSUtils.ValidateString(folder);
				if (DMSFileIO.Exists(
					folder,
					filename) == true)
				{
					DMSDebugLog.Write("FileIO.ReadTextFile\r\n    folder = \"" + folder + "\"\r\n    filename = \"" + filename + "\"");
					File file = new File(
						folder,
						filename);
					StringBuilder sb = new StringBuilder();
					try
					{
						FileReader fileReader = new FileReader(
							file);
						if (fileReader != null)
						{
							BufferedReader bufferedReader = new BufferedReader(
								fileReader);
							if (bufferedReader != null)
							{
								String line = null;
								while ((line = bufferedReader.readLine()) != null)
								{
									line = line.trim();
									if (line.isEmpty() == false)
									{
										sb.append(line);
										sb.append(DMSConstants.CRLF);
									}
								}
								bufferedReader.close();
								bufferedReader = null;
							}
							fileReader.close();
							fileReader = null;
						}
					}
					catch (Exception e)
					{
						DMSDebugLog.Write(
							"FileIO.ReadTextFile",
							e);
					}
					result = sb.toString();
				}
			}
			catch (Exception e)
			{
				DMSDebugLog.Write(
					"FileIO.ReadTextFile",
					e);
			}
		}
		return (result);
	}

	public static
		File
		WriteBinaryFile(
			byte[] bytes,
			String folder,
			String filename)
	{
		File file = null;
		if ((bytes != null) && (filename != null) && (filename.isEmpty() == false))
		{
			try
			{
				folder = DMSUtils.ValidateString(folder);
				if ((folder.isEmpty() == true) || (DMSFileIO.CreateSubfolder(folder) == true))
				{
					Delete(
						folder,
						filename);
					if (bytes.length > 0)
					{
						DMSDebugLog.Write("FileIO.WriteBinaryFile\r\n    folder = \"" + folder + "\"\r\n    filename = \"" + filename + "\"");
						file = new File(
							folder,
							filename);
						FileOutputStream fos = new FileOutputStream(
							file);
						fos.write(bytes);
						fos.flush();
						fos.close();
						fos = null;
					}
				}
			}
			catch (Exception e)
			{
				DMSDebugLog.Write(
					"FileIO.WriteBinaryFile",
					e);
			}
		}
		return (file);
	}

	public static
		boolean
		Delete(
			String folder,
			String filename)
	{
		boolean result = false;
		try
		{
			if (Exists(
				folder,
				filename) == true)
			{
				folder = DMSUtils.ValidateString(folder);
				DMSDebugLog.Write("FileIO.Delete\r\n    folder = \"" + folder + "\"\r\n    filename = \"" + filename + "\"");
				File file = new File(
					folder,
					filename);
				result = file.delete();
				if (Exists(
					folder,
					filename) == true)
				{
					DMSDebugLog.WriteError(
						"FileIO.Delete",
						"Unable to delete \"" + filename + "\"");
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"FileIO.Delete",
				e);
		}
		return (result);
	}

	public static
		void
		ViewFolder(
			Context context,
			String folder)
	{
		try
		{
			if ((context != null) && (folder != null) && (folder.isEmpty() == false))
			{
				Uri startDir = Uri.fromFile(new File(
					folder));
				Intent intent = new Intent();
				intent.setData(startDir);
				intent.setAction(Intent.ACTION_VIEW);
				context.startActivity(intent);
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"FileIO.ViewFolder",
				e);
		}
	}

	public static
		List<File>
		GetFiles(
			String subfolder)
	{
		List<File> list = new ArrayList<File>();
		try
		{
			File file = new File(
				subfolder);
			if (file != null)
			{
				File[] files = file.listFiles();
				if ((files != null) && (files.length > 0))
				{
					List<DMSSortedFileModifyDate> tempList = new ArrayList<DMSSortedFileModifyDate>();
					for (File tempFile : files)
					{
						if (tempFile.isDirectory() == false)
						{
							tempList.add(new DMSSortedFileModifyDate(tempFile));
						}
					}

					if (tempList.size() > 1)
					{
						Collections.sort(tempList);
						for(DMSSortedFileModifyDate sfmd : tempList)
						{
							list.add(sfmd.GetFile());
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"FileIO.GetFiles",
				e);
		}
		return (list);
	}
}

