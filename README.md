# LSL - League of Legends Script Language

**Creators:** Dale Almonster & Stefan Niedesmonster

---

## Language Overview

LSL is an experimental domain-specific programming language designed to script League of Legends champion behavior and game mechanics. Its main characteristics are:

- Game-centric syntax with LoL-specific keywords (e.g., `cast`, `attack`, `onAbilityCast`).
- Emphasis on clarity for competitive gaming strategy with readable operators and event-driven architecture.
- Object-oriented design with game entities (Champions, Abilities, Items, Buffs).
- Support for variables, control flow, method chaining, and combo execution.
- Case-sensitive and whitespace-insensitive (except as token delimiters).
- Interactive REPL with multi-line paste mode and script execution.

---

## Getting Started

### Running LSL in IntelliJ IDEA (Recommended)

**REPL Mode:**
1. Open the project in IntelliJ IDEA
2. Right-click on `main.kt` in the `src/main` folder
3. Select "Run 'MainKt'"

**Execute a Script (test.txt):**
1. Move `test.txt` to the project root directory (or use `src/main/test.txt` as the path)
2. Click on the run configuration dropdown (top right)
3. Select "Edit Configurations..."
4. In the "Program arguments" field, add: `test.txt`
5. Click "Apply" and "OK"
6. Run the program

**Using REPL commands:**
```
LSL> run test.txt          // if test.txt is in project root
LSL> run src/main/test.txt  // if test.txt is in src/main folder
```

### Alternative: Command Line

If you prefer to run from command line:

**Step 1: Compile the project**

*On Windows (PowerShell):*
```powershell
kotlinc src/main/main.kt src/main/Scanner.kt src/main/Parser.kt src/main/Evaluator.kt src/main/Environment.kt src/main/GameEntities.kt src/main/Expr.kt src/main/Token.kt src/main/TokenType.kt src/main/Keywords.kt src/main/AstPrinter.kt -include-runtime -d lsl.jar
```

*On Linux/Mac (Bash):*
```bash
kotlinc src/main/*.kt -include-runtime -d lsl.jar
```

**Step 2: Run the program**

**REPL Mode:**
```bash
java -jar lsl.jar
```

**Execute a Script:**
```bash
java -jar lsl.jar test.txt
```

### REPL Commands

| Command | Description |
|---------|-------------|
| `exit` or `quit` | Exit the REPL |
| `paste` | Enter multi-line paste mode (type 'END' to finish) |
| `run <filename>` | Execute a script file |
| `clear` | Reset the environment |
| `help` | Show help message |

---

## Keywords

The following are reserved words in LSL and cannot be used as identifiers:

### Entity Types
| Keyword | Purpose |
|---------|---------|
| `champion` | Champion entity type |
| `ability` | Ability entity type |
| `item` | Item entity type |
| `buff` | Buff entity type |

### Event Handlers
| Keyword | Purpose |
|---------|---------|
| `onAbilityCast` | Event triggered when ability is cast |
| `onAttack` | Event triggered on basic attack |
| `onDeath` | Event triggered on champion death |
| `onHealthBelow` | Event triggered when health below threshold |
| `onManaBelow` | Event triggered when mana below threshold |
| `onEnemyApproach` | Event triggered when enemy approaches |
| `onAllyNearby` | Event triggered when ally is nearby |
| `onCooldownReady` | Event triggered when cooldown resets |

### Actions
| Keyword | Purpose |
|---------|---------|
| `cast` | Cast an ability |
| `useItem` | Use an item from inventory |
| `attack` | Perform basic attack |
| `moveTo` | Move to location |
| `recall` | Return to base |
| `teleport` | Teleport to location |

### Query Functions
| Keyword | Purpose |
|---------|---------|
| `enemyInRange` | Check if enemy is in range |
| `nearestEnemy` | Find nearest enemy |
| `allyInRange` | Check if ally is in range |
| `hasBuff` | Check if entity has buff |
| `itemReady` | Check if item is ready |
| `cooldownReady` | Check if ability is ready |
| `getHealth` | Get current health |
| `getMana` | Get current mana |

### Control Flow
| Keyword | Purpose |
|---------|---------|
| `if` | Conditional statement |
| `then` | Keyword after if condition |
| `else` | Else branch |
| `end` | End of control block |
| `while` | Looping construct |
| `do` | Keyword after while condition |
| `for` | For loop (reserved) |
| `combo` | Group multiple actions |

### Logic
| Keyword | Purpose |
|---------|---------|
| `and` | Logical AND |
| `or` | Logical OR |
| `not` | Logical NOT |
| `true` | Boolean literal for true |
| `false` | Boolean literal for false |

### Variables
| Keyword | Purpose |
|---------|---------|
| `var` | Variable declaration |
| `print` | Print statement |
| `nil` | Null value |

---

## Operators

**Arithmetic:**
- `+` (addition, also string concatenation)
- `-` (subtraction)
- `*` (multiplication)
- `/` (division)
- `%` (modulo)

**Comparison:**
- `==` (equal to)
- `<` (less than)
- `>` (greater than)
- `<=` (less than or equal to)
- `>=` (greater than or equal to)

**Logical:**
- `and` (logical AND)
- `or` (logical OR)
- `not` (logical NOT)

**Assignment:**
- `=` (assign value)

**Other:**
- `.` (method call operator)

---

## Data Types

### Literals

- **Numbers**: Integers and floating point numbers (`600`, `30.5`)
- **Strings**: Enclosed in double quotes (`"Lux"`, `"Light Binding"`)
- **Booleans**: `true`, `false`
- **Nil**: `nil` represents null/empty value

### Game Entities

LSL provides four built-in entity types:

#### ChampionEntity
Represents a champion with health, mana, and abilities.

**Constructor:**
```lsl
var champion champ = ChampionEntity("Lux");
```

**Properties:**
- `name` - Champion name
- `level` - Current level (default: 1)
- `health` - Current health (default: 100.0)
- `mana` - Current mana (default: 100.0)
- `abilities` - List of learned abilities

**Methods:**
- `cast(abilityName, manaCost)` - Cast an ability
- `attack(target)` - Attack a target
- `moveTo(location)` - Move to location
- `useItem(itemName)` - Use an item
- `recall()` - Recall to base (restores 50 health and mana)
- `addAbility(abilityName)` - Learn a new ability
- `onCastHealth(threshold, action)` - Register health event handler
- `onCastMana(threshold, action)` - Register mana event handler
- `checkEvents()` - Trigger registered event handlers

#### AbilityEntity
Represents an ability with cooldown and stats.

**Constructor:**
```lsl
var ability flash = AbilityEntity("Flash");
```

**Properties:**
- `name` - Ability name
- `cooldown` - Current cooldown (default: 0.0)
- `manaCost` - Mana cost
- `damage` - Damage dealt

**Methods:**
- `setCooldown(cd)` - Set cooldown duration
- `reduceCooldown(amount)` - Reduce cooldown by amount

#### ItemEntity
Represents an item with stats.

**Constructor:**
```lsl
var item sword = ItemEntity("Infinity Edge");
```

**Properties:**
- `name` - Item name
- `cost` - Gold cost
- `stats` - Map of stat bonuses

**Methods:**
- `addStat(statName, value)` - Add a stat bonus

#### BuffEntity
Represents a temporary buff.

**Constructor:**
```lsl
var buff speed = BuffEntity("Speed Boost");
```

**Properties:**
- `name` - Buff name
- `duration` - Duration in seconds
- `stackable` - Whether buff can stack

---

## Identifiers

- Must begin with a letter (a–z, A–Z) or underscore (_)
- May contain letters, digits (0–9), and underscores
- Cannot match reserved keywords
- Case-sensitive (`Champion` and `champion` are different)

---

## Comments

- **Line comments**: Start with `//` and continue until the end of the line

```lsl
// This is a comment
var x = 5; // This is also a comment
```

---

## Syntax Style

- Statements must end with a semicolon (`;`)
- Whitespace is ignored except as a separator between tokens
- Blocks use keywords `then`/`do` and `end` for control flow
- Braces `{ ... }` for champion declarations and combo blocks
- Method calls support chaining for fluent API style

---

## Grammar

```
program         ::= declaration* EOF

declaration     ::= varDecl | statement

varDecl         ::= "var" typeSpec? IDENTIFIER ("=" expression)? ";"
typeSpec        ::= "champion" | "ability" | "item" | "buff"

statement       ::= championStmt
                  | ifStmt
                  | whileStmt
                  | comboStmt
                  | printStmt
                  | block
                  | exprStmt

championStmt    ::= "champion" IDENTIFIER "{" eventHandler* "}"

eventHandler    ::= eventType "(" params? ")" "{" statement* "}"
eventType       ::= "onAbilityCast" | "onAttack" | "onDeath" 
                  | "onHealthBelow" | "onManaBelow" | "onEnemyApproach"
                  | "onAllyNearby" | "onCooldownReady"

ifStmt          ::= "if" expression "then" statement* ("else" statement*)? "end"

whileStmt       ::= "while" expression "do" statement* "end"

comboStmt       ::= "combo" "{" statement* "}"

printStmt       ::= "print" expression ";"

block           ::= "{" statement* "}"

exprStmt        ::= expression ";"

expression      ::= assignment

assignment      ::= IDENTIFIER "=" assignment | logicOr

logicOr         ::= logicAnd ("or" logicAnd)*

logicAnd        ::= equality ("and" equality)*

equality        ::= comparison ("==" comparison)*

comparison      ::= term (("<" | "<=" | ">" | ">=") term)*

term            ::= factor (("+" | "-") factor)*

factor          ::= unary (("*" | "/" | "%") unary)*

unary           ::= "not" unary | call

call            ::= primary ( "(" arguments? ")" | "." IDENTIFIER "(" arguments? ")" )*

primary         ::= "true" | "false" | "nil"
                  | NUMBER | STRING | IDENTIFIER
                  | "(" expression ")"

arguments       ::= expression ("," expression)*
```

---

## Sample Code

### Basic Variables and Arithmetic
```lsl
var x = 5 + 3;
print x;

var health = 100;
var mana = 50.5;
var championName = "Yasuo";
var isAlive = true;
```

### Champion Entity Usage
```lsl
var champion champ = ChampionEntity("Lux");
print champ;

champ.cast("Light Binding", 50.0);
champ.attack("Zed");
champ.moveTo("mid lane");
champ.recall();
```

### Method Chaining
```lsl
var champion yasuo = ChampionEntity("Yasuo");
yasuo.cast("Steel Tempest", 30.0)
     .attack("enemy")
     .moveTo("top lane")
     .recall();
```

### Control Flow
```lsl
var hp = 30;

if hp < 50 then
    print "Health is low!";
    print "Consider recalling";
end

if hp > 80 then
    print "Health is high";
else
    print "Health needs attention";
end
```

### While Loops
```lsl
var counter = 0;
while counter < 5 do
    print counter;
    counter = counter + 1;
end
```

### Combo System
```lsl
var champion zed = ChampionEntity("Zed");

combo {
    zed.cast("Living Shadow", 40.0);
    zed.cast("Razor Shuriken", 60.0);
    zed.attack("target");
    zed.cast("Death Mark", 100.0);
}
```

### Event Handlers
```lsl
var champion ezreal = ChampionEntity("Ezreal");
ezreal.onCastHealth(30.0, "Use Health Potion");
ezreal.onCastMana(20.0, "Recall to base");
ezreal.checkEvents();
```

### Champion Declaration with Events
```lsl
champion Ahri {
    onHealthBelow(threshold) {
        print "Ahri health critical!";
    }

    onManaBelow(threshold) {
        print "Ahri mana low!";
    }
}
```

### Complex Combat Scenario
```lsl
var champion akali = ChampionEntity("Akali");
var currentHealth = 45;
var currentMana = 80;

if currentHealth < 50 and currentMana > 60 then
    print "Low health but enough mana - play safe";
    akali.cast("Shuriken Flip", 30.0);
    akali.moveTo("under tower");

    if currentHealth < 30 then
        print "Critical health!";
        akali.recall();
    end
end
```

---

## Design Rationale

**Game-Centric Keywords**: LSL uses League of Legends terminology (`cast`, `attack`, `combo`, `recall`) to make code intuitive for competitive players.

**Event-Driven Architecture**: Event handlers (`onAttack`, `onHealthBelow`) naturally mirror how champions behave in-game.

**Object-Oriented Design**: Game entities (Champions, Abilities, Items, Buffs) encapsulate state and behavior, making code more organized and reusable.

**Method Chaining**: Methods return `this` to enable fluent API style, making action sequences read naturally.

**Familiar Syntax**: C-like operators and control flow make LSL accessible while keeping it unique to gaming.

**Interactive REPL**: Immediate feedback for testing and learning, with multi-line paste mode for complex scripts.

**Type System**: Optional type hints (`var champion champ`) improve code clarity without requiring verbose declarations.

**Simplicity First**: Core constructs (variables, control flow, entities, events) keep the language minimal and easy to extend.

**Case Sensitivity**: Allows flexibility and avoids ambiguity with keywords.

---

## Error Handling

LSL provides clear error messages with line numbers:

- **Scanner Errors**: Reports unexpected characters, unterminated strings
- **Parser Errors**: Reports syntax errors with token context
- **Runtime Errors**: Reports type mismatches, undefined variables, method errors

Example error output:
```
[line 5] Error at 'champion': Expect ';' after expression.
[line 12] Runtime error: Undefined variable 'enemy'.
```

---

## Future Enhancements

- For loops implementation
- Additional query functions for game state
- More sophisticated event system
- Champion stat tracking
- Ability cooldown management
- Team coordination features

---

## License

LSL is an educational project created for learning programming language design.