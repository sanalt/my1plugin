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
