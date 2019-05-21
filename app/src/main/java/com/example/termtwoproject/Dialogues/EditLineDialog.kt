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
import java.lang.ClassCastException

class EditLineDialog: AppCompatDialogFragment() {

    lateinit var dialogListener: EditDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_edit_drawing, null)

        val edit_radio_group = view?.findViewById<RadioGroup>(R.id.edit_radio_group)
        val bundle: Bundle? = arguments
        val fragments = bundle?.getIntegerArrayList("fragments")

        for (fragment in fragments.orEmpty()) {
            val radioButton: RadioButton = RadioButton(context)
            radioButton.text = "Line ${fragment.toString()}"
            edit_radio_group?.addView(radioButton)
        }
        
        builder.setView(view)
            .setTitle("Edit Line")
            .setNegativeButton("cancel", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
            .setPositiveButton("Edit", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialogListener.editFragment()
                }
            })
        return builder.create()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            dialogListener = activity as EditDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() +
        "Must implement EditDialogListener")

        }
    }

    interface EditDialogListener {
        fun editFragment()
    }
}