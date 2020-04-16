package com.maxciv.scigraph.util

import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author maxim.oleynik
 * @since 16.04.2020
 */
class DataBuffer <T>(initialData: List<T> = listOf()) {

    private val data: CopyOnWriteArrayList<T> = CopyOnWriteArrayList(initialData.toMutableList())

    fun addItem(item: T) {
        data.add(item)
    }

    fun getItemsAndReset(): List<T> {
        data.toArray()
        return data.toList().also { data.clear() }
    }
}
