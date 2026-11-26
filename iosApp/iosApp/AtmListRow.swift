import SharedLogic
import SwiftUI

struct AtmListRow: View {
    let model: AtmRowModel
    let language: AppLanguage
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 6) {
                HStack(alignment: .firstTextBaseline) {
                    Text(model.bankName)
                        .font(.title3.weight(.semibold))
                        .foregroundStyle(AtmTheme.ink)
                        .multilineTextAlignment(.leading)
                    Spacer(minLength: 8)
                    if let meters = model.distanceMeters?.doubleValue {
                        Text(formatDistance(meters))
                            .font(.caption)
                            .foregroundStyle(AtmTheme.muted)
                    }
                }
                Text(model.atmName)
                    .font(.body)
                    .foregroundStyle(AtmTheme.ink)
                    .multilineTextAlignment(.leading)
                StatusLine(presentation: model.status, language: language)
                FreshnessChip(presentation: model.freshness, language: language)
                let brands = model.rowCardBrands
                if !brands.isEmpty {
                    Text(brands.joined(separator: " · "))
                        .font(.caption)
                        .foregroundStyle(AtmTheme.muted)
                }
                if let updated = L.formatted(model.updated, language) {
                    Text(updated)
                        .font(.caption)
                        .foregroundStyle(AtmTheme.muted)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .frame(minHeight: AtmTheme.minTap)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        Divider()
            .overlay(AtmTheme.surfaceVariant)
            .padding(.leading, 16)
    }

    private func formatDistance(_ meters: Double) -> String {
        if meters >= 1000 {
            let km = (meters / 100.0).rounded() / 10.0
            return "\(km) km"
        }
        return "\(Int(meters.rounded())) m"
    }
}
