package net.sunshow.toolkit.kotlin.base.enums

import net.sunshow.toolkit.kotlin.base.delegate.LoggerDelegate

/**
 * 启用禁用状态
 * author: sunshow.
 */
enum class EnableStatus(val value: Int, val alias: String) {

    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    companion object {
        private val logger by LoggerDelegate()


        private var MAP: Map<Int, EnableStatus>
        private var LIST: List<EnableStatus>

        init {
            val map = mutableMapOf<Int, EnableStatus>()
            val list = mutableListOf<EnableStatus>()
            for (v in values()) {
                map[v.value] = v
                list.add(v)
            }

            MAP = map.toMap() // to readonly
            LIST = list.toList()
        }

        operator fun get(value: Int): EnableStatus? {
            return try {
                MAP[value]
            } catch (e: Exception) {
                logger.error(e.message, e)
                null
            }

        }

        fun list(): List<EnableStatus> {
            return LIST
        }
    }
}
