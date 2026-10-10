package com.ijtc.kiosklauncher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusView: TextView
    private lateinit var openButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val titleView = TextView(this).apply {
            text = getString(R.string.title)
            textSize = 28f
            gravity = Gravity.CENTER
        }
        layout.addView(
            titleView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        statusView = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }
        layout.addView(
            statusView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        openButton = Button(this).apply {
            text = getString(R.string.open_app)
            setOnClickListener { launchRubelApp() }
        }
        layout.addView(
            openButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        updateAppAvailability()
    }

    private fun updateAppAvailability() {
        val launchIntent = packageManager.getLaunchIntentForPackage(TARGET_PACKAGE)
        openButton.isEnabled = launchIntent != null
        statusView.setText(
            if (launchIntent != null) R.string.app_available else R.string.app_missing
        )
    }

    private fun launchRubelApp() {
        val launchIntent = packageManager.getLaunchIntentForPackage(TARGET_PACKAGE)
        if (launchIntent == null) {
            statusView.setText(R.string.app_missing)
            openButton.isEnabled = false
            return
        }

        try {
            startActivity(launchIntent)
        } catch (_: ActivityNotFoundException) {
            statusView.setText(R.string.app_unavailable)
            openButton.isEnabled = false
        }
    }

    private companion object {
        const val TARGET_PACKAGE = "com.ijtc.rubelapp"
    }
}
