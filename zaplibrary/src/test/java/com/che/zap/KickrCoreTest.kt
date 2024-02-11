package com.che.zap

import com.che.zap.utils.ProtoDecode
import org.junit.Test

class KickrCoreTest {

    // three

    // idle - repeats at 1 pps
    // 0x08 0x00 0x10 0x00 0x18 0x00 0x20 0x00 0x28 0x00 0x30 0xCC 0x81 0x02
    // spinning
    // 0x08 0x00 0x10 0x00 0x18 0x9F 0x01 0x20 0x00 0x28 0x92 0x24 0x30 0xCC 0x81 0x02
    // spinning less
    // 0x08 0x00 0x10 0x00 0x18 0x70 0x20 0x00 0x28 0xB1 0x19 0x30 0xCC 0x81 0x02

    // 2A
    // connecting ?
    // 0x08 0x03 0x12 0x11 0x22 0x0F 0x41 0x54 0x58 0x20 0x30 0x31 0x2C 0x20 0x53 0x54 0x58 0x20 0x30
    // 0x08 0x03 0x12 0x11 0x22 0x0F 0x41 0x54 0x58 0x20 0x30 0x31 0x2C 0x20 0x53 0x54 0x58 0x20 0x30

    // connected?
    // 0x08 0x03 0x12 0x0D 0x22 0x0B 0x52 0x49 0x44 0x45 0x5F 0x4F 0x4E 0x28 0x30 0x29 0x00
    // 0x08 0x03 0x12 0x27 0x22 0x25 0x67 0x61 0x70 0x5F 0x70 0x61 0x72 0x61 0x6D 0x73 0x5F 0x63 0x68

    @Test
    fun testData() {

        // trainer state idle
        //var message = byteArrayOf(0x08, 0x00, 0x10, 0x00, 0x18, 0x00, 0x20, 0x00, 0x28, 0x00, 0x30, 0xCC.toByte(), 0x81.toByte(), 0x02)
        //var result = protoToString(message)
        //println(result)

        // trainer state spinning
        //message = byteArrayOf(0x08, 0x00, 0x10, 0x00, 0x18, 0x9F.toByte(), 0x01, 0x20, 0x00, 0x28, 0x92.toByte(), 0x24, 0x30, 0xCC.toByte(), 0x81.toByte(), 0x02)
        //state = GenericProtoBufferDecoded(message)
        //assert(state != null)

        // connecting ?
        //message = byteArrayOf(0x08, 0x03, 0x12, 0x11, 0x22, 0x0F, 0x41, 0x54, 0x58, 0x20, 0x30, 0x31, 0x2C, 0x20, 0x53, 0x54, 0x58, 0x20, 0x30)
        //state = GenericProtoBufferDecoded(message)
        //assert(state != null)

        // connected?
        //message = byteArrayOf(0x08, 0x03, 0x12, 0x0D, 0x22, 0x0B, 0x52, 0x49, 0x44, 0x45, 0x5F, 0x4F, 0x4E, 0x28, 0x30, 0x29, 0x00)
        //result = protoToString(message)
        //println(result)
    }


    @Test
    fun captureData() {

        // was manually processing capture, then did the csv below this test

        // 03 is the status sent constantly
        // 04 to kickr changing gear
        // 00 to kickr why?
        // 3c from kickr in response to gear change

        // data is from kickr unless marked otherwise

        val datas = arrayListOf(
            "03 08 00 10 00 18 00 20 00 28 00 30 cc 81 02",
            "03 08 42 10 00 18 b8 03 20 00 28 e5 63 30 cc 81 02",
            "03 08 9f 01 10 01 18 8c 04 20 00 28 d5 76 30 cc 81 02",
            "03 08 7b 10 23 18 a5 04 20 00 28 c1 7c 30 cc 81 02",
            "03 08 6e 10 27 18 d6 04 20 00 28 cb 87 01 30 cc 81 02",
            "03 08 69 10 29 18 e8 04 20 00 28 c4 8b 01 30 cc 81 02",
            "03 08 50 10 2c 18 80 05 20 00 28 81 91 01 30 cc 81 02",
            "03 08 31 10 31 18 92 05 20 00 28 88 95 01 30 cc 81 02",
            "03 08 13 10 37 18 bd 05 20 00 28 f8 9e 01 30 cc 81 02",
            "03 08 00 10 00 18 f8 04 20 00 28 a4 8f 01 30 cc 81 02",
            // To Feb 11, 2024 09:43:36.939844000
            "03 08 00 10 00 18 e0 04 20 00 28 dd 89 01 30 cc 81 02",
            "03 08 00 10 00 18 c7 04 20 00 28 86 84 01 30 cc 81 02",
            "03 08 00 10 00 18 ac 04 20 00 28 fd 7d 30 cc 81 02",
            "03 08 00 10 00 18 9a 04 20 00 28 fd 79 30 cc 81 02",
            "03 08 00 10 00 18 fe 03 20 00 28 c3 73 30 cc 81 02",
            "03 08 00 10 00 18 ea 03 20 00 28 8e 6f 30 cc 81 02",
            "03 08 3d 10 00 18 a7 04 20 00 28 80 7d 30 cc 81 02",
            //Feb 11, 2024 09:43:43.961858000
            "04 22 02 10 11", // to kickr   [changing from gear 12 down?]
            "03 08 6e 10 2f 18 de 05 20 00 28 b0 a6 01 30 cc 81 02",
            "03 08 71 10 35 18 f6 05 20 00 28 de ab 01 30 cc 81 02",
            "03 08 92 01 10 39 18 8e 06 20 00 28 8f b1 01 30 cc 81 02",
            "03 08 76 10 3a 18 9e 06 20 00 28 e5 b4 01 30 cc 81 02",
            "03 08 63 10 3c 18 a2 06 20 00 28 e8 b5 01 30 cc 81 02",
            "03 08 63 10 3d 18 ac 06 20 00 28 f4 b7 01 30 cc 81 02",
            //02 03 20 19 00 15 00 04 00 1b 50 00 03 08 00 10 00 18 dd 05 20 00 28 8b a6 01 30 cc 81 02
            // Feb 11, 2024 09:44:08.422405000
            "04 2a 04 10 b8 ad 01", // to kickr
            "3c 08 88 04 12 06 0a 04 40 b8 ad 01",
            "04 2a 04 10 b0 9f 01", // to kickr
            "3c 08 88 04 12 06 0a 04 40 b0 9f 01",
            "04 2a 04 10 a0 83 01", // to kickr
            "00 08 88 04", // to kickr
            "03 08 00 10 00 18 c1 05 20 00 28 dd 9f 01 30 cc 81 02",
            "3c 08 88 04 12 06 0a 04 40 a0 83 01",
            "04 2a 03 10 c4 77", // to kickr
            "04 2a 03 10 e8 6b", // to kickr
            "00 08 88 04", // to kickr
            "3c 08 88 04 12 05 0a 03 40 e8 6b",
            "04 2a 03 10 8c 60", // to kickr
            "00 08 88 04", // to kickr
            "3c 08 88 04 12 05 0a 03 40 8c 60",
            "04 2a 03 10 dc 56", // to kickr
            "04 2a 03 10 ac 4d", // to kickr
            "00 08 88 04", // to kickr
            "03 08 00 10 00 18 ac 05 20 00 28 8b 9b 01 30 cc 81 02",
            "3c 08 88 04 12 05 0a 03 40 ac 4d"
            // Feb 11, 2024 09:44:10.055401000
        )

        for (data in datas) {
            val cap = data.toHexByteArray()
            val sub = cap.copyOfRange(1, cap.size)
            val result = "" + cap[0] + ": " +  protoToString(sub)
            println(result)
        }
    }

    private val regex = Regex("[^A-Za-z0-9 ]") // theres some nonsense in the csv

    private val csvSplitRegex = Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")

    @Test
    fun captureCsvData() {

        // bt data capture running with play controllers and core. virtual shifting enabled
        // - started at gear 12.
        // - slowly spun up and down then
        // - worked down the gears to min 1
        // - worked up the gears to max 22 (?)
        // - returned to 11? or 12

        val raw = false // toggle between raw and protobuf read
        val outputLineNo = false
        val outputSource = false

        val csvStream = this.javaClass.classLoader?.getResourceAsStream("zwift-play-kickr-core.csv")
        if (csvStream != null) {
            val reader = csvStream.bufferedReader()
            reader.readLine() // header
            reader.forEachLine {

                val tokens = it.split(csvSplitRegex)

                val lineNo = tokens[0].toInt()

                var source = "In \t"
                if (tokens[1].contains("Phone"))
                    source = "Out\t"

                val sanitised = regex.replace(tokens[7], "") // works
                val data = sanitised.toHexByteArray()

                val outputData = if (raw) {
                    sanitised
                } else {
                    val sub = data.copyOfRange(1, data.size)
                    protoToString(sub)
                }

                val typeString = guessedType(data[0])


                // filters

                // trainer notifications only
                /*if (data[0] != 3.toByte())
                    return@forEachLine*/

                // no trainer notifications
                /*if (data[0] == 3.toByte())
                    return@forEachLine*/

                // ignoring the play data from the capture, only left it in for an idea when buttons were pressed
                if (tokens[1].contains("Play") || tokens[2].contains("Play"))
                    return@forEachLine


                var result = ""
                if (outputLineNo) result += "#$lineNo:  "
                if (outputSource) result += "$source "
                result += "$typeString $outputData"

                println(result)
            }
        }
    }


    //GearChangeCommand  : {  5: {  2: 30300 } }    - this doesn't look like its a protobuf
    //0                  : {  1: 520 } - this does
    //GearChangeResponse : {  1: 520 2: {  1: {  8: 30300 } } } - this does but maybe the inner isn't?

    // guess!
    private fun guessedType(type: Byte): String {
        return when (type) {
            3.toByte() ->  "TrainerNotification"
            4.toByte() ->  "GearChangeCommand  "
            60.toByte() -> "GearChangeResponse "
            else -> "$type                  "
        } + ":"
    }

    private fun protoToString(data: ByteArray): String {
        return ProtoDecode.decodeProto(data, true)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun String.toHexByteArray(): ByteArray = this.replace(" ", "").hexToByteArray()

}