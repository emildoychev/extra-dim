package com.example.extradim

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.ContentResolver
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.provider.Settings

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No layout needed for this activity

        val intent = Intent("android.settings.REDUCE_BRIGHT_COLORS_SETTINGS")

        // Check if the intent can be resolved before starting the activity
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Handle the case where the device doesn't have the Settings activity
            // You can show a message to the user here
        }

        finish() // Close this activity after launching settings
    }
}

class BrightnessTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        toggleReduceBrightColorsActivated()
        updateTile()
    }

    private fun toggleReduceBrightColorsActivated() {
        val currentSetting = getReduceBrightColorsActivated()
        val newValue = if (currentSetting == 0) 1 else 0
        val command = "settings put secure reduce_bright_colors_activated $newValue"
        executeAsRoot(command)
    }

    private fun getReduceBrightColorsActivated(): Int {
        val resolver: ContentResolver = contentResolver
        return Settings.Secure.getInt(resolver, "reduce_bright_colors_activated", 0)
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val nowState = getReduceBrightColorsActivated()
        if (nowState == 0) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Extra Dim: Off"
        } else {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Extra Dim: On"
        }
        tile.updateTile()
    }

    private fun executeAsRoot(command: String) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            outputStream.write("$command\n".toByteArray())
            outputStream.flush()
            outputStream.close()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
