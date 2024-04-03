package utils

import org.apache.commons.codec.binary.Hex

object Utils {

  def hexToString(hexStr:String):String= {
    val bytes: Array[Byte] = Hex.decodeHex(hexStr.toCharArray)
    new String(bytes, "UTF-8")
  }

  def lovelaceToAda(lovelace:Long):String= {
    (lovelace.toDouble/1000000.0).toString
  }

  def getAssetNameFromUnit(unit:String):String = {
    if(unit.equalsIgnoreCase("lovelace")) "lovelace"
    else hexToString(unit.substring(56))
  }
}
