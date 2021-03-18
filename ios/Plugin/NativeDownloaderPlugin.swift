import Foundation
import Capacitor

struct NativeDownloadParam {
    var id: String?
    var url: URL?
    var headers: [String:String]?
    var size: Int?
    var filePath: String?
    var fileName: String?
    
    init(call: CAPPluginCall) {
        
        guard let params = call.get("params",[String:Any].self) else { return }
    
        if let param = params["id"] as? String {
            id = param
        }
        if let param = params["url"] as? String, let u = URL(string: param) {
            url = u
        }
        if let param = params["headers"] as? [String:String] {
            headers = param
        }
        if let param = params["size"] as? Int {
            size = param
        }
        if let param = params["filePath"] as? String {
            filePath = param
        }
        if let param = params["fileName"] as? String {
            fileName = param
        }
    }
}

@objc(NativeDownloaderPlugin)
public class NativeDownloaderPlugin: CAPPlugin {
    private var client = NativeDownload()
    var onChangedStatusCalls: [String:[CAPPluginCall]] = [:]
    var onProgressCalls: [String:[CAPPluginCall]] = [:]
    var onCompleteCalls: [String:[CAPPluginCall]] = [:]
    var onFailedCalls: [String:[CAPPluginCall]] = [:]

    override public func load() {
        print("native downloader: initalized ...")
        client.clean()
    }

    @objc func echo(_ call: CAPPluginCall) {
    }

    @objc func add(_ call: CAPPluginCall) {
        let param = NativeDownloadParam(call: call)
        guard
            let id = param.id,
            let url = param.url,
            let headers = param.headers,
            let size = param.size,
            let filePath = param.filePath,
            let fileName = param.fileName else {
                call.reject("invalid arguments")
                return
        }
        
        let task = NativeDownloadTask(id: id, url: url, headers: headers, size: size, filePath: filePath, fileName: fileName)
        task.onChangeStatus = { [weak self] status in
            guard let self = self else { return }
            let result: [String:Any] = [
                "id": id,
                "status": status.rawValue,
            ]
            self.notifyListeners("onChangeStatus", data: result)
        }
        task.onProgress = { [weak self] progress in
            guard let self = self else { return }
            let result: [String:Any] = [
               "id": id,
               "progress": progress,
            ]
            
            self.notifyListeners("onProgress",  data: result, retainUntilConsumed: true)
        }
        task.onComplete = { [weak self] fileURL, fileName in
            guard let self = self else { return }
            let result: [String:Any] = [
               "id": id,
               "fileUrl": fileURL.absoluteString,
            ]
            self.notifyListeners("onComplete", data: result, retainUntilConsumed: true)
            NativeDownloadNotification.send(title: "", body: "\(fileName) のダウンロードが完了しました", badge: 0)
        }
        task.onFailed = { [weak self] message in
            guard let self = self else { return }
            let result: [String:Any] = [
               "id": id,
               "message": message,
            ]
            self.notifyListeners("onFailed",  data: result, retainUntilConsumed: true)
        }
        client.add(task: task)
        let result = ["id": id]
        call.resolve(result)
    }
    
    
    // ダウンロードを開始
    @objc func start(_ call: CAPPluginCall) {
        let param = NativeDownloadParam(call: call)
        guard let id = param.id else {
            call.reject("invalid arguments")
            return
        }
        
        client.start(id: id)
        let result = ["id": id]
        call.resolve(result)

    }
}
