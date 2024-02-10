package com.che.zap.proto

import com.google.protobuf.ByteString
import com.google.protobuf.CodedInputStream
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.WireFormat

class ClickNotification(message: ByteArray) {

    companion object {
        const val BTN_PRESSED = 0

        private const val UP_NAME = "Plus"
        private const val DOWN_NAME = "Minus"
    }

    var buttonUpPressed = false
    var buttonDownPressed = false

    init {
        // Taking ProtoDecode and simplifying it as we know the fields are probably enums and varints, (which are both varint wiretypes).

        val input: CodedInputStream = CodedInputStream.newInstance(ByteString.copyFrom(message).asReadOnlyByteBuffer())
        while (true) {
            val tag = input.readTag()
            val type: Int = WireFormat.getTagWireType(tag)
            if (tag == 0 || type == WireFormat.WIRETYPE_END_GROUP)
                break
            val number: Int = WireFormat.getTagFieldNumber(tag)
            when (type) {
                WireFormat.WIRETYPE_VARINT -> {
                    val value = input.readInt64().toInt() // biggest number we expect is an int
                    // NOTE: this is with 1.2.1 firmware. this could change...
                    when (number) {
                        1 -> buttonUpPressed = value == BTN_PRESSED
                        2 -> buttonDownPressed = value == BTN_PRESSED
                        else -> throw InvalidProtocolBufferException("Unexpected tag") // firmware change perhaps?
                    }
                }
                else -> throw InvalidProtocolBufferException("Unexpected wire type")
            }
        }
    }

    fun diff(previousNotification: ClickNotification): String {
        var diff = ""
        diff += diff(UP_NAME, buttonUpPressed, previousNotification.buttonUpPressed)
        diff += diff(DOWN_NAME, buttonDownPressed, previousNotification.buttonDownPressed)
        return diff
    }

    private fun diff(title: String, pressedValue: Boolean, oldPressedValue: Boolean): String {
        if (pressedValue != oldPressedValue)
            return "$title=${if (pressedValue) "Pressed" else "Released"} "
        return ""
    }

    override fun toString(): String {

        var text = "ClickNotification("

        text += if (buttonUpPressed) UP_NAME else ""
        text += if (buttonDownPressed) DOWN_NAME else ""

        text += ")"
        return text
    }
}

