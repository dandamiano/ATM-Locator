import Foundation
import SharedLogic

enum L {
    static func text(_ key: StringKey, _ language: AppLanguage, n: Int64? = nil) -> String {
        let boxed: KotlinLong? = n.map { KotlinLong(value: $0) }
        return StringTablesKt.text(key: key, language: language, n: boxed)
    }

    static func formatted(_ relative: RelativeTimePresentation, _ language: AppLanguage) -> String? {
        relative.formatted(language: language)
    }
}

enum ToastCopy {
    static func message(for error: ApiError) -> String {
        if let bad = error as? ApiError.BadRequest {
            let msg = bad.message?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            return msg.isEmpty ? "Request could not be completed" : msg
        }
        if let rate = error as? ApiError.RateLimited {
            if let waitBox = rate.retryAfterSeconds {
                return "Too many requests. Try again in \(waitBox.int64Value)s"
            }
            return "Too many requests. Try again later"
        }
        if error is ApiError.Offline {
            return "You are offline"
        }
        if error is ApiError.NotConfigured {
            return "API is not configured"
        }
        if error is ApiError.UnsupportedUntilOpenApi {
            return "ATM search is not available yet"
        }
        if error is ApiError.NotFound {
            return "Not found"
        }
        if let unexpected = error as? ApiError.Unexpected {
            let msg = unexpected.message?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            return msg.isEmpty ? "Something went wrong" : msg
        }
        return "Something went wrong"
    }
}
