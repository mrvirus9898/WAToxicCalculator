package com.example.archelon.watoxiccalculator

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class privacy : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        val privacyTxt = findViewById<TextView>(R.id.privacyPolicy)

        privacyTxt.text = "This app does not gather information on it's users, nor does it " +
                "gather information on it's users waste. Information gathered is only for logging and debug " +
                "purposes. However, such privacy comes with a cost. If this app is deleted, or removed from " +
                "your device than you will lose all your waste stream and custom chemical information."
    }
}
