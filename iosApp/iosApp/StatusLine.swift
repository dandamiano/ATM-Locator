import SharedLogic
import SwiftUI

struct StatusLine: View {
    let presentation: StatusPresentation
    let language: AppLanguage
    var emphasize: Bool = false

    var body: some View {
        let heavy = emphasize
            || presentation.iconKey == StatusIconKey.outofcash
            || presentation.iconKey == StatusIconKey.outofservice
        let label = L.text(presentation.labelKey, language)

        HStack(spacing: 6) {
            Image(systemName: iconName(for: presentation.iconKey))
                .font(.system(size: 16))
                .foregroundStyle(AtmTheme.ink)
                .accessibilityHidden(true)
            Text(label)
                .font(heavy ? .body.weight(.semibold) : .body)
                .foregroundStyle(AtmTheme.ink)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel(label)
    }

    private func iconName(for key: StatusIconKey) -> String {
        if key == StatusIconKey.operational {
            return "checkmark.circle.fill"
        }
        if key == StatusIconKey.unknown {
            return "info.circle.fill"
        }
        return "exclamationmark.triangle.fill"
    }
}
