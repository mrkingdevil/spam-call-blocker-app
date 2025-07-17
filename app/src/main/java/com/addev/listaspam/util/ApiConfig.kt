package com.addev.listaspam.util

/**
 * Provides API keys from environment variables.
 */
object ApiConfig {
    val UNKNOWN_PHONE_API_KEY: String = System.getenv("UNKNOWN_PHONE_API_KEY") ?: ""
    val TELLOWS_API_KEY: String = System.getenv("TELLOWS_API_KEY") ?: ""
    val TRUECALLER_API_KEY: String = System.getenv("TRUECALLER_API_KEY") ?: ""
}
