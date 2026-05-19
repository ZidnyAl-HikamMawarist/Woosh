package com.example.woosh.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage {
    ID, EN, CN
}

data class WooshStrings(
    val app_name: String = "Woosh",
    val profile: String = "Profil",
    val tickets: String = "Tiket",
    val home: String = "Beranda",
    val loyalty: String = "Loyalty",
    val settings: String = "Pengaturan",
    val change_password: String = "Ubah Kata Sandi",
    val delete_account: String = "Hapus Akun",
    val logout: String = "Keluar",
    val help_center: String = "Pusat Bantuan",
    val saved_passengers: String = "Daftar Penumpang",
    val info_services: String = "Informasi & Layanan",
    val version: String = "Versi Aplikasi",
    val reschedule: String = "Reschedule",
    val refund: String = "Refund",
    val scan_qr: String = "Scan saat masuk peron",
    val points: String = "Points",
    val language: String = "Bahasa"
)

val IndonesianStrings = WooshStrings()

val EnglishStrings = WooshStrings(
    profile = "Profile",
    tickets = "Tickets",
    home = "Home",
    loyalty = "Loyalty",
    settings = "Settings",
    change_password = "Change Password",
    delete_account = "Delete Account",
    logout = "Logout",
    help_center = "Help Center",
    saved_passengers = "Passenger List",
    info_services = "Info & Services",
    version = "App Version",
    reschedule = "Reschedule",
    refund = "Refund",
    scan_qr = "Scan at platform entrance",
    points = "Points",
    language = "Language"
)

val ChineseStrings = WooshStrings(
    profile = "个人中心",
    tickets = "我的车票",
    home = "首页",
    loyalty = "会员权益",
    settings = "设置",
    change_password = "修改密码",
    delete_account = "注销账号",
    logout = "退出登录",
    help_center = "帮助中心",
    saved_passengers = "常用联系人",
    info_services = "信息与服务",
    version = "版本号",
    reschedule = "改签",
    refund = "退票",
    scan_qr = "进站请扫码",
    points = "积分",
    language = "语言"
)

val LocalWooshStrings = staticCompositionLocalOf { IndonesianStrings }

object WooshTheme {
    val strings: WooshStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalWooshStrings.current
}
