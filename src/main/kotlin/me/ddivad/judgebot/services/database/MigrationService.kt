package me.ddivad.judgebot.services.database

import me.ddivad.judgebot.dataclasses.Meta
import me.ddivad.judgebot.services.database.migrations.*
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.annotations.Service

@Service
class MigrationService(private val database: DatabaseService, private val connection: ConnectionService) {
    suspend fun runMigrations() {
        var meta = database.meta.getCurrentVersion()

        if (meta == null) {
            meta = Meta(0)
            database.meta.save(meta)
        }

        var currentVersion = meta.version
        println("Current DB Version: v$currentVersion")

        while(true) {
            val nextVersion = currentVersion + 1
            try {
                when(nextVersion) {
                    1 -> ::v1
                    2 -> ::v2
                    else -> break
                }(connection.db)

            } catch (t: Throwable) {
                println("Failed to migrate database to v$nextVersion")
                throw t
            }
            currentVersion = nextVersion
        }
        if (currentVersion != meta.version) {
            meta = meta.copy(version = currentVersion)
            database.meta.save(meta)
            println("Finished database migrations.")
        }
    }
}