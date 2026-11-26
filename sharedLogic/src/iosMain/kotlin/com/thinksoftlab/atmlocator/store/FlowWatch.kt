package com.thinksoftlab.atmlocator.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Collects a [StateFlow] on the main dispatcher for SwiftUI observers.
 * No SKIE required — call [close] when the observer leaves the scene.
 */
class FlowWatcher<T : Any>(
    flow: StateFlow<T>,
    onEach: (T) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val job: Job = scope.launch {
        flow.collect { onEach(it) }
    }

    fun close() {
        job.cancel()
        scope.cancel()
    }
}

fun watchLocatorState(
    store: LocatorStore,
    onEach: (LocatorState) -> Unit,
): FlowWatcher<LocatorState> = FlowWatcher(store.state, onEach)
