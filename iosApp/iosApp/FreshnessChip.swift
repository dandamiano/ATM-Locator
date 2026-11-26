import SharedLogic
import SwiftUI

/// Renders the shared freshness display model. Does not recompute minutes.
struct FreshnessChip: View {
    let presentation: FreshnessPresentation
    let language: AppLanguage

    var body: some View {
        let label = L.text(presentation.labelKey, language)
        let minutes = presentation.showMinutes ? presentation.minutesText : nil
        let body = minutes.map { "\(label) · \($0)" } ?? label
        let style = presentation.style

        Group {
            if style == FreshnessStyle.solidgold {
                Text(body)
                    .font(.caption.weight(.medium))
                    .foregroundStyle(AtmTheme.ink)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(AtmTheme.gold, in: RoundedRectangle(cornerRadius: 6))
            } else if style == FreshnessStyle.outlinegold {
                Text(body)
                    .font(.caption.weight(.medium))
                    .foregroundStyle(AtmTheme.ink)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .overlay(
                        RoundedRectangle(cornerRadius: 6)
                            .stroke(AtmTheme.gold, lineWidth: 1.5)
                    )
            } else {
                Text(body)
                    .font(.caption)
                    .foregroundStyle(AtmTheme.muted)
                    .padding(.vertical, 2)
            }
        }
    }
}
