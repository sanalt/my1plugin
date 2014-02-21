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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DMSCrosshairView
	extends
	View
{
	private Paint mPaint = new Paint();
//	private float mAzimuth = 0;
//	private float mPitch = 0;
//	private float mRoll = 0;
//	private int mOffsetColor = Color.argb(128, 255, 0, 0);

	public DMSCrosshairView(
		Context context,
		AttributeSet attrs)
	{
		super(
			context,
			attrs);

		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.MITER);
	}

//	public
//		void
//		Update(
//			MotionSensor sensor)
//	{
//		try
//		{
//			if (sensor != null)
//			{
//				mAzimuth = sensor.GetAzimuth();
//				mPitch = sensor.GetPitch();
//				mRoll = sensor.GetRoll();
//				invalidate();
//			}
//		}
//		catch (Exception e)
//		{
//			DebugLog.Write(
//				"CrosshairView.Update",
//				e);
//		}
//	}

	@Override
	protected
		void
		onDraw(
			Canvas canvas)
	{
		try
		{
			DrawCrosshairs(canvas);
			super.onDraw(canvas);
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"CrosshairView.onDraw",
				e);
		}
	}

	private
		void
		DrawCrosshairs(Canvas canvas)
	{
		try
		{
			if ((canvas != null) && (mPaint != null))
			{
				int width = getWidth();
				int height = getHeight();
				if ((width > 0) && (height > 0))
				{
					int centerx = width / 2;
					int centery = height / 2;
					
					mPaint.setColor(Color.BLACK);
					mPaint.setStrokeWidth(2f);
					
					canvas.drawLine(
						centerx,
						0,
						centerx,
						height,
						mPaint);
					canvas.drawLine(
						0,
						centery,
						width,
						centery,
						mPaint);
					
//					if (mRoll != 0)
//					{
//						mPaint.setColor(mOffsetColor);
//						mPaint.setStrokeWidth(1f);
//						
//						canvas.rotate(
//							-mRoll * 360 / (2 * 3.14159f),
//							centerx,
//							centery);
//						
//						canvas.drawLine(
//							centerx,
//							-2000,
//							centerx,
//							+2000,
//							mPaint);
//						canvas.drawLine(
//							-2000,
//							centery,
//							2000,
//							centery,
//							mPaint);
//					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"CrosshairView.DrawCrosshair",
				e);
		}
	}
}
