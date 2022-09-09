package com.jrodiz.helloworld01

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat.setY
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.random.Random


private const val TAG = "rodiz_"
private const val AD_BACKGROUND = -1
private const val AD_TRANSLATION = 0
private const val DUMMY = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val recyclerView: RecyclerView? =
            findViewById<RecyclerViewContainer?>(R.id.recycler_view)?.innerRv

        val provider = MLInterScrollerViewProvider().also {
            it.initWith(recyclerView!!)
        }
        recyclerView?.adapter = CustomAdapter(
            arrayOf(
                DUMMY,
                DUMMY,
                AD_BACKGROUND,
                DUMMY,
                DUMMY,
                AD_TRANSLATION,
                DUMMY,
                DUMMY,
                DUMMY,
                DUMMY
            ), provider
        )

    }

}

class CustomAdapter(
    private val dataSet: Array<Int>,
    private val provider: MLInterScrollerViewProvider
) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class SimpleViewHolder(view: View) : ViewHolder(view) {
        private val container: View = view.findViewById(R.id.container)
        private val colors = listOf(
            Color.BLUE,
            Color.DKGRAY,
            Color.GRAY,
            Color.LTGRAY,
            Color.RED,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN
        )

        private val indexColor = Random.nextInt(0, colors.size)

        fun onBind() {
            container.setBackgroundColor(colors[indexColor])
        }
    }

    class TransparentBackgroundAd(view: View) : CustomAdapter.ViewHolder(view) {
        private val webView: ImageView = view.findViewById(R.id.webView)
        private val background: ImageView = view.findViewById(R.id.backgroundView)

        init {
            webView.alpha = 0f
            background.visibility = View.INVISIBLE
        }
    }

    class TranslateAdView(view: View) : CustomAdapter.ViewHolder(view)


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            AD_TRANSLATION -> {
                TranslateAdView(provider.getView(viewGroup.context, viewGroup))
            }
            AD_BACKGROUND -> {
                TransparentBackgroundAd(inflater.inflate(R.layout.ad_row_item, viewGroup, false))
            }
            else -> {
                SimpleViewHolder(
                    inflater.inflate(R.layout.text_row_item, viewGroup, false)
                )
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (viewHolder is SimpleViewHolder) {
            viewHolder.onBind()
        }
    }

    override fun getItemCount() = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return dataSet[position]
    }
}

class MLInterScrollerViewProvider {

    var listener: Observer? = null

    interface Observer {
        fun updatePosition()
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        // This will get fired a LOT while the UI is changing. Do not run the visibility check
        // with each execution of this callback. Instead, wait for the UI to settle down then
        // call it once.  Hence the short delay.
        Log.v(TAG, "scrollListener")
        listener?.updatePosition()
    }


    fun initWith(recyclerView: RecyclerView) {
        recyclerView.viewTreeObserver?.addOnScrollChangedListener(scrollListener)
    }

    fun getView(
        ctx: Context,
        viewGroup: ViewGroup,
        attachToRoot: Boolean = false
    ): View {
        val view: View =
            LayoutInflater.from(ctx).inflate(R.layout.ad_row_item, viewGroup, attachToRoot)
        val tvHeader: TextView = view.findViewById(R.id.tvHeader)
        val webView: ImageView = view.findViewById(R.id.webView)

        listener = object : Observer {
            val headerLocation = IntArray(2)

            override fun updatePosition() {
                tvHeader.getLocationOnScreen(headerLocation)
                val hy = headerLocation[1]
                println("hy = $hy")

                val useOption1 = false
                if (useOption1) {
                    val params = (webView.layoutParams as ConstraintLayout.LayoutParams).apply {
                        if (hy < 0) {
                            topMargin = abs(hy)
                            bottomMargin = hy
                        }
                    }

                    webView.layoutParams = params
                } else {
                    val tvHeight = tvHeader.height.toFloat()
                    if (hy < 0) {
                        webView.y = abs(hy).toFloat() + tvHeight
                    } else if (webView.y != tvHeight) {
                        webView.y = tvHeight
                    }
                }

            }
        }

        return view
    }
}