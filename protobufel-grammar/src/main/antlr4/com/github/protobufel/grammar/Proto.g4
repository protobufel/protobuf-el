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
/**
* A complete .proto format grammar.
* This grammar is used for the direct construction of a protobuf 
* FileDescriptorProto object. 
*
*/
grammar Proto;
import ProtoLiterals;

// all collections must have unique respective names
//proto : (fileOption | publicImport | regularImport | SEMI)* packageStatement? 
//        (fileOption | message | extend | enumStatement | service | SEMI )* EOF ;

// lax mix as in protoc
proto : topRepeatedStatement* packageStatement? topRepeatedStatement* EOF ;

topRepeatedStatement 
		: publicImport 
		| regularImport 
		| fileOption 
		| message 
		| extend 
		| enumStatement 
		| service 
		| SEMI ;

publicImport : 'import' 'public' importPath SEMI ;

regularImport : 'import' importPath SEMI ;

packageStatement : 'package' packageName SEMI ;

// Options stuff

fileOption : 'option' ( standardFileOption | customFileOption ) SEMI ;
customFileOption : customOption ;
standardFileOption 
    : 'java_package' '=' StringLiteral	# standardFileOptionJavaPackage
    | 'java_outer_classname' '=' StringLiteral	# standardFileOptionJavaOuterClassname
    | 'java_multiple_files' '=' BooleanLiteral	# standardFileOptionJavaMultipleFiles
    | 'java_generate_equals_and_hash' '=' BooleanLiteral # standardFileOptionJavaGenerateEqualsAndHash
    | 'optimize_for' '=' optimizeMode	# standardFileOptionOptimizeFor
    | 'go_package' '=' StringLiteral	# standardFileOptionGoPackage
    | 'cc_generic_services' '=' BooleanLiteral	# standardFileOptionCcGenericServices
    | 'java_generic_services' '=' BooleanLiteral # standardFileOptionJavaGenericServices
    | 'py_generic_services' '=' BooleanLiteral  # standardFileOptionPyGenericServices
    | 'java_string_check_utf8' '=' BooleanLiteral  # standardFileOptionJavaStringCheckUtf8
    | 'deprecated' '=' BooleanLiteral  # standardFileOptionDeprecated
    ;
optimizeMode 
    : 'SPEED'
    | 'CODE_SIZE'
    | 'LITE_RUNTIME'  
    ;

customOption: customOptionName '=' customOptionValue ;
customOptionName 
		: 'default' {notifyErrorListeners("cannot use default option here");} 
		| customOptionNamePart ( '.' customOptionNamePart )* ;
customOptionNamePart : '(' customOptionNamePartId ')' | identifier ;
customOptionNamePartId : extendedId ;
//TODO: should this also include list values?
customOptionValue: optionScalarValue | optionAggregateValue ;

optionAggregateValue: '{' optionAggregateValueField* RBRACE ;
optionAggregateValueField : aggregateCustomOptionName 
    ( 
    ':' (optionScalarValue | optionScalarListValue | optionAggregateListValue) 
    | optionAggregateValue 
    ) 
    ;
// we don't enforce List's element types; it should be done during actual TextFormat parsing! 
optionAggregateListValue: '[' (optionAggregateValue (',' optionAggregateValue)*)? ']' ;
optionScalarListValue: '[' (optionScalarValue (',' optionScalarValue)*)? ']' ;

aggregateCustomOptionName 
    : ( aggregateCustomOptionNamePart | identifier ) 
    ( '.' ( aggregateCustomOptionNamePart | identifier ) )* ;
aggregateCustomOptionNamePart : '[' extendedId ']' ;

service: 'service' identifier '{' ( methodStatement | serviceOption | SEMI )* RBRACE ;
serviceOption: 'option' ( standardServiceOption | customOption ) SEMI ;
standardServiceOption 
    : 'deprecated' '=' BooleanLiteral  # standardServiceOptionDeprecated
    ;

methodStatement: 'rpc' identifier '(' extendedId ')' 'returns' '(' extendedId ')' (SEMI | '{' methodOption* RBRACE) ;
methodOption: 'option' ( standardMethodOption | customOption ) SEMI ;
standardMethodOption 
    : 'deprecated' '=' BooleanLiteral  # standardMethodOptionDeprecated
    ;

message : 'message' identifier '{' messageStatements? RBRACE ;
messageStatements : (messageOption | groupOrFieldOrExtend | message | extensions 
	| enumStatement | oneofStatement | SEMI)+ ;
	
oneofStatement: 'oneof' identifier '{' oneofGroupOrField+ RBRACE ;
oneofGroupOrField : oneofGroup | oneofField ;
oneofField: 
    ( optionalScalarField | 
    	enumDefaultField |
      ( scalarType | extendedId ) identifier '=' fieldNumber fieldOptions?
    )
    SEMI 
    ;
oneofGroup : 'group' groupIdentifier '=' fieldNumber '{' messageStatements RBRACE ;
    

groupOrFieldOrExtend: group | field | extend ;

messageOption : 'option' ( standardMessageOption | customOption ) SEMI ;
standardMessageOption 
    : 'message_set_wire_format' '=' BooleanLiteral # standardMessageOptionMessageSetWireFormat
    | 'no_standard_descriptor_accessor' '=' BooleanLiteral  # standardMessageOptionNoStandardDescriptorAccessor 
    | 'deprecated' '=' BooleanLiteral  # standardMessageOptionDeprecated
    ;

extensions: 'extensions' fieldNumber extensionRangeEnd? SEMI ;
extensionRangeEnd: 'to' (fieldNumber | 'max') ;

extend : 'extend' extendedId '{' groupOrField+  RBRACE ;

groupOrField : group | field ;

group : label 'group' groupIdentifier '=' fieldNumber '{' messageStatements RBRACE ;

//enumStatement : 'enum' identifier '{' ( enumOption | enumField | SEMI )* RBRACE ;
enumStatement : 'enum' identifier '{' ( enumOption | SEMI )? enumField (enumOption | SEMI | enumField)* RBRACE ;
enumOption: 'option' ( standardEnumOption | customOption ) SEMI ;
standardEnumOption 
    : 'allow_alias' '=' BooleanLiteral	# standardEnumOptionAllowAlias
    | 'deprecated' '=' BooleanLiteral  # standardEnumOptionDeprecated
    ;

enumField : identifier '=' enumValue enumValueOptions? SEMI ;
enumValue : NegativeIntegerLiteral | IntegerLiteral ;
enumValueOptions: '[' (standardEnumValueOption | customOption) (',' customOption)* ']' ;
standardEnumValueOption 
    :  'deprecated' '=' BooleanLiteral  # standardEnumValueOptionDeprecated
    ;

field :
    ( 'optional' optionalScalarField | 
    	'optional' enumDefaultField |
      label ( scalarType | extendedId ) identifier '=' fieldNumber fieldOptions?
    )
    SEMI 
    ;
    
enumDefaultField: extendedId identifier '=' fieldNumber 
		'[' (fieldOption ',')* enumDefaultFieldOption ( ',' fieldOption )* ']' ;
enumDefaultFieldOption: 'default' '=' identifier ;

optionalScalarField 
    : doubleField
    | floatField
    | int32Field
    | int64Field
    | uint32Field
    | uint64Field
    | sint32Field
    | sint64Field
    | fixed32Field
    | fixed64Field
    | sfixed32Field
    | sfixed64Field
    | boolField
    | bytesField
    | stringField
    ;

doubleField : 'double' identifier '=' fieldNumber ('[' doubleFieldOption ( ',' doubleFieldOption )* ']')? ;
floatField : 'float' identifier '=' fieldNumber ('[' floatFieldOption ( ',' floatFieldOption )* ']')? ;
int32Field : 'int32' identifier '=' fieldNumber ('[' int32FieldOption ( ',' int32FieldOption )* ']')? ;
int64Field : 'int64' identifier '=' fieldNumber ('[' int64FieldOption ( ',' int64FieldOption )* ']')? ;
uint32Field : 'uint32' identifier '=' fieldNumber ('[' uint32FieldOption ( ',' uint32FieldOption )* ']')? ;
uint64Field : 'uint64' identifier '=' fieldNumber ('[' uint64FieldOption ( ',' uint64FieldOption )* ']')? ;
sint32Field : 'sint32' identifier '=' fieldNumber ('[' sint32FieldOption ( ',' sint32FieldOption )* ']')? ;
sint64Field : 'sint64' identifier '=' fieldNumber ('[' sint64FieldOption ( ',' sint64FieldOption )* ']')? ;
fixed32Field : 'fixed32' identifier '=' fieldNumber ('[' fixed32FieldOption ( ',' fixed32FieldOption )* ']')? ;
fixed64Field : 'fixed64' identifier '=' fieldNumber ('[' fixed64FieldOption ( ',' fixed64FieldOption )* ']')? ;
sfixed32Field : 'sfixed32' identifier '=' fieldNumber ('[' sfixed32FieldOption ( ',' sfixed32FieldOption )* ']')? ;
sfixed64Field : 'sfixed64' identifier '=' fieldNumber ('[' sfixed64FieldOption ( ',' sfixed64FieldOption )* ']')? ;
boolField : 'bool' identifier '=' fieldNumber ('[' boolFieldOption ( ',' boolFieldOption )* ']')? ;
stringField : 'string' identifier '=' fieldNumber ('[' stringFieldOption ( ',' stringFieldOption )* ']')? ;
bytesField : 'bytes' identifier '=' fieldNumber ('[' bytesFieldOption ( ',' bytesFieldOption )* ']')? ;

doubleFieldOption : 'default' '=' doubleValue | fieldOption ;
floatFieldOption : 'default' '=' floatValue | fieldOption ;
int32FieldOption : 'default' '=' int32Value | fieldOption ;
int64FieldOption : 'default' '=' int64Value | fieldOption ;
uint32FieldOption : 'default' '=' uint32Value | fieldOption ;
uint64FieldOption : 'default' '=' uint64Value | fieldOption ;
sint32FieldOption : 'default' '=' sint32Value | fieldOption ;
sint64FieldOption : 'default' '=' sint64Value | fieldOption ;
fixed32FieldOption : 'default' '=' fixed32Value | fieldOption ;
fixed64FieldOption : 'default' '=' fixed64Value | fieldOption ;
sfixed32FieldOption : 'default' '=' sfixed32Value | fieldOption ;
sfixed64FieldOption : 'default' '=' sfixed64Value | fieldOption ;
boolFieldOption : 'default' '=' boolValue | fieldOption ;
stringFieldOption : 'default' '=' stringValue | fieldOption ;
bytesFieldOption : 'default' '=' bytesValue | fieldOption ;

fieldOptions : '[' fieldOption (',' fieldOption)* ']' ;
fieldOption : standardFieldOption | customOption ;
standardFieldOption 
    : 'ctype' '=' cType	# standardFieldOptionCTypeOption
    | 'packed' '=' BooleanLiteral   # standardFieldOptionPacked
    | 'lazy' '=' BooleanLiteral # standardFieldOptionLazy 
    | 'deprecated' '=' BooleanLiteral   # standardFieldOptionDeprecated 
    // Experimental  
    | 'experimental_map_key' '=' StringLiteral  # standardFieldOptionExperimentalMapKey
    // Google-internal  
    | 'weak' '=' BooleanLiteral # standardFieldOptionWeak
    ;
cType 
    : 'STRING'
    | 'CORD'
    | 'STRING_PIECE'  
    ;    

packageName : identifier ('.' identifier)* ;

fieldNumber : IntegerLiteral ;

importPath : StringLiteral ;

label : 'optional' | 'repeated' | 'required' ;

optionScalarValue 
    :   (NegativeIntegerLiteral
    |   IntegerLiteral)   
    |   doubleValue  
    |   (StringLiteral
    |   BooleanLiteral)    
    |   identifier 
    ;

literal
    :   numberLiteral
    |   ( StringLiteral  |   BooleanLiteral )
    ;

numberLiteral 
    :   NegativeIntegerLiteral 
    |   NegativeFloatingPointLiteral
    |   IntegerLiteral    
    |   FloatingPointLiteral
    ;

unsignedNumberLiteral 
    :   IntegerLiteral    
    |   FloatingPointLiteral
    ;

intLiteral 
    :   NegativeIntegerLiteral 
    |   IntegerLiteral    
    ; 
    
doubleValue :	NegativeIntegerLiteral | IntegerLiteral | NegativeFloatingPointLiteral | FloatingPointLiteral ;
floatValue :	NegativeIntegerLiteral | IntegerLiteral | NegativeFloatingPointLiteral | FloatingPointLiteral ;
int32Value :	NegativeIntegerLiteral | IntegerLiteral ;
int64Value :	NegativeIntegerLiteral | IntegerLiteral ;
uint32Value :	IntegerLiteral ;
uint64Value :	IntegerLiteral ;
sint32Value :	NegativeIntegerLiteral | IntegerLiteral ;
sint64Value :	NegativeIntegerLiteral | IntegerLiteral ;
fixed32Value :	NegativeIntegerLiteral | IntegerLiteral ;
fixed64Value :	NegativeIntegerLiteral | IntegerLiteral ;
sfixed32Value :	NegativeIntegerLiteral | IntegerLiteral ;
sfixed64Value :	NegativeIntegerLiteral | IntegerLiteral ;
boolValue :	BooleanLiteral;
bytesValue :	StringLiteral | ByteStringLiteral;
stringValue :	StringLiteral;

groupIdentifier
    :   'Import'
    |   'Go_package'
    |   'Java_generic_services'
    |   'Repeated'
    |   'CODE_SIZE'
    |   'String'
    |   'Ctype'
    |   'Bool'
    |   'Public'
    |   'To'
    |   'Service'
    |   'LITE_RUNTIME'
    |   'Allow_alias'
    |   'Packed'
    |   'Option'
    |   'Weak'
    |   'Message_set_wire_format'
    |   'Deprecated'
    |   'Double'
    |   'Sint64'
    |   'Bytes'
    |   'Sint32'
    |   'Optional'
    |   'Enum'
    |   'Max'
    |   'No_standard_descriptor_accessor'
    |   'Extensions'
    |   'Extend'
    |   'Group'
    |   'Lazy'
    |   'Int64'
    |   'Cc_generic_services'
    |   'Int32'
    |   'Java_generate_equals_and_hash'
    |   'Message'
    |   'Fixed64'
    |   'Experimental_map_key'
    |   'Returns'
    |   'Fixed32'
    |   'Rpc'
    |   'Optimize_for'
    |   'Py_generic_services'
    |   'STRING'
    |   'CORD'
    |   'Required'
    |   'Package'
    |   'STRING_PIECE'
    |   'Java_package'
    |   'Java_multiple_files'
    |   'Uint32'
    |   'Java_outer_classname'
    |   'Float'
    |   'Sfixed64'
    |   'Sfixed32'
    |   'SPEED'
    |   'Default'
    |   'Uint64'
    |   'oneof'
    |   'deprecated'
    |   'java_string_check_utf8'
    // all Java built-in
    |   'Override'
    |   'Deprecated'
    |   'SuppressWarnings'
    |   'SafeVarargs'
    |   'FunctionalInterface'
    |   'Retention'
    |   'Documented'
    |   'Target'
    |   'Inherited'
    |   'Repeatable'
    |   'String'
    |   'Boolean'
    |   'Byte'
    |   'Character'
    |   'Short'
    |   'Integer'
    |   'Long'
    |   'Float'
    |   'Double'
    |   'Class'
    |   'Object'
    |   UpperIdentifier ;

identifier 
    :   'import'
    |   'go_package'
    |   'java_generic_services'
    |   'repeated'
    |   'CODE_SIZE'
    |   'string'
    |   'ctype'
    |   'bool'
    |   'public'
    |   'to'
    |   'service'
    |   'LITE_RUNTIME'
    |   'allow_alias'
    |   'packed'
    |   'option'
    |   'weak'
    |   'message_set_wire_format'
    |   'deprecated'
    |   'double'
    |   'sint64'
    |   'bytes'
    |   'sint32'
    |   'optional'
    |   'enum'
    |   'max'
    |   'no_standard_descriptor_accessor'
    |   'extensions'
    |   'extend'
    |   'group'
    |   'lazy'
    |   'int64'
    |   'cc_generic_services'
    |   'int32'
    |   'java_generate_equals_and_hash'
    |   'message'
    |   'fixed64'
    |   'experimental_map_key'
    |   'returns'
    |   'fixed32'
    |   'rpc'
    |   'optimize_for'
    |   'py_generic_services'
    |   'STRING'
    |   'CORD'
    |   'required'
    |   'package'
    |   'STRING_PIECE'
    |   'java_package'
    |   'java_multiple_files'
    |   'uint32'
    |   'java_outer_classname'
    |   'float'
    |   'sfixed64'
    |   'sfixed32'
    |   'SPEED'
    |   'default'
    |   'uint64'
    |   'oneof'
    |   'deprecated'
    |   'java_string_check_utf8'
    // all Java reserved and built-in
    |   'abstract'
    |   'assert'
    |   'boolean'
    |   'break'
    |   'byte'
    |   'case'
    |   'catch'
    |   'char'
    |   'class'
    |   'const'
    |   'continue'
    |   'default'
    |   'do'
    |   'double'
    |   'else'
    |   'enum'
    |   'extends'
    |   'final'
    |   'finally'
    |   'float'
    |   'for'
    |   'if'
    |   'goto'
    |   'implements'
    |   'import'
    |   'instanceof'
    |   'int'
    |   'interface'
    |   'long'
    |   'native'
    |   'new'
    |   'package'
    |   'private'
    |   'protected'
    |   'public'
    |   'return'
    |   'short'
    |   'static'
    |   'strictfp'
    |   'super'
    |   'switch'
    |   'synchronized'
    |   'this'
    |   'throw'
    |   'throws'
    |   'transient'
    |   'try'
    |   'void'
    |   'volatile'
    |   'while'
    |   'Override'
    |   'Deprecated'
    |   'SuppressWarnings'
    |   'SafeVarargs'
    |   'FunctionalInterface'
    |   'Retention'
    |   'Documented'
    |   'Target'
    |   'Inherited'
    |   'Repeatable'
    |   'String'
    |   'Boolean'
    |   'Byte'
    |   'Character'
    |   'Short'
    |   'Integer'
    |   'Long'
    |   'Float'
    |   'Double'
    |   'Class'
    |   'Object'
    |   UpperIdentifier 
    |   LowerIdentifier ;

extendedId : '.'? identifier ('.' identifier)* ;

// Lexer rules
//

// Keywords, sort of

scalarType :
		'double' |
		'float' |
		'int32' |
		'int64' |
		'uint32' |
		'uint64' |
		'sint32' |
		'sint64' |
		'fixed32' |
		'fixed64' |
		'sfixed32' |
		'sfixed64' |
		'bool' |
		'string' |
		'bytes' ;

// ProtoLiterals lexer

