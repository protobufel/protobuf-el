/*
 * Copyright © 2014, David Tesler (https://github.com/protobufel)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 [The "BSD licence"]
 Copyright (c) 2013 Terence Parr, Sam Harwell
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/** A Java 1.7 grammar for ANTLR v4 derived from ANTLR v3 Java grammar.
 *  Uses ANTLR v4's left-recursive expression notation.
 *  It parses ECJ, Netbeans, JDK etc...
 *
 *  Sam Harwell cleaned this up significantly and updated to 1.7!
 *
 *  You can test with
 *
 *  $ antlr4 Java.g4
 *  $ javac *.java
 *  $ grun Java compilationUnit *.java
 */
lexer grammar ProtoLiterals;


// §3.10.1 Integer Literals
NegativeIntegerLiteral : '-' IntegerLiteral ;
    
IntegerLiteral
    :   DecimalIntegerLiteral
    |   HexIntegerLiteral
    |   OctalIntegerLiteral
    ;

fragment
DecimalIntegerLiteral
    :   DecimalNumeral
    ;

fragment
HexIntegerLiteral
    :   HexNumeral
    ;

fragment
OctalIntegerLiteral
    :   OctalNumeral
    ;

fragment
DecimalNumeral
    :   '0'
    |   NonZeroDigit Digits?
    ;

fragment
Digits
    :   Digit+
    ;

fragment
Digit
    :   '0'
    |   NonZeroDigit
    ;

fragment
NonZeroDigit
    :   [1-9]
    ;

fragment
HexNumeral
    :   '0' [xX] HexDigits?
    ;

fragment
HexDigits
    :   HexDigit+
    ;

fragment
HexDigit
    :   [0-9a-fA-F]
    ;

fragment
OctalNumeral
    :   '0' OctalDigits
    ;

fragment
OctalDigits
    :   OctalDigit+
    ;

fragment
OctalDigit
    :   [0-7]
    ;

// §3.10.2 Floating-Point Literals

NegativeFloatingPointLiteral : '-' FloatingPointLiteral ;

FloatingPointLiteral
    :   DecimalFloatingPointLiteral
    |   'inf'
    |   'nan'
    ;

fragment
DecimalFloatingPointLiteral
    :   Digits '.' Digits? ExponentPart?
    |   '.' Digits ExponentPart?
    |   Digits ExponentPart
    |   Digits
    ;

fragment
ExponentPart
    :   ExponentIndicator SignedInteger
    ;

fragment
ExponentIndicator
    :   [eE]
    ;

fragment
SignedInteger
    :   Sign? Digits
    ;

fragment
Sign
    :   [+-]
    ;

// §3.10.3 Boolean Literals

BooleanLiteral
    :   'true'
    |   'false'
    ;

fragment
SingleCharacter
    :   ~['\\]
    ;

// §3.10.5 String Literals

StringLiteral 
    :   '"' StringCharacters? '"' 
    |   '\'' SingleStringCharacters? '\''    
    ;

fragment
StringCharacters
    :   StringCharacter+
    ;

fragment
StringCharacter
    :   ~["\\]
    |   EscapeSequence
    ;


fragment
SingleStringCharacters
    :   SingleStringCharacter+
    ;

fragment
SingleStringCharacter
    :   ~['\\]
    |   EscapeSequence
    ;

// §3.10.6 Escape Sequences for Character and String Literals

fragment
EscapeSequence
    :   '\\' [?btnfr"'\\]
    |   OctalEscape
    |   UnicodeEscape
    ;

fragment
OctalEscape
    :   '\\' OctalDigit
    |   '\\' OctalDigit OctalDigit
    |   '\\' ZeroToThree OctalDigit OctalDigit
    ;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

fragment
ZeroToThree
    :   [0-3]
    ;

// ByteString Literals

fragment
ByteEscapeSequence
    :   SimpleEscapeSequence
    |   OctalEscape
    |   HexadecimalEscapeSequence
    |   UniversalCharacterName
    ;

fragment
SimpleEscapeSequence
    :   '\\' ['"?abfnrtv\\]
    ;

fragment
UniversalCharacterName
    :   '\\u' HexQuad
    |   '\\U' HexQuad HexQuad
    ;

fragment
HexQuad
    :   HexDigit HexDigit HexDigit HexDigit
    ;

fragment
HexadecimalEscapeSequence
    :   '\\x' HexDigit+
    ;

ByteStringLiteral
    :   EncodingPrefix? '"' SCharSequence? '"'
    ;

fragment
EncodingPrefix
    :   'u8'
    |   'u'
    |   'U'
    |   'L'
    ;

fragment
SCharSequence
    :   SChar+
    ;

fragment
SChar
    :   ~["\\\r\n]
    |   ByteEscapeSequence
    ;


// Statement closers
RBRACE : '}' ;
SEMI : ';' ;

// §3.8 Identifiers (must appear after all keywords in the grammar)

LowerIdentifier
    :   JavaLowerLetter JavaLetterOrDigit*
    ;

UpperIdentifier
    :   JavaUpperLetter JavaLetterOrDigit*
    ;

fragment
JavaLowerLetter
    :   [a-z$_] // these are the "java letters" below 0xFF
    |  JavaNonASCIILetter 
    ;

fragment
JavaUpperLetter
    :   [A-Z] // these are the "java letters" below 0xFF
    |  JavaNonASCIILetter 
    ;

fragment
JavaNonASCIILetter
    :   // covers all characters above 0xFF which are not a surrogate
        ~[\u0000-\u00FF\uD800-\uDBFF]
        {Character.isJavaIdentifierStart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment
JavaLetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0xFF
    |   // covers all characters above 0xFF which are not a surrogate
        ~[\u0000-\u00FF\uD800-\uDBFF]
        {Character.isJavaIdentifierPart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

// §3.9 Keywords

fragment
ABSTRACT      : 'abstract';
 
fragment
ASSERT        : 'assert';
 
fragment
BOOLEAN       : 'boolean';
 
fragment
BREAK         : 'break';
 
fragment
BYTE          : 'byte';
 
fragment
CASE          : 'case';
 
fragment
CATCH         : 'catch';
 
fragment
CHAR          : 'char';
 
fragment
CLASS         : 'class';
 
fragment
CONST         : 'const';
 
fragment
CONTINUE      : 'continue';
 
fragment
DEFAULT       : 'default';
 
fragment
DO            : 'do';
 
fragment
DOUBLE        : 'double';
 
fragment
ELSE          : 'else';
 
fragment
ENUM          : 'enum';
 
fragment
EXTENDS       : 'extends';
 
fragment
FINAL         : 'final';
 
fragment
FINALLY       : 'finally';
 
fragment
FLOAT         : 'float';
 
fragment
FOR           : 'for';
 
fragment
IF            : 'if';
 
fragment
GOTO          : 'goto';
 
fragment
IMPLEMENTS    : 'implements';
 
fragment
IMPORT        : 'import';
 
fragment
INSTANCEOF    : 'instanceof';
 
fragment
INT           : 'int';
 
fragment
INTERFACE     : 'interface';
 
fragment
LONG          : 'long';
 
fragment
NATIVE        : 'native';
 
fragment
NEW           : 'new';
 
fragment
PACKAGE       : 'package';
 
fragment
PRIVATE       : 'private';
 
fragment
PROTECTED     : 'protected';
 
fragment
PUBLIC        : 'public';
 
fragment
RETURN        : 'return';
 
fragment
SHORT         : 'short';
 
fragment
STATIC        : 'static';
 
fragment
STRICTFP      : 'strictfp';
 
fragment
SUPER         : 'super';
 
fragment
SWITCH        : 'switch';
 
fragment
SYNCHRONIZED  : 'synchronized';
 
fragment
THIS          : 'this';
 
fragment
THROW         : 'throw';
 
fragment
THROWS        : 'throws';
 
fragment
TRANSIENT     : 'transient';
 
fragment
TRY           : 'try';
 
fragment
VOID          : 'void';
 
fragment
VOLATILE      : 'volatile';
 
fragment
WHILE         : 'while';

fragment
ReservedKeywords
    :  ABSTRACT
    |  ASSERT
    |  BOOLEAN
    |  BREAK
    |  BYTE
    |  CASE
    |  CATCH
    |  CHAR
    |  CLASS
    |  CONST
    |  CONTINUE
    |  DEFAULT
    |  DO
    |  DOUBLE
    |  ELSE
    |  ENUM
    |  EXTENDS
    |  FINAL
    |  FINALLY
    |  FLOAT
    |  FOR
    |  IF
    |  GOTO
    |  IMPLEMENTS
    |  IMPORT
    |  INSTANCEOF
    |  INT
    |  INTERFACE
    |  LONG
    |  NATIVE
    |  NEW
    |  PACKAGE
    |  PRIVATE
    |  PROTECTED
    |  PUBLIC
    |  RETURN
    |  SHORT
    |  STATIC
    |  STRICTFP
    |  SUPER
    |  SWITCH
    |  SYNCHRONIZED
    |  THIS
    |  THROW
    |  THROWS
    |  TRANSIENT
    |  TRY
    |  VOID
    |  VOLATILE
    |  WHILE
    ;

// Built-ins

fragment
BuiltInOverride            : 'Override';
 
fragment
BuiltInDeprecated          : 'Deprecated';
 
fragment
BuiltInSuppressWarnings    : 'SuppressWarnings';
 
fragment
BuiltInSafeVarargs         : 'SafeVarargs';
 
fragment
BuiltInFunctionalInterface : 'FunctionalInterface';
 
fragment
BuiltInRetention           : 'Retention';
 
fragment
BuiltInDocumented          : 'Documented';
 
fragment
BuiltInTarget              : 'Target';
 
fragment
BuiltInInherited           : 'Inherited';
 
fragment
BuiltInRepeatable          : 'Repeatable';

fragment
BuiltInString             : 'String';
 
fragment
BuiltInBoolean             : 'Boolean';
 
fragment
BuiltInByte                : 'Byte';
 
fragment
BuiltInCharacter           : 'Character';
 
fragment
BuiltInShort               : 'Short';
 
fragment
BuiltInInteger             : 'Integer';
 
fragment
BuiltInLong                : 'Long';
 
fragment
BuiltInFloat               : 'Float';
 
fragment
BuiltInDouble              : 'Double';
 
fragment
BuiltInClass               : 'Class';
 
fragment
BuiltInObject              : 'Object';

fragment
BuiltIns
    :  BuiltInOverride
    |  BuiltInDeprecated
    |  BuiltInSuppressWarnings
    |  BuiltInSafeVarargs
    |  BuiltInFunctionalInterface
    |  BuiltInRetention
    |  BuiltInDocumented
    |  BuiltInTarget
    |  BuiltInInherited
    |  BuiltInRepeatable
    |  BuiltInString
    |  BuiltInBoolean
    |  BuiltInByte
    |  BuiltInCharacter
    |  BuiltInShort
    |  BuiltInInteger
    |  BuiltInLong
    |  BuiltInFloat
    |  BuiltInDouble
    |  BuiltInClass
    |  BuiltInObject
    ;

fragment 
JavaAllReserved : ReservedKeywords | BuiltIns ;
 
//
// Whitespace and comments
//

WS  :  [ \t\r\n\u000C]+ -> skip;

COMMENT : '/*' .*? '*/'  -> channel(HIDDEN) ;
LINE_COMMENT :   '//' ~[\r\n]* -> channel(HIDDEN) ;

