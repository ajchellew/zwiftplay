package com.che.zwiftplayemulator.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.che.zwiftplayemulator.R
import com.che.zwiftplayemulator.ble.AdvertiserManager
import com.che.zwiftplayemulator.ble.GattServerManager
import com.che.zwiftplayemulator.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

@SuppressLint("MissingPermission")
class EmulatorBluetoothService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1

        const val DATA_PLANE_ACTION = "DataPlane"
    }

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var advertiserManager: AdvertiserManager
    private lateinit var gattServerManager: GattServerManager

    private var previousName = ""

    private val defaultScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        advertiserManager = AdvertiserManager(bluetoothManager.adapter.bluetoothLeAdvertiser)
        gattServerManager = GattServerManager(this, bluetoothManager)

        NotificationHelper.createChannel(this)
        val notification = NotificationCompat.Builder(this, NotificationHelper.SERVICE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(resources.getString(R.string.service_notification_channel_name))
            .setContentText(resources.getString(R.string.service_notification_channel_summary))
            .setAutoCancel(true)

        notification.foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE

        // need to start service foreground
        startForeground(NOTIFICATION_ID, notification.build())

        registerReceiver(bluetoothReceiver, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        })

        gattServerManager.start()
        startAdvertising()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
        stopAdvertising()
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (context == null) return

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                        BluetoothAdapter.STATE_ON -> startAdvertising()
                        BluetoothAdapter.STATE_OFF -> stopAdvertising()
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Timber.d("Bond state changed for device ${device?.address}: ${device?.bondState}")
                    /*when (device?.bondState) {
                        BluetoothDevice.BOND_BONDED -> addDevice(device)
                        BluetoothDevice.BOND_NONE -> removeDevice(device)
                    }*/
                }
            }
        }
    }

    private fun startAdvertising() {
        advertiserManager.start()
    }

    private fun stopAdvertising() {
        advertiserManager.stop(null)
        //bluetoothManager.adapter.name = previousName
    }


    override fun onBind(intent: Intent?): IBinder? =
        when (intent?.action) {
            DATA_PLANE_ACTION -> DataPlane()
            else -> null
        }

    override fun onUnbind(intent: Intent?): Boolean =
        when (intent?.action) {
            DATA_PLANE_ACTION -> {
                //managerUpdateChannel = null
                true
            }
            else -> false
        }

    inner class DataPlane : Binder() {
        /*fun setManagerUpdateChannel(sendChannel: SendChannel<Pair<Int, ZwiftPlayBleManager>>) {
            managerUpdateChannel = sendChannel
        }*/
    }
}

