package com.github.ai.kpdiff.domain.diff.differ

import com.github.ai.kpdiff.DatabaseFactory.PASSWORD_KEY
import com.github.ai.kpdiff.DatabaseFactory.createAttachmentsDatabase
import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.DatabaseFactory.createModifiedAttachmentsDatabases
import com.github.ai.kpdiff.DatabaseFactory.createModifiedDatabase
import com.github.ai.kpdiff.TestData.BINARY_1
import com.github.ai.kpdiff.TestData.BINARY_2
import com.github.ai.kpdiff.TestData.BINARY_3
import com.github.ai.kpdiff.TestData.BINARY_4
import com.github.ai.kpdiff.TestData.BINARY_5
import com.github.ai.kpdiff.TestData.BINARY_6
import com.github.ai.kpdiff.TestData.ENTRY_BINARIES_CHANGE
import com.github.ai.kpdiff.TestData.ENTRY_BINARIES_DELETE
import com.github.ai.kpdiff.TestData.ENTRY_BINARIES_INSERT
import com.github.ai.kpdiff.TestData.ENTRY_CLOUD_KEYS
import com.github.ai.kpdiff.TestData.ENTRY_FACEBOOK
import com.github.ai.kpdiff.TestData.ENTRY_GITHUB
import com.github.ai.kpdiff.TestData.ENTRY_GITLAB
import com.github.ai.kpdiff.TestData.ENTRY_GOOGLE
import com.github.ai.kpdiff.TestData.ENTRY_GOOGLE_MODIFIED
import com.github.ai.kpdiff.TestData.GROUP_CODING
import com.github.ai.kpdiff.TestData.GROUP_EMAIL
import com.github.ai.kpdiff.TestData.GROUP_INTERNET
import com.github.ai.kpdiff.TestData.GROUP_ROOT
import com.github.ai.kpdiff.TestData.GROUP_SHOPPING
import com.github.ai.kpdiff.TestData.GROUP_SOCIAL
import com.github.ai.kpdiff.TestData.NEW_KEY
import com.github.ai.kpdiff.TestData.OLD_KEY
import com.github.ai.kpdiff.TestEntityFactory.newField
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffEvent
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.buildNodeTree
import com.github.ai.kpdiff.testUtils.sortForAssertion
import com.github.ai.kpdiff.utils.Fields.FIELD_NOTES
import com.github.aivanovski.keepasstreediff.PathDiffer
import com.github.aivanovski.keepasstreediff.UuidDiffer
import io.kotest.matchers.shouldBe
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
                root = createDatabase(PASSWORD_KEY).buildNodeTree()
            )
            val rhs = KeepassDatabase(
                root = createModifiedDatabase(PASSWORD_KEY).buildNodeTree()
            )

            // act
            val diff = differ.getDiff(lhs, rhs)

            // assert
            diff.lhs shouldBe lhs
            diff.rhs shouldBe rhs

            val events = diff.events.sortForAssertion()
            events shouldBe createExpectedEvents()
        }
    }

    @Test
    fun `diff should work with attachments`() {
        listOf(
            PathDatabaseDiffer(differ = PathDiffer()),
            UuidDatabaseDiffer(differ = UuidDiffer())
        ).forEach { differ ->
            // arrange
            val lhs = KeepassDatabase(
                root = createAttachmentsDatabase(PASSWORD_KEY).buildNodeTree()
            )
            val rhs = KeepassDatabase(
                root = createModifiedAttachmentsDatabases(PASSWORD_KEY).buildNodeTree()
            )

            // act
            val diff = differ.getDiff(lhs, rhs)

            // assert
            diff.lhs shouldBe lhs
            diff.rhs shouldBe rhs

            val events = diff.events.sortForAssertion()
            events shouldBe createExpectedAttachmentsEvents()
        }
    }

    companion object {

        private fun createExpectedAttachmentsEvents(): List<DiffEvent<DatabaseEntity>> {
            return listOf(
                DiffEvent.Delete(
                    parentUuid = ENTRY_BINARIES_DELETE.uuid,
                    entity = newField(
                        name = BINARY_3.name,
                        value = BINARY_3.data
                    )
                ),
                DiffEvent.Delete(
                    parentUuid = ENTRY_BINARIES_DELETE.uuid,
                    entity = newField(
                        name = BINARY_4.name,
                        value = BINARY_4.data
                    )
                ),
                DiffEvent.Delete(
                    parentUuid = ENTRY_BINARIES_CHANGE.uuid,
                    entity = newField(
                        name = BINARY_5.name,
                        value = BINARY_5.data
                    )
                ),

                DiffEvent.Insert(
                    parentUuid = ENTRY_BINARIES_INSERT.uuid,
                    entity = newField(
                        name = BINARY_1.name,
                        value = BINARY_1.data
                    )
                ),
                DiffEvent.Insert(
                    parentUuid = ENTRY_BINARIES_INSERT.uuid,
                    entity = newField(
                        name = BINARY_2.name,
                        value = BINARY_2.data
                    )
                ),
                DiffEvent.Insert(
                    parentUuid = ENTRY_BINARIES_CHANGE.uuid,
                    entity = newField(
                        name = BINARY_6.name,
                        value = BINARY_6.data
                    )
                )
            )
        }

        private fun createExpectedEvents(): List<DiffEvent<DatabaseEntity>> {
            return listOf(
                DiffEvent.Delete(
                    parentUuid = GROUP_ROOT.uuid,
                    entity = GROUP_EMAIL
                ),
                DiffEvent.Insert(
                    parentUuid = GROUP_INTERNET.uuid,
                    entity = GROUP_SHOPPING
                ),

                DiffEvent.Delete(
                    parentUuid = GROUP_CODING.uuid,
                    entity = ENTRY_GITHUB
                ),

                DiffEvent.Insert(
                    parentUuid = GROUP_SOCIAL.uuid,
                    entity = ENTRY_FACEBOOK
                ),
                DiffEvent.Insert(
                    parentUuid = GROUP_CODING.uuid,
                    entity = ENTRY_GITLAB
                ),

                DiffEvent.Update(
                    oldParentUuid = ENTRY_GOOGLE.uuid,
                    newParentUuid = ENTRY_GOOGLE.uuid,
                    oldEntity = newField(
                        name = FIELD_NOTES,
                        value = ENTRY_GOOGLE.fields[FIELD_NOTES].orEmpty()
                    ),
                    newEntity = newField(
                        name = FIELD_NOTES,
                        value = ENTRY_GOOGLE_MODIFIED.fields[FIELD_NOTES].orEmpty()
                    )
                ),

                DiffEvent.Delete(
                    parentUuid = ENTRY_CLOUD_KEYS.uuid,
                    entity = newField(
                        name = OLD_KEY.name,
                        value = OLD_KEY.data
                    )
                ),
                DiffEvent.Insert(
                    parentUuid = ENTRY_CLOUD_KEYS.uuid,
                    entity = newField(
                        name = NEW_KEY.name,
                        value = NEW_KEY.data
                    )
                )
            )
        }
    }
}