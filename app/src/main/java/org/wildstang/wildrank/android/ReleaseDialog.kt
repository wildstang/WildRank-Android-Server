package org.wildstang.wildrank.android

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReleaseDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val mainActivity = (activity as MainActivity)
            var lastUsed = mainActivity.getLastRelease()
            if (lastUsed == "master") {
                lastUsed = "master-cached"
            }

            // create base list of releases
            val releases = ArrayList<String>()
            releases.add("Last Used: $lastUsed")
            releases.add("Latest Remote Release")
            releases.add("Master Branch")

            // add any other local releases
            val files = context!!.getExternalFilesDir("")!!.listFiles()
            if (files != null) {
                for (file in files) {
                    val name = file.nameWithoutExtension
                    if (name.startsWith("WildRank-") &&
                        !name.endsWith("-master") &&
                        !name.endsWith("-$lastUsed") &&
                        !name.endsWith("-")) {
                        releases.add("Cached: ${name.substring(9)}")
                    }
                }
            }

            releases.add("Manual Release")

            // build dialog
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose a Release")
                .setItems(releases.toTypedArray()) { _, which ->
                    // choose release name
                    var release = when (which)  {
                        0 -> lastUsed
                        1 -> "latest"
                        2 -> "master"
                        releases.size - 1 -> "manual"
                        else -> releases[which].substring(8)
                    }

                    if (release != "manual") {
                        // launch app
                        CoroutineScope(Dispatchers.Main).launch { mainActivity.init(release) }
                    }
                    else {
                        ManualReleaseDialog().show(mainActivity.supportFragmentManager, "dialog")
                    }
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // use latest if clicked off
    override fun onCancel(dialog: DialogInterface) {
        CoroutineScope(Dispatchers.Main).launch { (activity as MainActivity).init("latest") }
    }
}