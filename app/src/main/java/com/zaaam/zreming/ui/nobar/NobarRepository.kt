package com.zaaam.zreming.ui.nobar

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NobarRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {

    suspend fun createRoom(roomId: String): NobarRoom {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User belum login")
        val username = auth.currentUser?.displayName ?: "Anonim"

        val data = mapOf(
            "id" to roomId,
            "contentTitle" to "Nobar Bareng",
            "hostUid" to uid,
            "hostUsername" to username,
            "createdAt" to FieldValue.serverTimestamp(),
        )

        firestore
            .collection("nobar_rooms")
            .document(roomId)
            .set(data)
            .await()

        return NobarRoom(
            id = roomId,
            contentTitle = "Nobar Bareng",
            hostUid = uid,
            hostUsername = username,
        )
    }

    suspend fun roomExists(roomId: String): Boolean {
        return try {
            val doc = firestore
                .collection("nobar_rooms")
                .document(roomId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getMutualFriends(): List<NobarFriend> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        return try {
            val followingSnap = firestore
                .collection("users")
                .document(uid)
                .collection("following")
                .get()
                .await()

            val followerSnap = firestore
                .collection("users")
                .document(uid)
                .collection("followers")
                .get()
                .await()

            val followingIds = followingSnap.documents.map { it.id }.toSet()
            val followerIds = followerSnap.documents.map { it.id }.toSet()
            val mutualIds = followingIds.intersect(followerIds)

            val friends = mutableListOf<NobarFriend>()
            for (friendId in mutualIds) {
                val userDoc = firestore
                    .collection("users")
                    .document(friendId)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    friends.add(
                        NobarFriend(
                            uid = friendId,
                            username = userDoc.getString("username") ?: "user",
                            verified = userDoc.getBoolean("verified") ?: false,
                        )
                    )
                }
            }
            friends
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendInvite(roomId: String, targetUsername: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User belum login")

        val targetSnap = firestore
            .collection("users")
            .whereEqualTo("username", targetUsername)
            .limit(1)
            .get()
            .await()

        val targetUid = targetSnap.documents.firstOrNull()?.id
            ?: throw IllegalStateException("User tidak ditemukan")

        val invite = mapOf(
            "roomId" to roomId,
            "fromUid" to uid,
            "fromUsername" to auth.currentUser?.displayName ?: "Anonim",
            "createdAt" to FieldValue.serverTimestamp(),
            "status" to "pending",
        )

        firestore
            .collection("users")
            .document(targetUid)
            .collection("nobar_invites")
            .document(roomId)
            .set(invite)
            .await()
    }
}