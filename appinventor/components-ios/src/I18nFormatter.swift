// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * Formats i18n templates containing named placeholders such as {name}.
 */
enum I18nFormatter {
  static func format(_ template: String?,
      values: [String: String]?) -> String {
    guard let template = template, !template.isEmpty else {
      return ""
    }

    guard let values = values, !values.isEmpty else {
      return template
    }

    var result = ""
    var index = template.startIndex

    while index < template.endIndex {
      guard let start = template[index...].firstIndex(of: "{") else {
        result.append(contentsOf: template[index...])
        break
      }

      let nameStart = template.index(after: start)

      guard let end = template[nameStart...].firstIndex(of: "}") else {
        result.append(contentsOf: template[index...])
        break
      }

      result.append(contentsOf: template[index..<start])

      let name = String(template[nameStart..<end])

      if let value = values[name] {
        result.append(value)
      } else {
        result.append(contentsOf: template[start...end])
      }

      index = template.index(after: end)
    }

    return result
  }
}
