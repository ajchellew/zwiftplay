using ABI.Windows.ApplicationModel.UserDataTasks;
using InTheHand.Bluetooth;
using ZwiftPlayConsoleApp.BLE;
using ZwiftPlayConsoleApp.Zap;

public class Program
{
    private static readonly Dictionary<string, ZwiftPlayBleManager> _bleManagers = new();

    public static void Main(string[] args)
    {
        // bluetooth availablity on PC.

        var available = Bluetooth.GetAvailabilityAsync().GetAwaiter().GetResult();
        if (!available)
        {
            throw new ArgumentException("Need Bluetooth");
        }

        /*Bluetooth.AvailabilityChanged += delegate(object? sender, EventArgs eventArgs)
        {
            Console.WriteLine("BLE Availability Changed:" + eventArgs.ToString());
        };*/

        Bluetooth.AdvertisementReceived += (sender, scanResult) =>
        {
            // not always getting name... so checking for the manufacturer data.
            if (!scanResult.ManufacturerData.ContainsKey(ZapConstants.ZWIFT_MANUFACTURER_ID))
            {
                return;
            }

            if (_bleManagers.ContainsKey(scanResult.Device.Id))
            {
                return;
            }

            var data = scanResult.ManufacturerData[ZapConstants.ZWIFT_MANUFACTURER_ID];
            var typeByte = data[0];

            if (typeByte != ZapConstants.RC1_LEFT_SIDE && typeByte != ZapConstants.RC1_RIGHT_SIDE)
            {
                return;
            }

            var device = scanResult.Device;
            Console.WriteLine("Connecting to " + device.Id);
            var clientManager = new ZwiftPlayBleManager(device, typeByte == ZapConstants.RC1_LEFT_SIDE);

            _bleManagers[device.Id] = clientManager;

            clientManager.ConnectAsync();
        };

        // keep scanning until we find both controllers.
        Task.Run(async () =>
        {
            while (_bleManagers.Count < 2)
            {
                Console.WriteLine("Start BLE Scan - Connected " + _bleManagers.Count + "/2");
                await Bluetooth.RequestLEScanAsync();
                await Task.Delay(30000);
            }
            Console.WriteLine("BLE Scan - loop done");
        });

        var run = true;
        while (run)
        {
            var line = Console.ReadLine();
            if (line != null)
            {
                var split = line.Split(" ");
                switch (split[0])
                {
                    case "q":
                    case "quit":
                        run = false;
                        break;
                }
            }
        }
    }
}