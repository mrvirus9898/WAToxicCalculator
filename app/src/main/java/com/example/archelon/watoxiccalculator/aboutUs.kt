package com.example.archelon.watoxiccalculator

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class aboutUs : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        val aboutTxt = findViewById<TextView>(R.id.aboutUs)

        aboutTxt.text = "This app was developed to help workers, and business owners " +
                "navigate Washington's unique environmental laws. It was created by " +
                "Nicholas G Bennett, at the behest of North Seattle Community College, " +
                "as part of the Application's Development program."
    }
}
