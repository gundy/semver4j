grammar NodeSemverExpression;

SPACE: ' ' | '\t';

LOGICAL_OR_OPERATOR: SPACE* '||' SPACE*;

wildcard
    : ASTERISK
    | 'X'|'x'
    ;

basicRange: range;

part
    : integer			# integerIdentifier
    | (HYPHEN | NON_ZERO_NUMBER | letter) (HYPHEN | ZERO | NON_ZERO_NUMBER | letter)+  # nonIntegerIdentifier
	;


range
	: left=fullSemver SPACE HYPHEN SPACE right=fullSemver  # hyphenatedRangeOfFullySpecifiedVersions
	| simple (SPACE simple)*                    # logicalAndOfSimpleExpressions
	|                                           # emptyRange
	;

simple
	: partialWildcardSemver                    # wildcardRange
	| unaryOperator SPACE* partialWildcardSemver # operator
	| (GT EQ | LT EQ)? ASTERISK                # wildcardOperator
	| TILDE SPACE* fullSemver                  # tildeRange
	| CARET SPACE* fullSemver                  # caretRange
	| fullSemver                               # fullySpecifiedSemver
	;

rangeSet
	: basicRange (LOGICAL_OR_OPERATOR basicRange)* EOF   # logicalOrOfMultipleRanges
	;

unaryOperator
    : (GT EQ? | LT EQ? | EQ)
    ;

LT:  '<';
GT:  '>';
EQ:  '=';

fullSemver
	: EQ? ('V'|'v')? major=integer ('.' minor=integer ('.' patch=integer (HYPHEN? preReleaseIdentifiers=identifiers)? (PLUS buildIdentifiers=identifiers)?)?)?
	;

partialWildcardSemver
	: EQ? ('V'|'v')? major=integer ('.' wildcard ('.' wildcard)? )?
	| EQ? ('V'|'v')? major=integer ('.' minor=integer ('.' wildcard)? )?
	| EQ? ('V'|'v')? major=integer ('.' minor=integer ('.' patch=integer (HYPHEN? preReleaseIdentifiers=identifiers)? (PLUS buildIdentifiers=identifiers)?)?)?
	;

TILDE: '~';
CARET: '^';
HYPHEN: '-';
ZERO: '0';
NON_ZERO_NUMBER: '1'..'9';

integer
	: ZERO
	| NON_ZERO_NUMBER (ZERO | NON_ZERO_NUMBER)*
	;

ASTERISK: '*';


letter: 'A' | 'B' | 'C' | 'D' | 'E' | 'F' | 'G' | 'H' | 'I' | 'J' | 'K'
			| 'L' | 'M' | 'N' | 'O' | 'P' | 'Q' | 'R' | 'S' | 'T' | 'U' | 'V'
			| 'W' | 'X' | 'Y' | 'Z'
			| 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h' | 'i' | 'j' | 'k'
			| 'l' | 'm' | 'n' | 'o' | 'p' | 'q' | 'r' | 's' | 't' | 'u' | 'v'
			| 'w' | 'x' | 'y' | 'z';


PLUS:   '+';


identifiers
	: part (PERIOD part)*
	;

PERIOD
    : '.'
    ;


