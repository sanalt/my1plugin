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

import java.io.IOException;

import com.digimarc.DMSDemo.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;


public class DMSSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private String TAG = "DigimarcSurfaceView";
       
    //Camera related definitions
    private SurfaceHolder mHolder = null;
    private DMSFocusHandler mFocusHandler = null;
      
    //State related definitions
    public static enum ReadState{ Initializing, Reading, Resolving };
    private ReadState mState = ReadState.Initializing;
    private DMSSurfaceView mContext;
    
    @SuppressWarnings("deprecation")
	public DMSSurfaceView(Context context, AttributeSet a) {
        super(context, a);
        mContext = this;
        
        //Get the surfaceholder and add a callback to itself
        //note that this class implements SurfaceHolder.Callback
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        DMSCameraWrapper.create();
    }
    
     
    /**
     * Sets the state to reading.  Meant to be called by RichPayoffSample after a successful resolve
     * to allow watermarks to be read again
     */
    public void setReadingState(){
        mState = ReadState.Reading;
    }
   
    public void setState(ReadState state) {
    	mState = state;
    }
   
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
     	
    	 ViewGroup parent = (ViewGroup)this.getParent();
         
    	 // NOTE to developer:  getOrientation() is only implemented in version 11 and above.
    	 int rotation = 0;
    	 if(android.os.Build.VERSION.SDK_INT >= 11){
    	       rotation = (int)getRotation();
    	 }
    	 
         try {
			DMSCameraWrapper.get().open(holder, parent.getWidth(), parent.getHeight(), parent, rotation);
		} catch (IOException e) {
			 Log.e(TAG, "failed to connect Camera", e);
			e.printStackTrace();
		}
		
        DMSCameraWrapper.get().startPreview();
        DMSCameraWrapper.get().getPreviewFrame(null, 0);
        
        mFocusHandler = new DMSFocusHandler(mContext);
        DMSCameraWrapper.get().autoFocus(mFocusHandler);   
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 
        mState = ReadState.Reading;
    }
  
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Clean the camera up
        mFocusHandler.removeMessages(R.id.autofocus_message);
 
        DMSCameraWrapper.get().stopPreview();
        DMSCameraWrapper.get().close();
    }

    public void doAutoFocus(){
    	if (!DMSCameraWrapper.get().hasContinousMode() ) {
    		DMSCameraWrapper.get().autoFocus(mFocusHandler);
    	} 
    }
}
