package github.yunlingz.camfilter

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import java.lang.Exception

class RtSurfaceView(val mContext: Context) : SurfaceView(mContext), SurfaceHolder.Callback {
  private val mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
  private var mCamera: Camera? = null

  init {
    holder.addCallback(this)
    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    holder.setKeepScreenOn(true)
  }

  private fun stopCamera() {
    mCamera?.stopPreview()
    mCamera?.release()
  }

  private fun startCamera() {
    try {
      mCamera = Camera.open(mCameraId)
    } catch (e: Exception) {
      Log.e("CAM_DEBUG", "$e")
      e.printStackTrace()
      Toast.makeText(mContext, "failed to open camera", Toast.LENGTH_SHORT).show()
    }

    val params = mCamera?.parameters;
    if (params?.supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
      params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
    }
    params?.previewFormat = ImageFormat.NV21
    params?.pictureFormat = ImageFormat.JPEG
    mCamera?.parameters = params
    try {
      mCamera?.setPreviewDisplay(holder)
      mCamera?.setDisplayOrientation(90)
      mCamera?.startPreview()
    } catch (e: Exception) {
      Log.e("CAM_DEBUG", "$e")
      e.printStackTrace()
      Toast.makeText(context, "failed to start camera", Toast.LENGTH_SHORT).show()
      stopCamera()
    }
  }

  override fun surfaceCreated(holder: SurfaceHolder) {
    startCamera()
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    stopCamera()
  }


}