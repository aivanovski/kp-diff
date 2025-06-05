package com.github.ai.kpdiff.domain.patch

import org.junit.jupiter.api.Test

class PatchFileParserTest {

    @Test
    fun `parse`() {
        val result = PatchFileParser().parse(CONTENT)

        println("result=$result")
    }

    companion object {

//        ~ Group 'Database'
//        ~     Group 'Internet'
//        ~         Entry 'Cloud keys'
//        -             Attachment 'old-key.ssh' 15 Bytes
//        +             Attachment 'new-key.ssh' 15 Bytes

        private val CONTENT = """
            ~ Group 'Database'
            -     Group 'Email'

            ~ Group 'Database'
            ~     Group 'Internet'
            +         Group 'Shopping'

            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Group 'Coding'
            -             Entry 'Github.com'
            -                 Field 'Title': 'Github.com'
            -                 Field 'UserName': 'john.doe@example.com'
            -                 Field 'Password': 'abc123'
            -                 Field 'URL': 'https://github.com'
            -                 Field 'Notes': ''
            +             Entry 'Gitlab'
            +                 Field 'Title': 'Gitlab'
            +                 Field 'UserName': 'john.doe@example.com'
            +                 Field 'Password': 'abc123'
            +                 Field 'URL': 'https://gitlab.com'
            +                 Field 'Notes': ''

            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Entry 'Google'
            ~             Field 'Notes': '' Changed to 'https://google.com'

            ~ Group 'Database'
            ~     Group 'Internet'
            ~         Group 'Social'
            +             Entry 'Facebook'
            +                 Field 'Title': 'Facebook'
            +                 Field 'UserName': 'john.doe@example.com'
            +                 Field 'Password': 'abc123'
            +                 Field 'URL': 'https://facebook.com'
            +                 Field 'Notes': ''
        """.trimIndent().trim()
    }
}