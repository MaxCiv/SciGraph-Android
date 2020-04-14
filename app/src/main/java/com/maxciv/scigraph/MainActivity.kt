package com.maxciv.scigraph

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/**
 * @author maxim.oleynik
 * @since 14.04.2020
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
