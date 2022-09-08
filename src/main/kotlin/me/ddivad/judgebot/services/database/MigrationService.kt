package me.ddivad.judgebot.services.database

import me.ddivad.judgebot.dataclasses.Meta
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.database.migrations.v1
import me.ddivad.judgebot.services.database.migrations.v2
import me.jakejmattson.discordkt.annotations.Service
import mu.KotlinLogging

@Service
class MigrationService(private val database: DatabaseService, private val connection: ConnectionService) {
    private val logger = KotlinLogging.logger { }

    suspend fun runMigrations() {
        var meta = database.meta.getCurrentVersion()

        if (meta == null) {
            meta = Meta(0)
            database.meta.save(meta)
        }

        var currentVersion = meta.version
        logger.info { "Current DB Version: v$currentVersion" }

        while (true) {
            val nextVersion = currentVersion + 1
            try {
                when (nextVersion) {
                    1 -> ::v1
                    2 -> ::v2
                    else -> break
                }(connection.db)

            } catch (t: Throwable) {
                logger.error(t) { "Failed to migrate database to v$nextVersion" }
                throw t
            }
            currentVersion = nextVersion
        }
        if (currentVersion != meta.version) {
            meta = meta.copy(version = currentVersion)
            database.meta.save(meta)
            logger.info { "Finished database migrations." }
        }
    }
}