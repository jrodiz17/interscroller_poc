package com.jrodiz.helloworld01

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.random.Random


private const val TAG = "rodiz_"
private const val AD_BACKGROUND = -1
private const val AD_TRANSLATION = 0
private const val DUMMY = 1

class MainActivity : AppCompatActivity() {

    private val customScope: CoroutineScope
        get() = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val recyclerView: RecyclerView? =
            findViewById<RecyclerViewContainer?>(R.id.recycler_view)?.innerRv

        recyclerView?.setOnScrollChangeListener { _, _, _, _, _ ->
            startTracking()
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
            ), scrollEvent
        )

    }

    override fun onStop() {
        super.onStop()
        stopTracking()
    }

    private val scrollEvent = ScrollEvent()

    private fun startTracking() {
        if (job != null) {
            return
        }
        job = customScope.launch {
            while (isActive) {
                delay(10)
                scrollEvent.onScrolledChanged()
            }
        }
    }

    private fun stopTracking() {
        job?.cancel()
        job = null
        customScope.cancel()
    }

}


class ScrollEvent {
    interface Observer {
        fun onScrolledChanged()
    }

    var listener: Observer? = null

    fun onScrolledChanged() {
        listener?.onScrolledChanged()
    }
}

class CustomAdapter(
    private val dataSet: Array<Int>,
    private val scrollEvent: ScrollEvent
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

    class TransparentBackgroundAd(view: View) : ViewHolder(view) {
        private val webView: ImageView = view.findViewById(R.id.webView)
        private val background: ImageView = view.findViewById(R.id.backgroundView)

        init {
            webView.alpha = 0f
            background.visibility = View.INVISIBLE
        }
    }

    class TranslateAdView(view: View, scrollEvent: ScrollEvent) : ViewHolder(view) {
        private val tvHeader: TextView = view.findViewById(R.id.tvHeader)
        private val webView: ImageView = view.findViewById(R.id.webView)

        init {
            scrollEvent.listener = object : ScrollEvent.Observer {
                val headerLocation = IntArray(2)
                //val webViewLocation = IntArray(2)

                override fun onScrolledChanged() {
                    tvHeader.getLocationOnScreen(headerLocation)
                    //webView.getLocationOnScreen(webViewLocation)
                    //val hx = headerLocation[0].toFloat()
                    val hy = headerLocation[1].toFloat()
                    //val wx = webViewLocation[0].toFloat()
                    //val wy = webViewLocation[1].toFloat()
                    webView.post {

                        webView.animate()
                            .translationY(if (hy > 0) 0f else abs(hy))
                            .setDuration(0)
                            .start()
                    }
                    //Log.d(TAG, "[$hx, $hy]")
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            AD_TRANSLATION -> {
                TranslateAdView(
                    inflater.inflate(R.layout.ad_row_item, viewGroup, false),
                    scrollEvent = scrollEvent
                )
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