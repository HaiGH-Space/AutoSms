package com.gmail.nthai.data.local

import com.gmail.nthai.di.DatabaseModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DatabaseMigrationTest {

    @Test
    fun testAllMigrationsDefinedAndOrdered() {
        val migrations = DatabaseModule.ALL_MIGRATIONS
        assertNotNull(migrations)
        assertEquals(12, migrations.size)

        // Verify version pairs
        assertEquals(1, migrations[0].startVersion)
        assertEquals(2, migrations[0].endVersion)

        assertEquals(4, migrations[1].startVersion)
        assertEquals(5, migrations[1].endVersion)

        assertEquals(5, migrations[2].startVersion)
        assertEquals(6, migrations[2].endVersion)

        assertEquals(6, migrations[3].startVersion)
        assertEquals(7, migrations[3].endVersion)

        assertEquals(7, migrations[4].startVersion)
        assertEquals(8, migrations[4].endVersion)

        assertEquals(8, migrations[5].startVersion)
        assertEquals(9, migrations[5].endVersion)

        assertEquals(9, migrations[6].startVersion)
        assertEquals(10, migrations[6].endVersion)

        assertEquals(10, migrations[7].startVersion)
        assertEquals(11, migrations[7].endVersion)

        assertEquals(11, migrations[8].startVersion)
        assertEquals(12, migrations[8].endVersion)

        assertEquals(12, migrations[9].startVersion)
        assertEquals(13, migrations[9].endVersion)

        assertEquals(13, migrations[10].startVersion)
        assertEquals(14, migrations[10].endVersion)

        assertEquals(14, migrations[11].startVersion)
        assertEquals(15, migrations[11].endVersion)
    }

    @Test
    fun testLatestDatabaseVersionMatchesAppDatabase() {
        val latestMigration = DatabaseModule.ALL_MIGRATIONS.last()
        assertEquals(15, latestMigration.endVersion)
    }
}
