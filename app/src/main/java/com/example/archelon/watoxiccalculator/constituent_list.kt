package com.example.archelon.watoxiccalculator

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.*



class constituent_list : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    var streamName: String = String()
    private val defaultName = "Add A Constituent"
    private val dbHelper = FeedReaderDbHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constituent_list)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        streamName = intent.getStringExtra("wasteName")
        this.title = streamName

        render_chems()
    }

    fun render_chems(){
        var constituentList: MutableList<String> = ArrayList<String>()
        val tempMap = dbHelper.retrieveConstituents(streamName)
        var keyList: MutableList<String> = ArrayList<String>()
        var rankList: MutableList<String> = ArrayList<String>()
        keyList.add("-1")
        keyList.addAll(tempMap.keys.toMutableList())
        constituentList.add(defaultName)
        constituentList.addAll(tempMap.values)
        rankList.add("N")

        for(temp in tempMap.values){
            rankList.add(dbHelper.retrieveChemRank(temp))
        }

        var designation: ranking = ranking()



        viewManager = LinearLayoutManager(this)
        viewAdapter = ConstCardAdapter(constituentList, keyList, rankList, this)

        recyclerView = findViewById<RecyclerView>(R.id.constituent_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
        setRecyclerViewItemTouchListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val input_name = data?.getStringExtra("Name")
        val input_index = data?.getStringExtra("Index")

        if(input_name != null && input_index != null) {
            if(input_index.equals("-1")) {
                dbHelper.putChemWithWaste(streamName, input_name)
            }else{
                dbHelper.updateWasteListChemId(streamName, input_index, input_name)
            }
        }
        render_chems()

    }

    private fun setRecyclerViewItemTouchListener() {

        //1
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                //2
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val temp: TextView = viewHolder.itemView.findViewById(R.id.chemicalName)
                dbHelper.deleteConstituent(streamName, temp.text.toString().substringBefore(":"))
                /*val toast = Toast.makeText(applicationContext, viewHolder.adapterPosition.toString(), Toast.LENGTH_LONG)
                toast.show()*/
                viewHolder.adapterPosition
                viewAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                render_chems()
            }
        }

        //4
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


/*Depricated, leave in place for now
*/

    class ConstListAdapter(private val myDataset: MutableList<String>, private val keyList: MutableList<String>, private val rankList: MutableList<String>, private val mContext : constituent_list) :
        RecyclerView.Adapter<ConstListAdapter.MyViewHolder>() {

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): ConstListAdapter.MyViewHolder {
            // create a new view
            val textView = TextView(parent.context)
            // set the view's size, margins, paddings and layout parameters
            textView.textSize = 25.5.toFloat()
            textView.setPadding(20,20,20,20)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(20,20,20,20)
            textView.layoutParams = params
            return MyViewHolder(textView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            var rank: String = rankList[position]
            if (rank.equals("N")){ rank = ": N/A"}
            else{ rank = ": " + rank}
            if(position == 0){rank = ""}
            holder.textView.text = (myDataset[position] + rank)
            holder.textView.setOnClickListener {
                val intent = Intent(mContext, Chemical_List::class.java).apply {
                        putExtra("Index", keyList[position])
                }
                mContext.startActivityForResult(intent,0)
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = myDataset.size
    }

    fun saveConcentration(wasteName: String, chemName: String, inputConcentration: String){
        dbHelper.updateConcentration(wasteName,chemName,inputConcentration)
    }

//Currently in use
    class ConstCardAdapter(private val myDataset: MutableList<String>, private val keyList: MutableList<String>,
                           private val rankList: MutableList<String>, private val mContext : constituent_list) :
        RecyclerView.Adapter<ConstCardAdapter.ViewHolder>() {
        var designation: ranking = ranking()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            //var itemImage: ImageView
            var itemTitle: TextView
            var itemConst: TextView
            var itemDetail: EditText
            var layout: RelativeLayout

            init {
                //itemImage = itemView.findViewById(R.id.item_image)
                itemTitle = itemView.findViewById(R.id.chemicalName)
                itemConst = itemView.findViewById(R.id.concentrationText)
                itemDetail = itemView.findViewById(R.id.concentrationEdit)
                layout = itemView.findViewById(R.id.cardlayout)
            }
        }

        private val images = R.drawable.abc_ic_star_half_black_36dp

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
            val v = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.card_layout, viewGroup, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            var rank: String = rankList[position]
            var conc: String = " "
            var tempConc = mContext.dbHelper.retrieveConc(mContext.streamName,myDataset[position])
            if (rank.equals("N")){ rank = ": N/A"}
            else{ rank = ": " + rank}
            if(position == 0){
                rank = ""
            }
            else{
                viewHolder.itemDetail.setText(tempConc)
                designation.addToSum(tempConc,rankList[position])
            }
            viewHolder.itemTitle.text = myDataset[position] + rank
            viewHolder.itemTitle.textSize = 22.toFloat()
            //viewHolder.itemDetail.setText(details[postion])
            viewHolder.itemConst.setText("Set Concentration")

            viewHolder.itemDetail.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                    if (event.action === KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        Log.d("Target Conc", viewHolder.itemDetail.text.toString())
                        val temp2 = tempConc
                        mContext.saveConcentration(mContext.streamName,myDataset[position],viewHolder.itemDetail.text.toString())

                        mContext.title = mContext.streamName + " " + designation.replaceConcentration(tempConc,viewHolder.itemDetail.text.toString(),rankList[position])
                        tempConc = mContext.dbHelper.retrieveConc(mContext.streamName,myDataset[position])
                        return true
                    }
                    return false
                }
            })
            //viewHolder.itemImage.setImageResource(images)
            viewHolder.layout.setOnClickListener {
                val intent = Intent(mContext, Chemical_List::class.java).apply {
                    putExtra("Index", keyList[position])
                    putExtra("Stream_Name", mContext.streamName)
                }
                mContext.startActivityForResult(intent,0)
            }
            if(position == (myDataset.lastIndex)){
                mContext.title = mContext.streamName + " " + designation.returnDesignation()
            }
        }

        override fun getItemCount(): Int {
            return myDataset.size
        }
    }


    override fun onBackPressed(){
        if(streamName.equals("Add A Waste Stream")){
                val dialog: WasteNameDialogFragment = WasteNameDialogFragment()
                dialog.setContext(this, streamName)
                dialog.show(supportFragmentManager, "Waste Stream Name")
        }
        else{
            val back_intent = Intent()
            setResult(AppCompatActivity.RESULT_OK, back_intent)
            finish()
        }
    }
}