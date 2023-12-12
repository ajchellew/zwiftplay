using Google.Protobuf;
using ZwiftPlayConsoleApp.BLE;

namespace ZwiftPlayConsoleApp.Zap.Proto;

public class ControllerNotification
{
    private const int BTN_PRESSED = 0;

    private const string SHOULDER_NAME = "Shoulder";
    private const string POWER_NAME = "Power";
    private const string STEER_NAME = "Steer/Brake";
    private const string UNKNOWN_NAME = "???";

    private bool _isRightController = false;

    public bool buttonYPressed = false;// or up on left controller
    public bool buttonZPressed = false; // or left on left controller
    public bool buttonAPressed = false;// or right on left controller
    public bool buttonBPressed = false; // or down on left controller

    public bool shoulderButtonPressed = false;
    public bool powerButtonPressed = false;

    // on the left this will be negative when steering and positive when breaking and vice versa on right
    public int steerBrakeValue = 0;

    public int somethingValue = 0;

    public ControllerNotification(byte[] messageBytes)
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
                        case 1:
                            _isRightController = value == BTN_PRESSED;
                            break;
                        case 2:
                            buttonYPressed = value == BTN_PRESSED;
                            break;
                        case 3:
                            buttonZPressed = value == BTN_PRESSED;
                            break;
                        case 4:
                            buttonAPressed = value == BTN_PRESSED;
                            break;
                        case 5:
                            buttonBPressed = value == BTN_PRESSED;
                            break;
                        case 6:
                            shoulderButtonPressed = value == BTN_PRESSED;
                            break;
                        case 7:
                            powerButtonPressed = value == BTN_PRESSED;
                            break;
                        case 8:
                            steerBrakeValue = ProtoUtils.GetSignedValue((int)value);
                            break;
                        case 9:
                            somethingValue = (int)value;
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

    public string Diff(ControllerNotification previousNotification)
    {
        var diff = "";
        diff += Diff(NameY(), buttonYPressed, previousNotification.buttonYPressed);
        diff += Diff(NameZ(), buttonZPressed, previousNotification.buttonZPressed);
        diff += Diff(NameA(), buttonAPressed, previousNotification.buttonAPressed);
        diff += Diff(NameB(), buttonBPressed, previousNotification.buttonBPressed);
        diff += Diff(SHOULDER_NAME, shoulderButtonPressed, previousNotification.shoulderButtonPressed);
        diff += Diff(POWER_NAME, powerButtonPressed, previousNotification.powerButtonPressed);
        diff += Diff(STEER_NAME, steerBrakeValue, previousNotification.steerBrakeValue);
        diff += Diff(UNKNOWN_NAME, somethingValue, previousNotification.somethingValue);
        return diff;
    }

    private string Diff(string title, bool pressedValue, bool oldPressedValue)
    {
        if (pressedValue != oldPressedValue)
            return $"{title}={(pressedValue ? "Pressed" : "Released")} ";
        return "";
    }

    private string Diff(string title, int newValue, int oldValue)
    {
        if (newValue != oldValue)
            return $"{title}={newValue} ";
        return "";
    }

    private string NameController() => _isRightController ? "Right" : "Left";

    private string NameY() => _isRightController? "Y" : "Up";

    private string NameZ() => _isRightController? "Z" : "Left";

    private string NameA() => _isRightController? "A" : "Right";

    private string NameB() => _isRightController ? "B" : "Down";

    public override string ToString()
    {
        var text = "ControllerNotification(";

        text += $"{NameController()} ";

        text += buttonYPressed ? NameY() : "";
        text += buttonZPressed ? NameZ() : "";
        text += buttonAPressed ? NameA() : "";
        text += buttonBPressed ? NameB() : "";

        text += shoulderButtonPressed ? SHOULDER_NAME : "";
        text += powerButtonPressed ? POWER_NAME : "";

        text += steerBrakeValue != 0 ? $"{STEER_NAME}: {steerBrakeValue}" : "";

        text += somethingValue != 0 ? $"{UNKNOWN_NAME}: {somethingValue}" : "";

        text += ")";
        return text;
    }

    public ButtonChange[] DiffChange(ControllerNotification? previousNotification)
    {
        var diffList = new List<ButtonChange>();
        DiffChange(diffList, _isRightController ? ZwiftPlayButton.Y : ZwiftPlayButton.Up, buttonYPressed, previousNotification?.buttonYPressed ?? false);
        DiffChange(diffList, _isRightController ? ZwiftPlayButton.Z : ZwiftPlayButton.Left, buttonZPressed, previousNotification?.buttonZPressed ?? false);
        DiffChange(diffList, _isRightController ? ZwiftPlayButton.A : ZwiftPlayButton.Right, buttonAPressed, previousNotification?.buttonAPressed ?? false);
        DiffChange(diffList, _isRightController ? ZwiftPlayButton.B : ZwiftPlayButton.Down, buttonBPressed, previousNotification?.buttonBPressed ?? false);
        DiffChange(diffList, _isRightController ? ZwiftPlayButton.RightShoulder : ZwiftPlayButton.LeftShoulder, shoulderButtonPressed, previousNotification?.shoulderButtonPressed ?? false);
        DiffChange(diffList, _isRightController ? ZwiftPlayButton.RightPower: ZwiftPlayButton.LeftPower, powerButtonPressed, previousNotification?.powerButtonPressed ?? false);
        //diff += Diff(STEER_NAME, steerBrakeValue, previousNotification.steerBrakeValue);
        //diff += Diff(UNKNOWN_NAME, somethingValue, previousNotification.somethingValue);
        return diffList.ToArray();
    }

    private void DiffChange(List<ButtonChange> changes, ZwiftPlayButton button, bool pressedValue, bool oldPressedValue)
    {
        if (pressedValue != oldPressedValue)
        {
            changes.Add(new ButtonChange(){ Button = button, IsPressed = pressedValue });
        }
    }

}

public class ProtoUtils
{

    public static int GetSignedValue(int value)
    {
        var negativeBit = value & 0b1;
        var num = value >> 1;
        return negativeBit == 1 ? -num : num;
    }

}