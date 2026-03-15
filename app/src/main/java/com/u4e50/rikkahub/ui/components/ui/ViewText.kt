package com.u4e50.rikkahub.ui.components.ui

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

@Composable
fun ViewText(
    text: CharSequence,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val density = LocalDensity.current
    val mergedStyle = style.merge(LocalContentColor.current)
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                movementMethod = LinkMovementMethod.getInstance()
                setText(text)
                setComposeTextStyle(density, mergedStyle)
            }
        },
        modifier = modifier,
        update = { view ->
            view.setComposeTextStyle(density, mergedStyle)
            view.text = text
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun TextViewPreview() {
    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val style = MaterialTheme.typography.bodyMedium
            Text(
                text = buildAnnotatedString {
                    append("How many roads must a man walk down How many roads must a man walk downHow many roads must a man walk down")
                    withStyle(SpanStyle(fontSize = 39.sp)) {
                        append("BIG TEXT")
                    }
                    append("ahah")
                },
                style = style,
            )

            HorizontalDivider()

            // AndroidView TextView 澶嶅埢鐗堟湰
            // 鍒涘缓 SpannableString 鏉ュ鍒?AnnotatedString 鐨勬晥鏋?
            val fullText =
                "How many roads must a man walk down How many roads must a man walk downHow many roads must a man walk downBIG TEXTahah"
            val spannableString = SpannableString(fullText)

            // 鎵惧埌 "BIG TEXT" 鐨勪綅缃苟搴旂敤澶у瓧浣撴牱寮?
            val bigTextStart = fullText.indexOf("BIG TEXT")
            val bigTextEnd = bigTextStart + "BIG TEXT".length

            // 灏?39.sp 杞崲涓哄儚绱?
            val density = LocalDensity.current
            val bigTextSizePx = with(density) { 39.sp.toPx().toInt() }
            spannableString.setSpan(
                AbsoluteSizeSpan(bigTextSizePx),
                bigTextStart,
                bigTextEnd,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            ViewText(
                text = spannableString,
                modifier = Modifier.fillMaxWidth(),
                style = style
            )
        }
    }
}

private fun TextView.setComposeTextStyle(
    density: Density,
    textStyle: TextStyle
) {
    with(density) {
        // text color
        setTextColor(textStyle.color.toArgb())

        // text size
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.fontSize.toPx())

        // letter spacing
        if (textStyle.letterSpacing.isSpecified) {
            letterSpacing = when (textStyle.letterSpacing.type) {
                TextUnitType.Em -> textStyle.letterSpacing.value
                TextUnitType.Sp -> textStyle.letterSpacing.toPx() / textStyle.fontSize.toPx()
                else -> 1f
            }
        }

        // decoration
        textStyle.textDecoration?.let {
            var flags = paintFlags
            if (it.contains(TextDecoration.Underline)) {
                flags = flags or Paint.UNDERLINE_TEXT_FLAG
            }
            if (it.contains(TextDecoration.LineThrough)) {
                flags = flags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            paintFlags = flags
        }

        // align
        textStyle.textAlign.let {
            gravity = when (it) {
                TextAlign.Left -> Gravity.LEFT
                TextAlign.Right -> Gravity.RIGHT
                TextAlign.Center -> Gravity.CENTER_HORIZONTAL
                TextAlign.Start -> Gravity.START
                TextAlign.End -> Gravity.END
                TextAlign.Justify -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_CHARACTER
                    }
                    // 涓ょ瀵归綈涔熼渶瑕佷竴涓熀纭€鐨?gravity锛岄€氬父鏄?START
                    Gravity.START
                }

                else -> gravity // 淇濇寔褰撳墠 gravity
            }
        }

        // line height
        if (textStyle.lineHeight.isSpecified) {
            val lineHeightPx = when (textStyle.lineHeight.type) {
                TextUnitType.Em -> textStyle.lineHeight.value * textStyle.fontSize.toPx()
                TextUnitType.Sp -> textStyle.lineHeight.toPx()
                else -> textStyle.lineHeight.value // 榛樿浣跨敤 px
            }
            // Android P (API 28) 鍙婁互涓婄増鏈彲浠ョ洿鎺ヨ缃楂?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lineHeight = lineHeightPx.roundToInt()
            } else {
                // 瀵逛簬鏃х増鏈紝閫氳繃 setLineSpacing 瀹炵幇
                // 绗竴涓弬鏁版槸棰濆闂磋窛锛岀浜屼釜鏄楂樺€嶆暟
                // extra = desired_line_height - font_metrics_height
                val fontMetrics = paint.fontMetricsInt
                val extraSpacing = lineHeightPx - (fontMetrics.descent - fontMetrics.ascent)
                setLineSpacing(extraSpacing.toFloat(), 1.0f)
            }
        }

        // shadow
        textStyle.shadow?.let { shadow ->
            setShadowLayer(
                shadow.blurRadius,
                shadow.offset.x,
                shadow.offset.y,
                shadow.color.toArgb()
            )
        }

        // 杩欐槸鏈€澶嶆潅鐨勯儴鍒嗭紝鍥犱负瀹冮渶瑕佸皢 Compose 鐨勫瓧浣撴蹇垫槧灏勫埌 Android 鐨?Typeface
        val typefaceStyle = getAndroidTypefaceStyle(
            fontWeight = textStyle.fontWeight,
            fontStyle = textStyle.fontStyle
        )
        val finalTypeface = when (textStyle.fontFamily) {
            FontFamily.SansSerif, null -> Typeface.create(Typeface.SANS_SERIF, typefaceStyle)
            FontFamily.Serif -> Typeface.create(Typeface.SERIF, typefaceStyle)
            FontFamily.Monospace -> Typeface.create(Typeface.MONOSPACE, typefaceStyle)
            FontFamily.Cursive -> Typeface.create(
                Typeface.SANS_SERIF,
                typefaceStyle
            ) // Cursive 娌℃湁鐩存帴鏄犲皠锛屽洖閫€鍒?SansSerif
            // 娉ㄦ剰锛氳繖閲屾病鏈夊鐞嗚嚜瀹氫箟瀛椾綋 (FontFamily(Font(...)))
            // 瑕佸鐞嗚嚜瀹氫箟瀛椾綋锛岄渶瑕佹洿澶嶆潅鐨勯€昏緫鏉ュ姞杞藉瓧浣撹祫婧?
            else -> Typeface.create(typeface, typefaceStyle)
        }
        setTypeface(finalTypeface)
    }
}

private fun getAndroidTypefaceStyle(
    fontWeight: FontWeight?,
    fontStyle: FontStyle?
): Int {
    val isBold = fontWeight != null && fontWeight >= FontWeight.W600
    val isItalic = fontStyle == FontStyle.Italic
    return when {
        isBold && isItalic -> Typeface.BOLD_ITALIC
        isBold -> Typeface.BOLD
        isItalic -> Typeface.ITALIC
        else -> Typeface.NORMAL
    }
}

