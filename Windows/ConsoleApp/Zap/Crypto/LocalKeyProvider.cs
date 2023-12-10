using System.Security.Cryptography;

namespace ZwiftPlayConsoleApp.Zap.Crypto;

public class LocalKeyProvider
{
    private readonly ECDsa _ecdsa = ECDsa.Create(ECCurve.NamedCurves.nistP256);

    public byte[] GetPublicKeyBytes()
    {
        // have checked this. fairly sure this is the public key in uncompressed format.

        return EncryptionUtils.EcParamsToPublicKeyBytes(_ecdsa.ExportParameters(false));
    }

    public ECParameters GetParams()
    {
        return _ecdsa.ExportParameters(false);
    }

    public ECParameters GetParamsWithPrivateKey()
    {
        return _ecdsa.ExportParameters(true);
    }

    public byte[] GetPrivateKey()
    {
        return _ecdsa.ExportECPrivateKey();
    }
}
