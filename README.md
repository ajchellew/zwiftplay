# Zwift Play

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

## Host app

The first step has been to create an app that pretends to be Zwift and listens to the real controllers. Immediate problem is no characteristics are giving any data so I imagine like [Sterzo](https://github.com/matandoocorpo/Zwift-Steer/issues/4) there is some kind of handshake.. 
