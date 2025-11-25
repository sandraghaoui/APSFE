package com.example.aps.api

import com.example.aps.api.PeopleRead
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class LoyaltyRepository(private val supabase: SupabaseClient) {

    // Get current logged-in user id (Supabase auth uid)
    suspend fun currentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    /**
     * Always return a People row for this user:
     *  - if it exists in "people" -> return it
     *  - otherwise create one with 0 points and 0 balance, then return it
     */
    suspend fun getOrCreatePeople(userId: String): PeopleRead {

        // 1) Try to find existing row using a proper query filter
        val peopleList = supabase
            .from("people")
            .select {
                filter {
                    eq("uuid", userId)
                }
            }
            .decodeList<PeopleRead>()

        val existing = peopleList.firstOrNull()
        if (existing != null) return existing

        // 2) Create new row with default values
        val body = mapOf(
            "uuid" to userId,
            "plate_number" to null,
            "loyalty_points" to 0,
            "balance" to 0.0
        )

        return supabase
            .from("people")
            .insert(body) {
                select()            // return inserted row
            }
            .decodeSingle<PeopleRead>()
    }
}
