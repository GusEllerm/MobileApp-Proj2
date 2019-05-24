package com.example.termtwoproject.Dialogues

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.termtwoproject.R
import java.lang.ClassCastException
import java.util.Collections.max

class ViewLineDialog: AppCompatDialogFragment() {

    lateinit var dialogListener: ViewLineDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_view_drawing, null)

        val linearLayout = view?.findViewById<LinearLayout>(R.id.view_drawing_layout)
        val bundle: Bundle? = arguments
        val fragments = bundle?.getIntegerArrayList("fragments")
        val currentFragment = bundle?.getInt("currentFragment")
        val viewFragments = bundle?.getIntegerArrayList("viewFragments")
        val checkboxes = mutableListOf<CheckBox>()
        val selectedFragments = ArrayList<Int>()
        var unselectedFragments = mutableListOf<Int>()

        for (fragment in fragments.orEmpty()) {
            val checkbox: CheckBox = CheckBox(context)
            checkboxes.add(checkbox)
            checkbox.text = "Line ${fragment.toString()}"
            if (viewFragments!!.contains(fragment)) {
                Log.d("HERE", "$fragment $currentFragment")
                checkbox.isChecked = true
            }
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
                    for (checkbox in checkboxes) {
                        if (checkbox.isChecked) {
                            selectedFragments.add(Character.getNumericValue(checkbox.text[checkbox.text.lastIndex]))
                        }
                    }
                    for (frag in viewFragments.orEmpty()) {
                        if (!selectedFragments.contains(frag)){
                            unselectedFragments.add(frag)
                        }
                    }
                    dialogListener.viewFragment(selectedFragments, unselectedFragments)
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
        fun viewFragment(selectedFragments: MutableList<Int>, unselectedFragments: MutableList<Int>)
    }
}