package com.example.ui.components

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.local.LegalNoteEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonGreenPrimary
import com.example.ui.theme.PriceTagGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun DocumentScannerModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onDocumentDigitized: (title: String, content: String, category: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    var isScanningProcess by remember { mutableStateOf(false) }
    var scannedResultTitle by remember { mutableStateOf("") }
    var scannedResultCategory by remember { mutableStateOf("Contract") }
    var scannedResultText by remember { mutableStateOf("") }
    var hasScannedOutput by remember { mutableStateOf(false) }

    val categories = listOf("Contract", "Employment", "Property / Rent", "Notice", "Receipt / Invoice", "General Legal")

    // Laser scanning animation line
    val scanLineY = remember { Animatable(0f) }

    LaunchedEffect(isScanningProcess) {
        if (isScanningProcess) {
            scanLineY.snapTo(0f)
            scanLineY.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(24.dp))
                .testTag("document_scanner_modal"),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B3828)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = NeonGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Legal Document Scanner",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = PriceTagGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Camera OCR & Instant Digitization",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_scanner_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (hasScannedOutput) {
                    // Review & Edit Scanned Legal Summary Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B3828))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PriceTagGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Document Successfully Digitized!",
                                    color = PriceTagGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "Document Title", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = scannedResultTitle,
                            onValueChange = { scannedResultTitle = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreenPrimary,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scanned_title_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Category Tag", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                val isSelected = scannedResultCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonGreenPrimary else DarkSurface)
                                        .border(1.dp, if (isSelected) NeonGreenPrimary else DarkCardBorder, RoundedCornerShape(8.dp))
                                        .clickable { scannedResultCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Extracted Legal Clauses & Summary", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = scannedResultText,
                            onValueChange = { scannedResultText = it },
                            minLines = 7,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreenPrimary,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scanned_text_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    hasScannedOutput = false
                                },
                                modifier = Modifier.testTag("rescan_document_button")
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rescan", color = TextMuted)
                            }

                            Button(
                                onClick = {
                                    if (scannedResultTitle.isNotBlank() && scannedResultText.isNotBlank()) {
                                        onDocumentDigitized(scannedResultTitle, scannedResultText, scannedResultCategory)
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGreenPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("save_scanned_note_button")
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save & Attach to Case", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Live Camera Finder Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                    ) {
                        if (hasCameraPermission) {
                            // CameraX Preview Feed
                            var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    }

                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        val cameraProvider = cameraProviderFuture.get()

                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }

                                        val imgCap = ImageCapture.Builder()
                                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                            .build()
                                        imageCapture = imgCap

                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                        try {
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                ctx as androidx.lifecycle.LifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imgCap
                                            )
                                        } catch (e: Exception) {
                                            Log.e("DocumentScannerModal", "Camera binding failed", e)
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))

                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Document Viewfinder Overlay Box & Corner Guides
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                val frameWidth = width * 0.85f
                                val frameHeight = height * 0.70f
                                val left = (width - frameWidth) / 2f
                                val top = (height - frameHeight) / 2f

                                // Darken background outside frame
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.45f)
                                )

                                // Document frame outline
                                drawRoundRect(
                                    color = if (isScanningProcess) NeonGreenPrimary else Color.White,
                                    topLeft = Offset(left, top),
                                    size = Size(frameWidth, frameHeight),
                                    cornerRadius = CornerRadius(12.dp.toPx()),
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                // Laser Scanning Line animation
                                if (isScanningProcess) {
                                    val currentY = top + (frameHeight * scanLineY.value)
                                    drawLine(
                                        color = NeonGreenPrimary,
                                        start = Offset(left + 8.dp.toPx(), currentY),
                                        end = Offset(left + frameWidth - 8.dp.toPx(), currentY),
                                        strokeWidth = 4.dp.toPx()
                                    )
                                }
                            }

                            // Capture trigger button
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 20.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (isScanningProcess) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = NeonGreenPrimary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Digitizing & Performing OCR...", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Capture Button
                                        Button(
                                            onClick = {
                                                isScanningProcess = true
                                                scope.launch {
                                                    delay(1800) // Simulate fast camera capture + Gemini OCR extraction
                                                    val now = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                                                    scannedResultTitle = "Scanned Contract Summary ($now)"
                                                    scannedResultCategory = "Contract"
                                                    scannedResultText = """
                                                        • Document Type: Service Agreement / Employment Contract
                                                        • Digitized Date: $now
                                                        • Key Obligations: Standard confidentiality clause, 30-day termination notice requirement, non-solicitation term for 12 months.
                                                        • Monetary Terms: $120/hr advisory consultation rate specified.
                                                        • Extracted Action Item: Review dispute resolution and arbitration jurisdiction before signing.
                                                    """.trimIndent()
                                                    isScanningProcess = false
                                                    hasScannedOutput = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = NeonGreenPrimary,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(28.dp),
                                            modifier = Modifier
                                                .height(50.dp)
                                                .testTag("capture_document_button")
                                        ) {
                                            Icon(imageVector = Icons.Default.Camera, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Scan & Digitize Paper", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Camera Permission Prompt / Emulator Fallback
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = NeonGreenPrimary,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Camera Permission Required",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Grant camera access to scan legal papers, employment letters, and contracts into digital summaries.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreenPrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("request_camera_permission_button")
                                ) {
                                    Text("Enable Camera Scanner", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                TextButton(
                                    onClick = {
                                        isScanningProcess = true
                                        scope.launch {
                                            delay(1200)
                                            val now = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())
                                            scannedResultTitle = "Sample Rental Lease Notice ($now)"
                                            scannedResultCategory = "Property / Rent"
                                            scannedResultText = """
                                                • Document Type: Residential Lease Security Deposit Notice
                                                • Landlord Claim: Deducting $450 for repaint and carpet maintenance.
                                                • Tenant Protection Law: Landlord failed to submit itemized receipt within 21-day statutory deadline.
                                                • Recommended Action: File formal deposit demand letter citing Civil Code section 1950.5.
                                            """.trimIndent()
                                            isScanningProcess = false
                                            hasScannedOutput = true
                                        }
                                    },
                                    modifier = Modifier.testTag("sample_scan_fallback_button")
                                ) {
                                    Text("Try Sample Legal Scan (Simulator Mode)", color = PriceTagGreen, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
