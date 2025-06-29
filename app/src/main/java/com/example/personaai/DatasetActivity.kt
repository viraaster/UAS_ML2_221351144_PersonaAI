package com.example.personaai

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.BufferedReader
import java.io.InputStreamReader

class DatasetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataset)

        val tableContainer = findViewById<LinearLayout>(R.id.tableContainer)
        val inputStream = assets.open("data.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines().take(11)

        if (lines.isEmpty()) return

        val headers = lines[0].split(",")
        val rows = lines.drop(1)

        val context = this
        val headerColor = ContextCompat.getColor(context, R.color.dataset) // warna header
        val textColor = ContextCompat.getColor(context, R.color.black)
        val cardMargin = 12
        val cellPadding = 12

        tableContainer.orientation = LinearLayout.HORIZONTAL

        for (colIndex in headers.indices) {
            val cardView = CardView(context).apply {
                radius = 18f
                cardElevation = 6f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(cardMargin, cardMargin, cardMargin, cardMargin)
                }
            }

            val columnLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // Header TextView
            val headerTextView = TextView(context).apply {
                text = headers[colIndex].trim()
                setTypeface(null, Typeface.BOLD)
                setTextColor(textColor)
                gravity = Gravity.CENTER
                setPadding(cellPadding, cellPadding, cellPadding, cellPadding)
                setSingleLine(true)
                ellipsize = TextUtils.TruncateAt.END
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(headerColor)
                    setTextColor(ContextCompat.getColor(context, R.color.text))
                }
            }

            columnLayout.addView(headerTextView)

            // Isi Baris
            for (row in rows) {
                val cells = row.split(",")
                val value = if (colIndex < cells.size) cells[colIndex].trim() else ""
                val cellText = TextView(context).apply {
                    text = value
                    setTextColor(textColor)
                    gravity = Gravity.CENTER
                    setPadding(cellPadding, 8, cellPadding, 8)
                }
                columnLayout.addView(cellText)
            }

            cardView.addView(columnLayout)
            tableContainer.addView(cardView)
        }
    }
}