package com.example.archelon.watoxiccalculator

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.CardView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(findViewById(R.id.my_toolbar))

        /*val grdview: GridView = findViewById(R.id.gridview)
        grdview.adapter = ImageAdapter(this)*/
        setWasteLink()
        setDOELink()
        setAboutUs()
        setSyncLink()
        setPrivacy()
    }

    private fun dbtest(){
        val dbCooper = FeedReaderDbHelper(this)
        dbCooper.purge()
        dbCooper.generate()
        dbCooper.preload_database()
        dbCooper.purgeWastes()
        dbCooper.generateWastes()
    }

    private fun setPrivacy(){
        val privacyLayout = findViewById<LinearLayout>(R.id.privacyLayout)
        privacyLayout.setOnClickListener {
            val privacyIntent = Intent(this, privacy::class.java)
            startActivity(privacyIntent)
            Log.d("Send To: ", "Privacy")

        }
    }

    private fun setAboutUs(){
        val aboutUsLayout = findViewById<LinearLayout>(R.id.aboutUsLayout)
        aboutUsLayout.setOnClickListener {
            val aboutUsIntent = Intent(this, aboutUs::class.java)
            startActivity(aboutUsIntent)
            Log.d("Send To: ", "AboutUs")

        }
    }

    private fun setSyncLink(){
        val syncLayout = findViewById<LinearLayout>(R.id.syncLayout)
        syncLayout.setOnClickListener {dbtest()}
    }

    private fun setWasteLink() {
        val wasteLayout = findViewById<LinearLayout>(R.id.wasteLayout)
        wasteLayout.setOnClickListener {
            val constituentIntent = Intent(this, WasteStreams::class.java)
            startActivity(constituentIntent)
        }
    }

    private fun setDOELink(){
        val doeCard = findViewById<LinearLayout>(R.id.doeLayout)
        doeCard.setOnClickListener {goToUrl("https://ecology.wa.gov/")}
    }

    private fun goToUrl(url: String) {
        val uriUrl = Uri.parse(url)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }

    class ImageAdapter(private val mContext: MainActivity) : BaseAdapter() {

        private val mThumbIds = arrayOf<Int>(
            R.drawable.qmark, R.drawable.beaker,
            R.drawable.sync, R.drawable.stop)

        private val bttnLables = arrayOf<String>(
            "Tutorial","View Waste Streams","Sync Chemical List","Do not click"
        )

        override fun getCount(): Int = mThumbIds.size

        override fun getItem(position: Int): Any? = null

        override fun getItemId(position: Int): Long = 0L

        // create a new ImageView for each item referenced by the Adapter
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val imageView: Button
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = Button(mContext)
                imageView.layoutParams = ViewGroup.LayoutParams(275, 175)
                //imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                //imageView.setPadding(15, 15, 15, 15)
                if(position == 0) {
                    imageView.setOnClickListener {

                    }
                }else if(position == 1){
                    imageView.setOnClickListener {
                        val constituent_intent = Intent(mContext, WasteStreams::class.java)
                        mContext.startActivity(constituent_intent)
                    }
                }
                else if(position == 2){
                    imageView.setOnClickListener {
                        mContext.dbtest()
                    }
                }
                else {

                }

            } else {
                imageView = convertView as Button
            }

            //imageView.setImageResource(mThumbIds[position])
            imageView.text = bttnLables[position]
            imageView.setTextColor(Color.LTGRAY)
            imageView.setBackgroundResource(mThumbIds[position])
            return imageView
        }
    }
}
