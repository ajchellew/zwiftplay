using ZwiftPlayConsoleApp.Utils.BitConverter;

namespace ZwiftPlayConsoleApp.Utils;
public class Utils
{
    public static string ByteArrayToStringHex(byte[] bytes)
    {
        var hexValue = EndianBitConverter.ToString(bytes);
        return hexValue.Replace("-", " ");
    }
}
