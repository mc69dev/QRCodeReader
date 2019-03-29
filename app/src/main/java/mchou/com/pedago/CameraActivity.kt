package mchou.com.pedago

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_camera.*
import android.graphics.SurfaceTexture
import android.view.Surface
import java.util.*
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraMetadata
import android.util.Size;
import android.view.TextureView.SurfaceTextureListener
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.CameraDevice
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CameraActivity : AppCompatActivity() {
    private val TAG = "tests"
    private val REQUEST_CAMERA_PERMISSION = 200

    var cameraId: String? = null
    var cameraDevice: CameraDevice? = null
    var imageDimension: Size? = null

    var cameraCaptureSessions: CameraCaptureSession? = null
    var captureRequest: CaptureRequest? = null
    var captureRequestBuilder: CaptureRequest.Builder? = null

    var capture_image: File? = null

    var mBackgroundHandler: Handler? = null
    var mBackgroundThread: HandlerThread? = null

    /*var ORIENTATIONS :HashMap<Int,Int> = HashMap<Int,Int>()
    companion object {
        init {

            ORIENTATIONS.put(Surface.ROTATION_0, 90)
            ORIENTATIONS.put(Surface.ROTATION_90, 0)
            ORIENTATIONS.put(Surface.ROTATION_180, 270)
            ORIENTATIONS.put(Surface.ROTATION_270, 180)
        }
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //check Camera
        ifNoCamBye()

        btn_capture.bringToFront()
        btn_capture.setOnClickListener { takePicture() }
        textureView.surfaceTextureListener=textureViewListener

    }
    fun log(message :String){
        Log.i(TAG , message)
    }

    /**
     * Camera(2) Listeners
     */
    private val textureViewListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
            log("onSurfaceTextureSizeChanged")
        }
        override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
            // log("onSurfaceTextureUpdated")
        }
        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
            log("onSurfaceTextureDestroyed!")
            return true
        }
        override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
            log("onSurfaceTextureAvailable!")
            openCamera()
        }
    }
    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            log("CameraDevice.StateCallback - onOpened : Camera (Device) = $cameraDevice")
            createCameraPreview()
        }
        override fun onDisconnected(camera: CameraDevice) {
            closeCamera()
            log("CameraDevice.StateCallback - onDisconnected : Camera (Device) = $cameraDevice")
        }
        override fun onError(camera: CameraDevice, error: Int) {
            closeCamera()
            log("CameraDevice.StateCallback - onError : Camera (Device) = $cameraDevice")
        }
    }
    private val captureListener = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted( session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult ) {
            super.onCaptureCompleted(session, request, result)
            log("CameraCaptureSession.CaptureCallback - onCaptureCompleted! file : $capture_image")
            Toast.makeText(this@CameraActivity, "Saved: $capture_image", Toast.LENGTH_LONG).show()
            createCameraPreview()
        }
    }

     /**
     * Background Tasks
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }
    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    /**
     * Hardware Check!
     */
    fun ifNoCamBye() {
        if(!checkCameraHardware(this@CameraActivity)) {
            Toast.makeText(this@CameraActivity, "No Camera! :(", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    /**
     * Camera(2) Functions
     */
    fun openCamera()  {
      val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            if(manager.cameraIdList.isEmpty())
                return

            cameraId = manager.cameraIdList[0]
            log("Camera ID (0) = $cameraId")

            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!

            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            log("imageDimension :"+imageDimension!!.width+"x"+imageDimension!!.height)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@CameraActivity,
                    arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(cameraId, cameraStateCallback , null)
        }
        catch(e: Throwable){
            run {e.printStackTrace()}
        }
    }
    fun closeCamera()  {
        cameraDevice!!.close()
        cameraDevice = null
        log("Camera (Device) closed!")
    }
    fun createCameraPreview(){
        log("createCameraPreview..")
        try {
            val texture = textureView.surfaceTexture!!
            //log("SurfaceTexture : $texture")

            /* if(null==imageDimension)
            texture.setDefaultBufferSize(300, 350)
            else*/
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)

            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)

            cameraDevice!!.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    if (null == cameraDevice)
                        return

                    log("createCameraPreview - session ready : $cameraCaptureSession")
                    cameraCaptureSessions = cameraCaptureSession
                    updatePreview()
                }
                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(this@CameraActivity, "Configuration change", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            log("CameraAccessException - e : $e")
        }
    }

    fun updatePreview() {
        if (null == cameraDevice) {
            log("updatePreview error!")
            return
        }

        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions!!.setRepeatingRequest(
                captureRequestBuilder!!.build(), null,
                mBackgroundHandler)
        } catch (e: CameraAccessException) {
            log("CameraAccessException - e : $e")
        }
    }

    private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1
    fun takePicture() {
        if (null == cameraDevice) {
            log("cameraDevice is null")
            return
        }
        log("takePicture..")

        val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
                    this@CameraActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
        {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(this@CameraActivity,
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION)
        }

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.getId())
            var jpegSizes: Array<Size>? = null
            if (characteristics != null) {
                jpegSizes =
                    characteristics.get<StreamConfigurationMap>(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(
                        ImageFormat.JPEG
                    )
            }
            var width = 640
            var height = 480

            /*if (jpegSizes != null && 0 < jpegSizes.size) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }*/

            val image_format = ImageFormat.JPEG //ImageFormat.RAW_SENSOR
            val reader = ImageReader.newInstance(width, height, image_format, 1)

            val outputSurfaces = ArrayList<Surface>(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView.surfaceTexture))

            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            // Orientation
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, Surface.ROTATION_0)// ORIENTATIONS.get(rotation))

            var formatted =""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")
                formatted = current.format(formatter)
            }

            val filename="/pic_$formatted.jpg"
            var file = File( Environment.getExternalStorageDirectory().toString()+filename)

            val readerListener = object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {

                    log("onImageAvailable..")
                    var image: Image? = null
                    try {
                        image = reader.acquireLatestImage()
                        val buffer = image!!.planes[0].getBuffer()
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)

                        log("onImageAvailable..call save..")
                        save(bytes)
                    } catch (e: FileNotFoundException) {
                        log("onImageAvailable..error 1 : $e")
                    } catch (e: IOException) {
                        log("onImageAvailable..error 2 : $e")
                    } finally {
                        if (image != null) {
                            image!!.close()
                        }
                    }
                }

                //@Throws(IOException::class)
                private fun save(bytes: ByteArray) {

                    log("save..@bytes.size")
                    var output: OutputStream? = null
                    try {
                        output = FileOutputStream(file)
                        log("save..output : $output")

                        output!!.write(bytes)
                        log("save..OK")
                    }catch (e:Exception){
                        log("Exception :$e")
                    }
                    finally {
                        if (null != output) {
                            output!!.close()
                        }
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)

            cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        log("CameraAccessException..error : $e")
                    }
                }
                override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        log("CameraAccessException..error : $e")
                    }
                }
            }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            log("CameraAccessException..error : $e")
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this@CameraActivity, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        log("onResume")

        startBackgroundThread()
        if (textureView.isAvailable())
            openCamera()
        else
            textureView.surfaceTextureListener=textureViewListener
    }
    override fun onPause() {
        log("onPause")
        stopBackgroundThread()
        super.onPause()
    }



}



