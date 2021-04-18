package jp.snuffy.cap.plugins.nativedownloader


import android.annotation.SuppressLint
import androidx.annotation.RequiresApi

import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

import com.tonyodev.fetch2.*
import com.tonyodev.fetch2.DownloadNotification.ActionType.*
import com.tonyodev.fetch2.util.*

abstract class NativeDownloadFetchNotificationManager(context: Context) : FetchNotificationManager {

    private val context: Context = context.applicationContext
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val downloadNotificationsMap = mutableMapOf<Int, DownloadNotification>()
    private val downloadNotificationsBuilderMap = mutableMapOf<Int, NotificationCompat.Builder>()
    private val downloadNotificationExcludeSet = mutableSetOf<Int>()

    override val notificationManagerAction: String = "ADVANCE_DOWNLOADER_FETCH_NOTIFICATION_MANAGER_ACTION_" + System.currentTimeMillis()

    override val broadcastReceiver: BroadcastReceiver
        get() = object: BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                onDownloadNotificationActionTriggered(context, intent, this@NativeDownloadFetchNotificationManager)
            }

        }

    init {
        initialize()
    }

    companion object {
        const val SILENT_NOTIFICATION_CHANNEL_ID = "1900009"
        const val SILENT_NOTIFICATION_CHANNEL_NAME = "1900010"
        const val LOUD_NOTIFICATION_CHANNEL_ID = "1900099"
        const val LOUD_NOTIFICATION_CHANNEL_NAME = "1900100"
    }

    private fun initialize() {
        registerBroadcastReceiver()
        createNotificationChannels(context, notificationManager)
    }

    override fun registerBroadcastReceiver() {
        context.registerReceiver(broadcastReceiver, IntentFilter(notificationManagerAction))
    }

    override fun unregisterBroadcastReceiver() {
        context.unregisterReceiver(broadcastReceiver)
    }

    override fun createNotificationChannels(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(SILENT_NOTIFICATION_CHANNEL_ID, SILENT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            createChannel(LOUD_NOTIFICATION_CHANNEL_ID, LOUD_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(channelId: String, channelName: String, importance: Int) {
        var channel: NotificationChannel? = notificationManager.getNotificationChannel(channelId)
        if (channel == null) {
            channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun getChannelId(notificationId: Int, context: Context): String {
        val downloadNotification = this.downloadNotificationsMap.get(notificationId)
        if (downloadNotification != null) {
            return if (downloadNotification.status == Status.COMPLETED || downloadNotification.status == Status.FAILED) {
                LOUD_NOTIFICATION_CHANNEL_ID
            } else {
                SILENT_NOTIFICATION_CHANNEL_ID
            }
        }
        return SILENT_NOTIFICATION_CHANNEL_ID
    }

    override fun updateGroupSummaryNotification(groupId: Int,
                                                notificationBuilder: NotificationCompat.Builder,
                                                downloadNotifications: List<DownloadNotification>,
                                                context: Context): Boolean {
        val style = NotificationCompat.InboxStyle()
        for (downloadNotification in downloadNotifications) {
            val contentTitle = getSubtitleText(context, downloadNotification)
            style.addLine("${downloadNotification.total} $contentTitle")
        }
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(context.getString(R.string.fetch_notification_default_channel_name))
                .setContentText("")
                .setStyle(style)
                .setGroup(groupId.toString())
                .setGroupSummary(true)
        return false
    }

    override fun updateNotification(notificationBuilder: NotificationCompat.Builder,
                                    downloadNotification: DownloadNotification,
                                    context: Context) {
        val smallIcon = if (downloadNotification.isDownloading) {
            android.R.drawable.stat_sys_download
        } else {
            android.R.drawable.stat_sys_download_done
        }
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(smallIcon)
                .setContentTitle(downloadNotification.title)
                .setContentText(getSubtitleText(context, downloadNotification))
                .setOngoing(downloadNotification.isOnGoingNotification)
                .setGroup(downloadNotification.groupId.toString())
                .setGroupSummary(false)
        if (downloadNotification.isFailed || downloadNotification.isCompleted) {
            notificationBuilder.setProgress(0, 0, false)
        } else {
            val progressIndeterminate = downloadNotification.progressIndeterminate
            val maxProgress = if (downloadNotification.progressIndeterminate) 0 else 100
            val progress = if (downloadNotification.progress < 0) 0 else downloadNotification.progress
            notificationBuilder.setProgress(maxProgress, progress, progressIndeterminate)
        }
        when {
            downloadNotification.isDownloading -> {
                notificationBuilder.setTimeoutAfter(getNotificationTimeOutMillis())
                        .addAction(
                                context.resources.getIdentifier("fetch_notification_pause", "drawable", context.packageName),
                                context.getString(
                                        context.resources.getIdentifier("fetch_notification_download_pause", "string", context.packageName)
                                ),
                                getActionPendingIntent(downloadNotification, PAUSE))
                        .addAction(
                                context.resources.getIdentifier("fetch_notification_cancel", "drawable", context.packageName),
                                context.getString(
                                        context.resources.getIdentifier("fetch_notification_download_cancel", "string", context.packageName)
                                ),
                                getActionPendingIntent(downloadNotification, CANCEL))
            }
            downloadNotification.isPaused -> {
                notificationBuilder.setTimeoutAfter(getNotificationTimeOutMillis())
                        .addAction(
                                context.resources.getIdentifier("fetch_notification_resume", "drawable", context.packageName),
                                context.getString(
                                        context.resources.getIdentifier("fetch_notification_download_resume", "string", context.packageName)
                                ),
                                getActionPendingIntent(downloadNotification, RESUME))
                        .addAction(
                                context.resources.getIdentifier("fetch_notification_cancel", "drawable", context.packageName),
                                context.getString(
                                        context.resources.getIdentifier("fetch_notification_download_cancel", "string", context.packageName)
                                ),
                                getActionPendingIntent(downloadNotification, CANCEL))
            }
            downloadNotification.isQueued -> {
                notificationBuilder.setTimeoutAfter(getNotificationTimeOutMillis())
            }
            else -> {
                notificationBuilder.setTimeoutAfter(DEFAULT_NOTIFICATION_TIMEOUT_AFTER_RESET)
            }
        }
    }

    override fun getActionPendingIntent(downloadNotification: DownloadNotification,
                                        actionType: DownloadNotification.ActionType): PendingIntent {
        synchronized(downloadNotificationsMap) {
            val intent = Intent(notificationManagerAction)
            intent.putExtra(EXTRA_NAMESPACE, downloadNotification.namespace)
            intent.putExtra(EXTRA_DOWNLOAD_ID, downloadNotification.notificationId)
            intent.putExtra(EXTRA_NOTIFICATION_ID, downloadNotification.notificationId)
            intent.putExtra(EXTRA_GROUP_ACTION, false)
            intent.putExtra(EXTRA_NOTIFICATION_GROUP_ID, downloadNotification.groupId)
            val action = when (actionType) {
                CANCEL -> ACTION_TYPE_CANCEL
                DELETE -> ACTION_TYPE_DELETE
                RESUME -> ACTION_TYPE_RESUME
                PAUSE -> ACTION_TYPE_PAUSE
                RETRY -> ACTION_TYPE_RETRY
                else -> ACTION_TYPE_INVALID
            }
            intent.putExtra(EXTRA_ACTION_TYPE, action)
            return PendingIntent.getBroadcast(context, downloadNotification.notificationId + action, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun getGroupActionPendingIntent(groupId: Int,
                                             downloadNotifications: List<DownloadNotification>,
                                             actionType: DownloadNotification.ActionType): PendingIntent {
        synchronized(downloadNotificationsMap) {
            val intent = Intent(notificationManagerAction)
            intent.putExtra(EXTRA_NOTIFICATION_GROUP_ID, groupId)
            intent.putExtra(EXTRA_DOWNLOAD_NOTIFICATIONS, ArrayList(downloadNotifications))
            intent.putExtra(EXTRA_GROUP_ACTION, true)
            val action = when (actionType) {
                CANCEL_ALL -> ACTION_TYPE_CANCEL_ALL
                DELETE_ALL -> ACTION_TYPE_DELETE_ALL
                RESUME_ALL -> ACTION_TYPE_RESUME_ALL
                PAUSE_ALL -> ACTION_TYPE_PAUSE_ALL
                RETRY_ALL -> ACTION_TYPE_RETRY_ALL
                else -> ACTION_TYPE_INVALID
            }
            intent.putExtra(EXTRA_ACTION_TYPE, action)
            return PendingIntent.getBroadcast(context, groupId + action, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun cancelNotification(notificationId: Int) {
        synchronized(downloadNotificationsMap) {
            notificationManager.cancel(notificationId)
            downloadNotificationsBuilderMap.remove(notificationId)
            downloadNotificationExcludeSet.remove(notificationId)
            val downloadNotification = downloadNotificationsMap[notificationId]
            if (downloadNotification != null) {
                downloadNotificationsMap.remove(notificationId)
                notify(downloadNotification.groupId)
            }
        }
    }

    override fun cancelOngoingNotifications() {
        synchronized(downloadNotificationsMap) {
            val iterator = downloadNotificationsMap.values.iterator()
            var downloadNotification: DownloadNotification
            while (iterator.hasNext()) {
                downloadNotification = iterator.next()
                if (!downloadNotification.isFailed && !downloadNotification.isCompleted) {
                    notificationManager.cancel(downloadNotification.notificationId)
                    downloadNotificationsBuilderMap.remove(downloadNotification.notificationId)
                    downloadNotificationExcludeSet.remove(downloadNotification.notificationId)
                    iterator.remove()
                    notify(downloadNotification.groupId)
                }
            }
        }
    }

    override fun notify(groupId: Int) {
        synchronized(downloadNotificationsMap) {
            val groupedDownloadNotifications = downloadNotificationsMap.values.filter { it.groupId == groupId }
            val groupSummaryNotificationBuilder = getNotificationBuilder(groupId, groupId)
            val useGroupNotification = updateGroupSummaryNotification(groupId, groupSummaryNotificationBuilder, groupedDownloadNotifications, context)
            var notificationId: Int
            var notificationBuilder: NotificationCompat.Builder
            for (downloadNotification in groupedDownloadNotifications) {
                if (shouldUpdateNotification(downloadNotification)) {
                    notificationId = downloadNotification.notificationId
                    notificationBuilder = getNotificationBuilder(notificationId, groupId)
                    updateNotification(notificationBuilder, downloadNotification, context)
                    notificationManager.notify(notificationId, notificationBuilder.build())
                    when(downloadNotification.status) {
                        Status.COMPLETED,
                        Status.FAILED -> {
                            downloadNotificationExcludeSet.add(downloadNotification.notificationId)
                        }
                        else -> {}
                    }
                }
            }
            if (useGroupNotification) {
                notificationManager.notify(groupId, groupSummaryNotificationBuilder.build())
            }
        }
    }

    override fun shouldUpdateNotification(downloadNotification: DownloadNotification): Boolean {
        return !downloadNotificationExcludeSet.contains(downloadNotification.notificationId)
    }

    override fun shouldCancelNotification(downloadNotification: DownloadNotification): Boolean {
        return downloadNotification.isPaused
    }

    override fun postDownloadUpdate(download: Download): Boolean {
        return synchronized(downloadNotificationsMap) {
            if (downloadNotificationsMap.size > 50) {
                downloadNotificationsBuilderMap.clear()
                downloadNotificationsMap.clear()
            }
            val downloadNotification = downloadNotificationsMap[download.id] ?: DownloadNotification()
            downloadNotification.status = download.status
            downloadNotification.progress = download.progress
            downloadNotification.notificationId = download.id
            downloadNotification.groupId = download.group
            downloadNotification.etaInMilliSeconds = download.etaInMilliSeconds
            downloadNotification.downloadedBytesPerSecond = download.downloadedBytesPerSecond
            downloadNotification.total = download.total
            downloadNotification.downloaded = download.downloaded
            downloadNotification.namespace = download.namespace
            downloadNotification.title = getDownloadNotificationTitle(download)
            downloadNotificationsMap[download.id] = downloadNotification
            if (downloadNotificationExcludeSet.contains(downloadNotification.notificationId)
                    && !downloadNotification.isFailed && !downloadNotification.isCompleted) {
                downloadNotificationExcludeSet.remove(downloadNotification.notificationId)
            }
            if (downloadNotification.isCancelledNotification || shouldCancelNotification(downloadNotification)) {
                cancelNotification(downloadNotification.notificationId)
            } else {
                notify(download.group)
            }
            true
        }
    }

    @SuppressLint("RestrictedApi")
    override fun getNotificationBuilder(notificationId: Int, groupId: Int): NotificationCompat.Builder {
        synchronized(downloadNotificationsMap) {
            val notificationBuilder = downloadNotificationsBuilderMap[notificationId]
                    ?: NotificationCompat.Builder(context, getChannelId(notificationId, context))
            downloadNotificationsBuilderMap[notificationId] = notificationBuilder
            notificationBuilder
                    .setGroup(notificationId.toString())
                    .setStyle(null)
                    .setProgress(0, 0, false)
                    .setContentTitle(null)
                    .setContentText(null)
                    .setContentIntent(null)
                    .setGroupSummary(false)
                    .setTimeoutAfter(DEFAULT_NOTIFICATION_TIMEOUT_AFTER_RESET)
                    .setOngoing(false)
                    .setGroup(groupId.toString())
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .mActions.clear()
            return notificationBuilder
        }
    }

    override fun getNotificationTimeOutMillis(): Long {
        return DEFAULT_NOTIFICATION_TIMEOUT_AFTER
    }

    abstract override fun getFetchInstanceForNamespace(namespace: String): Fetch

    override fun getDownloadNotificationTitle(download: Download): String {
        return download.fileUri.lastPathSegment ?: Uri.parse(download.url).lastPathSegment ?: download.url
    }

    override fun getSubtitleText(context: Context, downloadNotification: DownloadNotification): String {
        return when {
            downloadNotification.isCompleted -> context.getString(R.string.fetch_notification_download_complete)
            downloadNotification.isFailed -> context.getString(R.string.fetch_notification_download_failed)
            downloadNotification.isPaused -> context.getString(R.string.fetch_notification_download_paused)
            downloadNotification.isQueued -> context.getString(R.string.fetch_notification_download_starting)
            downloadNotification.etaInMilliSeconds < 0 -> context.getString(R.string.fetch_notification_download_downloading)
            else -> getEtaText(context, downloadNotification.etaInMilliSeconds)
        }
    }

    private fun getEtaText(context: Context, etaInMilliSeconds: Long): String {
        var seconds = (etaInMilliSeconds / 1000)
        val hours = (seconds / 3600)
        seconds -= (hours * 3600)
        val minutes = (seconds / 60)
        seconds -= (minutes * 60)
        return when {
            hours > 0 -> context.getString(
                    context.resources.getIdentifier("fetch_notification_download_eta_hrs", "string", context.packageName),
                    hours,
                    minutes,
                    seconds
            )
            minutes > 0 -> context.getString(
                    context.resources.getIdentifier("fetch_notification_download_eta_min", "string", context.packageName),
                    minutes,
                    seconds
            )
            else -> context.getString(
                    context.resources.getIdentifier("fetch_notification_download_eta_sec", "string", context.packageName),
                    seconds
            )
        }
    }

}
