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
                println("hy = '$'hy")

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

    companion object {
         val url = "http://ana-base.wis.pr/ana/index.html"
         val creative : String = """
             <!DOCTYPE html><head></head><body><style>body,html{margin:0;padding:0;}</style><script>var inFiF=inDapIF=true;</script><script>
	(function() {
	    var bridge = window.cmpBridge = {};
		var body = document.body;
		var iframe = document.createElement('iframe');
		var commandSources = {}
		var commandCallbacks = {}

		iframe.style.cssText = 'display:none';
		iframe.name = '__cmpLocator';
		body.appendChild(iframe);

		window.__cmp = (command, param, callback) => {
			var callId = Math.random() + "";
			console.log("cmp command from __cmp - " + command + ", param: " + param + ", id: " + callId)
			commandCallbacks[callId] = callback;
			bridge.executeNativeCall(command, param, callId);
		}

		window.addEventListener("message", function(event) {
			var json = typeof event.data === "string" ? JSON.parse(event.data) : event.data;
			if(json.__cmpCall) {
				var i = json.__cmpCall;
				commandSources[i.callId] = event.source
				console.log("cmp command from handler: " + i.command + " params: " + i.parameter + " id: " + i.callId)
				bridge.executeNativeCall(i.command, i.parameter, i.callId);
			}
		}, false);

	    bridge.executeNativeCall = function(command, param, callId) {
	        var call = "mlcmp://" + command;
	        if (param != null) {
                call += "?param=" + encodeURIComponent(param);
	        }
	        if (callId != null) {
	        	if (param == null) {
		        	call += "?";
		        } else {
		        	call += "&";
		        }
	        	call += "callId=" + encodeURIComponent(callId);
	        }

	        var iframe = document.createElement("IFRAME");
	        iframe.setAttribute("src", call);
	        document.documentElement.appendChild(iframe);
	        iframe.parentNode.removeChild(iframe);
	        iframe = null;
	    };

	    bridge.setReturnData = function(result, success, callId) {
	    	console.log("setReturnData - callId: " + callId + " success: " + success + " res: " + JSON.stringify(result));
	    	if (commandSources[callId] != null) {
		    	console.log("setReturnData - posting to source")
		    	commandSources[callId].postMessage(JSON.stringify({
							__cmpReturn: {
								returnValue: result,
								success:  success,
								callId: callId
							}
						}), "*");	
		    	delete commandSources[callId]
	    	} else if (commandCallbacks[callId] != null) {
	    		console.log("setReturnData - calling callback")
	    		commandCallbacks[callId](result, success)
	    		delete commandCallbacks[callId]
	    	} else {
	    		console.log("setReturnData - did not find any callback or source")
	    	}
	    }
	})();
</script>
<script type="text/javascript" src="https://dfa0j9td3zcbm.cloudfront.net/omsdk-v1.js"></script><script type="text/javascript">
;(function(omidGlobal, factory, exports) {
  // CommonJS support
  if (typeof exports === 'object' && typeof exports.nodeName !== 'string') {
    factory(omidGlobal, exports);

  // If neither AMD nor CommonJS are used, export to a versioned name in the
  // global context.
  } else {
    var exports = {};
    var versions = ['1.3.1-iab2040'];
    var additionalVersionString = '';
    if (!!additionalVersionString) {
       versions.push(additionalVersionString);
    }

    factory(omidGlobal, exports);

    function deepFreeze(object) {
      for (var key in object) {
        if (object.hasOwnProperty(key)) {
          object[key] = deepFreeze(object[key]);
        }
      }
      return Object.freeze(object);
    }

    // Inject and freeze the exported components of omid.
    for (var key in exports) {
      if (exports.hasOwnProperty(key)) {
        if (Object.getOwnPropertyDescriptor(omidGlobal, key) == null) {
          // Define the top level property in the global scope
          Object.defineProperty(omidGlobal, key, {
            value: {},
          });
        }
        versions.forEach(function(version) {
          if (Object.getOwnPropertyDescriptor(omidGlobal[key], version) == null) {
            var frozenObject = deepFreeze(exports[key]);
            // Define the object exports keyed-off versions
            Object.defineProperty(omidGlobal[key], version, {
              get: function () {
                return frozenObject;
              },
              enumerable: true,
            });
          }
        });
      }
    }
  }
}(typeof global === 'undefined' ? this : global, function(omidGlobal, omidExports) {
  'use strict';
var '$'jscomp = '$'jscomp || {};
'$'jscomp.scope = {};
'$'jscomp.arrayIteratorImpl = function(a) {
  var b = 0;
  return function() {
    return b < a.length ? {done:!1, value:a[b++]} : {done:!0};
  };
};
'$'jscomp.arrayIterator = function(a) {
  return {next:'$'jscomp.arrayIteratorImpl(a)};
};
'$'jscomp.makeIterator = function(a) {
  var b = "undefined" != typeof Symbol && Symbol.iterator && a[Symbol.iterator];
  return b ? b.call(a) : '$'jscomp.arrayIterator(a);
};
'$'jscomp.arrayFromIterator = function(a) {
  for (var b, c = []; !(b = a.next()).done;) {
    c.push(b.value);
  }
  return c;
};
'$'jscomp.arrayFromIterable = function(a) {
  return a instanceof Array ? a : '$'jscomp.arrayFromIterator('$'jscomp.makeIterator(a));
};
'$'jscomp.ASSUME_ES5 = !1;
'$'jscomp.ASSUME_NO_NATIVE_MAP = !1;
'$'jscomp.ASSUME_NO_NATIVE_SET = !1;
'$'jscomp.SIMPLE_FROUND_POLYFILL = !1;
'$'jscomp.objectCreate = '$'jscomp.ASSUME_ES5 || "function" == typeof Object.create ? Object.create : function(a) {
  var b = function() {
  };
  b.prototype = a;
  return new b;
};
'$'jscomp.underscoreProtoCanBeSet = function() {
  var a = {a:!0}, b = {};
  try {
    return b.__proto__ = a, b.a;
  } catch (c) {
  }
  return !1;
};
'$'jscomp.setPrototypeOf = "function" == typeof Object.setPrototypeOf ? Object.setPrototypeOf : '$'jscomp.underscoreProtoCanBeSet() ? function(a, b) {
  a.__proto__ = b;
  if (a.__proto__ !== b) {
    throw new TypeError(a + " is not extensible");
  }
  return a;
} : null;
'$'jscomp.inherits = function(a, b) {
  a.prototype = '$'jscomp.objectCreate(b.prototype);
  a.prototype.constructor = a;
  if ('$'jscomp.setPrototypeOf) {
    var c = '$'jscomp.setPrototypeOf;
    c(a, b);
  } else {
    for (c in b) {
      if ("prototype" != c) {
        if (Object.defineProperties) {
          var d = Object.getOwnPropertyDescriptor(b, c);
          d && Object.defineProperty(a, c, d);
        } else {
          a[c] = b[c];
        }
      }
    }
  }
  a.superClass_ = b.prototype;
};
var module'$'exports'$'omid'$'common'$'argsChecker = {assertTruthyString:function(a, b) {
  if (!b) {
    throw Error("Value for " + a + " is undefined, null or blank.");
  }
  if ("string" !== typeof b && !(b instanceof String)) {
    throw Error("Value for " + a + " is not a string.");
  }
  if ("" === b.trim()) {
    throw Error("Value for " + a + " is empty string.");
  }
}, assertNotNullObject:function(a, b) {
  if (null == b) {
    throw Error("Value for " + a + " is undefined or null");
  }
}, assertNumber:function(a, b) {
  if (null == b) {
    throw Error(a + " must not be null or undefined.");
  }
  if ("number" !== typeof b || isNaN(b)) {
    throw Error("Value for " + a + " is not a number");
  }
}, assertNumberBetween:function(a, b, c, d) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertNumber)(a, b);
  if (b < c || b > d) {
    throw Error("Value for " + a + " is outside the range [" + c + "," + d + "]");
  }
}, assertFunction:function(a, b) {
  if (!b) {
    throw Error(a + " must not be truthy.");
  }
}, assertPositiveNumber:function(a, b) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertNumber)(a, b);
  if (0 > b) {
    throw Error(a + " must be a positive number.");
  }
}};
var module'$'exports'$'omid'$'common'$'VersionUtils = {}, module'$'contents'$'omid'$'common'$'VersionUtils_SEMVER_DIGITS_NUMBER = 3;
module'$'exports'$'omid'$'common'$'VersionUtils.isValidVersion = function(a) {
  return /\d+\.\d+\.\d+(-.*)?/.test(a);
};
module'$'exports'$'omid'$'common'$'VersionUtils.versionGreaterOrEqual = function(a, b) {
  a = a.split("-")[0].split(".");
  b = b.split("-")[0].split(".");
  for (var c = 0; c < module'$'contents'$'omid'$'common'$'VersionUtils_SEMVER_DIGITS_NUMBER; c++) {
    var d = parseInt(a[c], 10), e = parseInt(b[c], 10);
    if (d > e) {
      break;
    } else {
      if (d < e) {
        return !1;
      }
    }
  }
  return !0;
};
var module'$'exports'$'omid'$'common'$'ArgsSerDe = {}, module'$'contents'$'omid'$'common'$'ArgsSerDe_ARGS_NOT_SERIALIZED_VERSION = "1.0.3";
module'$'exports'$'omid'$'common'$'ArgsSerDe.serializeMessageArgs = function(a, b) {
  return (0,module'$'exports'$'omid'$'common'$'VersionUtils.isValidVersion)(a) && (0,module'$'exports'$'omid'$'common'$'VersionUtils.versionGreaterOrEqual)(a, module'$'contents'$'omid'$'common'$'ArgsSerDe_ARGS_NOT_SERIALIZED_VERSION) ? b : JSON.stringify(b);
};
module'$'exports'$'omid'$'common'$'ArgsSerDe.deserializeMessageArgs = function(a, b) {
  return (0,module'$'exports'$'omid'$'common'$'VersionUtils.isValidVersion)(a) && (0,module'$'exports'$'omid'$'common'$'VersionUtils.versionGreaterOrEqual)(a, module'$'contents'$'omid'$'common'$'ArgsSerDe_ARGS_NOT_SERIALIZED_VERSION) ? b ? b : [] : b && "string" === typeof b ? JSON.parse(b) : [];
};
var module'$'exports'$'omid'$'common'$'constants = {AdEventType:{IMPRESSION:"impression", STATE_CHANGE:"stateChange", GEOMETRY_CHANGE:"geometryChange", SESSION_START:"sessionStart", SESSION_ERROR:"sessionError", SESSION_FINISH:"sessionFinish", MEDIA:"media", VIDEO:"video", LOADED:"loaded", START:"start", FIRST_QUARTILE:"firstQuartile", MIDPOINT:"midpoint", THIRD_QUARTILE:"thirdQuartile", COMPLETE:"complete", PAUSE:"pause", RESUME:"resume", BUFFER_START:"bufferStart", BUFFER_FINISH:"bufferFinish", SKIPPED:"skipped", 
VOLUME_CHANGE:"volumeChange", PLAYER_STATE_CHANGE:"playerStateChange", AD_USER_INTERACTION:"adUserInteraction"}, MediaEventType:{LOADED:"loaded", START:"start", FIRST_QUARTILE:"firstQuartile", MIDPOINT:"midpoint", THIRD_QUARTILE:"thirdQuartile", COMPLETE:"complete", PAUSE:"pause", RESUME:"resume", BUFFER_START:"bufferStart", BUFFER_FINISH:"bufferFinish", SKIPPED:"skipped", VOLUME_CHANGE:"volumeChange", PLAYER_STATE_CHANGE:"playerStateChange", AD_USER_INTERACTION:"adUserInteraction"}};
module'$'exports'$'omid'$'common'$'constants.VideoEventType = module'$'exports'$'omid'$'common'$'constants.MediaEventType;
module'$'exports'$'omid'$'common'$'constants.ImpressionType = {DEFINED_BY_JAVASCRIPT:"definedByJavaScript", UNSPECIFIED:"unspecified", LOADED:"loaded", BEGIN_TO_RENDER:"beginToRender", ONE_PIXEL:"onePixel", VIEWABLE:"viewable", AUDIBLE:"audible", OTHER:"other"};
module'$'exports'$'omid'$'common'$'constants.ErrorType = {GENERIC:"generic", VIDEO:"video", MEDIA:"media"};
module'$'exports'$'omid'$'common'$'constants.AdSessionType = {NATIVE:"native", HTML:"html", JAVASCRIPT:"javascript"};
module'$'exports'$'omid'$'common'$'constants.EventOwner = {NATIVE:"native", JAVASCRIPT:"javascript", NONE:"none"};
module'$'exports'$'omid'$'common'$'constants.AccessMode = {FULL:"full", LIMITED:"limited"};
module'$'exports'$'omid'$'common'$'constants.AppState = {BACKGROUNDED:"backgrounded", FOREGROUNDED:"foregrounded"};
module'$'exports'$'omid'$'common'$'constants.Environment = {APP:"app", WEB:"web"};
module'$'exports'$'omid'$'common'$'constants.InteractionType = {CLICK:"click", INVITATION_ACCEPT:"invitationAccept"};
module'$'exports'$'omid'$'common'$'constants.CreativeType = {DEFINED_BY_JAVASCRIPT:"definedByJavaScript", HTML_DISPLAY:"htmlDisplay", NATIVE_DISPLAY:"nativeDisplay", VIDEO:"video", AUDIO:"audio"};
module'$'exports'$'omid'$'common'$'constants.MediaType = {DISPLAY:"display", VIDEO:"video"};
module'$'exports'$'omid'$'common'$'constants.Reason = {NOT_FOUND:"notFound", HIDDEN:"hidden", BACKGROUNDED:"backgrounded", VIEWPORT:"viewport", OBSTRUCTED:"obstructed", CLIPPED:"clipped"};
module'$'exports'$'omid'$'common'$'constants.SupportedFeatures = {CONTAINER:"clid", VIDEO:"vlid"};
module'$'exports'$'omid'$'common'$'constants.VideoPosition = {PREROLL:"preroll", MIDROLL:"midroll", POSTROLL:"postroll", STANDALONE:"standalone"};
module'$'exports'$'omid'$'common'$'constants.VideoPlayerState = {MINIMIZED:"minimized", COLLAPSED:"collapsed", NORMAL:"normal", EXPANDED:"expanded", FULLSCREEN:"fullscreen"};
module'$'exports'$'omid'$'common'$'constants.NativeViewKeys = {X:"x", LEFT:"left", Y:"y", TOP:"top", WIDTH:"width", HEIGHT:"height", AD_SESSION_ID:"adSessionId", IS_FRIENDLY_OBSTRUCTION_FOR:"isFriendlyObstructionFor", CLIPS_TO_BOUNDS:"clipsToBounds", CHILD_VIEWS:"childViews", END_X:"endX", END_Y:"endY", OBSTRUCTIONS:"obstructions", OBSTRUCTION_CLASS:"obstructionClass", OBSTRUCTION_PURPOSE:"obstructionPurpose", OBSTRUCTION_REASON:"obstructionReason", PIXELS:"pixels"};
module'$'exports'$'omid'$'common'$'constants.MeasurementStateChangeSource = {CONTAINER:"container", CREATIVE:"creative"};
module'$'exports'$'omid'$'common'$'constants.ElementMarkup = {OMID_ELEMENT_CLASS_NAME:"omid-element"};
module'$'exports'$'omid'$'common'$'constants.CommunicationType = {NONE:"NONE", DIRECT:"DIRECT", POST_MESSAGE:"POST_MESSAGE"};
module'$'exports'$'omid'$'common'$'constants.OmidImplementer = {OMSDK:"omsdk"};
var module'$'contents'$'omid'$'common'$'InternalMessage_GUID_KEY = "omid_message_guid", module'$'contents'$'omid'$'common'$'InternalMessage_METHOD_KEY = "omid_message_method", module'$'contents'$'omid'$'common'$'InternalMessage_VERSION_KEY = "omid_message_version", module'$'contents'$'omid'$'common'$'InternalMessage_ARGS_KEY = "omid_message_args", module'$'exports'$'omid'$'common'$'InternalMessage = function(a, b, c, d) {
  this.guid = a;
  this.method = b;
  this.version = c;
  this.args = d;
};
module'$'exports'$'omid'$'common'$'InternalMessage.isValidSerializedMessage = function(a) {
  return !!a && void 0 !== a[module'$'contents'$'omid'$'common'$'InternalMessage_GUID_KEY] && void 0 !== a[module'$'contents'$'omid'$'common'$'InternalMessage_METHOD_KEY] && void 0 !== a[module'$'contents'$'omid'$'common'$'InternalMessage_VERSION_KEY] && "string" === typeof a[module'$'contents'$'omid'$'common'$'InternalMessage_GUID_KEY] && "string" === typeof a[module'$'contents'$'omid'$'common'$'InternalMessage_METHOD_KEY] && "string" === typeof a[module'$'contents'$'omid'$'common'$'InternalMessage_VERSION_KEY] && (void 0 === a[module'$'contents'$'omid'$'common'$'InternalMessage_ARGS_KEY] || 
  void 0 !== a[module'$'contents'$'omid'$'common'$'InternalMessage_ARGS_KEY]);
};
module'$'exports'$'omid'$'common'$'InternalMessage.deserialize = function(a) {
  return new module'$'exports'$'omid'$'common'$'InternalMessage(a[module'$'contents'$'omid'$'common'$'InternalMessage_GUID_KEY], a[module'$'contents'$'omid'$'common'$'InternalMessage_METHOD_KEY], a[module'$'contents'$'omid'$'common'$'InternalMessage_VERSION_KEY], a[module'$'contents'$'omid'$'common'$'InternalMessage_ARGS_KEY]);
};
module'$'exports'$'omid'$'common'$'InternalMessage.prototype.serialize = function() {
  var a = {};
  a = (a[module'$'contents'$'omid'$'common'$'InternalMessage_GUID_KEY] = this.guid, a[module'$'contents'$'omid'$'common'$'InternalMessage_METHOD_KEY] = this.method, a[module'$'contents'$'omid'$'common'$'InternalMessage_VERSION_KEY] = this.version, a);
  void 0 !== this.args && (a[module'$'contents'$'omid'$'common'$'InternalMessage_ARGS_KEY] = this.args);
  return a;
};
var module'$'exports'$'omid'$'common'$'Communication = function(a) {
  this.to = a;
  this.communicationType_ = module'$'exports'$'omid'$'common'$'constants.CommunicationType.NONE;
};
module'$'exports'$'omid'$'common'$'Communication.prototype.sendMessage = function(a, b) {
};
module'$'exports'$'omid'$'common'$'Communication.prototype.handleMessage = function(a, b) {
  if (this.onMessage) {
    this.onMessage(a, b);
  }
};
module'$'exports'$'omid'$'common'$'Communication.prototype.serialize = function(a) {
  return JSON.stringify(a);
};
module'$'exports'$'omid'$'common'$'Communication.prototype.deserialize = function(a) {
  return JSON.parse(a);
};
module'$'exports'$'omid'$'common'$'Communication.prototype.isDirectCommunication = function() {
  return this.communicationType_ === module'$'exports'$'omid'$'common'$'constants.CommunicationType.DIRECT;
};
module'$'exports'$'omid'$'common'$'Communication.prototype.isCrossOrigin = function() {
};
var module'$'exports'$'omid'$'common'$'DetectOmid = {OMID_PRESENT_FRAME_NAME:"omid_v1_present", isOmidPresent:function(a) {
  try {
    return a.frames ? !!a.frames[module'$'exports'$'omid'$'common'$'DetectOmid.OMID_PRESENT_FRAME_NAME] : !1;
  } catch (b) {
    return !1;
  }
}, declareOmidPresence:function(a) {
  a.frames && a.document && (module'$'exports'$'omid'$'common'$'DetectOmid.OMID_PRESENT_FRAME_NAME in a.frames || (null == a.document.body && module'$'exports'$'omid'$'common'$'DetectOmid.isMutationObserverAvailable_(a) ? module'$'exports'$'omid'$'common'$'DetectOmid.registerMutationObserver_(a) : a.document.body ? module'$'exports'$'omid'$'common'$'DetectOmid.appendPresenceIframe_(a) : a.document.write('<iframe style="display:none" id="' + (module'$'exports'$'omid'$'common'$'DetectOmid.OMID_PRESENT_FRAME_NAME + '" name="') + (module'$'exports'$'omid'$'common'$'DetectOmid.OMID_PRESENT_FRAME_NAME + 
  '"></iframe>'))));
}, appendPresenceIframe_:function(a) {
  var b = a.document.createElement("iframe");
  b.id = module'$'exports'$'omid'$'common'$'DetectOmid.OMID_PRESENT_FRAME_NAME;
  b.name = module'$'exports'$'omid'$'common'$'DetectOmid.OMID_PRESENT_FRAME_NAME;
  b.style.display = "none";
  a.document.body.appendChild(b);
}, isMutationObserverAvailable_:function(a) {
  return "MutationObserver" in a;
}, registerMutationObserver_:function(a) {
  var b = new MutationObserver(function(c) {
    c.forEach(function(c) {
      "BODY" === c.addedNodes[0].nodeName && (module'$'exports'$'omid'$'common'$'DetectOmid.appendPresenceIframe_(a), b.disconnect());
    });
  });
  b.observe(a.document.documentElement, {childList:!0});
}};
var module'$'exports'$'omid'$'common'$'DirectCommunication = function(a) {
  module'$'exports'$'omid'$'common'$'Communication.call(this, a);
  this.communicationType_ = module'$'exports'$'omid'$'common'$'constants.CommunicationType.DIRECT;
  this.handleExportedMessage = module'$'exports'$'omid'$'common'$'DirectCommunication.prototype.handleExportedMessage.bind(this);
};
'$'jscomp.inherits(module'$'exports'$'omid'$'common'$'DirectCommunication, module'$'exports'$'omid'$'common'$'Communication);
module'$'exports'$'omid'$'common'$'DirectCommunication.prototype.sendMessage = function(a, b) {
  b = void 0 === b ? this.to : b;
  if (!b) {
    throw Error("Message destination must be defined at construction time or when sending the message.");
  }
  b.handleExportedMessage(a.serialize(), this);
};
module'$'exports'$'omid'$'common'$'DirectCommunication.prototype.handleExportedMessage = function(a, b) {
  module'$'exports'$'omid'$'common'$'InternalMessage.isValidSerializedMessage(a) && this.handleMessage(module'$'exports'$'omid'$'common'$'InternalMessage.deserialize(a), b);
};
module'$'exports'$'omid'$'common'$'DirectCommunication.prototype.isCrossOrigin = function() {
  return !1;
};
var module'$'exports'$'omid'$'common'$'eventTypedefs = {};
var module'$'exports'$'omid'$'common'$'exporter = {};
function module'$'contents'$'omid'$'common'$'exporter_getOmidExports() {
  return "undefined" === typeof omidExports ? null : omidExports;
}
function module'$'contents'$'omid'$'common'$'exporter_getOrCreateName(a, b) {
  return a && (a[b] || (a[b] = {}));
}
module'$'exports'$'omid'$'common'$'exporter.packageExport = function(a, b, c) {
  if (c = void 0 === c ? module'$'contents'$'omid'$'common'$'exporter_getOmidExports() : c) {
    a = a.split("."), a.slice(0, a.length - 1).reduce(module'$'contents'$'omid'$'common'$'exporter_getOrCreateName, c)[a[a.length - 1]] = b;
  }
};
var module'$'exports'$'omid'$'common'$'guid = {generateGuid:function() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function(a) {
    var b = 16 * Math.random() | 0;
    a = "y" === a ? (b & 3 | 8).toString(16) : b.toString(16);
    return a;
  });
}};
var module'$'exports'$'omid'$'common'$'logger = {error:function(a) {
  for (var b = [], c = 0; c < arguments.length; ++c) {
    b[c - 0] = arguments[c];
  }
  module'$'contents'$'omid'$'common'$'logger_executeLog(function() {
    throw new (Function.prototype.bind.apply(Error, [null, "Could not complete the test successfully - "].concat('$'jscomp.arrayFromIterable(b))));
  }, function() {
    return console.error.apply(console, '$'jscomp.arrayFromIterable(b));
  });
}, debug:function(a) {
  for (var b = [], c = 0; c < arguments.length; ++c) {
    b[c - 0] = arguments[c];
  }
  module'$'contents'$'omid'$'common'$'logger_executeLog(function() {
  }, function() {
    return console.error.apply(console, '$'jscomp.arrayFromIterable(b));
  });
}};
function module'$'contents'$'omid'$'common'$'logger_executeLog(a, b) {
  "undefined" !== typeof jasmine && jasmine ? a() : "undefined" !== typeof console && console && console.error && b();
}
;var module'$'exports'$'omid'$'common'$'OmidGlobalProvider = {}, module'$'contents'$'omid'$'common'$'OmidGlobalProvider_globalThis = eval("this");
function module'$'contents'$'omid'$'common'$'OmidGlobalProvider_getOmidGlobal() {
  if ("undefined" !== typeof omidGlobal && omidGlobal) {
    return omidGlobal;
  }
  if ("undefined" !== typeof global && global) {
    return global;
  }
  if ("undefined" !== typeof window && window) {
    return window;
  }
  if ("undefined" !== typeof module'$'contents'$'omid'$'common'$'OmidGlobalProvider_globalThis && module'$'contents'$'omid'$'common'$'OmidGlobalProvider_globalThis) {
    return module'$'contents'$'omid'$'common'$'OmidGlobalProvider_globalThis;
  }
  throw Error("Could not determine global object context.");
}
module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal = module'$'contents'$'omid'$'common'$'OmidGlobalProvider_getOmidGlobal();
var module'$'exports'$'omid'$'common'$'windowUtils = {};
function module'$'contents'$'omid'$'common'$'windowUtils_isValidWindow(a) {
  return null != a && "undefined" !== typeof a.top && null != a.top;
}
module'$'exports'$'omid'$'common'$'windowUtils.isCrossOrigin = function(a) {
  if (a === module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal) {
    return !1;
  }
  try {
    if ("undefined" === typeof a.location.hostname) {
      return !0;
    }
    module'$'contents'$'omid'$'common'$'windowUtils_isSameOriginForIE(a);
  } catch (b) {
    return !0;
  }
  return !1;
};
function module'$'contents'$'omid'$'common'$'windowUtils_isSameOriginForIE(a) {
  return "" === a.x || "" !== a.x;
}
module'$'exports'$'omid'$'common'$'windowUtils.resolveGlobalContext = function(a) {
  "undefined" === typeof a && "undefined" !== typeof window && window && (a = window);
  return module'$'contents'$'omid'$'common'$'windowUtils_isValidWindow(a) ? a : module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal;
};
module'$'exports'$'omid'$'common'$'windowUtils.resolveTopWindowContext = function(a) {
  return module'$'contents'$'omid'$'common'$'windowUtils_isValidWindow(a) ? a.top : module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal;
};
module'$'exports'$'omid'$'common'$'windowUtils.evaluatePageUrl = function(a) {
  if (!module'$'contents'$'omid'$'common'$'windowUtils_isValidWindow(a)) {
    return null;
  }
  try {
    var b = a.top;
    return (0,module'$'exports'$'omid'$'common'$'windowUtils.isCrossOrigin)(b) ? null : b.location.href;
  } catch (c) {
    return null;
  }
};
var module'$'exports'$'omid'$'common'$'PostMessageCommunication = function(a, b) {
  b = void 0 === b ? module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal : b;
  module'$'exports'$'omid'$'common'$'Communication.call(this, b);
  var c = this;
  this.communicationType_ = module'$'exports'$'omid'$'common'$'constants.CommunicationType.POST_MESSAGE;
  a.addEventListener("message", function(a) {
    if ("object" === typeof a.data) {
      var b = a.data;
      module'$'exports'$'omid'$'common'$'InternalMessage.isValidSerializedMessage(b) && (b = module'$'exports'$'omid'$'common'$'InternalMessage.deserialize(b), a.source && c.handleMessage(b, a.source));
    }
  });
};
'$'jscomp.inherits(module'$'exports'$'omid'$'common'$'PostMessageCommunication, module'$'exports'$'omid'$'common'$'Communication);
module'$'exports'$'omid'$'common'$'PostMessageCommunication.isCompatibleContext = function(a) {
  return !!(a && a.addEventListener && a.postMessage);
};
module'$'exports'$'omid'$'common'$'PostMessageCommunication.prototype.sendMessage = function(a, b) {
  b = void 0 === b ? this.to : b;
  if (!b) {
    throw Error("Message destination must be defined at construction time or when sending the message.");
  }
  b.postMessage(a.serialize(), "*");
};
module'$'exports'$'omid'$'common'$'PostMessageCommunication.prototype.isCrossOrigin = function() {
  return this.to ? (0,module'$'exports'$'omid'$'common'$'windowUtils.isCrossOrigin)(this.to) : !0;
};
var module'$'exports'$'omid'$'common'$'Rectangle = function(a, b, c, d) {
  this.x = a;
  this.y = b;
  this.width = c;
  this.height = d;
};
var module'$'exports'$'omid'$'common'$'serviceCommunication = {}, module'$'contents'$'omid'$'common'$'serviceCommunication_EXPORTED_SESSION_COMMUNICATION_NAME = ["omid", "v1_SessionServiceCommunication"], module'$'contents'$'omid'$'common'$'serviceCommunication_EXPORTED_VERIFICATION_COMMUNICATION_NAME = ["omid", "v1_VerificationServiceCommunication"], module'$'contents'$'omid'$'common'$'serviceCommunication_EXPORTED_SERVICE_WINDOW_NAME = ["omidVerificationProperties", "serviceWindow"];
function module'$'contents'$'omid'$'common'$'serviceCommunication_getValueForKeypath(a, b) {
  return b.reduce(function(a, b) {
    return a && a[b];
  }, a);
}
function module'$'contents'$'omid'$'common'$'serviceCommunication_startServiceCommunication(a, b, c, d) {
  if (!(0,module'$'exports'$'omid'$'common'$'windowUtils.isCrossOrigin)(b)) {
    try {
      var e = module'$'contents'$'omid'$'common'$'serviceCommunication_getValueForKeypath(b, c);
      if (e) {
        return new module'$'exports'$'omid'$'common'$'DirectCommunication(e);
      }
    } catch (f) {
    }
  }
  return d(b) ? new module'$'exports'$'omid'$'common'$'PostMessageCommunication(a, b) : null;
}
function module'$'contents'$'omid'$'common'$'serviceCommunication_startServiceCommunicationFromCandidates(a, b, c, d) {
  b = '$'jscomp.makeIterator(b);
  for (var e = b.next(); !e.done; e = b.next()) {
    if (e = module'$'contents'$'omid'$'common'$'serviceCommunication_startServiceCommunication(a, e.value, c, d)) {
      return e;
    }
  }
  return null;
}
module'$'exports'$'omid'$'common'$'serviceCommunication.startSessionServiceCommunication = function(a, b, c) {
  c = void 0 === c ? module'$'exports'$'omid'$'common'$'DetectOmid.isOmidPresent : c;
  var d = [a, (0,module'$'exports'$'omid'$'common'$'windowUtils.resolveTopWindowContext)(a)];
  b && d.unshift(b);
  return module'$'contents'$'omid'$'common'$'serviceCommunication_startServiceCommunicationFromCandidates(a, d, module'$'contents'$'omid'$'common'$'serviceCommunication_EXPORTED_SESSION_COMMUNICATION_NAME, c);
};
module'$'exports'$'omid'$'common'$'serviceCommunication.startVerificationServiceCommunication = function(a, b) {
  b = void 0 === b ? module'$'exports'$'omid'$'common'$'DetectOmid.isOmidPresent : b;
  var c = [], d = module'$'contents'$'omid'$'common'$'serviceCommunication_getValueForKeypath(a, module'$'contents'$'omid'$'common'$'serviceCommunication_EXPORTED_SERVICE_WINDOW_NAME);
  d && c.push(d);
  c.push((0,module'$'exports'$'omid'$'common'$'windowUtils.resolveTopWindowContext)(a));
  return module'$'contents'$'omid'$'common'$'serviceCommunication_startServiceCommunicationFromCandidates(a, c, module'$'contents'$'omid'$'common'$'serviceCommunication_EXPORTED_VERIFICATION_COMMUNICATION_NAME, b);
};
var module'$'exports'$'omid'$'common'$'VastProperties = function(a, b, c, d) {
  this.isSkippable = a;
  this.skipOffset = b;
  this.isAutoPlay = c;
  this.position = d;
};
module'$'exports'$'omid'$'common'$'VastProperties.prototype.toJSON = function() {
  return {isSkippable:this.isSkippable, skipOffset:this.skipOffset, isAutoPlay:this.isAutoPlay, position:this.position};
};
var module'$'exports'$'omid'$'common'$'version = {ApiVersion:"1.0", Version:"1.3.1-iab2040"};
var module'$'contents'$'omid'$'verificationClient'$'VerificationClient_VERIFICATION_CLIENT_VERSION = module'$'exports'$'omid'$'common'$'version.Version, module'$'contents'$'omid'$'verificationClient'$'VerificationClient_EventCallback;
function module'$'contents'$'omid'$'verificationClient'$'VerificationClient_getThirdPartyOmid() {
  var a = module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.omid3p;
  return a && "function" === typeof a.registerSessionObserver && "function" === typeof a.addEventListener ? a : null;
}
var module'$'exports'$'omid'$'verificationClient'$'VerificationClient = function(a) {
  if (this.communication = a || (0,module'$'exports'$'omid'$'common'$'serviceCommunication.startVerificationServiceCommunication)((0,module'$'exports'$'omid'$'common'$'windowUtils.resolveGlobalContext)())) {
    this.communication.onMessage = this.handleMessage_.bind(this);
  } else {
    if (a = module'$'contents'$'omid'$'verificationClient'$'VerificationClient_getThirdPartyOmid()) {
      this.omid3p = a;
    }
  }
  this.remoteIntervals_ = this.remoteTimeouts_ = 0;
  this.callbackMap_ = {};
  this.imgCache_ = [];
  this.injectionId_ = (a = module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.omidVerificationProperties) ? a.injectionId : void 0;
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.isSupported = function() {
  return !(!this.communication && !this.omid3p);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.registerSessionObserver = function(a, b) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertFunction)("functionToExecute", a);
  this.omid3p ? this.omid3p.registerSessionObserver(a, b, this.injectionId_) : this.sendMessage_("addSessionListener", a, b, this.injectionId_);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.addEventListener = function(a, b) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertTruthyString)("eventType", a);
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertFunction)("functionToExecute", b);
  this.omid3p ? this.omid3p.addEventListener(a, b) : this.sendMessage_("addEventListener", b, a);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.sendUrl = function(a, b, c) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertTruthyString)("url", a);
  module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.document && module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.document.createElement ? this.sendUrlWithImg_(a, b, c) : this.sendMessage_("sendUrl", function(a) {
    a && b ? b() : !a && c && c();
  }, a);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.sendUrlWithImg_ = function(a, b, c) {
  var d = this, e = module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.document.createElement("img");
  this.imgCache_.push(e);
  var f = function(a) {
    var b = d.imgCache_.indexOf(e);
    0 <= b && d.imgCache_.splice(b, 1);
    a && a();
  };
  e.addEventListener("load", f.bind(this, b));
  e.addEventListener("error", f.bind(this, c));
  e.src = a;
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.injectJavaScriptResource = function(a, b, c) {
  var d = this;
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertTruthyString)("url", a);
  module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.document ? this.injectJavascriptResourceUrlInDom_(a, b, c) : this.sendMessage_("injectJavaScriptResource", function(e, f) {
    e ? (d.evaluateJavaScript_(f, a), b()) : (module'$'exports'$'omid'$'common'$'logger.error("Service failed to load JavaScript resource."), c());
  }, a);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.injectJavascriptResourceUrlInDom_ = function(a, b, c) {
  var d = module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.document, e = d.body;
  d = d.createElement("script");
  d.onload = b;
  d.onerror = c;
  d.src = a;
  d.type = "application/javascript";
  e.appendChild(d);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.evaluateJavaScript_ = function(a, b) {
  try {
    eval(a);
  } catch (c) {
    module'$'exports'$'omid'$'common'$'logger.error('Error evaluating the JavaScript resource from "' + b + '".');
  }
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.setTimeout = function(a, b) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertFunction)("functionToExecute", a);
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertPositiveNumber)("timeInMillis", b);
  if (this.hasTimeoutMethods_()) {
    return module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.setTimeout(a, b);
  }
  var c = this.remoteTimeouts_++;
  this.sendMessage_("setTimeout", a, c, b);
  return c;
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.clearTimeout = function(a) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertPositiveNumber)("timeoutId", a);
  this.hasTimeoutMethods_() ? module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.clearTimeout(a) : this.sendOneWayMessage_("clearTimeout", a);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.setInterval = function(a, b) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertFunction)("functionToExecute", a);
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertPositiveNumber)("timeInMillis", b);
  if (this.hasIntervalMethods_()) {
    return module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.setInterval(a, b);
  }
  var c = this.remoteIntervals_++;
  this.sendMessage_("setInterval", a, c, b);
  return c;
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.clearInterval = function(a) {
  (0,module'$'exports'$'omid'$'common'$'argsChecker.assertPositiveNumber)("intervalId", a);
  this.hasIntervalMethods_() ? module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.clearInterval(a) : this.sendOneWayMessage_("clearInterval", a);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.hasTimeoutMethods_ = function() {
  return "function" === typeof module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.setTimeout && "function" === typeof module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.clearTimeout;
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.hasIntervalMethods_ = function() {
  return "function" === typeof module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.setInterval && "function" === typeof module'$'exports'$'omid'$'common'$'OmidGlobalProvider.omidGlobal.clearInterval;
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.handleMessage_ = function(a, b) {
  b = a.method;
  var c = a.guid;
  a = a.args;
  if ("response" === b && this.callbackMap_[c]) {
    var d = (0,module'$'exports'$'omid'$'common'$'ArgsSerDe.deserializeMessageArgs)(module'$'contents'$'omid'$'verificationClient'$'VerificationClient_VERIFICATION_CLIENT_VERSION, a);
    this.callbackMap_[c].apply(this, d);
  }
  "error" === b && window.console && module'$'exports'$'omid'$'common'$'logger.error(a);
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.sendOneWayMessage_ = function(a, b) {
  for (var c = [], d = 1; d < arguments.length; ++d) {
    c[d - 1] = arguments[d];
  }
  this.sendMessage_.apply(this, [a, null].concat('$'jscomp.arrayFromIterable(c)));
};
module'$'exports'$'omid'$'verificationClient'$'VerificationClient.prototype.sendMessage_ = function(a, b, c) {
  for (var d = [], e = 2; e < arguments.length; ++e) {
    d[e - 2] = arguments[e];
  }
  this.communication && (e = (0,module'$'exports'$'omid'$'common'$'guid.generateGuid)(), b && (this.callbackMap_[e] = b), d = new module'$'exports'$'omid'$'common'$'InternalMessage(e, "VerificationService." + a, module'$'contents'$'omid'$'verificationClient'$'VerificationClient_VERIFICATION_CLIENT_VERSION, (0,module'$'exports'$'omid'$'common'$'ArgsSerDe.serializeMessageArgs)(module'$'contents'$'omid'$'verificationClient'$'VerificationClient_VERIFICATION_CLIENT_VERSION, d)), this.communication.sendMessage(d));
};
(0,module'$'exports'$'omid'$'common'$'exporter.packageExport)("OmidVerificationClient", module'$'exports'$'omid'$'verificationClient'$'VerificationClient);

}, typeof exports === 'undefined' ? undefined : exports));
</script><script type="text/javascript" src="https://d11x7a53l5pste.cloudfront.net/script.js?cb=5" data-api-autoplay="top|block" data-custom_fields='{"cpm":"5","bidder":"sterling_cooper","creativeid":"c5007b98-9510-47f7-94f2-a7cfc2d8d664"}'></script>
        <!DOCTYPE html>
        <html>
        <head>
            <title>Timer</title>
            <style>
                .container {
                    margin: auto;
                    text-align: center;
                    color: white;
                    width: 100%;
                }

                body {
                    width: 100%;
                    margin: 0 auto;
                    background-color: black;
                }

                .center {
                    margin: 0;
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    -ms-transform: translate(-50%, -50%);
                    transform: translate(-50%, -50%);
                    font-size: 14px;
                }

                p {
                    margin: 0;
                }
            </style>
            <script>
                function startTime() {
                    const currTime = new Date();
                    document.getElementById('txt').innerHTML = currTime.getHours() + ":" + currTime.getMinutes() + ":" + currTime.getSeconds();
                }
            </script>
        </head>

        <body onload="startTime()">
            <div class="container">
                <div class="center">
                    <a href="http://whisper.sh" target="_blank"><p>Req received Sep 12 07:38:11</p></a>
                    
                    <p>Bid displayed <span id="txt"></span></p>
                </div>
            </div>
						<script src="https://s3-us-west-2.amazonaws.com/omsdk-files/compliance-js/omid-validation-verification-script-v1.js"></script>
        </body>
        </html>
    </script></body>
         """.trimIndent()
    }
}