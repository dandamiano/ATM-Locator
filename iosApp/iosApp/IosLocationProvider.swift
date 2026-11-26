import CoreLocation
import Foundation

enum LocationOutcome {
    case fixed(latitude: Double, longitude: Double)
    case denied
    case timedOut
}

/// Core Location when-in-use. Returns lat/lng only — never uploads anything else.
final class IosLocationProvider: NSObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var continuation: CheckedContinuation<LocationOutcome, Never>?
    private var timeoutWork: DispatchWorkItem?

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    func currentLocation(timeoutSeconds: TimeInterval = 12) async -> LocationOutcome {
        let status = manager.authorizationStatus
        switch status {
        case .denied, .restricted:
            return .denied
        case .notDetermined:
            let granted = await requestWhenInUse()
            if !granted { return .denied }
        case .authorizedWhenInUse, .authorizedAlways:
            break
        @unknown default:
            return .denied
        }

        if !CLLocationManager.locationServicesEnabled() {
            return .timedOut
        }

        return await withCheckedContinuation { cont in
            self.continuation = cont
            let work = DispatchWorkItem { [weak self] in
                self?.finish(.timedOut)
            }
            self.timeoutWork = work
            DispatchQueue.main.asyncAfter(deadline: .now() + timeoutSeconds, execute: work)
            manager.requestLocation()
        }
    }

    private func requestWhenInUse() async -> Bool {
        await withCheckedContinuation { cont in
            self.authContinuation = cont
            manager.requestWhenInUseAuthorization()
        }
    }

    private var authContinuation: CheckedContinuation<Bool, Never>?

    private func finish(_ outcome: LocationOutcome) {
        timeoutWork?.cancel()
        timeoutWork = nil
        guard let continuation else { return }
        self.continuation = nil
        continuation.resume(returning: outcome)
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard let authContinuation else { return }
        let status = manager.authorizationStatus
        switch status {
        case .notDetermined:
            return
        case .authorizedWhenInUse, .authorizedAlways:
            self.authContinuation = nil
            authContinuation.resume(returning: true)
        default:
            self.authContinuation = nil
            authContinuation.resume(returning: false)
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else {
            finish(.timedOut)
            return
        }
        finish(.fixed(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude))
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        if let clError = error as? CLError, clError.code == .denied {
            finish(.denied)
        } else {
            finish(.timedOut)
        }
    }
}
