import SwiftUI

enum AtmTheme {
    static let forest = Color(red: 0x12 / 255, green: 0x35 / 255, blue: 0x2c / 255)
    static let gold = Color(red: 0xc4 / 255, green: 0xa3 / 255, blue: 0x5a / 255)
    static let paper = Color(red: 0xf7 / 255, green: 0xf5 / 255, blue: 0xf0 / 255)
    static let ink = Color(red: 0x1a / 255, green: 0x1f / 255, blue: 0x1c / 255)
    static let muted = Color(red: 0x5c / 255, green: 0x65 / 255, blue: 0x60 / 255)
    static let surfaceVariant = Color(red: 0xec / 255, green: 0xe8 / 255, blue: 0xe0 / 255)

    /// Minimum interactive target (matches Android 48dp; above HIG 44pt floor).
    static let minTap: CGFloat = 48
}
