package com.bonepeople.android.starvault.global.storage

internal interface VaultSchemaMigration {
    val sourceVersion: Int
    val targetVersion: Int

    fun migrate(payload: VaultPayloadInfo): VaultPayloadInfo
}

internal object VaultSchemaMigrationRegistry {
    private const val FIRST_SCHEMA_VERSION = 1

    private val registeredMigrations = emptyList<VaultSchemaMigration>()
    private val migrationBySourceVersion = registeredMigrations.associateBy { migration ->
        migration.sourceVersion
    }.also { migrationMap ->
        require(migrationMap.size == registeredMigrations.size) { "Duplicate schema migration source version" }
        registeredMigrations.forEach { migration ->
            require(migration.sourceVersion in FIRST_SCHEMA_VERSION until VaultFileProtocol.SCHEMA_VERSION) {
                "Schema migration source version is outside the supported range"
            }
            require(migration.targetVersion == migration.sourceVersion + 1) {
                "Schema migrations must advance exactly one version"
            }
        }
    }

    fun requireReadableVersion(schemaVersion: Int) {
        if (schemaVersion !in FIRST_SCHEMA_VERSION..VaultFileProtocol.SCHEMA_VERSION) {
            throw unsupported(schemaVersion)
        }
        var currentVersion = schemaVersion
        while (currentVersion < VaultFileProtocol.SCHEMA_VERSION) {
            val migration = migrationBySourceVersion[currentVersion] ?: throw unsupported(schemaVersion)
            currentVersion = migration.targetVersion
        }
    }

    fun requireCurrentVersion(schemaVersion: Int) {
        if (schemaVersion != VaultFileProtocol.SCHEMA_VERSION) {
            throw unsupported(schemaVersion)
        }
    }

    fun migrateToCurrent(payload: VaultPayloadInfo): VaultPayloadInfo {
        requireReadableVersion(payload.schemaVersion)
        val expectedVaultId = payload.vault.id
        val expectedRevision = payload.revision
        var migratedPayload = payload
        while (migratedPayload.schemaVersion < VaultFileProtocol.SCHEMA_VERSION) {
            val migration = migrationBySourceVersion.getValue(migratedPayload.schemaVersion)
            val migrated = migration.migrate(migratedPayload)
            if (migrated.schemaVersion != migration.targetVersion) {
                throw VaultCodecException.InvalidFile("Schema migration produced an invalid target version")
            }
            if (migrated.vault.id != expectedVaultId || migrated.revision != expectedRevision) {
                throw VaultCodecException.InvalidFile("Schema migration changed the vault identity or revision")
            }
            migratedPayload = migrated
        }
        return migratedPayload
    }

    private fun unsupported(schemaVersion: Int): VaultCodecException.Unsupported {
        return VaultCodecException.Unsupported("Unsupported schema version: $schemaVersion")
    }
}