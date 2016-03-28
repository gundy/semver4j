grammar NodeSemverVersion;


PERIOD
    : '.'
    ;

PLUS: '+';

EQ: '=';

anyLetter: 'A' | 'B' | 'C' | 'D' | 'E' | 'F' | 'G' | 'H' | 'I' | 'J' | 'K'
			| 'L' | 'M' | 'N' | 'O' | 'P' | 'Q' | 'R' | 'S' | 'T' | 'U' | 'V'
			| 'W' | 'X' | 'Y' | 'Z'
			| 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h' | 'i' | 'j' | 'k'
			| 'l' | 'm' | 'n' | 'o' | 'p' | 'q' | 'r' | 's' | 't' | 'u' | 'v'
			| 'w' | 'x' | 'y' | 'z';
HYPHEN: '-';

ZERO: '0';
NON_ZERO_DIGITS: '1'..'9';

fullySpecifiedVersion
	: EQ? ('V'|'v')? major=integer (
		PERIOD minor=integer (
			PERIOD patch=integer
			(HYPHEN? preReleaseIdentifiers=identifiers)?
			(PLUS buildIdentifiers=identifiers)?
		)?
	)? EOF
	;


identifiers
	: identifier (PERIOD identifier)*
	;

identifier
	: integer									# integerIdentifier
	| (HYPHEN | ZERO | NON_ZERO_DIGITS | anyLetter)+		# nonIntegerIdentifier
    ;

integer
	: ZERO
	| NON_ZERO_DIGITS (ZERO | NON_ZERO_DIGITS)*
	;
