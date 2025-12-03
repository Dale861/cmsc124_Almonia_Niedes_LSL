package main

class Parser(private val tokens: List<Token>) {
    private var current = 0
    class ParseError : RuntimeException()

    fun parseStatements(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            try {
                statements.add(declaration())
            } catch (e: ParseError) {
                synchronize()
            }
        }
        return statements.toList() // Convert to immutable list
    }

    private fun declaration(): Stmt {
        return try {
            when {
                match(TokenType.VAR) -> varDeclaration()
                match(TokenType.FUN) -> funDeclaration()
                else -> statement()
            }
        } catch (e: ParseError) {
            synchronize()
            throw e
        }
    }

    private fun varDeclaration(): Stmt {
        var typeSpecifier: Token? = null
        if (match(TokenType.CHAMPION, TokenType.ABILITY, TokenType.ITEM, TokenType.BUFF)) {
            typeSpecifier = previous()
        }
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        } else if (typeSpecifier != null) {
            initializer = createDefaultInitializer(typeSpecifier)
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, typeSpecifier, initializer)
    }

    private fun createDefaultInitializer(typeSpecifier: Token): Expr {
        return when (typeSpecifier.type) {
            TokenType.CHAMPION -> Expr.Literal(ChampionEntity(null))
            TokenType.ABILITY -> Expr.Literal(AbilityEntity(null))
            TokenType.ITEM -> Expr.Literal(ItemEntity(null))
            TokenType.BUFF -> Expr.Literal(BuffEntity(null))
            else -> Expr.Literal(null)
        }
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun statement(): Stmt {
        return when {
            match(TokenType.CHAMPION) -> championStatement()
            match(TokenType.IF) -> ifStatement()
            match(TokenType.WHILE) -> whileStatement()
            match(TokenType.COMBO) -> comboStatement()
            match(TokenType.PRINT) -> printStatement()
            match(TokenType.LEFT_BRACE) -> Stmt.Block(block())
            match(TokenType.FOR) -> forStatement()
            match(TokenType.RETURN) -> returnStatement()
            else -> expressionStatement()
        }
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer = if (!check(TokenType.SEMICOLON)) {
            if (match(TokenType.VAR)) varDeclaration() else expressionStatement()
        } else null
        consume(TokenType.SEMICOLON, "Expect ';' after initializer.")

        val condition = if (!check(TokenType.SEMICOLON)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after condition.")

        val increment = if (!check(TokenType.RIGHT_PAREN)) expression() else null
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        val body = block()  // Use {} block
        return Stmt.For(initializer, condition, increment, body)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value = if (!check(TokenType.SEMICOLON)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun argumentList(): List<Token> {
        val params = mutableListOf<Token>()
        do {
            params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
        } while (match(TokenType.COMMA))
        return params
    }

    private fun funDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.")
        val params = if (!check(TokenType.RIGHT_PAREN)) {
            argumentList()
        } else emptyList()
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.")
        val body = block()
        return Stmt.Function(name, params, body)
    }

    private fun championStatement(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect champion name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after champion name.")
        val events = mutableListOf<Expr.EventHandler>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            events.add(eventHandler())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after champion body.")
        return Stmt.Champion(name, events.toList())
    }

    private fun eventHandler(): Expr.EventHandler {
        val eventType = consume(
            TokenType.ON_ABILITY_CAST, TokenType.ON_ATTACK, TokenType.ON_DEATH,
            TokenType.ON_HEALTH_BELOW, TokenType.ON_MANA_BELOW, TokenType.ON_ENEMY_APPROACH,
            TokenType.ON_ALLY_NEARBY, TokenType.ON_COOLDOWN_READY,
            message = "Expect event handler type."
        )
        consume(TokenType.LEFT_PAREN, "Expect '(' after event type.")
        val params = mutableListOf<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before event body.")
        val body = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            body.add(declaration())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after event body.")
        return Expr.EventHandler(eventType, params.toList(), body.toList())
    }

    private fun ifStatement(): Stmt {
        val condition = expression()
        consume(TokenType.THEN, "Expect 'then' after if condition.")
        val thenBranch = mutableListOf<Stmt>()
        while (!check(TokenType.ELSE) && !check(TokenType.END) && !isAtEnd()) {
            thenBranch.add(declaration())
        }
        var elseBranch: List<Stmt>? = null
        if (match(TokenType.ELSE)) {
            elseBranch = mutableListOf()
            while (!check(TokenType.END) && !isAtEnd()) {
                elseBranch.add(declaration())
            }
        }
        consume(TokenType.END, "Expect 'end' after if statement.")
        return Stmt.If(condition, thenBranch.toList(), elseBranch?.toList())
    }

    private fun whileStatement(): Stmt {
        val condition = expression()
        consume(TokenType.DO, "Expect 'do' after while condition.")
        val body = mutableListOf<Stmt>()
        while (!check(TokenType.END) && !isAtEnd()) {
            body.add(declaration())
        }
        consume(TokenType.END, "Expect 'end' after while body.")
        return Stmt.While(condition, body.toList())
    }

    private fun comboStatement(): Stmt {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'combo'.")
        val actions = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            actions.add(declaration())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after combo body.")
        return Stmt.Combo(actions.toList())
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements.toList()
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = logicOr()
        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun logicOr(): Expr {
        var expr = logicAnd()
        while (match(TokenType.OR)) {
            expr = Expr.LogicalOr(expr, previous(), logicAnd())  // Use LogicalOr
        }
        return expr
    }

    private fun logicAnd(): Expr {
        var expr = equality()
        while (match(TokenType.AND)) {
            expr = Expr.LogicalAnd(expr, previous(), equality())  // Use LogicalAnd
        }
        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(): Expr {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.NOT)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return call()
    }

    private fun call(): Expr {
        var expr = primary()
        while (true) {
            when {
                match(TokenType.LEFT_PAREN) -> expr = finishCall(expr)
                match(TokenType.DOT) -> {
                    val methodName = advanceAsIdentifier()
                    consume(TokenType.LEFT_PAREN, "Expect '(' after method name")
                    val args = mutableListOf<Expr>()
                    if (!check(TokenType.RIGHT_PAREN)) {
                        do {
                            args.add(expression())
                        } while (match(TokenType.COMMA))
                    }
                    consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments")
                    expr = Expr.MethodCall(expr, methodName, args.toList())
                }
                else -> break
            }
        }
        return expr
    }

    private fun advanceAsIdentifier(): Token {
        if (isAtEnd()) throw error(peek(), "Expect method name after '.'")
        val token = advance()
        return Token(TokenType.IDENTIFIER, token.lexeme, null, token.line)
    }

    private fun finishCall(callee: Expr): Expr {
        val args = mutableListOf<Expr>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(expression())
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments")
        val calleeToken = when (callee) {
            is Expr.Variable -> callee.name
            else -> throw error(peek(), "Invalid function call")
        }
        return if (callee is Expr.Variable) {
            Expr.Call(callee.name, args)
        } else {
            Expr.FunctionCall(callee, previous(), args)  // Use FunctionCall for expressions
        }
    }

    private fun primary(): Expr {
        when {
            match(TokenType.FALSE) -> return Expr.Literal(false)
            match(TokenType.TRUE) -> return Expr.Literal(true)
            match(TokenType.NIL) -> return Expr.Literal(null)
            match(TokenType.NUMBER, TokenType.STRING) -> return Expr.Literal(previous().literal)
            match(TokenType.IDENTIFIER) -> return Expr.Variable(previous())
            match(TokenType.LEFT_PAREN) -> {
                val expr = expression()
                consume(TokenType.RIGHT_PAREN, "Expect ')' after expression")
                return Expr.Grouping(expr)
            }
        }
        throw error(peek(), "Expect expression")
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun consume(vararg types: TokenType, message: String): Token {
        for (type in types) {
            if (check(type)) return advance()
        }
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): ParseError {
        println("[line ${token.line}] Error at '${token.lexeme}': $message")
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return
            when (peek().type) {
                TokenType.CHAMPION, TokenType.IF, TokenType.WHILE,
                TokenType.PRINT, TokenType.VAR, TokenType.FOR -> return
                else -> advance()
            }
        }
    }
}
