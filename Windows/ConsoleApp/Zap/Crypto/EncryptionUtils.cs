using System.Security.Cryptography;

namespace ZwiftPlayConsoleApp.Zap.Crypto;

public class EncryptionUtils
{
    public const int KEY_LENGTH = 32;
    public const int HKDF_LENGTH = 36;
    public const int MAC_LENGTH = 4;

    public static byte[] EcParamsToPublicKeyBytes(ECParameters parameters)
    {
        var ecPoint = parameters.Q;

        if (ecPoint.X == null || ecPoint.Y == null)
        {
            throw new ArgumentException("Public point is null. How?");
        }

        var publicKey = new byte[ecPoint.X.Length + ecPoint.Y.Length];
        ecPoint.X.CopyTo(publicKey, 0);
        ecPoint.Y.CopyTo(publicKey, ecPoint.X.Length); // Now you have a public key in publicKey
        return publicKey;
    }

}