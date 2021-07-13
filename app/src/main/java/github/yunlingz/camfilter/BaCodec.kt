package github.yunlingz.camfilter

object BaCodec {
  fun encode(byteArray: ByteArray): String {
    var result = CharArray(byteArray.size * 2)
    byteArray.forEachIndexed { i, b ->
      val iVa = (b.toInt()) and 0xFF
      var tmpStr = String.format("%02X", iVa)
      result[i * 2] = tmpStr[0]
      result[i * 2 + 1] = tmpStr[1]
    }
    return String(result)
  }

  fun decode(string: String): ByteArray {
    TODO("to implement")
  }
}