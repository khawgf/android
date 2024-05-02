package com.example.duolingoapp.tudien

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.duolingoapp.MainActivity
import com.example.duolingoapp.R
import com.example.duolingoapp.bocauhoi.QuestionListActivity
import com.google.cloud.translate.TranslateOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.cloud.translate.Translate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Recognize_text : AppCompatActivity() {
    lateinit var img : ImageView
    lateinit var btnSelectImage : Button
    lateinit var btnRecognizeText : Button
    lateinit var txtDisplay : TextView
    lateinit var txtBack : TextView
    private val api = "AIzaSyDUUUqaikkMGl07aY1inHBLiUva4IlH-fY"
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognize_text)
        img = findViewById(R.id.img)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnRecognizeText = findViewById(R.id.btnRecognize)
        txtDisplay = findViewById(R.id.txtdisplay)
        txtBack = findViewById(R.id.txtBack_RecoText)

        // Nhận extra từ Intent
        val nameScreen = intent.getStringExtra("screenCurrent")
        val screenBack = intent.getStringExtra("screenBack")

        // Tạo scroll cho textview
        txtDisplay.movementMethod = ScrollingMovementMethod() //java code: txtDisplay.setMovementMethod(new ScrollingMovementMethod());

        txtBack.setOnClickListener {
            if (screenBack==null){
                if (nameScreen == null) {
                    val intent = Intent(this@Recognize_text, MainActivity::class.java)
                    startActivity(intent)
                } else{
                    val intent = Intent(this@Recognize_text, DictionaryActivity::class.java)
                    intent.putExtra("screenCurrent", nameScreen)
                    startActivity(intent)
                }
            } else if (screenBack.equals("Từ điển")){
                val intent = Intent(this@Recognize_text, DictionaryActivity::class.java)
                intent.putExtra("screenCurrent", nameScreen)
                startActivity(intent)
            }
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent,"Select Image"),1)
            //txtDisplay.visibility = View.GONE
            txtDisplay?.setText("")
            img.visibility = View.VISIBLE
            btnSelectImage.visibility = View.GONE
            Thread.sleep(3000)
            btnRecognizeText.visibility= View.VISIBLE

        }

        btnRecognizeText.setOnClickListener {
            val bitmap = (img.drawable as BitmapDrawable).bitmap
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val recognize = FirebaseVision.getInstance().onDeviceTextRecognizer
            txtDisplay?.setText("Waiting ...")
            txtDisplay.visibility = View.VISIBLE
            //img.visibility = View.GONE
            recognize.processImage(image).addOnSuccessListener { firebaseVisionText ->
                RecognizeText(firebaseVisionText)
                txtDisplay.visibility = View.VISIBLE
                btnSelectImage.visibility = View.VISIBLE
                btnRecognizeText.visibility = View.GONE
            }.addOnFailureListener{
                txtDisplay?.setText("Failed to Recognize Text")
            }
        }
    }

    private fun RecognizeText(resulttext: FirebaseVisionText) {
        if (resulttext.textBlocks.size == 0) {
            txtDisplay?.setText("Data Not Found")
            return
        }
        val originalText = StringBuilder()
        for (block in resulttext.textBlocks) {
            originalText.append(block.text).append("\n")
        }
        translateText(originalText.toString())

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
                    txtDisplay.text = translation.translatedText // Hiển thị văn bản đã dịch
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Translation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            if(data != null){
                img.setImageURI(data!!.data)
            }
        }
    }
}