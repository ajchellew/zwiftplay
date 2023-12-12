using System.Runtime.InteropServices;
using ZwiftPlayConsoleApp.BLE;

namespace ZwiftPlayConsoleApp.Utils;

public class KeyboardKeys
{
    [DllImport("user32.dll", SetLastError = true)]
    static extern void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);

    public const int KEYEVENTF_KEYDOWN = 0x0; 
    public const int KEYEVENTF_KEYUP = 0x2;

    public const int LEFT = 0x25;
    public const int UP = 0x26;
    public const int RIGHT = 0x27;
    public const int DOWN = 0x28;

    public const int LCONTROL = 0xA2;
    public const int RCONTROL = 0xA3;

    public const int A = 0x41;
    public const int B = 0x42;
    public const int C = 0x43;
    public const int D = 0x44;
    public const int E = 0x45;
    public const int F = 0x46;
    public const int G = 0x47;
    public const int H = 0x48;
    public const int I = 0x49;
    public const int J = 0x4A;
    public const int K = 0x4B;
    public const int L = 0x4C;
    public const int M = 0x4D;
    public const int N = 0x4E;
    public const int O = 0x4F;
    public const int P = 0x50;
    public const int Q = 0x51;
    public const int R = 0x52;
    public const int S = 0x53;
    public const int T = 0x54;
    public const int U = 0x55;
    public const int V = 0x56;
    public const int W = 0x57;
    public const int X = 0x58;
    public const int Y = 0x59;
    public const int Z = 0x5A;

    public static void ProcessZwiftPlay(ButtonChange change)
    {
        var keyCode = GetKeyCode(change.Button);

        if (keyCode == null)
        {
            return;
        }

        if (change.IsPressed)
        {
            PressKey((byte)keyCode);
        }
        else
        {
            ReleaseKey((byte)keyCode);
        }
    }

    private static byte? GetKeyCode(ZwiftPlayButton button)
    {
        switch (button)
        {
            case ZwiftPlayButton.Up:
                return UP;
            case ZwiftPlayButton.Down:
                return DOWN;
            case ZwiftPlayButton.Left:
                return LEFT;
            case ZwiftPlayButton.Right:
                return RIGHT;
            case ZwiftPlayButton.LeftShoulder:
                return LCONTROL;
            case ZwiftPlayButton.LeftPower:
                break;
            
            case ZwiftPlayButton.A:
                return A;
            case ZwiftPlayButton.B:
                return B;
            case ZwiftPlayButton.Y:
                return Y;
            case ZwiftPlayButton.Z:
                return Z;
            case ZwiftPlayButton.RightShoulder:
                return RCONTROL;
            case ZwiftPlayButton.RightPower:
                break;
            
            default:
                throw new ArgumentOutOfRangeException(nameof(button), button, null);
        }

        return null;
    }

    private static void PressKey(byte keyCode)
    {
        // probably need to keep resending this until its released but not really tested.

        keybd_event(keyCode, 0x45, KEYEVENTF_KEYDOWN, 0);
    }

    private static void ReleaseKey(byte keyCode)
    {
        keybd_event(keyCode, 0x45, KEYEVENTF_KEYUP, 0);
    }
}
