package com.example.termtwoproject.Dialogues

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.termtwoproject.R
import java.lang.ClassCastException

class ViewLineDialog: AppCompatDialogFragment() {

    lateinit var dialogListener: ViewLineDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_view_drawing, null)

        val linearLayout = view?.findViewById<LinearLayout>(R.id.view_drawing_layout)
        val bundle: Bundle? = arguments
        val fragments = bundle?.getIntegerArrayList("fragments")

        for (fragment in fragments.orEmpty()) {
            val checkbox: CheckBox = CheckBox(context)
            checkbox.text = "Line ${fragment.toString()}"
            checkbox.gravity = 1

            linearLayout?.addView(checkbox)
        }

        //TODO - basecases for if there are no items in fragments

        builder.setView(view)
            .setTitle("Select Lines")
            .setNegativeButton("cancel", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
            .setPositiveButton("View", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialogListener.viewFragment()
                }
            })
        return builder.create()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            dialogListener = activity as ViewLineDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() +
            "Must implement ViewLineDialogListener")
        }
    }

    interface ViewLineDialogListener {
        fun viewFragment()
    }
}