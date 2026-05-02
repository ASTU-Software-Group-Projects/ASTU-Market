package com.market.astu.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.market.astu.data.model.User
import com.market.astu.data.model.UserRole
import com.market.astu.data.model.RunnerStatus
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun getCurrentUserProfile(): Result<User> {
        return try {
            val firebaseUser = auth.currentUser
                ?: throw IllegalStateException("Please sign in to view your profile.")

            val document = firestore.collection("users").document(firebaseUser.uid).get().await()
            val profile = document.toObject(User::class.java) ?: User(
                uid = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                displayName = firebaseUser.displayName.orEmpty(),
                role = UserRole.BUYER.name,
                roles = listOf(UserRole.BUYER.name),
                photoUrl = firebaseUser.photoUrl?.toString().orEmpty()
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val profile = document.toObject(User::class.java)
                ?: throw IllegalStateException("User profile not found.")
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateRunnerProtocol(): Result<User> {
        return try {
            val firebaseUser = auth.currentUser
                ?: throw IllegalStateException("Please sign in to unlock delivery tools.")
            val userRef = firestore.collection("users").document(firebaseUser.uid)
            firestore.runTransaction { transaction ->
                val current = transaction.get(userRef).toObject(User::class.java) ?: User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email.orEmpty(),
                    displayName = firebaseUser.displayName.orEmpty(),
                    role = UserRole.BUYER.name,
                    roles = listOf(UserRole.BUYER.name)
                )
                val updated = current.copy(
                    roles = (current.roles + current.role).distinct(),
                    trustScore = current.trustScore.coerceAtLeast(10),
                    runnerStatus = RunnerStatus.ACTIVE.name
                )
                transaction.set(userRef, updated)
                updated
            }.await()

            getCurrentUserProfile()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
