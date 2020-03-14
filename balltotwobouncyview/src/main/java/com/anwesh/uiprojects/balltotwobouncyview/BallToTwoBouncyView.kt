package com.anwesh.uiprojects.balltotwobouncyview

/**
 * Created by anweshmishra on 14/03/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodeColors : Array<String> = arrayOf("#673AB7", "#F44336", "#009688", "#8BC34A", "#FF9800")
val circles : Int = 2
val scGap : Float = 0.02f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 30
val sizeFactor : Float = 4f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
