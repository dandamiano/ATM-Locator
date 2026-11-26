package com.thinksoftlab.atmlocator.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed class LocationOutcome {
    data class Fixed(val latitude: Double, val longitude: Double) : LocationOutcome()
    data object Denied : LocationOutcome()
    data object TimedOut : LocationOutcome()
}

class AndroidLocationProvider(
    private val context: Context,
) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(timeoutMs: Long = 12_000L): LocationOutcome {
        if (!isLocationEnabled()) {
            return LocationOutcome.TimedOut
        }
        val last = withTimeoutOrNull(2_000L) { awaitLastLocation() }
        if (last != null) {
            return LocationOutcome.Fixed(last.latitude, last.longitude)
        }
        val fresh = withTimeoutOrNull(timeoutMs) { awaitFreshLocation() }
        return if (fresh != null) {
            LocationOutcome.Fixed(fresh.latitude, fresh.longitude)
        } else {
            LocationOutcome.TimedOut
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitLastLocation(): android.location.Location? =
        suspendCancellableCoroutine { cont ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }

    @SuppressLint("MissingPermission")
    private suspend fun awaitFreshLocation(): android.location.Location? =
        suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1_000L)
                .setMaxUpdates(1)
                .setDurationMillis(10_000L)
                .build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    client.removeLocationUpdates(this)
                    if (cont.isActive) {
                        cont.resume(result.lastLocation)
                    }
                }
            }
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            cont.invokeOnCancellation {
                client.removeLocationUpdates(callback)
            }
        }

    private fun isLocationEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
