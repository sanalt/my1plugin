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

public class DigiMarc extends CordovaPlugin{
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
}
