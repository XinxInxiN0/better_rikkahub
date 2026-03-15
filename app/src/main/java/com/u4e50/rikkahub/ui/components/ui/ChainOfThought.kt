package com.u4e50.rikkahub.ui.components.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.ArrowDown01
import me.rerere.hugeicons.stroke.ArrowRight01
import me.rerere.hugeicons.stroke.ArrowUp01
import me.rerere.hugeicons.stroke.Search01
import me.rerere.hugeicons.stroke.Sparkles
import com.u4e50.rikkahub.R

private val LocalCardColor = staticCompositionLocalOf { Color.White }

/**
 * 浠ユ椂闂寸嚎/姝ラ鍗＄墖鐨勫舰寮忓睍绀轰竴缁勬€濊€冭繃绋嬨€?
 *
 * 閫傜敤浜庢壙杞芥帹鐞嗘楠ゃ€佸伐鍏疯皟鐢ㄦ楠わ紝鎴栦袱鑰呮贩鍚堢殑閾惧紡鍐呭銆傜粍浠舵敮鎸侊細
 * - 鍦ㄦ楠よ緝澶氭椂鑷姩鎶樺彔锛屼粎灞曠ず鏈€鍚庤嫢骞叉
 * - 鐐瑰嚮椤堕儴鎺у埗鏉″睍寮€/鏀惰捣鍏ㄩ儴姝ラ
 * - 閫氳繃 [collapsedAdaptiveWidth] 鎺у埗鎶樺彔鎬佹槸鍚︿繚鎸佽嚜閫傚簲瀹藉害
 *
 * @param modifier 澶栧眰鍗＄墖鐨勪慨楗扮
 * @param cardColors 鍗＄墖閰嶈壊
 * @param steps 闇€瑕佹覆鏌撶殑姝ラ鏁版嵁鍒楄〃
 * @param collapsedVisibleCount 鎶樺彔鏃朵繚鐣欏彲瑙佺殑灏鹃儴姝ラ鏁?
 * @param collapsedAdaptiveWidth 鏄惁鍦ㄦ姌鍙犳€佷笅浣跨敤鍐呭鑷€傚簲瀹藉害
 * @param content 姣忎釜姝ラ鐨勫叿浣?UI锛岀敱 [ChainOfThoughtScope] 鎻愪緵姝ラ鏋勫缓鑳藉姏
 */
@Composable
fun <T> ChainOfThought(
    modifier: Modifier = Modifier,
    cardColors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
    ),
    steps: List<T>,
    collapsedVisibleCount: Int = 2,
    collapsedAdaptiveWidth: Boolean = false,
    content: @Composable ChainOfThoughtScope.(T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val canCollapse = steps.size > collapsedVisibleCount
    val shouldFillCollapseControlWidth = expanded || !collapsedAdaptiveWidth

    CompositionLocalProvider(
        LocalCardColor provides cardColors.containerColor
    ) {
        Card(
            modifier = modifier,
            colors = cardColors,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .animateContentSize(
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    ),
            ) {
                val visibleSteps = if (expanded || !canCollapse) {
                    steps
                } else {
                    steps.takeLast(collapsedVisibleCount)
                }

                // 鏄剧ず灞曞紑/鎶樺彔鎸夐挳锛堢粺涓€鍦ㄩ《閮級
                if (canCollapse) {
                    Row(
                        modifier = Modifier
                            .then(
                                if (shouldFillCollapseControlWidth) {
                                    Modifier.fillMaxWidth()
                                } else {
                                    Modifier
                                }
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable { expanded = !expanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 宸︿晶锛氬浘鏍囧尯鍩燂紙24.dp锛屽拰姝ラ鍥炬爣瀵归綈锛?
                        Box(
                            modifier = Modifier.width(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (expanded) HugeIcons.ArrowUp01 else HugeIcons.ArrowDown01,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        // 鍙充晶锛氭枃瀛楀尯鍩燂紙8.dp 闂磋窛鍚庡紑濮嬶紝鍜屾楠?label 瀵归綈锛?
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = if (expanded) {
                                stringResource(R.string.chain_of_thought_collapse)
                            } else {
                                stringResource(
                                    R.string.chain_of_thought_show_more_steps,
                                    steps.size - collapsedVisibleCount
                                )
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                val lineColor = MaterialTheme.colorScheme.outlineVariant
                val scope = remember { ChainOfThoughtScopeImpl() }
                Box(
                    modifier = Modifier.drawBehind {
                        val x = 12.dp.toPx()
                        val offsetPx = 18.dp.toPx()
                        drawLine(
                            color = lineColor,
                            start = Offset(x, offsetPx),
                            end = Offset(x, size.height - offsetPx),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                ) {
                    Column {
                        visibleSteps.fastForEach { step ->
                            scope.content(step)
                        }
                    }
                }
            }
        }
    }
}

/**
 * [ChainOfThought] 鍐呴儴浣跨敤鐨勬楠ゆ覆鏌撲綔鐢ㄥ煙銆?
 *
 * 閫氳繃璇ヤ綔鐢ㄥ煙鍙互澹版槑鍗曚釜姝ラ鐨勫浘鏍囥€佹爣棰樸€侀檮鍔犱俊鎭互鍙婂彲灞曞紑鍐呭锛?
 * 骞跺鐢ㄧ粺涓€鐨勬椂闂寸嚎甯冨眬涓庝氦浜掕涓恒€?
 */
interface ChainOfThoughtScope {
    /**
     * 澹版槑涓€涓潪鍙楁帶姝ラ锛岀敱缁勪欢鍐呴儴绠＄悊灞曞紑/鎶樺彔鐘舵€併€?
     *
     * @param icon 姝ラ鍥炬爣
     * @param label 姝ラ鏍囬鍖哄煙
     * @param extra 鏍囬鍙充晶鐨勯檮鍔犱俊鎭?
     * @param onClick 鑷畾涔夌偣鍑昏涓猴紱璁剧疆鍚庝紭鍏堜簬灞曞紑/鎶樺彔閫昏緫
     * @param collapsedAdaptiveWidth 鏄惁鍦ㄦ姌鍙犱笖鍐呭闅愯棌鏃朵娇鐢ㄨ嚜閫傚簲瀹藉害
     * @param content 姝ラ灞曞紑鍚庢樉绀虹殑鍐呭锛涗负 `null` 鏃舵楠や笉鍙睍寮€
     */
    @Composable
    fun ChainOfThoughtStep(
        icon: (@Composable () -> Unit)? = null,
        label: (@Composable () -> Unit),
        extra: (@Composable () -> Unit)? = null,
        onClick: (() -> Unit)? = null,
        collapsedAdaptiveWidth: Boolean = false,
        content: (@Composable () -> Unit)? = null,
    )

    /**
     * 澹版槑涓€涓彈鎺ф楠わ紝鐢卞閮ㄤ紶鍏ュ睍寮€鐘舵€併€?
     *
     * 閫傚悎闇€瑕佷笌澶栭儴鐘舵€佽仈鍔ㄧ殑鍦烘櫙锛屼緥濡傗€滄帹鐞嗕腑棰勮 / 瀹屾垚鍚庢敹璧封€濄€?
     *
     * @param expanded 褰撳墠鏄惁澶勪簬灞曞紑鐘舵€?
     * @param onExpandedChange 灞曞紑鐘舵€佸彉鍖栧洖璋?
     * @param icon 姝ラ鍥炬爣
     * @param label 姝ラ鏍囬鍖哄煙
     * @param extra 鏍囬鍙充晶鐨勯檮鍔犱俊鎭?
     * @param onClick 鑷畾涔夌偣鍑昏涓猴紱璁剧疆鍚庝紭鍏堜簬灞曞紑/鎶樺彔閫昏緫
     * @param collapsedAdaptiveWidth 鏄惁鍦ㄦ姌鍙犱笖鍐呭闅愯棌鏃朵娇鐢ㄨ嚜閫傚簲瀹藉害
     * @param contentVisible 鏄惁灞曠ず鍐呭鍖哄煙锛屽彲涓?[expanded] 瑙ｈ€?
     * @param content 姝ラ鍐呭锛涗负 `null` 鏃舵楠や笉鍙睍寮€
     */
    @Composable
    fun ControlledChainOfThoughtStep(
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        icon: (@Composable () -> Unit)? = null,
        label: (@Composable () -> Unit),
        extra: (@Composable () -> Unit)? = null,
        onClick: (() -> Unit)? = null,
        collapsedAdaptiveWidth: Boolean = false,
        contentVisible: Boolean = expanded,
        content: (@Composable () -> Unit)? = null,
    )
}

private class ChainOfThoughtScopeImpl : ChainOfThoughtScope {
    @Composable
    override fun ChainOfThoughtStep(
        icon: @Composable (() -> Unit)?,
        label: @Composable (() -> Unit),
        extra: @Composable (() -> Unit)?,
        onClick: (() -> Unit)?,
        collapsedAdaptiveWidth: Boolean,
        content: @Composable (() -> Unit)?
    ) {
        var expanded by remember { mutableStateOf(false) }
        ChainOfThoughtStepContent(
            icon = icon,
            label = label,
            extra = extra,
            onClick = onClick,
            collapsedAdaptiveWidth = collapsedAdaptiveWidth,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            contentVisible = expanded,
            content = content,
        )
    }

    @Composable
    override fun ControlledChainOfThoughtStep(
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        icon: @Composable (() -> Unit)?,
        label: @Composable (() -> Unit),
        extra: @Composable (() -> Unit)?,
        onClick: (() -> Unit)?,
        collapsedAdaptiveWidth: Boolean,
        contentVisible: Boolean,
        content: @Composable (() -> Unit)?
    ) {
        ChainOfThoughtStepContent(
            icon = icon,
            label = label,
            extra = extra,
            onClick = onClick,
            collapsedAdaptiveWidth = collapsedAdaptiveWidth,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            contentVisible = contentVisible,
            content = content,
        )
    }

    @Composable
    private fun ChainOfThoughtStepContent(
        icon: @Composable (() -> Unit)?,
        label: @Composable (() -> Unit),
        extra: @Composable (() -> Unit)?,
        onClick: (() -> Unit)?,
        collapsedAdaptiveWidth: Boolean,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        contentVisible: Boolean,
        content: @Composable (() -> Unit)?
    ) {
        val hasContent = content != null
        val shouldFillMaxWidth = !collapsedAdaptiveWidth || contentVisible

        Column(
            modifier = Modifier.then(
                if (shouldFillMaxWidth) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                }
            ),
        ) {
            // Label 琛岋細Icon + Label + Extra + 鎸囩ず鍣?
            Row(
                modifier = Modifier
                    .then(
                        if (shouldFillMaxWidth) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (onClick != null) {
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onClick() }
                        } else if (hasContent) {
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onExpandedChange(!expanded) }
                        } else {
                            Modifier
                        }
                    )
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon锛堜笉閫忔槑鑳屾櫙閬綇鑳屽悗鐨勮繛绾匡級
                Box(
                    modifier = Modifier.width(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(LocalCardColor.current),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (icon != null) {
                            Box(
                                modifier = Modifier.size(14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                icon()
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                // Label
                Box(
                    modifier = Modifier.then(
                        if (shouldFillMaxWidth) {
                            Modifier.weight(1f)
                        } else {
                            Modifier
                        }
                    )
                ) {
                    label()
                }

                // Extra
                if (extra != null) {
                    extra()
                }

                // 鎸囩ず鍣細onClick 鏄剧ず鍚戝彸绠ご锛宑ontent 鏄剧ず灞曞紑/鎶樺彔绠ご
                if (onClick != null) {
                    Icon(
                        imageVector = HugeIcons.ArrowRight01,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (hasContent) {
                    Icon(
                        imageVector = if (expanded) HugeIcons.ArrowUp01 else HugeIcons.ArrowDown01,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 灞曞紑鍐呭锛堢缉杩涘榻?label锛?
            if (contentVisible && hasContent) {
                Box(
                    modifier = Modifier
                        .then(
                            if (shouldFillMaxWidth) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                            }
                        )
                        .padding(start = 32.dp, top = 4.dp, bottom = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChainOfThoughtPreview() {
    // 瀹氫箟姝ラ鏁版嵁绫?
    data class StepData(
        val label: String,
        val icon: ImageVector?,
        val status: String?,
        val hasContent: Boolean = false,
        val hasOnClick: Boolean = false,
        val controlled: Boolean = false,
    )

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Chain of thought")
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
            ) {
                // 鍙楁帶鐘舵€佺ず渚?
                var controlledExpanded by remember { mutableStateOf(false) }

                ChainOfThought(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    steps = listOf(
                        StepData("Searching the web", HugeIcons.Search01, "3 results", hasContent = true),
                        StepData("Reading documents", HugeIcons.Sparkles, "Completed", hasOnClick = true),
                        StepData(
                            "Analyzing results (controlled)",
                            HugeIcons.Sparkles,
                            "In progress",
                            hasContent = true,
                            controlled = true
                        ),
                        StepData("Step without icon", null, null),
                        StepData("Final step", HugeIcons.Sparkles, "Done"),
                    ),
                    collapsedVisibleCount = 2,
                ) { step ->
                    val iconComposable: (@Composable () -> Unit)? = step.icon?.let {
                        {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    val labelComposable: @Composable () -> Unit = {
                        Text(step.label, style = MaterialTheme.typography.bodyMedium)
                    }
                    val extraComposable: (@Composable () -> Unit)? = step.status?.let {
                        {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    val onClickHandler: (() -> Unit)? = if (step.hasOnClick) {
                        { /* Open bottom sheet */ }
                    } else null
                    val contentComposable: (@Composable () -> Unit)? = if (step.hasContent) {
                        {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (step.label.contains("Search")) {
                                    listOf(
                                        "example.com - Example Domain",
                                        "docs.example.com - Documentation",
                                        "blog.example.com - Blog Post"
                                    ).forEach { result ->
                                        Text(
                                            text = "• $result",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "This is expandable content showing detailed analysis. " +
                                            "It can contain multiple lines of text, code snippets, " +
                                            "or any other composable content.",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    } else null

                    if (step.controlled) {
                        // 鍙楁帶鐗堟湰
                        ControlledChainOfThoughtStep(
                            expanded = controlledExpanded,
                            onExpandedChange = { controlledExpanded = it },
                            icon = iconComposable,
                            label = labelComposable,
                            extra = extraComposable,
                            onClick = onClickHandler,
                            content = contentComposable,
                        )
                    } else {
                        // 闈炲彈鎺х増鏈?
                        ChainOfThoughtStep(
                            icon = iconComposable,
                            label = labelComposable,
                            extra = extraComposable,
                            onClick = onClickHandler,
                            content = contentComposable,
                        )
                    }
                }
            }
        }
    }
}

