using InTheHand.Bluetooth;

namespace ZwiftPlayConsoleApp.Zap;

public class ZapBleUuids
{
    // The Zwift custom characteristic details were found from decompiling the Zwift Companion app and searching for the service UUID

    // ZAP Service - Zwift Accessory Protocol
    public static readonly BluetoothUuid ZWIFT_CUSTOM_SERVICE_UUID = BluetoothUuid.FromGuid(Guid.Parse("00000001-19CA-4651-86E5-FA29DCDD09D1"));
    public static readonly BluetoothUuid ZWIFT_ASYNC_CHARACTERISTIC_UUID = BluetoothUuid.FromGuid(Guid.Parse("00000002-19CA-4651-86E5-FA29DCDD09D1"));
    public static readonly BluetoothUuid ZWIFT_SYNC_RX_CHARACTERISTIC_UUID = BluetoothUuid.FromGuid(Guid.Parse("00000003-19CA-4651-86E5-FA29DCDD09D1"));
    public static readonly BluetoothUuid ZWIFT_SYNC_TX_CHARACTERISTIC_UUID = BluetoothUuid.FromGuid(Guid.Parse("00000004-19CA-4651-86E5-FA29DCDD09D1"));
    // This doesn't appear in the real hardware but is found in the companion app code.
    // val ZWIFT_DEBUG_CHARACTERISTIC_UUID: UUID = UUID.fromString("00000005-19CA-4651-86E5-FA29DCDD09D1")
    // I have not seen this characteristic used. Guess it could be for Device Firmware Update (DFU)? it is a chip from Nordic.
    public static readonly BluetoothUuid ZWIFT_UNKNOWN_6_CHARACTERISTIC_UUID = BluetoothUuid.FromGuid(Guid.Parse("00000006-19CA-4651-86E5-FA29DCDD09D1"));
}