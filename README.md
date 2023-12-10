# Zwift Play



> Note: When I started this I just assumed I'd need to listen to a characteristic notification and have to work out what button it was. Instead the controllers sit within the same API as the other Zwift hardware, the connection relies on creating a encryption keys and the messages are all encrypted. Unexpected for simple Bluetooth buttons, far better than most Bluetooth Accessories. Initially I didn't finish decoding these, 3 months later I saw [redditor](https://www.reddit.com/r/Zwift/comments/17bofib/comment/k5l0fb7/) and [mod maker](https://www.gta5-mods.com/scripts/gt-bike-v) Makinolo had done it, so I needed to get further and I now had some helpful pointers. 



An attempt to reverse engineer the Zwift Play controllers. Initially I wanted to find someone that had done this as I wanted the ability to 'return a ride on' from a button, this isn't even available in the Companion app. 

When I couldn't find someone doing anything with the controllers I decided to buy them. They are cool, even after a few uses I'd recommend them. 

But still I need a project...

## BLE Spec

The controller presents several standard Bluetooth services and one non-standard.

- Generic Access Service
- Device Information Service
- Battery Information Service


and Zwift Play's unique Service:

```
Service: 00000001-19ca-4651-86e5-fa29dcdd09d1
- Characteristic: 00000002-19ca-4651-86e5-fa29dcdd09d1 Properties: Notify 
  - Descriptor: 0x2902
- Characteristic: 00000003-19ca-4651-86e5-fa29dcdd09d1 Properties: Write Write-NoResponse 
- Characteristic: 00000004-19ca-4651-86e5-fa29dcdd09d1 Properties: Indicate Read 
  - Descriptor: 0x2902
- Characteristic: 00000006-19ca-4651-86e5-fa29dcdd09d1 Properties: Indicate Read Write Write-NoResponse
  - Descriptor: 0x2902
```

Packet captures also revealed that the device can appear advertising with the same Mac address + 1

This shows the Nordic Semiconductor DFU service `0xfe59`
With the following characteristics:
```
8ec90001-f315-4f60-9fb8-838830daea50
8ec90002-f315-4f60-9fb8-838830daea50
```
These are standard and part of the Nordic Thingy dev units that I've previously played with.

## Host app

The first step was to create an app that pretends to be Zwift and listens to the real controllers. Immediate problem is no characteristics are giving any data so I imagine like [Sterzo](https://github.com/matandoocorpo/Zwift-Steer/issues/4) there is some kind of handshake.. 

## Packet Capture

Enabling Developer Option in Android "Enable Bluetooth HCI snoop log" allows logs of both sent and received data to be captured. Generating a bug report contains the log file and Wireshark can read it. 

On connection, after reading all the device information:
- Zwift sends the following to the SyncRX characteristic beginning `52 69 64 65 4f 6e 01 02` 
- The controller replies to the SyncTX characteristic beginning `52 69 64 65 4f 6e 01 01`

`52 69 64 65 4f 6e` = RideOn, nice. This packet is quite long so likely encrypted.

Once handshaken, Zwift sends 4 writes to the controller via SyncRX:
`00 00 00 00 510e364ab3a3d6`  
`01 00 00 00 7fe8e7c8389d63299dfe56039cc1aa`  
`02 00 00 00 f28e00fd32006a`  
`03 00 00 00 7eab5fcede8970f9`  

The data appears to start with an sequence number as an int

The controller then replies via SyncTx

`00 00 00 00 5c0e37b3d66f3b3998c100e9a4348356a9e0b8c2`  

Also with its own sequence number
This communication continues for about 10s

The Async Characteristic begins sending as well. It also uses the same counter for the sequence number

`01 00 00 00 477165c76c`  
`03 00 00 00 6b8a9b2850`  
`05 00 00 00 e0ebf44171`

Once the negotiation completes the controller constantly sends on the Async charactertistic. 

`20 00 00 00 728054c687c24756dab4a9bc4ed72a028cd762bac00283`  
`21 00 00 00 523764af5b83bf59c8888e324c27dedfccef22800b310c`  
`22 00 00 00 30dda16e2cdc34948cbd5ef68eb2767888a47c2515f10a`  
...  
`95 04 00 00 4d3fc13a6fa97ff3b88b7bb30b176605236ec38cd7d5c8`  
`96 04 00 00 5b632d249ff262bce53aea4044fa8102a2dd78fe1d4924`  

Keep alive? Button states? It changes despite not pressing anything. It changes a lot when a button is pressed, including the power button.

## Reverse Engineer

Decompiling the Zwift Companion app using [jadx GUI](https://github.com/skylot/jadx) gives some names to go with the characteristics. Along with references to _BrevetBlePeripheral_ and _ZapEncryption_ 

Deciding which controller is left and right was a mystery, however can now see its in the ManufacturerSpecificData of a ScanResult.
The characteristic names ASYNC, SYNC_TX and SYNC_RX. Essentially another Serial Port Service implementation over BLE.

The handshake appears to be simple, `RideOn 0x00 0x09` followed by a 64 byte public key. However sending this appears to do nothing. Sending the captured data however works and doesn't match the `0x00 0x09` found in the code. 

The fact that the obvious sequence number in the packet is in little endian and having seen that the main Zwift app sends Protocol Buffer messages to the companion makes me assume the data received is a Protocol Buffer message.

In the `Zap` class `ControllerNotification` extends `GeneratedMessageLite` would appear to fit the bill, the same protocol buffer could vary in length if certain data is not sent. I guess Zap which sits next to ZwiftProtocol stands for _Zwift Accessory Protocol_?

`ControllerNotification` doesn't appear to be used, I believe the companion app doesn't manage any of the communication and instead it acts as a MITM to the main Zwift application via `BleRequestProcessor`.

### 3 months later...

3 months have passed since I touched this side project, I saw Makinolo's post on Reddit and decided I needed to finish at least decoding off. The messages were indeed encrypted and were protocol buffer messages.

See [Makinolo's blog post](https://www.makinolo.com/blog/2023/10/08/connecting-to-zwift-play-controllers/) for a write up on the encryption used. I couldn't write it better. The only catch is that in .NET the encryption AAD is handled in a seperate call where as in the Java code its left on the end of the payload to decrypt. I should have known this sooner as I had the same issue with working with AES-GCM between Android and desktop used in another project.

Now we have a very dumb app that:
- Scans for the devices
- Connects to the devices
- Reads the characteristics and manufacturer data
- Performs the required handshake and generates the shared secret key
- Decrypts and decodes _some_ messages
- Logs buttons pressed

![image](https://github.com/ajchellew/zwiftplay/assets/17216760/86c91c0c-30d6-41b4-925c-dd74c7b9c488)

### Some time later

Have also created a .NET application to process the button presses.
![image](https://github.com/ajchellew/zwiftplay/assets/17216760/0a590f3b-7217-4f76-9652-8380fce3287a)
