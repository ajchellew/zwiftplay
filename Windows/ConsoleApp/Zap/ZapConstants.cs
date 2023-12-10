namespace ZwiftPlayConsoleApp.Zap;

public class ZapConstants
{
    public const int ZWIFT_MANUFACTURER_ID = 2378; // Zwift, Inc
    public const byte RC1_LEFT_SIDE = 3;
    public const byte RC1_RIGHT_SIDE = 2;

    public static readonly byte[] RIDE_ON = { 82, 105, 100, 101, 79, 110 };

    // these don't actually seem to matter, its just the header has to be 7 bytes RIDEON + 2
    public static readonly byte[] REQUEST_START = { 0, 9 }; //byteArrayOf(1, 2)
    public static readonly byte[] RESPONSE_START = { 1, 3 };// from device

    // Message types received from device
    public const byte CONTROLLER_NOTIFICATION_MESSAGE_TYPE = 7;
    public const byte EMPTY_MESSAGE_TYPE = 21;
    public const byte BATTERY_LEVEL_TYPE = 25;
}