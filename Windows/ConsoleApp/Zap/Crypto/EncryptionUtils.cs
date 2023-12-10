using System.Security.Cryptography;

namespace ZwiftPlayConsoleApp.Zap.Crypto;

public class EncryptionUtils
{
    public const int KEY_LENGTH = 32;
    public const int HKDF_LENGTH = 36;
    public const int MAC_LENGTH = 4;
}