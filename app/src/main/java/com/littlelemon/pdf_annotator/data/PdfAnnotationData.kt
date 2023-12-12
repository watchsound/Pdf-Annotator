package com.littlelemon.pdf_annotator.data

import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import com.littlelemon.pdf_annotator.R
import java.util.LinkedList
import java.util.UUID

enum class AnnotationType(val title: Int ) {
    Highlight(R.string.annotation_highlight),
    HasNote(R.string.annotation_has_notes),
    StrikeOut(R.string.annotation_strikeout),
    Underline(R.string.annotation_underline_single),
    UnderlineDouble(R.string.annotation_underline_double),
    UnderlineDash(R.string.annotation_underline_dash),
    Squiggly(R.string.annotation_underline_squiggly)
}

data class AnnotationItem(
    val uuid : UUID,
    var type: AnnotationType = AnnotationType.Highlight,
    var color: Color = Color.Red,
    var text: String = "",
    var regions: MutableList<Rect>,
    var extraRef: Any? = null
){
    fun region(): Rect {
        var r0 = regions[0]
        var x0 = r0.left
        var y0 = r0.top
        var x1 = r0.right
        var y1 = r0.bottom
        regions.forEach {
            x0 = if( it.left < x0 )  it.left else x0
            y0 = if( it.top < y0 )  it.top else y0
            x1 = if( it.right > x1 )  it.right else x1
            y1 = if( it.bottom > y1 )  it.bottom else y1
        }
        return Rect( x0, y0, x1, y1)
    }
}

data class AnnotationInPage(
    var pageNum: Int = 0,
    var items: MutableList<AnnotationItem>
){
}


data class AnnotationInDoc(
    var scale: Float=1F,
    var pages: MutableList<AnnotationInPage>
){
}





