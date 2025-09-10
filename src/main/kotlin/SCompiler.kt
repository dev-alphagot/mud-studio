package io.github.devalphagot

import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.String

fun Byte.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(Byte.SIZE_BYTES)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(this)
    return buffer.array()
}

fun Short.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(Short.SIZE_BYTES)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.putShort(this)
    return buffer.array()
}

fun Int.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(Int.SIZE_BYTES)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.putInt(this)
    return buffer.array()
}

val tMap = mutableMapOf<String, MutableList<String>>()

lateinit var scrMap: Map<String, String>

fun compile(){
    scrMap = json.decodeFromStream<Map<String, String>>(File("workspace/script_names.json").inputStream())

    val sc = File("workspace/scripts/").listFiles()
    val sci = sc.map { it.nameWithoutExtension }

    File("compiled/text/997.csv").writeText(
        sci.mapIndexed { i, it -> "${i}\t${scrMap[it] ?: ""}" }.joinToString("\n")
    )

    sc.forEach { file ->
        val ir = mutableListOf<ByteArray>()
        val opcodeSizes = mutableListOf<Int>()
        var cc = chars["narrator"]!!

        fun tAdd(s: String): Int {
            if(cc.locator !in tMap.keys) tMap[cc.locator] = mutableListOf<String>(cc._name)
            if(tMap[cc.locator]!!.isEmpty()) tMap[cc.locator]!!.add(cc._name)
            tMap[cc.locator]!!.add(s)

            return tMap[cc.locator]!!.indexOf(s)
        }

        file.readLines().map { it.trim() }.forEachIndexed { pc, ln ->
            if(ln.isEmpty()){
                opcodeSizes.add(0)
                return@forEachIndexed
            }

            val opc = ln.substring(0..3)
            opcodeSizes.add(when(opc){
                "chrs" -> 3
                "bgmp" -> 3
                "text" -> 5
                "sela" -> 8
                "seld" -> 1
                "jump" -> 3
                "cpjm" -> 4
                "wait" -> 3
                "rgsv" -> 3
                "rgld" -> 3
                "sndp" -> 3
                "next" -> 3
                "addi" -> 2
                "subi" -> 2
                "adds" -> 3
                "subs" -> 3
                "tclr" -> 2
                "regs" -> 2
                "clea" -> 1
                "RST!" -> 1
                "END!" -> 1
                else  -> 0
            })
        }

        fun jumpLocator(li: Int): Short {
            if(li <= 1) return 0

            return try {
                opcodeSizes.subList(0, li - 1).sum().toShort() // li - 1 하는 게 맞음 (줄 번호를 1..n -> 0..n-1로 만드는 것)
            } catch(_: Exception) {
                (opcodeSizes.sum()).toShort()
            }
        }

        file.readLines().map { it.trim() }.forEachIndexed { pc, lno ->
            if(lno.isEmpty()) return@forEachIndexed

            val ln = lno.split("//")[0].trim()
            if(ln.isEmpty()) return@forEachIndexed

            val opc = ln.substring(0..3)
            val opr = if(ln.length >= 6) ln.substring(5) else ""

            when(opc){
                "chrs" -> {
                    cc = chars[opr] ?: throw Exception("character not found")
                    ir.add(
                        0x01.toByte().toByteArray() + cc.id.toShort().toByteArray()
                    )
                }
                "bgmp" -> {
                    ir.add(
                        0x02.toByte().toByteArray() + opr.toShort().toByteArray()
                    )
                }
                "text" -> {
                    ir.add(
                        0x03.toByte().toByteArray() + ((cc.id * 100000) +
                                tAdd(opr)).toByteArray()
                    )
                }
                "sela" -> {
                    val op1 = opr.split(", ")[0].toByte().toByteArray()
                    val op2 = jumpLocator(opr.split(", ")[1].toInt()).toByteArray()
                    val op3 = opr.split(", ")
                        .run { subList(2, size) }.joinToString(", ")

                    ir.add(
                        0x04.toByte().toByteArray() + op1 + op2 + ((cc.id * 100000) +
                                tAdd(op3)).toByteArray()
                    )
                }
                "seld" -> {
                    ir.add(
                        0x05.toByte().toByteArray()
                    )
                }
                "jump" -> {
                    ir.add(
                        0x06.toByte().toByteArray() + jumpLocator(opr.toInt()).toByteArray()
                    )
                }
                "cpjm" -> {
                    val op1 = opr.split(", ")[0].toByte().toByteArray()
                    val op2 = jumpLocator(opr.split(", ")[1].toInt()).toByteArray()

                    ir.add(
                        0x07.toByte().toByteArray() + op1 + op2
                    )
                }
                "wait" -> {
                    ir.add(
                        0x08.toByte().toByteArray() + opr.toUShort().toShort().toByteArray()
                    )
                }
                "rgsv" -> {
                    ir.add(
                        0x09.toByte().toByteArray() + opr.toShort().toByteArray()
                    )
                }
                "rgld" -> {
                    ir.add(
                        0x0A.toByte().toByteArray() + opr.toShort().toByteArray()
                    )
                }
                "sndp" -> {
                    ir.add(
                        0x0B.toByte().toByteArray() + opr.toShort().toByteArray()
                    )
                }
                "next" -> {
                    ir.add(
                        0x0C.toByte().toByteArray() + sci.indexOf(opr).toShort().toByteArray()
                    )
                }
                "addi" -> {
                    ir.add(
                        0x0D.toByte().toByteArray() + opr.toByte().toByteArray()
                    )
                }
                "subi" -> {
                    ir.add(
                        0x0E.toByte().toByteArray() + opr.toByte().toByteArray()
                    )
                }
                "adds" -> {
                    ir.add(
                        0x0F.toByte().toByteArray() + opr.toShort().toByteArray()
                    )
                }
                "subs" -> {
                    ir.add(
                        0x10.toByte().toByteArray() + opr.toShort().toByteArray()
                    )
                }
                "tclr" -> {
                    ir.add(
                        0x11.toByte().toByteArray() + ConsoleColor.valueOf(opr).ordinal.toByte().toByteArray()
                    )
                }
                "regs" -> {
                    ir.add(
                        0x12.toByte().toByteArray() + opr.toByte().toByteArray()
                    )
                }
                "clea" -> {
                    ir.add(
                        0x13.toByte().toByteArray()
                    )
                }
                "RST!" -> {
                    ir.add(
                        0x14.toByte().toByteArray()
                    )
                }
                "END!" -> {
                    ir.add(
                        0x15.toByte().toByteArray()
                    )
                }
            }
        }

        val ba = ByteBuffer.allocate(opcodeSizes.sum())
        ir.forEach { `in` -> ba.put(`in`) }

        File("compiled/scripts/${file.nameWithoutExtension}.bin").writeBytes(ba.array())

        println("${file.nameWithoutExtension} COMPLETED")
    }

    chars.forEach { (s, c) ->
        File("compiled/text/${c.id}.csv").writeText(
            (
                    tMap[s]?.mapIndexed { id, str ->
                        "${id}\t${str}"
                    }?.joinToString("\n") ?: "0\t${c._name}"
            )
        )
    }

//    tMap.forEach {
//        File("compiled/text/${chars[it.key]!!.id}.csv").writeText(
//            it.value.mapIndexed { id, str ->
//                "${id}\t${str}"
//            }.joinToString("\n")
//        )
//    }

    File("compiled/script_index.txt").writeText(sci.joinToString("\n"))

    File("compiled/text/996.csv").writeText(
        json.decodeFromStream<Map<String, String>>(File("workspace/script_comments.json").inputStream()).toList().sortedBy { it.first }.let {
            it.joinToString("\n"){ i ->
                "${it.indexOf(i)}\t${i.second}"
            }
        }
    ) // .filter { it.first in wCollMap.keys }
}
