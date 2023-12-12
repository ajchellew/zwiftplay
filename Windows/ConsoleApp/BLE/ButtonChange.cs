namespace ZwiftPlayConsoleApp.BLE;

public class ButtonChange
{
    public bool IsPressed
    {
        get; set;
    }

    public ZwiftPlayButton Button
    {
        get; set;
    }
}

public enum ZwiftPlayButton
{
    Up,
    Down,
    Left,
    Right,
    // Left Steer/Brake
    LeftShoulder,
    LeftPower,

    A,
    B,
    Y,
    Z,
    // Right Steer/Brake
    RightShoulder,
    RightPower,
}