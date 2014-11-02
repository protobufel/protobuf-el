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

import static com.github.protobufel.grammar.ParserUtils.getTotalFieldCount;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.protobufel.grammar.ParserUtils.CommonTokenStreamEx;
import com.github.protobufel.grammar.ProtoParser.CustomOptionContext;
import com.github.protobufel.grammar.ProtoParser.CustomOptionNamePartContext;
import com.github.protobufel.grammar.ProtoParser.CustomOptionValueContext;
import com.github.protobufel.grammar.ProtoParser.EnumStatementContext;
import com.github.protobufel.grammar.ProtoParser.FileOptionContext;
import com.github.protobufel.grammar.ProtoParser.OptionScalarValueContext;
import com.github.protobufel.grammar.ProtoParser.PackageStatementContext;
import com.github.protobufel.grammar.ProtoParser.ProtoContext;
import com.github.protobufel.grammar.ProtoParser.PublicImportContext;
import com.github.protobufel.grammar.ProtoParser.RegularImportContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionCcGenericServicesContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionGoPackageContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaGenerateEqualsAndHashContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaGenericServicesContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaMultipleFilesContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaOuterClassnameContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionJavaPackageContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionOptimizeForContext;
import com.github.protobufel.grammar.ProtoParser.StandardFileOptionPyGenericServicesContext;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.UninterpretedOption;

/**
 * A parser from .proto into FileDescriptorProto with {@link DescriptorProtos.SourceCodeInfo}.
 *
 * @author protobufel@gmail.com David Tesler
 */
// FIXME fully implement, refactor, and enable!
class SourceInfoProtoFileParser extends ProtoFileParser {
  private static boolean treatStandardOptionAsUninterpreted = true;
  private final LocationBuilder locationBuilder;
  private FileOptions.Builder fileOptionsBuilder; // FIXME add just to compile this file


  public SourceInfoProtoFileParser(final CommonTokenStreamEx tokens, final String protoName) {
    super(protoName);
    locationBuilder = new LocationBuilder(fileBuilder.getSourceCodeInfoBuilder(), tokens);
  }

  public static boolean isTreatStandardOptionAsUninterpreted() {
    return treatStandardOptionAsUninterpreted;
  }

  public static void setTreatStandardOptionAsUninterpreted(
      final boolean treatStandardOptionAsUninterpreted) {
    SourceInfoProtoFileParser.treatStandardOptionAsUninterpreted =
        treatStandardOptionAsUninterpreted;
  }

  @Override
  public ParsedContext getParsed() {
    // return fileBuilder;
    return null;
  }

  @Override
  public void exitProto(final ProtoContext ctx) {
    super.exitProto(ctx);
    locationBuilder.addLocation(0).comments(ctx).setAllSpan(ctx);
  }

  @Override
  public void exitRegularImport(final RegularImportContext ctx) {
    super.exitRegularImport(ctx);
    // locationBuilder.addLocation()
    // .comments(ctx)
    // .setAllSpan(ctx.importPath())
    // .addPath(FileDescriptorProto.DEPENDENCY_FIELD_NUMBER)
    // .addPath(fileBuilder.getDependencyCount() - 1);

    locationBuilder.addLocationForPrimitive(FileDescriptorProto.DEPENDENCY_FIELD_NUMBER)
        .comments(ctx).setAllSpan(ctx.importPath());
  }

  @Override
  public void exitPublicImport(final PublicImportContext ctx) {
    super.exitPublicImport(ctx);
    // locationBuilder.addLocation()
    // .setAllSpan(ctx.Public())
    // .addPath(FileDescriptorProto.PUBLIC_DEPENDENCY_FIELD_NUMBER)
    // .addPath(fileBuilder.getPublicDependencyCount() - 1)
    //
    // .addLocation()
    // .comments(ctx)
    // .setAllSpan(ctx.importPath())
    // .addPath(FileDescriptorProto.DEPENDENCY_FIELD_NUMBER)
    // .addPath(fileBuilder.getDependencyCount() - 1);

    locationBuilder.addLocationForPrimitive(FileDescriptorProto.PUBLIC_DEPENDENCY_FIELD_NUMBER)
        .setAllSpan((TerminalNode) ctx.getChild(1))

        .addLocationForPrimitive(FileDescriptorProto.DEPENDENCY_FIELD_NUMBER).comments(ctx)
        .setAllSpan(ctx.importPath());
  }

  @Override
  public void exitPackageStatement(final PackageStatementContext ctx) {
    super.exitPackageStatement(ctx);
    // locationBuilder.addLocation()
    // .comments(ctx)
    // .setAllSpan(ctx.packageName())
    // .addPath(FileDescriptorProto.PACKAGE_FIELD_NUMBER);

    locationBuilder.addLocationForPrimitive(FileDescriptorProto.PACKAGE_FIELD_NUMBER).comments(ctx)
        .setAllSpan(ctx.packageName());
  }

  @Override
  public void exitStandardFileOptionJavaPackage(final StandardFileOptionJavaPackageContext ctx) {
    super.exitStandardFileOptionJavaPackage(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.STRING_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitStandardFileOptionGoPackage(final StandardFileOptionGoPackageContext ctx) {
    super.exitStandardFileOptionGoPackage(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.STRING_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitStandardFileOptionJavaMultipleFiles(
      final StandardFileOptionJavaMultipleFilesContext ctx) {
    super.exitStandardFileOptionJavaMultipleFiles(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.IDENTIFIER_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitStandardFileOptionJavaOuterClassname(
      final StandardFileOptionJavaOuterClassnameContext ctx) {
    super.exitStandardFileOptionJavaOuterClassname(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.STRING_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitStandardFileOptionCcGenericServices(
      final StandardFileOptionCcGenericServicesContext ctx) {
    super.exitStandardFileOptionCcGenericServices(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.IDENTIFIER_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitStandardFileOptionPyGenericServices(
      final StandardFileOptionPyGenericServicesContext ctx) {
    super.exitStandardFileOptionPyGenericServices(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.IDENTIFIER_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitStandardFileOptionJavaGenericServices(
      final StandardFileOptionJavaGenericServicesContext ctx) {
    super.exitStandardFileOptionJavaGenericServices(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.IDENTIFIER_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitStandardFileOptionJavaGenerateEqualsAndHash(
      final StandardFileOptionJavaGenerateEqualsAndHashContext ctx) {
    super.exitStandardFileOptionJavaGenerateEqualsAndHash(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.IDENTIFIER_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitStandardFileOptionOptimizeFor(final StandardFileOptionOptimizeForContext ctx) {
    super.exitStandardFileOptionOptimizeFor(ctx);
    doStandardOptionSource(ctx, UninterpretedOption.IDENTIFIER_VALUE_FIELD_NUMBER);
  }

  @Override
  public void exitFileOption(final FileOptionContext ctx) {
    super.exitFileOption(ctx);

    if (ctx.customFileOption() != null) {
      final CustomOptionContext customOptionCtx = ctx.customFileOption().customOption();

      locationBuilder.addLocation().setAllSpan(ctx)
          .addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER);

      final int optionIndex =
          (treatStandardOptionAsUninterpreted ? getTotalFieldCount(fileOptionsBuilder)
              : fileOptionsBuilder.getUninterpretedOptionCount()) - 1;

      locationBuilder.addLocationClone().addPath(FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER)
          .addPath(optionIndex);

      locationBuilder.addLocationClone().clearComments()
          .addPath(UninterpretedOption.NAME_FIELD_NUMBER)
          .setAllSpan(customOptionCtx.customOptionName());

      int i = -1;

      for (final CustomOptionNamePartContext namePart : customOptionCtx.customOptionName()
          .customOptionNamePart()) {
        locationBuilder.addLocation().addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
            .addPath(FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER).addPath(optionIndex)
            .addPath(UninterpretedOption.NAME_FIELD_NUMBER).addPath(++i).setAllSpan(namePart);

        locationBuilder
            .addLocationClone()
            .addPath(UninterpretedOption.NamePart.NAME_PART_FIELD_NUMBER)
            .setAllSpan(
                namePart.customOptionNamePartId() == null ? namePart.identifier() : namePart
                    .customOptionNamePartId());
      }

      // customOption value locations: can be scalar or aggregate!

      final CustomOptionValueContext customOptionValue = customOptionCtx.customOptionValue();
      int valuePath;

      if (customOptionValue.optionAggregateValue() != null) {
        valuePath = UninterpretedOption.AGGREGATE_VALUE_FIELD_NUMBER;
      } else {
        final OptionScalarValueContext optionScalarValue = customOptionValue.optionScalarValue();

        if (optionScalarValue.doubleValue() != null) {
          valuePath = UninterpretedOption.DOUBLE_VALUE_FIELD_NUMBER;
        } else if (optionScalarValue.identifier() != null) {
          valuePath = UninterpretedOption.IDENTIFIER_VALUE_FIELD_NUMBER;
        } else if (optionScalarValue.StringLiteral() != null
            || optionScalarValue.BooleanLiteral() != null) {
          valuePath = UninterpretedOption.STRING_VALUE_FIELD_NUMBER;
        } else if (optionScalarValue.NegativeIntegerLiteral() != null) {
          valuePath = UninterpretedOption.NEGATIVE_INT_VALUE_FIELD_NUMBER;
        } else if (optionScalarValue.IntegerLiteral() != null) {
          valuePath = UninterpretedOption.POSITIVE_INT_VALUE_FIELD_NUMBER;
        } else { // we shouldn't arrive here!
          throw new RuntimeException("custom option value has unidentified type!");
        }
      }

      locationBuilder.addLocation().addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
          .addPath(FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER).addPath(optionIndex)
          .addPath(valuePath).setAllSpan(customOptionValue);

      // should the aggregate locations be added here? BTW, protoc fails on aggregates!
    }
  }

  private void doStandardOptionSource(final StandardFileOptionContext ctx,
      final int customOptionValueType) {
    final FileOptionContext parentCtx = (FileOptionContext) ctx.getParent();

    locationBuilder.addLocation().setAllSpan(parentCtx)
        .addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER);

    if (treatStandardOptionAsUninterpreted) {
      final int optionIndex = getTotalFieldCount(fileOptionsBuilder) - 1;

      locationBuilder.addLocation().comments(parentCtx)
          .addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
          .addPath(FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER).addPath(optionIndex)
          .setAllSpan(parentCtx);

      locationBuilder.addLocation().addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
          .addPath(FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER).addPath(optionIndex)
          .addPath(UninterpretedOption.NAME_FIELD_NUMBER)
          .setAllSpan((TerminalNode) ctx.getChild(0));

      locationBuilder.addLocation().addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
          .addPath(FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER).addPath(optionIndex)
          .addPath(UninterpretedOption.NAME_FIELD_NUMBER).addPath(0)
          .setAllSpan((TerminalNode) ctx.getChild(0));

      locationBuilder.addLocation().addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
          .addPath(FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER).addPath(optionIndex)
          .addPath(UninterpretedOption.NAME_FIELD_NUMBER).addPath(0)
          .addPath(UninterpretedOption.NamePart.NAME_PART_FIELD_NUMBER)
          .setAllSpan((TerminalNode) ctx.getChild(0));

      locationBuilder.addLocation().addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
          .addPath(FileOptions.UNINTERPRETED_OPTION_FIELD_NUMBER).addPath(optionIndex)
          .addPath(customOptionValueType).setAllSpan(ctx.getChild(2));
    } else {
      locationBuilder
          .addLocation()
          .comments(parentCtx)
          .addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
          .addPath(
              FileOptions.getDescriptor().findFieldByName(ctx.getChild(0).getText()).getNumber())
          .setAllSpan(ctx.getChild(2));
    }
  }

  // ********************* EnumStatement START ****************

  @Override
  public void enterEnumStatement(final EnumStatementContext ctx) {
    super.enterEnumStatement(ctx);
    locationBuilder.addEnumLocation().comments(ctx).setAllSpan(ctx)

    .addLocationForPrimitive(EnumDescriptorProto.NAME_FIELD_NUMBER).setAllSpan(ctx.identifier());
  }

  @Override
  public void exitEnumStatement(final EnumStatementContext ctx) {
    super.exitEnumStatement(ctx);
    locationBuilder.popScope();
  }

  // ***************************** Universal Options treatment START
  // *********************

  private void doCustomOption(final CustomOptionContext ctx, final ParserRuleContext parentCtx) {
    locationBuilder.addLocation().setAllSpan(parentCtx);


    locationBuilder.addOptionLocation().setAllSpan(parentCtx).comments(parentCtx)

    .addOptionNameLocation().setAllSpan(ctx.customOptionName());

    for (final CustomOptionNamePartContext namePart : ctx.customOptionName().customOptionNamePart()) {
      locationBuilder
          .addLocationForPrimitive(UninterpretedOption.NAME_FIELD_NUMBER)
          .setAllSpan(namePart)

          .addLocationForPrimitive(UninterpretedOption.NamePart.NAME_PART_FIELD_NUMBER)
          .setAllSpan(
              namePart.customOptionNamePartId() == null ? namePart.identifier() : namePart
                  .customOptionNamePartId());
    }

    // exit OptionName scope
    locationBuilder.popScope();

    // customOption value locations: can be scalar or aggregate!

    final CustomOptionValueContext customOptionValue = ctx.customOptionValue();
    int valuePath;

    if (customOptionValue.optionAggregateValue() != null) {
      valuePath = UninterpretedOption.AGGREGATE_VALUE_FIELD_NUMBER;
    } else {
      final OptionScalarValueContext optionScalarValue = customOptionValue.optionScalarValue();

      if (optionScalarValue.doubleValue() != null) {
        valuePath = UninterpretedOption.DOUBLE_VALUE_FIELD_NUMBER;
      } else if (optionScalarValue.identifier() != null) {
        valuePath = UninterpretedOption.IDENTIFIER_VALUE_FIELD_NUMBER;
      } else if (optionScalarValue.StringLiteral() != null
          || optionScalarValue.BooleanLiteral() != null) {
        valuePath = UninterpretedOption.STRING_VALUE_FIELD_NUMBER;
      } else if (optionScalarValue.NegativeIntegerLiteral() != null) {
        valuePath = UninterpretedOption.NEGATIVE_INT_VALUE_FIELD_NUMBER;
      } else if (optionScalarValue.IntegerLiteral() != null) {
        valuePath = UninterpretedOption.POSITIVE_INT_VALUE_FIELD_NUMBER;
      } else { // we shouldn't arrive here!
        throw new RuntimeException("custom option value has unidentified type!");
      }
    }

    locationBuilder.addLocationForPrimitive(valuePath).setAllSpan(customOptionValue)

    // exit Option scope
        .popScope();

    // should the aggregate locations be added here? BTW, protoc fails on
    // aggregates!
  }

  private void doStandardOption(final ParserRuleContext ctx, final ParserRuleContext parentCtx,
      final int customOptionValueType) {
    locationBuilder.addOptionLocation().setAllSpan(parentCtx).comments(parentCtx);

    if (treatStandardOptionAsUninterpreted) {
      locationBuilder.addOptionNameLocation().setAllSpan((TerminalNode) ctx.getChild(0))

      .addLocationForPrimitive(UninterpretedOption.NAME_FIELD_NUMBER)
          .setAllSpan((TerminalNode) ctx.getChild(0))

          .addLocationForPrimitive(UninterpretedOption.NamePart.NAME_PART_FIELD_NUMBER)
          .setAllSpan((TerminalNode) ctx.getChild(0))

          // exit OptionName scope
          .popScope()

          .addLocationForPrimitive(customOptionValueType).setAllSpan(ctx.getChild(2))

          // exit Option scope
          .popScope();
    } else { // we have all needed info to do this directly!
      throw new UnsupportedOperationException("standard option location is not implemented");
      /*
       * locationBuilder.addLocation().comments(parentCtx)
       * .addPath(FileDescriptorProto.OPTIONS_FIELD_NUMBER)
       * .addPath(FileOptions.getDescriptor().findFieldByName(
       * ctx.getChild(0).getText()).getNumber()) .setAllSpan(ctx.getChild(2));
       */
    }
  }

  // ***************************** Universal Options treatment END *********************
}
