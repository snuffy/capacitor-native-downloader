struct NativeDownloadSystem {
    static func freeSize() -> Int? {
        guard
            let path = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).last,
            let systemAttrs = try? FileManager.default.attributesOfFileSystem(forPath: path),
            let freeSize = systemAttrs[FileAttributeKey.systemFreeSize] as? NSNumber else { return nil }
        return freeSize.intValue
    }
}
