package com.example.archelon.watoxiccalculator

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class Edit_Chemical : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit__chemical)

        val input_name = intent.getStringExtra("Name")
        val input_lc50 = intent.getFloatExtra("lc50", 0.0.toFloat())
        val input_ld50 = intent.getFloatExtra("ld50", 0.0.toFloat())
        val input_dermal = intent.getFloatExtra("dermal", 0.0.toFloat())
        val input_inhalation = intent.getFloatExtra("inhalation", 0.0.toFloat())
        var input_index = intent.getIntExtra("Index", 0)

        val name: EditText = findViewById(R.id.name)
        val lc50: EditText = findViewById(R.id.lc50_input)
        val ld50: EditText = findViewById(R.id.ld50_input)
        val dermal: EditText = findViewById(R.id.dermal_input)
        val inhalation: EditText = findViewById(R.id.inhalation_input)

        /*val label: TextView = findViewById(R.id.textView5)
        label.text = input_name*/
        name.setText(input_name)
        lc50.setText(input_lc50.toString())
        ld50.setText(input_ld50.toString())
        dermal.setText(input_dermal.toString())
        inhalation.setText(input_inhalation.toString())



        val savebutton: Button = findViewById(R.id.save)
        savebutton.setOnClickListener {
            val save_intent = Intent()
            val dbHelper = FeedReaderDbHelper(this)

            val input_chemical: chemical = chemical(name.text.toString(),
                lc50.text.toString().toFloatOrNull(),
                ld50.text.toString().toFloatOrNull(),
                dermal.text.toString().toFloatOrNull(),
                inhalation.text.toString().toFloatOrNull()
            )

            if(dbHelper.updateChemicalEntry(input_chemical, input_name)){

            }
            else if(name.text.toString() != null) {
                dbHelper.addToChemDB(input_chemical)
            }
            setResult(RESULT_OK, save_intent)
            finish()
        }
    }

}
