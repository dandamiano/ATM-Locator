import SharedLogic
import SwiftUI

struct ContentView: View {
    let store: LocatorStore

    var body: some View {
        LocatorRootView(store: store)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(store: LocatorFactory.makeStore())
    }
}
