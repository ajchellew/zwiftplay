package com.che.zwiftplayhost.service

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.util.isEmpty
import com.che.zap.device.ZapConstants.BC1
import com.che.zap.device.ZapConstants.ZWIFT_MANUFACTURER_ID
import com.che.zap.device.ZapConstants.RC1_LEFT_SIDE
import com.che.zap.device.ZapConstants.RC1_RIGHT_SIDE
import com.che.zwiftplayhost.R
import com.che.zwiftplayhost.ble.BleControllerScanner
import com.che.zwiftplayhost.ble.ZwiftAccessoryBleManager
import com.che.zap.utils.Logger
import com.che.zwiftplayhost.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch


class BluetoothService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1

        const val DATA_PLANE_ACTION = "DataPlane"

        const val ON_INITIALISED_UPDATE = 1
        const val BATTERY_LEVEL_UPDATE = 2
    }

    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private var managerUpdateChannel: SendChannel<Pair<Int, ZwiftAccessoryBleManager>>? = null
    private val clientManagers = mutableMapOf<String, ZwiftAccessoryBleManager>()

    private lateinit var bleScanner: BleControllerScanner

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createChannel(this)
        val notification = NotificationCompat.Builder(this, NotificationHelper.SERVICE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(resources.getString(R.string.service_notification_channel_name))
            .setContentText(resources.getString(R.string.service_notification_channel_summary))
            .setAutoCancel(true)

        notification.foregroundServiceBehavior = FOREGROUND_SERVICE_IMMEDIATE

        // need to start service foreground
        startForeground(NOTIFICATION_ID, notification.build())

        registerReceiver(bluetoothReceiver, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        })

        bleScanner = BleControllerScanner(this)
        bleScanner.registerListener(object : BleControllerScanner.ScannerCallback {
            override fun onResult(scanResult: ScanResult) {
                //Logger.d(TAG, "Found BLE device ${scanResult.device.address}")
                synchronized(clientManagers) {
                    addDeviceFromScan(scanResult)
                    // if we find both controllers, or if click stop scanning
                    if (clientManagers.size == 2
                        || (clientManagers.size == 1 && clientManagers.values.first().typeByte == BC1))
                        bleScanner.stop()
                }
            }

            override fun onError(errorCode: Int) {
                Logger.e("Scanner Error code: $errorCode")
            }
        })

        // Startup scanning for controllers
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter?.isEnabled == true) startScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
        stopBleServices()
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (context == null) return

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                        BluetoothAdapter.STATE_ON -> startScanning()
                        BluetoothAdapter.STATE_OFF -> stopBleServices()
                    }
                }
                // we don't bond to controllers
                /*BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Log.d(TAG, "Bond state changed for device ${device?.address}: ${device?.bondState}")
                    when (device?.bondState) {
                        BluetoothDevice.BOND_BONDED -> addDevice(device)
                        BluetoothDevice.BOND_NONE -> removeDevice(device)
                    }
                }*/
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? =
        when (intent?.action) {
            DATA_PLANE_ACTION -> DataPlane()
            else -> null
        }

    override fun onUnbind(intent: Intent?): Boolean =
        when (intent?.action) {
            DATA_PLANE_ACTION -> {
                managerUpdateChannel = null
                true
            }
            else -> false
        }

    inner class DataPlane : Binder() {
        fun setManagerUpdateChannel(sendChannel: SendChannel<Pair<Int, ZwiftAccessoryBleManager>>) {
            managerUpdateChannel = sendChannel
        }
    }

    private fun startScanning() {
        bleScanner.start()
    }

    private fun stopBleServices() {
        clientManagers.values.forEach { clientManager ->
            clientManager.close()
        }
        clientManagers.clear()
    }

    private fun addDeviceFromScan(scanResult: ScanResult) {

        if (scanResult.scanRecord == null) return

        val device = scanResult.device

        if (!clientManagers.containsKey(device.address)) {
            Logger.d("Connecting ${device.address}")

            val manData = scanResult.scanRecord!!.manufacturerSpecificData
            if (manData == null || manData.isEmpty()) return

            // 2378 is the value in the manufacturer data
            val data = scanResult.scanRecord?.getManufacturerSpecificData(ZWIFT_MANUFACTURER_ID) ?: return

            // We expect a device of BrevetDeviceType.RC1 which is 2 or 3 depending on which side it is
            // or for a click BrevetDeviceType.BC1 which is 9
            val typeByte = data[0]
            if (typeByte != RC1_LEFT_SIDE && typeByte != RC1_RIGHT_SIDE && typeByte != BC1) {
                Logger.d("Unknown device type: $typeByte")
                return
            }

            val clientManager = ZwiftAccessoryBleManager(this, typeByte)
            clientManager.registerListener(bleManagerCallback)
            clientManager.connect(device).useAutoConnect(true).enqueue()
            clientManagers[device.address] = clientManager
        }
    }

    private fun removeDevice(device: BluetoothDevice) {
        clientManagers.remove(device.address)?.let {
            it.unregisterListener(bleManagerCallback)
            it.close()
        }
    }

    private val bleManagerCallback = object : ZwiftAccessoryBleManager.Callback {
        override fun initialised(address: String) {
            clientManagers[address]?.let {
                defaultScope.launch {
                    managerUpdateChannel?.send(Pair(ON_INITIALISED_UPDATE, it))
                }
            }
        }

        override fun batteryLevelUpdate(address: String, level: Int) {
            clientManagers[address]?.let {
                defaultScope.launch {
                    managerUpdateChannel?.send(Pair(BATTERY_LEVEL_UPDATE, it))
                }
            }
        }
    }
}