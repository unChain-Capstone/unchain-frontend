package com.unchain.ui.settings.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.unchain.ui.settings.viewpager.achievement.VpAchievementFragment
import com.unchain.ui.settings.viewpager.settings.VpSettingsFragment
import com.unchain.ui.settings.viewpager.userprofile.VpUserProfileFragment

class VPAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VpUserProfileFragment()
            1 -> VpAchievementFragment()
            2 -> VpSettingsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}