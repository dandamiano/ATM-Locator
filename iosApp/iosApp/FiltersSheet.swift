import SharedLogic
import SwiftUI

struct FiltersSheet: View {
    @ObservedObject var observer: StoreObserver
    let onDismiss: () -> Void
    let onApply: () -> Void

    @State private var bankText = ""
    @State private var serviceText = ""
    @State private var congestionText = ""
    @State private var maxWaitText = ""

    private var language: AppLanguage { observer.state.lang }
    private var draft: AtmFilters { observer.state.draftFilters }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text(L.text(StringKey.filters, language))
                        .font(.title2.weight(.semibold))
                        .foregroundStyle(AtmTheme.ink)

                    labeledField(L.text(StringKey.filterbank, language), text: $bankText) { value in
                        observer.updateDraftFilters { $0.replacingBanks(bank: value) }
                    }

                    Text(L.text(StringKey.filterstatus, language))
                        .font(.headline)
                        .foregroundStyle(AtmTheme.ink)

                    statusChips

                    VStack(alignment: .leading, spacing: 4) {
                        Text(L.text(StringKey.filtermaxwait, language))
                            .font(.subheadline)
                            .foregroundStyle(AtmTheme.muted)
                        TextField("", text: $maxWaitText)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)
                            .onChange(of: maxWaitText) { _, value in
                                let digits = value.filter(\.isNumber)
                                if digits != value { maxWaitText = digits }
                                let minutes: KotlinInt? = Int32(digits).map { KotlinInt(value: $0) }
                                observer.updateDraftFilters { $0.replacingMaxWait(minutes: minutes) }
                            }
                        Text(L.text(StringKey.maxwaithelper, language))
                            .font(.caption)
                            .foregroundStyle(AtmTheme.muted)
                    }

                    labeledField(L.text(StringKey.filterservice, language), text: $serviceText) { value in
                        observer.updateDraftFilters { $0.replacingServices(service: value) }
                    }

                    labeledField(L.text(StringKey.filtercongestion, language), text: $congestionText) { value in
                        observer.updateDraftFilters { $0.replacingCongestion(value: value) }
                    }

                    HStack(spacing: 12) {
                        Button {
                            bankText = ""
                            serviceText = ""
                            congestionText = ""
                            maxWaitText = ""
                            observer.updateDraftFilters { _ in
                                FilterDraftHelpersKt.emptyAtmFilters()
                            }
                        } label: {
                            Text(L.text(StringKey.clear, language))
                                .font(.headline)
                                .foregroundStyle(AtmTheme.forest)
                                .frame(maxWidth: .infinity)
                                .frame(minHeight: AtmTheme.minTap)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(AtmTheme.forest, lineWidth: 1.5)
                                )
                        }
                        Button(action: onApply) {
                            Text(L.text(StringKey.apply, language))
                                .font(.headline)
                                .foregroundStyle(AtmTheme.paper)
                                .frame(maxWidth: .infinity)
                                .frame(minHeight: AtmTheme.minTap)
                                .background(AtmTheme.forest, in: RoundedRectangle(cornerRadius: 10))
                        }
                    }
                    .padding(.top, 12)
                }
                .padding(16)
            }
            .background(AtmTheme.paper.ignoresSafeArea())
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close", action: onDismiss)
                }
            }
        }
        .onAppear {
            bankText = draft.firstBankOrEmpty()
            serviceText = draft.firstServiceOrEmpty()
            congestionText = draft.congestionOrEmpty()
            maxWaitText = draft.maxWaitTextOrEmpty()
        }
    }

    private var statusChips: some View {
        let codes = draft.defaultStatusCodes()
        return FlowLayout(spacing: 8) {
            ForEach(codes, id: \.self) { code in
                let selected = draft.containsStatus(code: code)
                let label = L.text(StatusPresentationKt.presentStatus(code: code).labelKey, language)
                Button {
                    observer.updateDraftFilters { $0.togglingStatus(code: code) }
                } label: {
                    Text(label)
                        .font(.caption.weight(.medium))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 8)
                        .foregroundStyle(selected ? AtmTheme.paper : AtmTheme.ink)
                        .background(
                            selected ? AtmTheme.forest : AtmTheme.surfaceVariant,
                            in: Capsule()
                        )
                }
                .buttonStyle(.plain)
            }
        }
    }

    private func labeledField(
        _ title: String,
        text: Binding<String>,
        onChange: @escaping (String) -> Void
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.subheadline)
                .foregroundStyle(AtmTheme.muted)
            TextField("", text: text)
                .textFieldStyle(.roundedBorder)
                .onChange(of: text.wrappedValue) { _, value in
                    onChange(value)
                }
        }
    }
}

/// Simple wrapping flow for filter chips.
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        layout(in: proposal.width ?? 0, subviews: subviews).size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = layout(in: bounds.width, subviews: subviews)
        for (index, frame) in result.frames.enumerated() {
            subviews[index].place(
                at: CGPoint(x: bounds.minX + frame.minX, y: bounds.minY + frame.minY),
                proposal: ProposedViewSize(frame.size)
            )
        }
    }

    private func layout(in width: CGFloat, subviews: Subviews) -> (size: CGSize, frames: [CGRect]) {
        var frames: [CGRect] = []
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var maxWidth: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > width, x > 0 {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            frames.append(CGRect(origin: CGPoint(x: x, y: y), size: size))
            rowHeight = max(rowHeight, size.height)
            x += size.width + spacing
            maxWidth = max(maxWidth, x - spacing)
        }
        return (CGSize(width: maxWidth, height: y + rowHeight), frames)
    }
}
