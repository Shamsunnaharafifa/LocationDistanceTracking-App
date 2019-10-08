package com.bs23.distancetrackingapp.extensionFunction

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

import com.bs23.distancetrackingapp.util.NonNullMediatorLiveData

fun <T> LiveData<T>.nonNull(): NonNullMediatorLiveData<T> {
    val mediator: NonNullMediatorLiveData<T> = NonNullMediatorLiveData()
    mediator.addSource(this) { it?.let { mediator.value = it } }
    return mediator
}

fun <T> NonNullMediatorLiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, androidx.lifecycle.Observer {
        it?.let(observer)
    })

    fun coerceAtLeast(i: Int, any: Any) {
    }

}