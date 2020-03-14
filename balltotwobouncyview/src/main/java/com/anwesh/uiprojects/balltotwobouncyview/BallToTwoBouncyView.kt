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
val yFactor : Float = 4f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBouncyBallToTwo(scale : Float, size : Float, h : Float, paint : Paint) {
    val y : Float = h / yFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, 2)
    val sf2 : Float = sf.divideScale(1, 2)
    save()
    translate(0f, -(h / 2 + size) * (1 - sf1))
    for (j in 0..(circles - 1)) {
        val sj : Float = 1f - 2 * j
        val sfj : Float = sf2.divideScale(j, circles)
        drawCircle(0f, y * sj * sfj, size, paint)
    }
    restore()
}

fun Canvas.drawBBTNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodeColors.size)
    val size : Float = gap / sizeFactor
    paint.color = Color.parseColor(nodeColors[i])
    save()
    translate(gap * (i + 1), h / 2)
    drawBouncyBallToTwo(scale, size, h, paint)
    restore()
}

class BallToTwoBouncyView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BBTNode(var i : Int, val state : State = State()) {

        private var next : BBTNode? = null
        private var prev : BBTNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodeColors.size - 1) {
                next = BBTNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBBTNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BBTNode {
            var curr : BBTNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BouncyBallToTwo(var i : Int) {

        private var curr : BBTNode = BBTNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BallToTwoBouncyView) {

        private val bbt : BouncyBallToTwo = BouncyBallToTwo(0)
        private val animator : Animator = Animator(view)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bbt.draw(canvas, paint)
            animator.animate {
                bbt.update {
                    animator.start()
                }
            }
        }

        fun handleTap() {
            bbt.startUpdating {
                animator.start()
            }
        }
    }
}