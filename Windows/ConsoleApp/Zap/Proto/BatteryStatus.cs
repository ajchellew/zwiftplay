namespace ZwiftPlayConsoleApp.Zap.Proto;
public class BatteryStatus
{
    public int Level { get; set; }

    public BatteryStatus(byte[] messageBytes)
    {
    }
}