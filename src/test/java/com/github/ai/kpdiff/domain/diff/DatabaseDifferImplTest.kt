package com.github.ai.kpdiff.domain.diff

import com.github.ai.kpdiff.TestData.DB_WITH_PASSWORD
import com.github.ai.kpdiff.TestData.DB_WITH_PASSWORD_MODIFIED
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.EntryEntity.Companion.PROPERTY_TITLE
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.open
import com.github.ai.kpdiff.utils.buildNodeTree
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class DatabaseDifferImplTest {

    @Test
    fun `diff should work with real database`() {
        // arrange
        val lhs = KeepassDatabase(
            root = DB_WITH_PASSWORD.open().content.group.buildNodeTree()
        )
        val rhs = KeepassDatabase(
            root = DB_WITH_PASSWORD_MODIFIED.open().content.group.buildNodeTree()
        )

        // act
        val diff = DatabaseDifferImpl().getDiff(lhs, rhs)

        // assert
        diff.size shouldBe 5

        val first = diff[0]
        val second = diff[1]
        val third = diff[2]
        val fourth = diff[3]
        val fifth = diff[4]

        first.shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
        first.node.value.name shouldBe "Inner group 3"

        second.shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
        second.node.value.name shouldBe "Inner group 2"

        third.shouldBeInstanceOf<DiffEvent.Update<EntryEntity>>()
        third.oldNode.value.properties[PROPERTY_TITLE] shouldBe "Entry 4"
        third.newNode.value.properties[PROPERTY_TITLE] shouldBe "Entry 4 modified"

        fourth.shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
        fourth.node.value.properties[PROPERTY_TITLE] shouldBe "Entry 3"

        fifth.shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
        fifth.node.value.properties[PROPERTY_TITLE] shouldBe "Entry 5"
    }
}