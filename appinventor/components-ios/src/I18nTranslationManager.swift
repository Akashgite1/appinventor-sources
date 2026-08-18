// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * Supplies Form operations to the i18n translation manager.
 */
protocol I18nTranslationProvider {
  func openI18nAsset(_ assetPath: String) throws -> Data
  var i18nFormName: String { get }
  func lookupI18nComponent(_ componentName: String) -> AnyObject?
}

/**
 * Loads App Inventor i18n data and resolves dynamic translation keys.
 */
@objc(I18nTranslationManager)
public final class I18nTranslationManager: NSObject {
  private static let manifestVersion = 2
  private static let languageFileVersion = 1
  private static let manifestAsset = "i18n/manifest.json"
  private static let legacyTranslationsAsset = "i18n/translations.json"
  private static let assetPrefix = "i18n/"

  typealias LanguageCodeProvider = () -> String

  private let languageCodeProvider: LanguageCodeProvider
  private var translationsRoot: [String: Any]?
  private var manifestRoot: [String: Any]?
  private var activeLanguageEntries: [String: String] = [:]
  private var previewLanguageOverride = ""

  private(set) var activeLanguage = ""

  init(languageCodeProvider: @escaping LanguageCodeProvider =
      I18nTranslationManager.deviceLanguageCode) {
    self.languageCodeProvider = languageCodeProvider
    super.init()
  }

  func load(from provider: I18nTranslationProvider) {
    clearLoadedTranslations()

    if !loadSplitAssets(from: provider) {
      loadLegacyAsset(from: provider)
    }

    applyLoadedTranslations(to: provider)
  }

  @discardableResult
  func loadFromJSON(_ json: String) -> Bool {
    if json.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
      clear()
      return true
    }

    guard let data = json.data(using: .utf8),
        let root = Self.parseRoot(data) else {
      return false
    }

    clearLoadedTranslations()
    translationsRoot = root
    return true
  }

  func lookupDynamic(_ key: String,
      values: [String: String]? = nil) -> String {
    if key.isEmpty {
      return ""
    }

    if manifestRoot != nil {
      return lookupSplitDynamic(key, values: values)
    }

    return lookupLegacyDynamic(key, values: values)
  }

  func setPreviewLanguageOverride(_ language: String?) {
    previewLanguageOverride =
        language?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
  }

  func clearPreviewLanguageOverride() {
    previewLanguageOverride = ""
  }

  func clear() {
    clearLoadedTranslations()
    previewLanguageOverride = ""
  }

  /**
   * Updates the active Companion form with a preview language.
   */
  @objc public static func setPreviewLanguageForCompanion(
      _ language: String,
      _ translationsJSON: String) {
    guard let form = Form.activeForm else {
      return
    }

    let manager = form.i18nTranslationManager
    manager.setPreviewLanguageOverride(language)

    if !translationsJSON
        .trimmingCharacters(in: .whitespacesAndNewlines)
        .isEmpty {
      if manager.loadFromJSON(translationsJSON) {
        manager.applyLoadedTranslations(to: form)
      }
    } else {
      manager.applyLoadedTranslations(to: form)
    }
  }


  func applyLoadedTranslations(to provider: I18nTranslationProvider) {
    if let manifest = manifestRoot,
        let entries = manifest["entries"] as? [String: Any] {
      applySplitTranslations(
          entries,
          languageEntries: activeLanguageEntries,
          to: provider)
      return
    }

    if let root = translationsRoot,
        let entries = root["entries"] as? [String: Any] {
      applyLegacyTranslations(
          entries,
          language: selectLegacyLanguage(),
          to: provider)
    }
  }

  private func loadSplitAssets(
      from provider: I18nTranslationProvider) -> Bool {
    let manifestData: Data

    do {
      manifestData = try provider.openI18nAsset(Self.manifestAsset)
    } catch {
      return false
    }

    guard let manifest = Self.parseRoot(manifestData),
        Self.integerValue(manifest["version"]) == Self.manifestVersion,
        let languagePaths = manifest["languages"] as? [String: Any],
        manifest["entries"] is [String: Any] else {
      return false
    }

    var language = selectSplitLanguage(languagePaths)
    var languageEntries: [String: String]?

    if !language.isEmpty {
      languageEntries = loadSplitLanguageEntries(
          from: provider,
          languagePaths: languagePaths,
          language: language)

      let languageOnly = Self.languageOnly(language)

      if languageEntries == nil,
          !languageOnly.isEmpty,
          languageOnly != language,
          languagePaths[languageOnly] != nil {
        let fallbackEntries = loadSplitLanguageEntries(
            from: provider,
            languagePaths: languagePaths,
            language: languageOnly)

        if let fallbackEntries = fallbackEntries {
          language = languageOnly
          languageEntries = fallbackEntries
        }
      }
    }

    if languageEntries == nil {
      language = ""
      languageEntries = [:]
    }

    translationsRoot = nil
    manifestRoot = manifest
    activeLanguageEntries = languageEntries ?? [:]
    activeLanguage = language
    return true
  }

  private func loadSplitLanguageEntries(
      from provider: I18nTranslationProvider,
      languagePaths: [String: Any],
      language: String) -> [String: String]? {
    guard let relativePath = languagePaths[language] as? String else {
      return nil
    }

    let trimmedPath = relativePath.trimmingCharacters(
        in: .whitespacesAndNewlines)

    guard Self.isSafeLanguageAssetPath(trimmedPath) else {
      return nil
    }

    let data: Data

    do {
      data = try provider.openI18nAsset(Self.assetPrefix + trimmedPath)
    } catch {
      return nil
    }

    guard let root = Self.parseRoot(data),
        Self.integerValue(root["version"]) == Self.languageFileVersion,
        let declaredLanguage = root["language"] as? String,
        declaredLanguage.trimmingCharacters(in: .whitespacesAndNewlines)
            == language,
        let entries = root["entries"] as? [String: Any] else {
      return nil
    }

    var stringEntries: [String: String] = [:]

    for (key, value) in entries {
      if let value = value as? String {
        stringEntries[key] = value
      }
    }

    return stringEntries
  }

  private func loadLegacyAsset(
      from provider: I18nTranslationProvider) {
    guard let data = try? provider.openI18nAsset(
        Self.legacyTranslationsAsset),
        let root = Self.parseRoot(data) else {
      return
    }

    clearLoadedTranslations()
    translationsRoot = root
  }

  private func lookupSplitDynamic(_ key: String,
      values: [String: String]?) -> String {
    guard let entries = manifestRoot?["entries"] as? [String: Any],
        let entry = entries[key] as? [String: Any] else {
      return ""
    }

    var template = activeLanguageEntries[key] ?? ""

    if template.isEmpty {
      template = Self.baseText(from: entry)
    }

    return I18nFormatter.format(template, values: values)
  }

  private func lookupLegacyDynamic(_ key: String,
      values: [String: String]?) -> String {
    guard let root = translationsRoot,
        let entries = root["entries"] as? [String: Any],
        let entry = entries[key] as? [String: Any] else {
      return ""
    }

    let language = selectLegacyLanguage()
    let translations = entry["translations"] as? [String: Any]
    var template = translations?[language] as? String ?? ""

    if template.isEmpty {
      template = Self.baseText(from: entry)
    }

    return I18nFormatter.format(template, values: values)
  }

  private func applySplitTranslations(
      _ entries: [String: Any],
      languageEntries: [String: String],
      to provider: I18nTranslationProvider) {
    for (key, value) in entries {
      guard let entry = value as? [String: Any],
          let source = entry["source"] as? [String: Any] else {
        continue
      }

      let screenName = source["screen"] as? String ?? ""

      if !screenName.isEmpty, screenName != provider.i18nFormName {
        continue
      }

      let componentName = source["component"] as? String ?? ""
      let propertyName = source["property"] as? String ?? ""
      var translatedValue = languageEntries[key] ?? ""

      if translatedValue.isEmpty {
        translatedValue = Self.baseText(from: entry)
      }

      guard !translatedValue.isEmpty,
          let component = provider.lookupI18nComponent(componentName) else {
        continue
      }

      _ = Self.applyStringProperty(
          component,
          propertyName: propertyName,
          translatedValue: translatedValue)
    }
  }

  private func applyLegacyTranslations(
      _ entries: [String: Any],
      language: String,
      to provider: I18nTranslationProvider) {
    if language.isEmpty {
      return
    }

    for value in entries.values {
      guard let entry = value as? [String: Any],
          let source = entry["source"] as? [String: Any] else {
        continue
      }

      let screenName = source["screen"] as? String ?? ""

      if !screenName.isEmpty, screenName != provider.i18nFormName {
        continue
      }

      let componentName = source["component"] as? String ?? ""
      let propertyName = source["property"] as? String ?? ""
      let translations = entry["translations"] as? [String: Any]
      var translatedValue = translations?[language] as? String ?? ""

      if translatedValue.isEmpty {
        translatedValue = Self.baseText(from: entry)
      }

      guard !translatedValue.isEmpty,
          let component = provider.lookupI18nComponent(componentName) else {
        continue
      }

      _ = Self.applyStringProperty(
          component,
          propertyName: propertyName,
          translatedValue: translatedValue)
    }
  }

  private static func applyStringProperty(
      _ component: AnyObject,
      propertyName: String,
      translatedValue: String) -> Bool {
    if propertyName.isEmpty {
      return false
    }

    guard let object = component as? NSObject else {
      return false
    }

    let setter = NSSelectorFromString("set\(propertyName):")

    guard object.responds(to: setter) else {
      return false
    }

    _ = object.perform(setter, with: translatedValue as NSString)
    return true
  }

  private func selectSplitLanguage(
      _ languages: [String: Any]) -> String {
    if !previewLanguageOverride.isEmpty {
      let previewLanguage = Self.resolveAvailableLanguage(
          languages,
          requestedLanguage: previewLanguageOverride)

      if !previewLanguage.isEmpty {
        return previewLanguage
      }
    }

    return Self.resolveAvailableLanguage(
        languages,
        requestedLanguage: languageCodeProvider())
  }

  private func selectLegacyLanguage() -> String {
    if !previewLanguageOverride.isEmpty {
      return previewLanguageOverride
    }

    let deviceLanguage = languageCodeProvider()
    let entries = translationsRoot?["entries"] as? [String: Any]

    if Self.legacyContainsLanguage(entries, language: deviceLanguage) {
      return deviceLanguage
    }

    let languageOnly = Self.languageOnly(deviceLanguage)

    if Self.legacyContainsLanguage(entries, language: languageOnly) {
      return languageOnly
    }

    return deviceLanguage
  }

  private static func legacyContainsLanguage(
      _ entries: [String: Any]?, language: String) -> Bool {
    guard let entries = entries, !language.isEmpty else {
      return false
    }

    for value in entries.values {
      guard let entry = value as? [String: Any],
          let translations = entry["translations"] as? [String: Any] else {
        continue
      }

      if translations[language] != nil {
        return true
      }
    }

    return false
  }

  private static func resolveAvailableLanguage(
      _ languages: [String: Any],
      requestedLanguage: String) -> String {
    let normalized = requestedLanguage
        .trimmingCharacters(in: .whitespacesAndNewlines)
        .replacingOccurrences(of: "_", with: "-")

    if normalized.isEmpty {
      return ""
    }

    if languages[normalized] != nil {
      return normalized
    }

    let languageOnly = self.languageOnly(normalized)

    if !languageOnly.isEmpty, languages[languageOnly] != nil {
      return languageOnly
    }

    return ""
  }

  private static func languageOnly(_ language: String) -> String {
    guard let separator = language.firstIndex(where: {
      $0 == "-" || $0 == "_"
    }) else {
      return language
    }

    if separator == language.startIndex {
      return language
    }

    return String(language[..<separator])
  }

  private static func isSafeLanguageAssetPath(
      _ relativePath: String) -> Bool {
    return !relativePath.isEmpty
        && !relativePath.hasPrefix("/")
        && !relativePath.contains("..")
        && !relativePath.contains("\\")
        && relativePath.hasSuffix(".json")
  }

  private static func baseText(from entry: [String: Any]) -> String {
    if let baseText = entry["baseText"] as? String,
        !baseText.isEmpty {
      return baseText
    }

    guard let source = entry["source"] as? [String: Any] else {
      return ""
    }

    return source["baseText"] as? String ?? ""
  }

  private func clearLoadedTranslations() {
    translationsRoot = nil
    manifestRoot = nil
    activeLanguageEntries = [:]
    activeLanguage = ""
  }

  private static func parseRoot(_ data: Data) -> [String: Any]? {
    guard let object = try? JSONSerialization.jsonObject(
        with: data,
        options: []) else {
      return nil
    }

    return object as? [String: Any]
  }

  private static func integerValue(_ value: Any?) -> Int? {
    return (value as? NSNumber)?.intValue
  }

  static func deviceLanguageCode() -> String {
    let identifier = Locale.preferredLanguages.first
        ?? Locale.current.identifier

    return identifier.replacingOccurrences(of: "_", with: "-")
  }
}

extension Form: I18nTranslationProvider {
  var i18nFormName: String {
    return formName
  }

  func openI18nAsset(_ assetPath: String) throws -> Data {
    let path = AssetManager.shared.pathForExistingFileAsset(assetPath)

    guard !path.isEmpty else {
      throw NSError(
          domain: "AIComponentKit.I18nTranslation",
          code: 1,
          userInfo: [NSFilePathErrorKey: assetPath])
    }

    return try Data(contentsOf: URL(fileURLWithPath: path))
  }

  func lookupI18nComponent(_ componentName: String) -> AnyObject? {
    if componentName == formName {
      return self
    }

    guard let component = environment[componentName] else {
      return nil
    }

    return component as AnyObject
  }
}
