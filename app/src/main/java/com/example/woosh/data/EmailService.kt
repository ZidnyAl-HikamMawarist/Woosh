package com.example.woosh.data

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EmailJSRequest(
    val service_id: String,
    val template_id: String,
    val user_id: String,
    val template_params: Map<String, String>,
    val accessToken: String? = null
)

object EmailService {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true
                encodeDefaults = false // Ini akan mencegah null/default dikirim ke API
            })
        }
    }

    // Ganti dengan kredensial EmailJS Anda
    private val SERVICE_ID = com.example.woosh.BuildConfig.EMAILJS_SERVICE_ID 
    private val TEMPLATE_ID = com.example.woosh.BuildConfig.EMAILJS_TEMPLATE_ID
    private val PUBLIC_KEY = com.example.woosh.BuildConfig.EMAILJS_PUBLIC_KEY 
    private val PRIVATE_KEY = com.example.woosh.BuildConfig.EMAILJS_PRIVATE_KEY

    suspend fun sendTicketEmail(
        toEmail: String, 
        name: String, 
        seats: String, 
        train: String,
        ticketId: String = "WSH-123456",
        date: String = "12 Okt 2026",
        totalPrice: String = "Rp 600.000",
        paymentMethod: String = "QRIS"
    ): Pair<Boolean, String?> {
        return try {
            val response: HttpResponse = client.post("https://api.emailjs.com/api/v1.0/email/send") {
                contentType(ContentType.Application.Json)
                setBody(EmailJSRequest(
                    service_id = SERVICE_ID,
                    template_id = TEMPLATE_ID,
                    user_id = PUBLIC_KEY,
                    accessToken = PRIVATE_KEY,
                    template_params = mapOf(
                        "toEmail" to toEmail,
                        "to_email" to toEmail, // Alias for compatibility
                        "name" to name,
                        "seats" to seats,
                        "train" to train,
                        "ticketId" to ticketId,
                        "date" to date,
                        "totalPrice" to totalPrice,
                        "paymentMethod" to paymentMethod,
                        "message" to "Terima kasih telah memesan tiket Woosh. Berikut adalah detail tiket Anda."
                    )
                ))
            }
            if (response.status == HttpStatusCode.OK) {
                Pair(true, null)
            } else {
                val errorBody = response.bodyAsText()
                Pair(false, "EmailJS Error (${response.status}): $errorBody")
            }
        } catch (e: Exception) {
            Pair(false, "Network Error: ${e.message}")
        }
    }
}
