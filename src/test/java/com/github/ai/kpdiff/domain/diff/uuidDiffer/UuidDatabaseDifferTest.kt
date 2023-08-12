package com.github.ai.kpdiff.domain.diff.uuidDiffer

import com.github.ai.kpdiff.TestData.DB_WITH_PASSWORD
import com.github.ai.kpdiff.TestData.DB_WITH_PASSWORD_MODIFIED
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.EntryEntity
import com.github.ai.kpdiff.entity.FieldEntity
import com.github.ai.kpdiff.entity.GroupEntity
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.open
import com.github.ai.kpdiff.testUtils.sortForAssertion
import com.github.ai.kpdiff.utils.buildNodeTree
import com.github.ai.kpdiff.utils.getTitle
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class UuidDatabaseDifferTest {

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
        val diff = UuidDatabaseDiffer().getDiff(lhs, rhs)

        // assert
        diff.lhs shouldBe lhs
        diff.rhs shouldBe rhs

        val events = diff.events.sortForAssertion()
        events.size shouldBe 5

        val iterator = events.iterator()
        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
            node.value.name shouldBe "Inner group 2"
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
            node.value.name shouldBe "Inner group 3"
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
            node.value.getTitle() shouldBe "Entry 3"
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
            node.value.getTitle() shouldBe "Entry 5"
        }

        with(iterator.next()) {
            shouldBeInstanceOf<DiffEvent.Update<FieldEntity>>()
            oldNode.value.name shouldBe "Title"
            oldNode.value.value shouldBe "Entry 4"
            newNode.value.value shouldBe "Entry 4 modified"
        }
    }
}