using ZwiftPlayConsoleApp.Utils;
using ZwiftPlayConsoleApp.Zap;
using ZwiftPlayConsoleApp.Zap.Crypto;

namespace ZwiftPlayConsoleApp.BLE;

public abstract class AbstractZapDevice
{
    public static bool Debug = false;

    private readonly LocalKeyProvider _localKeyProvider = new();
    protected readonly ZapCrypto _zapEncryption;

    protected AbstractZapDevice()
    {
        _zapEncryption = new ZapCrypto(_localKeyProvider);
    }

    public byte[] BuildHandshakeStart()
    {
        var buffer = new ByteBuffer();
        buffer.WriteBytes(ZapConstants.RIDE_ON);
        buffer.WriteBytes(ZapConstants.REQUEST_START);
        buffer.WriteBytes(_localKeyProvider.GetPublicKeyBytes());
        return buffer.ToArray();
    }

    public void ProcessCharacteristic(string characteristicName, byte[] bytes)
    {
        if (Debug)
            Console.WriteLine($"{characteristicName} {Utils.Utils.ByteArrayToStringHex(bytes)}");

        // todo make better like below kotlin
        if (bytes[0] == ZapConstants.RIDE_ON[0] && bytes[1] == ZapConstants.RIDE_ON[1] && bytes[2] == ZapConstants.RIDE_ON[2] && bytes[3] == ZapConstants.RIDE_ON[3])
        {
            ProcessDevicePublicKeyResponse(bytes);
        }
        else
        {
            ProcessEncryptedData(bytes);
        }

        // todo
        /*when {
            bytes.startsWith(ZapConstants.RIDE_ON.plus(ZapConstants.RESPONSE_START))->processDevicePublicKeyResponse(bytes)
            bytes.size > Int.SIZE_BYTES + EncryptionUtils.MAC_LENGTH->processEncryptedData(bytes)
            else ->Logger.e("Unprocessed - Data Type: ${bytes.toHexString()}")
        }*/
    }

    protected abstract void ProcessEncryptedData(byte[] bytes);

    private void ProcessDevicePublicKeyResponse(byte[] bytes)
    {
        // checked this looks ok

        var headerSize = ZapConstants.RIDE_ON.Length + ZapConstants.RESPONSE_START.Length;
        var devicePublicKeyBytes = new byte[bytes.Length - headerSize];

        if (devicePublicKeyBytes.Length != 64)
        {
            throw new ArgumentException("Key should be 64 bytes...");
        }

        Array.Copy(bytes, headerSize, devicePublicKeyBytes, 0, devicePublicKeyBytes.Length);

        _zapEncryption.Initialise(devicePublicKeyBytes);

        if (Debug)
            Console.WriteLine($"Device Public Key - ${Utils.Utils.ByteArrayToStringHex(devicePublicKeyBytes)}");
    }
}