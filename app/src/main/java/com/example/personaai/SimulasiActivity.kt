package com.example.personaai

import android.animation.ObjectAnimator
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.personaai.databinding.ActivitySimulasiBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimulasiActivity : AppCompatActivity() {
    private lateinit var interpreter: Interpreter
    private val modelPath = "predict_type_personality.tflite"

    // Views
    private lateinit var seekBarAge: SeekBar
    private lateinit var tvAgeValue: TextView
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var seekBarIntroversion: SeekBar
    private lateinit var seekBarThinking: SeekBar
    private lateinit var seekBarSensing: SeekBar
    private lateinit var seekBarJudging: SeekBar
    private lateinit var spinnerInterest: Spinner
    private lateinit var btnPredict: Button

    private lateinit var cardResult: CardView
    private lateinit var tvMbtiResult: TextView
    private lateinit var tvMbtiDesc: TextView
    private lateinit var tvMbtiTokoh: TextView
    private lateinit var tvMbtiPasangan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulasi)

        interpreter = Interpreter(loadModelFile())
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        seekBarAge = findViewById(R.id.seekBarAge)
        tvAgeValue = findViewById(R.id.tvAgeValue)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        seekBarIntroversion = findViewById(R.id.seekBarIntroversion)
        seekBarThinking = findViewById(R.id.seekBarThingking)
        seekBarSensing = findViewById(R.id.seekBarSensing)
        seekBarJudging = findViewById(R.id.seekBarJudging)
        spinnerInterest = findViewById(R.id.spinnerInterest)
        btnPredict = findViewById(R.id.btnPredict)

        cardResult = findViewById(R.id.cardResult)
        tvMbtiResult = findViewById(R.id.tvMbtiResult)
        tvMbtiDesc = findViewById(R.id.tvMbtiDesc)
        tvMbtiTokoh = findViewById(R.id.tvMbtiTokoh)
        tvMbtiPasangan = findViewById(R.id.tvMbtiPasangan)

        // Spinner data
        val interestOptions = arrayOf("Unknown", "Arts", "Sports", "Technology", "Others")
        spinnerInterest.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, interestOptions)
    }

    private fun setupListeners() {
        seekBarAge.setOnSeekBarChangeListener(getSeekBarListener(tvAgeValue, " years"))
        seekBarIntroversion.setOnSeekBarChangeListener(getSeekBarListener(findViewById(R.id.tvIntroversionValue), ""))
        seekBarThinking.setOnSeekBarChangeListener(getSeekBarListener(findViewById(R.id.tvThingkingValue), ""))
        seekBarSensing.setOnSeekBarChangeListener(getSeekBarListener(findViewById(R.id.tvSensingValue), ""))
        seekBarJudging.setOnSeekBarChangeListener(getSeekBarListener(findViewById(R.id.tvJudgingValue), ""))

        btnPredict.setOnClickListener {
            val inputArray = buildInputArray()
            val output = Array(1) { FloatArray(16) } // Output class 16 jenis MBTI
            interpreter.run(inputArray, output)
            val resultIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
            val mbti = mbtiTypes[resultIdx]

            showResult(mbti)
        }
    }

    private fun buildInputArray(): Array<FloatArray> {
        val age = seekBarAge.progress.toFloat()
        val gender = if (findViewById<RadioButton>(R.id.rbMale).isChecked) 0f else 1f
        val intro = seekBarIntroversion.progress.toFloat()
        val think = seekBarThinking.progress.toFloat()
        val sense = seekBarSensing.progress.toFloat()
        val judge = seekBarJudging.progress.toFloat()

        val interestOneHot = FloatArray(5) // Unknown, Arts, Sports, Tech, Others
        when (spinnerInterest.selectedItem.toString()) {
            "Unknown" -> interestOneHot[0] = 1f
            "Arts" -> interestOneHot[1] = 1f
            "Sports" -> interestOneHot[2] = 1f
            "Technology" -> interestOneHot[3] = 1f
            "Others" -> interestOneHot[4] = 1f
        }

        return arrayOf(
            floatArrayOf(age, gender, intro, sense, think, judge) + interestOneHot
        )
    }

    private fun showResult(mbti: String) {
        val desc = mbtiDescriptions[mbti] ?: "-"
        val tokoh = mbtiFamousPeople[mbti] ?: "-"
        val pasangan = mbtiMatches[mbti] ?: "-"

        tvMbtiResult.text = "MBTI kamu adalah: $mbti"
        tvMbtiDesc.text = "📝 Deskripsi: $desc"
        tvMbtiTokoh.text = "💃 Tokoh Terkenal: $tokoh"
        tvMbtiPasangan.text = "💝 Pasangan Cocok: $pasangan"

        cardResult.visibility = View.VISIBLE
        cardResult.alpha = 0f
        ObjectAnimator.ofFloat(cardResult, View.ALPHA, 0f, 1f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun getSeekBarListener(textView: TextView, suffix: String): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = "$progress$suffix"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private val mbtiTypes = arrayOf("ISTJ","ISFJ","INFJ","INTJ","ISTP","ISFP","INFP","INTP",
        "ESTP","ESFP","ENFP","ENTP","ESTJ","ESFJ","ENFJ","ENTJ")

    private val mbtiDescriptions = mapOf(
        "ISTJ" to "Tegas, logis, bertanggung jawab, dan berorientasi pada detail.",
        "ISFJ" to "Setia, peduli, dan suka membantu orang lain dengan cara yang praktis.",
        "INFJ" to "Penuh visi, idealis, dan memiliki komitmen kuat pada nilai-nilai.",
        "INTJ" to "Logis, strategis, independen dan fokus pada tujuan.",
        "ISTP" to "Praktis, suka bereksperimen, dan pemecah masalah yang tangguh.",
        "ISFP" to "Tenang, sensitif, artistik, dan menghargai kebebasan pribadi.",
        "INFP" to "Idealistik, penuh empati, dan berprinsip.",
        "INTP" to "Penasaran, logis, dan senang menganalisis ide atau sistem.",
        "ESTP" to "Spontan, energik, suka tantangan, dan berpikir cepat.",
        "ESFP" to "Ceria, ramah, dan suka jadi pusat perhatian.",
        "ENFP" to "Antusias, kreatif, dan suka berpetualang.",
        "ENTP" to "Cerdas, penuh ide, suka debat, dan sangat inovatif.",
        "ESTJ" to "Terorganisir, tegas, dan sangat menghargai struktur.",
        "ESFJ" to "Hangat, peduli, dan suka menjaga keharmonisan sosial.",
        "ENFJ" to "Karismatik, empatik, dan sangat mendukung orang lain.",
        "ENTJ" to "Pemimpin alami, tegas, dan punya visi besar untuk masa depan."
    )

    private val mbtiFamousPeople = mapOf(
        "ISTJ" to "Angela Merkel, George Washington",
        "ISFJ" to "Beyoncé, Mother Teresa",
        "INFJ" to "Martin Luther King Jr., Nicole Kidman",
        "INTJ" to "Elon Musk, Stephen Hawking",
        "ISTP" to "Clint Eastwood, Bruce Lee",
        "ISFP" to "Britney Spears, Michael Jackson",
        "INFP" to "William Shakespeare, Audrey Hepburn",
        "INTP" to "Albert Einstein, Bill Gates",
        "ESTP" to "Ernest Hemingway, Madonna",
        "ESFP" to "Elvis Presley, Marilyn Monroe",
        "ENFP" to "Robert Downey Jr., Robin Williams",
        "ENTP" to "Mark Twain, Thomas Edison",
        "ESTJ" to "Emma Watson, Judge Judy",
        "ESFJ" to "Jennifer Lopez, Hugh Jackman",
        "ENFJ" to "Oprah Winfrey, Barack Obama",
        "ENTJ" to "Steve Jobs, Gordon Ramsay"
    )

    private val mbtiMatches = mapOf(
        "ISTJ" to "ESFP, ESTP",
        "ISFJ" to "ESFP, ESTP",
        "INFJ" to "ENFP, ENTP",
        "INTJ" to "ENFP, ENTP",
        "ISTP" to "ESFJ, ESTJ",
        "ISFP" to "ENFJ, ESFJ",
        "INFP" to "ENFJ, ENTJ",
        "INTP" to "ENTJ, ENFJ",
        "ESTP" to "ISFJ, ISTJ",
        "ESFP" to "ISFJ, ISTJ",
        "ENFP" to "INFJ, INTJ",
        "ENTP" to "INFJ, INTJ",
        "ESTJ" to "ISFP, INFP",
        "ESFJ" to "ISFP, INFP",
        "ENFJ" to "INFP, ISFP",
        "ENTJ" to "INTP, INFP"
    )
}