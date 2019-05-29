package com.maptionary.application.Dialogues

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import com.maptionary.application.R
import java.lang.ClassCastException

class CreateNewLineDialog: AppCompatDialogFragment() {

    lateinit var dialogListener: NewLineDialogListner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_new_drawing, null)

        builder.setView(view)
            .setTitle("New Line")
            .setNegativeButton("cancel", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
            .setPositiveButton("Create", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialogListener.newFragment()
                }
            })
        return builder.create()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            dialogListener = activity as NewLineDialogListner
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() +
            "Must impelment ExampleDialogListner")
        }
    }

    interface NewLineDialogListner {
        fun newFragment()
    }
}