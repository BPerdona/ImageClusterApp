package br.com.imageclusterapp.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlinx.coroutines.launch


@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onOpenGallery: () -> Unit,
    onImageSave: (bmp: Bitmap) -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    val clusters by viewModel.clusters.collectAsState(initial = 3)
    val scrollState = rememberScrollState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var imageSaved by remember {
        mutableStateOf(false)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                backgroundColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Image Cluster",
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                SelectedImage(imageBitmap = viewState.imageBitmap)

                ProgressionIndicator(
                    enableButton = viewState.enableButton,
                    currentText = viewState.currentText
                )

                PreviewClusteredImage(
                    clusteredImage = viewState.clusteredImage,
                    onSaveImage = {
                        onImageSave(it)
                        imageSaved = true
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("Image saved")
                        }
                    },
                    imageSaved = imageSaved,
                )

                TimeAndColorList(
                    clusteredImage = viewState.clusteredImage,
                    viewState.processingTime,
                    centroids = viewState.centroids
                )

                ButtonAndClusterInput(
                    enableButton = viewState.enableButton,
                    clusters = clusters,
                    onValueChange = viewModel::setClusterInput,
                    onOpenGallery = {
                        onOpenGallery()
                        imageSaved = false
                    }
                )

                Spacer(modifier = Modifier.height(65.dp))
            }
        }
    }
}

@Composable
private fun ButtonAndClusterInput(
    enableButton: Boolean,
    clusters: Int?,
    onValueChange: (String) -> Unit,
    onOpenGallery: () -> Unit
) {
    if (enableButton) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                modifier = Modifier.width(120.dp),
                value = if (clusters == null) "" else clusters.toString(),
                onValueChange = {
                    onValueChange(it)
                },
                singleLine = true,
                label = {
                    Text(text = "Clusters", fontSize = 17.sp)
                },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    focusedIndicatorColor = Color.Black,
                    cursorColor = Color.DarkGray,
                    unfocusedIndicatorColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.width(30.dp))
            Button(
                modifier = Modifier.scale(1.2f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                ),
                onClick = onOpenGallery,
            ) {
                Text(text = "Pick Photo")
            }
        }
    }
}

@Composable
private fun TimeAndColorList(
    clusteredImage: ImageBitmap?,
    processingTime: Long,
    centroids: List<Color>
) {
    if (clusteredImage != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Processing Time -> ${convertTime(processingTime)}",
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            for (it in centroids) {
                val hexColor = it.toArgb()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .padding(start = 10.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = Color.Black,
                                RoundedCornerShape(14.dp)
                            )
                            .background(it)
                    )
                    Text(
                        text = "RGB -> Red %03d | Green %03d | Blue %03d".format(
                            hexColor.red,
                            hexColor.green,
                            hexColor.blue
                        ),
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewClusteredImage(
    imageSaved: Boolean,
    clusteredImage: ImageBitmap?,
    onSaveImage: (bmp: Bitmap) -> Unit
) {
    if (clusteredImage != null) {
        Divider(
            modifier = Modifier.padding(horizontal = 10.dp),
            color = Color.LightGray,
            thickness = 1.dp
        )
        Image(
            modifier = Modifier
                .padding(20.dp)
                .clip(RoundedCornerShape(10.dp)),
            bitmap = clusteredImage,
            contentDescription = "Galery Image"
        )
        if (!imageSaved)
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                ),
                onClick = {
                    onSaveImage(clusteredImage.asAndroidBitmap())
                },
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .scale(1.1f)
            ) {
                Text(text = "Save Image")
            }
        else
            Text(
                modifier = Modifier.padding(bottom = 20.dp),
                text = "Image successfully saved",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                fontSize = 18.sp
            )
    }
}

@Composable
private fun ProgressionIndicator(enableButton: Boolean, currentText: String) {
    if (!enableButton) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = currentText, color = Color.Black, fontSize = 19.sp)
        Spacer(modifier = Modifier.height(30.dp))
        CircularProgressIndicator(
            modifier = Modifier.scale(1.2f),
            color = Color(0xFF3EA040)
        )
    }
}

@Composable
private fun SelectedImage(imageBitmap: ImageBitmap?) {
    if (imageBitmap != null) {
        Image(
            modifier = Modifier
                .padding(20.dp)
                .clip(RoundedCornerShape(10.dp)),
            bitmap = imageBitmap,
            contentDescription = "Galery Image"
        )
    } else {
        Image(
            modifier = Modifier
                .alpha(0.50f)
                .padding(20.dp)
                .size(300.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Gray),
            imageVector = Icons.Default.Search,
            contentDescription = "Place Holder"
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun convertTime(ms: Long): String {
    val sec = (ms / 1000) % 60
    val min = (ms / 60000) % 60
    return "$min min | $sec sec."
}