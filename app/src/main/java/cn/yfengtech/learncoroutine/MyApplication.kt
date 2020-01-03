package cn.yfengtech.learncoroutine

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.yf.smarttemplate.SmartTemplate
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

private const val URL_USER = "https://api.github.com/users/yfengtech"

/**
 * 为了加深记忆，记录一些协程的用法
 *
 * 感谢扔物线的视频和文章，帮助理解协程(https://kaixue.io)
 *
 * Created by yf.
 * @date 2020-01-03
 */
class MyApplication : Application() {

    private lateinit var mContext: Context

    private val okHttpClient = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        mContext = this

        SmartTemplate.init(this) {
            itemList {
                title = "初窥协程-简单用法"
                desc = "阻塞用法/非阻塞用法"
                executionItem {
                    title = "异步调用（不阻塞线程）"
                    desc = "等待网络接口返回"
                    execute {
                        runAsync()
                    }
                }
                executionItem {
                    title = "同步调用（阻塞线程）"
                    desc = "等待网络接口返回"
                    execute {
                        runBlock()
                    }
                }
            }

            itemList {
                title = "进阶用法"
                desc = "列举几个常见的业务场景"
                executionItem {
                    title = "合并网络请求"
                    desc = "等待网络接口返回"
                    execute {
                        mergeResponse()
                    }
                }
                itemList {
                    title = "取消job"
                    desc = "取消一个已经在协程中执行的任务"

                    var job: Job? = null
                    executionItem {
                        title = "开始任务"
                        execute {
                            job = GlobalScope.launch(Dispatchers.IO) {
                                Toast.makeText(mContext, "开始任务", Toast.LENGTH_SHORT)
                                for (i in 0..100) {
                                    delay(2000)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(mContext, "任务执行中：$i", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        }
                    }
                    executionItem {
                        title = "取消任务"
                        execute {
                            if (job?.isActive == true) {
                                job?.cancel()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * GlobalScope.launch 开启一个协程，不阻塞当前线程
     *
     * 异步执行，看上去像同步执行的，实则由协程库去调度线程切换
     */
    private fun runAsync() {

        debugLog("进入协程代码块")

        // 开启一个协程
        GlobalScope.launch(Dispatchers.Main) {
            debugLog("协程代码块 start")
            // 挂起线程，IO线程执行网络请求
            val result = withContext(Dispatchers.IO) {
                // 内部可以做大量的耗时操作，网络加载，图片处理等
                sendRequest(URL_USER)
            }
            // 执行完毕会切回Main线程
            Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show()
            debugLog("协程代码块 end")
        }

        debugLog("离开协程代码块")
    }

    /**
     * runBlocking 操作符，同步执行，会阻塞当前线程
     */
    private fun runBlock() {

        debugLog("进入协程代码块")

        val result = runBlocking(Dispatchers.IO) {
            // 在IO线程执行，但会阻塞当前线程
            debugLog("阻塞代码块 start")
            delay(2000)
            debugLog("阻塞代码块 end")
            "返回结果"
        }
        debugLog("离开协程代码块")

        Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show()
    }

    /**
     * 合并接口数据
     */
    private fun mergeResponse() {

        debugLog("进入协程代码块")

        GlobalScope.launch(Dispatchers.Main) {
            debugLog("协程代码块 start")
            val result1 = async(Dispatchers.IO) {
                debugLog("请求接口1")
                sendRequest(URL_USER)
            }
            val result2 = async(Dispatchers.IO) {
                debugLog("请求接口2")
                sendRequest(URL_USER)
            }
            // 此处await会等待结果
            val finalResult = result1.await() + result2.await()
            debugLog("合并数据")
            Toast.makeText(mContext, finalResult, Toast.LENGTH_SHORT).show()
            debugLog("协程代码块 end")
        }
        debugLog("离开协程代码块")

    }

    /**
     * 这个方法会阻塞线程，同步的方式请求网络
     *
     * @return 网络请求结果
     */
    private fun sendRequest(url: String): String {
        val request = Request.Builder().url(url).get().build()
        val call = okHttpClient.newCall(request)
        return call.execute().body?.string() ?: "无数据"
    }
}

internal inline fun <reified T : Any> T.debugLog(value: String) {
    Log.d("CoroutineLearning", "[${this::class.java.simpleName}] : [$value]")
}