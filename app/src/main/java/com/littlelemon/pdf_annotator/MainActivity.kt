package com.littlelemon.pdf_annotator

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.preference.PreferenceManager
import com.littlelemon.pdf_annotator.data.AnnotationInPage
import com.littlelemon.pdf_annotator.data.AnnotationItem
import com.littlelemon.pdf_annotator.data.AnnotationType
import com.littlelemon.pdf_annotator.data.annotationData
import com.littlelemon.pdf_annotator.ui.theme.PdfAnnotatorTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.PDFRenderer
import java.util.UUID
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext);

        val assetManager =  applicationContext.assets
        val document = PDDocument.load(assetManager.open("pdf/sample3.pdf"))
        val data = annotationData
        val dpi = applicationContext.resources.displayMetrics.densityDpi
        var scale : Float = (dpi / 72F).roundToInt().toFloat()
        var myTextSelection = MyTextSelection()
        val renderer = PDFRenderer(document)
        val stripper = MyTextStripperByArea()
        stripper.sortByPosition = true
        //it looks a bug from original api, we need to invoke getText(doc) first.
        stripper.prepareTextLoc(document)
        var pagePos = 0


        setContent {
            PdfAnnotatorTheme {
                // A surface container using the 'background' color from the theme
                Box(modifier = Modifier.fillMaxSize()){
                    var notePopupState: PopupState = remember {
                        PopupState(false)
                    }
                    var optionPopupState = remember { mutableStateOf(false) }

                    var popupTextValue = remember { mutableStateOf("")  }

                    val openNoteDialog = remember { mutableStateOf(false) }
                    val noteInEdit = remember { mutableStateOf("") }

                    val openColorDialog = remember { mutableStateOf(false) }
                    val colorInSelection = remember { mutableStateOf(Color.Red.copy(alpha=1f)) }


                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PdfView( document , pagePos,  renderer,
                            stripper, myTextSelection
                        ) {
                            optionPopupState.value = true
                        }
                    }
                    AnnotationView(
                        count = data.pages[pagePos].items.size,
                       // scale = scale,
                        item = { index ->
                            val datum = data.pages[pagePos].items[index]
                            when( datum.type ){
                                AnnotationType.Highlight -> Highlight(datum, scale,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .annotLoc(
                                            region = datum.region(),
                                            scale = scale
                                        ))
                                AnnotationType.HasNote -> HasNote(datum, scale,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .annotLoc(
                                            region = datum.region(),
                                            scale = scale
                                        ),
                                    onClick = {
                                        popupTextValue.value = datum.text
                                        notePopupState.isVisible = true  } )
                                AnnotationType.StrikeOut -> StrikeOut(datum, scale,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .annotLoc(
                                            region = datum.region(),
                                            scale = scale
                                        ))
                                AnnotationType.Underline -> Underline(datum, scale,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .annotLoc(
                                            region = datum.region(),
                                            scale = scale
                                        ))
                                AnnotationType.UnderlineDouble -> UnderlineDouble(datum, scale,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .annotLoc(
                                            region = datum.region(),
                                            scale = scale
                                        ))
                                AnnotationType.UnderlineDash -> UnderlineDash(datum,scale,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .annotLoc(
                                            region = datum.region(),
                                            scale = scale
                                        ))
                                AnnotationType.Squiggly -> Squiggly(datum,scale,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .annotLoc(
                                            region = datum.region(),
                                            scale = scale
                                        ))
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize(),
                    )
                    SmallFloatingActionButton(
                        onClick = { openColorDialog.value = !openColorDialog.value  },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(all = 16.dp)
                            .align(alignment = Alignment.BottomEnd),
                    ) {
                        Icon(Icons.Filled.Add, "show color picker.")
                    }
                    when {
                        openColorDialog.value ->{
                             ColorSelectionDialog(
                                this@MainActivity,
                                openColorDialog ,
                                colorInSelection ,
                            )
                        }

                        openNoteDialog.value -> {
                             AddNoteDialog(
                                 noteInEdit  ,
                                 openNoteDialog ,
                                 myTextSelection ,
                                 data.pages[pagePos],
                                 colorInSelection.value,
                            )
                        }


                        notePopupState.isVisible -> {
                            NoteAlertDialog(
                                onDismissRequest = { notePopupState.isVisible= false },
                                onConfirmation = {
                                    notePopupState.isVisible = false
                                    println("Confirmation registered") // Add logic here to handle confirmation.
                                },
                                dialogTitle = "My Note",
                                dialogText =  popupTextValue.value,
                              //  icon = Icons.Default.Info
                            )
                        }
                        optionPopupState.value -> {
                            AnnotationTypeSelectionDialog(
                                optionPopupState  ,
                            openNoteDialog ,
                            myTextSelection ,
                                data.pages[pagePos],
                                    colorInSelection.value
                            )
                        }
                    }
                }
            }
        }
    }
}

fun annotationCreated(myTextSelection: MyTextSelection, inPage: AnnotationInPage,
                      type:AnnotationType, color: Color ): AnnotationItem{
    var rs =  myTextSelection.toRectangles()
    var color2 =  if( type == AnnotationType.Highlight ) color.copy(alpha = 0.4f) else color
    var annotation = AnnotationItem( UUID.randomUUID(),
        type,
        color2,
        "",
        rs )
    inPage.items.add(annotation)
    return annotation
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSelectionDialog(
    activity : Activity,
    openColorDialog: MutableState<Boolean>,
    colorInSelection: MutableState<Color>,
) {
    Popup(
        popupPositionProvider =
        WindowBottomOffsetPositionProvider(),
        onDismissRequest = { openColorDialog.value = false },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly ,
            modifier = Modifier.background(Color.White)
        ) {

            arrayOf(Color.Red, Color.White, Color.Yellow, Color.Blue, Color.Green,
                Color.Gray, Color.Magenta, Color.Cyan) .forEach {  color ->
                TextButton(
                    modifier = Modifier.width(45.dp).height(45.dp)
                        .background(color) ,
                    onClick = {
                        colorInSelection.value = color
                        val sharedPref =  activity.getPreferences(Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putInt( "annotationColor" , color.toArgb())
                            apply()
                        }
                        openColorDialog.value = false
                    }) {
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    noteInEdit : MutableState<String>,
    openNoteDialog: MutableState<Boolean>,
    myTextSelection: MyTextSelection,
    annotationInPage: AnnotationInPage,
    color: Color,
) {
    AlertDialog(
        title = {
            Text(text = "Add Note:")
        },
        text = {
            TextField(
                value = noteInEdit.value,
                onValueChange = { noteInEdit.value = it },
                singleLine = true
            )
        },
        onDismissRequest = {
            openNoteDialog.value = false
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val item = annotationCreated(myTextSelection, annotationInPage,
                        AnnotationType.HasNote, color)
                    item.text = noteInEdit.value
                    openNoteDialog.value = false
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    openNoteDialog.value = false
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}


@Composable
fun AnnotationTypeSelectionDialog(
     optionPopupState : MutableState<Boolean>,
     openNoteDialog: MutableState<Boolean>,
     myTextSelection: MyTextSelection,
     annotationInPage: AnnotationInPage,
    color: Color,
) {
   // var rs =  myTextSelection.last()!!.center()
    Popup(
        popupPositionProvider =
        WindowCenterOffsetPositionProvider( ),
        onDismissRequest = { optionPopupState.value = false },
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.background(Color.White)
        ){
            AnnotationType.values().forEach { anonType ->
                TextButton(onClick =  {
                    if( anonType == AnnotationType.HasNote){
                        optionPopupState.value = false
                        openNoteDialog.value = true
                    } else {
                        annotationCreated(myTextSelection, annotationInPage,
                            anonType, color)
                        optionPopupState.value = false
                    }
                }) {
                    Text( anonType.name )
                }
            }
        }
    }
}

@Composable
fun NoteAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
   // icon: ImageVector,
) {
    AlertDialog(
        //icon = {
         //   Icon(icon, contentDescription = "Example Icon")
        //},
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
    )
}
class WindowOffsetPositionProvider(
    private val x : Int = 0,
    private val y : Int = 0
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        return IntOffset(
              x,
              y
        )
    }
}
class WindowCenterOffsetPositionProvider(
    private val x : Int = 0,
    private val y : Int = 0
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        return IntOffset(
            (windowSize.width - popupContentSize.width) /2  + x,
            (windowSize.height - popupContentSize.height) / 2 + y
        )
    }
}
class WindowBottomOffsetPositionProvider(
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        return IntOffset(
            (windowSize.width - popupContentSize.width)   ,
            (windowSize.height - popupContentSize.height)
        )
    }
}

