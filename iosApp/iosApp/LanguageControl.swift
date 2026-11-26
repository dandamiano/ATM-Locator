import SharedLogic
import SwiftUI

struct LanguageControl: View {
    let language: AppLanguage
    let onLanguageChange: (AppLanguage) -> Void

    var body: some View {
        HStack(spacing: 2) {
            languageOption(
                label: L.text(StringKey.languageenglish, language),
                selected: language == AppLanguage.en
            ) {
                onLanguageChange(AppLanguage.en)
            }
            Text("|")
                .foregroundStyle(AtmTheme.muted)
            languageOption(
                label: L.text(StringKey.languagechichewa, language),
                selected: language == AppLanguage.ny
            ) {
                onLanguageChange(AppLanguage.ny)
            }
        }
        .padding(.horizontal, 4)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(AtmTheme.muted.opacity(0.6), lineWidth: 1)
        )
        .frame(minHeight: AtmTheme.minTap)
    }

    private func languageOption(
        label: String,
        selected: Bool,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Text(label)
                .font(selected ? .subheadline.weight(.semibold) : .subheadline)
                .foregroundStyle(selected ? AtmTheme.forest : AtmTheme.muted)
                .frame(minHeight: AtmTheme.minTap)
                .padding(.horizontal, 8)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}
