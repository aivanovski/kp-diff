package com.github.ai.kpdiff.domain.patch

import app.keemobile.kotpass.errors.FormatError
import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.kpdiff.domain.patch.model.EntityReference
import com.github.ai.kpdiff.domain.patch.model.EntryReference
import com.github.ai.kpdiff.domain.patch.model.Field
import com.github.ai.kpdiff.domain.patch.model.GroupEntity
import com.github.ai.kpdiff.domain.patch.model.GroupReference
import com.github.ai.kpdiff.domain.patch.model.PatchEvent
import com.github.ai.kpdiff.entity.exception.InvalidLineFormat
import com.github.ai.kpdiff.entity.exception.ParsingException
import java.util.LinkedList
import java.util.Queue

class PatchFileParser {

    fun parse(content: String): Either<ParsingException, List<PatchEvent>> =
        either {
            if (content.isBlank()) {
                raise(ParsingException("Empty .patch file"))
            }

            val allLines: Queue<String> = LinkedList<String>()
                .apply {
                    addAll(content.splitIntoLines())
                }

            val events = mutableListOf<PatchEvent>()

            while (allLines.isNotEmpty()) {
                val line = allLines.peek()
                val type = line.getLineType().bind()
                val indentLevel = line.getIndentLevel().bind()


                if (!line.isAnchorLine().bind()) {
                    raise(InvalidLineFormat(line))
                }

                val anchoredEvents = parseAnchoredEvents(allLines).bind()
                events.addAll(anchoredEvents)
            }

//            val events = content.splitIntoEventLines().bind()
//                .map { eventLiens ->
//
//                    val event = parseEntityEvent(eventLiens).bind()
//
//                    event
//                }
//
//            events
            listOf()
        }

    private fun String.isAnchorLine(): Either<ParsingException, Boolean> {
        val line = this
        return either {
            line.startsWith("~") && line.getIndentLevel().bind() == 2
        }
    }

    private fun String.getEntityType(): Either<ParsingException, EntityType> {
        val line = this

        return either {
            val trimmed = line.removePrefix().bind().trim()

            when {
                trimmed.startsWith(GROUP) -> EntityType.GROUP
                trimmed.startsWith(ENTRY) -> EntityType.ENTRY
                trimmed.startsWith(FIELD) -> EntityType.FIELD
                trimmed.startsWith(ATTACHMENT) -> EntityType.ATTACHMENT
                else -> raise(InvalidLineFormat(line))
            }
        }
    }

    private fun String.removePrefix(): Either<ParsingException, String> {
        val line = this

        return either {
            val prefix = when (line.getLineType().bind()) {
                LineType.UPDATE -> "~"
                LineType.INSERT -> "+"
                LineType.DELETE -> "-"
            }

            line.removePrefix(prefix = prefix)
        }
    }

    private fun String.getLineType(): Either<ParsingException, LineType> {
        val line = this

        return either {
            when {
                line.startsWith("~") -> LineType.UPDATE
                line.startsWith("+") -> LineType.INSERT
                line.startsWith("-") -> LineType.DELETE
                else -> raise(InvalidLineFormat(line))
            }
        }
    }


    private fun String.splitIntoLines(): List<String> {
        return this.split("\n")
            .map { line -> line.trim() }
            .filter { line -> line.isNotEmpty() }
    }

    private fun parseAnchoredEvents(allLines: Queue<String>): Either<ParsingException, List<PatchEvent>> =
        either {
            var parents = mutableListOf<EntityReference>()
            var fields = mutableMapOf<String, String>()

            while (true) {
                val line = allLines.poll()
                val indentLevel = line.getIndentLevel().bind()
                val lineType = line.getLineType().bind()
                val entityType = line.getEntityType().bind()

                val nextLine = allLines.peek()
                val nextIndentLevel = nextLine.getIndentLevel().bind()
                val isNextAnchorLine = nextLine.isAnchorLine().bind()

                when {
                    !isNextAnchorLine && nextIndentLevel > indentLevel -> {
                        if (lineType == LineType.UPDATE
                            && (entityType == EntityType.GROUP || entityType == EntityType.ENTRY)
                        ) {
                            val parent = parseParentReference(line).bind()
                            parents.add(parent)
                        } else if (lineType == LineType.INSERT || lineType == LineType.DELETE) {
                            val entity = parseParentReference(line).bind()

                            val entityName = when (entity) {
                                is GroupReference -> entity.name
                                is EntryReference -> entity.name
                                else -> raise(InvalidLineFormat(line))
                            }


                        } else {
                            raise(InvalidLineFormat(line))
                        }
                    }

                    !isNextAnchorLine && nextIndentLevel == indentLevel -> {
                        if (entityType == EntityType.FIELD) {

                        }

                    }
                }
            }

            listOf()
        }

    private fun readLinesByIndentLevel() {

    }

    private fun String.splitIntoEventLines(): Either<ParsingException, List<List<String>>> {
        val content = this

        return either {
            val allLines: Queue<String> = LinkedList<String>()
                .apply {
                    addAll(content.split("\n"))
                }

            val allEventsLines = mutableListOf<List<String>>()

            var eventLines = mutableListOf<String>()
            var prevIndentLevel: Int? = null

            while (allLines.isNotEmpty()) {
                val line = allLines.poll()
                if (line.isBlank()) {
                    continue
                }

                val indentLevel = line.getIndentLevel().bind()

                if (prevIndentLevel != null && prevIndentLevel > indentLevel) {
                    allEventsLines.add(eventLines)
                    eventLines = mutableListOf<String>()
                }

                eventLines.add(line)

                prevIndentLevel = indentLevel
            }

            if (eventLines.isNotEmpty()) {
                allEventsLines.add(eventLines)
            }

            allEventsLines
        }
    }

    private fun parseEntityEvent(lines: List<String>): Either<ParsingException, List<PatchEvent>> =
        either {
            val parents = mutableListOf<EntityReference>()
            val queue: Queue<String> = LinkedList(lines)

            val lastIndentLevel = queue.last().getIndentLevel().bind()

            while (queue.isNotEmpty()) {
                val line = queue.poll()
                val indentLine = line.getIndentLevel().bind()
                if (indentLine < lastIndentLevel) {
                    parents.add(parseParentReference(line).bind())
                } else {
                    println("Entity: $line")
                }
            }

            println("Parents: ${parents.size}")
            parents.forEach { println("    parent: $it") }

            listOf()
//            PatchEvent.Insert(
//                parents = parents,
//                entity = GroupEntity("name")
//            )
        }

    private fun parseParentReference(line: String): Either<ParsingException, EntityReference> =
        // TODO: refactor
        either {
            val valueLine = line
                .removePrefix("~")
                .removePrefix("-")
                .removePrefix("+")
                .trim()

            val valueStart = valueLine.indexOf("'")
            val valueEnd = valueLine.lastIndexOf("'")

            if (!valueLine.startsWith(GROUP) && !valueLine.startsWith(ENTRY)) {
                raise(ParsingException("Invalid line format: $line"))
            }

            val (_, typeEnd) = when {
                valueLine.startsWith(GROUP) -> {
                    val index = valueLine.indexOf(GROUP)
                    index to (index + GROUP.length)
                }

                valueLine.startsWith(ENTRY) -> {
                    val index = valueLine.indexOf(ENTRY)
                    index to (index + ENTRY.length)
                }

                else -> raise(ParsingException("Invalid line format: $line"))
            }

            if (valueStart <= typeEnd || valueEnd <= typeEnd || valueStart + 1 >= valueEnd) {
                raise(ParsingException("Invalid line format: $line"))
            }

            when {
                valueLine.startsWith(GROUP) -> {
                    GroupReference(
                        name = valueLine.substring(valueStart + 1, valueEnd)
                    )
                }

                valueLine.startsWith(ENTRY) -> {
                    EntryReference(
                        name = valueLine.substring(valueStart + 1, valueEnd)
                    )
                }

                else -> raise(ParsingException("Invalid line format: $line"))
            }
        }

    private fun parseEntity(lines: List<String>): Either<ParsingException, GroupEntity> =
        either {

            raise(ParsingException(""))
        }

    private fun parseField(line: String): Either<ParsingException, Field> =
        either {
            val lineType = line.getLineType().bind()

            if (lineType == LineType.INSERT) {

            }

            Field(
                name = "",
                value = ""
            )
        }

    private fun String.isValidPatchLine(): Boolean {
        val line = this.trim()
        if (line.isBlank()) {
            return false
        }

        return false
    }

    private fun String.getIndentLevel(): Either<ParsingException, Int> {
        val line = this.trim()

        return either {
            // TODO: check line starts with ~, - or +

            var index = 1
            while (line[index].isWhitespace()) {
                index++
            }

            index
        }
    }


    companion object {
        private const val GROUP = "Group"
        private const val ENTRY = "Entry"
        private const val FIELD = "Field"
        private const val ATTACHMENT = "Attachment"
    }

    enum class LineType {
        INSERT,
        DELETE,
        UPDATE
    }

    enum class EntityType {
        GROUP,
        ENTRY,
        FIELD,
        ATTACHMENT
    }
}

