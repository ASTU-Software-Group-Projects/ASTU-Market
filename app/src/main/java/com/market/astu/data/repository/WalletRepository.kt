package com.market.astu.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.market.astu.data.model.User
import com.market.astu.data.model.Wallet
import com.market.astu.data.model.WalletTransaction
import com.market.astu.data.model.WalletTransactionType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Please sign in to use your wallet.")
    }

    suspend fun getWallet(): Result<Wallet> {
        return try {
            val uid = requireUserId()
            val walletRef = firestore.collection("wallets").document(uid)
            val snapshot = walletRef.get().await()
            val wallet = snapshot.toObject(Wallet::class.java) ?: Wallet(uid = uid)
            if (!snapshot.exists()) {
                walletRef.set(wallet.copy(updatedAt = Timestamp.now())).await()
            }
            Result.success(wallet.copy(uid = uid))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(limit: Long = 25): Result<List<WalletTransaction>> {
        return try {
            val uid = requireUserId()
            val snapshot = firestore.collection("wallets")
                .document(uid)
                .collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val transactions = snapshot.documents.mapNotNull { document ->
                document.toObject(WalletTransaction::class.java)?.copy(id = document.id)
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun topUp(amount: Double): Result<Unit> {
        return try {
            require(amount > 0.0) { "Top-up amount must be greater than zero." }
            val uid = requireUserId()
            val walletRef = firestore.collection("wallets").document(uid)
            val transactionRef = walletRef.collection("transactions").document()
            firestore.runTransaction { transaction ->
                val currentWallet = transaction.get(walletRef).toObject(Wallet::class.java) ?: Wallet(uid = uid)
                val updatedWallet = currentWallet.copy(
                    uid = uid,
                    balance = currentWallet.balance + amount,
                    updatedAt = Timestamp.now()
                )
                transaction.set(walletRef, updatedWallet)
                transaction.set(
                    transactionRef,
                    WalletTransaction(
                        amount = amount,
                        type = WalletTransactionType.TOP_UP.name,
                        description = "ETB top-up",
                        timestamp = Timestamp.now()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Send / Receive ---

    suspend fun findUserByPhone(phone: String): Result<User> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("phoneNumber", phone)
                .limit(1)
                .get()
                .await()
            val user = snapshot.documents.firstOrNull()?.toObject(User::class.java)
                ?: throw IllegalStateException("No user found with that phone number.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMoney(toUid: String, amount: Double): Result<Unit> {
        return try {
            require(amount > 0) { "Amount must be greater than zero." }
            val fromUid = requireUserId()
            if (fromUid == toUid) throw IllegalStateException("You cannot send money to yourself.")
            val fromWalletRef = firestore.collection("wallets").document(fromUid)
            val toWalletRef = firestore.collection("wallets").document(toUid)
            val fromTxRef = fromWalletRef.collection("transactions").document()
            val toTxRef = toWalletRef.collection("transactions").document()
            firestore.runTransaction { transaction ->
                val fromWallet = transaction.get(fromWalletRef).toObject(Wallet::class.java)
                    ?: throw IllegalStateException("Sender wallet not found.")
                if (fromWallet.balance < amount) throw IllegalStateException("Insufficient balance.")
                val toWallet = transaction.get(toWalletRef).toObject(Wallet::class.java) ?: Wallet(uid = toUid)
                val now = Timestamp.now()
                transaction.set(fromWalletRef, fromWallet.copy(
                    uid = fromUid,
                    balance = fromWallet.balance - amount,
                    updatedAt = now
                ))
                transaction.set(toWalletRef, toWallet.copy(
                    uid = toUid,
                    balance = toWallet.balance + amount,
                    updatedAt = now
                ))
                transaction.set(fromTxRef, WalletTransaction(
                    amount = -amount,
                    type = WalletTransactionType.SEND.name,
                    description = "Sent to user $toUid",
                    timestamp = now
                ))
                transaction.set(toTxRef, WalletTransaction(
                    amount = amount,
                    type = WalletTransactionType.RECEIVE.name,
                    description = "Received from user $fromUid",
                    timestamp = now
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Voucher top-up ---

    suspend fun topUpFromVoucher(voucherCode: String): Result<Unit> {
        return try {
            val voucherRef = firestore.collection("vouchers").document(voucherCode)
            val voucherSnapshot = voucherRef.get().await()
            if (!voucherSnapshot.exists()) throw IllegalStateException("Invalid voucher code.")
            val amount = voucherSnapshot.getDouble("amount") ?: 0.0
            require(amount > 0) { "Voucher has no value." }
            val alreadyRedeemed = voucherSnapshot.getBoolean("redeemed") ?: false
            if (alreadyRedeemed) throw IllegalStateException("Voucher already redeemed.")
            val uid = requireUserId()
            val walletRef = firestore.collection("wallets").document(uid)
            firestore.runTransaction { transaction ->
                val wallet = transaction.get(walletRef).toObject(Wallet::class.java) ?: Wallet(uid = uid)
                transaction.update(voucherRef, mapOf(
                    "redeemed" to true,
                    "redeemedBy" to uid,
                    "redeemedAt" to Timestamp.now()
                ))
                transaction.set(walletRef, wallet.copy(
                    uid = uid,
                    balance = wallet.balance + amount,
                    updatedAt = Timestamp.now()
                ))
                val txRef = walletRef.collection("transactions").document()
                transaction.set(txRef, WalletTransaction(
                    amount = amount,
                    type = WalletTransactionType.VOUCHER_TOPUP.name,
                    description = "Voucher top-up: $voucherCode",
                    timestamp = Timestamp.now()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Airtime purchase (deducts balance) ---

    suspend fun buyAirtime(phoneNumber: String, amount: Double): Result<Unit> {
        return try {
            require(amount > 0) { "Amount must be positive." }
            val uid = requireUserId()
            val walletRef = firestore.collection("wallets").document(uid)
            val txRef = walletRef.collection("transactions").document()
            firestore.runTransaction { transaction ->
                val wallet = transaction.get(walletRef).toObject(Wallet::class.java)
                    ?: throw IllegalStateException("Wallet not found.")
                if (wallet.balance < amount) throw IllegalStateException("Insufficient balance.")
                val now = Timestamp.now()
                transaction.set(walletRef, wallet.copy(
                    balance = wallet.balance - amount,
                    updatedAt = now
                ))
                transaction.set(txRef, WalletTransaction(
                    amount = -amount,
                    type = WalletTransactionType.AIRTIME.name,
                    description = "Airtime recharge to $phoneNumber",
                    timestamp = now
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
