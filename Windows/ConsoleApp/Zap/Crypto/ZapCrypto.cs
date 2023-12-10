using System.Security.Cryptography;
using HkdfStandard;
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
        // todo endianness ??
        buffer.WriteInt32(counter);
        var nonceBytes = buffer.ToArray();

        var clearBytes = new byte[payloadBytes.Length];

        _aesCcm.Decrypt(nonceBytes, payloadBytes, tagBytes, clearBytes);

        return clearBytes;
    }

    private byte[] GenerateHmacKeyDerivationFunctionBytes(byte[] devicePublicKeyBytes)
    {
        var serverPublicKey = GeneratePublicKey(devicePublicKeyBytes, _localKeyProvider.GetParams());

        var saltBuffer = new ByteBuffer();
        saltBuffer.WriteBytes(EncryptionUtils.EcParamsToPublicKeyBytes(serverPublicKey.ExportParameters()));
        saltBuffer.WriteBytes(_localKeyProvider.GetPublicKeyBytes());
        var salt = saltBuffer.ToArray();

        var sharedSecretBytes = GenerateSharedSecretBytes(_localKeyProvider.GetParamsWithPrivateKey(), serverPublicKey);

        return GenerateHKDFBytes(sharedSecretBytes, salt);
    }

    private ECDiffieHellmanPublicKey GeneratePublicKey(byte[] devicePublicKeyBytes, ECParameters ecParameters)
    {
        // think this is ok

        var xBytes = new byte[32];
        Array.Copy(devicePublicKeyBytes, 0, xBytes, 0, xBytes.Length);
        var yBytes = new byte[32];
        Array.Copy(devicePublicKeyBytes, 32, yBytes, 0, yBytes.Length);

        var devicePublicKeyPoint = new ECPoint { X = xBytes, Y = yBytes };

        var remoteParameters = new ECParameters { Curve = ecParameters.Curve, Q = devicePublicKeyPoint };
        var remoteEcdh = ECDiffieHellman.Create(remoteParameters);
        return remoteEcdh.PublicKey;
    }

    private byte[] GenerateSharedSecretBytes(/*byte[] privateKey,*/ECParameters parameters, ECDiffieHellmanPublicKey serverPublicKey)
    {
        var ecdh = new ECDiffieHellmanCng();
        ecdh.ImportParameters(parameters);

        return ecdh.DeriveKeyMaterial(serverPublicKey);

        //ecdh.DeriveKeyFromHmac() -??
    }

    private byte[] GenerateHKDFBytes(byte[] sharedSecretBytes, byte[] salt)
    {
        /*var hmacKey = ecdh.DeriveKeyFromHmac(serverPublicKey, HashAlgorithmName.SHA256, salt);
        return hmacKey;*/

        /*var hmac = new HMACSHA256();
        hmac.Key = sharedSecretBytes;
        hmac.Initialize();*/

        /*var hmac = new HMACSHA256(sharedSecretBytes);
        byte[] result = hmac.ComputeHash(salt);*/

        return Hkdf.DeriveKey(HashAlgorithmName.SHA256, sharedSecretBytes, EncryptionUtils.HKDF_LENGTH, salt);

        //return Hkdf.Expand(HashAlgorithmName.SHA256, sharedSecretBytes, EncryptionUtils.HKDF_LENGTH, salt);
    }
}