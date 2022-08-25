package com.jrodiz.helloworld01

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewContainer : ConstraintLayout {
    constructor(ctx: Context) : this(ctx, null)
    constructor(ctx: Context, attributeSet: AttributeSet?) : this(ctx, attributeSet, 0)
    constructor(ctx: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        ctx,
        attributeSet,
        defStyle
    ) {
        init()
    }

    var innerRv: RecyclerView? = null
    var adView: ImageView? = null

    fun init() {
        innerRv = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        adView = ImageView(context).apply {
            setBackgroundResource(R.drawable.adhere)
        }
        addView(
            adView,
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        addView(
            innerRv,
            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

}