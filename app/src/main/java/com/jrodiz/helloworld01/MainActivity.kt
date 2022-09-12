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
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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

    class TranslateAdView(view: View) : CustomAdapter.ViewHolder(view)


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            AD_TRANSLATION -> {
                TranslateAdView(provider.getView(viewGroup.context, viewGroup))
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
        val webView: WebView = view.findViewById(R.id.webView)

        webView.webChromeClient = object : WebChromeClient() {

        }
        webView.webViewClient = object : WebViewClient() {

        }

        webView.settings.javaScriptEnabled = true
        webView.loadDataWithBaseURL(url, creative, "text/html", "UTF-8", null)
        //webView.loadUrl("https://medialab.la/")

        listener = object : Observer {
            val headerLocation = IntArray(2)

            override fun updatePosition() {
                tvHeader.getLocationOnScreen(headerLocation)
                val hy = headerLocation[1]
                //println("hy = $hy")

                val tvHeight = tvHeader.height.toFloat()
                webView.y = (if (hy < 0) {
                    abs(hy)
                } else {
                    (hy * -1f)
                }).toFloat() + tvHeight
            }
        }

        return view
    }

    companion object {
        val url = "http://ana-base.wis.pr/ana/index.html"
        val creative: String = """
             <script src="mraid.js"></script> <style id="stl">html,body,#ctr,#bgblr{margin:0;padding:0;width:100%;height:100%;overflow:hidden;border:none;position:absolute;top:0;left:0;}body{background:black;}</style> <div style="display:none" id="adm">ICAgIDxzY3JpcHQgdHlwZT0idGV4dC9qYXZhc2NyaXB0Ij4KICAgICAgcnViaWNvbl9jYiA9IE1hdGgucmFuZG9tKCk7IHJ1Ymljb25fcnVybCA9IGRvY3VtZW50LnJlZmVycmVyOyBpZih0b3AubG9jYXRpb249PWRvY3VtZW50LmxvY2F0aW9uKXtydWJpY29uX3J1cmwgPSBkb2N1bWVudC5sb2NhdGlvbjt9IHJ1Ymljb25fcnVybCA9IGVzY2FwZShydWJpY29uX3J1cmwpOwogICAgICB3aW5kb3cucnViaWNvbl9hZCA9ICIzNjQxODQ0IiArICIuIiArICJqcyI7CiAgICAgIHdpbmRvdy5ydWJpY29uX2NyZWF0aXZlID0gIjM4NDYxNTIiICsgIi4iICsgImpzIjsKICAgIDwvc2NyaXB0Pgo8ZGl2IGRhdGEtcnAtdHlwZT0idHJwLWRpc3BsYXktY3JlYXRpdmUiIGRhdGEtcnAtaW1wcmVzc2lvbi1pZD0iNTAyNDZkZGUtYWQ2ZC00MzU1LWJjY2QtNDYxOTMyNzBkZjZiIiBkYXRhLXJwLWFxaWQ9IjIxNDk6MTEyMDE5MDkiIGRhdGEtcnAtYWNjdC1pZD0iMTUyNzgiPgo8ZGl2IHN0eWxlPSJ3aWR0aDogMDsgaGVpZ2h0OiAwOyBvdmVyZmxvdzogaGlkZGVuOyI+PGltZyBib3JkZXI9IjAiIHdpZHRoPSIxIiBoZWlnaHQ9IjEiIHNyYz0iaHR0cDovL2JlYWNvbi1pYWQzLnJ1Ymljb25wcm9qZWN0LmNvbS9iZWFjb24vZC81MDI0NmRkZS1hZDZkLTQzNTUtYmNjZC00NjE5MzI3MGRmNmI/b289MCZhY2NvdW50SWQ9MTUyNzgmc2l0ZUlkPTE3MzI1MiZ6b25lSWQ9MTIxODI3OCZzaXplSWQ9NjcmZT02QTFFNDBFMzg0REE1NjNCNjM4NTc1NUI5QTlGQTFCQzI1NTVEQTU3NkE1MjkxODk3MzE2QjlCOEFDOTQ5OTY0QTY0RkRDODJDQUUwRDFGNjAwQTI4RDRBREFBNjlERTZEQkE4QkJDQTQ5MTAxMjMzQUU2NjQzMDVGMjU0NEIwNTY4QjgxMzdGRjFCM0YzRURGMTIxNEIxNkYwODRCMjUwOTJBNzFEQ0M4NUVFREIzOTk5QTQ0NDRFRkM2RjJBQTA4NjhDODIzQzY5RjFDRUEyRUJDMDM1MEExNDNFNTAzNTI0RDIzQUVENTg5QzZDNzc2NEQyN0E3ODA2MkIwNkYwRDM2OUNGMkZBQjFGOTY3QUFFQTEwNDlBNDMwNjExOUJBNTg5NzVCMjZDMjA1QjIyM0U5MjNEMUE0QjQ2NUQ5N0I2MEVCRjg1NERERUZFMDQwNDczM0ZCMDRGN0RBQkRCNjdCNUVGMDgyMzZGODRCMTg5REIzNjRDMjJBRTkyMDQ3RjdCODI0QzYzQTYxOEQ1QjY3ODFBODA5RjAxMkVDREUyMTVGODkxRUUwNDMzOUY1Rjk1QkUwNzZFNTQ5OURFQzIwQzNDNTg5RDNEQ0ExQzBFRDcxQjY3RkVDMjVEMDE0QTAzMDY4N0QxOTJFOTVEMDc1NjFDQjZCMDdCQjQ0NDE5OTk3RUU1NkQxRkNBQzdCNEYxODNDRUQ3RTJENTdBMjU4Q0ZENTE2QTI2NEY4QjY0M0ZGMjI1MjIzQjJBRkNFQTZCRTNCMUM4RjRGQkNFNzk2RTUyRjg0QjdBMzNCQTg4MEU2Q0ZDOTVBMkNFRkI2RjY2Q0VBNkMxOUYwRkUwNkNGQTBBMjgyQUJBQzIzREZDMjlDM0VEMDhFOTY1MEE3MDMyRkMxNTk1RjkyMjc5OUFBNEYxMUM5NjlDMjI4RURDNjhFNUQ0MjZCOEYzRjJDNDQ3QzU2NzhFMTQ2MkUxQjQ4N0U5QkQ2MUU0M0MzQUUwMzUwMEM1RkNEQzFEMDVBMEJEQjA4NTE0MTkwMTgwNTcyNkE1NjRDNDg3RTBBQzlDRDRENjhCNEE0RDA4RTZBNjkxNjMwMTQzQjE4OTUzNzNGNzFBODM3OEMxMzJGNkIxNTMzNTJGOTcwOEVFRjFGMTQ2MzY4MTg0OUM5RDRFRTBGRDUzMkU4MkVBN0E3MDIwNERFNDg4MzkzNTA2RDhBMzBCMzdCRkREQTAwNjA0NDdCQUVDNkNCRTJCRjdBMzkyNEE4QjcyRUMzQjM2MDQ1OTQ1OUZBRENCODlCQzZFNjc1RTBBNDA4NDRBRjJCMTI0NEM0NEMzQkU3NUE1OUJBQkUyMDJGMjk2Mjg1Nzk3MTRFRUIzQUZFQzlGREMzQkMzNjIzMDg1MjFCM0QxNjQwNERFODExQjBDNTcyMzQwQUJDQkZBNkI4NUY2OThDQjMwQUNBNTNBNEY5MzVEQTIyMUIzODgwQzVBMTM0RUExODBGQzdCNTFGRjIzRDdFNTc3RTdGQjEzRjE4RERDNjE1NjZEMjk0MjMzMEVDRkNCRDM1MEJFNEM3MzJENERFNUMyNkFEMkI4RTE3NDZGNjU4RjY4MEE2QjMyOURCQURFOUNCMzI0MTM5MzNBQTcwNzkwRjMwNTlBMTg0RkU3NEI4RjRGRTE4NEU5REJDRkU1ODJBMUUyOEI1N0YwNzkyREQ4Rjc1OENGOUFGRDYxMEQzRTZGN0NCODI2MTY4MEQ2MzUwOUZFRDM1RUU3NDIzQTk1M0UxNkVDQjUzQjlBRUQ0MEJDODc3QUE0MzlFMzc5Q0E3Njg4ODRDMTNERDE3RUQyOEREM0MyQUQ5M0QzQUNFQzgxMDgwMUM4NzlGODQ3MTlFRThFOUQ1MjgwNTkzNDIzQUYzQkQyNkFFQjY0RkU0QTRCOEIwQTM3NjA0RUFCQTUzOTZBRDNBNUMzMTA2OEM4NTU4QUQ0NzAxRUMwMzMxNTA5QTU5REQwRUZEM0EyNDcyQkE2MkU5MUVFQ0YwOEQ2QUQyREVEMTZGQjdDOEFCMzEyNTdGQzA2RUY5RkRDRDdBNDlCOTM0NjY1QThCNTk0QjVBRjU5NTY2Qzk0RkU3NDA5ODVDN0QwQkQ5OEE3QTQ3NjEyQkFEMkUxNzVDQjMxMjQxQkJDOEZBMTk2ODZBODZDQTI5NzlDQzQ3MUU0NDdFRDM5Q0NCQUMxQTBEMjEwMEJEOUUwNEVENDE4QkQyM0VENTBEOUVGNjU3N0U3MDVCQzVDNjZFQTQ0NkI0MzZDMjBCRDNFMDQzNEY1NDIwRjBFRDk0NkNGQ0IwQjM2ODMxQjI1NENDRUI0MDY5ODQ0MkQ4OUFDODY5MjEzOUFGN0Y3NEE3QTQ4ODE3MEE3NDVCMTZBNEMwOTk0MUREQTQwOEY4MzRCRjhCQ0QxNzdFOEZENEZGQzRDRjcwNDk3IiBhbHQ9IiIgLz48L2Rpdj4KCjxzY3JpcHQgdHlwZT0ndGV4dC9qYXZhc2NyaXB0Jz4oZnVuY3Rpb24oKXsNCnZhciB3ID0gd2luZG93Ow0KdmFyIHNjID0gdy5zY3JlZW47DQp2YXIgdXJsID0gJ2h0dHBzOi8vYWRzLnVzLmNyaXRlby5jb20vZGVsaXZlcnkvci9hanMucGhwP3o9ODFCNTAwOUNBMDZENzE3QyZ1PSU3Q3RiT2pxeWMlMkZ3eDJqMHJPU1JGeXZFWFNBYXZoYzdkRUNFUElseHRadTdZTSUzRCU3QyZjMT1aVnVrN3FoeHZLc0ljSV9yUHN6UkFrZ0dlQ2hYZlBFVkhZS1hneHcwb3AzQ2JKX0luOW9xOE5rd0FpMFlMeG9IRWhaNTQ4V3VEZUFGSk9wUEp4YWtuOHlRRlllMVQ1VkhLY3VRTnJwWHBjVDB5T1NJQ085UERfU1VHeE1FVkxJWHhKRjJyVFAtbklaWmFXQ2JvNjB0Z3c2aFBCWXVXa2lmMUdXcjNGUGl3NU9QeFhuWW9YWE5pQU0yUWxaT1NJMGhDR2dCbVdhV2VVYjl5NU1DdFlrLVd4NGNiU3lvM3hvc1pjYTF5YzFpbTdaOG1FOUFIYXdiTUxMQVF5a2tEQ0tXNjV0MU5SZkV4TUlPLTBTaEc1OWcwN0tpS1BNdVA4U3VtcWV1akRoUlN3cjBqVjBiUHJ0cE9XWkx1VGR5cjRLbWxRVUh1U21uZGVnZUJ0WXFOZkJWLWVHZGZxRzF0UlNLbG5qXzk5NWhLeEM2aHlLT2NrckFHOVkzTE1XWjA4VVhRYlROcnlOZTNWZFQ2RkhDWVY4aWx3NnJnUTM5RTBLRWZHVXREU3Jvb3lGVmFULWUzYlNzZ1U2dHhJNHhxektXTHQwVFhoXzdzUDlpOWpUSHh5YU8wMzVvTnEzMVgweF9rRldnMktkb1hzWlNTN0xJUldVRnRaRUExT04zLUZXYVRTVFZlM1I0Y1NZcnRTaFFtNktqbm5teW5pMGVpNXI1R2tBZGs3cVZVR2cyT1FlZFptYUxlRHdYdUlObGVwUFFUcl81SmhvSGtqWTNFYUQwSy1pbzdCWldYVFdBWDBWTEhlZ010d2swN2Y1NXBqdFFYVE9LQXpQb0h1bUZxYTU5WkhfcDF1YzdHNmRNQTg5VHoySXRITi1JT1MxNEpiMVRYTGhFQ04wSGlncVJ2S2VBSlRBOVRDU2R3bENQZWY1ZUhoUldpTGFoTUxBWUxUZ1ZkeG5UcnliYzFaWXBkY0FkNzJSb3VRUzR2T0xodlJpMFo4bHMxc0lUaWlINXdQaXgnICsgJyZ3dnc9JyArICh3LmlubmVyV2lkdGggfHwgKHNjICYmIChzYy53aWR0aCB8fCBzYy5hdmFpbFdpZHRoKSkgfHwgMCkgKyAnJnd2aD0nICsgKHcuaW5uZXJIZWlnaHQgfHwgKHNjICYmIChzYy5oZWlnaHQgfHwgc2MuYXZhaWxIZWlnaHQpKSB8fCAwKTsNCnZhciBzID0gIjwiKyJzY3JpcHQgdHlwZT1cJ3RleHQvamF2YXNjcmlwdFwnIHNyYz1cJyIgKyB1cmwgKyAiXCc+PCIrIi9zY3JpcHQ+IjsNCmRvY3VtZW50LndyaXRlKHMpO30pKCk7DQo8L3NjcmlwdD4KPC9kaXY+Cg</div> <div id="bgblr" style="z-index:0;background:center/cover;-webkit-filter:saturate(0.5) blur(15px);filter:saturate(0.5) blur(15px);"></div> <iframe id="ctr" frameborder="0" scrolling="no" width="100%" height="100%" style="z-index:1"></iframe> <script type="text/javascript">(function(){ function MRAID(){function k(b){try{if("object"===typeof b.mraid&&b.mraid.getState)var g=b.mraid}catch(l){}return b.parent!==b?g||k(b.parent):g}var h=window,e=k(h)||{ntfnd:!0},c="{offsetX:0,offsetY:0,x:0,y:0,width:"+h.innerWidth+",height:"+h.innerHeight+",useCustomClose:!1}",L="addEventListener",V="isViewable",d=this,f={removeEventListener:0,open:"window.top.open(a)",close:0,unload:0,useCustomClose:0,expand:0,playVideo:0,resize:0,storePicture:0,createCalendarEvent:0,supports:"{sms:!1,tel:!1,calendar:!1,storePicture:!1,inlineVideo:!1,orientation:!1,vpaid:!1,location:!1}", VERSIONS:{},STATES:{LOADING:"loading",DEFAULT:"default"},PLACEMENTS:{},ORIENTATIONS:{},FEATURES:{},EVENTS:{READY:"ready",ERROR:"error"},CLOSEPOSITIONS:{}};c={Version:'"2.0"',PlacementType:'"unknown"',OrientationProperties:"{allowOrientationChange:!1}",CurrentAppOrientation:'{orientation:""}',CurrentPosition:c,DefaultPosition:c,State:'"default"',ExpandProperties:c,MaxSize:c,ScreenSize:c,ResizeProperties:c,Location:"{}"};d._L=[];d[L]=function(b,g){"ready"===b||"viewableChange"===b?d._L.push({c:g,a:!0}): "stateChange"===b&&d._L.push({c:g,a:"default"});"function"===typeof e[L]&&e[L].apply(h,arguments)};for(var a in c)f["get"+a]=c[a],f["set"+a]="undefined";for(a in f)e[a]?d[a]=e[a]:(d[a]=f[a]?"object"===typeof f[a]?f[a]:new Function("return "+f[a]):function(){},e[a]=d[a]);d[V]=function(){return!!e.ntfnd||e[V]()===true||e[V]()==="true"};d.getState=function(){var s=e.getState();"object"===typeof s&&s.state&&(s=s.state);return e.ntfnd||d[V]()&&s==="loading"?"default":s};return e.ntfnd?(setTimeout(function(){d._L.forEach(function(b){b.c.call(window,b.a)})},1),d.ntfnd=!0,d):e}; function P(a){return decodeURIComponent(escape(atob(a)))}var D=document,PC=!0,CR=!0,Q="querySelector",E="replace",M="mraid",T="addEventListener",ctr=D[Q]("\x23ctr"),A=P(D[Q]("\x23adm").innerText),O=JSON.parse(P("eyJwdWJBcHBJRCI6MTg5NDA1NiwicHViSUQiOjEwMDc2ODcsImRzcElEIjo4ODcsImRzcE5hbWUiOiIiLCJpbXBJRCI6IjEiLCJjcmlkIjoiMzg0NjE1MiIsInB1Yk5hbWUiOiIiLCJpc01SQUlEIjpmYWxzZSwiaXNIVE1MNSI6ZmFsc2V9")),F=!1,CF=!1,L="<",R=[[P("PHNjcmlwdFtePl0qIHNyYz1bJyJdP21yYWlkLmpzWyciXT9bXj5dKj48L3NjcmlwdD4="),""],[P("W+KAmOKAmV0="),"'"]],B=["aHR0cHM6Ly9nb3QucHVibmF0aXZlLm5ldC9pbXByZXNzaW9uP2FpZD0xODk0MDU2JnQ9TXZDZ1ZFRzhablBwQm9FZDlxNXh2bkk5RFdnVWktQmpOMnVMWUt0THI4cldTbWlENHVXdHU3MHZKNDYxTmEwdzhRVmU2T0d0V0VER1VJZXFVLUliTUpMNEMzTVgtVGJOVHBkTzZBbXBRNDU1dGcwMThKQVh2WThiWDNabFNjWFhtdTIxTmlLVzhlalZQY2xISThvSGxSU0s4dlptWnMzQTViRWZVcEZURUxJdHk0UGxNU29vS1FIOHA3eW1leUtJY3NiME5KLVhjMGc5dDJXTXpTNEpvUkJCRmVoSTNiTUxhTS1UdElKb25JNzlWQ2tKYklzN2F2Y2FFSC11RWJiSXlTTHFOc0JUaDM5SDB3aUMwU2oxRW9TN1NFMFBUa0JXQjltbkRxT0tGSW1WUjBYTmFITjMwMjBKRzJsTHNPMXFrODBsbG9fN3dvVW12TmUxcnJjVGZESFJ6MGJGYnlwM0pEUV9mcUY2eF9lNXpxMWFfVXdwN3JSMTA0bkhQZ1h2b2dvZmNoZWYyYUZac1RDMjlKVnNjcWMtR2RBREFScmI0RVI0TUV2QkpfZDBDcktLbmVTc21XYktDVXdod2xnUU5UQ0t5bnE4VWRTNTVMWDI4NFE2OUdiZlJmMW5hbU1KdnZyU0N4VURDQ0U0SzU0TTVLLXV5NnVKRUNtRzhQNnZmc1hGTXQtdWswa2ZxenVQaHNua3dPeXMzSzdvZXFCV0JLMU9sX3BqWXIzU29HaWVuYVFNa052Q09wMkRCSmFHMVFfSFZTUWZlZFl6bGE5Q3ViNWlvLWVKaUw2cnoybWRadGZ2SjBBR2VHMnNQc052N2NMTDhBdUNncXVQMjJHRWdoX1YzaW5mX0x6dHVmRmR6QXYtUWhnOFptVmlRazh0VHI1UFFNeEpWZ01kRTVONkRtMUtfWDRmRXE1WGdRVHF3NmRtalVYR19kNnVLdGM0ZTMzU1hfSTIwM1dvVV9oMzNGWExwTEdaWW9JQnVnd2ExeUttTGltaTlEa2wxUWdUT3Bwd3dQQWhMTzE4ZHZJQ0oycGhlTkF0Sk5FcGNaYWZwcnc5Tm5fMUhiLTBvSjg2Qm80Zm93TUR1YlBtVGt1SnZmeXNQaTZOcGpGVE9xUk5iOENidUYzNWVJekJaSkpGQmFSaUFrakpoTGhIS3pnOWdqaE43amdQcnpYWEU3OXhtcDhWMkN2aGFFZTdhc3JmQ083S2FDZ3JIdnQ0WW9xdnBpcm5vZG5fSEpZUEJmMTV5MU9vQkxQekc1WVZlQ0lpdGt1RFRkQU1pQUx0ejh3a21ZLU9JdXIzeFVGMGtWVmMycXVnNDJjQXNqTEMtVWY1MnhTMDRSNGxGb1hnWmdPOFpWV2pDendGNDZVMEdzQ051V3hsS1h4TjlqNjRQMXJRcUdqMzl2aXN3eGh6TElBNm1PYkg1T2xzSktuM1p3U3BwWXFjcHB0Z3VxbEhEYlU4UHVZdU9McDlqVkRleUVreVZTcC11YkZyaUh5bDN2b2hGV255V092SFB6dGw3RnVXWDh4diZweD0xJmFwPSR7QVVDVElPTl9QUklDRX0","aHR0cHM6Ly9nby52cnZtLmNvbS90P3BjPSR7QVVDVElPTl9QUklDRX0mYWRuZXQ9NTgmYWg9YWRzZXJ2ZXItbWFzdGVyLTc1ZGQ5NjY2OGItcmhnMnQmYj1tZWRpYWxhYiZjb3VudHJ5Q29kZT1VU0EmZT1BZEltcEludGVybmFsJnBhaWQ9MTU1NDcmcG9pZD0zOCZyPTAwMDAwMjMwNjMxYmMzYTZkY2E2NDMwMWU4OThmMWQ0JnJwPWV5SmpjbWxrSWpvaU16ZzBOakUxTWlJc0ltRjFZMTlpYVdSZlkzQnRYM1psSWpvaU5WODViMVpwV25Sa1dVbGhibWQ2ZGpOaWFVbEhPV0pHVlV0WWEwWjJNR28yWVhveWFWRWlMQ0poZFdOZmNHRnBaRjlqY0cwaU9pSXdMakkwT0RnNE9ERTBPREUwT0RFME9ERTBJaXdpY0hKblgyUnRaRjl3WVdsa1gyTndiVjkyWlNJNklra3RUbmczVmxaWlpEVlpkUzFNYlMxb056RnlVVTVaVFY5UFR6UlBRVGxGUVZKSWQxUjNJaXdpY0d4MFgyWmxaVjl5ZEY5MlpTSTZJbTVxT0ZCWldWQlNWMlJ4Um10MGVqZEdXazV3UzBabFdWUjNZME40UmpkMVUxTnhRV3AzSWl3aVlXUnVaWFJmYVdRaU9pSTFPQ0lzSW1sdWRsOXpkWEJmYm1GdFpTSTZJblp5ZG1WNFkyZ2lMQ0oyYkhOZlkyOTFiblJ5ZVY5amIyUmxJam9pZFhOaEluMCUzRCZ1aT02NTY0NjFmYy1iYzlmLTRkYTktODE0Yi02MDNmMjQ2ZmQyOWMmdWlzPUc"],CB=["aHR0cHM6Ly9nb3QucHVibmF0aXZlLm5ldC9jbGljay9zMnM/YWlkPTE4OTQwNTYmdD1WVV9xM3RvMWRoRnpQUXFvbnNwek9Odk5ZWlM0ZFItQlJHT2RTSURfcF9nY0dTNkhEenZzUHNzekktUzVXRFNFeVZabHFEV0lmTnZxWnlTaTJ5b2N2ZWpyNlBXQjItb0ZCUFNJa3piOGF1em1ROU9DWWxrazNUd2hTRkN3SWp5aXZQSHg4ZWNrSERLUzhfUFhOQVdraWVIeFViMnd1bHNTRDJ3RE9MWUFYdHdLclZBXzRTM3BMSHRqaU1PRE9Hb2p6NmRoXzVMS3lFWHk1bi0xRVZwMHJONDE2UTNZWUN0VTk5LUxVOG4yTkFJUzdocnRZOXZ0dFgtZktqLVFjSVdvYkpTczlweTY2QWRFQ3d3VW9ZSVNDc1hFaU5QLUI2NUEyM0VNTDk1OHplSHZic2tadFpZdThVMzNscXFhNEdwd19MdFpaV3g0cVpab01DTllMTTBxd2ZqdC1sUllub2ZmRFRET0YxOF9TOU5FZ2xxdmlEVEpHZHNLa3JuUWdtM29BVzBvLV9YU2gwZ3lzZ3RNSUdOOE5rbno1Yl9MalpwZ3UtMGpyMmViM0loZjk2S0tacVQ0aU1IcmFtRlRFOXlzbmtVVkUwSFNaWm95dXZTd1cxNVhfZTVMa3QycmVvbEh0Y2tjZ0k3X0p5MndkaS1NSTU0WDlHR2hCNlN0bEVpYk9pVnFIVUtJbmxidWp4WkRGclJ6N3JIR2VmNktMUDFOaGprRVFrM0xRY29tQ0xhQ1RZcXRLTm5BYk42SDY1Vy1HMVF0X25VRXdBWHRIRzl0NDM2UWVxWGtHWEY1NDNMckFyblFBeGxIY0JUV0dFQXJRZkpwbFJ6ZU8wRXdleTdtQTJHOHFyTXNkQVZ3cThEV3VKMWg0T08yS3dYbW83YkNGODdhLXZtWjVOU0JhcUNpV0t0MVVwMjVpcGVTdHQ1aFlqYkRMR0xGNEdQczhzaHEtT0tWanJud3Nkb0pMYWV1N2RvUzVfZzNYQ3d3Q3NGd280TFkxZGhnUW1CRjNrb3ZDbG45Ym5TRHpfT2Y1UQ","aHR0cHM6Ly9nby52cnZtLmNvbS90P3BjPSR7QVVDVElPTl9QUklDRX0mYWRuZXQ9NTgmYWg9YWRzZXJ2ZXItbWFzdGVyLTc1ZGQ5NjY2OGItcmhnMnQmYj1tZWRpYWxhYiZjb3VudHJ5Q29kZT1VU0EmZT1jbGljayZwYWlkPTE1NTQ3JnBvaWQ9Mzgmcj0wMDAwMDIzMDYzMWJjM2E2ZGNhNjQzMDFlODk4ZjFkNCZycD1leUpqY21sa0lqb2lNemcwTmpFMU1pSXNJbUYxWTE5aWFXUmZZM0J0WDNabElqb2lhbk52ZG1kSlNWTkJlbXhxZGtSNFozbzBRMjF3TmxwdFpVMTNVMUp0TUVoNldXUjBXVUVpTENKaGRXTmZjR0ZwWkY5amNHMGlPaUl3TGpJME9EZzRPREUwT0RFME9ERTBPREUwSWl3aWNISm5YMlJ0WkY5d1lXbGtYMk53YlY5MlpTSTZJbUptWmtOUE1XMUdaelJaYUU1VlNFbzFjVkYxV25WR1ZqWmhNa3BvTlVFelgzWktWMjUzSWl3aWNHeDBYMlpsWlY5eWRGOTJaU0k2SWpWcGFqaDBOMEo2TVhOU1RGZG1NV0pIV25CaVFsOWtiVWx6VEVwS09VNWFPVWRmVDFObklpd2lZV1J1WlhSZmFXUWlPaUkxT0NJc0ltbHVkbDl6ZFhCZmJtRnRaU0k2SW5aeWRtVjRZMmdpTENKMmJITmZZMjkxYm5SeWVWOWpiMlJsSWpvaWRYTmhJbjAlM0QmdWk9NjU2NDYxZmMtYmM5Zi00ZGE5LTgxNGItNjAzZjI0NmZkMjljJnVpcz1H"],i;for(i in B)B[i]=P(B[i])[E](P("JHtBVUNUSU9OX1BSSUNFfQ=="),"0.27");for(i in CB)CB[i]=P(CB[i]); function RN(){if(!ctr||!A)return;window[M]=new MRAID;for(r in R)A=A[E](new RegExp(R[r][0],"g"),R[r][1]);A=L+"style>html,body{margin:0;padding:0;width:100%;height:100%;overflow:hidden;border:none;}"+L+"/style>"+L+'script type="text/javascript">'+MRAID.toString()+"window.mraid=new MRAID();"+L+"/script>"+A;if(O.aqh&&O.aqf){var a=function(b){return(b||"")[E](P("JWN1c3RfaW1wIQ=="),O.impID)[E](P("JURFTUFORF9JRCE="),O.dspID)[E](P("JWRzcE5hbWUh"), O.dspName)[E](P("JURFTUFORF9DUkVBVElWRV9JRCE="),O.crid)[E](P("JVBVQkxJU0hFUl9JRCE="),O.pubID)[E](P("JXB1Yk5hbWUh"),O.pubName)[E](P("JSVXSURUSCUl"),(ctr.contentWindow||window).innerWidth)[E](P("JSVIRUlHSFQlJQ=="),(ctr.contentWindow||window).innerHeight)};O.aqh=a(O.aqh);O.aqf=a(O.aqf);A=O.aqh+A+O.aqf}A=L+"!DOCTYPE html>\n"+A;window[M][T]("ready",MR);window[M][T]("viewableChange",MV);CR&&FA();MV();PC&&setTimeout(EX,1)}function FA(){if(!F){for(var i in B)(new Image).src=B[i];F=!0}} function MR(){window[M][T]("viewableChange",MV);MV()}function MV(){"true"===window[M].isViewable()||!0===window[M].isViewable()?(CR||FA(),PC||setTimeout(EX,1),setInterval(FS,100)):setTimeout(MV,100)}; function FS(){var a=ctr.contentWindow;if(a){var b=window.innerWidth,h=window.innerHeight,g=ctr.style,k=!1,l=10,m=10,e=a.document.querySelectorAll("body,div,span,p,section,article,a,img,canvas,video,iframe");for(a=0;a<e.length;a++){var c=e[a].offsetWidth;var d=e[a].offsetHeight;if((c===320&&d===480||c===480&&d===320||c===300&&d===250&&b!==300&&h!==250||(c===300||c===320)&&d===50&&h===50&&c!==b)&&!k){var f=d*b/c>h?h/d:b/c;if(f!==0){k=!0;g.width=c+"px";g.height=d+"px";g.transform="scale("+f+","+f+")";d*b/c>h? (g.top=(f-1)*d/2+"px",g.left=(b-c)/2+"px"):(g.top=(h-d)/2+"px",g.left=(f-1)*c/2+"px")}}f=(e[a].style.backgroundImage||"").match(P("XnVybFwoIihodHRwLispIlwpJA=="));if(k&&("IMG"===e[a].nodeName&&e[a].src||f&&f[1])&&c>l&&d>m){l=c;m=d;var n=e[a].src?e[a].src:f[1]}}n&&(document[Q]("\x23bgblr").style.backgroundImage="url("+n+")")}} function EX(){if(A){try{var a=ctr.contentWindow,b=a.document;b.open();a[T]("click",function(e){if(!CF){for(var i in CB)(new Image).src=CB[i];CF=true}},true);a[T]("load",FS);b.write(A);b.close()}catch(h){}A=""}}RN();})();</script>
         """.trimIndent()
    }
}