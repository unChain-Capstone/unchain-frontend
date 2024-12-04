package com.unchain.utils

import androidx.navigation.NavOptions
import com.unchain.R

object NavigationUtils {
    fun getSlideLeftAnimation(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)    // Fragment baru masuk dari kanan
            .setExitAnim(R.anim.slide_out_left)     // Fragment lama keluar ke kiri
            .setPopEnterAnim(R.anim.slide_in_left)  // Kembali: fragment lama masuk dari kiri
            .setPopExitAnim(R.anim.slide_out_right) // Kembali: fragment baru keluar ke kanan
            .build()
    }

    fun getSlideRightAnimation(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_left)     // Fragment baru masuk dari kiri
            .setExitAnim(R.anim.slide_out_right)    // Fragment lama keluar ke kanan
            .setPopEnterAnim(R.anim.slide_in_right) // Kembali: fragment lama masuk dari kanan
            .setPopExitAnim(R.anim.slide_out_left)  // Kembali: fragment baru keluar ke kiri
            .build()
    }
}
