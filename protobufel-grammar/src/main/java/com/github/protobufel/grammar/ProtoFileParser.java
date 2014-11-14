//
// Copyright © 2014, David Tesler (https://github.com/protobufel)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// * Neither the name of the <organization> nor the
// names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

package com.github.protobufel.grammar;

import static com.github.protobufel.grammar.PrimitiveTypesUtil.protoEscapeBytes;
import static com.github.protobufel.grammar.PrimitiveTypesUtil.unescapeBytes;
import static com.github.protobufel.grammar.PrimitiveTypesUtil.unescapeText;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import com.github.protobufel.grammar.ErrorListeners.ConsoleProtoErrorListener;
import com.github.protobufel.grammar.ErrorListeners.IBaseProtoErrorListener;
import com.github.protobufel.grammar.ErrorListeners.IProtoErrorListener;
import com.github.protobufel.grammar.Exceptions.FieldInExtensionRangeException;
import com.github.protobufel.grammar.Exceptions.InvalidExtensionRange;
import com.github.protobufel.grammar.Exceptions.NonUniqueException;
import com.github.protobufel.grammar.Exceptions.NonUniqueExtensionNumber;
import com.github.protobufel.grammar.Exceptions.UnresolvedTypeNameException;
import com.github.protobufel.grammar.PrimitiveTypesUtil.InvalidEscapeSequenceException;
import com.github.protobufel.grammar.ProtoParser.BoolFieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.BytesFieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.CustomOptionContext;
import com.github.protobufel.grammar.ProtoParser.CustomOptionNamePartContext;
import com.github.protobufel.grammar.ProtoParser.DoubleFieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.EnumDefaultFieldContext;
import com.github.protobufel.grammar.ProtoParser.EnumDefaultFieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.EnumFieldContext;
import com.github.protobufel.grammar.ProtoParser.EnumStatementContext;
import com.github.protobufel.grammar.ProtoParser.ExtendContext;
import com.github.protobufel.grammar.ProtoParser.ExtensionRangeEndContext;
import com.github.protobufel.grammar.ProtoParser.ExtensionsContext;
import com.github.protobufel.grammar.ProtoParser.FieldContext;
import com.github.protobufel.grammar.ProtoParser.FieldNumberContext;
import com.github.protobufel.grammar.ProtoParser.Fixed32FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.Fixed64FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.FloatFieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.GroupContext;
import com.github.protobufel.grammar.ProtoParser.Int32FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.Int64FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.MessageContext;
import com.github.protobufel.grammar.ProtoParser.MethodStatementContext;
import com.github.protobufel.grammar.ProtoParser.OneofFieldContext;
import com.github.protobufel.grammar.ProtoParser.OneofGroupContext;
import com.github.protobufel.grammar.ProtoParser.OneofStatementContext;
import com.github.protobufel.grammar.ProtoParser.OptionAggregateValueContext;
import com.github.protobufel.grammar.ProtoParser.OptionAggregateValueFieldContext;
import com.github.protobufel.grammar.ProtoParser.OptionScalarValueContext;
import com.github.protobufel.grammar.ProtoParser.OptionalScalarFieldContext;
import com.github.protobufel.grammar.ProtoParser.ProtoContext;
import com.github.protobufel.grammar.ProtoParser.PublicImportContext;
import com.github.protobufel.grammar.ProtoParser.RegularImportContext;
import com.github.protobufel.grammar.ProtoParser.ServiceContext;
import com.github.protobufel.grammar.ProtoParser.Sfixed32FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.Sfixed64FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.Sint32FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.Sint64FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.StandardEnumOptionAllowAliasContext;
import com.github.protobufel.grammar.ProtoParser.StandardEnumOptionDeprecatedContext;
import com.github.protobufel.grammar.ProtoParser.StandardEnumValueOptionDeprecatedContext;
import com.github.protobufel.grammar.ProtoParser.StandardFieldOptionCTypeOptionContext;
import com.github.protobufel.grammar.ProtoParser.StandardFieldOptionDeprecatedContext;
import com.github.protobufel.grammar.ProtoParser.StandardFieldOptionExperimentalMapKeyContext;
import com.github.protobufel.grammar.ProtoParser.StandardFieldOptionLazyContext;
import com.github.protobufel.grammar.ProtoParser.StandardFieldOptionPackedContext;
import com.github.protobufel.grammar.ProtoParser.StandardFieldOptionWeakContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionCcGenericServicesContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionDeprecatedContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionGoPackageContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaGenerateEqualsAndHashContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaGenericServicesContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaMultipleFilesContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaOuterClassnameContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaPackageContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaStringCheckUtf8Context;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionOptimizeForContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionPyGenericServicesContext;
import com.github.protobufel.grammar.ProtoParser.StandardMessageOptionDeprecatedContext;
import com.github.protobufel.grammar.ProtoParser.StandardMessageOptionMessageSetWireFormatContext;
import com.github.protobufel.grammar.ProtoParser.StandardMessageOptionNoStandardDescriptorAccessorContext;
import com.github.protobufel.grammar.ProtoParser.StandardMethodOptionDeprecatedContext;
import com.github.protobufel.grammar.ProtoParser.StandardServiceOptionDeprecatedContext;
import com.github.protobufel.grammar.ProtoParser.StringFieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.Uint32FieldOptionContext;
import com.github.protobufel.grammar.ProtoParser.Uint64FieldOptionContext;
import com.github.protobufel.grammar.SymbolScopes.NameContext;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto.ExtensionRange;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.FileOptions.OptimizeMode;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto;
import com.google.protobuf.DescriptorProtos.OneofDescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.UninterpretedOption;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;

/**
 * A parser from .proto into FileDescriptorProto.
 *
 * @author protobufel@gmail.com David Tesler
 */
class ProtoFileParser extends ProtoBaseListener {
  private static final String AGREGATE_VALUE_FIELD_DELIMITER = " ";
  // private static final int UNINTERPRETED_OPTION_FIELD_NUMBER = 999;
  public static final int MAX_FIELD_NUMBER = 536870911;
  protected final FileDescriptorProto.Builder fileBuilder;
  // protected SourceCodeInfo.Builder sourceBuilder;
  private final Scopes scopes;
  // the context reverse lookup by its protoBuilder, mostly needed for fields validations
  private final ContextLookup contextLookup;

  // The big question is whether to enforce uniqueness constraints in the parser
  // as much as possible, or defer it to FieldDescriptor.buildFrom() method; and
  // whether this method does all the checks!

  public ProtoFileParser(final String protoName) {
    this(protoName, new ConsoleProtoErrorListener(protoName));
  }

  public ProtoFileParser(final String protoName, final IBaseProtoErrorListener errorListener) {
    fileBuilder = FileDescriptorProto.newBuilder().setName(protoName);
    errorListener.setProtoName(protoName);
    contextLookup = new ContextLookup(errorListener);
    scopes = new Scopes(fileBuilder, contextLookup);
    // this.sourceBuilder = fileBuilder.getSourceCodeInfoBuilder();
  }

  public IProtoErrorListener getErrorListener() {
    return contextLookup.getErrorListener();
  }

  @Override
  public void enterProto(final ProtoContext ctx) {
    if (ctx.packageStatement() != null) {
      fileBuilder.setPackage(ctx.packageStatement().packageName().getText());
    }

    scopes.init();
  }

  public ParsedContext getParsedWithResolution() {
    // resolve what it can, next - during linking!
    // FIXME report errors early if there are no dependencies, instead of waiting for linking!
    // FIXME do nothing if there is a public dependency?
    final List<FieldDescriptorProto.Builder> unresolved = scopes.resolveAllSymbols();
    // only local validation here, as per protoc - other protos might clash with these!
    scopes.validateAllExtensionNumbers();
    return new ParsedContext(fileBuilder, unresolved, contextLookup);
  }

  public ParsedContext getParsed() {
    return new ParsedContext(fileBuilder, null, contextLookup);
  }

  @Override
  public void exitRegularImport(final RegularImportContext ctx) {
    fileBuilder.addDependency(removeQuotes(ctx.importPath().getText()));
  }

  @Override
  public void exitPublicImport(final PublicImportContext ctx) {
    fileBuilder.addDependency(removeQuotes(ctx.importPath().getText()));
    fileBuilder.addPublicDependency(fileBuilder.getDependencyCount() - 1);
    // FIXME everything after import public should really be ignored!
  }

  // @Override
  // public void exitPackageStatement(PackageStatementContext ctx) {
  // fileBuilder.setPackage(ctx.packageName().getText());
  // }

  // ****************************** All Options START ************************************

  // **** FileOptions

  @Override
  public void exitStandardFileOptionJavaPackage(final StandardFileOptionJavaPackageContext ctx) {
    verifyOptionNameUnique("javaPackage", ctx.getStart());
    scopes.getFileOptions().setJavaPackage(removeQuotes(ctx.StringLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionGoPackage(final StandardFileOptionGoPackageContext ctx) {
    verifyOptionNameUnique("goPackage", ctx.getStart());
    scopes.getFileOptions().setGoPackage(removeQuotes(ctx.StringLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionJavaOuterClassname(
      final StandardFileOptionJavaOuterClassnameContext ctx) {
    verifyOptionNameUnique("javaOuterClassname", ctx.getStart());
    scopes.getFileOptions().setJavaOuterClassname(removeQuotes(ctx.StringLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionJavaMultipleFiles(
      final StandardFileOptionJavaMultipleFilesContext ctx) {
    verifyOptionNameUnique("javaMultipleFiles", ctx.getStart());
    scopes.getFileOptions().setJavaMultipleFiles(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionCcGenericServices(
      final StandardFileOptionCcGenericServicesContext ctx) {
    verifyOptionNameUnique("ccGenericServices", ctx.getStart());
    scopes.getFileOptions().setCcGenericServices(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionPyGenericServices(
      final StandardFileOptionPyGenericServicesContext ctx) {
    verifyOptionNameUnique("pyGenericServices", ctx.getStart());
    scopes.getFileOptions().setPyGenericServices(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionJavaGenericServices(
      final StandardFileOptionJavaGenericServicesContext ctx) {
    verifyOptionNameUnique("javaGenericServices", ctx.getStart());
    scopes.getFileOptions().setJavaGenericServices(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionJavaGenerateEqualsAndHash(
      final StandardFileOptionJavaGenerateEqualsAndHashContext ctx) {
    verifyOptionNameUnique("javaGenerateEqualsAndHash", ctx.getStart());
    scopes.getFileOptions().setJavaGenerateEqualsAndHash(
        Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionOptimizeFor(final StandardFileOptionOptimizeForContext ctx) {
    verifyOptionNameUnique("optimizeFor", ctx.getStart());
    scopes.getFileOptions().setOptimizeFor(OptimizeMode.valueOf(ctx.optimizeMode().getText()));
  }

  @Override
  public void exitStandardFileOptionJavaStringCheckUtf8(
      final StandardFileOptionJavaStringCheckUtf8Context ctx) {
    verifyOptionNameUnique("javaStringCheckUtf8", ctx.getStart());
    scopes.getFileOptions().setJavaStringCheckUtf8(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFileOptionDeprecated(final StandardFileOptionDeprecatedContext ctx) {
    verifyOptionNameUnique("deprecated", ctx.getStart());
    scopes.getFileOptions().setDeprecated(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  // **** Standard MessageOptions

  @Override
  public void exitStandardMessageOptionMessageSetWireFormat(
      final StandardMessageOptionMessageSetWireFormatContext ctx) {
    verifyOptionNameUnique("messageSetWireFormat", ctx.getStart());
    scopes.getMessageOptions().setMessageSetWireFormat(
        Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardMessageOptionNoStandardDescriptorAccessor(
      final StandardMessageOptionNoStandardDescriptorAccessorContext ctx) {
    verifyOptionNameUnique("noStandardDescriptorAccessor", ctx.getStart());
    scopes.getMessageOptions().setNoStandardDescriptorAccessor(
        Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardMessageOptionDeprecated(final StandardMessageOptionDeprecatedContext ctx) {
    verifyOptionNameUnique("deprecated", ctx.getStart());
    scopes.getMessageOptions().setDeprecated(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  // **** Standard ServiceOptions

  @Override
  public void exitStandardServiceOptionDeprecated(final StandardServiceOptionDeprecatedContext ctx) {
    verifyOptionNameUnique("deprecated", ctx.getStart());
    scopes.getServiceOptions().setDeprecated(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  // **** Standard MethodOptions

  @Override
  public void exitStandardMethodOptionDeprecated(final StandardMethodOptionDeprecatedContext ctx) {
    verifyOptionNameUnique("deprecated", ctx.getStart());
    scopes.getMethodOptions().setDeprecated(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  // **** Standard EnumOptions

  @Override
  public void exitStandardEnumOptionDeprecated(final StandardEnumOptionDeprecatedContext ctx) {
    verifyOptionNameUnique("deprecated", ctx.getStart());
    scopes.getEnumOptions().setDeprecated(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardEnumOptionAllowAlias(final StandardEnumOptionAllowAliasContext ctx) {
    verifyOptionNameUnique("allow_alias", ctx.getStart());
    scopes.getEnumOptions().setAllowAlias(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  // **** Standard EnumValueOptions

  @Override
  public void exitStandardEnumValueOptionDeprecated(
      final StandardEnumValueOptionDeprecatedContext ctx) {
    verifyOptionNameUnique("deprecated", ctx.getStart());
    scopes.getEnumValueOptions().setDeprecated(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  // field options

  @Override
  public void exitStandardFieldOptionCTypeOption(final StandardFieldOptionCTypeOptionContext ctx) {
    verifyOptionNameUnique("ctype", ctx.getStart());
    scopes.getFieldOptions().setCtype(FieldOptions.CType.valueOf(ctx.cType().getText()));
  }

  @Override
  public void exitStandardFieldOptionExperimentalMapKey(
      final StandardFieldOptionExperimentalMapKeyContext ctx) {
    verifyOptionNameUnique("experimentalMapKey", ctx.getStart());
    scopes.getFieldOptions().setExperimentalMapKey(removeQuotes(ctx.StringLiteral().getText()));
  }

  @Override
  public void exitStandardFieldOptionLazy(final StandardFieldOptionLazyContext ctx) {
    verifyOptionNameUnique("lazy", ctx.getStart());
    scopes.getFieldOptions().setLazy(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFieldOptionDeprecated(final StandardFieldOptionDeprecatedContext ctx) {
    verifyOptionNameUnique("deprecated", ctx.getStart());
    scopes.getFieldOptions().setDeprecated(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFieldOptionWeak(final StandardFieldOptionWeakContext ctx) {
    verifyOptionNameUnique("weak", ctx.getStart());
    scopes.getFieldOptions().setWeak(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitStandardFieldOptionPacked(final StandardFieldOptionPackedContext ctx) {
    verifyOptionNameUnique("packed", ctx.getStart());
    scopes.getFieldOptions().setPacked(Boolean.valueOf(ctx.BooleanLiteral().getText()));
  }

  @Override
  public void exitCustomOption(final CustomOptionContext ctx) {
    verifyCustomOptionNameUnique(ctx);
    final UninterpretedOption.Builder optionBuilder = scopes.addCustomOption();

    for (final CustomOptionNamePartContext part : ctx.customOptionName().customOptionNamePart()) {
      if (part.identifier() == null) {
        optionBuilder.addNameBuilder().setNamePart(part.customOptionNamePartId().getText())
            .setIsExtension(true);
      } else {
        optionBuilder.addNameBuilder().setNamePart(part.identifier().getText())
            .setIsExtension(false);
      }
    }

    final OptionAggregateValueContext optionAggregateValue =
        ctx.customOptionValue().optionAggregateValue();

    if (optionAggregateValue != null) {
      // optionBuilder.setAggregateValue(optionAggregateValue.getText());
      final String aggregateValue = getAggregateValue(optionAggregateValue);
      optionBuilder.setAggregateValue(aggregateValue);
    } else {
      final OptionScalarValueContext optionScalarValue =
          ctx.customOptionValue().optionScalarValue();

      // TODO: 'inf', 'nan', and '\?' - deal with or not to deal with?
      if (optionScalarValue.identifier() != null) {
        optionBuilder.setIdentifierValue(optionScalarValue.identifier().getText());
      } else if (optionScalarValue.BooleanLiteral() != null) {
        optionBuilder.setStringValue(ByteString.copyFromUtf8(optionScalarValue.BooleanLiteral()
            .getText()));
      } else if (optionScalarValue.StringLiteral() != null) {
        optionBuilder.setStringValue(ByteString.copyFromUtf8(removeQuotes(optionScalarValue
            .StringLiteral().getText())));
      } else if (optionScalarValue.IntegerLiteral() != null) {
        // setCustomOptionIntValue(optionScalarValue.IntegerLiteral(), optionBuilder);
        optionBuilder.setPositiveIntValue(PrimitiveTypesUtil.parseUInt64(optionScalarValue
            .IntegerLiteral().getText()));
      } else if (optionScalarValue.NegativeIntegerLiteral() != null) {
        // setCustomOptionIntValue(optionScalarValue.NegativeIntegerLiteral(), optionBuilder);
        optionBuilder.setNegativeIntValue(PrimitiveTypesUtil.parseInt64(optionScalarValue
            .NegativeIntegerLiteral().getText()));
      } else if (optionScalarValue.doubleValue() != null) {
        optionBuilder.setDoubleValue(Double.parseDouble(optionScalarValue.doubleValue().getText()));
      }
    }
  }

  /**
   * Verifies uniqueness for custom options with a single NamePart. The multipart custom options
   * should be validated during UninterpetedOption resolution in OptionsBuilder, not here!
   */
  private void verifyCustomOptionNameUnique(final CustomOptionContext ctx) {
    if (ctx.customOptionName().customOptionNamePart().size() == 1) {
      final CustomOptionNamePartContext namePart = ctx.customOptionName().customOptionNamePart(0);
      verifyOptionNameUnique(namePart.getText(), namePart.getStart());
    }
  }

  private String getAggregateValue(final OptionAggregateValueContext optionAggregateValue) {
    final StringBuilder sb = new StringBuilder();
    appendSpaceToAggregateField(sb, optionAggregateValue);
    return sb.toString();
  }

  private void appendSpaceToAggregateField(final StringBuilder sb, final RuleNode ctx) {
    for (int i = 0; i < ctx.getChildCount(); i++) {
      final ParseTree child = ctx.getChild(i);

      if (child instanceof RuleNode) {
        appendSpaceToAggregateField(sb, (RuleNode) child);

        if (child instanceof OptionAggregateValueFieldContext) {
          sb.append(AGREGATE_VALUE_FIELD_DELIMITER);
        }
      } else {
        sb.append(child.getText());
      }
    }
  }



  /*
   * private StringBuilder appendDelimitedAggregateValue(final StringBuilder sb, final
   * OptionAggregateValueContext optionAggregateValue) { sb.append("{");
   * 
   * for (OptionAggregateValueFieldContext field : optionAggregateValue.optionAggregateValueField())
   * { appendDelimitedAggregateValueField(sb, field).append(AGREGATE_VALUE_FIELD_DELIMITER); }
   * 
   * return sb.append("}"); }
   * 
   * private StringBuilder appendDelimitedAggregateValueField(final StringBuilder sb, final
   * OptionAggregateValueFieldContext field) {
   * sb.append(field.aggregateCustomOptionName().getText());
   * 
   * final OptionAggregateValueContext optionAggregateValue = field.optionAggregateValue();
   * 
   * if (optionAggregateValue != null) { appendDelimitedAggregateValue(sb, optionAggregateValue); }
   * else { sb.append(":");
   * 
   * final OptionAggregateListValueContext optionAggregateListValue = field
   * .optionAggregateListValue();
   * 
   * if (optionAggregateListValue != null) { appendAggregateValueList(sb, optionAggregateListValue);
   * } }
   * 
   * return sb; }
   * 
   * private void appendAggregateValueList(final StringBuilder sb, final
   * OptionAggregateListValueContext optionAggregateListValue) { sb.append("[");
   * 
   * final List<OptionAggregateValueContext> aggregateValues = optionAggregateListValue
   * .optionAggregateValue();
   * 
   * if (!aggregateValues.isEmpty()) { for (OptionAggregateValueContext aggregateValue :
   * aggregateValues) { appendDelimitedAggregateValue(sb, aggregateValue).append(","); }
   * 
   * sb.deleteCharAt(sb.length() - 1); }
   * 
   * sb.append("]"); }
   */

  // ****************************** All Options END ************************************

  // **** Messages

  @Override
  public void enterMessage(final MessageContext ctx) {
    scopes.addMessage(ctx.identifier().getText());
  }

  @Override
  public void exitMessage(final MessageContext ctx) {
    scopes.popScope();
  }

  // Groups

  @Override
  public void enterGroup(final GroupContext ctx) {
    scopes.addGroup(ctx.groupIdentifier().getText());
  }

  @Override
  public void exitGroup(final GroupContext ctx) {
    scopes.popScope();
    final String groupName = ctx.groupIdentifier().getText();

    // add group's field
    final FieldDescriptorProto.Builder fieldBuilder = scopes.addField();
    setFieldBuilder(fieldBuilder, groupName.toLowerCase(), ctx.fieldNumber().getText(),
        ctx.label().getText(), FieldDescriptorProto.Type.TYPE_GROUP).setTypeName(groupName);

    scopes.popScope();
    contextLookup.addGroup(fieldBuilder, ctx);
    scopes.addIfUnresolved(fieldBuilder);
    scopes.verifyField(fieldBuilder);
  }

  // Extensions

  @Override
  public void exitExtensions(final ExtensionsContext ctx) {
    final Integer extensionStart = Integer.decode(ctx.fieldNumber().getText());
    final ExtensionRangeEndContext extensionRangeEnd = ctx.extensionRangeEnd();
    final Integer extensionEnd;

    if (extensionRangeEnd == null) {
      extensionEnd = extensionStart;
    } else {
      final FieldNumberContext fieldNumber = extensionRangeEnd.fieldNumber();
      extensionEnd = fieldNumber == null ? MAX_FIELD_NUMBER : Integer.decode(fieldNumber.getText());
    }

    scopes.addExtensionRange(extensionStart, extensionEnd, ctx);
  }

  // EnumStatements

  @Override
  public void enterEnumField(final EnumFieldContext ctx) {
    scopes.addEnumValue();
  }

  @Override
  public void exitEnumField(final EnumFieldContext ctx) {
    final EnumValueDescriptorProto.Builder enumValueBuilder =
        EnumValueDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
    enumValueBuilder.setName(ctx.identifier().getText()).setNumber(
        Integer.decode(ctx.enumValue().getText()));
    scopes.popScope();
  }

  @Override
  public void enterEnumStatement(final EnumStatementContext ctx) {
    scopes.addEnum(ctx.identifier().getText());
  }

  @Override
  public void exitEnumStatement(final EnumStatementContext ctx) {
    scopes.popScope();
  }

  // Extends

  @Override
  public void enterExtend(final ExtendContext ctx) {
    scopes.addExtend(ctx.extendedId().getText());
  }

  @Override
  public void exitExtend(final ExtendContext ctx) {
    scopes.popScope();
  }

  // Fields

  @Override
  public void enterField(final FieldContext ctx) {
    scopes.addField();
  }

  @Override
  public void exitField(final FieldContext ctx) {
    final FieldDescriptorProto.Builder fieldBuilder =
        FieldDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
    final OptionalScalarFieldContext optionalScalarField = ctx.optionalScalarField();

    if (optionalScalarField != null) {
      final ParseTree primitiveField = optionalScalarField.getChild(0);
      setScalarFieldBuilder(fieldBuilder, primitiveField.getChild(1).getText(), // name
          primitiveField.getChild(3).getText(), // number
          "optional", // label
          primitiveField.getChild(0).getText()); // type
    } else if (ctx.enumDefaultField() != null) {
      final EnumDefaultFieldContext enumDefaultField = ctx.enumDefaultField();
      setMessageFieldBuilder(fieldBuilder, enumDefaultField.identifier().getText(),
          enumDefaultField.fieldNumber().getText(), "optional",
          enumDefaultField.extendedId().getText()).setType(Type.TYPE_ENUM);
    } else if (ctx.scalarType() != null) {
      setScalarFieldBuilder(fieldBuilder, ctx.identifier().getText(), ctx.fieldNumber().getText(),
          ctx.label().getText(), ctx.scalarType().getText());
    } else { // this is a message or enum field
      setMessageFieldBuilder(fieldBuilder, ctx.identifier().getText(), ctx.fieldNumber().getText(),
          ctx.label().getText(), ctx.extendedId().getText());
    }

    scopes.popScope();
    contextLookup.addField(fieldBuilder, ctx);
    scopes.addIfUnresolved(fieldBuilder);
    scopes.verifyField(fieldBuilder);
  }

  @Override
  public void exitEnumDefaultFieldOption(final EnumDefaultFieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitSint64FieldOption(final Sint64FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitSfixed64FieldOption(final Sfixed64FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitSint32FieldOption(final Sint32FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitBoolFieldOption(final BoolFieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitInt32FieldOption(final Int32FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitSfixed32FieldOption(final Sfixed32FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitDoubleFieldOption(final DoubleFieldOptionContext ctx) {
    // TODO: what to do about 'inf' and 'nan'?
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitFloatFieldOption(final FloatFieldOptionContext ctx) {
    // TODO: what to do about 'inf' and 'nan'?
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitFixed64FieldOption(final Fixed64FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitFixed32FieldOption(final Fixed32FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitUint32FieldOption(final Uint32FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitUint64FieldOption(final Uint64FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  @Override
  public void exitBytesFieldOption(final BytesFieldOptionContext ctx) {
    if (ctx.getChildCount() == 3) {
      verifyOptionNameUnique("default", ctx.getStart());
      final FieldDescriptorProto.Builder fieldBuilder =
          FieldDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
      final String input = ctx.getChild(2).getText();

      try {
        final String result =
            protoEscapeBytes(unescapeBytes(input.substring(1, input.length() - 1)));
        fieldBuilder.setDefaultValue(result);
      } catch (final InvalidEscapeSequenceException e) {
        contextLookup.reportInvalidDefaultValue((ParserRuleContext) ctx.getChild(2), e);
      }
    }
  }

  @Override
  public void exitStringFieldOption(final StringFieldOptionContext ctx) {
    // TODO: what to do about '\?'
    if (ctx.getChildCount() == 3) {
      verifyOptionNameUnique("default", ctx.getStart());
      final FieldDescriptorProto.Builder fieldBuilder =
          FieldDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
      final String input = ctx.getChild(2).getText();

      try {
        final String result =
            unescapeText(input.substring(1, input.length() - 1).replaceAll("\\\\[?]", "?"));
        fieldBuilder.setDefaultValue(result);
      } catch (final InvalidEscapeSequenceException e) {
        contextLookup.reportInvalidDefaultValue((ParserRuleContext) ctx.getChild(2), e);
      }
    }
  }

  @Override
  public void exitInt64FieldOption(final Int64FieldOptionContext ctx) {
    setFieldDefaultValue(ctx);
  }

  private void setFieldDefaultValue(final ParserRuleContext ctx) {
    if (ctx.getChildCount() == 3) {
      verifyOptionNameUnique("default", ctx.getStart());
      final FieldDescriptorProto.Builder fieldBuilder =
          FieldDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
      fieldBuilder.setDefaultValue(ctx.getChild(2).getText());
    }
  }

  // services

  @Override
  public void enterService(final ServiceContext ctx) {
    scopes.addService();
  }

  @Override
  public void exitService(final ServiceContext ctx) {
    final ServiceDescriptorProto.Builder serviceBuilder =
        ServiceDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
    serviceBuilder.setName(ctx.identifier().getText());
    scopes.popScope();
  }

  // methods

  @Override
  public void enterMethodStatement(final MethodStatementContext ctx) {
    scopes.addMethod();
  }

  @Override
  public void exitMethodStatement(final MethodStatementContext ctx) {
    final MethodDescriptorProto.Builder methodBuilder =
        MethodDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
    methodBuilder.setName(ctx.identifier().getText()).setInputType(ctx.extendedId(0).getText())
        .setOutputType(ctx.extendedId(1).getText());
    scopes.popScope();
  }

  // oneofs

  @Override
  public void enterOneofStatement(final OneofStatementContext ctx) {
    scopes.addOneOf();
  }

  @Override
  public void exitOneofStatement(final OneofStatementContext ctx) {
    final OneofDescriptorProto.Builder oneofBuilder =
        OneofDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
    oneofBuilder.setName(ctx.identifier().getText());
    scopes.popScope();
    scopes.verifyOneofName(oneofBuilder);
  }

  // oneofFields

  @Override
  public void enterOneofField(final OneofFieldContext ctx) {
    scopes.addField();
  }

  // TODO: reuse exitField and this - they differ very little
  @Override
  public void exitOneofField(final OneofFieldContext ctx) {
    final FieldDescriptorProto.Builder fieldBuilder =
        FieldDescriptorProto.Builder.class.cast(scopes.getProtoBuilder());
    final OptionalScalarFieldContext optionalScalarField = ctx.optionalScalarField();

    if (optionalScalarField != null) {
      final ParseTree primitiveField = optionalScalarField.getChild(0);
      setScalarFieldBuilder(fieldBuilder, primitiveField.getChild(1).getText(), // name
          primitiveField.getChild(3).getText(), // number
          "optional", // label
          primitiveField.getChild(0).getText()); // type
    } else if (ctx.enumDefaultField() != null) {
      final EnumDefaultFieldContext enumDefaultField = ctx.enumDefaultField();
      setMessageFieldBuilder(fieldBuilder, enumDefaultField.identifier().getText(),
          enumDefaultField.fieldNumber().getText(), "optional",
          enumDefaultField.extendedId().getText()).setType(Type.TYPE_ENUM);
    } else if (ctx.scalarType() != null) {
      setScalarFieldBuilder(fieldBuilder, ctx.identifier().getText(), ctx.fieldNumber().getText(),
          "optional", ctx.scalarType().getText());
    } else { // this is a message field
      setMessageFieldBuilder(fieldBuilder, ctx.identifier().getText(), ctx.fieldNumber().getText(),
          "optional", ctx.extendedId().getText());
    }

    scopes.popScope();
    contextLookup.addField(fieldBuilder, ctx);
    scopes.addIfUnresolved(fieldBuilder);
    scopes.verifyField(fieldBuilder);
  }

  // oneofGroups

  @Override
  public void enterOneofGroup(final OneofGroupContext ctx) {
    scopes.addGroup(ctx.groupIdentifier().getText());
  }

  @Override
  public void exitOneofGroup(final OneofGroupContext ctx) {
    scopes.popScope();
    final String groupName = ctx.groupIdentifier().getText();

    // add group's field
    final FieldDescriptorProto.Builder fieldBuilder = scopes.addField();
    setFieldBuilder(fieldBuilder, groupName.toLowerCase(), ctx.fieldNumber().getText(), "optional",
        FieldDescriptorProto.Type.TYPE_GROUP).setTypeName(groupName);

    scopes.popScope();
    contextLookup.addGroup(fieldBuilder, ctx);
    scopes.addIfUnresolved(fieldBuilder);
    scopes.verifyField(fieldBuilder);
  }

  // **************** ParserUtils

  private FieldDescriptorProto.Builder setMessageFieldBuilder(
      final FieldDescriptorProto.Builder fieldBuilder, final String name, final String number,
      final String label, final String typeName) {
    return fieldBuilder.setName(name)
        .setLabel(FieldDescriptorProto.Label.valueOf("LABEL_" + label.toUpperCase()))
        .setNumber(Integer.decode(number))
        // .setType(FieldDescriptorProto.Type.TYPE_MESSAGE) this could be ENUM or MESSAGE, so we
        // don't set it!
        .setTypeName(typeName);
  }

  private FieldDescriptorProto.Builder setScalarFieldBuilder(
      final FieldDescriptorProto.Builder fieldBuilder, final String name, final String number,
      final String label, final String type) {
    return setFieldBuilder(fieldBuilder, name, number, label,
        FieldDescriptorProto.Type.valueOf("TYPE_" + type.toUpperCase()));
  }

  private FieldDescriptorProto.Builder setFieldBuilder(
      final FieldDescriptorProto.Builder fieldBuilder, final String name, final String number,
      final String label, final FieldDescriptorProto.Type type) {
    return fieldBuilder.setName(name)
        .setLabel(FieldDescriptorProto.Label.valueOf("LABEL_" + label.toUpperCase()))
        .setNumber(Integer.decode(number)).setType(type);
  }

  private String removeQuotes(final String text) {
    return text.substring(1, text.length() - 1);
  }

  private void verifyOptionNameUnique(final String optionName, final Token optionNameToken) {
    if (!scopes.isOptionNameUnique(optionName)) {
      contextLookup.reportNonUniqueOptionNameError(optionName, optionNameToken);
    }
  }

  // ContextLookup Stuff

  static final class ContextLookup {
    private final Map<Object, ParseTree> lookup;
    private final IProtoErrorListener errorListener;

    public ContextLookup(final IProtoErrorListener errorListener) {
      lookup = new IdentityHashMap<Object, ParseTree>();
      this.errorListener = errorListener;
    }

    public IProtoErrorListener getErrorListener() {
      return errorListener;
    }

    public ParseTree addField(final FieldDescriptorProtoOrBuilder key, final FieldContext ctx) {
      return lookup.put(key, ctx);
    }

    public ParseTree addField(final FieldDescriptorProtoOrBuilder key, final OneofFieldContext ctx) {
      return lookup.put(key, ctx);
    }

    public ParseTree addGroup(final FieldDescriptorProtoOrBuilder key, final GroupContext ctx) {
      return lookup.put(key, ctx);
    }

    public ParseTree addGroup(final FieldDescriptorProtoOrBuilder key, final OneofGroupContext ctx) {
      return lookup.put(key, ctx);
    }

    public ParseTree addExtensionRange(final ExtensionRange.Builder key, final ExtensionsContext ctx) {
      return lookup.put(key, ctx);
    }

    public ParseTree getContext(final Object key, final boolean removeMe) {
      return removeMe ? lookup.remove(key) : lookup.get(key);
    }

    public void reportInvalidDefaultValue(final ParserRuleContext ctx, final Exception e) {
      errorListener.validationError(ctx.getStart().getLine(), ctx.getStart()
          .getCharPositionInLine(), e.getMessage(), new RuntimeException(e));
    }

    public void reportFieldInExtensionRangeEror(final FieldDescriptorProtoOrBuilder field,
        final boolean removeMe) {
      final ParseTree context = getContext(field, removeMe);
      final Token token;

      if (context instanceof GroupContext) {
        token = getGroupNumberToken((GroupContext) context);
      } else { // this is a regular field
        token = getFieldNumberToken((FieldContext) context);
      }

      errorListener.validationError(token.getLine(), token.getCharPositionInLine(), null,
          new FieldInExtensionRangeException(field.getName()));
    }

    public void reportInvalidExtensionRange(final InvalidExtensionRange exception,
        final ExtensionsContext ctx) {
      final Token token = ctx.getStart();
      errorListener
          .validationError(token.getLine(), token.getCharPositionInLine(), null, exception);
    }

    public void reportNonUniqueFieldNameError(final FieldDescriptorProtoOrBuilder field,
        final boolean removeMe) {
      final ParseTree context = getContext(field, removeMe);
      final Token token;

      if (context instanceof GroupContext) {
        token = getGroupNameToken((GroupContext) context);
      } else { // this is a regular field
        token = getFieldNameToken((FieldContext) context);
      }

      errorListener.validationError(token.getLine(), token.getCharPositionInLine(), null,
          new NonUniqueException(field.getName(), "field name"));
    }


    public void reportNonUniqueOneofNameError(final OneofDescriptorProtoOrBuilder oneof,
        final boolean removeMe) {
      final OneofStatementContext context = (OneofStatementContext) getContext(oneof, removeMe);
      final Token token = context.identifier().getStart();

      errorListener.validationError(token.getLine(), token.getCharPositionInLine(), null,
          new NonUniqueException(oneof.getName(), "field name"));
    }


    public void reportNonUniqueFieldNumberError(final FieldDescriptorProtoOrBuilder field,
        final boolean removeMe) {
      final ParseTree context = getContext(field, removeMe);
      final Token token;

      if (context instanceof GroupContext) {
        token = getGroupNumberToken((GroupContext) context);
      } else { // this is a regular field
        token = getFieldNumberToken((FieldContext) context);
      }

      errorListener.validationError(token.getLine(), token.getCharPositionInLine(), null,
          new NonUniqueException(field.getName(), "field number"));
    }

    public void reportNonUniqueExtensionNumberError(final FieldDescriptorProtoOrBuilder field,
        final boolean removeMe) {
      final Token token = getFieldNumberToken((FieldContext) getContext(field, removeMe));
      errorListener.validationError(token.getLine(), token.getCharPositionInLine(), null,
          new NonUniqueExtensionNumber(field.getExtendee(), field.getName(), field.getNumber()));
    }

    public void reportUnresolvedTypeNameError(final FieldDescriptorProtoOrBuilder field,
        final List<String> unresolvedInfo, final boolean removeMe) {
      // final Token token = getFieldTypeNameToken((FieldContext) getContext(field, removeMe));
      // report just a start of the field context, as there can be also the extendContext
      // it would be overkill to cache extendContext just for this, though we could!
      final Token token = ((FieldContext) getContext(field, removeMe)).getStart();
      errorListener.validationError(token.getLine(), token.getCharPositionInLine(), null,
          new UnresolvedTypeNameException(field.getName(), unresolvedInfo));
    }

    public void reportNonUniqueOptionNameError(final String optionName, final Token optionNameToken) {
      errorListener.validationError(optionNameToken.getLine(), optionNameToken
          .getCharPositionInLine(), null, new NonUniqueException(optionName, "option name"));
    }

    private Token getGroupNumberToken(final GroupContext ctx) {
      return ctx.groupIdentifier().getStart();
    }

    private Token getGroupNameToken(final GroupContext ctx) {
      return ctx.fieldNumber().getStart();
    }

    private Token getFieldNumberToken(final FieldContext ctx) {
      if (ctx.fieldNumber() != null) {
        return ctx.fieldNumber().getStart();
      } else {
        return ((FieldNumberContext) ctx.optionalScalarField().getChild(0).getChild(3)).getStart();
      }
    }

    private Token getFieldNameToken(final FieldContext ctx) {
      if (ctx.identifier() != null) {
        return ctx.identifier().getStart();
      } else {
        return ((FieldNumberContext) ctx.optionalScalarField().getChild(0).getChild(1)).getStart();
      }
    }

    public void putAllFields(final Map<FieldDescriptorProtoOrBuilder, ? extends ParseTree> fields) {
      lookup.putAll(fields);
    }

    public int size() {
      return lookup.size();
    }

    public boolean isEmpty() {
      return lookup.isEmpty();
    }

    @Override
    public boolean equals(final Object o) {
      return lookup.equals(o);
    }

    @Override
    public int hashCode() {
      return lookup.hashCode();
    }
  }

  public static final class ParsedContext {
    private static final ParsedContext DEFAULT_DESCRIPTOR_CONTEXT = new ParsedContext();
    private FileDescriptorProtoOrBuilder proto;
    private final List<Map.Entry<FieldDescriptorProto.Builder, FieldContext>> unresolved;

    private ParsedContext() {
      proto = DescriptorProtos.getDescriptor().toProto();
      unresolved = Collections.emptyList();
    }

    ParsedContext(final FileDescriptorProto proto) {
      this.proto = proto;
      unresolved = Collections.emptyList();
    }

    private ParsedContext(final FileDescriptorProto.Builder proto,
        final List<FieldDescriptorProto.Builder> unresolved, final ContextLookup lookup) {
      this.proto = proto;

      if ((unresolved == null) || unresolved.isEmpty()) {
        this.unresolved = Collections.emptyList();
      } else {
        if (lookup == null) {
          throw new NullPointerException();
        }

        this.unresolved = new ArrayList<Map.Entry<FieldDescriptorProto.Builder, FieldContext>>();

        for (final FieldDescriptorProto.Builder field : unresolved) {
          final FieldContext context = (FieldContext) lookup.getContext(field, false);

          if (context == null) {
            throw new IllegalStateException("field context must not be null");
          }

          this.unresolved.add(new SimpleEntry<FieldDescriptorProto.Builder, FieldContext>(field,
              context));
        }
      }
    }

    public static ParsedContext getDescriptorProtoParsedContext() {
      return DEFAULT_DESCRIPTOR_CONTEXT;
    }

    public FileDescriptorProtoOrBuilder getProto() {
      return proto;
    }

    public boolean isBuilt() {
      return proto instanceof FileDescriptorProto;
    }

    public boolean resolveAllRefs(final Collection<FileDescriptor> dependencies,
        final IProtoErrorListener errorListener) {
      if (isBuilt()) {
        throw new IllegalStateException("not supported when proto is already built");
      }

      final Map<String, NameContext> cache = new HashMap<String, NameContext>();

      if (resolveAllRefs(dependencies, errorListener, cache)) {
        return true;
      }

      for (final Entry<FieldDescriptorProto.Builder, FieldContext> entry : unresolved) {
        reportUnresolvedTypeNameError(entry.getKey(), entry.getValue(), errorListener);
      }

      return false;
    }

    private boolean resolveAllRefs(final Collection<FileDescriptor> dependencies,
        final IProtoErrorListener errorListener, final Map<String, NameContext> cache) {
      if (unresolved.isEmpty()) {
        proto = ((FileDescriptorProto.Builder) proto).build();
        return true;
      } else if (dependencies.isEmpty()) {
        return false;
      }

      // resolve private dependencies first
      for (final FileDescriptor dependency : dependencies) {
        // FIXME check with protoc that only exact package names are searchable
        if (proto.getPackage().equals(dependency.getPackage())) {
          for (final Iterator<Entry<FieldDescriptorProto.Builder, FieldContext>> iterator =
              unresolved.iterator(); iterator.hasNext();) {
            final Entry<FieldDescriptorProto.Builder, FieldContext> entry = iterator.next();
            if (resolveField(entry.getKey(), entry.getValue(), dependency, cache)) {
              iterator.remove();
            }
          }

          if (unresolved.isEmpty()) {
            proto = ((FileDescriptorProto.Builder) proto).build();
            return true;
          }
        }
      }

      // now try public dependencies; they are transitive!
      for (final FileDescriptor dependency : dependencies) {
        final List<FileDescriptor> publicDependencies = dependency.getPublicDependencies();

        if (!publicDependencies.isEmpty()) {
          if (resolveAllRefs(publicDependencies, errorListener, cache)) {
            return true;
          }
        }
      }

      return false;
    }

    private void buildProto() {
      proto = ((FileDescriptorProto.Builder) proto).build();
    }

    private boolean resolveField(final FieldDescriptorProto.Builder field,
        final FieldContext fieldContext, final FileDescriptor dependency,
        final Map<String, NameContext> cache) {
      boolean isResolved = true;

      if (field.hasExtendee() && !field.getExtendee().startsWith(".")) {
        final NameContext nameContext = resolveName(field.getExtendee(), dependency, cache);

        if (nameContext.isEmpty()) {
          isResolved = false;
        } else {
          field.setExtendee(nameContext.getName());
        }
      }

      if (field.hasTypeName() && !field.getTypeName().startsWith(".")) {
        final NameContext nameContext = resolveName(field.getTypeName(), dependency, cache);

        if (nameContext.isEmpty()) {
          isResolved = false;
        } else {
          field.setTypeName(nameContext.getName());

          if (nameContext.isLeaf()) {
            field.setType(Type.TYPE_ENUM);
          } else {
            field.setType(Type.TYPE_MESSAGE);
          }
        }
      }

      return isResolved;
    }

    private NameContext resolveName(final String name, final FileDescriptor fileProto,
        final Map<String, NameContext> cache) {
      NameContext nameContext = cache.get(name);

      if (nameContext != null) {
        return nameContext;
      }

      final EnumDescriptor enumDesc = fileProto.findEnumTypeByName(name);

      if (enumDesc != null) {
        nameContext = NameContext.newResolvedInstance(enumDesc.getFullName(), true);
      } else {
        final Descriptor descriptor = fileProto.findMessageTypeByName(name);

        if (descriptor != null) {
          nameContext = NameContext.newResolvedInstance(descriptor.getFullName(), false);
        }
      }

      if ((nameContext == null) && !fileProto.getMessageTypes().isEmpty()) {
        nameContext = resolveName(name, fileProto.getMessageTypes());
      }

      if (nameContext == null) {
        nameContext = NameContext.emptyInstance();
      }

      cache.put(name, nameContext);
      return nameContext;
    }

    private NameContext resolveName(final String name, final List<Descriptor> messageTypes) {
      for (final Descriptor descriptor : messageTypes) {
        final EnumDescriptor enumDesc = descriptor.findEnumTypeByName(name);

        if (enumDesc != null) {
          return NameContext.newResolvedInstance(enumDesc.getFullName(), true);
        }

        final Descriptor messageDesc = descriptor.findNestedTypeByName(name);

        if (messageDesc != null) {
          return NameContext.newResolvedInstance(messageDesc.getFullName(), false);
        }

        if (descriptor.getNestedTypes().isEmpty()) {
          continue;
        }

        final NameContext nameContext = resolveName(name, descriptor.getNestedTypes());

        if (nameContext != null) {
          return nameContext;
        }
      }

      return null;
    }

    private List<String> getPublicDependencies(final FileDescriptorProtoOrBuilder fileProto) {
      if (fileProto.getPublicDependencyCount() == 0) {
        return Collections.emptyList();
      }

      final List<String> publicDependencies =
          new ArrayList<String>(fileProto.getPublicDependencyCount());

      for (final Integer index : fileProto.getPublicDependencyList()) {
        publicDependencies.add(fileProto.getDependency(index));
      }

      return publicDependencies;
    }

    private void reportUnresolvedTypeNameError(final FieldDescriptorProtoOrBuilder field,
        final FieldContext fieldContext, final IProtoErrorListener errorListener) {
      final Token token = fieldContext.getStart();
      errorListener.validationError(token.getLine(), token.getCharPositionInLine(), null,
          new UnresolvedTypeNameException(field.getName(), getUnresolvedInfo(field)));
    }

    // FIXME combine with Symbol.getUnresolvedInfo()?
    private List<String> getUnresolvedInfo(final FieldDescriptorProtoOrBuilder field) {
      final List<String> unresolvedProps = new ArrayList<String>();

      if (field.hasExtendee() && !field.getExtendee().startsWith(".")) {
        unresolvedProps.add("field " + field.getName() + "'s extendee property: '"
            + field.getExtendee() + "'");
      }

      if (field.hasTypeName() && !field.getTypeName().startsWith(".")) {
        unresolvedProps.add("field " + field.getName() + "'s typeName property: '"
            + field.getTypeName() + "'");
      }

      return unresolvedProps;
    }
  }
}
