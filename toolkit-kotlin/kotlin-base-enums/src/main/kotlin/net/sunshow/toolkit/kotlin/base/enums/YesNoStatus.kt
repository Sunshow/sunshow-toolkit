package net.sunshow.toolkit.kotlin.base.enums

import net.sunshow.toolkit.kotlin.base.delegate.LoggerDelegate

/**
 * 是否状态
 * author: sunshow.
 */
enum class YesNoStatus(val value: Int, val alias: String) {

    NO(0, "否"),
    YES(1, "是");

    companion object {
        private val logger by LoggerDelegate()


        private var MAP: Map<Int, YesNoStatus>
        private var LIST: List<YesNoStatus>

        init {
            val map = mutableMapOf<Int, YesNoStatus>()
            val list = mutableListOf<YesNoStatus>()
            for (v in values()) {
                map[v.value] = v
                list.add(v)
            }

            MAP = map.toMap() // to readonly
            LIST = list.toList()
        }

        operator fun get(value: Int): YesNoStatus? {
            return try {
                MAP[value]
            } catch (e: Exception) {
                logger.error(e.message, e)
                null
            }

        }

        fun list(): List<YesNoStatus> {
            return LIST
        }
    }
}
