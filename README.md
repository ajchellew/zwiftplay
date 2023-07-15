# Zwift Play

An attempt to reverse engineer the Zwift Play controllers. 

BLE Spec

The controller presents several standard Bluetooth services and one non-standard.

Generic Access Service
Device Information Service
Battery Information Service

and Zwift Plays unique Service:

```
Service: PlayController UUID: 00000001-19ca-4651-86e5-fa29dcdd09d1
- Characteristic: 00000002-19ca-4651-86e5-fa29dcdd09d1 Properties: N 
  - Descriptor: 00002902-0000-1000-8000-00805f9b34fb
- Characteristic: 00000003-19ca-4651-86e5-fa29dcdd09d1 Properties: W W-NR 
- Characteristic: 00000004-19ca-4651-86e5-fa29dcdd09d1 Properties: I R 
  - Descriptor: 00002902-0000-1000-8000-00805f9b34fb
- Characteristic: 00000006-19ca-4651-86e5-fa29dcdd09d1 Properties: I R W W-NR
  - Descriptor: 00002902-0000-1000-8000-00805f9b34fb
```
