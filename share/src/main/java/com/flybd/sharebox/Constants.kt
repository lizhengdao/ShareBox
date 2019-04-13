package com.flybd.sharebox

/**
 * Created by KerriGan on 2017/6/16 0016.
 */
object Constants {

    //NetWorkState.NONE
    const val AP_STATE = "com.ecjtu.sharebox.AP_STATE"

    const val ICON_HEAD = "head.png"

    const val KEY_INFO_OBJECT = "com.ecjtu.sharebox.Info"

    const val PREF_SERVER_PORT = "pref_server_port"

    // Boolean
    const val PREF_IS_FIRST_OPEN = "pref_is_first_open"

    const val ANDROID_HOTSPOT_IP = "192.168.43.1"

    const val DEFAULT_SERVER_PORT = "8000"

    enum class NetWorkState {
        NONE,
        WIFI,
        AP,
        P2P,
        MOBILE
    }
}
