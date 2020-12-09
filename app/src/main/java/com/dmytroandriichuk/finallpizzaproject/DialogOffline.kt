package com.dmytroandriichuk.finallpizzaproject

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment

//message about errors and allowing to check orders from local db
class DialogOffline(private val message: String): DialogFragment() {
    
    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Failed to sign in!")
                    .setMessage(message)
                    .setPositiveButton("Ok") {
                        dialog, _ ->  dialog.cancel()
                    }

            if (message == "Account is not verified"){
                builder.setNegativeButton("Send Verification Letter") { dialog, _ ->
                    (activity as MainActivity).sendVerificationLetter()
                    dialog.cancel()
                }
            } else if (message == "Connection error") {
                builder.setNegativeButton("Go offline") { dialog, _ ->
                    (activity as MainActivity).goOfflineClicked()
                    dialog.cancel()
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}