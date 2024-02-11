package com.che.zap.device

import com.che.zap.device.common.ZapConstants.BC1
import com.che.zap.device.common.ZapConstants.RC1_LEFT_SIDE
import com.che.zap.device.common.ZapConstants.RC1_RIGHT_SIDE

enum class DeviceType(val description: String) {

    ZWIFT_PLAY_LEFT("Left Play"),
    ZWIFT_PLAY_RIGHT("Right Play"),
    ZWIFT_CLICK("Click"),

    WAHOO_KICKR_CORE("KICKR Core");

    companion object {
        fun fromZwiftManufacturerData(typeByte: Byte): DeviceType {
            return when (typeByte) {
                RC1_LEFT_SIDE -> ZWIFT_PLAY_LEFT
                RC1_RIGHT_SIDE -> ZWIFT_PLAY_RIGHT
                BC1 -> ZWIFT_CLICK
                else -> throw Exception("Unknown type")
            }
        }
    }

    fun isPlayController(): Boolean = this == ZWIFT_PLAY_LEFT || this == ZWIFT_PLAY_RIGHT
}