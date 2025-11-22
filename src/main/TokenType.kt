package main

enum class TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, PERCENT, SEMICOLON,

    // One or two character tokens
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    PLUS, MINUS, STAR, SLASH,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords - Champion/Ability related
    CHAMPION, ABILITY, ITEM, BUFF,

    // Keywords - Events
    ON_ABILITY_CAST, ON_ATTACK, ON_DEATH,
    ON_HEALTH_BELOW, ON_MANA_BELOW, ON_ENEMY_APPROACH,
    ON_ALLY_NEARBY, ON_COOLDOWN_READY,

    // Keywords - Actions (REMOVED: PING, PLACE_WARD, and map-specific actions)
    CAST, USE_ITEM, ATTACK, MOVE_TO, RECALL, TELEPORT,

    // Keywords - Queries
    ENEMY_IN_RANGE, NEAREST_ENEMY, ALLY_IN_RANGE,
    HAS_BUFF, ITEM_READY, COOLDOWN_READY,
    GET_HEALTH, GET_MANA,

    // Keywords - Control Flow
    IF, ELSE, WHILE, FOR, DO, END,
    COMBO, THEN,

    // Keywords - Logic
    AND, OR, NOT, TRUE, FALSE,

    // Keywords - Variables and Output
    VAR, PRINT, NIL,

    // REMOVED: All map location keywords (DRAGON_PIT, BARON_PIT, etc.)

    EOF
}
