import SharedLogic
import SwiftUI

struct HomeView: View {
    @ObservedObject var observer: StoreObserver
    let onGoToResults: () -> Void

    @State private var searchText = ""
    @FocusState private var searchFocused: Bool

    private var language: AppLanguage { observer.state.lang }
    private var locationLoading: Bool { observer.gpsUi == .loading }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Text(L.text(StringKey.brandname, language))
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(AtmTheme.forest)
                Spacer()
                LanguageControl(language: language, onLanguageChange: observer.setLanguage)
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Text(L.text(StringKey.findusableatm, language))
                        .font(.largeTitle.weight(.semibold))
                        .foregroundStyle(AtmTheme.ink)
                        .padding(.top, 24)
                    Text(L.text(StringKey.homesupporting, language))
                        .font(.body)
                        .foregroundStyle(AtmTheme.muted)
                        .padding(.top, 8)

                    TextField(
                        L.text(StringKey.searchhint, language),
                        text: $searchText
                    )
                    .textFieldStyle(.roundedBorder)
                    .focused($searchFocused)
                    .submitLabel(.search)
                    .onSubmit(submitSearch)
                    .padding(.top, 16)

                    if observer.state.offline {
                        HStack {
                            Text(L.text(StringKey.offline, language))
                                .font(.body)
                                .foregroundStyle(AtmTheme.muted)
                            Spacer()
                            Button(L.text(StringKey.retry, language)) {
                                observer.loadCities()
                            }
                            .frame(minHeight: AtmTheme.minTap)
                        }
                        .padding(.top, 12)
                    }

                    if observer.state.citiesLoading && observer.state.cities.isEmpty {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                            .padding(24)
                    } else {
                        LazyVStack(alignment: .leading, spacing: 0) {
                            ForEach(observer.state.cities, id: \.id) { city in
                                Button {
                                    observer.selectCity(city)
                                    onGoToResults()
                                } label: {
                                    VStack(alignment: .leading, spacing: 0) {
                                        Text(city.name)
                                            .font(.title3)
                                            .foregroundStyle(AtmTheme.ink)
                                            .padding(.vertical, 12)
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                            .frame(minHeight: AtmTheme.minTap)
                                        Divider().overlay(AtmTheme.surfaceVariant)
                                    }
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.top, 8)
                    }
                }
                .padding(.horizontal, 16)
            }

            if observer.gpsUi == .denied || observer.gpsUi == .timedOut {
                HStack {
                    Text(L.text(StringKey.gpsdeniedtip, language))
                        .font(.body)
                        .foregroundStyle(AtmTheme.muted)
                    Spacer()
                    Button(L.text(StringKey.opensettings, language)) {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                    }
                    .frame(minHeight: AtmTheme.minTap)
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }

            Button {
                observer.useMyLocation(onLocated: onGoToResults)
            } label: {
                Text(
                    L.text(
                        locationLoading ? StringKey.gettinglocation : StringKey.usemylocation,
                        language
                    )
                )
                .font(.headline)
                .foregroundStyle(AtmTheme.paper)
                .frame(maxWidth: .infinity)
                .frame(minHeight: AtmTheme.minTap)
                .background(AtmTheme.forest, in: RoundedRectangle(cornerRadius: 10))
            }
            .disabled(locationLoading)
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
            .padding(.top, 8)
        }
        .background(AtmTheme.paper.ignoresSafeArea())
    }

    private func submitSearch() {
        let query = searchText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else { return }
        searchFocused = false
        observer.requestSearch(query)
        onGoToResults()
    }
}
