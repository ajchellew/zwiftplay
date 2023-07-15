# Zwift Play

An attempt to reverse engineer the Zwift Play controllers. 

## BLE Spec

The controller presents several standard Bluetooth services and one non-standard.

Generic Access Service
Device Information Service
Battery Information Service

and Zwift Plays unique Service:

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
