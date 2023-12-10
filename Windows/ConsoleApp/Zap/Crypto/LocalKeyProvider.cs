using Org.BouncyCastle.Asn1.X9;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Generators;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Security;
using ZwiftPlayConsoleApp.Utils;

namespace ZwiftPlayConsoleApp.Zap.Crypto;

public class LocalKeyProvider
{
    private readonly SecureRandom _rngCsp = new();

    internal readonly ECDomainParameters _ecParameters;
    private readonly ECPublicKeyParameters _publicKey;
    private readonly IBasicAgreement _ecdhKeyAgreement;

    public LocalKeyProvider()
    {
        var curve = ECNamedCurveTable.GetByName("secp256r1");
        _ecParameters = new ECDomainParameters(curve.Curve, curve.G, curve.N);

        var keyGeneratorParams = new ECKeyGenerationParameters(_ecParameters, _rngCsp);
        var keyGenerator = new ECKeyPairGenerator("ECDH");
        keyGenerator.Init(keyGeneratorParams);
        var keyPair = keyGenerator.GenerateKeyPair();
        var privateKey = keyPair.Private as ECPrivateKeyParameters;
        if (keyPair.Public is not ECPublicKeyParameters ecPublicKeyParameters)
        {
            throw new ArgumentException("How is this null");
        }
        _publicKey = ecPublicKeyParameters;

        var keyAgreement = AgreementUtilities.GetBasicAgreement("ECDH");
        keyAgreement.Init(privateKey);
        _ecdhKeyAgreement = keyAgreement;
    }

    public byte[] GetPublicKeyBytes()
    {
        var allKeyBytes = _publicKey.Q.GetEncoded(false);

        // trim off the 4 that says its a compressed encoding.
        var publicKeyRawByteArrayTrimmed = new byte[64];
        Array.Copy(allKeyBytes, 1, publicKeyRawByteArrayTrimmed, 0, allKeyBytes.Length - 1);
        return publicKeyRawByteArrayTrimmed;
    }

    public byte[] GetSharedSecret(byte[] publicKeyIn)
    {
        // put the byte back on so its known that its an uncompressed encoded export
        var buffer = new ByteBuffer();
        buffer.WriteByte(4);
        buffer.WriteBytes(publicKeyIn);

        var point = _ecParameters.Curve.DecodePoint(buffer.ToArray());
        var otherPubKey = new ECPublicKeyParameters(point, _ecParameters);

        var secret = _ecdhKeyAgreement.CalculateAgreement(otherPubKey);
        return secret.ToByteArrayUnsigned();
    }
}
