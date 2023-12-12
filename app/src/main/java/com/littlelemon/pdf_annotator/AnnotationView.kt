package com.littlelemon.pdf_annotator

import android.graphics.Rect
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.littlelemon.pdf_annotator.data.AnnotationInPage
import com.littlelemon.pdf_annotator.data.AnnotationType
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun AnnotationView(
    count: Int,
    item: @Composable AnnotationViewScope.(index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items  = @Composable { repeat(count) { index -> AnnotationViewScope.item(index) } }
    Layout(
        contents =  listOf( items ),
        modifier = modifier
    ) {
            (measurables), constraints ->
        val looseConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )
        // 4
        val startXs = measurables.map { measurable ->
            val parentData = measurable.parentData as? AnnotationViewParentData
            parentData?.startX ?: 0f
        }
        val startYs = measurables.map { measurable ->
            val parentData = measurable.parentData as? AnnotationViewParentData
            parentData?.startY ?: 0f
        }
        val placeables = measurables.map { measurable ->
            measurable.measure(looseConstraints)
        }

        layout(constraints.maxWidth,constraints.maxHeight) {
            for (i in placeables.indices) {
                var place = placeables[i]
                place.place( startXs[i].toInt() ,  startYs[i].toInt()  )
            }
        }
    }
}

@LayoutScopeMarker
@Immutable
object AnnotationViewScope {
    @Stable
    fun Modifier.annotLoc(
        region: Rect,
        scale: Float = 1f
    ): Modifier {
        return then(
            AnnotationViewParentData(
                startX = region.left * scale ,
                startY = region.top * scale
            )
        )
    }
}

class AnnotationViewParentData(
    val startX: Float,
    val startY: Float,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@AnnotationViewParentData
}
