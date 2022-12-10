package br.com.imageclusterapp.presentation

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import smile.clustering.kmeans
import java.io.InputStream
import androidx.compose.ui.graphics.Color as uiColor

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    data class ViewState(
        val imageBitmap: ImageBitmap? = null,
        val clusteredImage: ImageBitmap? = null,
        val centroids: List<uiColor> = listOf(),
        val enableButton: Boolean = true,
        val currentText: String = "",
        val processingTime: Long = 0
    )

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()

    private val _clusters = MutableStateFlow<Int?>(3)
    val clusters = _clusters.asStateFlow()

    fun setImageUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            getContext().contentResolver.let { contentResolver: ContentResolver ->
                val readUriPermission: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, readUriPermission)
                contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    _viewState.update {
                        it.copy(
                            imageBitmap = bitmap.asImageBitmap(),
                            clusteredImage = null,
                            currentText = "Collecting Image",
                            centroids = listOf(),
                            enableButton = false
                        )
                    }
                    clusterImage(bitmap.asImageBitmap())
                }
            }
        }
    }

    private fun clusterImage(imageBitmap: ImageBitmap) {
        viewModelScope.launch(Dispatchers.IO) {

            val startTime = System.currentTimeMillis()
            _viewState.update {
                it.copy(
                    currentText = "Collecting all Pixels"
                )
            }

            val bitmap = imageBitmap.asAndroidBitmap()
            val height = bitmap.height
            val width = bitmap.width
            val pixels = IntArray(height * width)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            _viewState.update {
                it.copy(
                    currentText = "Casting Pixel Color"
                )
            }

            // Collecting color from pixels
            val data = arrayOfNulls<DoubleArray>(height * width)
            pixels.forEachIndexed { index, value ->
                val color = uiColor(pixels[index])
                data[index] = doubleArrayOf(
                    color.red.toDouble(),
                    color.green.toDouble(),
                    color.blue.toDouble()
                )
            }

            _viewState.update {
                it.copy(
                    currentText = "Running KMeans Algorithm"
                )
            }

            // Start KMeans
            val nClusters = if (_clusters.value == 1) 3 else _clusters.value
            val result = kmeans(data.requireNoNulls(), nClusters ?: 3, 80)

            // Getting centroids List
            val centroids: MutableList<uiColor> = mutableListOf()
            result.centroids.forEachIndexed { index, centroid ->
                val (r, g, b) = centroid
                centroids.add(uiColor(red = r.toFloat(), green = g.toFloat(), blue = b.toFloat()))
                Log.e("tag", "Centroid $index -> R: $r | G: $g | B: $b")
            }

            _viewState.update {
                it.copy(
                    currentText = "Replacing Pixels"
                )
            }

            // Replacing Pixels
            for (x in result.y.indices) {
                pixels[x] = uiColor(
                    result.centroids[result.y[x]][0].toFloat(),
                    result.centroids[result.y[x]][1].toFloat(),
                    result.centroids[result.y[x]][2].toFloat(),
                ).toArgb()
            }

            _viewState.update {
                it.copy(
                    currentText = "Create new BitMap"
                )
            }

            // Create new BitMap
            val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

            // Applying pixels
            newBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

            val endTime = System.currentTimeMillis()
            _viewState.update {
                it.copy(
                    centroids = centroids,
                    enableButton = true,
                    currentText = "",
                    clusteredImage = newBitmap.asImageBitmap(),
                    processingTime = endTime - startTime
                )
            }
        }
    }

    fun setClusterInput(clusters: String) {
        if (clusters.isBlank())
            _clusters.value = null
        try {
            val castCluster = clusters.toInt()
            if (castCluster <= 0 || castCluster > 100) {
                return
            }
            _clusters.value = castCluster
        } catch (e: Exception) {
            return
        }
    }

    private fun getContext(): Context = getApplication<Application>().applicationContext
}
