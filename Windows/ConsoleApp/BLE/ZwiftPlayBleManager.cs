using InTheHand.Bluetooth;
using ZwiftPlayConsoleApp.Zap;

namespace ZwiftPlayConsoleApp.BLE;

public class ZwiftPlayBleManager
{
    private readonly ZwiftPlayDevice _zapDevice = new();

    private readonly BluetoothDevice _device;
    private readonly bool _isLeft;

    private static GattCharacteristic _asyncCharacteristic;
    private static GattCharacteristic _syncRxCharacteristic;
    private static GattCharacteristic _syncTxCharacteristic;

    public ZwiftPlayBleManager(BluetoothDevice device, bool isLeft)
    {
        _device = device;
        _isLeft = isLeft;
    }

    public async void ConnectAsync()
    {
        var gatt = _device.Gatt;
        await gatt.ConnectAsync();

        if (gatt.IsConnected)
        {
            Console.WriteLine("Connected");

            //var services = gatt.GetPrimaryServicesAsync().GetAwaiter().GetResult();
            RegisterCharacteristics(gatt);

            Console.WriteLine("Send Start");
            _syncRxCharacteristic.WriteValueWithResponseAsync(_zapDevice.BuildHandshakeStart()).GetAwaiter().GetResult();
        }
    }

    private async void RegisterCharacteristics(RemoteGattServer gatt)
    {
        var zapService = gatt.GetPrimaryServiceAsync(ZapBleUuids.ZWIFT_CUSTOM_SERVICE_UUID).GetAwaiter().GetResult();
        _asyncCharacteristic = zapService.GetCharacteristicAsync(ZapBleUuids.ZWIFT_ASYNC_CHARACTERISTIC_UUID)
            .GetAwaiter().GetResult();

        _syncRxCharacteristic = zapService.GetCharacteristicAsync(ZapBleUuids.ZWIFT_SYNC_RX_CHARACTERISTIC_UUID)
            .GetAwaiter().GetResult();
        _syncTxCharacteristic = zapService.GetCharacteristicAsync(ZapBleUuids.ZWIFT_SYNC_TX_CHARACTERISTIC_UUID)
            .GetAwaiter().GetResult();

        _asyncCharacteristic.StartNotificationsAsync().GetAwaiter().GetResult();
        _asyncCharacteristic.CharacteristicValueChanged += (sender, eventArgs) =>
        {
            _zapDevice.ProcessCharacteristic("Async", eventArgs.Value);
        };

        _syncTxCharacteristic.StartNotificationsAsync().GetAwaiter().GetResult();
        _syncTxCharacteristic.CharacteristicValueChanged += (sender, eventArgs) =>
        {
            _zapDevice.ProcessCharacteristic("Sync Tx", eventArgs.Value);
        };
    }
}