package com.unchain.ui.settings.viewpager.achievement

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unchain.R

class VpAchievementFragment : Fragment() {

    companion object {
        fun newInstance() = VpAchievementFragment()
    }

    private val viewModel: VpAchievementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_vp_achievement, container, false)
    }
}