using System.Security.Cryptography;
using ZwiftPlayConsoleApp.Utils;

namespace ZwiftPlayConsoleApp.Zap.Crypto;

public class ZapCrypto
{
    private readonly LocalKeyProvider _localKeyProvider;

    private byte[] _encryptionKeyBytes;
    private byte[] _ivBytes;

    private AesCcm _aesCcm;

    public ZapCrypto(LocalKeyProvider localKeyProvider)
    {
        _localKeyProvider = localKeyProvider;
    }

    public void Initialise(byte[] devicePublicKeyBytes)
    {
        var hkdfBytes = GenerateHmacKeyDerivationFunctionBytes(devicePublicKeyBytes);

        var keyBytes = new byte[EncryptionUtils.KEY_LENGTH];
        Array.Copy(hkdfBytes, 0, keyBytes, 0, keyBytes.Length);

        var ivBytes = new byte[EncryptionUtils.HKDF_LENGTH - EncryptionUtils.KEY_LENGTH];
        Array.Copy(hkdfBytes, 32, ivBytes, 0, ivBytes.Length);

        _encryptionKeyBytes = keyBytes;
        _aesCcm = new AesCcm(_encryptionKeyBytes);

        _ivBytes = ivBytes;
    }

    public byte[] Decrypt(int counter, byte[] payloadBytes, byte[] tagBytes)
    {
        var buffer = new ByteBuffer();
        buffer.WriteBytes(_ivBytes);
        buffer.WriteInt32(counter);
        var nonceBytes = buffer.ToArray();

        var clearBytes = new byte[payloadBytes.Length];

        _aesCcm.Decrypt(nonceBytes, payloadBytes, tagBytes, clearBytes);

        return clearBytes;
    }

    private byte[] GenerateHmacKeyDerivationFunctionBytes(byte[] devicePublicKeyBytes)
    {
        var saltBuffer = new ByteBuffer();
        saltBuffer.WriteBytes(devicePublicKeyBytes);
        saltBuffer.WriteBytes(_localKeyProvider.GetPublicKeyBytes());
        var salt = saltBuffer.ToArray();

        var sharedSecretBytes = _localKeyProvider.GetSharedSecret(devicePublicKeyBytes);

        return HKDF.DeriveKey(HashAlgorithmName.SHA256, sharedSecretBytes, EncryptionUtils.HKDF_LENGTH, salt);
    }
}