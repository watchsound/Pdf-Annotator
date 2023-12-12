package com.littlelemon.pdf_annotator

import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripperByArea
import com.tom_roush.pdfbox.text.TextPosition
import java.io.IOException
import java.util.Collections
import kotlin.math.abs
import kotlin.math.roundToInt



@Composable
fun PdfView(document: PDDocument, pagePos: Int,  renderer : PDFRenderer,
            stripper: MyTextStripperByArea,
            myTextSelection : MyTextSelection,
            selectionCallback: () -> Unit) {
    val paint = Paint().apply {
        color = Color.White.toArgb()
    }

    val dpi = LocalContext.current.resources.displayMetrics.densityDpi
    var scale : Float = (dpi / 72F).roundToInt().toFloat()

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var startX by remember { mutableStateOf(0f) }
    var startY by remember { mutableStateOf(0f) }
    var endX by remember { mutableStateOf(0f) }
    var endY by remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if( !startX.isNaN() ){
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            var p = stripper.getTextByLoc(pagePos, offsetX/scale , offsetY/scale )
                            if ( p != null  ){
                                myTextSelection.add(p)
                            }
                        }
                    },
                    onDragStart = { offset ->
                        Log.d("User Input", "Drag start @ ${offset.x}, ${offset.y}")
                        myTextSelection.start()
                        var p = stripper.getTextByLoc(pagePos, offset.x/scale , offset.y/scale )
                        if ( p == null ){
                            offsetX = 0f
                            offsetY = 0f
                            startX = Float.NaN
                            startX = Float.NaN
                            Log.d("User Input", "No word selected")
                        } else {
                            offsetX = offset.x
                            offsetY = offset.y
                            startX = offset.x
                            startY = offset.y
                            myTextSelection.add(p)
                            Log.d("User Input", "having some word selected")
                        }

                    },
                    onDragEnd = {
                        endX = offsetX
                        endY = offsetY
                        if( !startX.isNaN() ){
                            var p = stripper.getTextByLoc(pagePos, offsetX/scale , offsetY/scale )
                            if ( p != null ){
                                myTextSelection.add(p)
                            }
                            selectionCallback()
                        }
                        Log.d("User Input", "Drag end @ ${offsetX}, ${offsetY}")

                        val rect = RectF(startX/scale, startY/scale, endX/scale , endY/scale )
                        stripper.addRegion("class1", rect)
                        val firstPage = document.getPage(pagePos)
                        stripper.extractRegions(firstPage)
                        Log.d("","Text in the area:${rect}")
                        Log.d("",stripper.getTextForRegion("class1"))
                    },
                    onDragCancel = { Log.d("User Input", "Drag end @ ${offsetX}, ${offsetY}") }
                )
            }
    ) {
        renderer.renderPageToGraphics(pagePos,  paint,  drawContext.canvas.nativeCanvas, scale)
    }
}

fun TextPosition.containsPoint(x:Float, y:Float, margin:Float) : Boolean{
    var x1 = this.x
    var y1 = this.y - this.height
    var x2 = x1 + this.width
    var y2 = this.y  + margin
    return (x > x1 && x < x2 && y > y1 && y < y2)
}
fun TextPosition.toRectangle(): Rect{
    var x1 = this.x
    var y1 = this.y - this.height
    var x2 = x1 + this.width
    var y2 = this.y
    return Rect(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())
}
fun TextPosition.center(): Point {
    val c = this.toRectangle()
    return Point(c.centerX(), c.centerY())
}
fun Rect.extendBy(other:Rect){
    this.right = this.right.coerceAtLeast(other.right)
    // this.lowerLeftY = other.lowerLeftY
}


class MyTextSelection {
    companion object {
        const val error_margin = 5
    }

    internal class CustomComparator: Comparator<TextPosition> {
        override fun compare(x:  TextPosition, y: TextPosition): Int {
            if( abs(x.y - y.y) < error_margin ){
                return  x.x.compareTo(y.x)
            }
            return x.y.compareTo(y.y)
        }
    }

    private val textSelection :   MutableList<TextPosition> = mutableListOf()

    fun start(){
        textSelection.clear()
    }
    fun add(s : TextPosition){
        if( !textSelection.contains(s) )
            textSelection.add(s)
    }
    fun last(): TextPosition?{
        return if( textSelection.isEmpty() ) null else textSelection.last()
    }
    fun toRectangles(): MutableList<Rect>{
        textSelection.sortWith( CustomComparator() )
        val rs : MutableList<Rect> = mutableListOf()
        var cur : Rect? = null
        textSelection.forEachIndexed { index, textPosition ->
            var t = textPosition.toRectangle()
            if( cur == null ){
                cur = t
                rs.add(t)
            } else {
                if( abs(cur!!.top - t.top) < error_margin ){
                    cur!!.extendBy(t)
                } else {
                    cur = t
                    rs.add(t)
                }
            }
        }
        return rs
    }
}

class MyTextStripperByArea  : PDFTextStripperByArea() {
    //text position by pages
    private val textPositions : MutableList<MutableList<TextPosition>> = mutableListOf()
    private var curPagePos : Int = 0;
    private var forPosProcess : Boolean = false

    //    @Throws(IOException::class)
//    override fun getText(doc: PDDocument?): String?{
//        textPositions.clear()
//        curPagePos = 0
//        forPosProcess = true
//        val c : String? = super.getText(doc)
//        forPosProcess = false
//        return c
//    }
    fun prepareTextLoc(doc:PDDocument){
        textPositions.clear()
        curPagePos = 0
        synchronized(this) {
            forPosProcess = true
            getText(doc)
            forPosProcess = false
        }
    }

    fun getTextByLoc(page: Int, x:Float, y:Float) : TextPosition? {
        val ppos = textPositions[page]
        repeat(ppos.size){
            if( ppos[it].containsPoint(x,y, 2f) ) return ppos[it]
        }
        return null
    }

    @Throws(IOException::class)
    override fun extractRegions(page: PDPage?){
        super.extractRegions(page)
    }

    @Throws(IOException::class)
    override fun startPage(page: PDPage) {
        if(forPosProcess){
            if( curPagePos >= textPositions.size ){
                textPositions.add(mutableListOf())
            }
        }
        super.startPage(page)
    }
    @Throws(IOException::class)
    override fun endPage(page: PDPage) {
        super.endPage(page)
        if(forPosProcess) curPagePos++
    }

    @Throws(IOException::class)
    override fun writeLineSeparator() {
        super.writeLineSeparator()
    }

    override fun processTextPosition(text: TextPosition)
    {
        super.processTextPosition(text)
        if(forPosProcess) textPositions[curPagePos].add(text)
    }

    @Throws(IOException::class)
    override fun writeString(text: String, textPositions: List<TextPosition>) {
        super.writeString(text, textPositions)
    }

}