package com.example.termtwoproject.Dialogues

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.termtwoproject.R
import java.lang.ClassCastException


class UploadLineDialog : AppCompatDialogFragment() {

    lateinit var dialogListener: UploadLineDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_upload_drawing, null)

        //val linearLayout = view?.findViewById<LinearLayout>(R.id.view_upload_layout)
        val bundle: Bundle? = arguments
        //val fragments = bundle?.getIntegerArrayList("fragments")

        //for (fragment in fragments.orEmpty()) {
        //    val checkbox: CheckBox = CheckBox(context)
        //    checkbox.text = "Line ${fragment.toString()}"
        //    checkbox.gravity = 1
        //    linearLayout?.addView(checkbox)
        //}

        if (view != null) {
            val titleText = view.findViewById<TextView>(R.id.uploadTitleText)
            val categoryText = view.findViewById<TextView>(R.id.uploadCategoryText)
            val mapTypeText = view.findViewById<TextView>(R.id.uploadMapTypeText)
            val fragmentText = view.findViewById<TextView>(R.id.uploadFragmentAmounText)
            val snapshotView = view.findViewById<ImageView>(R.id.uploadSnapshot)
            titleText.text = String.format(getString(R.string.uploadTitle), "TEST TITLE")
            categoryText.text = String.format(getString(R.string.uploadCategory), "CATEGORY")
            mapTypeText.text = String.format(getString(R.string.uploadMapType), "MAPTYPE")
            fragmentText.text = String.format(getString(R.string.uploadFragmentAmount), 4)
            val bytes = bundle!!.getByteArray("snapshot")
            val bmp = BitmapFactory.decodeByteArray(bytes, 0,bytes.size)
            snapshotView.setImageBitmap(bmp)
        }

        builder.setView(view)
            .setTitle("Upload drawing")
            .setNegativeButton("cancel", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
            .setPositiveButton("Upload", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialogListener.uploadFragment()
                }
            })
        return builder.create()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            dialogListener = activity as UploadLineDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() +
                    "Must implement ViewLineDialogListener")
        }
    }

    interface UploadLineDialogListener {
        fun uploadFragment()
    }
}