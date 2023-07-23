package com.che.zwiftplayhost.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.che.zwiftplayhost.ble.BlePermissions
import com.che.zwiftplayhost.ble.ZwiftPlayBleManager
import com.che.zwiftplayhost.databinding.ActivityMainBinding
import com.che.zwiftplayhost.databinding.RecyclerItemDebugLineBinding
import com.che.zwiftplayhost.service.BluetoothService
import com.che.zwiftplayhost.service.isServiceRunning
import com.che.zwiftplayhost.utils.Logger
import com.che.zwiftplayhost.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val PERMISSION_REQUEST_CODE = 76
    }

    private lateinit var binding: ActivityMainBinding

    private val adapter = DebugLineAdapter()

    private var gattServiceConn: GattServiceConn? = null
    private var gattServiceData: BluetoothService.DataPlane? = null
    private val bleManagerUpdateNotifications = Channel<Pair<Int, ZwiftPlayBleManager>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewDebug = binding.recyclerViewDebug
        recyclerViewDebug.layoutManager = LinearLayoutManager(this)
        recyclerViewDebug.adapter = adapter

        lifecycleScope.launch(Dispatchers.Main) {
            for (newValue in bleManagerUpdateNotifications) {
                val bleManager = newValue.second
                when (newValue.first) {
                    1 -> {
                        val textViewMac = if (bleManager.isLeft) binding.textLeftMac else binding.textRightMac
                        val textViewBattery = if (bleManager.isLeft) binding.textLeftBattery else binding.textRightBattery
                        textViewMac.text = bleManager.bluetoothDevice?.address?.substring(12)
                        textViewBattery.text = "${bleManager.batteryLevel}%"
                    }
                    2 -> {
                        val textViewBattery = if (bleManager.isLeft) binding.textLeftBattery else binding.textRightBattery
                        textViewBattery.text = "${bleManager.batteryLevel}%"
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Logger.registerListener(loggerListener)

        val latestGattServiceConn = GattServiceConn()
        if (bindService(Intent(BluetoothService.DATA_PLANE_ACTION, null, this, BluetoothService::class.java), latestGattServiceConn, 0)) {
            gattServiceConn = latestGattServiceConn
        }
    }

    override fun onResume() {
        super.onResume()

        // We need runtime permissions for anything with BLE. Just simply prompt for this on resume of activity.
        if (!BlePermissions.hasRequiredPermissions(this))
            BlePermissions.requestPermissions(this, PERMISSION_REQUEST_CODE)
        else if (!NotificationHelper.notificationsEnabled(this))
            NotificationHelper.requestPermission(this, PERMISSION_REQUEST_CODE)
        else if (!isServiceRunning(this, BluetoothService::class.java))
            startBluetoothService()
    }

    private fun startBluetoothService() {
        startForegroundService(Intent(this, BluetoothService::class.java))
        Logger.d(TAG, "Started Service")
    }

    override fun onStop() {
        super.onStop()
        Logger.unregisterListener(loggerListener)

        if (gattServiceConn != null) {
            unbindService(gattServiceConn!!)
            gattServiceConn = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, BluetoothService::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // if all permissions are granted start the service now.
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED)
                    return
            }
        }
    }

    private inner class GattServiceConn : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (BluetoothService::class.java.name == name?.className) {
                gattServiceData = service as BluetoothService.DataPlane
                gattServiceData?.setManagerUpdateChannel(bleManagerUpdateNotifications)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            if (BluetoothService::class.java.name == name?.className) {
                gattServiceData = null
            }
        }
    }

    // region Debug display

    private val loggerListener = object : Logger.LogCallback {
        override fun newLogLine(line: String) {
            // updates from BLE device are going to be in background,
            // so need to marshal to UI thread to update recycler
            lifecycleScope.launch(Dispatchers.Main) {
                adapter.addLine(line)
                binding.recyclerViewDebug.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    class DebugLineAdapter : RecyclerView.Adapter<DebugLineAdapter.ViewHolder>() {

        private val lines = arrayListOf<String>()

        class ViewHolder(val binding: RecyclerItemDebugLineBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val binding = RecyclerItemDebugLineBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.binding.text.text = lines[position]
        }

        override fun getItemCount() = lines.size

        fun addLine(text: String) {
            synchronized(lines) {
                lines.add(text)
                notifyItemInserted(lines.size - 1)
            }
        }
    }

    // endregion

}