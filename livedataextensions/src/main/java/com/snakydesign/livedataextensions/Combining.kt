/**
 * Created by Adib Faramarzi
 */

@file:JvmName("Lives")
@file:JvmMultifileClass

package com.snakydesign.livedataextensions

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

/**
 * Merges this LiveData with another one, and emits any item that was emitted by any of them
 */
fun <T> LiveData<T>.mergeWith(vararg liveDatas: LiveData<T>): LiveData<T> {
    val mergeWithArray = mutableListOf<LiveData<T>>()
    mergeWithArray.add(this)
    mergeWithArray.addAll(liveDatas)
    return merge(mergeWithArray)
}


/**
 * Merges multiple LiveData, and emits any item that was emitted by any of them
 */
fun <T> merge(liveDataList: List<LiveData<T>>): LiveData<T> {
    val finalLiveData: MediatorLiveData<T> = MediatorLiveData()
    liveDataList.forEach { liveData ->

        liveData.value?.let {
            finalLiveData.value = it
        }

        finalLiveData.addSource(liveData) { source ->
            finalLiveData.value = source
        }
    }
    return finalLiveData
}

/**
 * Emits the `startingValue` before any other value.
 */
fun <T> LiveData<T>.startWith(startingValue: T?): LiveData<T> {
    val finalLiveData: MediatorLiveData<T> = MediatorLiveData()
    finalLiveData.value = startingValue
    finalLiveData.addSource(this) { source ->
        finalLiveData.value = source
    }
    return finalLiveData
}

/**
 * zips both of the LiveData and emits a value after both of them have emitted their values,
 * after that, emits values whenever any of them emits a value.
 *
 * The difference between combineLatest and zip is that the zip only emits after all LiveData
 * objects have a new value, but combineLatest will emit after any of them has a new value.
 */
fun <T, Y> zip(first: LiveData<T>, second: LiveData<Y>): LiveData<Pair<T, Y>> {
    return zip(first, second) { t, y -> Pair(t, y) }
}

fun <T, Y, Z> zip(first: LiveData<T>, second: LiveData<Y>, mapper: (T, Y) -> Z): LiveData<Z> {
    val finalLiveData: MediatorLiveData<Z> = MediatorLiveData()

    var firstEmitted = false
    var firstValue: T? = null

    var secondEmitted = false
    var secondValue: Y? = null
    finalLiveData.addSource(first) { value ->
        firstEmitted = true
        firstValue = value
        synchronized(finalLiveData) {
            if (firstEmitted && secondEmitted) {
                finalLiveData.value = mapper(firstValue!!, secondValue!!)
                firstEmitted = false
                secondEmitted = false
            }
        }
    }
    finalLiveData.addSource(second) { value ->
        secondEmitted = true
        secondValue = value
        synchronized(finalLiveData) {
            if (firstEmitted && secondEmitted) {
                finalLiveData.value = mapper(firstValue!!, secondValue!!)
                firstEmitted = false
                secondEmitted = false
            }
        }
    }
    return finalLiveData
}


/**
 * zips three LiveData and emits a value after all of them have emitted their values,
 * after that, emits values whenever any of them emits a value.
 *
 * The difference between combineLatest and zip is that the zip only emits after all LiveData
 * objects have a new value, but combineLatest will emit after any of them has a new value.
 */
fun <T, Y, X, Z> zip(first: LiveData<T>, second: LiveData<Y>, third: LiveData<X>, mapper: (T, Y, X) -> Z): LiveData<Z> {
    val finalLiveData: MediatorLiveData<Z> = MediatorLiveData()

    var firstEmitted = false
    var firstValue: T? = null

    var secondEmitted = false
    var secondValue: Y? = null

    var thirdEmitted = false
    var thirdValue: X? = null
    finalLiveData.addSource(first) { value ->
        firstEmitted = true
        firstValue = value
        if (firstEmitted && secondEmitted && thirdEmitted) {
            finalLiveData.value = mapper(firstValue!!, secondValue!!, thirdValue!!)
            firstEmitted = false
            secondEmitted = false
            thirdEmitted = false
        }
    }

    finalLiveData.addSource(second) { value ->
        secondEmitted = true
        secondValue = value
        if (firstEmitted && secondEmitted && thirdEmitted) {
            firstEmitted = false
            secondEmitted = false
            thirdEmitted = false
            finalLiveData.value = mapper(firstValue!!, secondValue!!, thirdValue!!)
        }
    }

    finalLiveData.addSource(third) { value ->
        thirdEmitted = true
        thirdValue = value
        if (firstEmitted && secondEmitted && thirdEmitted) {
            firstEmitted = false
            secondEmitted = false
            thirdEmitted = false
            finalLiveData.value = mapper(firstValue!!, secondValue!!, thirdValue!!)
        }
    }

    return finalLiveData
}

fun <T, Y, X> zip(first: LiveData<T>, second: LiveData<Y>, third: LiveData<X>): LiveData<Triple<T, Y, X>> {
    return zip(first, second, third) { t, y, x -> Triple(t, y, x) }
}

/**
 * Combines the latest values from two LiveData objects.
 * First emits after both LiveData objects have emitted a value, and will emit afterwards after any
 * of them emits a new value.
 *
 * The difference between combineLatest and zip is that the zip only emits after all LiveData
 * objects have a new value, but combineLatest will emit after any of them has a new value.
 */
fun <X, T, Z> combineLatest(first: LiveData<X>, second: LiveData<T>, mapper: (X, T) -> Z): LiveData<Z> {
    val finalLiveData: MediatorLiveData<Z> = MediatorLiveData()

    var firstEmitted = false
    var firstValue: X? = null

    var secondEmitted = false
    var secondValue: T? = null
    finalLiveData.addSource(first) { value ->
        firstEmitted = true
        firstValue = value
        synchronized(finalLiveData) {
            if (firstEmitted && secondEmitted) {
                finalLiveData.value = mapper(firstValue!!, secondValue!!)
            }
        }
    }
    finalLiveData.addSource(second) { value ->
        secondEmitted = true
        secondValue = value
        synchronized(finalLiveData) {
            if (firstEmitted && secondEmitted) {
                finalLiveData.value = mapper(firstValue!!, secondValue!!)
            }
        }
    }
    return finalLiveData
}
/**
 * Combines the latest values from two LiveData objects.
 * First emits after both LiveData objects have emitted a value, and will emit afterwards after any
 * of them emits a new value.
 *
 * The difference between combineLatest and zip is that the zip only emits after all LiveData
 * objects have a new value, but combineLatest will emit after any of them has a new value.
 */
fun <X, Y, T, Z> combineLatest(first: LiveData<X>, second: LiveData<Y>, third: LiveData<T>, mapper: (X, Y, T) -> Z): LiveData<Z> {
    val finalLiveData: MediatorLiveData<Z> = MediatorLiveData()

    var firstEmitted = false
    var firstValue: X? = null

    var secondEmitted = false
    var secondValue: Y? = null

    var thirdEmitted = false
    var thirdValue: T? = null
    finalLiveData.addSource(first) { value ->
        firstEmitted = true
        firstValue = value
        synchronized(finalLiveData) {
            if (firstEmitted && secondEmitted && thirdEmitted) {
                finalLiveData.value = mapper(firstValue!!, secondValue!!, thirdValue!!)
            }
        }
    }
    finalLiveData.addSource(second) { value ->
        secondEmitted = true
        secondValue = value
        synchronized(finalLiveData) {
            if (firstEmitted && secondEmitted && thirdEmitted) {
                finalLiveData.value = mapper(firstValue!!, secondValue!!, thirdValue!!)
            }
        }
    }
    finalLiveData.addSource(third) { value ->
        thirdEmitted = true
        thirdValue = value
        synchronized(finalLiveData) {
            if (firstEmitted && secondEmitted && thirdEmitted) {
                finalLiveData.value = mapper(firstValue!!, secondValue!!, thirdValue!!)
            }
        }
    }
    return finalLiveData
}