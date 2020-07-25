package utils

object StringUtil {

  // color
  // Prefix_Foreground_Background
  // Prefix:
  // c_ - default color prefix (can be omitted);
  // i_ - optional prefix which indicates that foreground color should be inverted (special rules will be applied to foreground and background colors);
  // h_ - optional prefix which indicates that foreground color should be highlighted (special rules will be applied to foreground and background colors).
  def parseColor(color: String): Array[String] = {
    //FIXME 目前暂时忽略prefix
    if (color != "") {
      val res = color
        .replace("c_", "")
        .replace("i_", "")
        .replace("h_", "")
        .replace("dark_", "dark")
        .replace("light_", "light")
        .split('_')
      res.length match {
        case 1 => res :+ ""
        case 2 => res
        case _ => throw new Exception("color format error")
      }
    } else Array("", "")
  }
}
