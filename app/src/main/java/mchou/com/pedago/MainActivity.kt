package mchou.com.pedago

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.content_main.*
import com.google.android.gms.vision.Frame
import android.graphics.drawable.BitmapDrawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.os.Build
import android.text.Spanned
import android.widget.ImageView
import android.widget.Toast

/**
 * TODO : check permissions!
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "tests"
    private val CAMERA_REQUEST_CODE = 2608

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)

        /* fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }*/

        //var btn = findViewById<Button>(R.id.btnProcess)
        //btn.setOnClickListener({view -> fun onClick(v: View) = setCodeDetection()})
        btnProcess.setOnClickListener { view ->
            loadCodeBar()
        }
    }

    private fun loadCodeBar() {
        capture_camera()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //if(data!=null)
            display_image(data!!)
        }
    }

    private fun capture_camera() {
        /*if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }*/

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun display_image(data: Intent) {
        val photo = data.extras!!.get("data") as Bitmap
        imgCode.setImageBitmap(photo)

        processCodeBar()
    }
    private fun processCodeBar() {
        Log.i("Test","process.. ")
        if(!checkCodeDetection())
            return

        val detector = BarcodeDetector.Builder(applicationContext)
            .setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE)
            .build()

        val codeBitmap = (imgCode.drawable as BitmapDrawable).bitmap  //((BitmapDrawable)imgCode.getDrawable()).getBitmap()

        try {
            val frame = Frame.Builder().setBitmap(codeBitmap).build()
            val barcodes = detector.detect(frame)

            val thisCode = barcodes.valueAt(0)

            var code_link = thisCode.rawValue

            var i=code_link.indexOf("http",0,true)
            var html = code_link.substring(i)
            Log.i(TAG, html)

            txtContent.movementMethod = LinkMovementMethod.getInstance()
            txtContent.text =  "<a href='"+html+"'>Code!</a>".toSpanned()

        }catch (e : Exception){
            Toast.makeText(applicationContext, "BarCode not correct!",Toast.LENGTH_LONG).show()
        }

        //fromHtml("<a href=\"" + code_link + "\">" + code_link + "</a>")
    }

    fun String.toSpanned(): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(this)
        }
    }

   /* fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        else Html.fromHtml(html)
    }*/
    private fun checkCodeDetection(): Boolean {
        val detector = BarcodeDetector.Builder(applicationContext)
            .setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE)
            .build()

        if (!detector.isOperational()) {
            txtContent.text = "Could not set up the detector!"
            return false
        }else
            txtContent.text = "the detector is operational!"
        return true
    }


/*    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }*/
}
