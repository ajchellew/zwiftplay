using ZwiftPlayConsoleApp.Utils.BitConverter;

namespace ZwiftPlayConsoleApp.Utils;

public class ByteBuffer
{
    private int _position = 0;

    private readonly List<byte> _buffer = new();
    private readonly EndianBitConverter _bitConverter;

    public byte this[int i] => _buffer[i];

    public ByteBuffer(byte[]? bytes = null, bool bigEndian = true)
    {
        _bitConverter = bigEndian ? EndianBitConverter.Big : EndianBitConverter.Little;
        if (bytes != null)
        {
            _buffer.AddRange(bytes);
        }
    }

    public byte[] ToArray()
    {
        return _buffer.ToArray();
    }

    public int Length()
    {
        return _buffer.Count;
    }

    // Read values
    public short ReadInt16()
    {
        return EndianBitConverter.Big.ToInt16(GetBytes(2), 0);
    }

    public int ReadInt32()
    {
        return EndianBitConverter.Big.ToInt32(GetBytes(4), 0);
    }

    public long ReadInt64()
    {
        return EndianBitConverter.Big.ToInt64(GetBytes(8), 0);
    }

    public string ReadString()
    {
        var str = "";
        var size = ReadInt16();
        var estring = new byte[size];

        for (int x = _position, i = 0; x < _position + size; x++, i++)
        {
            estring[i] = _buffer[x];
        }

        foreach (var x in estring)
        {
            str += (char)x;
        }

        _position += size;
        return str;
    }

    public float ReadFloat()
    {
        return _bitConverter.ToSingle(GetBytes(4), 0);
    }

    public double ReadDouble()
    {
        return _bitConverter.ToDouble(GetBytes(8), 0);
    }

    public bool ReadBool()
    {
        return GetBytes(1)[0] != 0;
    }

    public byte ReadByte()
    {
        return GetBytes(1)[0];
    }

    public char ReadChar()
    {
        return (char)ReadByte();
    }

    // Write values
    public void WriteInt16(short v)
    {
        var bytes = _bitConverter.GetBytes(v);
        _buffer.AddRange(bytes);
    }

    public void WriteInt32(int v)
    {
        var bytes = _bitConverter.GetBytes(v);
        /*if (BitConverter.IsLittleEndian && this.isBigEndian)
        {
            Array.Reverse(bytes);
        }*/
        _buffer.AddRange(bytes);
    }

    public void WriteInt64(long v)
    {
        var bytes = _bitConverter.GetBytes(v);
        _buffer.AddRange(bytes);
    }

    public void WriteFloat(float v)
    {
        var bytes = _bitConverter.GetBytes(v);
        _buffer.AddRange(bytes);
    }

    public void WriteDouble(double v)
    {
        var bytes = _bitConverter.GetBytes(v);
        _buffer.AddRange(bytes);
    }

    public void WriteString(string v)
    {
        var bytes = new byte[v.Length];

        for (var x = 0; x < v.Length; x++)
        {
            bytes[x] = (byte)v[x];
        }

        _buffer.AddRange(bytes);
    }

    public void WriteByte(byte v)
    {
        _buffer.Add(v);
    }

    public void WriteBytes(byte[] bytes)
    {
        _buffer.AddRange(bytes);
    }

    public void WriteChar(char v)
    {
        WriteByte((byte)v);
    }


    byte[] GetBytes(int pos)
    {
        var bytes = new byte[pos];

        for (int x = _position, i = 0; x < _position + pos; x++, i++)
        {
            bytes[i] = _buffer[x];
        }

        _position += pos;
        return bytes;
    }
}