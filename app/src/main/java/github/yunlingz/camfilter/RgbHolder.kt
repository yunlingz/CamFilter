package github.yunlingz.camfilter

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer

class RgbHolder(private val mRgbBmp: Bitmap) {
    private val mWidth = mRgbBmp.width
    private val mHeight = mRgbBmp.height
    private var mRgb: ByteArray? = null

    private fun toRgbByteArray(): ByteArray {
        if (mRgb != null) {
            return mRgb as ByteArray
        }
        mRgb = ByteArray(3 * mWidth * mHeight)
        val buffer = ByteBuffer.allocate(3 * mWidth * mHeight)
        for (x in 0..mHeight) {
            for (y in 0..mWidth) {
                val col = mRgbBmp.getPixel(y, x)
                buffer.putInt(Color.red(col))
                buffer.putInt(Color.green(col))
                buffer.putInt(Color.blue(col))
            }
        }
        return mRgb as ByteArray
    }
}

