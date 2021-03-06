package github.yunlingz.camfilter

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.renderscript.*
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eclipsesource.v8.*
import github.yunlingz.camfilter.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
  private lateinit var binding: ActivityMainBinding
  private lateinit var surfaceView: SurfaceView
  private lateinit var surfaceHolder: SurfaceHolder
  private lateinit var camera: Camera
  private lateinit var surfaceTexture: SurfaceTexture


  private lateinit var runtime: V8


  private var yuvToBitmapConvertor: YuvToBitmapConvertor? = null

  inner class YuvToBitmapConvertor(
    private val mWidth: Int,
    private val mHeight: Int
  ) {
    private val mArraySize = mWidth * mHeight * 3 / 2
    private val mRs = RenderScript.create(this@MainActivity)

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private val mScript = ScriptIntrinsicYuvToRGB.create(
      mRs, Element.U8_4(mRs)
    )
    private val mYuvType: Type.Builder = Type.Builder(mRs, Element.U8(mRs))
      .setX(mArraySize)
    private val mMemIn = Allocation.createTyped(
      mRs, mYuvType.create(), Allocation.USAGE_SCRIPT
    )
    private val mRgbaType: Type.Builder = Type.Builder(mRs, Element.RGBA_8888(mRs))
      .setX(mWidth)
      .setY(mHeight)
    private val mMemOut = Allocation.createTyped(
      mRs, mRgbaType.create(), Allocation.USAGE_SCRIPT
    )

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun convert(yuvByteArray: ByteArray): Bitmap {
      mMemIn.copyFrom(yuvByteArray)
      mScript.setInput(mMemIn)
      mScript.forEach(mMemOut)
      val bitmap = Bitmap.createBitmap(
        mWidth, mHeight, Bitmap.Config.ARGB_8888
      )
      mMemOut.copyTo(bitmap)
      return bitmap
//            val buffer = ByteBuffer.allocate(bitmap.rowBytes * bitmap.height)
//            bitmap.copyPixelsToBuffer(buffer)
//            val result = buffer.array()
////            Log.d("SHOW_BYTES", "${result.size}")
//            //        memOut.copyTo(bitmap)
//            return result

//            val stream = ByteArrayOutputStream()
//
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//
//            val byteArray = stream.toByteArray()
//
//            bitmap.recycle()
//
//
//            val another = getByteArrayFromByteBuffer(mMemOut.byteBuffer)
//            Log.d("BYTE_EQUAL", "${another.contentEquals(byteArray)}")
//            return byteArray
//            val buffer = ByteBuffer.allocate(4 * mHeight * mWidth)
//            bitmap.copyPixelsToBuffer(buffer)

//            val stream = ByteArrayOutputStream()
//            image.compressToJpeg(Rect(0, 0, width, height), 50, stream)
//            return stream.toByteArray()
//            Log.d("BYTE_SIZE", "${mMemOut.byteBuffer.remaining()}")
//            Log.d("BYTE_SIZE", "${mHeight * mWidth * 4}")
//            return getByteArrayFromByteBuffer(mMemOut.byteBuffer)
    }
  }


  private fun getByteArrayFromByteBuffer(byteBuffer: ByteBuffer): ByteArray {
    val bytesArray = ByteArray(byteBuffer.remaining())
    byteBuffer.get(bytesArray)
    return bytesArray
  }

  private fun Bitmap.rotate(): Bitmap {
    val matrix =
      Matrix().apply {
        postScale(1F, -1F, width / 2F, height / 2F);
        postRotate(270F);
      }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
  }

  private fun rgbaToBitmap(rgba: ByteArray): Bitmap? {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    Log.d("BYTE_SIZE", "0: ${rgba.size}")
    var bitmap: Bitmap? = null
    try {
      val options = BitmapFactory.Options()
      options.inPreferredConfig = Bitmap.Config.ARGB_8888
      bitmap = BitmapFactory.decodeByteArray(rgba, 0, rgba.size, options)
      //    bitmap =  BitmapFactory.decodeByteArray(rgba, 0, rgba.size, options)
    } catch (e: java.lang.Exception) {
      Log.d("BYTE_SIZE", "${e.printStackTrace()}")
    }
    return bitmap
  }

//    private fun ByteArray.toBitmap(): Bitmap {
////        val options = BitmapFactory.Options()
////        options.inPreferredConfig = Bitmap.Config.ARGB_8888
////        val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size, options)
//        Log.d("BYTE_SIZE", "${this.size}")
//        val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
//        return bitmap.rotate()
//    }


  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  fun yuv420ToBitmap(yuvByteArray: ByteArray, width: Int, height: Int): ByteArray {
    val rs = RenderScript.create(this)
    val script = ScriptIntrinsicYuvToRGB.create(
      rs, Element.U8_4(rs)
    )

    // Refer the logic in a section below on how to convert a YUV_420_888 image
    // to single channel flat 1D array. For sake of this example I'll abstract it
    // as a method.
    val yuvType: Type.Builder = Type.Builder(rs, Element.U8(rs))
      .setX(yuvByteArray.size)
    val memIn = Allocation.createTyped(
      rs, yuvType.create(), Allocation.USAGE_SCRIPT
    )
    val rgbaType: Type.Builder = Type.Builder(rs, Element.RGBA_8888(rs))
      .setX(width)
      .setY(height)
    val memOut = Allocation.createTyped(
      rs, rgbaType.create(), Allocation.USAGE_SCRIPT
    )

    // The allocations above "should" be cached if you are going to perform
    // repeated conversion of YUV_420_888 to Bitmap.
    memIn.copyFrom(yuvByteArray)
    script.setInput(memIn)
    script.forEach(memOut)
    val bitmap = Bitmap.createBitmap(
      width, height, Bitmap.Config.ARGB_8888
    )
    val buffer = ByteBuffer.allocate(4 * height * width)
    bitmap.copyPixelsToBuffer(buffer)
    //        memOut.copyTo(bitmap)
    return getByteArrayFromByteBuffer(buffer)
  }

  private fun nv21ByteArrayToJpeg(nv21: ByteArray, width: Int, height: Int): ByteArray {
//    var bitmap: Bitmap? = null
//    try {
//      val image = YuvImage(nv21, ImageFormat.NV21, width, height, null)
//      val stream = ByteArrayOutputStream()
//      image.compressToJpeg(Rect(0, 0, width, height), 50, stream)
//      stream.toByteArray()
//
//
//      val options = BitmapFactory.Options()
//      options.inPreferredConfig = Bitmap.Config.ARGB_8888
//      bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size(), options)
//
//      // rotation
//      val m = Matrix()
//      m.postScale(1F, -1F, width / 2F, height / 2F);
//      m.postRotate(270F)
//      bitmap = Bitmap.createBitmap(
//        bitmap, 0, 0, bitmap.width, bitmap.height,
//        m, true
//      )
//
//      stream.close()
//    } catch (e: IOException) {
//      e.printStackTrace()
//    }
//    return bitmap
    val image = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val stream = ByteArrayOutputStream()
    image.compressToJpeg(Rect(0, 0, width, height), 50, stream)
    return stream.toByteArray()
  }

//  private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
//    val byteCnt = bitmap.byteCount
//    val buffer = ByteBuffer.allocate(byteCnt)
//    val bytes = ByteArray(byteCnt)
//    bitmap.copyPixelsToBuffer(buffer)
//
//    buffer.rewind()
//    buffer.get(bytes)
//    return bytes
//  }

  private fun jpegByteArrayToBitmap(byteArray: ByteArray, width: Int, height: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

    // rotation
    val m = Matrix()
    m.postScale(1F, -1F, width / 2F, height / 2F);
    m.postRotate(270F)
    return Bitmap.createBitmap(
      bitmap, 0, 0, bitmap.width, bitmap.height,
      m, true
    )
  }

//  private fun nv21ToBitmap(nv21: ByteArray, width: Int, height: Int): Bitmap? {
//    var bitmap: Bitmap? = if (nv21.isNotEmpty()) {
//      BitmapFactory.decodeByteArray(nv21, 0, nv21.size)
//    } else {
//      null
//    }
//
//
//    if (bitmap != null) {
//      // rotation
//      val m = Matrix()
//      m.postScale(1F, -1F, width / 2F, height / 2F);
//      m.postRotate(270F)
//      bitmap = Bitmap.createBitmap(
//        bitmap, 0, 0, bitmap.width, bitmap.height,
//        m, true
//      )
//    }
//
//    return bitmap
//  }

  private fun stopCamera() {
    camera.setPreviewCallback(null)
    camera.stopPreview()
    camera.release()
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private fun startCamera() {
    requestPermission()

    try {
      camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)

    } catch (e: Exception) {
      Log.e("CAM_DEBUG", "$e")
      e.printStackTrace()
      Toast.makeText(this, "failed to open camera", Toast.LENGTH_SHORT).show()
      finish()
    }

    camera.setPreviewCallback { bytes, _ ->
      val width = camera.parameters.previewSize.width
      val height = camera.parameters.previewSize.height
      if (yuvToBitmapConvertor == null) {
        yuvToBitmapConvertor = YuvToBitmapConvertor(width, height)
      } else {
        //            Log.d("GET_BYTE", "${bytes.size}")

        // send bytes to js
        val newBytes = jsBridgeByString(bytes)

        val canvas = surfaceHolder.lockCanvas()
//      canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
        val rgbBmp = yuvToBitmapConvertor?.convert(newBytes)?.rotate()
        if (rgbBmp != null) {
          canvas.drawBitmap(rgbBmp, 0f, 0f, null)
        }
        surfaceHolder.unlockCanvasAndPost(canvas)
      }

    }

    val params = camera.parameters;
    if (params.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
      params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
    }
    params.previewFormat = ImageFormat.NV21
    params.pictureFormat = ImageFormat.JPEG
//    params.pictureFormat = ImageFormat.NV21
    camera.parameters = params
    try {
      camera.setPreviewTexture(surfaceTexture)
      camera.startPreview()
    } catch (e: Exception) {
      Log.e("CAM_DEBUG", "$e")
      e.printStackTrace()
      Toast.makeText(this, "failed to start camera", Toast.LENGTH_SHORT).show()
      stopCamera()
      finish()
    }
  }


  private fun requestPermission() {
    val requestCode = 1

    if (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
      ) != PackageManager.PERMISSION_GRANTED
    ) {

      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.CAMERA),
        requestCode
      )
    }
  }

  private fun jsBridgeByString(bytes: ByteArray): ByteArray {
    val cSet = charset("windows-1252")
    val passStr = String(bytes, cSet)
    return passStr.toByteArray(cSet)
  }

  private fun jsFilter(bytes: ByteArray): ByteArray {
//    Log.d("BYTE_SHAPE", "${bytes.size}")
    val buffer = V8ArrayBuffer(runtime, bytes.size)
    buffer.put(bytes)

    val jsArray = V8TypedArray(runtime, buffer, V8Value.BYTE, 0, bytes.size)

    // TODO: load webgl functions
    runtime.executeScript("var canvas = document.createElement('canvas');")

    val result = jsArray.getBytes(0, jsArray.length())

    jsArray.close()
    buffer.close()
    return result
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    // surface MVC
    surfaceView = binding.displaySurface
    surfaceHolder = surfaceView.holder
    surfaceHolder.addCallback(this)
    surfaceTexture = SurfaceTexture(2)
    runtime = V8.createV8Runtime()

    // display root
    setContentView(binding.root)

//    Toast.makeText(this, "INIT camera", Toast.LENGTH_SHORT).show()

  }

//  override fun onResume() {
//    super.onResume()
//    startCamera()
//  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun surfaceCreated(holder: SurfaceHolder) {
    startCamera()
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    stopCamera()
  }

  override fun onDestroy() {
    super.onDestroy()
    runtime.release(true)
  }

}