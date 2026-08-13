package microdata.absensi

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import microdata.absensi.ui.auth.LoginActivity
import microdata.absensi.ui.home.HomeActivity
import microdata.absensi.utils.UtilsSession

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logo = findViewById<ImageView>(R.id.iv_logo)
        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_logo))

        val tvMicrodata = findViewById<TextView>(R.id.tv_microdata)
        tvMicrodata.post {
            val paint = tvMicrodata.paint
            val textWidth = paint.measureText(tvMicrodata.text.toString())
            val startX = (tvMicrodata.width - textWidth) / 2f
            paint.shader = LinearGradient(
                startX,
                0f,
                startX + textWidth,
                0f,
                intArrayOf(Color.parseColor("#FF0071B5"), Color.parseColor("#FFFF0000")),
                null,
                Shader.TileMode.CLAMP
            )
            tvMicrodata.invalidate()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (UtilsSession(this).isLoggedIn()) {
                Intent(this, HomeActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 1500)
    }
}