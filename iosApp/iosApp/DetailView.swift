import SharedLogic
import SwiftUI

struct DetailView: View {
    @ObservedObject var observer: StoreObserver
    let onBack: () -> Void

    private var language: AppLanguage { observer.state.lang }
    private var state: LocatorState { observer.state }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.body.weight(.semibold))
                        .foregroundStyle(AtmTheme.ink)
                        .frame(width: AtmTheme.minTap, height: AtmTheme.minTap)
                        .contentShape(Rectangle())
                }
                Spacer()
                LanguageControl(language: language, onLanguageChange: observer.setLanguage)
            }
            .padding(.horizontal, 4)
            .padding(.vertical, 4)

            if shouldShowEmpty {
                emptyBody
            } else if let detail = state.detail {
                DetailBody(model: detail, language: language)
            } else {
                emptyBody
            }
        }
        .background(AtmTheme.paper.ignoresSafeArea())
    }

    private var shouldShowEmpty: Bool {
        state.detailError is ApiError.NotFound
            || (state.detail == nil && state.nearbyUnsupported)
            || state.detail == nil
    }

    private var emptyBody: some View {
        VStack(spacing: 16) {
            Spacer()
            Text(L.text(StringKey.atmnotlisted, language))
                .font(.title3.weight(.semibold))
                .foregroundStyle(AtmTheme.ink)
                .multilineTextAlignment(.center)
            Button(action: onBack) {
                Text(L.text(StringKey.changecity, language))
                    .font(.headline)
                    .foregroundStyle(AtmTheme.paper)
                    .frame(minHeight: AtmTheme.minTap)
                    .padding(.horizontal, 24)
                    .background(AtmTheme.forest, in: RoundedRectangle(cornerRadius: 10))
            }
            Spacer()
        }
        .padding(24)
    }
}

private struct DetailBody: View {
    let model: AtmRowModel
    let language: AppLanguage

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                Text(model.atmName)
                    .font(.title.weight(.semibold))
                    .foregroundStyle(AtmTheme.ink)
                Text(model.bankName)
                    .font(.title3)
                    .foregroundStyle(AtmTheme.muted)

                StatusLine(presentation: model.status, language: language, emphasize: true)
                    .padding(.top, 4)
                FreshnessChip(presentation: model.freshness, language: language)

                if let queueLine = queueAndUpdatedLine() {
                    Text(queueLine)
                        .font(.caption)
                        .foregroundStyle(AtmTheme.muted)
                }

                let services = model.services
                if !services.isEmpty {
                    Text(services.joined(separator: " · "))
                        .font(.body)
                        .foregroundStyle(AtmTheme.ink)
                        .padding(.top, 8)
                }
                let brands = model.cardBrands
                if !brands.isEmpty {
                    Text(brands.joined(separator: " · "))
                        .font(.body)
                        .foregroundStyle(AtmTheme.muted)
                }
                if let hours = model.hours, !hours.isEmpty {
                    Text(hours)
                        .font(.body)
                        .foregroundStyle(AtmTheme.ink)
                        .padding(.top, 4)
                }
                let place = [model.town, model.district]
                    .compactMap { $0?.trimmingCharacters(in: .whitespacesAndNewlines) }
                    .filter { !$0.isEmpty }
                    .joined(separator: ", ")
                if !place.isEmpty {
                    Text(place)
                        .font(.body)
                        .foregroundStyle(AtmTheme.muted)
                }

                if model.hasDirections {
                    Button(action: openDirections) {
                        Text(L.text(StringKey.opendirections, language))
                            .font(.headline)
                            .foregroundStyle(AtmTheme.paper)
                            .frame(maxWidth: .infinity)
                            .frame(minHeight: AtmTheme.minTap)
                            .background(AtmTheme.forest, in: RoundedRectangle(cornerRadius: 10))
                    }
                    .padding(.top, 24)
                    .padding(.bottom, 16)
                }
            }
            .padding(.horizontal, 16)
        }
    }

    private func queueAndUpdatedLine() -> String? {
        let showQueue = model.queueCount != nil
            && model.lastUpdatedAtMillis != nil
            && model.freshness.style != FreshnessStyle.muted
        let updated = L.formatted(model.updated, language)
        guard showQueue || updated != nil else { return nil }
        var parts: [String] = []
        if showQueue, let count = model.queueCount {
            parts.append("Queue \(count.intValue)")
        }
        if let updated {
            parts.append(updated)
        }
        return parts.joined(separator: " · ")
    }

    private func openDirections() {
        let hint = model.directionsHint?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if !hint.isEmpty, let url = URL(string: hint) {
            UIApplication.shared.open(url)
            return
        }
        guard let lat = model.latitude?.doubleValue,
              let lng = model.longitude?.doubleValue else { return }
        let urlString = "http://maps.apple.com/?daddr=\(lat),\(lng)"
        if let url = URL(string: urlString) {
            UIApplication.shared.open(url)
        }
    }
}
