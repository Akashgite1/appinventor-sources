// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class I18nTranslationManagerTests: XCTestCase {
  func testLoadsExactSplitLanguageAndFormatsValues() {
    let provider = DictionaryI18nAssetProvider([
      "i18n/manifest.json": """
        {
          "version": 2,
          "languages": {
            "zh": "languages/zh.json",
            "zh-Hant-TW": "languages/zh-Hant-TW.json"
          },
          "entries": {
            "greeting": {"baseText": "Hello, {name}"}
          }
        }
        """,
      "i18n/languages/zh-Hant-TW.json": """
        {
          "version": 1,
          "language": "zh-Hant-TW",
          "entries": {"greeting": "您好，{name}"}
        }
        """
    ])
    let manager = I18nTranslationManager {
      return "zh-Hant-TW"
    }

    manager.load(from: provider)

    XCTAssertEqual("zh-Hant-TW", manager.activeLanguage)
    XCTAssertEqual(
        "您好，Akash",
        manager.lookupDynamic(
            "greeting",
            values: ["name": "Akash"]))
  }

  func testFallsBackToLanguageOnlyWhenRegionalFileCannotLoad() {
    let provider = DictionaryI18nAssetProvider([
      "i18n/manifest.json": """
        {
          "version": 2,
          "languages": {
            "pt": "languages/pt.json",
            "pt-BR": "languages/pt-BR.json"
          },
          "entries": {
            "greeting": {"baseText": "Hello"}
          }
        }
        """,
      "i18n/languages/pt.json": """
        {
          "version": 1,
          "language": "pt",
          "entries": {"greeting": "Olá"}
        }
        """
    ])
    let manager = I18nTranslationManager {
      return "pt-BR"
    }

    manager.load(from: provider)

    XCTAssertEqual("pt", manager.activeLanguage)
    XCTAssertEqual("Olá", manager.lookupDynamic("greeting"))
  }

  func testUsesBaseTextWhenDeviceLanguageIsUnavailable() {
    let provider = DictionaryI18nAssetProvider([
      "i18n/manifest.json": """
        {
          "version": 2,
          "languages": {"hi": "languages/hi.json"},
          "entries": {
            "greeting": {"baseText": "Hello"}
          }
        }
        """
    ])
    let manager = I18nTranslationManager {
      return "fr-FR"
    }

    manager.load(from: provider)

    XCTAssertEqual("", manager.activeLanguage)
    XCTAssertEqual("Hello", manager.lookupDynamic("greeting"))
  }

  func testFallsBackToLegacyAssetWhenManifestIsInvalid() {
    let provider = DictionaryI18nAssetProvider([
      "i18n/manifest.json": """
        {"version": 99, "languages": {}, "entries": {}}
        """,
      "i18n/translations.json": """
        {
          "entries": {
            "greeting": {
              "baseText": "Hello",
              "translations": {"hi": "नमस्ते"}
            }
          }
        }
        """
    ])
    let manager = I18nTranslationManager {
      return "hi-IN"
    }

    manager.load(from: provider)

    XCTAssertEqual("नमस्ते", manager.lookupDynamic("greeting"))
  }

  func testRejectsUnsafeLanguageAssetPath() {
    let provider = DictionaryI18nAssetProvider([
      "i18n/manifest.json": """
        {
          "version": 2,
          "languages": {"en": "../secret.json"},
          "entries": {
            "greeting": {"baseText": "Hello"}
          }
        }
        """,
      "i18n/../secret.json": """
        {
          "version": 1,
          "language": "en",
          "entries": {"greeting": "Secret"}
        }
        """
    ])
    let manager = I18nTranslationManager {
      return "en"
    }

    manager.load(from: provider)

    XCTAssertEqual("Hello", manager.lookupDynamic("greeting"))
    XCTAssertFalse(provider.openedAssets.contains("i18n/../secret.json"))
  }

  func testPreviewLanguageOverridesLegacyDeviceLanguage() {
    let manager = I18nTranslationManager {
      return "en-US"
    }
    manager.setPreviewLanguageOverride("hi")
    manager.loadFromJSON("""
      {
        "entries": {
          "greeting": {
            "baseText": "Hello",
            "translations": {"hi": "नमस्ते"}
          }
        }
      }
      """)

    XCTAssertEqual("नमस्ते", manager.lookupDynamic("greeting"))
  }
}

private enum I18nAssetError: Error {
  case missingAsset
}

private final class DictionaryI18nAssetProvider:
    I18nTranslationAssetProvider {
  private let assets: [String: String]
  private(set) var openedAssets: [String] = []

  init(_ assets: [String: String]) {
    self.assets = assets
  }

  func openI18nAsset(_ assetPath: String) throws -> Data {
    openedAssets.append(assetPath)

    guard let contents = assets[assetPath],
        let data = contents.data(using: .utf8) else {
      throw I18nAssetError.missingAsset
    }

    return data
  }
}
