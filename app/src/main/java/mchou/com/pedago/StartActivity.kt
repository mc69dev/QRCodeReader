package mchou.com.pedago

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        var logo = findViewById<ImageView>(R.id.logo)
        logo.setOnClickListener { Util.show(applicationContext, CameraActivity::class.java) }
        supportActionBar!!.hide()
        loadingAnimation()
    }


    override fun onPause() {
        super.onPause()
        //start!!.animate().alpha(1f).duration = duration.toLong()
        //start!!.setOnClickListener { /*v -> Util.show(applicationContext, NavigationActivity::class.java)*/ }
    }

    private fun loadingAnimation() {
        val loading = findViewById<ProgressBar>(R.id.loading)
        var duration = resources.getInteger(android.R.integer.config_longAnimTime)

        /*start = findViewById(R.id.btn_start)
        start.setAlpha(0f)
        start.setVisibility(View.VISIBLE)*/

        loading.animate()
            .alpha(0f)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loading.setVisibility(View.GONE)
                    display()
                }

                private fun display() {

                    AnimUtil.CircularReveal(findViewById(R.id.tvTitle))
                    AnimUtil.CircularReveal(findViewById(R.id.logo))

                    gotoAfterDelay()

                    //Util.hideSystemUI(getWindow());
                    //Util.customToolbar(getSupportActionBar(), R.drawable.duke);
                    //getSupportActionBar().show();
                }

                private fun gotoAfterDelay() {
                    Handler().postDelayed(
                        {
                            Util.show(applicationContext, CameraActivity::class.java)
                        },
                        duration.toLong()
                    )
                }
            })
    }
}
