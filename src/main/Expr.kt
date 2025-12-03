package main

sealed class Expr {
    data class EventHandler(val eventType: Token, val params: List<Token>, val body: Stmt.Block) : Expr()
    data class Call(val callee: Token, val args: List<Expr>) : Expr()
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Unary(val operator: Token, val right: Expr) : Expr()
    data class Literal(val value: Any?) : Expr()
    data class Variable(val name: Token) : Expr()
    data class Assign(val name: Token, val value: Expr) : Expr()
    data class Grouping(val expression: Expr) : Expr()
    data class MethodCall(val obj: Expr, val methodName: Token, val args: List<Expr>) : Expr()

    // NEW: Logical operators (lab5)
    data class LogicalAnd(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class LogicalOr(val left: Expr, val operator: Token, val right: Expr) : Expr()

    // NEW: Function calls (generalized from Call)
    data class FunctionCall(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr()
}

sealed class Stmt {
    data class If(val condition: Expr, val thenBranch: Stmt.Block, val elseBranch: Stmt.Block?) : Stmt()
    data class Champion(val name: Token, val events: List<Expr.EventHandler>) : Stmt()
    data class While(val condition: Expr, val body: Stmt.Block) : Stmt()
    data class Combo(val actions: Stmt.Block) : Stmt()
    data class Expression(val expression: Expr) : Stmt()
    data class Block(val statements: List<Stmt>) : Stmt()
    data class Print(val expression: Expr) : Stmt()
    data class Var(val name: Token, val typeSpecifier: Token?, val initializer: Expr?) : Stmt()

    // NEW: Function declaration (lab5)
    data class Function(val name: Token, val params: List<Token>, val body: List<Stmt>) : Stmt()

    // NEW: Return statement (lab5)
    data class Return(val keyword: Token, val value: Expr?) : Stmt()

    // NEW: For loop (lab5)
    data class For(val initializer: Stmt?, val condition: Expr?, val increment: Expr?,
                   val body: List<Stmt>) : Stmt()
}
