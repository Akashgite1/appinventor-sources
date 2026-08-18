// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class I18nTranslationManagerTests: XCTestCase {
  func testLoadsExactSplitLanguageAndFormatsValues() {
    let provider = DictionaryI18nTranslationProvider([
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
    let provider = DictionaryI18nTranslationProvider([
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
    let provider = DictionaryI18nTranslationProvider([
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
    let provider = DictionaryI18nTranslationProvider([
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
    let provider = DictionaryI18nTranslationProvider([
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
  func testAppliesSplitTranslationToMatchingComponent() {
    let label = I18nTranslationTarget()
    label.Text = "Original"
    let provider = DictionaryI18nTranslationProvider(
        [
          "i18n/manifest.json": """
            {
              "version": 2,
              "languages": {"hi": "languages/hi.json"},
              "entries": {
                "labelText": {
                  "baseText": "Hello",
                  "source": {
                    "screen": "Screen1",
                    "component": "Label1",
                    "property": "Text"
                  }
                }
              }
            }
            """,
          "i18n/languages/hi.json": """
            {
              "version": 1,
              "language": "hi",
              "entries": {"labelText": "नमस्ते"}
            }
            """
        ],
        components: ["Label1": label])
    let manager = I18nTranslationManager {
      return "hi-IN"
    }

    manager.load(from: provider)

    XCTAssertEqual("नमस्ते", label.Text)
  }

  func testAppliesBaseTextWhenSplitTranslationIsMissing() {
    let label = I18nTranslationTarget()
    label.Text = "Original"
    let provider = DictionaryI18nTranslationProvider(
        [
          "i18n/manifest.json": """
            {
              "version": 2,
              "languages": {"hi": "languages/hi.json"},
              "entries": {
                "labelText": {
                  "baseText": "Hello",
                  "source": {
                    "screen": "Screen1",
                    "component": "Label1",
                    "property": "Text"
                  }
                }
              }
            }
            """,
          "i18n/languages/hi.json": """
            {
              "version": 1,
              "language": "hi",
              "entries": {}
            }
            """
        ],
        components: ["Label1": label])
    let manager = I18nTranslationManager {
      return "hi"
    }

    manager.load(from: provider)

    XCTAssertEqual("Hello", label.Text)
  }

  func testDoesNotApplyEntryForAnotherScreen() {
    let label = I18nTranslationTarget()
    label.Text = "Original"
    let provider = DictionaryI18nTranslationProvider(
        [
          "i18n/manifest.json": """
            {
              "version": 2,
              "languages": {"hi": "languages/hi.json"},
              "entries": {
                "labelText": {
                  "baseText": "Hello",
                  "source": {
                    "screen": "Screen2",
                    "component": "Label1",
                    "property": "Text"
                  }
                }
              }
            }
            """,
          "i18n/languages/hi.json": """
            {
              "version": 1,
              "language": "hi",
              "entries": {"labelText": "नमस्ते"}
            }
            """
        ],
        components: ["Label1": label])
    let manager = I18nTranslationManager {
      return "hi"
    }

    manager.load(from: provider)

    XCTAssertEqual("Original", label.Text)
  }

  func testAppliesLegacyTranslationToMatchingComponent() {
    let label = I18nTranslationTarget()
    label.Text = "Original"
    let provider = DictionaryI18nTranslationProvider(
        [
          "i18n/translations.json": """
            {
              "entries": {
                "labelText": {
                  "baseText": "Hello",
                  "source": {
                    "screen": "Screen1",
                    "component": "Label1",
                    "property": "Text"
                  },
                  "translations": {"hi": "नमस्ते"}
                }
              }
            }
            """
        ],
        components: ["Label1": label])
    let manager = I18nTranslationManager {
      return "hi-IN"
    }

    manager.load(from: provider)

    XCTAssertEqual("नमस्ते", label.Text)
  }

  func testFormTranslateUsesLoadedDynamicEntry() {
    let form = Form()
    form.i18nTranslationManager.setPreviewLanguageOverride("hi")
    form.i18nTranslationManager.loadFromJSON("""
      {
        "entries": {
          "welcome_message": {
            "baseText": "Hello",
            "translations": {
              "hi": "नमस्ते"
            }
          }
        }
      }
      """)

    XCTAssertEqual(
        "नमस्ते",
        form.Translate("welcome_message"))
    XCTAssertEqual(
        "",
        form.Translate("missing_key"))
  }

  func testFormTranslateWithValuesConvertsDictionaryValues() {
    let form = Form()
    form.i18nTranslationManager.setPreviewLanguageOverride("hi")
    form.i18nTranslationManager.loadFromJSON("""
      {
        "entries": {
          "message_count": {
            "baseText": "{name}, you have {count} messages.",
            "translations": {
              "hi": "{name}, आपके पास {count} संदेश हैं।"
            }
          }
        }
      }
      """)

    let values = [
      "name": "Akash",
      "count": 5
    ] as YailDictionary

    XCTAssertEqual(
        "Akash, आपके पास 5 संदेश हैं।",
        form.TranslateWithValues("message_count", values))
  }

}

private enum I18nAssetError: Error {
  case missingAsset
}

private final class DictionaryI18nTranslationProvider:
    I18nTranslationProvider {
  private let assets: [String: String]
  private let components: [String: AnyObject]
  private(set) var openedAssets: [String] = []
  let i18nFormName: String

  init(_ assets: [String: String],
      formName: String = "Screen1",
      components: [String: AnyObject] = [:]) {
    self.assets = assets
    self.i18nFormName = formName
    self.components = components
  }

  func openI18nAsset(_ assetPath: String) throws -> Data {
    openedAssets.append(assetPath)

    guard let contents = assets[assetPath],
        let data = contents.data(using: .utf8) else {
      throw I18nAssetError.missingAsset
    }

    return data
  }

  func lookupI18nComponent(_ componentName: String) -> AnyObject? {
    return components[componentName]
  }
}

private final class I18nTranslationTarget: NSObject {
  @objc dynamic var Text = ""
}
