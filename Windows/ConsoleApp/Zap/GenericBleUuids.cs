using InTheHand.Bluetooth;

namespace ZwiftPlayConsoleApp.Zap;

public class GenericBleUuids
{
    public static readonly BluetoothUuid GENERIC_ACCESS_SERVICE_UUID = 0x1800; //BluetoothUuid.FromShortId(1800);
    public static readonly BluetoothUuid DEVICE_NAME_CHARACTERISTIC_UUID = 0x2A00; // Zwift Play
    public static readonly BluetoothUuid APPEARANCE_CHARACTERISTIC_UUID = 0x2A01; // [964] Gamepad (HID Subtype)
    public static readonly BluetoothUuid PREFERRED_CONNECTION_PARAMS_CHARACTERISTIC_UUID = 0x2A04;
    public static readonly BluetoothUuid CENTRAL_ADDRESS_RESOLUTION_CHARACTERISTIC_UUID = 0x2AA6;

    public static readonly BluetoothUuid GENERIC_ATTRIBUTE_SERVICE_UUID = 0x1801;
    public static readonly BluetoothUuid SERVICE_CHANGED_CHARACTERISTIC_UUID = 0x2A05;

    public static readonly BluetoothUuid DEVICE_INFORMATION_SERVICE_UUID = 0x180A;
    public static readonly BluetoothUuid MANUFACTURER_NAME_STRING_CHARACTERISTIC_UUID = 0x2A29; // Zwift Inc.
    public static readonly BluetoothUuid SERIAL_NUMBER_STRING_CHARACTERISTIC_UUID = 0x2A25; // 02-1[MAC]
    public static readonly BluetoothUuid HARDWARE_REVISION_STRING_CHARACTERISTIC_UUID = 0x2A27; // B.0
    public static readonly BluetoothUuid FIRMWARE_REVISION_STRING_CHARACTERISTIC_UUID = 0x2A26; // 1.1.0

    public static readonly BluetoothUuid BATTERY_SERVICE_UUID = 0x180F;
    public static readonly BluetoothUuid BATTERY_LEVEL_CHARACTERISTIC_UUID = 0x2A19; // 89

    public static readonly BluetoothUuid DEFAULT_DESCRIPTOR_UUID = 0x2902;
}