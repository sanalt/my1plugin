/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.digimarc;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.digimarc.DMSUtils.DMSCrosshairView;
import com.digimarc.DMSUtils.DMSDebugLog;
import com.digimarc.DMSUtils.DMSErrorMsgDialog;
import com.digimarc.DMSUtils.DMSErrorMsgDialog.TYPE;
import com.digimarc.DMSUtils.DMSInactiveItem;
import com.digimarc.DMSUtils.DMSItemData;
import com.digimarc.DMSUtils.DMSListviewAdapter;
import com.digimarc.DMSUtils.DMSCache;

import com.digimarc.dms.DMSIListener;
import com.digimarc.dms.DMSManager;
import com.digimarc.dms.DMSMessage;
import com.digimarc.dms.DMSPayload;
import com.digimarc.dms.DMSQRCodeResult;
import com.digimarc.dms.DMSStatus;
import com.digimarc.dms.resolver.ResolveResult;
import com.digimarc.dms.resolver.StandardPayoff

public class DigiMarc extends CordovaPlugin, Activity implements	DMSIAudioVisualizerListener{
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext pCallbackContext) throws JSONException 
	{
		this.callbackContext = pCallbackContext;
		if (action.equals("start")) 
		{
			this.cordova.getThreadPool().execute(new Runnable()
			{
                public void run() 
                {
                    String res = "hello how are you";
                    callbackContext.success(res);
                }
            });
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
			return true;
		}
		else
		{
			callbackContext.error("socialSharing." + action + " is not a supported function. Did you mean '" + ACTION_SHARE_EVENT + "'?");
			return false;
		}
	}
	
	
	
	
	private DMSSurfaceView mSurfaceView = null;
	private Context mContext = null;
	List<DMSItemData> mItemList = new ArrayList<DMSItemData>();
	DMSListviewAdapter adapter = null;
	DMSManager dmsMgr = null;
	DMSImageSource imageSource = null;
	DMSAudioSource audioSource = null;

	// Cache the items in the history listview using the Cache utility implemented 
	// in package com.digimarc.DMSUtils.DMSCache
	final int CACHE_DEPTH = 20;
	int watermarksCount = 0;
	DMSCache<Integer, Object> resolveResultsCache = null;

	private DMSCrosshairView mCrosshairView = null;
	public static Activity mActivity = null;
	
	private String mOrigin = "unknown";

	/**
	 * DMSDK Watermark and Status Listener
	 */

	/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [watermark listener] */
	private DMSIListener watermarkListener = new DMSIListener()
	{
	/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [watermark listener] */


		@Override
		public void onStatus( int status) {
			ShowInfo(
				"DMSIListener Status",
				DMSStatus.getStatusDescription(status));
		}

		@Override
		public void onAudioWatermarkDetected( DMSMessage msg) {
			if (msg != null) {
				LogDMSMessage(msg);
				mActionListener.getResolver().resolve(
					msg.getPayload().getPayloadForResolver());
			}
		}

		@Override
		/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [detected image watermark] */
		public void onImageWatermarkDetected( DMSMessage msg) {
		/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [detected image watermark] */


		/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [resolve payload] */
			if (msg != null){
				LogDMSMessage(msg);
				mActionListener.getResolver().resolve(
					msg.getPayload().getPayloadForResolver());
			}
		/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [resolve payload] */

		}

		@Override
		public void onImageBarcodeDetected( DMSMessage msg) {
			if (msg != null) {
				LogDMSMessage(msg);
				mActionListener.getResolver().resolve(
					msg.getPayload().getPayloadForResolver());
			}
		}

		@Override
		public void onImageQRCodeDetected( DMSMessage msg) {
			if (msg != null) {
				LogDMSMessage(msg);
				addQRCodeToList(msg);
			}
		}

		@Override
		public void onError( int errorCode) {
			ShowError(
				"DMSIListener Error",
				DMSStatus.getStatusDescription(errorCode));
		}

		@Override
		public void onWarning( int warningCode) {
			
			ShowWarning(
				"DMSIListener Warning",
				DMSStatus.getStatusDescription(warningCode));
		}
	};

	public static void
		ShowError(
			String caption,
			String msg) {
		
		ShowMsg(
			TYPE.ERROR,
			caption,
			msg);
	}

	public static void ShowWarning(
			String caption,
			String msg) {
		
		ShowMsg(
			TYPE.WARNING,
			caption,
			msg);
	}

	public static void
		ShowInfo(
			String caption,
			String msg) {
		
		ShowMsg(
			TYPE.INFO,
			caption,
			msg);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void ShowMsg(
			DMSErrorMsgDialog.TYPE type,
			String title,
			String msg) {
		try {
			if ((title != null) && (title.isEmpty() == false) && (msg != null) && (msg.isEmpty() == false)) {
				DMSDebugLog.Write(
					title,
					msg);
				if ((type != TYPE.INFO) && (type != TYPE.QUESTION)) {
					DMSErrorMsgDialog dialog = new DMSErrorMsgDialog();
					dialog.mType = type;
					dialog.mTitle = title;
					dialog.mParagraph = msg;
					dialog.show(
						mActivity.getFragmentManager(),
						"dialog");
				}
			}
		}
		catch (Exception e) {
			DMSDebugLog.Write(
				"DMSDemo.ShowMsg",
				e);
		}
	}

	private void LogDMSMessage( DMSMessage msg) {
		try {
			if (msg != null) {
				mOrigin = msg.getMessageOrigin();
				if ((mOrigin == null) || (mOrigin.isEmpty() == true)) {
					mOrigin = "unknown";
				}
				String status = Integer.toString(msg.getStatus());
				DMSDebugLog.Write("LogDMSMessage\r\n    Origin: " + mOrigin + "\r\n    Status: " + status);
			}
		}
		catch (Exception e) {
			DMSDebugLog.Write(
				"DMSDemo.LogDMSMessage",
				e);
		}
	}

	
		private void addListViewItem( String description, Object value) {
		if ((description != null) && (description.isEmpty() == false) && (value != null)) {
			DMSItemData data = new DMSItemData();
			data.mOrigin = mOrigin;
			data.mDescription = description;
			data.mBitmap = DMSPreviewCallback.GetScaledPreviewImage();
			data.mValue = value;
			mItemList.add(data);
			adapter.notifyDataSetChanged();
		}
	}

	private void addQRCodeToList( DMSMessage dmsMsg) {
		try {
			if (dmsMsg != null) {
				DMSPayload payload = dmsMsg.getPayload();
				if(payload != null) {
					DMSQRCodeResult qr = payload.getQRPayloadForResolver();
					if(qr != null) {
						String payloadId = payload.getPayloadId();
						if((payloadId != null) && (payloadId.isEmpty() == false)) {
							addListViewItem(payloadId, qr);
							watermarksCount++;
							resolveResultsCache.set(
								watermarksCount,
								qr);
						}
						else {
							ShowError("DMSDemo.addQRCodeToList Error", "Empty DMS payload ID returned for that item.");
						}
					}
				}
			}
		}
		catch (Exception e) {
			DMSDebugLog.Write(
				"DMSDemo.addQRCodeToList",
				e);
		}
	}

	private void addWatermarkToList( ResolveResult resolveResult) {
		try {
			if(resolveResult != null) {
				StandardPayoff payoff = resolveResult.getStandardPayoff();
				if(payoff != null) {
					String description = payoff.getDescription();
					if((description != null) && (description.isEmpty() == false)) {
						addListViewItem(description, resolveResult);
						watermarksCount++;
						resolveResultsCache.set(
							watermarksCount,
							resolveResult);
					}
					else {
						String subtitle = payoff.getSubtitle();
						if((subtitle != null) && (subtitle.isEmpty() == false)) {
							//ShowError("DMSDemo.addWatermarkToList Error", subtitle);
							DMSInactiveItem inactiveItem = new DMSInactiveItem();
							addListViewItem(subtitle, inactiveItem);
							watermarksCount++;
							resolveResultsCache.set(
								watermarksCount,
								null);
						}
						else {
							ShowError("DMSDemo.addWatermarkToList Error", "Empty payoff description returned for that item.");
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"DMSDemo.addWatermarkToList",
				e);
		}
	}

	/**
	 * Audio visualizer
	 */
	private DMSVisualizationView mVisualizerView = null;
	private int mnScreenwidth = 0;
	private int mnScreenheight = 0;
	private Bitmap mBitmap = null;
	private Canvas mCanvas = null;
	private Paint mPresentPaint = new Paint();

	private
		void
		configVisualizer()
	{
		mVisualizerView = (DMSVisualizationView) findViewById(R.id.visualizer);
		mVisualizerView.setMinimumHeight(100);
		mPresentPaint.setStrokeWidth(3);
		mPresentPaint.setColor(0xFF33FF00);
		initializeScreenParameters();
	}

	/**
	 * Updates the audio visualizer with a buffer of data
	 * 
	 * @param b
	 *            The buffer of data
	 */
	public
		void
		updateVisualization(
			ByteBuffer b)
	{

		if (mBitmap == null)
			initializeScreenParameters();
		else
		{

			// Clear the canvas
			// https://groups.google.com/forum/#!msg/android-developers/-Y_jzFIRGy4/q9OJtvIIfJUJ

			mCanvas.drawColor(
				0,
				PorterDuff.Mode.CLEAR);
			DMSAudioVisualizer.boringOscilloscope(
				b,
				mCanvas,
				mPresentPaint,
				10,
				mnScreenwidth,
				mnScreenheight);
			// Draw the bitmap to the screen
			mVisualizerView.draw(mBitmap);
		}
	}

	/**
	 * Initialize visualizer bounds
	 */
	private void initializeScreenParameters() {
		try {
			mnScreenwidth = mVisualizerView.getWidth();
			mnScreenheight = mVisualizerView.getHeight();

			if ((mnScreenwidth != 0) && (mnScreenheight != 0)) {
				// Create the bitmap and canvas for the visualization
				if (mBitmap == null) {
					DMSDebugLog.Write("DMSDemo.initializeScreenParameters - bitmap was null, creating it");
					mBitmap = Bitmap.createBitmap(
						mnScreenwidth,
						mnScreenheight,
						Bitmap.Config.ARGB_8888);
				}

				if (mCanvas == null && mBitmap != null) {
					mCanvas = new Canvas(
						mBitmap);
				}
			}
		}
		catch (Exception e) {
			DMSDebugLog.Write(
				"DMSDemo.initializeScreenParameters",
				e);
		}
	}

	/**
	 * The PayoffActionListener defines the actions that are taken when someone
	 * selects a button in a rich template.
	 */
	private DMSPayoffListener mActionListener = null;

	@Override
	public void onCreate( Bundle savedInstanceState) {
		DMSDebugLog._appContext = this;
		DMSDebugLog._appName = "DMSDemo";
		DMSDebugLog.Write("DMSDemo.onCreate");
		try {
			super.onCreate(savedInstanceState);

			mActivity = this;

			setContentView(R.layout.main);

			/* Check if hardware has camera. Having a camera is a requirement */
			if (!deviceHasCamera()){
				AlertDialog alert = new AlertDialog.Builder(
					this).setTitle(
					"Camera Detection").setMessage(
					"No camera detected, aborting...").setPositiveButton(
					"OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick( DialogInterface dialog, int which) {
							finish(); // abort gracefully
						}
					}).create();
				alert.show();
			}

			// create camera and its surface view
			mSurfaceView = (DMSSurfaceView) findViewById(R.id.cameraView);

			mContext = this;

			mCrosshairView = (DMSCrosshairView) findViewById(R.id.crosshairView);
			if (mCrosshairView != null) {
				mCrosshairView.bringToFront();
			}

			/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [create payoff listener] */
			mActionListener = new DMSPayoffListener(
				mContext) {
			/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [create payoff listener] */


				@Override
				public void onStandardPayoffResolved( final ResolveResult result) {
					addWatermarkToList(result);
				}

			};

			// Bind the resolver service
			/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [start payoff listener] */
			mActionListener.start(this);
			/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [start payoff listener] */


			// setup visualizer
			configVisualizer();

			// create watermarks cache
			resolveResultsCache = new DMSCache( CACHE_DEPTH);

			// Assign the adapter to the list to display status list data
			ListView listView = (ListView) findViewById(R.id.listView1);
			listView.setDivider(new ColorDrawable(0x88000000));
			listView.setDividerHeight(1);
			adapter = new DMSListviewAdapter(
				this,
				mItemList);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick( AdapterView<?> adapter,
						View view,
						int position,
						long arg) {
					
					Object cacheItem = resolveResultsCache.get(position + 1);
					if (cacheItem != null) {
						if (cacheItem instanceof ResolveResult) {
							ResolveResult resolveResult = (ResolveResult) cacheItem;
							resolveStandard(resolveResult);
						}
						else if (cacheItem instanceof DMSQRCodeResult) {
							DMSQRCodeResult resolveResult = (DMSQRCodeResult) cacheItem;
							resolveQRPayoff(resolveResult);
						}
					}
				}
			});

			/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [create DMSDK] */
			// create DMSManager
			dmsMgr = DMSManager.getInstance();
			/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [create DMSDK] */


			/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [load config file] */
			// load configuration file
			String configJson = getResources().getString(
				R.string.dms_readers_config);
			dmsMgr.loadReadersConfigFromJSONString(configJson);
			/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [load config file] */


			/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [create media sources] */
			// create media sources
			imageSource = new DMSImageSource();
			audioSource = new DMSAudioSource();
			/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [create media sources] */


			audioSource.setDMSVisualizer(this);

			/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [set media source profiles] */
			// set media source profiles
			dmsMgr.setImageProfile(getProfile(true));
			dmsMgr.setAudioProfile(getProfile(false));
			/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [set media source profiles] */

			dmsMgr.reportNewDetectionsOnly(true);

			/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [open DMSDK session] */
			// open the DMSDK session
			if (!dmsMgr.openSession(
				mContext,
				watermarkListener,
				imageSource,
				audioSource))
			{
			/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [open DMSDK session] */
				ShowError(
					"Open Session Error",
					"Failed to open a DMS session.");
			}

			/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [start media sources] */
			// start media sources
			dmsMgr.startImageSource();
			dmsMgr.startAudioSource();
			/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [start media sources] */
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"DMSDemo.onCreate",
				e);
		}
	}

	/**
	 * Below are for obtaining preferences from the settings menu
	 */
	private int getListenSampleRate() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		String value = sharedPrefs.getString(
			"sample_rate_listen",
			"16000");
		return Integer.parseInt(value);
	}

	private	DMSManager.DMS_PROFILES getProfile( boolean image) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String value;

		if (image) {
			// default is LOW for image
			value = sharedPrefs.getString(
				"image_profile",
				"Low");
		}
		else {
			// default is MEDIUM for audio
			value = sharedPrefs.getString(
				"audio_profile",
				"Medium");
		}

		if (value.equals("Low")) {
			return DMSManager.DMS_PROFILES.LOW;
		}
		else if (value.equals("Medium")) {
			return DMSManager.DMS_PROFILES.MEDIUM;
		}
		else if (value.equals("High")) {
			return DMSManager.DMS_PROFILES.HIGH;
		}
		else if (value.equals("Idle")) {
			return DMSManager.DMS_PROFILES.IDLE;
		}

		return DMSManager.DMS_PROFILES.IDLE;
	}

	final int RESULT_PREFERENCES = 1;

	@Override
	protected void onActivityResult(
			int requestCode,
			int resultCode,
			Intent data) {
		
		super.onActivityResult(
			requestCode,
			resultCode,
			data);

		switch (requestCode) {
			case RESULT_PREFERENCES:
				dmsMgr.setImageProfile(getProfile(true));
				dmsMgr.setAudioProfile(getProfile(false));
				break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu) {
		MenuItem item = menu.add(
			Menu.NONE,
			0,
			0,
			"Settings...");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		if (super.onCreateOptionsMenu(menu)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				startActivityForResult(
					new Intent(
						this,
						DMSPreferenceActivity.class),
					RESULT_PREFERENCES);
				return true;
		}
		return false;

	}

	private void resolveStandard( final ResolveResult result){
		try{
			if (result != null){
				mSurfaceView.setReadingState();
				
				/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [report payoff action] */
				mActionListener.getResolver().reportAction(
					result.getStandardPayoff().getActionToken());
				/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [report payoff action] */
				/** - - - - - - - - - - - > > > - - - - - - - - - - - - - [handle payoff] */
				try {
					Intent i = new Intent(
						Intent.ACTION_VIEW);
					i.setData(Uri.parse(result.getStandardPayoff().getContent()));
					mContext.startActivity(i);
				/** - - - - - - - - - - - < < < - - - - - - - - - - - - - [handle payoff] */
				}
				catch (Exception e) {
					ShowError(
						"Resolve Standard Payoff Error",
						"Unable to resolve standard payoff.");
				}
			}
		}
		catch (Exception e) {
			DMSDebugLog.Write(
				"DMSDemo.resolveStandard",
				e);
		}
	}

	private void resolveQRPayoff( final DMSQRCodeResult result){
		try {
			Intent i = DMSQRCodeResult.intentForUri(result.uri);
			mContext.startActivity(i);
		}
		catch (Exception e) {
			ShowError(
				"Resolve QR Payoff Error",
				"Unable to resolve QR code.");
		}
	}

	/*
	 * Check if device has a back-facing camera
	 */
	private boolean deviceHasCamera(){
		boolean result = false;
		android.hardware.Camera camera;
		try {
			camera = android.hardware.Camera.open();
			camera.release();
			result = true;
		}
		catch (RuntimeException e) {
			DMSDebugLog.Write(
				"DMSDemo.deviceHasCamera",
				e);
		}
		return (result);
	}

	@Override
	protected void onResume() {
		adapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mActionListener.stop(this);

		dmsMgr.stopImageSource();
		dmsMgr.stopAudioSource();

		dmsMgr.closeSession();

		super.onDestroy();
	}

	@Override
	public void onAudioData( byte[] b){
		updateVisualization(ByteBuffer.wrap(
			b,
			0,
			b.length));
	}
}
