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


class Chemical_List : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val dbHelper = FeedReaderDbHelper(this)
    private var index: String = String()
    private var wasteName: String = String()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chemical__list)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        this.title = "Choose a Chemical"
        index = intent.getStringExtra("Index")
        wasteName = intent.getStringExtra("Stream_Name")
        render_ChemList()

    }

    fun render_ChemList(){
        viewManager = LinearLayoutManager(this)
        val tempList: MutableList<chemical> = ArrayList<chemical>()
        tempList.add(chemical("Add A Chemical", null, null, null, null))
        tempList.addAll(dbHelper.retrieveCorrectedTable(wasteName))
        viewAdapter = Chemical_List.ChemCardAdapter(tempList, this)


        recyclerView = findViewById<RecyclerView>(R.id.chemicallist_recycler).apply {
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

        render_ChemList()
    }

    private fun setRecyclerViewItemTouchListener() {

        //1
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                //2
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                //3
                val position = viewHolder.adapterPosition
                val save_intent = Intent()
                val temp: TextView = viewHolder.itemView.findViewById(R.id.chemicalName2) as TextView
                var txt: String = temp.text.toString().substringBefore(':')
                Log.d("Text View: ", txt)
                save_intent.putExtra("Name", txt)
                save_intent.putExtra("Index", index)
                setResult(RESULT_OK, save_intent)
                finish()
            }
        }

        //4
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    class ChemListAdapter(private val myDataset: MutableList<chemical>, private val mContext : Chemical_List) :
        RecyclerView.Adapter<ChemListAdapter.MyViewHolder>() {
        var trigger: Boolean = true
        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): ChemListAdapter.MyViewHolder {
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


            var rank: String = myDataset[position].rank().toString()
            if (rank.equals("N")){rank = "N/A"}
            holder.textView.text = (myDataset[position].name.toString() + ": " + rank)

            var lastDown: Long = 0

            holder.textView.setOnClickListener{
                        if(myDataset[position].name.toString().equals("Add A Chemical")){
                            //to do, add a chemical
                            val intent = Intent(mContext, Edit_Chemical::class.java).apply {
                                putExtra("Name", "Add A Chemical")
                                putExtra("lc50", 0.toFloat())
                                putExtra("ld50", 0.toFloat())
                                putExtra("dermal", 0.toFloat())
                                putExtra("inhalation", 0.toFloat())
                                putExtra("Index", position)
                            }
                            mContext.startActivityForResult(intent,0)
                        }else {
                            val intent = Intent(mContext, Edit_Chemical::class.java).apply {
                                putExtra("Name", myDataset[position].name)
                                putExtra("lc50", myDataset[position].lc50)
                                putExtra("ld50", myDataset[position].ld50)
                                putExtra("dermal", myDataset[position].dermal)
                                putExtra("inhalation", myDataset[position].inhalation)
                                putExtra("Index", position)
                            }
                            mContext.startActivityForResult(intent,0)
                        }
                    }
                }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = myDataset.size
    }

    class ChemCardAdapter(private val myDataset: MutableList<chemical>, private val mContext : Chemical_List) :
        RecyclerView.Adapter<ChemCardAdapter.ViewHolder>() {
        var trigger: Boolean = true

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            //var itemImage: ImageView
            var itemTitle: TextView
            /*var itemConst: TextView
            var itemDetail: EditText*/
            var layout: RelativeLayout

            init {
                //itemImage = itemView.findViewById(R.id.item_image)
                itemTitle = itemView.findViewById(R.id.chemicalName2)
                /*itemConst = itemView.findViewById(R.id.concentrationText)
                itemDetail = itemView.findViewById(R.id.concentrationEdit)*/
                layout = itemView.findViewById(R.id.chemLayout)
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


            var rank: String = myDataset[position].rank().toString()
            if (rank.equals("N")){rank = "N/A"}
            holder.itemTitle.text = (myDataset[position].name.toString() + ": " + rank)
            holder.itemTitle.textSize = 22.toFloat()

            var lastDown: Long = 0

            holder.itemTitle.setOnClickListener{
                if(myDataset[position].name.toString().equals("Add A Chemical")){
                    //to do, add a chemical
                    val intent = Intent(mContext, Edit_Chemical::class.java).apply {
                        putExtra("Name", "Add A Chemical")
                        putExtra("lc50", 0.toFloat())
                        putExtra("ld50", 0.toFloat())
                        putExtra("dermal", 0.toFloat())
                        putExtra("inhalation", 0.toFloat())
                        putExtra("Index", position)
                    }
                    mContext.startActivityForResult(intent,0)
                }else {
                    val intent = Intent(mContext, Edit_Chemical::class.java).apply {
                        putExtra("Name", myDataset[position].name)
                        putExtra("lc50", myDataset[position].lc50)
                        putExtra("ld50", myDataset[position].ld50)
                        putExtra("dermal", myDataset[position].dermal)
                        putExtra("inhalation", myDataset[position].inhalation)
                        putExtra("Index", position)
                    }
                    mContext.startActivityForResult(intent,0)
                }
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = myDataset.size
    }

}
