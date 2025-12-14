package com.example.menuannam

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ShowCardScreen(
    /*
    cardId: Int,
    getFlashCardById: suspend (Int) -> FlashCard?,
    deleteFlashCard: suspend (FlashCard) -> Unit,
    navigateBack: () -> Unit,
    changeMessage: (String) -> Unit,
    updateFlashCard: suspend (FlashCard) -> Unit
    */

    english: String,
    vietnamese: String,
    getFlashCardByPair: suspend (String, String) -> FlashCard?,
    deleteFlashCardByPair: suspend (FlashCard) -> Unit,
    navigateBack: () -> Unit,
    changeMessage: (String) -> Unit
) {
    var card by remember { mutableStateOf<FlashCard?>(null) }

    val scope = rememberCoroutineScope()

    /*
     Load card từ DB khi vào màn hình
        LaunchedEffect(cardId) {
            val loaded = getFlashCardById(cardId)
            card = loaded
            if (loaded != null) {
                englishText = loaded.englishCard ?: ""
                vietnameseText = loaded.vietnameseCard ?: ""
            }
        }
    */
    LaunchedEffect(Unit) {
        changeMessage("Đây là bottom bar của show card screen")
    }

    LaunchedEffect(english, vietnamese) {
        card = getFlashCardByPair(english, vietnamese)
    }

    if (card == null) {
        Text("Loading...")
        return
    }

    val currentCard = card!!

    Column {

        TextField(
            value = currentCard.englishCard ?: "",
            onValueChange = {}, // read-only nên bỏ trống
            readOnly = true,
            label = { Text(stringResource(R.string.English_Label)) }
        )

        TextField(
            value = currentCard.vietnameseCard?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.Vietnamese_Label)) }
        )

        // Nếu bạn vẫn muốn giữ nút Delete riêng:
        Button(onClick = {
            scope.launch {
                deleteFlashCardByPair(currentCard)
                changeMessage("Card deleted")
                navigateBack()
            }
        },
            modifier = Modifier.padding(8.dp)
        ){
            Text("Delete")
        }
    }
}