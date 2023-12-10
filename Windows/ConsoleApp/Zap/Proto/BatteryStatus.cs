using Google.Protobuf;

namespace ZwiftPlayConsoleApp.Zap.Proto;

public class BatteryStatus
{
    public int Level { get; set; }

    public BatteryStatus(byte[] messageBytes)
    {
        var input = new CodedInputStream(messageBytes);
        while (true)
        {
            var tag = input.ReadTag();
            var type = WireFormat.GetTagWireType(tag);
            if (tag == 0 || type == WireFormat.WireType.EndGroup)
            {
                break;
            }

            var number = WireFormat.GetTagFieldNumber(tag);

            switch (type)
            {
                case WireFormat.WireType.Varint:
                    var value = input.ReadInt64();
                    switch (number)
                    {
                        case 2:
                            Level = (int) value; // biggest number we expect is an int
                            break;
                        default:
                            throw new ArgumentException("Unexpected tag");
                    }
                    break;
                default:
                    throw new ArgumentException("Unexpected wire type");
                    break;
            }
        }
    }
}