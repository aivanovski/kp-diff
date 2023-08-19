package com.github.ai.kpdiff.domain.diff.differ

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
import com.github.aivanovski.keepasstreediff.PathDiffer
import com.github.aivanovski.keepasstreediff.UuidDiffer
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.UUID
import org.junit.jupiter.api.Test

internal class DatabaseDifferTest {

    @Test
    fun `diff should work with real database`() {
        listOf(
            PathDatabaseDiffer(differ = PathDiffer()),
            UuidDatabaseDiffer(differ = UuidDiffer())
        ).forEach { differ ->

            // arrange
            val lhs = KeepassDatabase(
                root = DB_WITH_PASSWORD.open().content.group.buildNodeTree()
            )
            val rhs = KeepassDatabase(
                root = DB_WITH_PASSWORD_MODIFIED.open().content.group.buildNodeTree()
            )

            // act
            val diff = differ.getDiff(lhs, rhs)

            // assert
            diff.lhs shouldBe lhs
            diff.rhs shouldBe rhs

            val events = diff.events.sortForAssertion()
            events.size shouldBe 5

            val iterator = events.iterator()
            with(iterator.next()) {
                shouldBeInstanceOf<DiffEvent.Delete<GroupEntity>>()
                parentUuid shouldBe UUID.fromString("a5f9fa21-73cf-4da8-9c5c-39f8dd61e9c2")
                entity.name shouldBe "Inner group 2"
            }

            with(iterator.next()) {
                shouldBeInstanceOf<DiffEvent.Insert<GroupEntity>>()
                parentUuid shouldBe UUID.fromString("a5f9fa21-73cf-4da8-9c5c-39f8dd61e9c2")
                entity.name shouldBe "Inner group 3"
            }

            with(iterator.next()) {
                shouldBeInstanceOf<DiffEvent.Delete<EntryEntity>>()
                parentUuid shouldBe UUID.fromString("f7adcc56-92da-4ac9-b72e-441a3add52ca")
                entity.getTitle() shouldBe "Entry 3"
            }

            with(iterator.next()) {
                shouldBeInstanceOf<DiffEvent.Insert<EntryEntity>>()
                parentUuid shouldBe UUID.fromString("6a09a1cd-b39e-5564-f736-a9fd0993bd80")
                entity.getTitle() shouldBe "Entry 5"
            }

            with(iterator.next()) {
                shouldBeInstanceOf<DiffEvent.Update<FieldEntity>>()

                oldParentUuid shouldBe UUID.fromString("a97f8961-018e-411c-be10-59a4b6777433")
                oldEntity.name shouldBe "Title"
                oldEntity.value shouldBe "Entry 4"

                newParentUuid shouldBe UUID.fromString("a97f8961-018e-411c-be10-59a4b6777433")
                newEntity.name shouldBe "Title"
                newEntity.value shouldBe "Entry 4 modified"
            }
        }
    }
}