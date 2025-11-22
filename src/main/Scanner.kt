package main

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '%' -> addToken(TokenType.PERCENT)
            ';' -> addToken(TokenType.SEMICOLON)  // FIX: Added semicolon handling
            '+' -> addToken(TokenType.PLUS)
            '-' -> addToken(TokenType.MINUS)
            '*' -> addToken(TokenType.STAR)
            '/' -> {
                if (match('/')) {
                    // Single-line comment
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '!' -> if (match('=')) addToken(TokenType.EQUAL_EQUAL) // For != operator
            ' ', '\r', '\t' -> {} // Ignore whitespace
            '\n' -> line++
            '"' -> string()
            else -> {
                when {
                    c.isDigit() -> number()
                    c.isLetter() || c == '_' -> identifier()
                    else -> error("Unexpected character: $c")
                }
            }
        }
    }

    private fun identifier() {
        while (peek().isLetterOrDigit() || peek() == '_') advance()

        val text = source.substring(start, current)
        val type = Keywords.ALL_KEYWORDS[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (peek().isDigit()) advance()

        // Look for decimal part
        if (peek() == '.' && peekNext().isDigit()) {
            advance() // Consume '.'
            while (peek().isDigit()) advance()
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            error("Unterminated string.")
            return
        }

        advance() // Closing "

        // Trim surrounding quotes
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun isAtEnd(): Boolean = current >= source.length

    private fun advance(): Char = source[current++]

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun error(message: String) {
        println("[Line $line] Error: $message")
    }
}