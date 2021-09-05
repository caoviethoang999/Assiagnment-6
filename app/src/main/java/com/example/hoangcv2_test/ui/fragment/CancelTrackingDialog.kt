package com.example.hoangcv2_test.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.hoangcv2_test.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class CancelTrackingDialog: DialogFragment() {

    private var yestListener:(()-> Unit)?=null

    fun setYesListenr(listener:() -> Unit){
        yestListener=listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){_,_ ->
                yestListener?.let { yes->
                    yes()
                }
            }
            .setNegativeButton("No"){dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}