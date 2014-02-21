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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.digimarc.DMSUtils.DMSDebugLog;
import com.digimarc.dms.imagereader.Payload;
import com.digimarc.dms.resolver.PayoffActionListener;
import com.digimarc.dms.resolver.ResolveResult;
import com.digimarc.dms.resolver.ResolverService;

public class DMSPayoffListener
	extends
	PayoffActionListener
{
	private String TAG = "DMSPayoffListener";

	public
		void
		start(
			Activity activity)
	{
		try
		{
			activity.bindService(
				new Intent(
					activity,
					ResolverService.class),
				mConnection,
				Context.BIND_AUTO_CREATE);
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"DMSPayoffListener.start",
				e);
		}
	}

	public
		void
		stop(
			Activity activity)
	{
		try
		{
			mResolver.removeOnResolvedListener(mResolveListener);
			activity.unbindService(mConnection);
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"DMSPayoffListener.stop",
				e);
		}
	}

	public
		ResolverService
		getResolver()
	{
		return mResolver;
	}

	public DMSPayoffListener(
		Context context)
	{
		super(
			context);
		mContext = context;
	}

	/**
	 * The OnResolvedListener, this is where you react to resolver events such
	 * as receiving a standard payoff.
	 * 
	 * The implementations below display an alert dialog with some basic
	 * information about the resolve result.
	 */
	private ResolverService.OnResolvedListener mResolveListener = new ResolverService.OnResolvedListener()
	{
		/**
		 * An error occurred during the resolve process, most likely a
		 * credential, network or manifest permission issue. Check logcat for
		 * more details.
		 */
		@Override
		public
			void
			onError(
				Payload payload)
		{
			if (payload != null)
			{
				DMSDemo.ShowError(
					"DMS Payoff Listener, Resolve Error",
					"A network error or error in connectivity has occurred.");
			}
		}

		/**
		 * The payoff was resolved but is inactive in the Digimarc resolver
		 */
		@Override
		public
			void
			onResolvedUnknown(
				final ResolveResult result)
		{
			if (result != null)
			{
				DMSDemo.ShowError(
					"DMS Payoff Listener, Unknown Resolve Error",
					"The payload is inactive or is not registered with the Digimarc Resolver OSP.");
			}
		}

		/**
		 * A standard payoff was detected
		 */
		@Override
		/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [receive payoff] */
		public
			void
			onResolvedWithPayoff(
				final ResolveResult result)
		{
			onStandardPayoffResolved(result);
		/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [receive payoff] */
		}
	};

	/**
	 * The OnReportActionListener, this is where you can react to reportAction
	 * events
	 */
	private ResolverService.OnReportActionListener mReportActionListener = new ResolverService.OnReportActionListener()
	{

		/**
		 * Report action was successful
		 */
		@Override
		public
			void
			onSuccess(
				String actionToken)
		{
			if (actionToken != null)
			{
				DMSDemo.ShowInfo(
					"DMSPayoffListener,  Report Action",
					"\"" + actionToken + "\" succeeded.");
			}
		}

		/**
		 * Report action failed
		 */
		@Override
		public
			void
			onFailed(
				String actionToken,
				int httpStatusCode)
		{
			if (actionToken != null)
			{
				DMSDemo.ShowError(
					"DMSPayoffListener,  Report Action",
					"\"" + actionToken + "\" failed, status code \"" + Integer.toString(httpStatusCode) + "\".");
			}
		}
	};

	/**
	 * The service connection for the resolver. This is where: * The resolver
	 * service is initialized * The OnResolvedListener is registered * The
	 * OnReportActionListener is registered * The surfaceview is discovered and
	 * told about the resolver and watermarkReadListener (not required but
	 * specific to this example).
	 */
	private ServiceConnection mConnection = new ServiceConnection()
	{
		@Override
		public
			void
			onServiceConnected(
				ComponentName className,
				IBinder service)
		{
			try
			{
				/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [create resolver] */
				mResolver = ((ResolverService.ResolverBinder) service).getService();
				/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [create resolver] */

				/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [supply resolver credentials] */
				// Initialize the resolver with username and password
				mResolver.init(
					DMSCredentials.DMRESOLVER_USERNAME,
					DMSCredentials.DMRESOLVER_PASSWORD);
				/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [supply resolver credentials] */

				/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [register listeners] */
				// Set the onResolved listener and reportAction listener
				mResolver.setOnReportActionListener(mReportActionListener);
				mResolver.setOnResolvedListener(mResolveListener);
				/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [register listeners] */
			}
			catch (Exception e)
			{
				DMSDebugLog.Write(
					"DMSPayoffListener.ServiceConnection.onServiceConnected",
					e);
			}
		}

		@Override
		public
			void
			onServiceDisconnected(
				ComponentName className)
		{
			// Make sure that the resolver is destroyed
			mResolver = null;
		}
	};
}
