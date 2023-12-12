package com.littlelemon.pdf_annotator

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * source code is from
 * "Creating a Popup With Custom Animation Using Jetpack Compose"
 * author: Tony Awino
 * from https://betterprogramming.pub/creating-a-popup-with-custom-animation-using-jetpack-compose-bad4af27d4ec
 *
 */

@Stable
class PopupState(
    isVisible: Boolean = false
) {
    /**
     * Horizontal alignment from which the popup will expand from and shrink to.
     */
    var horizontalAlignment: Alignment.Horizontal by mutableStateOf(Alignment.CenterHorizontally)

    /**
     * Boolean that defines whether the popup is displayed above or below the anchor.
     */
    var isTop: Boolean by mutableStateOf(false)

    /**
     * Boolean that defines whether the popup is currently visible or not.
     */
    var isVisible: Boolean by mutableStateOf(isVisible)
}

@Immutable
private data class CustomPopupPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val onPopupPositionFound: (Alignment.Horizontal, Boolean) -> Unit
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        val isFitEnd = (anchorBounds.left + contentOffsetX + popupContentSize.width) < windowSize.width
        val isFitStart = (anchorBounds.left - contentOffsetX - popupContentSize.width) > 0
        val popupHalfWidth = popupContentSize.width / 2
        val halfAnchor = (anchorBounds.right - anchorBounds.left) / 2
        val isFitCenter =
            ((anchorBounds.left + halfAnchor + popupHalfWidth) < windowSize.width) &&
                    ((anchorBounds.left + halfAnchor - popupHalfWidth) > 0)

        val endPlacementOffset = anchorBounds.left - contentOffsetX
        val centerPlacementOffset = anchorBounds.left - popupHalfWidth + contentOffsetX
        val startPlacementOffset = anchorBounds.right + contentOffsetX - popupContentSize.width

        val bottomCoordinatesY = anchorBounds.bottom + popupContentSize.height
        val isFitBottom = bottomCoordinatesY <= windowSize.height
        val topCoordinatesY = anchorBounds.top - popupContentSize.height
        val isFitTop = topCoordinatesY > 0 || anchorBounds.top > windowSize.height

        // Compute vertical position.
        val toBottom = anchorBounds.bottom + contentOffsetY
        val toTop = anchorBounds.top - contentOffsetY - popupContentSize.height
        val toCenter = anchorBounds.top - popupContentSize.height / 2
        val toDisplayBottom = windowSize.height - popupContentSize.height
        val yOffset = sequenceOf(
            if (isFitTop) toBottom else toTop,
            toCenter,
            toDisplayBottom
        ).firstOrNull {
            it + popupContentSize.height <= windowSize.height
        } ?: toTop

        val horizontalAndOffset = getHorizontalOffset(
            isFitsStart = isFitStart,
            isFitsEnd = isFitEnd,
            isFitsCenter = isFitCenter,
            endPlacementOffset = endPlacementOffset,
            startPlacementOffset = startPlacementOffset,
            centerPlacementOffset = centerPlacementOffset
        )

        onPopupPositionFound(horizontalAndOffset.first, isFitTop)
        return IntOffset(horizontalAndOffset.second, yOffset)
    }
}
@Composable
private fun CustomPopupContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOrigin: TransformOrigin,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Menu open/close animation.
    val transition = updateTransition(expandedStates, "Popup")

    // Scale animation.
    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded.
                tween(durationMillis = 200)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = 200)
            }
        },
        label = "Popup Scale"
    ) {
        if (it) {
            // Popup is expanded.
            1f
        } else {
            // Popup is dismissed.
            0f
        }
    }

    // Alpha animation.
    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded.
                tween(durationMillis = 200)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = 200)
            }
        },
        label = "Popup Alpha"
    ) {
        if (it) {
            // Popup is expanded.
            1f
        } else {
            // Popup is dismissed.
            0f
        }
    }

    // Helper function for applying animations to graphics layer.
    fun GraphicsLayerScope.graphicsLayerAnim() {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        this.transformOrigin = transformOrigin
    }

    Surface(
        modifier = Modifier
            .graphicsLayer {
                graphicsLayerAnim()
            }
    ) {
        Column(
            modifier = modifier
                .width(IntrinsicSize.Max)
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}


/**
 * Get the horizontal offset of the popup based on fitting and placement.
 *
 * @param isFitsStart    Whether the popup fits when placed at the start.
 * @param isFitsEnd      Whether the popup fits when placed at the end.
 * @param isFitsCenter   Whether the popup fits when placed at the center.
 * @param endPlacementOffset     The offset when the popup is placed at the end.
 * @param startPlacementOffset   The offset when the popup is placed at the start.
 * @param centerPlacementOffset  The offset when the popup is placed at the center.
 * @return A pair consisting of the horizontal alignment and the horizontal offset.
 */
private fun getHorizontalOffset(
    isFitsStart: Boolean,
    isFitsEnd: Boolean,
    isFitsCenter: Boolean,
    endPlacementOffset: Int,
    startPlacementOffset: Int,
    centerPlacementOffset: Int
): Pair<Alignment.Horizontal, Int> {

    // Check which alignment fits the best.
    val alignments = listOf(
        Alignment.Start,
        Alignment.CenterHorizontally,
        Alignment.End
    )

    // Check the corresponding offsets.
    val offsets = listOf(
        endPlacementOffset,
        centerPlacementOffset,
        startPlacementOffset
    )

    // Check which alignment and offset fits the best.
    val fallbacks = mutableListOf<Pair<Alignment.Horizontal, Int>>()
    for (index in 0..2) {
        if (listOf(isFitsEnd, isFitsCenter, isFitsStart)[index]) {
            fallbacks.add(Pair(alignments[index], offsets[index]))
        }
    }

    // If there is a fallback, choose it as the alignment and offset.
    val fallback = fallbacks.firstOrNull()
    if (fallback != null) {
        return fallback
    }

    // If there is no fallback, calculate the horizontal offset.
    val finalHorizontalFallback = if (isFitsStart) {
        0
    } else if (isFitsEnd) {
        2
    } else {
        1
    }
    val fallbackHorizontalOffset = offsets[finalHorizontalFallback]
    return Pair(alignments[finalHorizontalFallback], fallbackHorizontalOffset)
}

@Composable
fun CustomPopup(
    popupState: PopupState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) {
    // Create a transition state to track whether the popup is expanded.
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = popupState.isVisible

    // Only show the popup if it's visible.
    if (expandedStates.currentState || expandedStates.targetState) {
        val density = LocalDensity.current

        // Instantiate a CustomPopupPositionProvider with the given offset.
        val popupPositionProvider = CustomPopupPositionProvider(
            contentOffset = offset,
            density = density
        ) { alignment, isTop ->
            // Update the PopupState's alignment and direction.
            popupState.horizontalAlignment = alignment
            popupState.isTop = !isTop
        }

        // Display the popup using the Popup composable.
        Popup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = popupPositionProvider,
            properties = properties
        ) {
            // Display the popup's content using the CustomPopupContent composable.
            CustomPopupContent(
                expandedStates = expandedStates,
                transformOrigin = TransformOrigin(
                    pivotFractionX = when(popupState.horizontalAlignment) {
                        Alignment.Start -> 0f
                        Alignment.CenterHorizontally -> 0.5f
                        else -> 1f
                    },
                    pivotFractionY = if (popupState.isTop) 1f else 0f
                ),
                modifier = modifier,
                content = content
            )
        }
    }
}

@Composable
private fun IconWithCustomPopup(
    popupState: PopupState = remember {
        PopupState(false)
    },
    text: String
) {
    Box {
        CustomPopup(
            popupState = popupState,
            onDismissRequest = {
                popupState.isVisible = false
            }
        ) {
            Text(
                text = text,
                modifier = Modifier.background(Color.Yellow)
            )
        }
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info Icon",
            modifier = Modifier.clickable {
                popupState.isVisible = !popupState.isVisible
            }
        )
    }
}