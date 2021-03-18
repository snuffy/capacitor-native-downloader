struct NativeDownloadNotification {
    
    static func register() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.badge, .sound, .alert], completionHandler: { (granted, err) in
            guard err == nil else {
                print(err as Any)
                return
            }
            guard !granted else {
                print("通知拒否")
                return
            }
        })
    }
    
    static func send(title: String, body: String, badge: Int) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.badge = NSNumber(value: badge)
        let request = UNNotificationRequest(
            identifier: "NativeDownloadNotification",
            content: content,
            trigger:UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        )
        UNUserNotificationCenter.current().add(request, withCompletionHandler: nil)
    }
}