package com.patrolshield.presentation.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun QrCodeScanner(
    onScan: (String) -> Unit,
    modifier: Modifier = Modifier,
    isValidQrCode: (String) -> Boolean = { true }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var isScanning by remember { mutableStateOf(true) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    
    val cameraExecutor: ExecutorService by remember { mutableStateOf(Executors.newSingleThreadExecutor()) }
    
    val playSuccessSound = remember {
        {
            try {
                MediaPlayer.create(context, android.media.ToneGenerator.TONE_PROP_BEEP).apply {
                    start()
                    setOnCompletionListener { release() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    val vibrateInvalid = remember {
        {
            try {
                val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val provider = ProcessCameraProvider.getInstance(context).get()
            cameraProvider = provider
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    if (!hasCameraPermission) {
        PermissionRequestContent(
            onRequestPermission = {
                hasCameraPermission = true
            }
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        previewView = this
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    cameraProvider?.let { provider ->
                        bindCameraUseCases(
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            cameraProvider = provider,
                            previewView = view,
                            executor = cameraExecutor,
                            onBarcodeDetected = { barcode ->
                                if (isScanning && barcode.rawValue != null) {
                                    val code = barcode.rawValue!!
                                    if (code != lastScannedCode) {
                                        if (isValidQrCode(code)) {
                                            lastScannedCode = code
                                            playSuccessSound()
                                            onScan(code)
                                            isScanning = false
                                            lastScannedCode = null
                                        } else {
                                            vibrateInvalid()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            )
            
            ScanningOverlay(
                modifier = Modifier.fillMaxSize(),
                primaryColor = MaterialTheme.colorScheme.primary
            )
            
            if (!isScanning) {
                LaunchedEffect(Unit) {
                    delay(2000)
                    isScanning = true
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📷",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Please grant camera permission to scan QR codes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun ScanningOverlay(modifier: Modifier = Modifier, primaryColor: Color = Color.Blue) {
    Canvas(modifier = modifier) {
        val size = this.size
        val cornerLength = size.width * 0.1f
        val strokeWidth = 4.dp.toPx()
        val cornerRadius = 8.dp.toPx()
        
        val centerX = size.width / 2
        val centerY = size.height / 2
        val boxWidth = size.width * 0.6f
        val boxHeight = size.height * 0.4f
        val left = centerX - boxWidth / 2
        val right = centerX + boxWidth / 2
        val top = centerY - boxHeight / 2
        val bottom = centerY + boxHeight / 2
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = strokeWidth)
        )
        
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(left, top + cornerLength)
            lineTo(left, top + cornerRadius)
            quadraticBezierTo(left, top, left + cornerRadius, top)
            lineTo(left + cornerLength, top)
        }
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = strokeWidth * 1.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        val pathTopRight = androidx.compose.ui.graphics.Path().apply {
            moveTo(right - cornerLength, top)
            lineTo(right - cornerRadius, top)
            quadraticBezierTo(right, top, right, top + cornerRadius)
            lineTo(right, top + cornerLength)
        }
        drawPath(
            path = pathTopRight,
            color = primaryColor,
            style = Stroke(width = strokeWidth * 1.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        val pathBottomLeft = androidx.compose.ui.graphics.Path().apply {
            moveTo(left, bottom - cornerLength)
            lineTo(left, bottom - cornerRadius)
            quadraticBezierTo(left, bottom, left + cornerRadius, bottom)
            lineTo(left + cornerLength, bottom)
        }
        drawPath(
            path = pathBottomLeft,
            color = primaryColor,
            style = Stroke(width = strokeWidth * 1.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        val pathBottomRight = androidx.compose.ui.graphics.Path().apply {
            moveTo(right - cornerLength, bottom)
            lineTo(right - cornerRadius, bottom)
            quadraticBezierTo(right, bottom, right, bottom - cornerRadius)
            lineTo(right, bottom - cornerLength)
        }
        drawPath(
            path = pathBottomRight,
            color = primaryColor,
            style = Stroke(width = strokeWidth * 1.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    executor: ExecutorService,
    onBarcodeDetected: (Barcode) -> Unit
) {
    val preview = Preview.Builder()
        .build()
        .also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
    
    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(executor) { imageProxy ->
                processImageProxy(imageProxy, context, onBarcodeDetected)
            }
        }
    
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )
    } catch (exc: Exception) {
        exc.printStackTrace()
    }
}

private fun processImageProxy(
    imageProxy: ImageProxy,
    context: Context,
    onBarcodeDetected: (Barcode) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        
        val scanner = BarcodeScanning.getClient(options)
        
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    onBarcodeDetected(barcode)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
