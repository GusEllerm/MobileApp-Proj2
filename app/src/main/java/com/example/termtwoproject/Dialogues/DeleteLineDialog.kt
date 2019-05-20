package com.example.termtwoproject.Dialogues

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.termtwoproject.R
import java.io.File
import java.lang.ClassCastException

class DeleteLineDialog: AppCompatDialogFragment() {

    lateinit var dialogListner: DeleteLineDialogListner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_delete_drawing, null)

        val delete_radio_group = view?.findViewById<RadioGroup>(R.id.delete_radio_group)
        val bundle: Bundle? = arguments
        val fragments = bundle?.getIntegerArrayList("fragments")

        for (fragment in fragments.orEmpty()) {
            val radioButton: RadioButton = RadioButton(context)
            radioButton.text = "Line ${fragment.toString()}"
            delete_radio_group?.addView(radioButton)
        }

        builder.setView(view)
            .setTitle("Delete Line")
            .setNegativeButton("cancel", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
            .setPositiveButton("Delete", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialogListner.deleteFragment()
                }
            })
        return builder.create()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            dialogListner = activity as DeleteLineDialogListner
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() +
            "Must implement DeleteLineDialogListner")
        }
    }

    interface DeleteLineDialogListner {
        fun deleteFragment()
    }
}