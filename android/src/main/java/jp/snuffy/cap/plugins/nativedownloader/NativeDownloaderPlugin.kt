package jp.snuffy.cap.plugins.nativedownloader

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.getcapacitor.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2core.Func
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.jar.Manifest


@NativePlugin(name = "NativeDownloader")
class NativeDownloaderPlugin : Plugin() {
    private val implementation: NativeDownloader = NativeDownloader()
    private lateinit var fetch: Fetch

    companion object {
        const val TAG = "NativeDownloader"
        const val TASK_KEY = "prefsTasks"
    }

    @Suppress("UNUSED_PARAMETER")
    private var prefsTasks: SharedPreferences
        get() = activity.applicationContext.getSharedPreferences(TASK_KEY, Context.MODE_PRIVATE)
        set(value) {}

    private val typeToken = object : TypeToken<MutableMap<String, NativeDownloadTask>>() {}

    public override fun load() {
        super.load()

        val fetchConfiguration = FetchConfiguration.Builder(activity)
                .setNamespace("")
                .setDownloadConcurrentLimit(1)
                .setHttpDownloader(HttpUrlConnectionDownloader(Downloader.FileDownloaderType.SEQUENTIAL))
                .setNotificationManager(object : NativeDownloadFetchNotificationManager(activity) {
                    override fun getFetchInstanceForNamespace(namespace: String): Fetch {
                        return fetch
                    }
                })
                .build()
        fetch = Fetch.Impl.getInstance(fetchConfiguration)
        fetch.removeListener(AdvanceFetchListener())
        fetch.addListener(AdvanceFetchListener())
        fetch.removeAll();
        println("hi! This is NativeDownloader. Now intitilaizing ...")
    }


    @PluginMethod
    fun echo(call: PluginCall) {
        val value: String = call.getString("value")!!
        val ret = JSObject()
        ret.put("value", implementation.echo(value))
        call.resolve(ret)
    }

    @PluginMethod
    fun add(call: PluginCall) {
        val params = call.getObject("params")
        val tasks = getTasks()
        val path = params.getString("filePath")!!
        val index = System.currentTimeMillis()
        params.put("index", index)
        params.put("filePath", path.removePrefix("file://"))
        val task = Gson().fromJson(params.toString(), NativeDownloadTask::class.java)

        // MEMO: debug時は getFilePathを利用する
        // task.request = Request(task.url, getFilePath(task.url))
        task.request = Request(task.url, task.filePath)
        task.request?.apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
            if (task.headers != null) {
                for (header in task.headers) {
                    addHeader(header.key, header.value)
                }
            }

        }

        // サイズ計算をする
        val internalPath = Environment.getDataDirectory().absolutePath
        val availableSize = getAvailableSize(internalPath)
        if (availableSize < task.size) {
            call.reject("size over")
        }

        tasks[task.id] = task
        editTasks(tasks)
        val output = Gson().toJson(task)
        val result = JSObject(output)
        call.resolve(result)
    }

    @PluginMethod
    fun start(call: PluginCall) {
        val params = call.getObject("params")
        val id = params.getString("id")
        val tasks = getTasks()
        val task = tasks[id]
        task ?: return
        val output = Gson().toJson(task)
        val result = JSObject(output)
        val status:List<Status> = mutableListOf(Status.DOWNLOADING, Status.QUEUED, Status.ADDED)
        fetch.getDownloadsWithStatus(status, Func<List<Download>> { result ->
            if (result.isEmpty()) {
                val tasks = getTasks()
                if (tasks.isNotEmpty()) {
                    val requests = tasks.toSortedMap(Comparator { k1, k2 -> if (tasks[k1]!!.index > tasks[k2]!!.index)  1 else -1 })
                            .map { it.value.request } as MutableList<Request?>

                    fetch.enqueue(requests.filterNotNull())

                }
            }
        })

        call.resolve(result)
    }

    @PluginMethod
    private fun pause(call: PluginCall) {
        val params = call.getObject("params")
        val id = params.getString("id")
        val tasks = getTasks()
        val task = tasks[id]
        task ?: return

        val output = Gson().toJson(task)
        val result = JSObject(output)

        task.request?.id?.let { fetch.pause(it) }

        call.resolve(result)
    }


    @PluginMethod
    private fun resume(call: PluginCall) {
        val params = call.getObject("params")
        val id = params.getString("id")
        val tasks = getTasks()
        val task = tasks[id]
        task ?: return

        val output = Gson().toJson(task)
        val result = JSObject(output)

        task.request?.id?.let { fetch.resume(it) }

        call.resolve(result)
    }

    @PluginMethod
    private fun stop(call: PluginCall) {
        val params = call.getObject("params")
        val id = params.getString("id")
        val tasks = getTasks()
        val task = tasks[id]
        task ?: return

        val output = Gson().toJson(task)
        val result = JSObject(output)

        task.request?.id?.let { fetch.remove(it) }

        tasks.remove(task.id)
        editTasks(tasks)

        call.resolve(result)
    }

    private fun getTasks(): MutableMap<String, NativeDownloadTask> {
        return Gson().fromJson(prefsTasks.getString(TASK_KEY, "{}"), typeToken.type)
    }

    private fun editTasks(tasks: MutableMap<String, NativeDownloadTask>) {
        prefsTasks.edit().putString(TASK_KEY, Gson().toJson(tasks)).apply()
    }


    // MARK: - FetchListener
    private inner class AdvanceFetchListener : FetchListener {
        override fun onAdded(download: Download) {
            Log.d(TAG, "Added Download: ${download.url}")
        }

        override fun onCancelled(download: Download) {
            Log.d(TAG, "Cancelled Download: ${download.url}")
            // タスクから消去する
            val newTasks = getTasks().filter { entity -> entity.value.request?.id != download.request.id }.toMutableMap()
            editTasks(newTasks)

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.status.toString())
            notifyListeners("onChangeStatus", result)

        }
        // ダウンロード完了
        override fun onCompleted(download: Download) {
            Log.d(TAG, "Completed Download: ${download.url}")

            // MEMO: download が完了したら tasks から消す
            val restTask = getTasks().filter { it -> it.value.request?.id != download.request.id } as MutableMap<String, NativeDownloadTask>
            editTasks(restTask)

            // MEMO: ダウンロード中なものがなければ新規でダウンロードを開始する
            val status:List<Status> = mutableListOf(Status.DOWNLOADING, Status.ADDED, Status.QUEUED)
            fetch.getDownloadsWithStatus(status, Func<List<Download>> { result ->
                if (result.isEmpty()) {
                    val tasks = getTasks()
                    if (tasks.isNotEmpty()) {
                        val requests = tasks.toSortedMap(Comparator { k1, k2 -> if (tasks[k1]!!.index > tasks[k2]!!.index)  1 else -1 })
                                .map { it.value.request } as MutableList<Request?>

                        fetch.enqueue(requests.filterNotNull())
                    }
                }

            })
            val result = JSObject()
            result.put("id", download.id)
            result.put("fileUrl", download.url)
            notifyListeners("onComplete", result)
        }

        override fun onDeleted(download: Download) {
            Log.d(TAG, "Deleted Download: ${download.url}")

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.status.toString())
            notifyListeners("onChangeStatus", result)

        }

        override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
            Log.d(TAG, "DownloadBlockUpdated Download: ${download.url}")
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            Log.d(TAG, "Error Download: ${download.error}")

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.error.toString())
            notifyListeners("onFailed", result)
        }

        override fun onPaused(download: Download) {
            Log.d(TAG, "Paused Download: ${download.url}")

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.status.toString())
            notifyListeners("onChangeStatus", result)
        }

        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            Log.d(TAG, "Progress Download: ${download.progress}")
            val result = JSObject()
            result.put("id", download.id)
            result.put("progress", download.progress/100.0)
            notifyListeners("onProgress", result);
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            Log.d(TAG, "Queued Download: ${download.url}")

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.status.toString())
            notifyListeners("onChangeStatus", result)
        }

        override fun onRemoved(download: Download) {
            Log.d(TAG, "Removed Download: ${download.url}")

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.status.toString())
            notifyListeners("onChangeStatus", result)
        }

        override fun onResumed(download: Download) {
            Log.d(TAG, "Resumed Download: ${download.url}")

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.status.toString())
            notifyListeners("onChangeStatus", result)
        }

        override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
            Log.d(TAG, "Started Download: ${download.url}")

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.status.toString())
            notifyListeners("onChangeStatus", result)
        }

        override fun onWaitingNetwork(download: Download) {
            Log.d(TAG, "WaitingNetwork Download: ${download.url}")

            val result = JSObject()
            result.put("id", download.id)
            result.put("status", download.status.toString())
            notifyListeners("onChangeStatus", result)
        }
    }

    private fun getAvailableSize(path: String?): Long {
        var size: Long = -1
        if (path != null) {
            val fs = StatFs(path)
            val blockSize = fs.blockSizeLong.toLong()
            val availableBlockSize = fs.availableBlocksLong.toLong()
            size = blockSize * availableBlockSize
        }
        return size
    }
}