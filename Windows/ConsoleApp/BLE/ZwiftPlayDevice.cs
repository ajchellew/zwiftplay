using ZwiftPlayConsoleApp.Utils;
using ZwiftPlayConsoleApp.Zap;
using ZwiftPlayConsoleApp.Zap.Crypto;
using ZwiftPlayConsoleApp.Zap.Proto;

namespace ZwiftPlayConsoleApp.BLE;

public class ZwiftPlayDevice : AbstractZapDevice
{
    private int _batteryLevel;

    protected override void ProcessEncryptedData(byte[] bytes)
    {
        try
        {
            //if (LOG_RAW) Timber.d("Decrypted: ${bytes.toHexString()}")

            var counterBytes = new byte[4];
            Array.Copy(bytes, 0, counterBytes, 0, counterBytes.Length);
            var counter = new ByteBuffer(counterBytes).ReadInt32();

            var payloadBytes = new byte[bytes.Length - 4 - EncryptionUtils.MAC_LENGTH];
            Array.Copy(bytes, 4, payloadBytes, 0, payloadBytes.Length);

            var tagBytes = new byte[EncryptionUtils.MAC_LENGTH];
            Array.Copy(bytes, EncryptionUtils.MAC_LENGTH + payloadBytes.Length, tagBytes, 0, tagBytes.Length);

            var data = _zapEncryption.Decrypt(counter, payloadBytes, tagBytes);
            
            var type = data[0];

            var messageBytes = new byte[data.Length - 1];
            Array.Copy(data, 1, messageBytes, 0, messageBytes.Length);

            switch (type)
            {
                case ZapConstants.CONTROLLER_NOTIFICATION_MESSAGE_TYPE:
                    ProcessButtonNotification(new ControllerNotification(messageBytes));
                    break;
                case ZapConstants.EMPTY_MESSAGE_TYPE:
                    Console.WriteLine("Empty Message");
                    break;
                case ZapConstants.BATTERY_LEVEL_TYPE:
                    var notification = new BatteryStatus(messageBytes);
                    if (_batteryLevel != notification.Level)
                    {
                        _batteryLevel = notification.Level;
                        Console.WriteLine("Battery level update: $batteryLevel");
                    }
                    break;
                default:
                    Console.WriteLine($"Unprocessed - Type: {type} Data: {Utils.Utils.ByteArrayToStringHex(data)}");
                    break;
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Decrypt failed: " + ex.Message);
        }
    }

    private void ProcessButtonNotification(ControllerNotification controllerNotification)
    {
        
    }
}