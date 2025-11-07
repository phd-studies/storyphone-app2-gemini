package com.example.gemini_story_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StoryUiState(
    val userInput: String = "",
    val selectedImageUri: String? = null,
    val generatedStory: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class StoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro-vision",
        apiKey = BuildConfig.API_KEY
    )

    fun onUserInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(userInput = text)
    }

    fun onImageSelected(uri: String) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun generateStory(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val prompt = "Write a short story about ${_uiState.value.userInput}"
                val image = _uiState.value.selectedImageUri?.let { uriToBitmap(context, it) }

                val inputContent = content {
                    image?.let { image(it) }
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)
                _uiState.value = _uiState.value.copy(
                    generatedStory = response.text ?: "No story generated.",
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to generate story: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun uriToBitmap(context: Context, uriString: String): Bitmap {
        val uri = Uri.parse(uriString)
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }
}
