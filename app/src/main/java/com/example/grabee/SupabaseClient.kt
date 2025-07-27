package com.example.grabee  // Replace with your package name

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object SupabaseClient {

    // Configure custom JSON serializer (optional)
    private val customJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // Lazy initialization
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            // Auth configuration
            install(Auth) {
                alwaysAutoRefresh = true
                flowType = Auth.FlowType.PKCE
            }

            // Database configuration
            install(Postgrest) {
                // serializer = CustomSerializer() // Uncomment if using custom serialization
            }

            // Storage configuration
            install(Storage)

            // Realtime configuration
            install(Realtime)

            // Set custom JSON (optional)
            defaultSerializer = { _, _, value ->
                customJson.encodeToString(value::class.serializer(), value)
            }
        }
    }
}