package com.github.xnuvers007.chargeralarm

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

class EmergencySender(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun sendSMS(phoneNumber: String, message: String): Boolean {
        try {
            val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            // Divide message if it's too long
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            return true
        } catch (e: Exception) {
            Log.e("EmergencySender", "Failed to send SMS", e)
            return false
        }
    }

    suspend fun sendTelegramMessage(botToken: String, chatId: String, message: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=${java.net.URLEncoder.encode(message, "UTF-8")}"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("EmergencySender", "Failed to send Telegram message", e)
                false
            }
        }
    }

    suspend fun sendTelegramPhoto(botToken: String, chatId: String, photoFile: File, caption: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.telegram.org/bot$botToken/sendPhoto"
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", chatId)
                    .addFormDataPart("caption", caption)
                    .addFormDataPart(
                        "photo",
                        photoFile.name,
                        photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("EmergencySender", "Failed to send Telegram photo", e)
                false
            }
        }
    }
}
