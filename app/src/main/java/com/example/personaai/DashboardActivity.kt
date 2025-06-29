package com.example.personaai

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.personaai.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigasi antar activity
        binding.cardTentangAplikasi.setOnClickListener {
            startActivity(Intent(this, TentangAplikasiActivity::class.java))
        }
        binding.cardSimulasi.setOnClickListener {
            startActivity(Intent(this, SimulasiActivity::class.java))
        }
        binding.cardDataset.setOnClickListener {
            startActivity(Intent(this, DatasetActivity::class.java))
        }
        binding.cardFitur.setOnClickListener {
            startActivity(Intent(this, FiturActivity::class.java))
        }
        binding.cardModel.setOnClickListener {
            startActivity(Intent(this, ModelActivity::class.java))
        }
        binding.cardAboutDev.setOnClickListener {
            startActivity(Intent(this, AboutDevActivity::class.java))
        }
    }
}