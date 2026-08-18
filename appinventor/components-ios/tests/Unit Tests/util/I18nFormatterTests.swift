// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

class I18nFormatterTests: XCTestCase {
  func testReplacesNamedPlaceholders() {
    let values = [
      "name": "Akash",
      "count": "5"
    ]

    XCTAssertEqual(
        "Hello Akash, you have 5 messages.",
        I18nFormatter.format(
            "Hello {name}, you have {count} messages.",
            values: values))
  }

  func testAllowsPlaceholderReorderingAcrossLanguages() {
    let values = [
      "name": "Akash",
      "count": "5"
    ]

    XCTAssertEqual(
        "Akash, आपके पास 5 संदेश हैं।",
        I18nFormatter.format(
            "{name}, आपके पास {count} संदेश हैं।",
            values: values))
  }

  func testKeepsMissingPlaceholdersVisible() {
    XCTAssertEqual(
        "Hello Akash, you have {count} messages.",
        I18nFormatter.format(
            "Hello {name}, you have {count} messages.",
            values: ["name": "Akash"]))
  }

  func testLeavesMalformedTemplateUnchangedFromErrorPoint() {
    XCTAssertEqual(
        "Hello {name",
        I18nFormatter.format(
            "Hello {name",
            values: ["name": "Akash"]))
  }

  func testReturnsTemplateWhenValuesAreEmpty() {
    XCTAssertEqual(
        "Hello {name}",
        I18nFormatter.format(
            "Hello {name}",
            values: nil))
  }
}
