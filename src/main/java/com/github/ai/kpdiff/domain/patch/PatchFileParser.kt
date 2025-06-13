package com.github.ai.kpdiff.domain.patch

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.github.ai.kpdiff.domain.patch.model.EntityReference
import com.github.ai.kpdiff.domain.patch.model.EntityType
import com.github.ai.kpdiff.domain.patch.model.EntryEntity
import com.github.ai.kpdiff.domain.patch.model.EntryReference
import com.github.ai.kpdiff.domain.patch.model.Field
import com.github.ai.kpdiff.domain.patch.model.GroupEntity
import com.github.ai.kpdiff.domain.patch.model.GroupReference
import com.github.ai.kpdiff.domain.patch.model.LineToken
import com.github.ai.kpdiff.domain.patch.model.TokenValue
import com.github.ai.kpdiff.domain.patch.model.PatchEvent
import com.github.ai.kpdiff.domain.patch.model.TextLine
import com.github.ai.kpdiff.domain.patch.model.TokenType
import com.github.ai.kpdiff.entity.exception.InvalidPatchFileFormat
import com.github.ai.kpdiff.entity.exception.InvalidPatchLineFormat
import com.github.ai.kpdiff.entity.exception.PatchParsingException
import java.util.LinkedList
import java.util.Queue

class PatchFileParser {

    fun parse(content: String): Either<PatchParsingException, List<PatchEvent>> =
        either {
            if (content.isBlank()) {
                raise(PatchParsingException("Empty .patch file"))
            }

            val allLines: Queue<TextLine> = LinkedList<TextLine>()
                .apply {
                    addAll(content.splitIntoLines())
                }

            val events = mutableListOf<PatchEvent>()

            while (allLines.isNotEmpty()) {
                val line = allLines.peek()

                if (!line.isAnchorLine().bind()) {
                    raise(InvalidPatchLineFormat(line))
                }

                val anchoredEvents = parseAnchoredEvents(allLines).bind()
                events.addAll(anchoredEvents)
            }

            events
        }

    private fun TextLine.isAnchorLine(): Either<PatchParsingException, Boolean> {
        val line = this
        return either {
            line.text.startsWith("~") && line.getIndentLevel().bind() == 2
        }
    }

//    private fun String.removePrefix(): Either<ParsingException, String> {
//        val line = this
//
//        return either {
//            val prefix = when (line.getLineType().bind()) {
//                LineType.UPDATE -> "~"
//                LineType.INSERT -> "+"
//                LineType.DELETE -> "-"
//            }
//
//            line.removePrefix(prefix = prefix)
//        }
//    }

    private fun String.splitIntoLines(): List<TextLine> {
        return this.split("\n")
            .mapIndexed { index, line ->
                TextLine(
                    number = index + 1,
                    text = line
                )
            }
            .filter { line -> line.text.isNotEmpty() }
    }

    private fun parseAnchoredEvents(
        allLines: Queue<TextLine>
    ): Either<PatchParsingException, List<PatchEvent>> =
        either {
            val events = mutableListOf<PatchEvent>()

            var parents = mutableListOf<EntityReference>()

            while (allLines.isNotEmpty()) {
                val line = allLines.poll()
                val token = line.tokenize().bind()

                val isNextLineAnchor = allLines.peek()?.isAnchorLine()?.getOrNull() == true
                val nextLine: TextLine? = if (!isNextLineAnchor) {
                    allLines.peek()
                } else {
                    null
                }
                val nextToken = nextLine?.tokenize()?.getOrNull()

                when {
                    nextToken != null && token.isParentReference() -> {
                        val parent = when (token.entityType) {
                            EntityType.GROUP -> {
                                GroupReference(
                                    name = (token.value as TokenValue.Name).value
                                )
                            }

                            EntityType.ENTRY -> {
                                EntryReference(
                                    name = (token.value as TokenValue.Name).value
                                )
                            }

                            else -> raise(InvalidPatchLineFormat(line))
                        }

                        parents.add(parent)
                    }

                    token.isGroupInsertionOrDeletion()
                        && (nextToken == null || nextToken.isGroupInsertionOrDeletion()) -> {
                        val event = parseGroup(
                            parents = parents,
                            token = token
                        ).bind()

                        events.add(event)
                        parents = mutableListOf()
                    }

                    token.isEntryDeclaration() && nextToken != null && nextToken.isFieldDeclaration() -> {
                        val entryEvents = parseEntry(
                            allLines = allLines,
                            parents = parents,
                            entryToken = token,
                            targetIndentLevel = nextLine.getIndentLevel().bind()
                        ).bind()

                        events.addAll(entryEvents)
                        parents = mutableListOf()
                    }

                    else -> {
                        raise(PatchParsingException("Unhandled line: $line"))
                    }
                }
            }

            events
        }

    private fun LineToken.isParentReference(): Boolean {
        return type == TokenType.UPDATE
            && entityType == EntityType.GROUP
    }

    private fun LineToken.isGroupInsertionOrDeletion(): Boolean {
        return (type == TokenType.DELETE || type == TokenType.INSERT)
            && entityType == EntityType.GROUP
    }

    private fun LineToken.isEntryDeclaration(): Boolean {
        return entityType == EntityType.ENTRY
    }

    private fun readLinesWithIndent(
        allLines: Queue<TextLine>,
        targetIndentLevel: Int
    ): Either<PatchParsingException, List<TextLine>> =
        either {
            val lines = mutableListOf<TextLine>()

            while (allLines.isNotEmpty()) {
                val line = allLines.peek()
                val indentLevel = line.getIndentLevel().bind()
                if (indentLevel == targetIndentLevel) {
                    lines.add(allLines.poll())
                } else {
                    break
                }
            }

            if (lines.isEmpty()) {
                raise(PatchParsingException("Invalid .patch file format"))
            }

            lines
        }

    private fun parseGroup(
        parents: List<EntityReference>,
        token: LineToken
    ): Either<PatchParsingException, PatchEvent> =
        either {
            val name = (token.value as? TokenValue.Name)?.value
                ?: raise(InvalidPatchLineFormat(token.line))

            when (token.type) {
                TokenType.INSERT -> PatchEvent.Insert(
                    parents = parents,
                    entity = GroupEntity(name)
                )

                TokenType.DELETE -> PatchEvent.Delete(
                    parents = parents,
                    entity = GroupEntity(name)
                )

                else -> raise(InvalidPatchLineFormat(token.line))
            }
        }

    private fun parseEntry(
        allLines: Queue<TextLine>,
        parents: List<EntityReference>,
        entryToken: LineToken,
        targetIndentLevel: Int
    ): Either<PatchParsingException, List<PatchEvent>> =
        either {
            val events = mutableListOf<PatchEvent>()

            val entryName = (entryToken.value as? TokenValue.Name)?.value
                ?: raise(InvalidPatchLineFormat(entryToken.line))

            val lines = readLinesWithIndent(
                allLines = allLines,
                targetIndentLevel = targetIndentLevel
            ).bind()

            val tokens = lines.map { line -> line.tokenize().bind() }

            val tokenTypes = tokens
                .map { token -> token.type }
                .toSet()

            when (entryToken.type) {
                TokenType.INSERT -> {
                    val fields = tokens.toFieldMap().bind()

                    events.add(
                        PatchEvent.Insert(
                            parents = parents,
                            entity = EntryEntity(
                                name = entryName,
                                fields = fields
                            )
                        )
                    )
                }

                TokenType.DELETE -> {
                    val fields = tokens.toFieldMap().bind()

                    events.add(
                        PatchEvent.Delete(
                            parents = parents,
                            entity = EntryEntity(
                                name = entryName,
                                fields = fields
                            )
                        )
                    )
                }

                TokenType.UPDATE -> {
                    val fieldParents = parents.plus(EntryReference(entryName))

                    for (token in tokens) {
                        when (token.type) {
                            TokenType.INSERT -> {
                                val field = token.getField().bind()

                                events.add(
                                    PatchEvent.Insert(
                                        parents = fieldParents,
                                        entity = field
                                    )
                                )
                            }

                            TokenType.DELETE -> {
                                val field = token.getField().bind()

                                events.add(
                                    PatchEvent.Delete(
                                        parents = fieldParents,
                                        entity = field
                                    )
                                )
                            }

                            TokenType.UPDATE -> {
                                val values = (token.value as? TokenValue.UpdateValue)
                                    ?: raise(InvalidPatchLineFormat(token.line))

                                val oldField = Field(
                                    name = values.name,
                                    value = values.oldValue
                                )

                                val newField = Field(
                                    name = values.name,
                                    value = values.newValue
                                )

                                events.add(
                                    PatchEvent.Update(
                                        parents = fieldParents,
                                        oldEntity = oldField,
                                        newEntity = newField
                                    )
                                )
                            }
                        }
                    }
                }

                else -> raise(InvalidPatchFileFormat())
            }

            events
        }

    private fun LineToken.isFieldDeclaration(): Boolean {
        return (entityType == EntityType.FIELD)
    }

//    private fun String.splitIntoEventLines(): Either<ParsingException, List<List<String>>> {
//        val content = this
//
//        return either {
//            val allLines: Queue<String> = LinkedList<>()
//                .apply {
//                    addAll(content.split("\n"))
//                }
//
//            val allEventsLines = mutableListOf<List<String>>()
//
//            var eventLines = mutableListOf<String>()
//            var prevIndentLevel: Int? = null
//
//            while (allLines.isNotEmpty()) {
//                val line = allLines.poll()
//                if (line.isBlank()) {
//                    continue
//                }
//
//                val indentLevel = line.getIndentLevel().bind()
//
//                if (prevIndentLevel != null && prevIndentLevel > indentLevel) {
//                    allEventsLines.add(eventLines)
//                    eventLines = mutableListOf<String>()
//                }
//
//                eventLines.add(line)
//
//                prevIndentLevel = indentLevel
//            }
//
//            if (eventLines.isNotEmpty()) {
//                allEventsLines.add(eventLines)
//            }
//
//            allEventsLines
//        }
//    }

//    private fun parseEntityEvent(lines: List<String>): Either<ParsingException, List<PatchEvent>> =
//        either {
//            val parents = mutableListOf<EntityReference>()
//            val queue: Queue<String> = LinkedList(lines)
//
//            val lastIndentLevel = queue.last().getIndentLevel().bind()
//
//            while (queue.isNotEmpty()) {
//                val line = queue.poll()
//                val indentLine = line.getIndentLevel().bind()
//                if (indentLine < lastIndentLevel) {
//                    parents.add(parseParentReference(line).bind())
//                } else {
//                    println("Entity: $line")
//                }
//            }
//
//            println("Parents: ${parents.size}")
//            parents.forEach { println("    parent: $it") }
//
//            listOf()
////            PatchEvent.Insert(
////                parents = parents,
////                entity = GroupEntity("name")
////            )
//        }

//    private fun parseParentReference(line: String): Either<ParsingException, EntityReference> =
//        // TODO: refactor
//        either {
//            val valueLine = line
//                .removePrefix("~")
//                .removePrefix("-")
//                .removePrefix("+")
//                .trim()
//
//            val valueStart = valueLine.indexOf("'")
//            val valueEnd = valueLine.lastIndexOf("'")
//
//            if (!valueLine.startsWith(GROUP) && !valueLine.startsWith(ENTRY)) {
//                raise(ParsingException("Invalid line format: $line"))
//            }
//
//            val (_, typeEnd) = when {
//                valueLine.startsWith(GROUP) -> {
//                    val index = valueLine.indexOf(GROUP)
//                    index to (index + GROUP.length)
//                }
//
//                valueLine.startsWith(ENTRY) -> {
//                    val index = valueLine.indexOf(ENTRY)
//                    index to (index + ENTRY.length)
//                }
//
//                else -> raise(ParsingException("Invalid line format: $line"))
//            }
//
//            if (valueStart <= typeEnd || valueEnd <= typeEnd || valueStart + 1 >= valueEnd) {
//                raise(ParsingException("Invalid line format: $line"))
//            }
//
//            when {
//                valueLine.startsWith(GROUP) -> {
//                    GroupReference(
//                        name = valueLine.substring(valueStart + 1, valueEnd)
//                    )
//                }
//
//                valueLine.startsWith(ENTRY) -> {
//                    EntryReference(
//                        name = valueLine.substring(valueStart + 1, valueEnd)
//                    )
//                }
//
//                else -> raise(ParsingException("Invalid line format: $line"))
//            }
//        }

//    private fun parseField(line: String): Either<ParsingException, Field> =
//        either {
//            val lineType = line.getLineType().bind()
//
//            if (lineType == LineType.INSERT) {
//
//            }
//
//            Field(
//                name = "",
//                value = ""
//            )
//        }

    private fun TextLine.tokenize(): Either<PatchParsingException, LineToken> {
        val line = this

        return either {
            val tokenType = line.getTokenType().bind()
            val entityType = line.getEntityType().bind()

            LineToken(
                type = tokenType,
                entityType = entityType,
                value = line.getValues().bind(),
                line = line
            )
        }
    }

    private fun TextLine.getTokenType(): Either<PatchParsingException, TokenType> {
        val line = this

        return either {
            when {
                line.text.startsWith("~") -> TokenType.UPDATE
                line.text.startsWith("+") -> TokenType.INSERT
                line.text.startsWith("-") -> TokenType.DELETE
                else -> raise(InvalidPatchLineFormat(line))
            }
        }
    }


    private fun TextLine.getEntityType(): Either<PatchParsingException, EntityType> {
        val line = this

        return either {
            val trimmed = line.text.trim()

            when {
                GROUP_PATTERN.matches(trimmed) -> EntityType.GROUP
                ENTRY_PATTERN.matches(trimmed) -> EntityType.ENTRY
                FIELD_UPDATE_PATTERN.matches(trimmed) -> EntityType.FIELD
                FIELD_PATTERN.matches(trimmed) -> EntityType.FIELD
                // TODO: add attachment
                else -> raise(InvalidPatchLineFormat(line))
            }
        }
    }

    private fun Regex.extractGroupValues(text: String): List<String> {
        return findAll(text).firstOrNull()?.groupValues
            ?: emptyList()
    }

    private fun TextLine.getValues(): Either<PatchParsingException, TokenValue> {
        val line = this

        return either {
            val trimmed = line.text.trim()

            when {
                GROUP_PATTERN.matches(trimmed) -> {
                    val name = GROUP_PATTERN.extractGroupValues(trimmed).getOrNull(1)
                        ?: raise(InvalidPatchLineFormat(line))

                    TokenValue.Name(name)
                }

                ENTRY_PATTERN.matches(trimmed) -> {
                    val name = ENTRY_PATTERN.extractGroupValues(trimmed).getOrNull(1)
                        ?: raise(InvalidPatchLineFormat(line))

                    TokenValue.Name(name)
                }

                FIELD_UPDATE_PATTERN.matches(trimmed) -> {
                    val values = FIELD_UPDATE_PATTERN.extractGroupValues(trimmed)
                    val name = values.getOrNull(1) ?: raise(InvalidPatchLineFormat(line))
                    val oldValue = values.getOrNull(2) ?: raise(InvalidPatchLineFormat(line))
                    val newValue = values.getOrNull(3) ?: raise(InvalidPatchLineFormat(line))

                    TokenValue.UpdateValue(
                        name = name,
                        oldValue = oldValue,
                        newValue = newValue
                    )
                }

                FIELD_PATTERN.matches(trimmed) -> {
                    val values = FIELD_PATTERN.extractGroupValues(trimmed)
                    val name = values.getOrNull(1) ?: raise(InvalidPatchLineFormat(line))
                    val value = values.getOrNull(2) ?: raise(InvalidPatchLineFormat(line))

                    TokenValue.NameAndValue(
                        name = name,
                        value = value
                    )
                }

                // TODO: add attachment
                else -> raise(InvalidPatchLineFormat(line))
            }
        }
    }


//    private fun TextLine.isValidPatchLine(): Boolean {
//        val line = text.trim()
//        if (line.isBlank()) {
//            return false
//        }
//
//        return false
//    }

    private fun TextLine.getIndentLevel(): Either<PatchParsingException, Int> =
        either {
            // TODO: check line starts with ~, - or +

            var index = 1
            while (text[index].isWhitespace()) {
                index++
            }

            index
        }

    private fun LineToken.getField(): Either<PatchParsingException, Field> {
        val tokenValue = (this.value as? TokenValue.NameAndValue)
            ?: return InvalidPatchLineFormat(line).left()

        return Field(
            name = tokenValue.name,
            value = tokenValue.value
        ).right()
    }

//    private fun LineToken.getUpdateFields(): Either<PatchParsingException, Pair<Field, Field>> {
//
//    }

    private fun List<LineToken>.toFieldMap(): Either<PatchParsingException, Map<String, String>> {
        val tokens = this

        return either {
            tokens
                .map { token ->
                    val field = token.getField().bind()
                    field.name to field.value
                }
                .toMap()
        }
    }

    companion object {
        private const val GROUP = "Group"
        private const val ENTRY = "Entry"
        private const val FIELD = "Field"
        private const val ATTACHMENT = "Attachment"

        private val GROUP_PATTERN = "[~\\-+]\\s+Group\\s'(.*?)'".toRegex()
        private val ENTRY_PATTERN = "[~\\-+]\\s+Entry\\s'(.*?)'".toRegex()
        private val FIELD_PATTERN = "[~\\-+]\\s+Field\\s'(.*?)':\\s'(.*?)'".toRegex()
        private val FIELD_UPDATE_PATTERN =
            "[~\\-+]\\s+Field\\s'(.*?)':\\s'(.*?)'\\sChanged\\sto\\s'(.*?)'".toRegex()
    }

//    enum class LineType {
//        INSERT,
//        DELETE,
//        UPDATE
//    }
//
//    enum class EntityType {
//        GROUP,
//        ENTRY,
//        FIELD,
//        ATTACHMENT
//    }
}

