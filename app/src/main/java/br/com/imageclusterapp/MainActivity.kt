package br.com.imageclusterapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import br.com.imageclusterapp.presentation.MainScreen
import br.com.imageclusterapp.presentation.MainScreenViewModel
import br.com.imageclusterapp.ui.theme.ImageClusterAppTheme


class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainScreenViewModel>()

    private val selectSinglePhotoContract = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ imageUri ->
        imageUri?.let(viewModel::setImageUri)
    }

    private fun selectPhoto(){
        selectSinglePhotoContract.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageClusterAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(viewModel = viewModel, onButtonClick = { selectPhoto() })
                }
            }
        }
    }
}