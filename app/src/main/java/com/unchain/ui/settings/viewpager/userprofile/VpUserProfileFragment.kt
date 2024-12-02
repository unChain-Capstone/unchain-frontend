package com.unchain.ui.settings.viewpager.userprofile

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.unchain.R


class VpUserProfileFragment : Fragment() {

    private lateinit var viewModel: VpUserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_vp_user_profile, container, false)

        viewModel = ViewModelProvider(this)[VpUserProfileViewModel::class.java]

        val button = view.findViewById<Button>(R.id.updateButton)
        button.setOnClickListener {
            showUpdateInfoDialog()
        }

        viewModel.height.observe(viewLifecycleOwner) { height ->
            // Update TextView dengan data height baru
            view.findViewById<TextView>(R.id.tvHeightVp).text = height
        }
        viewModel.weight.observe(viewLifecycleOwner) { weight ->
            view.findViewById<TextView>(R.id.tvWeightVp).text = weight
        }
        viewModel.dob.observe(viewLifecycleOwner) { dob ->
            view.findViewById<TextView>(R.id.tvDOBVp).text = dob
        }
        return view
    }

    private fun showUpdateInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_info, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Update Info")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val heightInput = dialogView.findViewById<EditText>(R.id.etHeight)
                val weightInput = dialogView.findViewById<EditText>(R.id.etWeight)
                val dobInput = dialogView.findViewById<EditText>(R.id.etDOB)

                // Kirim data ke ViewModel
                viewModel.updateUserInfo(
                    newHeight = heightInput.text.toString(),
                    newWeight = weightInput.text.toString(),
                    newDOB = dobInput.text.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}