package com.example.gemini_story_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gemini_story_app.ui.theme.GeminiStoryAppTheme
import androidx.activity.result.PickVisualMediaRequest

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storyViewModel: StoryViewModel by viewModels()

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                storyViewModel.onImageSelected(it.toString())
            }
        }

        setContent {
            GeminiStoryAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StoryScreen(
                        storyViewModel = storyViewModel,
                        onSelectImageClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StoryScreen(
    storyViewModel: StoryViewModel,
    onSelectImageClick: () -> Unit
) {
    val uiState by storyViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = uiState.userInput,
            onValueChange = { storyViewModel.onUserInputChanged(it) },
            label = { Text("Enter a word or phrase") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSelectImageClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Select Image")
        }

        Button(
            onClick = { storyViewModel.generateStory(context) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Generate Story")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            Text(
                text = uiState.generatedStory,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StoryScreenPreview() {
    GeminiStoryAppTheme {
        StoryScreen(
            storyViewModel = StoryViewModel(),
            onSelectImageClick = {}
        )
    }
}
