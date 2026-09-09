# Variable Naming Rules — Fully Corrected AI Enforcement File

> **Purpose:** Use this file as an instruction/rule file for a code-editor AI.
> The AI must apply the rules below whenever it generates, edits, reviews, or
> refactors code, unless a language/framework/API requirement explicitly
> requires a different convention.
>
> **Basis:** Complete *Variable Naming.pptx – Clean Coding Principles*
> presentation plus the lecturer/viva notes supplied by the user.
> Where a lecturer note conflicted with the presentation or Java syntax, this
> file keeps the intended lesson but corrects the technical wording.

## 0. Mandatory AI Behaviour

The code-editor AI **MUST**:

1. Use meaningful, intention-revealing names.
2. Make names answer **Why, What, and How** as clearly as reasonably possible.
3. Avoid disinformation, vague distinctions, unpronounceable names, encodings,
   member prefixes, interface/abstract prefixes, and mental mappings.
4. Use searchable names for important values and concepts.
5. Use `i`, `j`, and `k` only as tiny, obvious loop counters.
6. Replace unexplained domain-specific magic values with meaningful constants.
7. Use nouns/noun phrases for classes/objects and verbs/verb phrases for methods.
8. Use one consistent word for one concept.
9. Avoid unnecessary context such as `carMake` inside class `Car`.
10. Keep functions small, focused, cohesive, and free from hidden side effects.
11. Prefer 0–2 parameters; treat 3 as a warning and more than 3 as a refactoring
    signal unless there is a strong justification.
12. Prefer self-explanatory code over comments that merely explain poor code.
13. Use comments for non-obvious intent, decisions, constraints, TODOs, and
    amplification—not for repeating obvious code.
14. Keep files, lines, indentation, declarations, and related functions
    consistently formatted and easy to read.
15. Aim for **high cohesion, low coupling, readability, writability, and
    maintainability**.
16. Never silently ignore these rules when proposing a "clean" solution.

### Rule Priority

When rules seem to conflict, use this order:

1. Correctness and required external/API conventions.
2. Project or language naming conventions.
3. These Clean Code rules.
4. Personal preference.

---

## 1. Meaningful Names

### 1.1 Use Intention-Revealing Names

A good name should clearly show the purpose of a variable, class, method, or object.

**Rules:**

- Choosing a good name takes time, but it saves more time later.

- A name should answer the important questions: ****Why does it exist? What does it do? How is it used?****

- If a name needs a comment to explain what it means, the name is not revealing its intent clearly enough.

**Bad:**

```java

int d; // elapsed time in days

```

**Good:**

```java

int elapsedTimeInDays;

int daysSinceCreation;

int daysSinceModification;

int fileAgeInDays;

```

The good names immediately explain the meaning of the stored value.

---

### 1.2 Use Names That Explain the Purpose of the Code

Unclear names make readers ask unnecessary questions.

Example of unclear code:

```java

public List<int[]> getThem() {

    List<int[]> list1 = new ArrayList<int[]>();

    for (int[] x : theList)

        if (x[0] == 4)

            list1.add(x);

    return list1;

}

```

This code creates several questions:

- What kind of things are in the list?

- What does the first element mean?

- What does the value `4` mean?

- What is the returned list used for?

In the presentation, the code belongs to a Minesweeper game. Better names make the meaning clearer:

```java

public List<int[]> getFlaggedCells() {

    List<int[]> flaggedCells = new ArrayList<int[]>();

    for (int[] cell : gameBoard)

        if (cell[STATUS_VALUE] == FLAGGED)

            flaggedCells.add(cell);

    return flaggedCells;

}

```

The names now reveal that:

- the list represents the ****game board****;

- the first element stores a ****status value****;

- the value `4` represents ****FLAGGED****;

- the returned list contains ****flagged cells****.

It can be improved further by introducing a `Cell` class and hiding the status value behind a descriptive method:

```java

public List<Cell> getFlaggedCells() {

    List<Cell> flaggedCells = new ArrayList<Cell>();

    for (Cell cell : gameBoard)

        if (cell.isFlagged())

            flaggedCells.add(cell);

    return flaggedCells;

}

```

**Rule:** Prefer names and abstractions such as `Cell`, `gameBoard`, `flaggedCells`, and `isFlagged()` instead of raw arrays, unclear indexes, and magic numbers.

---

### 1.3 Avoid Disinformation

Names should not cause readers to reach a false conclusion about the code.

**Bad:**

```java

Account[] accountsList = new Account[]{};

```

The name `accountsList` is misleading because the variable is an array, not a `List`.

**Good:**

```java

Account[] accounts = new Account[]{};

```

**Rule:** Do not include words in a name that incorrectly describe the variable's type, structure, or purpose.

---

### 1.4 Make Meaningful Distinctions

Names should explain the difference between variables.

Number-series names such as `a1`, `a2`, `data1`, or `data2` are usually non-informative and provide little help to the reader.

**Bad:**

```java

public static void copyChars(char a1[], char a2[]) {

    for (int i = 0; i < a1.length; i++) {

        a2[i] = a1[i];

    }

}

```

**Good:**

```java

public static void copyChars(char source[], char destination[]) {

    for (int i = 0; i < source.length; i++) {

        destination[i] = source[i];

    }

}

```

**Rule:** When two variables have different roles, their names should clearly express those roles.

---

### 1.5 Use Pronounceable Names

Names should be easy to say during discussions with other developers.

The presentation uses `genymdhms` as an example of a difficult name. It represents a generation date containing year, month, day, hour, minute, and second.

**Bad style:**

```java

class DtaRcrd102 {

    private Date genymdhms;

    private Date modymdhms;

    private final String pszqint = "102";

}

```

**Good style:**

```java

class Customer {

    private Date generationTimestamp;

    private Date modificationTimestamp;

    private final String recordId = "102";

}

```

**Rule:** Use complete, readable, and pronounceable words instead of compressed abbreviations that are difficult to understand.

---

### 1.6 Use Searchable Names

A name should be easy to search for throughout the codebase.

**Rules:**

- Single-letter names should normally be used only as local variables inside very small methods.

- The length and descriptiveness of a name should correspond to the size of its scope.

- If a variable is used in multiple places, give it a searchable name.

- Prefer named constants instead of unexplained numeric values.

Instead of code that depends heavily on names such as `j`, `s`, `t`, and literal values such as `4` and `5`, use meaningful names such as:

```java

int realDaysPerIdealDay = 4;

int WORK_DAYS_PER_WEEK = 5;

int sum = 0;

```

Then use descriptive names for task estimates, calculated days, and weeks.

**Rule:** Important values should have names that are easy to locate with a text search.

---

### 1.7 Avoid Encodings – Hungarian Notation

Do not encode type information into a variable name.

Hungarian notation was useful when languages had weaker type checking. With modern strong typing, it adds unnecessary information.

**Bad:**

```java

PhoneNumber phoneString;

```

The variable name still says `String` even though its type is now `PhoneNumber`.

**Good:**

```java

PhoneNumber phoneNumber;

```

**Rule:** Let the programming language show the type; let the variable name show the meaning.

---

### 1.8 Avoid Encodings – Member Prefixes

Do not add unnecessary prefixes such as `m_` to instance variables.

**Bad:**

```java

public class Part {

    private String m_dsc;

    void setName(String name) {

        m_dsc = name;

    }

}

```

**Good:**

```java

public class Part {

    String description;

    void setDescription(String description) {

        this.description = description;

    }

}

```

**Rule:** Prefer a meaningful variable name and use language features such as `this` when needed.

---

### 1.9 Avoid Encodings – Interfaces and Implementations

Users of a type do not need unnecessary name prefixes that expose whether the type is an interface or abstract class.

**Avoid names such as:**

```java

IShapeFactory

AbstractShapeFactory

```

**Prefer a clear domain name:**

```java

ShapeFactory

```

**Rule:** Name an abstraction for what it represents, not for the technical mechanism used to implement it.

---

### 1.10 Avoid Mental Mappings

Readers should not have to mentally translate an unclear name into a meaningful concept.

****Bad idea:****

```java

String[] l = ...;

for (int i = 0; i < l.length; i++) {

    String u1 = l[i];

    Dispatch(u1);

}

```

****Better:****

```java

String[] locations = ...;

for (String location : locations) {

    Dispatch(location);

}

```

**Rule:** Use names that directly express the concept instead of forcing the reader to remember what a short symbol means.

---

### 1.11 Class Names

Classes and objects should use ****noun**** or ****noun-phrase**** names.

**Good examples from the presentation:**

- `Customer`

- `Account`

- `Address`

- `AddressParser`

Avoid vague names such as:

- `Manager`

- `Processor`

- `Data`

- `Info`

Also avoid using verbs as class names.

**Rule:** A class name should identify what the object ****is****.

---

### 1.12 Method Names

Methods should use ****verb**** or ****verb-phrase**** names because methods perform actions or answer questions.

**Good examples:**

```java

report.save();

customer.getName();

customer.setName("John");

supplier.postPayment(address);

```

**Rule:** A method name should clearly describe the action it performs or the information it returns.

---

### 1.13 Use One Word per Concept

Use one consistent word for one concept throughout the codebase.

**Confusing:**

```java

monthlyReport.getCustomers();

monthlyReport.fetchLoyalCustomers();

monthlyReport.retreiveSuppliers();

```

The methods use `get`, `fetch`, and `retreive` for the same general concept.

**Consistent:**

```java

monthlyReport.getCustomers();

monthlyReport.getLoyalCustomers();

monthlyReport.getSuppliers();

```

**Rule:** If several operations mean the same thing, use the same vocabulary for all of them.

---

### 1.14 Use Solution-Domain Names

The readers of source code are programmers, so Computer Science terminology is appropriate when it clearly describes the solution.

Examples from the presentation:

- `BubbleSort`

- `AccountVisitor`

- `JobQueue`

**Rule:** Use established programming or Computer Science terms when they accurately express the concept.

---

### 1.15 Use Problem-Domain Names

When there is no useful solution-domain term, use terminology from the real-world problem being solved.

**Rule:** Choose names that match the language used by the business, user, or application domain so that the code can be discussed using the same vocabulary as the problem.

---

### 1.16 Do Not Add Unnecessary Context

Do not repeat context that is already obvious from the surrounding class or scope.

Inside a `Car` class:

**Bad:**

```java

public class Car {

    private String carMake;

    private String carModel;

    private String carColor;

}

```

**Good:**

```java

public class Car {

    private String make;

    private String model;

    private String color;

}

```

**Rules:**

- Add no more context to a name than necessary.

- Shorter names are generally better when they remain clear and meaningful.

- Do not repeat the class name in every field name when the class already provides that context.

---

## 2. Naming Rules Reinforced by the Functions Section

The presentation's function rules support good variable and method naming.

### 2.1 Keep Functions Small

- Functions should be small.

- They should be even smaller than you initially think.

- Smaller functions reduce the amount of context a reader must hold, making names easier to understand.

### 2.2 Keep Blocks Simple

Code inside `if`, `else`, `while`, and similar blocks should be kept very small, ideally one line that calls a clearly named function.

This allows a descriptive function name to explain the purpose of the block.

### 2.3 Functions Should Do One Thing

> Functions should do one thing. They should do it well. They should do it only.

A function that has one clear responsibility is easier to name accurately.

### 2.4 Use One Level of Abstraction per Function

Do not mix high-level concepts with low-level implementation details in the same function.

Follow the ****step-down rule**** so the code reads from higher-level operations to lower-level details.

### 2.5 Use Descriptive Function Names

The presentation repeats several naming principles here:

- Long descriptive names are better than short confusing names.

- Spend time choosing a good name.

- Try several different names and read the code to see which one communicates best.

- Be consistent with names.

- A name should answer ****Why, What, and How****.

- If the name needs a comment to explain it, its intent has not been revealed clearly enough.

### 2.6 Keep Function Arguments Limited

Preferred number of parameters:

1\. Zero parameters – ****Niladic****

2\. One parameter – ****Monadic****

3\. Two parameters – ****Dyadic****

4\. Three parameters – ****Triadic****, but try to avoid where possible

5\. More than three – ****Polyadic****, which should be avoided unless there is strong justification

When more than two or three values are closely related, they can be wrapped into a class of their own.

Example from the presentation:

```java

Circle makeCircle(double x, double y, double radius);

```

can become:

```java

Circle makeCircle(Point center, double radius);

```

This improves both structure and naming clarity.

### 2.7 Avoid Hidden Side Effects

A function should not promise one thing through its name while secretly doing something else.

**Rule:** The function's name and actual behavior must agree.

### 2.8 Follow Command-Query Separation

A function should either:

- ****do something****, or

- ****answer something****,

but not both.

This separation makes method names easier to understand because the name can clearly represent either a command or a query.

---

## 3. Naming Rules Reinforced by the Comments Section

### 3.1 Comments Do Not Make Up for Bad Code

A comment should not be used to repair an unclear variable or method name.

If the code can be made self-explanatory by renaming a variable, method, or class, prefer the better name.

### 3.2 Explain Yourself in Code

Instead of writing a comment to explain a complicated condition, use a descriptive method name.

****Less clear:****

```java

// Check to see if the employee is eligible for full benefits

if ((employee.flags & HOURLY_FLAG) && (employee.age > 65)) {

    ...

}

```

****Clearer:****

```java

if (employee.isEligibleForFullBenefits()) {

    ...

}

```

**Rule:** Prefer meaningful names that make the code explain itself.

### 3.3 Use Function Names to Convey Information

Where possible, put the information into the function name instead of a comment.

For example, a method named `responderBeingTested()` communicates more clearly than a vague method name followed by a comment explaining what it returns.

### 3.4 Use Comments for Intent, Not for Poor Names

Useful comments can explain ****why a decision was made****, especially when the reason is not obvious from the code.

The presentation gives examples such as:

- explaining why many threads are being created to reproduce a race condition;

- explaining that `SimpleDateFormat` is not thread-safe and therefore each instance must be created independently.

### 3.5 TODO Comments

- `TODO` comments can indicate work that must be completed later.

- They should be regularly reviewed, cleaned, and reduced.

### 3.6 Amplification Comments

A comment can be useful when it emphasizes the importance of a non-obvious detail that must not be removed or changed carelessly.

### 3.7 Write High-Quality Comments

If a comment is necessary, spend the time required to make it clear, accurate, and useful.

### 3.8 Avoid Redundant Comments

Do not repeat what the code already says clearly. Sometimes reading the code itself is faster than reading a repetitive comment.

### 3.9 Avoid Unnecessary Mandated Comments

Not every function or parameter needs a Javadoc comment.

**Rule:** Do not generate comments only to satisfy a habit or template when the name and code already communicate the information clearly.

---

## 4. Variable Placement and Formatting Rules

These rules from the formatting section affect how easily variable names can be understood in context.

### 4.1 Vertical Formatting

- Try to keep a file below an upper limit of about ****500 lines****.

- A source file should read like a newspaper.

- The beginning should give the high-level summary.

- More detailed information should appear as the reader moves downward.

### 4.2 Use Vertical Openness Between Concepts

Separate different concepts with whitespace so variables, methods, and logical sections are visually easy to identify.

### 4.3 Declare Variables Close to Their Usage

**Rule:** Local variables should be declared as close as possible to the place where they are used.

This reduces the distance between a name and the code that gives the name meaning.

### 4.4 Put Instance Variables at the Top of the Class

Instance variables should be declared near the top of the class so readers can quickly see the object's state.

### 4.5 Keep Related Functions Vertically Close

If one function calls another:

- keep them close to each other where possible;

- place the caller above the callee where possible.

This makes names and the relationships between operations easier to follow.

### 4.6 Prefer Short Lines

- Prefer short lines.

- The presentation recommends a maximum of about ****80 characters per line****.

### 4.7 Use Proper Indentation

Do not collapse scopes into a single line.

Good indentation makes the context of variables, conditions, and functions easier to understand.

---

---

## 5. Additional Lecturer/Viva Rules and Technical Corrections

These points are included so the file also contains the extra rules and examples
from the lecturer/viva notes.

### 5.1 `i`, `j`, and `k` Are Acceptable for Small Loop Counters

Single-letter names are acceptable when their scope is tiny and their meaning is
obvious.

```java
for (int i = 0; i < 10; i++) {
    for (int j = 0; j < 10; j++) {
        for (int k = 0; k < 10; k++) {
            // Small, obvious local scope.
        }
    }
}
```

**Rules:**

- `i`, `j`, and `k` are acceptable for conventional loop counters.
- Do not use them for important domain values or long-lived variables.
- If the index itself has meaning, use a descriptive name.

```java
for (int studentIndex = 0;
     studentIndex < students.size();
     studentIndex++) {
    ...
}
```

### 5.2 Magic Numbers — Correct Rule

Do not treat **every numeric literal** as a magic number.

This is acceptable:

```java
for (int i = 0; i < 10; i++) {
    ...
}
```

The `0` is a conventional starting index and does not need a named constant.

A number becomes a problem when it represents a meaningful domain rule,
threshold, status, configuration, conversion, or special condition and its
meaning is not obvious.

**Bad:**

```java
if (mark > 80) {
    assignGradeA();
}
```

**Good Java style:**

```java
static final int A_GRADE_MARK = 80;

if (mark > A_GRADE_MARK) {
    assignGradeA();
}
```

For the Minesweeper example:

**Bad:**

```java
if (cell[0] == 4) {
    ...
}
```

**Better:**

```java
if (cell[STATUS_VALUE] == FLAGGED) {
    ...
}
```

**Better still:**

```java
if (cell.isFlagged()) {
    ...
}
```

**AI enforcement rule:** Replace unexplained **domain-specific** numbers with
meaningful constants or abstractions. Do not mechanically replace obvious loop
indices such as `0` and `1`.

### 5.3 Searchable Names Are Useful in a Viva

Names should be easy to search in an editor during:

- a viva;
- debugging;
- maintenance;
- code review;
- refactoring.

For example, searching for:

```text
WORK_DAYS_PER_WEEK
```

is more precise than searching for every occurrence of:

```text
5
```

### 5.4 Collection Names Must Not Lie About Their Type

If an array contains accounts:

```java
Account[] accounts = new Account[]{};
```

Do not write:

```java
Account[] accountsList = new Account[]{};
```

because it is not a `List`.

Prefer a plural name that communicates the contents:

```text
accounts
customers
orders
flaggedCells
```

Only mention the concrete collection type in the name when that type is
accurate and genuinely important to understanding the code.

### 5.5 Class/Object Names vs Method Names

Classes and objects should normally be nouns or noun phrases:

```text
Person
Customer
Account
Address
AddressParser
```

Methods/functions should normally be verbs or verb phrases:

```text
getName()
setName()
save()
postPayment()
calculateTotal()
isEligibleForFullBenefits()
```

### 5.6 Interface and Abstract-Class Names

Do not automatically prefix interfaces with `I`.

**Avoid:**

```java
IShapeFactory
```

**Prefer:**

```java
ShapeFactory
```

Do not automatically prefix an abstract class with `Abstract`.

**Avoid when unnecessary:**

```java
AbstractShapeFactory
```

**Prefer the domain concept:**

```java
ShapeFactory
```

If concrete implementations require distinction, use meaningful names such as:

```text
DefaultShapeFactory
CachedShapeFactory
RemoteShapeFactory
```

### 5.7 One Word per Concept

Do not use multiple words for the same conceptual operation.

**Avoid:**

```java
monthlyReport.getCustomers();
monthlyReport.fetchLoyalCustomers();
monthlyReport.retrieveSuppliers();
```

**Prefer:**

```java
monthlyReport.getCustomers();
monthlyReport.getLoyalCustomers();
monthlyReport.getSuppliers();
```

### 5.8 Do Not Add Unnecessary Context

Inside:

```java
class Car
```

avoid:

```java
carMake
carModel
carColor
```

Prefer:

```java
make
model
color
```

The class already supplies the `Car` context.

### 5.9 High Cohesion and Low Coupling

Aim for:

```text
High Cohesion
Low Coupling
```

- **High cohesion:** code inside a class/function strongly belongs to the same
  responsibility.
- **Low coupling:** a unit depends on as little unnecessary knowledge about
  other units as possible.
- A change in one component should not unnecessarily propagate into unrelated
  components.

This supports smaller, clearer functions and more meaningful names.

### 5.10 Parameter Rule — Correct Interpretation

The presentation's order is:

1. Zero parameters — **Niladic** — ideal when natural.
2. One parameter — **Monadic**.
3. Two parameters — **Dyadic**.
4. Three parameters — **Triadic** — try to avoid where possible.
5. More than three — **Polyadic** — avoid unless strongly justified.

**AI enforcement:** Target **0–2 parameters** whenever practical.

Three parameters are not absolutely forbidden, but they should trigger a design
check. More than three should normally be refactored.

When related values belong together, wrap them in an object.

```java
Circle makeCircle(double x, double y, double radius);
```

can become:

```java
Circle makeCircle(Point center, double radius);
```

### 5.11 Use Comments for Decisions and Non-Obvious Reasons

Prefer:

```java
if (employee.isEligibleForFullBenefits()) {
    ...
}
```

over a complex condition plus a comment that merely restates its meaning.

Use comments when the **reason** cannot be expressed clearly by naming alone.

Example:

```java
// SimpleDateFormat is not thread-safe, so create a new instance per call.
```

If code must not be removed, explain **why**:

```java
// Do not remove this delay.
// It prevents the external service from rejecting back-to-back requests.
waitForRateLimitWindow();
```

Avoid useless comments such as:

```java
// Do not remove
```

with no reason.

### 5.12 Amplification Comments

A comment may emphasize an important non-obvious detail.

```java
String listItemContent = match.group(3).trim();

// trim() is required here because leading spaces make the item
// parse as a nested list.
```

Use amplification comments only where removing or changing a subtle detail could
cause incorrect behaviour.

### 5.13 Redundant Comments, Not "Redundant Code"

The presentation specifically warns about **redundant comments**.

**Bad:**

```java
// Increment i by one.
i++;
```

The comment adds no information.

The AI should still remove duplicated/dead code when appropriate, but that is a
separate clean-code concern from the presentation's redundant-comment rule.

### 5.14 Maintainability Heuristic

Use this lecturer/viva memory aid:

```text
Maintainability = Readability + Writability
```

Treat it as a practical heuristic rather than a formal mathematical equation.

Clean naming, high cohesion, low coupling, small functions, useful comments, and
consistent formatting make code easier to understand and safer to change.

### 5.15 Formatting Numbers — Correct Interpretation

The presentation gives two different guidelines:

```text
Vertical/file formatting: approximately 500 lines maximum per file
Horizontal formatting: approximately 80 characters maximum per line
```

Do not confuse them.

- **500 lines** refers to overall source-file/vertical formatting.
- **80 characters** refers to horizontal line length.

### 5.16 Avoid Collapsing Scopes to One Line

**Avoid:**

```java
if (isValid) { save(); }
```

**Prefer:**

```java
if (isValid) {
    save();
}
```

A block may contain only one meaningful function call while still using proper
multi-line indentation.

---

## 6. Final Mandatory Code-Editor AI Checklist

Before returning code, the AI must verify all applicable items:

### Naming

- [ ] Names reveal intent.
- [ ] Names communicate Why, What, and How.
- [ ] Names are truthful and not misleading.
- [ ] Similar variables have meaningful distinctions.
- [ ] Names are pronounceable.
- [ ] Important names are searchable.
- [ ] `i`, `j`, and `k` are used only in tiny obvious loop scopes.
- [ ] Domain-specific magic values have meaningful names/constants.
- [ ] Type information is not unnecessarily encoded in names.
- [ ] Member prefixes such as `m_` are avoided.
- [ ] Interface names avoid unnecessary `I` prefixes.
- [ ] Abstract-class names avoid unnecessary `Abstract` prefixes.
- [ ] Mental mappings are avoided.
- [ ] Classes/objects use nouns or noun phrases.
- [ ] Methods/functions use verbs or verb phrases.
- [ ] Variables use meaningful nouns/noun phrases where appropriate.
- [ ] One word is used consistently per concept.
- [ ] Solution-domain terms are used when suitable.
- [ ] Problem-domain terms are used when suitable.
- [ ] Unnecessary context is removed.

### Functions

- [ ] Functions are small.
- [ ] Functions do one thing, do it well, and do it only.
- [ ] Each function uses one level of abstraction.
- [ ] Step-down organization is followed.
- [ ] Function names are descriptive.
- [ ] Functions target 0–2 parameters when practical.
- [ ] Three parameters are justified.
- [ ] More than three parameters are refactored or strongly justified.
- [ ] Hidden side effects are avoided.
- [ ] Command-query separation is followed where practical.
- [ ] Cohesion is high.
- [ ] Coupling is low.

### Comments

- [ ] Comments are not being used to repair bad naming.
- [ ] Code explains itself through names wherever possible.
- [ ] Necessary comments explain intent, decisions, or constraints.
- [ ] TODO comments are specific and maintainable.
- [ ] Amplification comments protect important non-obvious details.
- [ ] Comments are clear and accurate.
- [ ] Redundant comments are removed.
- [ ] Unnecessary mandated/Javadoc comments are avoided.

### Formatting

- [ ] File structure reads from high level to details, like a newspaper.
- [ ] File length is kept near/below the approximate 500-line guideline when practical.
- [ ] Related concepts have vertical openness.
- [ ] Local variables are declared close to use.
- [ ] Instance variables are near the top of the class.
- [ ] Caller and callee functions are vertically close where practical.
- [ ] The caller is above the callee where practical.
- [ ] Lines are kept near/below the approximate 80-character guideline.
- [ ] Scopes are not collapsed onto one line.
- [ ] Indentation is consistent and readable.

### Final quality goal

```text
Meaningful Names
+ Small Focused Functions
+ High Cohesion
+ Low Coupling
+ Useful Non-Redundant Comments
+ Consistent Formatting
= More Maintainable Code
```

The generated code should be easy to **read, search, explain in a viva, write,
modify, test, and maintain**.


## 7. Consolidated Variable Naming Checklist

Before accepting a variable name, check the following:

- [ ] Does the name reveal its ****intent****?

- [ ] Does it explain ****why it exists, what it contains, and how it is used****?

- [ ] Can the code be understood without a comment explaining the name?

- [ ] Is the name truthful and free from ****disinformation****?

- [ ] If there are similar variables, do their names show a ****meaningful distinction****?

- [ ] Is the name ****pronounceable****?

- [ ] Is the name ****searchable****?

- [ ] Are single-letter names limited to very small, local scopes?

- [ ] Does the name's length and detail fit the size of its scope?

- [ ] Does it avoid encoding the variable's type into the name?

- [ ] Does it avoid unnecessary prefixes such as `m_`?

- [ ] Does it avoid forcing the reader to make a mental mapping?

- [ ] Does it use the same word consistently for the same concept?

- [ ] Does it use suitable solution-domain or problem-domain terminology?

- [ ] Does it avoid unnecessary repeated context such as `carMake` inside `Car`?

- [ ] Is the variable declared close to where it is used?

- [ ] If it is an instance variable, is it placed near the top of the class?

- [ ] Is the surrounding function small enough that the variable's meaning is easy to understand?

- [ ] Are comments used only when the code and names cannot clearly express the necessary intent?

---

## 8. Quick Naming Pattern Reference

\| Element | Preferred Style | Example |

\|---|---|---|

\| Local variable | Meaningful noun/noun phrase | `elapsedTimeInDays` |

\| Collection | Plural noun showing contents | `flaggedCells` |

\| Boolean | Question/state wording | `isFlagged`, `isEligibleForFullBenefits` |

\| Class/Object | Noun or noun phrase | `Customer`, `AddressParser` |

\| Method | Verb or verb phrase | `getName`, `setName`, `save` |

\| Constant | Searchable meaningful constant | `WORK_DAYS_PER_WEEK`, `FLAGGED` |

\| Source/destination pair | Role-based distinction | `source`, `destination` |

\| Domain concept | Problem-domain word | `gameBoard`, `Account` |

\| Technical concept | Solution-domain word | `BubbleSort`, `JobQueue` |

---

## 9. Main Principle

The overall message of the presentation is that names are a major part of clean code. A good name reduces the need for comments, lowers the amount of mental translation required from the reader, and makes the purpose of the code visible directly from the source.

When choosing between a short unclear name and a longer descriptive name, prefer the name that makes the code easiest to understand. At the same time, do not add unnecessary context that is already obvious from the surrounding class or scope.

---

## 10. Practice Exercise from the Presentation

Use an older Java codebase and identify at least ****five Clean Code principle violations****. For each violation, answer:

1\. What is the violation?

2\. Why is the principle important?

3\. What is the suggested fix?

When reviewing naming violations, use the checklist above to decide whether variables, classes, methods, constants, and parameters communicate their purpose clearly.