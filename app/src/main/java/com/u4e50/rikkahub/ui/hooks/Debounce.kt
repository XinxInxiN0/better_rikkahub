package com.u4e50.rikkahub.ui.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 鍒涘缓涓€涓槻鎶栧嚱鏁板寘瑁呭櫒
 *
 * @param delayMillis 寤惰繜鏃堕棿锛堟绉掞級
 * @param function 瑕佹墽琛岀殑鍑芥暟
 * @return 鍖呰鍚庣殑闃叉姈鍑芥暟
 */
@Composable
fun <T> useDebounce(
    delayMillis: Long = 300,
    function: (T) -> Unit
): (T) -> Unit {
    val scope = rememberCoroutineScope()
    val debounceJob = remember { mutableStateOf<Job?>(null) }

    return remember {
        { param: T ->
            debounceJob.value?.cancel()
            debounceJob.value = scope.launch {
                delay(delayMillis)
                function(param)
            }
        }
    }
}

/**
 * 鍒涘缓涓€涓妭娴佸嚱鏁板寘瑁呭櫒
 *
 * @param intervalMillis 闂撮殧鏃堕棿锛堟绉掞級
 * @param function 瑕佹墽琛岀殑鍑芥暟
 * @return 鍖呰鍚庣殑鑺傛祦鍑芥暟
 */
@Composable
fun <T> useThrottle(
    intervalMillis: Long = 300,
    function: (T) -> Unit
): (T) -> Unit {
    val scope = rememberCoroutineScope()
    val isThrottling = remember { AtomicBoolean(false) }
    val latestParam = remember { mutableStateOf<T?>(null) }

    return remember {
        { param: T ->
            latestParam.value = param

            if (!isThrottling.getAndSet(true)) {
                function(param)

                scope.launch {
                    delay(intervalMillis)
                    isThrottling.set(false)

                    // 濡傛灉鍦ㄨ妭娴佹湡闂存湁鏂扮殑鍙傛暟锛屽垯鍦ㄨ妭娴佺粨鏉熷悗鎵ц涓€娆?
                    latestParam.value?.let { latestValue ->
                        // 閲嶇疆鍙傛暟
                        latestParam.value = null
                        // 鐢ㄦ渶鏂扮殑鍙傛暟鍐嶆璋冪敤鑺傛祦鍑芥暟
                        function(latestValue)
                    }
                }
            }
        }
    }
}

