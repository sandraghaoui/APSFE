package com.example.aps.api

import com.example.aps.api.AdminRead
import com.example.aps.api.PeopleRead
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class LoyaltyRepository(private val supabase: SupabaseClient) {

    // Get current logged in user id (Supabase auth uid)
    suspend fun currentUserId(): String? =
        supabase.auth.currentUserOrNull()?.id

    // ----- ADMINS -----

    // True if the user is listed in the admins table
    suspend fun isAdmin(userId: String): Boolean {
        val admins: List<AdminRead> = supabase
            .from("admins")
            .select()               // no server-side filter
            .decodeList<AdminRead>()

        return admins.any { it.uuid == userId }
    }

    // ----- PEOPLE / LOYALTY -----

    /**
     * If user is NOT admin:
     *   - ensure there is a row in people with same uuid
     *   - return that row
     * If user IS admin:
     *   - return null (admins don't have loyalty)
     */
    suspend fun getOrCreatePeopleIfNotAdmin(userId: String): PeopleRead? {

        // Admins: no loyalty account
        if (isAdmin(userId)) return null

        // 1. Try to find existing people row on the client side
        val peopleList: List<PeopleRead> = supabase
            .from("people")
            .select()               // again, no server-side filter
            .decodeList<PeopleRead>()

        val existing = peopleList.firstOrNull { it.uuid == userId }
        if (existing != null) return existing

        // 2. If none exists, create a default entry
        val body = mapOf(
            "uuid" to userId,
            "plate_number" to null,
            "loyalty_points" to 0,
            "balance" to 0.0
        )

        return supabase
            .from("people")
            .insert(body) {
                select()
            }
            .decodeSingle<PeopleRead>()
    }
}
