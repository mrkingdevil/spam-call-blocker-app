package com.addev.listaspam.util

import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.addev.listaspam.util.ApiConfig
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Utility object for interacting with the UnknownPhone API to check if a phone number is marked as spam.
 */
object ApiUtils {
    private const val UNKNOWN_PHONE_API_URL = "https://secure.unknownphone.com/api/"
    private val UNKNOWN_PHONE_API_KEY = ApiConfig.UNKNOWN_PHONE_API_KEY

    private const val TELLOWS_API_URL = "www.tellows.de"
    private val TELLOWS_API_KEY = ApiConfig.TELLOWS_API_KEY

    private const val TRUECALLER_API_URL_EU = "search5-eu.truecaller.com"
    private const val TRUECALLER_API_URL_NONEU = "search5-noneu.truecaller.com"
    private val TRUECALLER_API_KEY = ApiConfig.TRUECALLER_API_KEY

    private const val TRUE_CALLER_REPORT_API_URL_EU = "https://filter-store4-eu.truecaller.com/v4/filters?encoding=json"
    private const val TRUE_CALLER_REPORT_API_URL_NONEU = "https://filter-store4-noneu.truecaller.com/v4/filters?encoding=json"

    private val EU_COUNTRIES = setOf(
        "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR",
        "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL",
        "PL", "PT", "RO", "SK", "SI", "ES", "SE"
    )

    private val client = OkHttpClient()

    /**
     * Sends a POST request to the UnknownPhone API to retrieve information about the given phone number.
     *
     * The method constructs a form-encoded request with necessary parameters, sends it using OkHttp,
     * and interprets the response to determine if the phone number is likely to be spam.
     *
     * @param number The phone number to check, in international format.
     * @return `true` if the number has an average rating lower than 3 (i.e., bad or dangerous), otherwise `false`.
     */
    fun checkListaSpamApi(number: String, lang: String): Boolean {
        val formBody = FormBody.Builder()
            .add("user_type", "free")
            .add("api_key", UNKNOWN_PHONE_API_KEY)
            .add("phone", number)
            .add("_action", "_get_info_for_phone")
            .add("lang", lang)
            .build()

        val request = Request.Builder()
            .url(UNKNOWN_PHONE_API_URL)
            .post(formBody)
            .header("Connection", "Keep-Alive")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Host", "secure.unknownphone.com")
            .header("User-Agent", "okhttp/3.14.9")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return false

            val bodyString = response.body?.string() ?: return false

            val avgRating =
                JSONObject(bodyString).optString("avg_ratings").toFloatOrNull() ?: return false

            // Average ratings:
            // 5 - safe
            // 4 - good
            // 3 - neutral
            // 2 - bad
            // 1 - dangerous
            avgRating < 3
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sends a POST report to the UnknownPhone API for a given phone number, marking it with a comment and metadata.
     *
     * This method constructs and sends a form-encoded POST request that includes the phone number, a comment,
     * call type, language, and optional metadata. It's used to report marketing or spam calls to the platform.
     *
     * @param phone The phone number being reported.
     * @param comment The user comment about the phone number.
     * @param lang The language code (e.g., "ES" for Spanish).
     * @return `true` if the report was submitted successfully; `false` otherwise.
     */
    fun reportToUnknownPhone(
        phone: String,
        comment: String,
        isSpam: Boolean,
        lang: String,
    ): Boolean {
        val optRating = if (isSpam) "1" else "5"

        val formBuilder = FormBody.Builder()
            .add("api_key", UNKNOWN_PHONE_API_KEY)
            .add("phone", phone)
            .add("_action", "_submit_comment")
            .add("comment", comment)
            .add("lang", lang)
            .add("_opt_rating", optRating)

        val request = Request.Builder()
            .url(UNKNOWN_PHONE_API_URL)
            .post(formBuilder.build())
            .header("Connection", "Keep-Alive")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Host", "secure.unknownphone.com")
            .header("User-Agent", "okhttp/3.14.9")
            .build()

        return try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    fun checkTruecallerSpamApi(number: String, countryCode: String): Boolean {
        val deviceCountryCode = Locale.getDefault().country

        val host = if (EU_COUNTRIES.contains(deviceCountryCode.uppercase())) {
            TRUECALLER_API_URL_EU
        } else {
            TRUECALLER_API_URL_NONEU
        }

        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegments("v2/search")
            .addQueryParameter("q", number)
            .addQueryParameter("countryCode", countryCode)
            .addQueryParameter("type", "4")
            .addQueryParameter("locAddr", "")
            .addQueryParameter("placement", "SEARCHRESULTS,HISTORY,DETAILS")
            .addQueryParameter("adId", "")
            .addQueryParameter("encoding", "json")
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .header("Connection", "Keep-Alive")
            .header("User-Agent", "Truecaller/9.00.3 (Android;10)")
            .header("Authorization", "Bearer $TRUECALLER_API_KEY")
            .header("Host", "search5-eu.truecaller.com")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return false

            val bodyString = response.body?.string() ?: return false
            val json = JSONObject(bodyString)
            val dataArray = json.optJSONArray("data") ?: return false
            if (dataArray.length() == 0) return false

            val firstEntry = dataArray.getJSONObject(0)
            val spamInfo = firstEntry.optJSONObject("spamInfo")
            val spamType = spamInfo?.optString("spamType")
            val spamScore = spamInfo?.optInt("spamScore", 0) ?: 0 // Reports quantity

            !spamType.isNullOrBlank() && spamScore > 1
        } catch (e: Exception) {
            false
        }
    }

    fun reportToTruecaller(
        phone: String,
        comment: String,
        isSpam: Boolean
    ): Boolean {
        val deviceCountryCode = Locale.getDefault().country

        val host = if (EU_COUNTRIES.contains(deviceCountryCode.uppercase())) {
            TRUE_CALLER_REPORT_API_URL_EU
        } else {
            TRUE_CALLER_REPORT_API_URL_NONEU
        }


        val requestBodyJson = JSONArray().put(
            JSONObject().apply {
                put("value", phone)
                put("label", comment.take(10))
                put("comment", comment)
                put("rule", if (isSpam) "BLACKLIST" else "WHITELIST")
                put("type", "OTHER")
                put("source", "detailView")
            }
        ).toString()

        val requestBody = requestBodyJson.toRequestBody("application/json; charset=UTF-8".toMediaType())

        val request = Request.Builder()
            .url(host)
            .put(requestBody)
            .header("Authorization", "Bearer $TRUECALLER_API_KEY")
            .header("Content-Type", "application/json; charset=UTF-8")
            .header("Connection", "Keep-Alive")
            .header("Host", "filter-store4-eu.truecaller.com")
            .header("User-Agent", "Truecaller/9.00.3 (Android;10)")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return false
            val json = JSONObject(responseBody)
            json.has("data")
        } catch (e: Exception) {
            false
        }
    }

    fun checkTellowsSpamApi(number: String, country: String): Boolean {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(TELLOWS_API_URL)
            .addPathSegments("basic/num/$number")
            .addQueryParameter("xml", "1")
            .addQueryParameter("partner", "androidapp")
            .addQueryParameter("apikey", TELLOWS_API_KEY)
            .addQueryParameter("overridecountryfilter", "1")
            .addQueryParameter("country", country)
            .addQueryParameter("showcomments", "50")
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .header("Connection", "Keep-Alive")
            .header("Host", TELLOWS_API_URL)
            .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 6.0; I14 Pro Max Build/MRA58K)")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return false

            val bodyString = response.body?.string() ?: return false

            val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(bodyString.byteInputStream())

            val scoreNode = xml.getElementsByTagName("score").item(0)
            val score = scoreNode?.textContent?.toIntOrNull() ?: return false

            // Tellows scores: 1 (safe) to 9 (very dangerous)
            score >= 7
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sends a report to Tellows about a phone number, submitting a comment and associated metadata.
     *
     * This method builds a POST request to the Tellows API with form data including the phone number,
     * comment, complaint type, user type, and score.
     *
     * @param phone The phone number to report (without country code prefix, if already localized).
     * @param comment A description of the issue or behavior associated with the number.
     * @param isSpam
     * @param lang Language and country code, e.g. "es".
     * @return `true` if the report was accepted; `false` otherwise.
     */
    fun reportToTellows(
        phone: String,
        comment: String,
        isSpam: Boolean,
        lang: String = "es",
    ): Boolean {
        val userScore = if (isSpam) 9 else 1
        val complainTypeId = if (isSpam) 5 else 2 // 5 is aggressive advertising and 2 is reliable number

        val url = HttpUrl.Builder()
            .scheme("https")
            .host(TELLOWS_API_URL)
            .addPathSegments("basic/num/$phone")
            .addQueryParameter("xml", "1")
            .addQueryParameter("partner", "androidapp")
            .addQueryParameter("apikey", TELLOWS_API_KEY)
            .addQueryParameter("createcomment", "1")
            .addQueryParameter("country", lang)
            .addQueryParameter("lang", lang)
            .addQueryParameter("user_auth", "")
            .addQueryParameter("user_email", "")
            .build()

        val formBody = FormBody.Builder()
            .add("caller", phone)
            .add("comment", comment)
            .add("complain_type_id", complainTypeId.toString())
            .add("user", "Android")
            .add("userscore", userScore.toString())
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .header("Accept-Encoding", "gzip")
            .header("Connection", "Keep-Alive")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Host", TELLOWS_API_URL)
            .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 6.0; I14 Pro Max Build/MRA58K)")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return false

            // Check for success in JSON response
            val json = JSONObject(responseBody)
            json.optBoolean("success", false)
        } catch (e: Exception) {
            false
        }
    }

}