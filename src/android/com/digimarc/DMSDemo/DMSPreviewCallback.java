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
 
 ***************************************************************************************************/
package com.digimarc.DMSDemo;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import com.digimarc.DMSUtils.DMSBitmapUtils;
import com.digimarc.DMSUtils.DMSBitmapUtils.SCALING_LOGIC;
import com.digimarc.DMSUtils.DMSConstants;
import com.digimarc.DMSUtils.DMSDebugLog;
import com.digimarc.DMSUtils.DMSFileIO;
import com.digimarc.DMSUtils.DMSOpsCounter;
import com.digimarc.dms.DMSManager;

public class DMSPreviewCallback
	implements
	Camera.PreviewCallback
{
	public static String PREVIEW_IMAGE_FILENAME = "DigimarcPreviewFrameImage.jpg";

	private static final String TAG = "PreviewCallback";

	/**
	 * Change to true if you want to write the image data to sd card
	 */
	private static boolean captureImageToSD = false;
	private static byte[] mSavedImageData = null;
	
	private class PreviewData
	{
		public Size mPreviewSize;
		@SuppressWarnings("unused")
		public int mPreviewFormat;
	};

	DMSManager mDmsMgr = null;
	DMSOpsCounter mFps = null;

	private long mLastPreviewImageSave = 0;

	private static PreviewData mPreviewData = null;

	public DMSPreviewCallback()
	{
		mPreviewData = new PreviewData();
		
		mDmsMgr = DMSManager.getInstance();
		mFps = new DMSOpsCounter(
			"camera FPS");
	}

	public
		void
		setPreviewData(
			int previewFormat,
			Size previewSize)
	{
		mPreviewData.mPreviewSize = previewSize;
		mPreviewData.mPreviewFormat = previewFormat;
	}

	@Override
	public
		void
		onPreviewFrame(
			byte[] data,
			Camera camera)
	{

		// for tracking actual frame rate;
		mFps.signalEvent();

		// queue data to ICP SDK
		mDmsMgr.incomingImageBuffer(
			data,
			mPreviewData.mPreviewSize.width,
			mPreviewData.mPreviewSize.height);

		SavePreviewImage(
			data,
			camera);

		// put buffer back in the camera buffer pool
		DMSCameraWrapper.get().bufferUseComplete(
			data);
	}

	private
		void
		SavePreviewImage(
			byte[] data,
			Camera camera)
	{
		try
		{
			if ((data != null) && (data.length > 0) && (camera != null))
			{
				long diff = System.currentTimeMillis() - mLastPreviewImageSave;
				if (diff > (DMSConstants.ONE_SECOND_MILLISECS / 2))
				{
					Camera.Parameters parameters = camera.getParameters();
					if (parameters != null)
					{
						byte[] byteArray = DMSBitmapUtils.ConvertPreviewImage(
							data,
							parameters);
						if ((byteArray != null) && (byteArray.length > 0))
						{					
							if (captureImageToSD) {
								// Write to SD Card
								String subfolder = GetPreviewImageSubfolder();
								if (DMSFileIO.CreateSubfolder(subfolder) == true)
								{
									DMSFileIO.WriteBinaryFile(
										byteArray,
										subfolder,
										PREVIEW_IMAGE_FILENAME);
									File file = new File(
										subfolder,
										PREVIEW_IMAGE_FILENAME);
									
								}
							}
							mLastPreviewImageSave = System.currentTimeMillis();
							mSavedImageData = byteArray;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"DMSPreviewCallback.SavePreviewImage",
				e);
		}
	}

	public static
		String
		GetPreviewImageSubfolder()
	{
		return (DMSFileIO.GetDebugSubfolder());
	}

	public synchronized static
		Bitmap
		GetPreviewImage()
	{
		Bitmap imageBitmap = null;
		
		if (captureImageToSD) {
			return (DMSBitmapUtils.LoadBitmap(
				GetPreviewImageSubfolder(),
				PREVIEW_IMAGE_FILENAME));
		} else {
			if (null != mSavedImageData) {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.outHeight = mPreviewData.mPreviewSize.height;
				opt.outWidth = mPreviewData.mPreviewSize.width;
				imageBitmap = BitmapFactory.decodeByteArray(mSavedImageData, 0, mSavedImageData.length, opt);
			}
		}
		return imageBitmap;
	}

	public static
		Bitmap
		GetScaledPreviewImage()
	{
		Bitmap bitmap = null;
		try
		{
			Bitmap sourceBitmap = GetPreviewImage();
			if (sourceBitmap != null)
			{
				bitmap = DMSBitmapUtils.CreateScaledBitmap(
					sourceBitmap,
					128,
					128,
					SCALING_LOGIC.CROP);
				if (bitmap != null)
				{
					bitmap = DMSBitmapUtils.RotateBitmap(
						bitmap,
						90);

					int width = bitmap.getWidth() - 1;
					int height = bitmap.getHeight() - 1;
					Canvas canvas = new Canvas(
						bitmap);
					Paint paint = new Paint(
						Paint.FILTER_BITMAP_FLAG);
					paint.setColor(Color.BLACK);
					paint.setStyle(Style.STROKE);
					canvas.drawRect(
						0,
						0,
						width,
						height,
						paint);
				}
				sourceBitmap.recycle();
				sourceBitmap = null;
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"DMSPreviewCallback.GetScaledPreviewImage",
				e);
		}
		return (bitmap);
	}
}
