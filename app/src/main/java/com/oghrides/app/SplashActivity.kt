package com.oghrides.app

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splashLogo)
        val title = findViewById<TextView>(R.id.splashTitle)
        val subtitle = findViewById<TextView>(R.id.splashSubtitle)

        logo.scaleX = 0f
        logo.scaleY = 0f
        logo.alpha = 0f
        title.alpha = 0f
        subtitle.alpha = 0f

        val logoScale = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1.1f, 1f).apply {
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
        }

        val logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1.1f, 1f).apply {
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
        }

        val logoFade = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f).apply {
            duration = 600
        }

        val titleFade = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 300
        }

        val subtitleFade = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 500
        }

        val animSet = AnimatorSet().apply {
            playTogether(logoScale, logoScaleY, logoFade, titleFade, subtitleFade)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}
                override fun onAnimationEnd(p0: Animator) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}
            })
            startDelay = 400
            duration = 1800
        }
        animSet.start()
    }
}
