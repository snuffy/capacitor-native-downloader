//
//  NativeDownloaderUserDefaults.swift
//  TAD
//
//  Created by hirose.yuuki on 2020/05/24.
//

import Foundation

struct NativeDownloaderUserDefaults {
    private static let KeyDownloadingFileURLs = "downloading_file_urls"
    
    static var downloadingFileURLs: [String] {
        get {
            return getArray(k: KeyDownloadingFileURLs)
        }
        set(v) {
            set(k: KeyDownloadingFileURLs, arr: v)
        }
    }
}

extension NativeDownloaderUserDefaults {
    
    // Getter
    private static func getString(k: String) -> String {
        let d = UserDefaults.standard
        return d.string(forKey: key(k: k)) ?? ""
    }
    
    private static func getInt(k: String) -> Int {
        let d = UserDefaults.standard
        return d.integer(forKey: key(k: k))
    }
    
    private static func getInt64(k: String) -> Int64 {
        let d = UserDefaults.standard
        if let sv = d.string(forKey: key(k: k)), let iv = Int64(sv) {
            return iv
        } else {
            return 0
        }
    }
    
    private static func getBool(k: String) -> Bool {
        return UserDefaults.standard.bool(forKey: key(k: k))
    }
    
    private static func getArray<T>(k: String) -> [T] {
        let d = UserDefaults.standard
        if let arr = d.array(forKey: key(k: k)) as? [T] {
            return arr
        }
        return []
    }
    
    private static func getDictionary<E, T>(k: String) -> [E: T] {
        let d = UserDefaults.standard
        if let dict = d.dictionary(forKey: key(k: k)) as? [E: T] {
            return dict
        }
        return [:]
    }
    
    // Setter
    private static func set(k: String, value: String) {
        let d = UserDefaults.standard
        d.setValue(value, forKey: key(k: k))
        d.synchronize()
    }
    
    private static func set(k: String, int: Int) {
        let d = UserDefaults.standard
        d.setValue(int, forKey: key(k: k))
        d.synchronize()
    }
    
    private static func set(k: String, int64: Int64) {
        let d = UserDefaults.standard
        d.setValue(int64, forKey: key(k: k))
        d.synchronize()
    }
    
    private static func set(k: String, bool: Bool) {
        let d = UserDefaults.standard
        d.setValue(bool, forKey: key(k: k))
        d.synchronize()
    }
    
    private static func set<T>(k: String, arr: [T]) {
        let d = UserDefaults.standard
        d.setValue(arr, forKey: key(k: k))
        d.synchronize()
    }
    
    private static func set<E, T>(k: String, dict: [E: T]) {
        let d = UserDefaults.standard
        d.setValue(dict, forKey: key(k: k))
        d.synchronize()
    }
    
    // remove
    private static func remove(k: String) {
        let d = UserDefaults.standard
        d.removeObject(forKey: key(k: k))
        d.synchronize()
    }
    
    private static func key(k: String) -> String {
        return "NativeDownloader:\(k)"
    }
}
