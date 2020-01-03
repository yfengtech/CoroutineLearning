# CoroutineLearning
练习演示项目(android项目)，初窥Kotlin Coroutine

## 引入

```
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3'
```

## 简单用法

实现同步调用（阻塞当前线程）和异步调用（不阻塞当前线程）

### 同步调用

```kotlin
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
```

输出log

```
2020-01-04 00:06:53.491 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [MyApplication] : [进入协程代码块]
2020-01-04 00:06:53.512 23636-24123/cn.yfengtech.learncoroutine D/CoroutineLearning: [BlockingCoroutine] : [阻塞代码块 start]
2020-01-04 00:06:55.515 23636-24123/cn.yfengtech.learncoroutine D/CoroutineLearning: [BlockingCoroutine] : [阻塞代码块 end]
2020-01-04 00:06:55.517 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [MyApplication] : [离开协程代码块]
```

### 异步调用

```kotlin
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
```

输出log

```
2020-01-04 00:07:46.888 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [MyApplication] : [进入协程代码块]
2020-01-04 00:07:46.898 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [MyApplication] : [离开协程代码块]
2020-01-04 00:07:46.904 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [StandaloneCoroutine] : [协程代码块 start]
2020-01-04 00:07:48.052 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [StandaloneCoroutine] : [协程代码块 end]

```


## 进阶用法

合并多个网络接口返回的结果，取消执行中的任务

### 合并接口

```kotlin
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
```

输出log

```
2020-01-04 00:11:16.788 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [MyApplication] : [进入协程代码块]
2020-01-04 00:11:16.790 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [MyApplication] : [离开协程代码块]
2020-01-04 00:11:16.791 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [StandaloneCoroutine] : [协程代码块 start]
2020-01-04 00:11:16.798 23636-24125/cn.yfengtech.learncoroutine D/CoroutineLearning: [DeferredCoroutine] : [请求接口1]
2020-01-04 00:11:16.799 23636-24123/cn.yfengtech.learncoroutine D/CoroutineLearning: [DeferredCoroutine] : [请求接口2]
2020-01-04 00:11:17.945 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [StandaloneCoroutine] : [合并数据]
2020-01-04 00:11:17.958 23636-23636/cn.yfengtech.learncoroutine D/CoroutineLearning: [StandaloneCoroutine] : [协程代码块 end]
```

### 中止任务

参考源码MyApplication.kt
