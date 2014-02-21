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

import android.os.SystemClock;
import android.util.Log;

public class DMSOpsCounter {

	private static final String TAG = "OpsCounter";

	private static final long REPORT_INTERVAL = 1000;

	private long mCount;
	private long mLastReport;
	private String mName;
	private long mInterval;

	public DMSOpsCounter( String name )
	{
		mCount = 0;
		mLastReport = 0;
		mName = name;
		mInterval = REPORT_INTERVAL;
	}

	public void signalEvent()
	{
		long current = SystemClock.elapsedRealtime();
		if ( mLastReport == 0 )
			mLastReport = current;

		mCount++;

		if ( current - mLastReport > mInterval )
		{
			long diff = current - mLastReport;
			float fps = mCount / ( (float) diff / 1000.0f );
			Log.i( TAG, "OPS: counter - " + mName + ", ops - " + fps );

			mCount = 0;
			mLastReport = current;
		}
	}

	public void reset()
	{
		mCount = 0;
		mLastReport = 0;
	}
}
