package com.example.archelon.watoxiccalculator

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_constitutent_card_view.*

class constitutentCardView : AppCompatActivity() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    //private var adapter: RecyclerView.Adapter<CardAdapter.ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constitutent_card_view)
        //setSupportActionBar(toolbar)

        /*layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager

        adapter = CardAdapter()
        recycler_view.adapter = adapter*/

    }
}
