
package com.littlelemon.pdf_annotator

import android.graphics.Rect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.littlelemon.pdf_annotator.data.*
import java.lang.Math.min
import kotlin.random.Random


private val lineThickness = 1.dp

var Rect.width: Int
    get() = right - left
    set(value) { right = left + value }
var Rect.height: Int
    get() = bottom - top
    set(value) { bottom = top + value }

@Composable
@OptIn(ExperimentalTextApi::class)
fun Highlight(
    item: AnnotationItem,
    scale: Float=1f,
    modifier: Modifier = Modifier,
) {
    var fullRegion = item.region();
    var fheight = fullRegion.height * scale  //.coerceAtLeast(25*scale)
    var fwidth = fullRegion.width *scale
    Box(
        modifier = modifier
            .height(fheight .dp)
            .width(fwidth.dp)
    ) {
        Spacer(
            modifier = Modifier
                .drawWithCache {
                    onDrawBehind {
                        translate(
                            left = -fullRegion.left * scale,
                            top = -fullRegion.top * scale
                        ) {
                            for (region in item.regions) {
                                drawRect(
                                    topLeft = Offset(
                                        region.left * scale,
                                        region.top * scale
                                    ),
                                    size = Size(region.width * scale, region.height * scale),
                                    color = item.color
                                )
                            }
                        }
                    }
                }
                .height(fheight .dp)
                .width(fwidth.dp)
        )
    }
}

@Composable
fun Underline(
    item: AnnotationItem,
    scale: Float=1f,
    modifier: Modifier = Modifier,
) {
    var fullRegion = item.region();
    var fheight = fullRegion.height * scale  //.coerceAtLeast(25*scale)
    var fwidth = fullRegion.width *scale
    val lineThicknessPx =  LocalDensity.current.run{  lineThickness.toPx() }
    Box(
        modifier = modifier
            .height(fheight .dp)
            .width(fwidth.dp)
    ){
        Spacer(
            modifier = Modifier
                .drawWithCache {
                    onDrawBehind {
                        translate(
                            left = -fullRegion.left * scale,
                            top = -fullRegion.top * scale
                        ) {
                            val path = Path()
                            for (region in item.regions) {
                                path.moveTo(
                                    region.left * scale,
                                    region.bottom * scale - lineThicknessPx * scale
                                )
                                path.lineTo(
                                    region.right * scale,
                                    region.bottom * scale - lineThicknessPx * scale
                                )
                            }
                            drawPath(
                                color = item.color,
                                path = path,
                                style = Stroke(
                                    width = lineThicknessPx  ,
                                    //  pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            )
                        }

                    }
                }
                .height(fheight .dp)
                .width(fwidth.dp)
        )
    }
}
@Composable
fun UnderlineDouble(
    item: AnnotationItem,
    scale: Float=1f,
    modifier: Modifier = Modifier,
) {
    var fullRegion = item.region();
    var fheight = fullRegion.height * scale  //.coerceAtLeast(25*scale)
    var fwidth = fullRegion.width *scale
    val lineThicknessPx =  LocalDensity.current.run{  lineThickness.toPx() }
    Box(
        modifier = modifier
            .height(fheight .dp)
            .width(fwidth.dp)
    ){
        Spacer(
            modifier = Modifier
                .drawWithCache {
                    onDrawBehind {
                        translate(
                            left = -fullRegion.left * scale,
                            top = -fullRegion.top * scale
                        ) {
                            val path = Path()
                            for (region in item.regions) {
                                path.moveTo(
                                    region.left * scale,
                                    region.bottom * scale - lineThicknessPx * scale
                                )
                                path.lineTo(
                                    region.right * scale,
                                    region.bottom * scale - lineThicknessPx * scale
                                )
                                path.moveTo(
                                    region.left * scale,
                                    region.bottom * scale - lineThicknessPx * 2 * scale
                                )
                                path.lineTo(
                                    region.right * scale,
                                    region.bottom * scale - lineThicknessPx * 2 * scale
                                )
                            }
                            drawPath(
                                color = item.color,
                                path = path,
                                style = Stroke(
                                    width = lineThicknessPx  ,
                                    //  pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            )
                        }

                    }
                }
                .height(fheight .dp)
                .width(fwidth.dp)
        )
    }
}

@Composable
fun UnderlineDash(
    item: AnnotationItem,
    scale: Float=1f,
    modifier: Modifier = Modifier,
) {
    var fullRegion = item.region();
    var fheight = fullRegion.height * scale  //.coerceAtLeast(25*scale)
    var fwidth = fullRegion.width *scale
    val lineThicknessPx =  LocalDensity.current.run{  lineThickness.toPx() }
    Box(
        modifier = modifier
            .height(fheight .dp)
            .width(fwidth.dp)
    ){
        Spacer(
            modifier = Modifier
                .drawWithCache {
                    onDrawBehind {
                        translate(
                            left = -fullRegion.left * scale,
                            top = -fullRegion.top * scale
                        ) {
                            val path = Path()
                            for (region in item.regions) {
                                path.moveTo(
                                    region.left * scale,
                                    region.bottom * scale - lineThicknessPx * scale
                                )
                                path.lineTo(
                                    region.right * scale,
                                    region.bottom * scale - lineThicknessPx * scale
                                )
                            }
                            drawPath(
                                color = item.color,
                                path = path,
                                style = Stroke(
                                    width = lineThicknessPx  ,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            )
                        }

                    }
                }
                .height(fheight .dp)
                .width(fwidth.dp)
        )
    }
}
@Composable
fun HasNote(
    item: AnnotationItem,
    scale: Float=1f,
    modifier: Modifier = Modifier,
    onClick: (   ) -> Unit,
) {
    var fullRegion = item.region();
    var fheight = fullRegion.height * scale  //.coerceAtLeast(25*scale)
    var fwidth = fullRegion.width *scale
    val lineThicknessPx =  LocalDensity.current.run{  lineThickness.toPx() }
    Box(
        modifier = modifier
            .height(fheight.dp)
            .width(fwidth.dp)
            .clickable(
            ) {
                onClick( )
            }
    ){
        Spacer(
            modifier = Modifier
                .drawWithCache {
                    onDrawBehind {
                        translate(
                            left = -fullRegion.left * scale,
                            top = -fullRegion.top * scale
                        ) {
                            val path = Path()
                            for (region in item.regions) {
                                path.moveTo(
                                    region.left * scale,
                                    region.bottom * scale - lineThicknessPx * scale
                                )
                                path.lineTo(
                                    region.right * scale,
                                    region.bottom * scale - lineThicknessPx * scale
                                )
                            }
                            drawPath(
                                color = item.color,
                                path = path,
                                style = Stroke(
                                    width = lineThicknessPx  ,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            )
                        }
                    }
                }
                .height(fheight.dp)
                .width(fwidth.dp)
        ) 
        Icon(
              painter = painterResource(R.drawable.stickynote),
              contentDescription = "",
              modifier = Modifier.offset( 0.dp, - (fheight/2).dp)
        )
    }
}

@Composable
fun StrikeOut(
    item: AnnotationItem,
    scale: Float=1f,
    modifier: Modifier = Modifier,
) {
    var fullRegion = item.region();
    var fheight = fullRegion.height * scale  //.coerceAtLeast(25*scale)
    var fwidth = fullRegion.width *scale
    val lineThicknessPx =  LocalDensity.current.run{  lineThickness.toPx() }
    Box(
        modifier = modifier
            .height(fheight .dp)
            .width(fwidth.dp)
    ){
        Spacer(
            modifier = Modifier
                .drawWithCache {
                    onDrawBehind {
                        translate(
                            left = -fullRegion.left * scale,
                            top = -fullRegion.top * scale
                        ) {
                            val path = Path()
                            for (region in item.regions) {
                                path.moveTo(
                                    region.left * scale,
                                    region.top * scale + fheight / 2 * scale
                                )
                                path.lineTo(
                                    region.right * scale,
                                    region.top * scale + fheight / 2 * scale
                                )
                            }
                            drawPath(
                                color = item.color,
                                path = path,
                                style = Stroke(
                                    width = lineThicknessPx  ,
                                    //  pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            )
                        }

                    }
                }
                .height(fheight .dp)
                .width(fwidth.dp)
        )
    }
}

@Composable
fun Squiggly(
    item: AnnotationItem,
    scale: Float=1f,
    modifier: Modifier = Modifier,
) {
    var fullRegion = item.region();
    var fheight = fullRegion.height * scale  //.coerceAtLeast(25*scale)
    var fwidth = fullRegion.width *scale
    val lineThicknessPx =  LocalDensity.current.run{  lineThickness.toPx() }

    var path by remember {
        mutableStateOf(createZipPath(scale, item))
    }
    Box(
        modifier = modifier
            .height(fheight .dp)
            .width(fwidth.dp)
    ){
        Spacer(
            modifier = Modifier
                .drawWithCache {
                    onDrawBehind {
                        translate(
                            left = -fullRegion.left * scale,
                            top = -fullRegion.top * scale
                        ) {
                            drawPath(
                                color = item.color,
                                path = path,
                                style = Stroke(
                                    width = lineThicknessPx  ,
                                    //   pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            )
                        }

                    }
                }
                .height(fheight .dp)
                .width(fwidth.dp)
        )
    }
}

fun createZipPath(scale: Float=1f, item: AnnotationItem): Path {
    val aipLength = 30 * scale
    val zipHeight = 10f.coerceAtMost(5 * scale)
    val path = Path()
    var rx0: Float
    var ry0: Float
    var rx1: Float
    var ry1: Float
    var rw: Int
    var rh: Int
    var curpos: Int
    for (region in item.regions) {
        rx0 = region.left * scale
        ry0 = region.top * scale
        rx1 = region.right * scale
        ry1 = region.bottom * scale
        rw = (region.width * scale).toInt()
        rh = (region.height * scale).toInt()
        path.moveTo(rx0, ry0 + rh)
        curpos = rx0.toInt()
        while (curpos < rx1) {
            //random pick length [10-30]
            if (rx1 - curpos <= zipHeight) {
                path.lineTo(rx1, ry1 - zipHeight)
                break
            }
            curpos += Random.nextInt(
                zipHeight.toInt(),
                min((rx1 - curpos).toInt(), aipLength.toInt())
            )
            path.lineTo(curpos.toFloat(), ry1 - zipHeight)
            curpos -= Random.nextInt((2 * scale).toInt(), zipHeight.toInt())
            path.lineTo(curpos.toFloat(), ry1)
        }
    }
    return path
}

