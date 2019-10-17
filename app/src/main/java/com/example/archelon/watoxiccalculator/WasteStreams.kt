package com.example.archelon.watoxiccalculator

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.view.Menu
import android.view.MenuInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import java.util.*

class WasteStreams : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var wastes: ArrayList<waste> = ArrayList()
    //private var wasteList: MutableList<String> = ArrayList<String>()
    private val dbHelper = FeedReaderDbHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waste_streams)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        val tempchem: ArrayList<chemical> = ArrayList()
        tempchem.add(chemical("Add A Chemical", null,null,null,null))
        wastes.add(waste("Add A Waste Stream", tempchem))



        render_wastes()
    }

    fun render_wastes(){
        var wasteList: MutableList<String> = ArrayList<String>()
        wasteList.add("Add A Waste Stream")
        wasteList.addAll(dbHelper.retrieveWastes())
        //Sometimes this breaks ¯\_(ツ)_/¯
        viewManager = LinearLayoutManager(this)
        viewAdapter = WasteStreams.WasteCardAdapter(wasteList, this)

        this.setTitle("All Waste Streams")

        recyclerView = findViewById<RecyclerView>(R.id.wastestream_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)


        return true
    }

    fun getDesignation(wasteName: String): String{
        val tempConst = dbHelper.retrieveConstituents(wasteName)
        var tempRank: MutableList<String> = ArrayList<String>()
        var tempConcentration: MutableList<String> = ArrayList<String>()
        var displayRank: ranking = ranking()

        for(constit in tempConst.values){
            tempRank.add(dbHelper.retrieveChemRank(constit))
            tempConcentration.add(dbHelper.retrieveConc(wasteName,constit))
        }
        for(position in 0..tempRank.lastIndex){
            displayRank.addToSum(tempConcentration[position],tempRank[position])
        }

        return displayRank.returnDesignation()
    }

    class WasteAdapter(private val myDataset: MutableList<String>, private val mContext : WasteStreams) :
        RecyclerView.Adapter<WasteAdapter.MyViewHolder>() {

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): WasteAdapter.MyViewHolder {
            // create a new view
            val textView = TextView(parent.context)
            // set the view's size, margins, paddings and layout parameters
            textView.setPadding(20,20,20,20)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(20,20,20,20)
            textView.layoutParams = params


            return MyViewHolder(textView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            var lastDown: Long = 0

            holder.textView.text = (myDataset[position] + ": " + mContext.getDesignation(myDataset[position]))
            holder.textView.textSize = 22.toFloat()

            holder.textView.setOnClickListener {
                //for what ever reason, onTouch would not work without also having an on click ¯\_(ツ)_/¯
            }
            holder.textView.setOnTouchListener(object : View.OnTouchListener{
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            lastDown = System.currentTimeMillis()
                        }
                        MotionEvent.ACTION_UP ->{
                            if(((System.currentTimeMillis() - lastDown) >= 1500) && !(lastDown.equals(0)) && !(myDataset[position].equals("Add A Waste Stream")) ){
                                //to do, edit chemical
                                val dialog: StreamNameDialogFragment = StreamNameDialogFragment()
                                dialog.setContext(mContext, myDataset[position])
                                dialog.show(mContext.supportFragmentManager, "Waste Stream Name")

                                myDataset[position] = dialog.newName
                                Log.d("New Name", myDataset[position])
                                holder.textView.text = (myDataset[position] + ": " + mContext.getDesignation(myDataset[position]))
                            }else if((myDataset[position].equals("Add A Waste Stream"))){
                                val dialog: StreamNameDialogFragment = StreamNameDialogFragment()
                                dialog.setContext(mContext, myDataset[position])
                                dialog.show(mContext.supportFragmentManager, "New Stream Name")
                            }else{
                                val intent = Intent(mContext, constituent_list::class.java).apply {
                                    putExtra("wasteName",myDataset[position])
                                }
                                mContext.startActivityForResult(intent,0)
                            }
                            /*val toast = Toast.makeText(mContext, (System.currentTimeMillis() - lastDown).toString(), Toast.LENGTH_LONG)
                            toast.show()*/
                        }
                    }

                    return v?.onTouchEvent(event) ?: true
                }

            })

        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = myDataset.size
    }

    class WasteCardAdapter(private val myDataset: MutableList<String>, private val mContext : WasteStreams) :
        RecyclerView.Adapter<WasteCardAdapter.ViewHolder>() {
        var trigger: Boolean = true

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            //var itemImage: ImageView
            var itemTitle: TextView
            /*var itemConst: TextView
            var itemDetail: EditText*/
            var layout: RelativeLayout
            val thisItem: View

            init {
                //itemImage = itemView.findViewById(R.id.item_image)
                itemTitle = itemView.findViewById(R.id.chemicalName2)
                /*itemConst = itemView.findViewById(R.id.concentrationText)
                itemDetail = itemView.findViewById(R.id.concentrationEdit)*/
                layout = itemView.findViewById(R.id.chemLayout)
                thisItem = itemView
            }
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
            val v = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.chem_layout, viewGroup, false)
            return ViewHolder(v)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            var lastDown: Long = 0
            val designation = mContext.getDesignation(myDataset[position])
            holder.itemTitle.text = (myDataset[position] + ": " + designation)
            if(designation.equals("WT02")){
                holder.thisItem.setBackgroundColor(Color.parseColor("#F99F00"))
                holder.itemTitle.setTextColor(Color.parseColor("#000000"))
            }
            else if(designation.equals("WT01")){
                holder.thisItem.setBackgroundColor(Color.parseColor("#FD0404"))
                holder.itemTitle.setTextColor(Color.parseColor("#000000"))
            }
            holder.itemTitle.textSize = 22.toFloat()

            holder.layout.setOnClickListener {
                //for what ever reason, onTouch would not work without also having an on click ¯\_(ツ)_/¯
            }
            holder.layout.setOnTouchListener(object : View.OnTouchListener{
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            lastDown = System.currentTimeMillis()
                        }
                        MotionEvent.ACTION_UP ->{
                            if(((System.currentTimeMillis() - lastDown) >= 1500) && !(lastDown.equals(0)) && !(myDataset[position].equals("Add A Waste Stream")) ){
                                //to do, edit chemical
                                val dialog: StreamNameDialogFragment = StreamNameDialogFragment()
                                dialog.setContext(mContext, myDataset[position])
                                dialog.show(mContext.supportFragmentManager, "Waste Stream Name")

                                myDataset[position] = dialog.newName
                                Log.d("New Name", myDataset[position])
                                holder.itemTitle.text = (myDataset[position] + ": " + mContext.getDesignation(myDataset[position]))
                            }else if((myDataset[position].equals("Add A Waste Stream"))){
                                val dialog: StreamNameDialogFragment = StreamNameDialogFragment()
                                dialog.setContext(mContext, myDataset[position])
                                dialog.show(mContext.supportFragmentManager, "New Stream Name")
                            }else{
                                val intent = Intent(mContext, constituent_list::class.java).apply {
                                    putExtra("wasteName",myDataset[position])
                                }
                                mContext.startActivityForResult(intent,0)
                            }
                            /*val toast = Toast.makeText(mContext, (System.currentTimeMillis() - lastDown).toString(), Toast.LENGTH_LONG)
                            toast.show()*/
                        }
                    }
                    return v?.onTouchEvent(event) ?: true
                }
            })
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = myDataset.size
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /*val input_chems: ArrayList<chemical>? = data?.getParcelableArrayListExtra("chemicals")
        val input_name = data?.getStringExtra("wasteName")
        var input_index: Int? = data?.getIntExtra("position", -1)

        if(input_index == wastes.lastIndex && input_chems != null && !(input_chems[0].name.equals("Add A Chemical"))){
            wastes[input_index].chems = input_chems
            wastes[input_index].stream_name = input_name

            val tempchem: ArrayList<chemical> = ArrayList()
            tempchem.add(chemical("Add A Chemical", null,null,null,null))
            wastes.add(waste("Add A Waste Stream", tempchem))

        }*/
        render_wastes()

    }
}
