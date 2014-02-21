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
 
***************************************************************************************************/

package com.digimarc.DMSDemo;

import java.io.IOException;
import java.util.List;

import android.annotation.TargetApi;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.digimarc.dms.imagereader.DMSReaderCpm;

public final class DMSCameraWrapper {
	private static final int CAM_BUFFERS = 5;

    private int mPreviewFormat;
    private Size mPreviewSize;
    private boolean mPreviewing;
    private boolean mAutoFocusing;
    private boolean mTorchPending = false;
    private boolean mSetTorchMode = false;
    private static int mCurrentCameraOrientation = -1;
    private static ViewGroup mSurface = null;

    private Camera mCamera;
    private static DMSCameraWrapper mThis = null;
    private Handler mAutoFocusHandler;
  
    private final DMSPreviewCallback mPreviewCallback;
    private final AutoFocusCallback mAutoFocusCallback;

    private Size mOptimalPreviewSize = null;

    private int mAutoFocusRestartMessage;
    private int mAutoFocusCompleteMessage;

    private static int AUTOFOCUS_SLEEPTIME = 2000;

//    private byte[] mPreviewBuffer = null;
    private PixelFormat mPixelFormat = null;
    
    private DMSReaderCpm dmsReader = new DMSReaderCpm();
    
    private static final String TAG = "CameraWrapper"; 
    public static DMSCameraWrapper get() {
        return mThis;
    }

    public static void create() {
        if (mThis == null) {
            mThis = new DMSCameraWrapper();
        }
    }

    public void cancelAutoFocus() {
    	Log.i(TAG,"cancelAutoFocus" );
    	
        if (mCamera == null || !mAutoFocusing)
            return;

        // Cancelling auto focus causes problems on Android 2.3.  Catching autofocus cancel exception seems to help.
        try 
        {
        	mCamera.cancelAutoFocus();
        }
        catch(Exception ex)
        {
        }
    }

    /**
     * Sets the rotation of the camera to match that of the screen.
     * Handles the API differences for setting camera parameters
     * while preview is active.  Is smart enough to not change orientation
     * if it isn't needed.
     * @param width The width of the layout
     * @param height The height of the layout
     * @param rotation The current rotation of the screen
     */
    @TargetApi(9)
    public void setRotationToMatchScreen(int width, int height, int rotation){
        
    	if(mCamera == null)
    		return;
    	
        //Convert the rotation enum to the actual number of degrees
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        /*
         * Unfortunately we can't have a 100% working solution on anything < API 9.
         * For instance there are some devices where the back facing camera is mounted
         * in landscape mode, so holding the device in portrait requires a 90 degree
         * rotate.  On the bright side this side-effect has always existed in our product.
         * 
         * API 9 gives you tools to query how the camera is mounted so you can make the
         * compensation.  With API 8, all we can do is pray that the camera is mounted 
         * at 0 degrees.
         */
        int finalCameraRotation;
        if(android.os.Build.VERSION.SDK_INT > 8){
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(0, cameraInfo);
            
            finalCameraRotation = (cameraInfo.orientation - degrees + 360) % 360;
//            Log.i(TAG, "Rotation before: " + degrees + " rotation after: " + finalCameraRotation);
        }
        else{
            finalCameraRotation = 90;
        }
        
        //If rotation is actually needed
        if(mCurrentCameraOrientation == -1 || finalCameraRotation != mCurrentCameraOrientation){
            //If API is > 13 then we can change the camera orientation while preview
            //is active.
            if(android.os.Build.VERSION.SDK_INT > 13){
                mCamera.setDisplayOrientation(finalCameraRotation);
            }
            //Otherwise, we have to stop preview
            else{
            	mPreviewing = false;
                mCamera.stopPreview();
                mCamera.setDisplayOrientation(finalCameraRotation);
                startPreview();
                
                if(mAutoFocusCallback != null)
                	mCamera.autoFocus(mAutoFocusCallback);
            }

            //Store the final rotation value for later comparison
            mCurrentCameraOrientation = finalCameraRotation;
        }
        
        //Update the aspect ratio of the surfaceview
        //dor setCameraPreviewLayout(width, height);
    }
    
    DMSCameraWrapper() {
        mCamera                 = null;
        mPreviewing             = false;
        mAutoFocusing           = false;

        mPreviewCallback        = new DMSPreviewCallback();
        mAutoFocusCallback      = new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (mAutoFocusHandler == null)
                    return;

                mAutoFocusing = false;
                commitCameraParameter();

                // Immediately send an autofocus complete to the handler, followed by a
                // delayed messaged to restart autofocus.
                Message completeMsg = mAutoFocusHandler.obtainMessage(mAutoFocusCompleteMessage, success);
                mAutoFocusHandler.sendMessage(completeMsg);

                Message restartMsg = mAutoFocusHandler.obtainMessage(mAutoFocusRestartMessage);
                mAutoFocusHandler.sendMessageDelayed(restartMsg, AUTOFOCUS_SLEEPTIME);
                mAutoFocusHandler = null;
            }
        };
    }

    //Determines if the handset supports torch and if Digimarc supports torch on this device
    public boolean isTorchSupported() {
        final Camera.Parameters params = mCamera.getParameters();
        List<String> flashModes = params.getSupportedFlashModes();

        if (flashModes == null)//Flash isn't supported on this handset
            return false;

        if (!flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) //Torch mode isn't supported.
            return false;
                
        return DMSTorchBlacklist.isSupported(Build.MODEL); //Torch is on Digimarc's buggy hardware list.
    }

    private void setAutoFocusHandler(Handler handler) {
        mAutoFocusHandler = handler;
    }

    /**
     * Iterates through the parameter queue and commits each parameter change
     */
    private void commitCameraParameter() {
        /*
         * This is the simple solution for now.  This should use a bitmap or enum to set
         * the list of pending options that need changing.
         */
        if(mTorchPending && !mAutoFocusing) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();

                if(mSetTorchMode)
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                else
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                mCamera.cancelAutoFocus();
                mCamera.setParameters(parameters);
                /*
                 * This message is sent any time a successful torch transition is made
                 * i.e. on -> off, off -> on.  Originally it was going to reflect whether
                 * the change was successful or not, now, if the parameter change fails, the
                 * application crashes since something really wrong is going on.
                 *
                 * Apart from this, to determine whether the torch was just set on or off
                 * successfully would require tracking torch status and user intent in this
                 * class.  So the simple solution is to send the message any time a successful
                 * transition is made.  The UI will crash rather than getting out of sync.
                 *
                 *  The check for null is to satisfy the state of the open() call where the
                 *  mUIHandler has not yet been set.
                 */
                mTorchPending = false;
            }
            catch (RuntimeException e) {
            	Log.e(TAG, "Failed to set camera parameters");
                e.printStackTrace();
            }
        }
    }

    /**
     * opens the camera and resizes the camera surfaceview.
     * 
     * @param surfaceHolder The surfaceholder for live camera preview
     * @param screenWidth Device screen width.
     * @param screenHeight Device screen height.
     * @param surface The parent element that contains the camera view
     * @param rotation The screen orientation from getWindowManager().getDefaultDisplay().getRotation() 
     * 
     * @throws IOException
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void open(SurfaceHolder surfaceHolder, int screenWidth, int screenHeight, ViewGroup surface, int rotation ) throws IOException {

        mSurface = surface;

        if (mCamera != null) {
            Log.e(TAG, "Attempting to open camera that is already open.");
            return;
        }


        try{
            mCamera = Camera.open();
            if(mCamera==null)
            	mCamera = Camera.open(0);
        } catch (RuntimeException ex){
            Log.e(TAG, "Couldn't acquire camera");
            throw new IOException();
        }

        Camera.Parameters parameters = dmsReader.getBestParameters(mCamera.getParameters());

        /**
         * We are using mOptimalPreviewSize here because of bug #301
         * When the power button is pressed, onPause is called followed
         * by the Android OS calling onSurfaceChanged with swizzled width/height
         * parameters.
         *
         * We know that the surface is the correct size on the first run of the app
         * so here we are storing those correct values to use on subsequent calls.
         */
        if(mOptimalPreviewSize == null){
        	// Our call above to getBestCameraParameters has already picked the preview size, so all we need to do is store it.
        	mOptimalPreviewSize = parameters.getPreviewSize();
        }

        if (mOptimalPreviewSize != null){
            parameters.setPreviewSize(mOptimalPreviewSize.width, mOptimalPreviewSize.height);
        }

        //For 2.2 and later, set pixel format
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1){
            mPixelFormat = new PixelFormat();
            PixelFormat.getPixelFormatInfo(parameters.getPreviewFormat(), mPixelFormat);
        }

        mCamera.setParameters(parameters);

        //It's important that we set camera parameters before calling this for the first time
        setRotationToMatchScreen(screenWidth, screenHeight, rotation);

        mCamera.setPreviewDisplay(surfaceHolder);

        parameters = mCamera.getParameters();
        mPreviewFormat = parameters.getPreviewFormat();
        mPreviewSize = parameters.getPreviewSize();
    }

    /**
     * Calculate the perfect aspect ratio to fit the live camera preview on the screen
     * @param surfaceWidth width of the surface that live preview frames are drawn to
     * @param surfaceHeight height of the surface that live preview frames are drawn to
     * @return Point containing x = width, y = height of the perfect AR
     */
    private static Point calculateAR(int surfaceWidth, int surfaceHeight){
    	//Get the size of the preview
        Size cameraPreviewSize = DMSCameraWrapper.get().getParameters().getPreviewSize();
        int cameraWidth = cameraPreviewSize.width;
        int cameraHeight = cameraPreviewSize.height;
        
        int finalWidth, finalHeight;
        
        //Are we landscape?
        boolean inLandscape = surfaceWidth > surfaceHeight;
        
        if (inLandscape){
            //Calculate the new width based on the aspect ratio of the camera preview
            //new height = Aspect Ratio * screen width
            finalWidth = (int) (((float)cameraWidth / (float)cameraHeight) * (float)surfaceHeight);
            finalHeight = surfaceHeight;
            
            if(finalWidth > surfaceWidth){
                int newHeight;
                
                finalWidth = surfaceWidth;
                newHeight = (int) (((float)cameraHeight / (float)cameraWidth) * surfaceWidth);
                finalHeight = newHeight;
                
                if(finalHeight > newHeight)
                    return null;
            }
        }
        else{
            //Get the original height of the camera surface
            int originalHeight = surfaceHeight;
            
            //Calculate the new height based on the aspect ratio of the camera preview
            //new height = Aspect Ratio * screen width
            int correctedSurfaceViewHeight = (int) (((float)cameraWidth / (float)cameraHeight) * (float)surfaceWidth);
            finalHeight = correctedSurfaceViewHeight;
            finalWidth = surfaceWidth;
            
            //If the corrected height is greater than the original height then that means
            //the image won't fit on the screen.  Instead of fitting height, fit the width
            if(correctedSurfaceViewHeight > originalHeight){
                //Oops, the camera has a funky aspect ratio so we now have to adjust the width instead
                finalHeight = originalHeight;
                int newWidth = (int) (((float)cameraHeight / (float)cameraWidth) * (float) originalHeight);
                finalWidth = newWidth;
                
                //?!? for cameras that support the walking stick ratio
                if(newWidth > surfaceWidth){
                    Log.e(TAG, "The camera has a walking stick ratio!?");
                    return null;
                }
            }
        }
        
        return new Point(finalWidth, finalHeight);
    }
    
    /**
     * Update the camera preview layout so it doesn't look like a funhouse mirror
     * @param maxSurfaceWidth The maximum surface width
     * @param maxSurfaceHeight The maximum surface height
     */
    private static void setCameraPreviewLayout(int maxSurfaceWidth, int maxSurfaceHeight) { 
        
    	if(DMSCameraWrapper.get().getParameters() == null)
    		return;
    	
    	Point size = calculateAR(maxSurfaceWidth, maxSurfaceHeight);
    	if(size == null){
    		Log.e(TAG, "Aspect ratio calculation failed!");
    		return;
    	}

    	int gravity = ( maxSurfaceHeight == size.y ) ? Gravity.CENTER : Gravity.TOP;

    	FrameLayout.LayoutParams cameraSurfaceLayoutParams = new FrameLayout.LayoutParams(size.x, size.y, gravity);
        mSurface.setLayoutParams(cameraSurfaceLayoutParams);
    }

    public void close() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            mCurrentCameraOrientation = -1;
        }
    }

    public void startPreview() {
        if (mCamera == null || mPreviewing) {
            return;
        }
        
        mCamera.startPreview();
        mPreviewing = true;
    }

    public void getPreviewFrame(Handler handler, int message) {
        if (mCamera == null || !mPreviewing)
            return;

        mPreviewCallback.setPreviewData(mPreviewFormat, mPreviewSize);

        /* For 2.2 and later, provide a buffer to use for preview data
         * Google's original implementation before 2.2 for live camera preview frames was 
         * to allocate a byte array for each new frame.  This causes a garbage collection 
         * to occur for every single preview frame that we process.  On the HTC Vivid, capturing 
         * preview frames at 12 fps leads to a total GC penalty of 342 ms every second!
         * 
         * In Android 2.2 and later, Google provides a way to specify a static buffer that will 
         * be re-used when capturing preview frames.  On the HTC Vivid this provides a 1.8 fps 
         * increase, not bad!  It also eliminates the frivolous allocation/GC cycle which leads 
         * to a safer and more predictable memory footprint for the app.
         */

        int bufferSize = (mPreviewSize.width * mPreviewSize.height * mPixelFormat.bitsPerPixel) / 8;
        for ( int i = 0; i < CAM_BUFFERS; i++ )
        {
        	byte[] buffer = new byte[ bufferSize ];
        	mCamera.addCallbackBuffer(buffer);
        }
        mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
    }

    public void bufferUseComplete( byte[] buffer )
    {
    	if ( mCamera != null && mPreviewing )
    	{
    		if ( buffer != null )
    			mCamera.addCallbackBuffer(buffer);
    	}
    }

    public void autoFocus(Handler handler) {
        if (mCamera != null && mPreviewing && mAutoFocusCallback != null) {
        	
        	if (hasContinousMode()) {
        		return;
        	}
        	
            setAutoFocusHandler(handler);

            mAutoFocusing = true;
            
            /* Prevent a failed autofocus from crashing the app.  The Galaxy Nexus
             * in particular seems to throw a runtime exception occasionally when
             * autofocusing after a successful watermark read.
             * 
             * ICS has many more focus modes available and this is an old way of
             * doing things so it's possible that we're just stretching an old
             * autofocus model too far.  Also quite possible that 4.0.2 is just
             * buggy.
             * 
             * Catching the exception isn't a big deal, the user doesn't notice the 
             * missed autofocus and from my testing it doesn't appear to ever have 
             * consecutive failures. 
             */
            try{
                mCamera.autoFocus(mAutoFocusCallback);
            } catch (RuntimeException e){
                Log.e("CameraWrapper", "autofocus failed!");
            }
        }
    }

    public void stopPreview() {
        if (mCamera != null && mPreviewing) {
            mPreviewing = false;

            cancelAutoFocus();
            
            mCamera.stopPreview();

            // The following call will cause the buffer pool to be flushed
            mCamera.setPreviewCallback(null);

            setAutoFocusHandler(null);
            
            mSetTorchMode = false;
            mTorchPending = false;
        }
    }

    public Camera.Parameters getParameters() {
        if (mCamera != null) {
            return mCamera.getParameters();
        }
        return null;
    }

    /**
     * Enables/Disables the torch as long as the hardware supports torch mode
     * @param state Whether to enable or disable the torch
     */
    public void setTorch(boolean state) {
        
        //Only commit the camera parameter if the state is different
        if(mSetTorchMode != state){
            mSetTorchMode = state;
            mTorchPending = true;

            commitCameraParameter();
        }
    }
    
    /**
     * Check if camera has continous mode capability
     */
    public boolean hasContinousMode() {
       	if (mCamera != null ) {
    		String focusMode = getParameters().getFocusMode();
  
    		if(focusMode != null && !focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
    			return true;
    		}
    	}
    	return false; 
    }
}
