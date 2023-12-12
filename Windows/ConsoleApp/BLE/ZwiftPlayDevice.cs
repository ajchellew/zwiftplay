using ZwiftPlayConsoleApp.Utils;
using ZwiftPlayConsoleApp.Zap;
using ZwiftPlayConsoleApp.Zap.Crypto;
using ZwiftPlayConsoleApp.Zap.Proto;

namespace ZwiftPlayConsoleApp.BLE;

public class ZwiftPlayDevice : AbstractZapDevice
{
    private int _batteryLevel;
    private ControllerNotification? _lastButtonState;

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
                    if (Debug)
                        Console.WriteLine("Empty Message");
                    break;
                case ZapConstants.BATTERY_LEVEL_TYPE:
                    var notification = new BatteryStatus(messageBytes);
                    if (_batteryLevel != notification.Level)
                    {
                        _batteryLevel = notification.Level;
                        Console.WriteLine($"Battery level update: {_batteryLevel}");
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

    private const bool SendKeys = false;

    private void ProcessButtonNotification(ControllerNotification notification)
    {
        if (SendKeys)
        {
            var changes = notification.DiffChange(_lastButtonState);
            foreach (var change in changes)
            {
                KeyboardKeys.ProcessZwiftPlay(change);
            }
        }
        else
        {
            if (_lastButtonState == null)
            {
                Console.WriteLine(notification.ToString());
            }
            else
            {
                var diff = notification.Diff(_lastButtonState);
                if (!string.IsNullOrEmpty(diff)) // get repeats of the same state
                {
                    Console.WriteLine(diff);
                }
            }
        }

        _lastButtonState = notification;
    }
}