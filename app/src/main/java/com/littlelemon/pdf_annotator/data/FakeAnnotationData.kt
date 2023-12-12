package com.littlelemon.pdf_annotator.data

import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import java.util.UUID


// In the real world, you should get this data from a backend.
val annotationData = AnnotationInDoc(4f,
    mutableListOf(
        AnnotationInPage(0,
          mutableListOf(
              AnnotationItem(
                  uuid = UUID.randomUUID(),
                  type = AnnotationType.Highlight,
                  color = Color.Red.copy(alpha = 0.3f),
                  regions = mutableListOf(
                      Rect(80,70,400,100 ))
              ),
              AnnotationItem(
                  uuid = UUID.randomUUID(),
                  type = AnnotationType.Underline,
                  regions = mutableListOf(Rect(100,100,300,130) )
              ),
              AnnotationItem(
                  uuid = UUID.randomUUID(),
                  type = AnnotationType.StrikeOut,
                  regions = mutableListOf(Rect(200,130,300,160))
              ),
              AnnotationItem(
                  uuid = UUID.randomUUID(),
                  type = AnnotationType.UnderlineDouble,
                  regions = mutableListOf(Rect(100,160,300,180) )
              ),
              AnnotationItem(
                  uuid = UUID.randomUUID(),
                  type = AnnotationType.UnderlineDash,
                  regions = mutableListOf(Rect(100,180,300,210) )
              ),
              AnnotationItem(
                  uuid = UUID.randomUUID(),
                  type = AnnotationType.HasNote,
                  text = "this is sa at tests for all \n sfasdf weh atat ",
                  regions = mutableListOf(Rect(100,200,300,230) )
              ),
              AnnotationItem(
                  uuid = UUID.randomUUID(),
                  type = AnnotationType.Squiggly,
                  regions = mutableListOf(Rect(80,230,300,260) )
              )
    )))
)
