package com.bonepeople.android.starvault.global.storage

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object VaultCryptography {
    private val cipherProvider by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        BouncyCastleProvider()
    }

    fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        if (salt.size != VaultFileProtocol.SALT_SIZE) {
            throw VaultCodecException.InvalidFile("Salt length is invalid")
        }
        val parameters = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(VaultFileProtocol.ARGON2_VERSION)
            .withMemoryAsKB(VaultFileProtocol.ARGON2_MEMORY_KIB)
            .withIterations(VaultFileProtocol.ARGON2_ITERATIONS)
            .withParallelism(VaultFileProtocol.ARGON2_PARALLELISM)
            .withSalt(salt)
            .build()
        val key = ByteArray(VaultFileProtocol.FILE_KEY_SIZE)
        return try {
            Argon2BytesGenerator().apply { init(parameters) }.generateBytes(password, key)
            key
        } catch (throwable: Throwable) {
            key.fill(0)
            throw throwable
        } finally {
            parameters.clear()
        }
    }

    fun createCipher(
        mode: Int,
        key: ByteArray,
        nonce: ByteArray,
        authenticatedHeader: ByteArray,
    ): Cipher {
        if (key.size != VaultFileProtocol.FILE_KEY_SIZE) {
            throw VaultCodecException.InvalidFile("File key length is invalid")
        }
        if (nonce.size != VaultFileProtocol.NONCE_SIZE) {
            throw VaultCodecException.InvalidFile("Nonce length is invalid")
        }
        return try {
            Cipher.getInstance("AES/GCM/NoPadding", cipherProvider).apply {
                init(
                    mode,
                    SecretKeySpec(key, "AES"),
                    GCMParameterSpec(VaultFileProtocol.AUTHENTICATION_TAG_SIZE * 8, nonce),
                )
                updateAAD(authenticatedHeader)
            }
        } catch (exception: GeneralSecurityException) {
            throw VaultCodecException.Unsupported("AES-256-GCM is unavailable", exception)
        }
    }
}