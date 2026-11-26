import Combine
import Foundation
import SharedLogic

enum GpsUiState {
    case idle
    case loading
    case denied
    case timedOut
}

/// Thin ObservableObject over LocatorStore. Collects StateFlow via sharedLogic FlowWatcher.
@MainActor
final class StoreObserver: ObservableObject {
    let store: LocatorStore

    @Published private(set) var state: LocatorState
    @Published private(set) var gpsUi: GpsUiState = .idle

    private let locationProvider = IosLocationProvider()
    private var watcher: FlowWatcher<LocatorState>?

    init(store: LocatorStore) {
        self.store = store
        self.state = store.state.value as! LocatorState
        self.watcher = FlowWatchKt.watchLocatorState(store: store) { [weak self] next in
            DispatchQueue.main.async {
                self?.state = next
            }
        }
        store.loadCities()
    }

    deinit {
        watcher?.close()
    }

    func setLanguage(_ language: AppLanguage) {
        store.setLanguage(language: language)
    }

    func loadCities() {
        store.loadCities()
    }

    func selectCity(_ city: City) {
        store.selectCity(city: city)
    }

    func requestSearch(_ text: String) {
        store.requestSearch(text: text.trimmingCharacters(in: .whitespacesAndNewlines))
    }

    func requestNearby(latitude: Double, longitude: Double) {
        store.requestNearby(latitude: latitude, longitude: longitude)
    }

    func selectDetail(atmId: String) {
        store.selectDetail(atmId: atmId)
    }

    func clearSelection() {
        store.clearSelection()
    }

    func clearToast() {
        store.clearToast()
    }

    func updateDraftFilters(_ transform: @escaping (AtmFilters) -> AtmFilters) {
        store.updateDraftFilters(transform: transform)
    }

    func dismissDraftFilters() {
        store.dismissDraftFilters()
    }

    func applyDraftFilters() {
        store.applyDraftFilters()
        store.reloadActiveQuery()
    }

    func clearFilters() {
        store.clearFilters()
        store.reloadActiveQuery()
    }

    func reloadActiveQuery() {
        store.reloadActiveQuery()
    }

    func markGpsDenied() {
        gpsUi = .denied
    }

    func useMyLocation(onLocated: @escaping () -> Void) {
        Task { @MainActor in
            gpsUi = .loading
            let outcome = await locationProvider.currentLocation()
            switch outcome {
            case .fixed(let latitude, let longitude):
                gpsUi = .idle
                store.requestNearby(latitude: latitude, longitude: longitude)
                onLocated()
            case .denied:
                gpsUi = .denied
            case .timedOut:
                gpsUi = .timedOut
            }
        }
    }
}

enum LocatorFactory {
    static func makeStore() -> LocatorStore {
        let tag = Locale.preferredLanguages.first ?? "en"
        return IosLocatorFactoryKt.createIosLocatorStore(deviceLanguageTag: tag)
    }
}
