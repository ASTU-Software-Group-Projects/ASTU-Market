package com.market.astu.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.market.astu.data.model.RunnerStatus
import com.market.astu.data.model.User
import com.market.astu.data.model.UserRegistration
import com.market.astu.data.model.Wallet
import com.market.astu.data.model.WalletTransaction
import com.market.astu.data.model.WalletTransactionType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isUserAuthenticated(): Boolean = auth.currentUser != null

    suspend fun signUpWithEmail(registration: UserRegistration): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(
                registration.email,
                registration.password
            ).await()
            val firebaseUser = authResult.user ?: throw Exception("Registration failed")
            firebaseUser.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(registration.displayName)
                    .build()
            ).await()
            val user = User(
                uid = firebaseUser.uid,
                email = registration.email,
                displayName = registration.displayName,
                phoneNumber = registration.phoneNumber,
                address = registration.address,
                role = registration.primaryRole().name,
                roles = registration.normalizedRoles(),
                storeName = registration.storeName,
                bio = registration.bio,
                runnerStatus = RunnerStatus.INACTIVE.name
            )
            val wallet = Wallet(
                uid = firebaseUser.uid,
                balance = 0.0,
                monthlyYieldEarned = 0.0,
                updatedAt = Timestamp.now()
            )
            val welcomeTx = WalletTransaction(
                amount = 0.0,
                type = WalletTransactionType.TOP_UP.name,
                description = "Wallet created",
                timestamp = Timestamp.now()
            )
            val batch = firestore.batch()
            val userRef = firestore.collection("users").document(firebaseUser.uid)
            val walletRef = firestore.collection("wallets").document(firebaseUser.uid)
            val welcomeRef = walletRef.collection("transactions").document()
            batch.set(userRef, user)
            batch.set(walletRef, wallet)
            batch.set(welcomeRef, welcomeTx)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
