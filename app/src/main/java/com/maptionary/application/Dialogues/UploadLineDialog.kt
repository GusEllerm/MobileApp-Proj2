package com.maptionary.application.Dialogues

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.maptionary.application.R
import com.maptionary.application.models.GpsMap
import org.json.JSONObject
import java.lang.ClassCastException


class UploadLineDialog : AppCompatDialogFragment() {

    lateinit var dialogListener: UploadLineDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_upload_drawing, null)

        //val linearLayout = view?.findViewById<LinearLayout>(R.id.view_upload_layout)
        val bundle: Bundle? = arguments
        var map : GpsMap? = null
        if (bundle != null) {
            map = GpsMap.getGpsMapFromJSON(JSONObject(bundle.getString("GpsMap")))
        }

        if (view != null && map != null) {
            val titleText = view.findViewById<TextView>(R.id.uploadTitleText)
            val categoryText = view.findViewById<TextView>(R.id.uploadCategoryText)
            val mapTypeText = view.findViewById<TextView>(R.id.uploadMapTypeText)
            val fragmentText = view.findViewById<TextView>(R.id.uploadFragmentAmounText)
            val snapshotView = view.findViewById<ImageView>(R.id.uploadSnapshot)
            titleText.text = String.format(getString(R.string.uploadTitle), map.title)
            categoryText.text = String.format(getString(R.string.uploadCategory), map.category)
            mapTypeText.text = String.format(getString(R.string.uploadMapType), map.type)
            fragmentText.text = String.format(getString(R.string.uploadFragmentAmount), map.fragmentAmount)
            val bytes = Base64.decode(map.imageData, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0,bytes.size)
            snapshotView.setImageBitmap(bmp)
        }

        builder.setView(view)
            .setTitle("Upload drawing")
            .setNegativeButton("cancel", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialogListener.exitDialog(dialog)
                }
            })
            .setPositiveButton("Upload", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialogListener.uploadGpsMap(map)
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
        fun uploadGpsMap(gpsMap : GpsMap?)
        fun exitDialog(dialog : DialogInterface?)
    }
}