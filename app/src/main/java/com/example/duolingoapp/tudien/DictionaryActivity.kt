package com.example.duolingoapp.tudien

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.duolingoapp.MainActivity
import com.example.duolingoapp.R
import com.example.duolingoapp.bocauhoi.QuestionListActivity
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


class DictionaryActivity : AppCompatActivity() {

    private lateinit var home: Button
    private lateinit var screenCurrent: Button
    private lateinit var searchBtn: Button
    private lateinit var searchInput: EditText
    private lateinit var wordTextview: TextView
    private lateinit var phoneticTextview: TextView
    private lateinit var textViewTranslation: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView
    private lateinit var meaningRecyclerView: RecyclerView
    private lateinit var txtUploadImage: TextView

    lateinit var adapter: MeaningAdapter

    private var mediaPlayer: MediaPlayer? = null
    private val api = "AIzaSyDUUUqaikkMGl07aY1inHBLiUva4IlH-fY"
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dictionary)

        home = findViewById(R.id.btnMain)
        screenCurrent = findViewById(R.id.btnListCourse_Subject)
        searchBtn = findViewById(R.id.search_btn)
        searchInput =  findViewById(R.id.search_input)
        wordTextview = findViewById(R.id.word_textview)
        phoneticTextview = findViewById(R.id.phonetic_textview)
        textViewTranslation = findViewById(R.id.textViewTranslation)
        progressBar = findViewById(R.id.progress_bar)
        imageView = findViewById(R.id.image_view)
        meaningRecyclerView = findViewById(R.id.meaning_recycler_view)
        txtUploadImage = findViewById(R.id.txtUploadImage)

        // Nhận extra từ Intent
        var nameScreen = intent.getStringExtra("screenCurrent")
        screenCurrent.text = nameScreen

        Log.d("nameScreen", nameScreen.toString());

        screenCurrent.visibility = if (nameScreen == null) {
            View.GONE
        } else {
            View.VISIBLE
        }

        home.setOnClickListener {
            val intent = Intent(this@DictionaryActivity, MainActivity::class.java)
            startActivity(intent)
        }

        screenCurrent.setOnClickListener {
            val intent = Intent(this@DictionaryActivity, QuestionListActivity::class.java)
            intent.putExtra("screenCurrent", nameScreen)
            startActivity(intent)
        }

        txtUploadImage.setOnClickListener {
            val intent = Intent(this@DictionaryActivity, Recognize_text::class.java)
            intent.putExtra("screenCurrent", nameScreen)
            intent.putExtra("screenBack", "Từ điển")
            startActivity(intent)
        }

        searchBtn.setOnClickListener {
            val word = searchInput.text.toString()
            getMeaning(word)
        }

        imageView.setOnClickListener {
            val word = searchInput.text.toString()
            GlobalScope.launch {
                try {
                    val response = RetrofitInstance.dictionaryApi.getMeaning(word)
                    val wordResult = response.body()?.firstOrNull()
                    wordResult?.let { result ->
                        var audioUrl: String? = null
                        // Duyệt qua danh sách phonetics
                        for (ph in result.phonetics) {
                            // Kiểm tra trường audio có giá trị hay không
                            if (!ph.audio.isNullOrEmpty()) {
                                audioUrl = ph.audio
                                break // Thoát khỏi vòng lặp khi tìm thấy audio có giá trị
                            }
                        }
                        // Kiểm tra xem đã tìm thấy audio có giá trị hay không
                        audioUrl?.let { url ->
                            Log.d("Audio URL", url)
                            playAudio(url)
                        } ?: run {
                            runOnUiThread {
                                Toast.makeText(applicationContext, "No valid audio found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: run {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "No data found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                    }
                }
            }
        }


        adapter = MeaningAdapter(emptyList())
        meaningRecyclerView.layoutManager = LinearLayoutManager(this)
        meaningRecyclerView.adapter = adapter


    }

    private fun playAudio(audioUrl: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

        try {
            mediaPlayer!!.setDataSource(audioUrl)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Toast.makeText(this, "Audio started playing..", Toast.LENGTH_SHORT).show()
    }


    private fun getMeaning(word: String) {
        setInProgress(true)
        GlobalScope.launch {
            try {
                val response = RetrofitInstance.dictionaryApi.getMeaning(word)
                if(response.body()==null){
                    throw (Exception())
                }
                runOnUiThread {
                    setInProgress(false)
                    response.body()?.first()?.let {
                        setUI(it)
                        translateText(it.word) // Dịch từ
                    }
                }
            }catch (e : Exception){
                runOnUiThread{
                    setInProgress(false)
                    Toast.makeText(applicationContext,"Something went wrong",Toast.LENGTH_SHORT).show()
                    clearUI()
                }
            }
        }
    }

    private fun clearUI() {
        wordTextview.text = ""
        phoneticTextview.text = ""
        adapter.updateNewData(emptyList())
        imageView.visibility = View.GONE
        textViewTranslation.text = ""
    }
    private fun translateText(text: String) {
        GlobalScope.launch {
            try {
                val targetLang = "vi" // Mã ngôn ngữ đích (tiếng Việt)
                val sourceLang = "en" // Mã ngôn ngữ nguồn (tiếng Anh)
                val translate = TranslateOptions.newBuilder().setApiKey(api).build().service // Sử dụng khóa API
                val translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targetLang),
                    Translate.TranslateOption.sourceLanguage(sourceLang)
                )
                runOnUiThread {
                    textViewTranslation.text = translation.translatedText // Cập nhật văn bản đã dịch
                    Log.d(TAG, "Translated text: ${translation.translatedText}") // Ghi nhật ký văn bản đã dịch
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Translation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun setUI(response: WordResult) {
        wordTextview.text = response.word
        phoneticTextview.text = response.phonetic
        adapter.updateNewData(response.meanings)

        imageView.visibility = View.VISIBLE
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            searchBtn.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        } else {
            searchBtn.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }
}
