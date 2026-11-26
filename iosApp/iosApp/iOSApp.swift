import SharedLogic
import SwiftUI

@main
struct iOSApp: App {
    private let store = LocatorFactory.makeStore()

    var body: some Scene {
        WindowGroup {
            ContentView(store: store)
                .preferredColorScheme(.light)
        }
    }
}
