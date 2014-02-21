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
package com.digimarc.DMSUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

public class DMSBitmapUtils
{
	public static enum SCALING_LOGIC
	{
		CROP,
		FIT
	}

	public static
		byte[]
		ConvertPreviewImage(
			byte[] data,
			Camera.Parameters parameters)
	{
		byte[] byteArray = null;
		try
		{
			if ((data != null) && (data.length > 0) && (parameters != null))
			{
				int format = parameters.getPreviewFormat();
				if ((format == ImageFormat.NV21) || (format == ImageFormat.YUY2 || format == ImageFormat.NV16))
				{
					int width = parameters.getPreviewSize().width;
					int height = parameters.getPreviewSize().height;

					// Get the YuV image
					YuvImage yuv_image = new YuvImage(
						data,
						format,
						width,
						height,
						null);

					// Convert YuV to Jpeg
					Rect rect = new Rect(
						0,
						0,
						width,
						height);

					ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
					if (output_stream != null)
					{
						yuv_image.compressToJpeg(
							rect,
							100,
							output_stream);
						if (output_stream.size() > 0)
						{
							byteArray = output_stream.toByteArray();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"BitmapUtils.ConvertPreviewImage",
				e);
		}
		return (byteArray);
	}

	public static
		Bitmap
		DecodeResource(
			Resources res,
			int resId,
			int dstWidth,
			int dstHeight,
			SCALING_LOGIC scalingLogic)
	{
		Bitmap unscaledBitmap = null;
		try
		{
			Options options = new Options();
			options.inJustDecodeBounds = true;
			options.inDither = true;
			options.inScaled = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			BitmapFactory.decodeResource(
				res,
				resId,
				options);
			options.inJustDecodeBounds = false;
			options.inSampleSize = CalculateSampleSize(
				options.outWidth,
				options.outHeight,
				dstWidth,
				dstHeight,
				scalingLogic);
			unscaledBitmap = BitmapFactory.decodeResource(
				res,
				resId,
				options);
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"BitmapUtils.decodeResource",
				e);
		}
		return unscaledBitmap;
	}

	public static
		Bitmap
		LoadBitmap(
			String folder,
			String filename)
	{
		Bitmap bitmap = null;
		try
		{
			if ((folder != null) && (folder.isEmpty() == false) && (filename != null) && (filename.isEmpty() == false))
			{
				if (DMSFileIO.Exists(
					folder,
					filename) == true)
				{
					File path = new File(
						folder,
						filename);
					Options options = new Options();
					options.inJustDecodeBounds = false;
					options.inDither = true;
					options.inScaled = false;
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					bitmap = BitmapFactory.decodeFile(
						path.getAbsolutePath(),
						options);
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"BitmapUtils.LoadBitmap",
				e);
		}
		return (bitmap);
	}

	public static
		Bitmap
		CreateScaledBitmap(
			Bitmap unscaledBitmap,
			int dstWidth,
			int dstHeight,
			SCALING_LOGIC scalingLogic)
	{
		Bitmap scaledBitmap = null;
		try
		{
			if (unscaledBitmap != null)
			{
				Rect srcRect = CalculateSrcRect(
					unscaledBitmap.getWidth(),
					unscaledBitmap.getHeight(),
					dstWidth,
					dstHeight,
					scalingLogic);
				Rect dstRect = CalculateDstRect(
					unscaledBitmap.getWidth(),
					unscaledBitmap.getHeight(),
					dstWidth,
					dstHeight,
					scalingLogic);
				if ((srcRect != null) && (dstRect != null))
				{
					scaledBitmap = Bitmap.createBitmap(
						dstRect.width(),
						dstRect.height(),
						Config.ARGB_8888);
					if (scaledBitmap != null)
					{
						Canvas canvas = new Canvas(
							scaledBitmap);
						Paint paint = new Paint();
						paint.setAntiAlias(true);
						paint.setFilterBitmap(true);
						paint.setDither(true);
						canvas.drawBitmap(
							unscaledBitmap,
							srcRect,
							dstRect,
							paint);
						canvas = null;
					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"BitmapUtils.CreateScaledBitmap",
				e);
		}
		return scaledBitmap;
	}

	public static
		Bitmap
		RotateBitmap(
			Bitmap sourceBitmap,
			float degree)
	{
		Bitmap bitmap = null;
		try
		{
			Matrix matrix = new Matrix();
			matrix.postRotate(degree);
			bitmap = Bitmap.createBitmap(
				sourceBitmap,
				0,
				0,
				sourceBitmap.getWidth(),
				sourceBitmap.getHeight(),
				matrix,
				true);
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"BitmapUtils.RotateBitmap",
				e);
		}
		return (bitmap);
	}

	public static
		int
		CalculateSampleSize(
			int srcWidth,
			int srcHeight,
			int dstWidth,
			int dstHeight,
			SCALING_LOGIC scalingLogic)
	{
		int result = 1;
		try
		{
			if (scalingLogic == SCALING_LOGIC.FIT)
			{
				final float srcAspect = (float) srcWidth / (float) srcHeight;
				final float dstAspect = (float) dstWidth / (float) dstHeight;

				if (srcAspect > dstAspect)
				{
					result = srcWidth / dstWidth;
				}
				else
				{
					result = srcHeight / dstHeight;
				}
			}
			else
			{
				final float srcAspect = (float) srcWidth / (float) srcHeight;
				final float dstAspect = (float) dstWidth / (float) dstHeight;

				if (srcAspect > dstAspect)
				{
					result = srcHeight / dstHeight;
				}
				else
				{
					result = srcWidth / dstWidth;
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"BitmapUtils.CalculateSampleSize",
				e);
		}
		return (result);
	}

	public static
		Rect
		CalculateSrcRect(
			int srcWidth,
			int srcHeight,
			int dstWidth,
			int dstHeight,
			SCALING_LOGIC scalingLogic)
	{
		Rect rect = null;
		try
		{
			if (scalingLogic == SCALING_LOGIC.CROP)
			{
				final float srcAspect = (float) srcWidth / (float) srcHeight;
				final float dstAspect = (float) dstWidth / (float) dstHeight;

				if (srcAspect > dstAspect)
				{
					final int srcRectWidth = (int) (srcHeight * dstAspect);
					final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
					rect = new Rect(
						srcRectLeft,
						0,
						srcRectLeft + srcRectWidth,
						srcHeight);
				}
				else
				{
					final int srcRectHeight = (int) (srcWidth / dstAspect);
					final int scrRectTop = (int) (srcHeight - srcRectHeight) / 2;
					rect = new Rect(
						0,
						scrRectTop,
						srcWidth,
						scrRectTop + srcRectHeight);
				}
			}
			else
			{
				rect = new Rect(
					0,
					0,
					srcWidth,
					srcHeight);
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"BitmapUtils.CalculateSrcRect",
				e);
		}
		return (rect);
	}

	public static
		Rect
		CalculateDstRect(
			int srcWidth,
			int srcHeight,
			int dstWidth,
			int dstHeight,
			SCALING_LOGIC scalingLogic)
	{
		Rect rect = null;
		try
		{
			if (scalingLogic == SCALING_LOGIC.FIT)
			{
				final float srcAspect = (float) srcWidth / (float) srcHeight;
				final float dstAspect = (float) dstWidth / (float) dstHeight;

				if (srcAspect > dstAspect)
				{
					rect = new Rect(
						0,
						0,
						dstWidth,
						(int) (dstWidth / srcAspect));
				}
				else
				{
					rect = new Rect(
						0,
						0,
						(int) (dstHeight * srcAspect),
						dstHeight);
				}
			}
			else
			{
				rect = new Rect(
					0,
					0,
					dstWidth,
					dstHeight);
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"BitmapUtils.CalculateDstRect",
				e);
		}
		return (rect);
	}

	// public static
	// Bitmap
	// CreateScaledBitmap(
	// Bitmap sourceBitmap,
	// int targetWidth,
	// int targetHeight)
	// {
	// Bitmap bitmap = null;
	// try
	// {
	// if ((sourceBitmap != null) && (sourceBitmap.getWidth() > 0) &&
	// (sourceBitmap.getHeight() > 0) && (targetWidth > 0) && (targetHeight >
	// 0))
	// {
	// bitmap = Bitmap.createBitmap(
	// targetWidth,
	// targetHeight,
	// Config.ARGB_8888);
	//
	// float ratioX = targetWidth / (float) bitmap.getWidth();
	// float ratioY = targetHeight / (float) bitmap.getHeight();
	// float middleX = targetWidth / 2.0f;
	// float middleY = targetHeight / 2.0f;
	//
	// Matrix scaleMatrix = new Matrix();
	// scaleMatrix.setScale(
	// ratioX,
	// ratioY,
	// middleX,
	// middleY);
	//
	// Canvas canvas = new Canvas(
	// bitmap);
	// canvas.setMatrix(scaleMatrix);
	// canvas.drawBitmap(
	// bitmap,
	// middleX - bitmap.getWidth() / 2,
	// middleY - bitmap.getHeight() / 2,
	// new Paint(
	// Paint.FILTER_BITMAP_FLAG));
	// canvas = null;
	// }
	// }
	// catch (Exception e)
	// {
	// DMSDebugLog.Write(
	// "BitmapUtils.CreateScaledBitmap",
	// e);
	// }
	// return (bitmap);
	// }
}
