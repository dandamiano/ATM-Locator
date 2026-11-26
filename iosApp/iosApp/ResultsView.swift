import SharedLogic
import SwiftUI

struct ResultsView: View {
    @ObservedObject var observer: StoreObserver
    let onBack: () -> Void
    let onOpenFilters: () -> Void
    let onAtmClick: (String) -> Void
    let onChangeCity: () -> Void

    private var language: AppLanguage { observer.state.lang }
    private var state: LocatorState { observer.state }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 4) {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.body.weight(.semibold))
                        .foregroundStyle(AtmTheme.ink)
                        .frame(width: AtmTheme.minTap, height: AtmTheme.minTap)
                        .contentShape(Rectangle())
                }
                .accessibilityLabel(L.text(StringKey.changecity, language))

                VStack(alignment: .leading, spacing: 2) {
                    Text(placeLabel(state.query))
                        .font(.title3.weight(.semibold))
                        .foregroundStyle(AtmTheme.ink)
                    Text(L.text(StringKey.atmcount, language, n: Int64(state.results.count)))
                        .font(.caption)
                        .foregroundStyle(AtmTheme.muted)
                }
                Spacer(minLength: 4)
                Button(L.text(StringKey.filters, language), action: onOpenFilters)
                    .frame(minHeight: AtmTheme.minTap)
                LanguageControl(language: language, onLanguageChange: observer.setLanguage)
            }
            .padding(.horizontal, 4)
            .padding(.vertical, 4)

            if state.resultsOutdated {
                Text(L.text(StringKey.maybeoutdated, language))
                    .font(.body)
                    .foregroundStyle(AtmTheme.muted)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
            }

            content
        }
        .background(AtmTheme.paper.ignoresSafeArea())
    }

    @ViewBuilder
    private var content: some View {
        if state.resultsLoading {
            ResultsSkeleton()
        } else if state.nearbyUnsupported {
            EmptyMessage(
                message: L.text(StringKey.nearbyunavailable, language),
                language: language,
                showEmptyActions: false,
                showRetry: false,
                onClearFilters: { observer.clearFilters() },
                onChangeCity: onChangeCity,
                onRetry: { observer.reloadActiveQuery() }
            )
        } else if state.offline && state.results.isEmpty && !state.resultsOutdated {
            EmptyMessage(
                message: L.text(StringKey.offline, language),
                language: language,
                showEmptyActions: false,
                showRetry: true,
                onClearFilters: { observer.clearFilters() },
                onChangeCity: onChangeCity,
                onRetry: { observer.reloadActiveQuery() }
            )
        } else if state.results.isEmpty {
            EmptyMessage(
                message: L.text(StringKey.noatmsmatch, language),
                language: language,
                showEmptyActions: true,
                showRetry: false,
                onClearFilters: { observer.clearFilters() },
                onChangeCity: onChangeCity,
                onRetry: { observer.reloadActiveQuery() }
            )
        } else {
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(state.results, id: \.id) { row in
                        AtmListRow(model: row, language: language) {
                            onAtmClick(row.id)
                        }
                    }
                }
            }
        }
    }

    private func placeLabel(_ query: AtmQuery?) -> String {
        if let city = query as? AtmQuery.ByCity {
            return city.city.name
        }
        if query is AtmQuery.Nearby {
            return "Nearby"
        }
        if let text = query as? AtmQuery.Text {
            return text.text
        }
        return ""
    }
}

private struct ResultsSkeleton: View {
    var body: some View {
        VStack(spacing: 16) {
            ForEach(0..<4, id: \.self) { _ in
                HStack {
                    ProgressView()
                    Spacer()
                }
                .frame(height: 88)
                .padding(.horizontal, 16)
            }
        }
        .padding(.top, 16)
    }
}

private struct EmptyMessage: View {
    let message: String
    let language: AppLanguage
    let showEmptyActions: Bool
    let showRetry: Bool
    let onClearFilters: () -> Void
    let onChangeCity: () -> Void
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Text(message)
                .font(.title3.weight(.semibold))
                .foregroundStyle(AtmTheme.ink)
                .multilineTextAlignment(.center)
            if showEmptyActions {
                Button(action: onClearFilters) {
                    Text(L.text(StringKey.clearfilters, language))
                        .font(.headline)
                        .foregroundStyle(AtmTheme.paper)
                        .frame(maxWidth: .infinity)
                        .frame(minHeight: AtmTheme.minTap)
                        .background(AtmTheme.forest, in: RoundedRectangle(cornerRadius: 10))
                }
                Button(action: onChangeCity) {
                    Text(L.text(StringKey.changecity, language))
                        .font(.headline)
                        .foregroundStyle(AtmTheme.forest)
                        .frame(maxWidth: .infinity)
                        .frame(minHeight: AtmTheme.minTap)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(AtmTheme.forest, lineWidth: 1.5)
                        )
                }
            }
            if showRetry {
                Button(action: onRetry) {
                    Text(L.text(StringKey.retry, language))
                        .font(.headline)
                        .foregroundStyle(AtmTheme.paper)
                        .frame(maxWidth: .infinity)
                        .frame(minHeight: AtmTheme.minTap)
                        .background(AtmTheme.forest, in: RoundedRectangle(cornerRadius: 10))
                }
            }
            Spacer()
        }
        .padding(24)
    }
}
