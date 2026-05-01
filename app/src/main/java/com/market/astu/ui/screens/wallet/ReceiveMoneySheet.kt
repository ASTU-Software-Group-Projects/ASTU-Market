package com.market.astu.ui.screens.wallet

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveMoneySheet(
    onDismiss: () -> Unit,
    receiveCode: String
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Receive CAMPUS", style = MaterialTheme.typography.headlineSmall)

            val bitmap = remember(receiveCode) {
                runCatching {
                    BarcodeEncoder().encodeBitmap(receiveCode, BarcodeFormat.QR_CODE, 512, 512)
                }.getOrNull()
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Receive QR",
                    modifier = Modifier.size(250.dp)
                )
            } else {
                Text("Could not generate QR code", color = MaterialTheme.colorScheme.error)
            }

            Text(
                "Your receive code: $receiveCode",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedButton(onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Send me CAMPUS: $receiveCode")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share receive code"))
            }) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Share")
            }

            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}