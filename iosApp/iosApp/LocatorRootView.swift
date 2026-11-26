import SharedLogic
import SwiftUI

private enum LocatorRoute: Hashable {
    case results
    case detail(String)
}

struct LocatorRootView: View {
    @StateObject private var observer: StoreObserver
    @State private var path: [LocatorRoute] = []
    @State private var showFilters = false
    @State private var toastMessage: String?

    init(store: LocatorStore) {
        _observer = StateObject(wrappedValue: StoreObserver(store: store))
    }

    var body: some View {
        NavigationStack(path: $path) {
            HomeView(observer: observer) {
                goToResults()
            }
            .navigationBarHidden(true)
            .navigationDestination(for: LocatorRoute.self) { route in
                switch route {
                case .results:
                    ResultsView(
                        observer: observer,
                        onBack: { popToHome() },
                        onOpenFilters: { showFilters = true },
                        onAtmClick: { id in
                            observer.selectDetail(atmId: id)
                            path.append(.detail(id))
                        },
                        onChangeCity: { popToHome() }
                    )
                    .navigationBarHidden(true)
                case .detail:
                    DetailView(observer: observer) {
                        observer.clearSelection()
                        if !path.isEmpty { path.removeLast() }
                    }
                    .navigationBarHidden(true)
                }
            }
        }
        .tint(AtmTheme.forest)
        .sheet(isPresented: $showFilters, onDismiss: {
            observer.dismissDraftFilters()
        }) {
            FiltersSheet(
                observer: observer,
                onDismiss: {
                    showFilters = false
                    observer.dismissDraftFilters()
                },
                onApply: {
                    observer.applyDraftFilters()
                    showFilters = false
                }
            )
            .presentationDetents([.large])
        }
        .onChange(of: observer.state.toastError) { _, error in
            guard let error else { return }
            toastMessage = ToastCopy.message(for: error)
            observer.clearToast()
        }
        .overlay(alignment: .bottom) {
            if let toastMessage {
                Text(toastMessage)
                    .font(.subheadline)
                    .foregroundStyle(AtmTheme.paper)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(AtmTheme.ink.opacity(0.92), in: Capsule())
                    .padding(.bottom, 32)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.4) {
                            withAnimation { self.toastMessage = nil }
                        }
                    }
            }
        }
        .animation(.easeInOut(duration: 0.2), value: toastMessage)
    }

    private func goToResults() {
        if path.last != .results {
            path = [.results]
        }
    }

    private func popToHome() {
        path.removeAll()
    }
}
