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

import static com.github.protobufel.grammar.ProtoFileParser.MAX_FIELD_NUMBER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import com.github.protobufel.grammar.Exceptions.InvalidExtensionRange;
import com.github.protobufel.grammar.ProtoFileParser.ContextLookup;
import com.github.protobufel.grammar.ProtoParser.ExtensionsContext;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.DescriptorProto.Builder;
import com.google.protobuf.DescriptorProtos.DescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumOptions;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.MethodOptions;
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.DescriptorProtos.UninterpretedOption;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.GeneratedMessage;

/**
 * Classes for {@link ProtoFileParser}'s proto elements scope processing, including some uniqueness 
 * validation.
 *   
 * @author protobufel@gmail.com David Tesler
 */
class Scopes {
  private Scope<?> currentScope;
  private final FileScope rootScope;
  private final SymbolScopes symbolScopes;
  private final ContextLookup contextLookup;

  public Scopes(final FileDescriptorProto.Builder fileBuilder, final ContextLookup contextLookup) {
    rootScope = new FileScope(fileBuilder);
    currentScope = rootScope;
    this.contextLookup = contextLookup;
    symbolScopes = new SymbolScopes();
  }

  public void init() {
    symbolScopes.initGlobalScope(getFileBuilder().getPackage());
  }

  public FileDescriptorProto.Builder getFileBuilder() {
    return rootScope.protoBuilder;
  }

  @SuppressWarnings("unchecked")
  public List<FieldDescriptorProto.Builder> resolveAllSymbols() {
    return (List<FieldDescriptorProto.Builder>) symbolScopes.resolveAllSymbols(contextLookup);
  }

  // FIXME local extension fields with the same extendee, whether resolved or not, mustn't have
  // duplicates!
  public void validateAllExtensionNumbers() {
    final Map<String, Set<Integer>> extensionNumbers = new HashMap<String, Set<Integer>>(); // multiset
    Map<String, Set<Integer>> localExtensionNumbers = new HashMap<String, Set<Integer>>();

    for (final FieldDescriptorProtoOrBuilder extension : rootScope.protoBuilder
        .getExtensionOrBuilderList()) {
      processExtensionField(extensionNumbers, localExtensionNumbers, extension);
    }

    localExtensionNumbers = null;

    for (final DescriptorProtoOrBuilder childProto : rootScope.protoBuilder
        .getMessageTypeOrBuilderList()) {
      processAllExtensions(childProto, extensionNumbers);
    }
  }

  protected void processAllExtensions(final DescriptorProtoOrBuilder proto,
      final Map<String, Set<Integer>> extensionNumbers) {
    Map<String, Set<Integer>> localExtensionNumbers = new HashMap<String, Set<Integer>>();

    for (final FieldDescriptorProtoOrBuilder extension : proto.getExtensionOrBuilderList()) {
      processExtensionField(extensionNumbers, localExtensionNumbers, extension);
    }

    localExtensionNumbers = null;

    for (final DescriptorProtoOrBuilder childProto : proto.getNestedTypeOrBuilderList()) {
      processAllExtensions(childProto, extensionNumbers);
    }
  }

  protected void processExtensionField(final Map<String, Set<Integer>> extensionNumbers,
      final Map<String, Set<Integer>> localExtensionNumbers,
      final FieldDescriptorProtoOrBuilder extension) {
    if (extension.getExtendee().startsWith(".")) {
      // use global cache
      lookupExtension(extensionNumbers, extension);
    } else {
      // use local Message-level cache
      lookupExtension(localExtensionNumbers, extension);
    }
  }

  protected void lookupExtension(final Map<String, Set<Integer>> extensionNumberCache,
      final FieldDescriptorProtoOrBuilder extension) {
    Set<Integer> fieldNumbers = extensionNumberCache.get(extension.getExtendee());

    if (fieldNumbers == null) {
      fieldNumbers = new HashSet<Integer>();
      fieldNumbers.add(extension.getNumber());
      extensionNumberCache.put(extension.getExtendee(), fieldNumbers);
    } else if (!fieldNumbers.add(extension.getNumber())) {
      contextLookup.reportNonUniqueExtensionNumberError(extension, false);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends GeneratedMessage.Builder<T>> T getProtoBuilder() {
    return (T) currentScope.protoBuilder;
  }

  public Scope<?> popScope() {
    currentScope = currentScope.popScope();
    return currentScope;
  }

  public Scope<?> popScope(final Descriptor type) {
    currentScope = currentScope.popScope(type);
    return currentScope;
  }

  public Descriptor getDescriptor() {
    return currentScope.getDescriptor();
  }

  public Scope<?> getCurrentScope() {
    return currentScope;
  }

  private <T extends Scope<?>> T pushScope(final T scope) {
    currentScope = scope;
    return scope;
  }

  public DescriptorProto.Builder addMessage(final String messageName) {
    final DescriptorProto.Builder builder = currentScope.addMessage().setName(messageName);
    pushScope(new MessageScope(builder, currentScope));
    symbolScopes.pushScope(messageName);
    return builder;
  }

  public DescriptorProto.Builder addGroup(final String messageName) {
    final DescriptorProto.Builder builder = currentScope.addMessage().setName(messageName);
    pushScope(new GroupScope(builder, currentScope));
    symbolScopes.pushScope(messageName);
    return builder;
  }

  public EnumDescriptorProto.Builder addEnum(final String enumName) {
    final EnumDescriptorProto.Builder builder = currentScope.addEnum().setName(enumName);
    pushScope(new EnumScope(builder, currentScope));
    symbolScopes.addLeaf(enumName);
    return builder;
  }

  public EnumValueDescriptorProto.Builder addEnumValue() {
    final EnumValueDescriptorProto.Builder builder = currentScope.addEnumValue();
    pushScope(new EnumValueScope(builder, currentScope));
    return builder;
  }

  public FieldDescriptorProto.Builder addField() {
    FieldDescriptorProto.Builder builder = null;
    // FIXME and then remove try!
    try {
      builder = currentScope.addField();
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    pushScope(new FieldScope(builder, currentScope));
    return builder;
  }

  public boolean addIfUnresolved(final FieldDescriptorProto.Builder protoBuilder) {
    return symbolScopes.addIfUnresolved(protoBuilder);
  }

  public ServiceDescriptorProto.Builder addService() {
    final ServiceDescriptorProto.Builder builder = currentScope.addService();
    pushScope(new ServiceScope(builder, currentScope));
    return builder;
  }

  public MethodDescriptorProto.Builder addMethod() {
    final MethodDescriptorProto.Builder builder = currentScope.addMethod();
    pushScope(new MethodScope(builder, currentScope));
    return builder;
  }

  public void addExtensionRange(final int start, final int end, final ExtensionsContext ctx) {
    currentScope.addExtensionRange(start, end, ctx);
  }

  public void addExtend(final String extendee) {
    pushScope(newExtendScope(currentScope, extendee));
  }

  private <BType extends GeneratedMessage.Builder<BType>> ExtendScope<BType> newExtendScope(
      final Scope<BType> parent, final String extendee) {
    return new ExtendScope<BType>(parent, extendee);
  }

  public OneofDescriptorProto.Builder addOneOf() {
    final OneofDescriptorProto.Builder builder = currentScope.addOneof();
    pushScope(new OneofScope(builder, (MessageScope) currentScope));
    return builder;
  }

  public boolean isOptionNameUnique(final String optionName) {
    return currentScope.isOptionNameUnique(optionName);
  }

  public void verifyField(final FieldDescriptorProto.Builder field) {
    if (currentScope instanceof MessageScope) {
      final MessageScope scope = (MessageScope) currentScope;

      if (!scope.verifyFieldNameUnique(field.getName())) {
        contextLookup.reportNonUniqueFieldNameError(field, false);
      }

      if (!scope.verifyFieldNumberUnique(field.getNumber())) {
        contextLookup.reportNonUniqueFieldNumberError(field, false);
      }
    }
  }

  public void verifyOneofName(final OneofDescriptorProto.Builder oneof) {
    if (currentScope instanceof MessageScope) {
      final MessageScope scope = (MessageScope) currentScope;

      if (!scope.verifyOneofNameUnique(oneof.getName())) {
        contextLookup.reportNonUniqueOneofNameError(oneof, false);
      }
    }
  }

  public UninterpretedOption.Builder addCustomOption() {
    return currentScope.addCustomOption();
  }

  public FileOptions.Builder getFileOptions() {
    return currentScope.getFileOptions();
  }

  public MessageOptions.Builder getMessageOptions() {
    return currentScope.getMessageOptions();
  }

  public FieldOptions.Builder getFieldOptions() {
    return currentScope.getFieldOptions();
  }

  public EnumOptions.Builder getEnumOptions() {
    return currentScope.getEnumOptions();
  }

  public EnumValueOptions.Builder getEnumValueOptions() {
    return currentScope.getEnumValueOptions();
  }

  public ServiceOptions.Builder getServiceOptions() {
    return currentScope.getServiceOptions();
  }

  public MethodOptions.Builder getMethodOptions() {
    return currentScope.getMethodOptions();
  }

  // ***************** Member classes START

  protected abstract class Scope<BType extends GeneratedMessage.Builder<BType>> {
    private static final String NOT_APPLICABLE_IN_CURRENT_SCOPE =
        "not applicable in current scope!";
    protected final BType protoBuilder;
    private Scope<?> parent;
    // validation data
    private Set<String> optionNames = null;

    private Scope(final BType protoBuilder, final Scope<?> parent) {
      this.protoBuilder = protoBuilder;
      this.parent = parent;
    }

    protected Scope<?> getParent() {
      return parent;
    }

    public Scope<?> popScope() {
      if (parent == null) {
        throw new RuntimeException("cannot pop the root scope");
      }

      popSymbolScope();
      final Scope<?> result = parent;
      parent = null;
      return result;
    }

    /**
     * To be overriden by symbol storing scopes. At the moment, the symbol storing scopes are :
     * <ul>
     * <li>MessageScope
     * <li>EnumScope
     * <li>GroupScope
     */
    protected void popSymbolScope() {
      // NOOP
    }

    public Scope<?> popScope(final Descriptor type) {
      Scope<?> scope = this;

      while (!scope.getDescriptor().equals(type)) {
        scope = scope.popScope();
      }

      return scope;
    }

    public Descriptor getDescriptor() {
      return protoBuilder.getDescriptorForType();
    }

    public BType getProtoBuilder() {
      return protoBuilder;
    }

    public boolean isOptionNameUnique(final String optionName) {
      if (optionNames == null) {
        optionNames = new HashSet<String>();
      }

      return optionNames.add(optionName);
    }

    protected DescriptorProto.Builder addGroup() {
      return addMessage();
    }

    protected DescriptorProto.Builder addMessage() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected EnumDescriptorProto.Builder addEnum() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected FieldDescriptorProto.Builder addExtension() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected OneofDescriptorProto.Builder addOneof() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected void addExtensionRange(final int start, final int end, final ExtensionsContext ctx) {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected FieldDescriptorProto.Builder addField() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected EnumValueDescriptorProto.Builder addEnumValue() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected ServiceDescriptorProto.Builder addService() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected MethodDescriptorProto.Builder addMethod() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    // Options stuff

    protected UninterpretedOption.Builder addCustomOption() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected FileOptions.Builder getFileOptions() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected MessageOptions.Builder getMessageOptions() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected FieldOptions.Builder getFieldOptions() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected EnumOptions.Builder getEnumOptions() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected EnumValueOptions.Builder getEnumValueOptions() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected ServiceOptions.Builder getServiceOptions() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }

    protected MethodOptions.Builder getMethodOptions() {
      throw new RuntimeException(NOT_APPLICABLE_IN_CURRENT_SCOPE);
    }
  }

  protected class FileScope extends Scope<FileDescriptorProto.Builder> {

    private FileScope(final FileDescriptorProto.Builder protoBuilder) {
      super(protoBuilder, null);
    }

    @Override
    protected EnumDescriptorProto.Builder addEnum() {
      return protoBuilder.addEnumTypeBuilder();
    }

    @Override
    protected DescriptorProto.Builder addMessage() {
      return protoBuilder.addMessageTypeBuilder();
    }

    @Override
    protected FieldDescriptorProto.Builder addExtension() {
      return protoBuilder.addExtensionBuilder();
    }

    @Override
    protected ServiceDescriptorProto.Builder addService() {
      return protoBuilder.addServiceBuilder();
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return protoBuilder.getOptionsBuilder().addUninterpretedOptionBuilder();
    }

    @Override
    protected FileOptions.Builder getFileOptions() {
      return protoBuilder.getOptionsBuilder();
    }
  }

  protected class MessageScope extends Scope<DescriptorProto.Builder> {
    private static final int RESERVED_EXTENSION_RANGE_END = 19999;
    private static final int RESERVED_EXTENSION_RANGE_START = 19000;
    private IntegerRanges ranges;
    // TODO: uniqueness validation data - only validated what FileDescriptor won't, maybe we should!
    private Set<String> fieldNames = null;
    private Set<Integer> fieldNumbers = null;
    private Set<String> oneofNames = null;

    protected MessageScope(final DescriptorProto.Builder protoBuilder, final Scope<?> parent) {
      super(protoBuilder, parent);
    }

    @Override
    public Scope<?> popScope() {
      verifyFieldsNotInExtensionRanges();
      return super.popScope();
    }

    @Override
    protected void popSymbolScope() {
      // super.popSymbolScope();
      symbolScopes.popScope();
    }

    public boolean verifyOneofNameUnique(final String oneofName) {
      if (oneofNames == null) {
        oneofNames = new HashSet<String>();
      }

      return oneofNames.add(oneofName);
    }

    public boolean verifyFieldNameUnique(final String fieldName) {
      if (fieldNames == null) {
        fieldNames = new HashSet<String>();
      }

      return fieldNames.add(fieldName);
    }

    public boolean verifyFieldNumberUnique(final int fieldNumber) {
      if (fieldNumbers == null) {
        fieldNumbers = new HashSet<Integer>();
      }

      return fieldNumbers.add(fieldNumber);
    }

    public boolean verifyFieldsNotInExtensionRanges() {
      boolean isValid = true;

      for (final FieldDescriptorProtoOrBuilder field : protoBuilder.getFieldOrBuilderList()) {
        if (isInExtensionRanges(field.getNumber())) {
          contextLookup.reportFieldInExtensionRangeEror(field, false);
          isValid = false;
        }
      }

      return isValid;
    }

    private boolean isInExtensionRanges(final int fieldNumber) {
      return ranges != null && ranges.contains(fieldNumber);
    }

    @Override
    protected void addExtensionRange(final int start, final int end, final ExtensionsContext ctx) {
      checkValidExtensionRange(start, end, ctx);
      checkNoExtensionRangeOverlaps(start, end, ctx);
      protoBuilder.addExtensionRangeBuilder().setStart(start).setEnd(end + 1);
    }

    private boolean checkValidExtensionRange(final int start, final int end,
        final ExtensionsContext ctx) {
      if (start < 0 || start > end || end > MAX_FIELD_NUMBER) {
        contextLookup.reportInvalidExtensionRange(
            new InvalidExtensionRange("wrong extension range"), ctx);
      } else if (start >= RESERVED_EXTENSION_RANGE_START && end <= RESERVED_EXTENSION_RANGE_END) {
        contextLookup.reportInvalidExtensionRange(new InvalidExtensionRange(
            "cannot use reserved extension range"), ctx);
      }

      return true;
    }

    private boolean checkNoExtensionRangeOverlaps(final int start, final int end,
        final ExtensionsContext ctx) {
      ranges = ranges == null ? new IntegerRanges() : ranges;

      if (ranges.addRange(start, end)) {
        return true;
      }

      contextLookup.reportInvalidExtensionRange(new InvalidExtensionRange(start, end), ctx);
      return false;
    }

    @Override
    protected EnumDescriptorProto.Builder addEnum() {
      return protoBuilder.addEnumTypeBuilder();
    }

    @Override
    protected DescriptorProto.Builder addMessage() {
      return protoBuilder.addNestedTypeBuilder();
    }

    @Override
    protected FieldDescriptorProto.Builder addExtension() {
      return protoBuilder.addExtensionBuilder();
    }

    @Override
    protected OneofDescriptorProto.Builder addOneof() {
      return protoBuilder.addOneofDeclBuilder();
    }

    @Override
    protected FieldDescriptorProto.Builder addField() {
      return protoBuilder.addFieldBuilder();
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return protoBuilder.getOptionsBuilder().addUninterpretedOptionBuilder();
    }

    @Override
    protected MessageOptions.Builder getMessageOptions() {
      return protoBuilder.getOptionsBuilder();
    }
  }

  protected class GroupScope extends MessageScope {

    protected GroupScope(final DescriptorProto.Builder protoBuilder, final Scope<?> parent) {
      super(protoBuilder, parent);
    }
  }

  /**
   * A special scope delegating to its parent scope. All fields and groups of the extend element
   * should be within the parent scope; however, at least a location info for extend might require
   * this to be in its own scope.
   * 
   * @param <BType>
   * @author protobufel@gmail.com David Tesler
   */
  protected class ExtendScope<BType extends GeneratedMessage.Builder<BType>> extends Scope<BType> {
    private final String extendee;

    private ExtendScope(final Scope<BType> parent, final String extendee) {
      super(parent.getProtoBuilder(), parent);
      this.extendee = extendee;
    }

    @Override
    protected FieldDescriptorProto.Builder addField() {
      return getParent().addExtension().setExtendee(extendee);
    }

    @Override
    protected DescriptorProto.Builder addGroup() {
      return getParent().addGroup();
    }

    @Override
    protected Builder addMessage() {
      return getParent().addMessage();
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return getParent().addCustomOption();
    }

    @Override
    protected MessageOptions.Builder getMessageOptions() {
      return getParent().getMessageOptions();
    }

    @Override
    protected FileOptions.Builder getFileOptions() {
      return getParent().getFileOptions();
    }
  }


  /**
   * A special scope delegating to its parent scope. All fields and groups of the extend element
   * should be within the parent scope; however, at least a location info for extend might require
   * this to be in its own scope.
   * 
   * @param <BType>
   * @author protobufel@gmail.com David Tesler
   */
  protected class OneofScope extends Scope<OneofDescriptorProto.Builder> {
    private final int oneofIndex;

    private OneofScope(final OneofDescriptorProto.Builder protoBuilder,
        final Scope<DescriptorProto.Builder> parent) {
      super(protoBuilder, parent);
      oneofIndex = parent.getProtoBuilder().getOneofDeclCount() - 1;
    }

    @Override
    protected FieldDescriptorProto.Builder addField() {
      return getParent().addField().setOneofIndex(oneofIndex);
    }

    @Override
    protected DescriptorProto.Builder addGroup() {
      return getParent().addGroup();
    }

    @Override
    protected Builder addMessage() {
      return getParent().addMessage();
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return getParent().addCustomOption();
    }

    @Override
    protected MessageOptions.Builder getMessageOptions() {
      return getParent().getMessageOptions();
    }
  }

  protected class EnumScope extends Scope<EnumDescriptorProto.Builder> {

    private EnumScope(final EnumDescriptorProto.Builder protoBuilder, final Scope<?> parent) {
      super(protoBuilder, parent);
    }

    @Override
    protected EnumValueDescriptorProto.Builder addEnumValue() {
      return protoBuilder.addValueBuilder();
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return protoBuilder.getOptionsBuilder().addUninterpretedOptionBuilder();
    }

    @Override
    protected EnumOptions.Builder getEnumOptions() {
      return protoBuilder.getOptionsBuilder();
    }
  }

  protected class FieldScope extends Scope<FieldDescriptorProto.Builder> {

    private FieldScope(final FieldDescriptorProto.Builder protoBuilder, final Scope<?> parent) {
      super(protoBuilder, parent);
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return protoBuilder.getOptionsBuilder().addUninterpretedOptionBuilder();
    }

    @Override
    protected FieldOptions.Builder getFieldOptions() {
      return protoBuilder.getOptionsBuilder();
    }
  }

  protected class EnumValueScope extends Scope<EnumValueDescriptorProto.Builder> {

    private EnumValueScope(final EnumValueDescriptorProto.Builder protoBuilder,
        final Scope<?> parent) {
      super(protoBuilder, parent);
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return protoBuilder.getOptionsBuilder().addUninterpretedOptionBuilder();
    }

    @Override
    protected EnumValueOptions.Builder getEnumValueOptions() {
      return protoBuilder.getOptionsBuilder();
    }
  }

  protected class ServiceScope extends Scope<ServiceDescriptorProto.Builder> {

    private ServiceScope(final ServiceDescriptorProto.Builder protoBuilder, final Scope<?> parent) {
      super(protoBuilder, parent);
    }

    @Override
    protected MethodDescriptorProto.Builder addMethod() {
      return protoBuilder.addMethodBuilder();
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return protoBuilder.getOptionsBuilder().addUninterpretedOptionBuilder();
    }

    @Override
    protected ServiceOptions.Builder getServiceOptions() {
      return protoBuilder.getOptionsBuilder();
    }
  }

  protected class MethodScope extends Scope<MethodDescriptorProto.Builder> {

    private MethodScope(final MethodDescriptorProto.Builder protoBuilder, final Scope<?> parent) {
      super(protoBuilder, parent);
    }

    @Override
    protected UninterpretedOption.Builder addCustomOption() {
      return protoBuilder.getOptionsBuilder().addUninterpretedOptionBuilder();
    }

    @Override
    protected MethodOptions.Builder getMethodOptions() {
      return protoBuilder.getOptionsBuilder();
    }
  }

  private interface IntegralTypePlus<T extends IntegralTypePlus<? super T>> extends Comparable<T> {
    public T getCeiling();
  }

  private static final class IntegerPlus implements IntegralTypePlus<IntegerPlus> {
    private final int value;
    private final boolean isCeiling;

    private IntegerPlus(final int value, final boolean isCeiling) {
      this.value = value;
      this.isCeiling = isCeiling;
    }

    public static IntegerPlus valueOf(final int value) {
      return new IntegerPlus(value, false);
    }

    @Override
    public int compareTo(final IntegerPlus other) {
      return value == other.value ? Boolean.compare(isCeiling, other.isCeiling) : Integer.compare(
          value, other.value);
    }

    public int compareTo(final int value) {
      return this.value == value ? Boolean.compare(isCeiling, false) : Integer.compare(this.value,
          value);
    }

    @Override
    public IntegerPlus getCeiling() {
      return isCeiling ? this : new IntegerPlus(value, true);
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("IntegerPlus [value=").append(value).append(", isCeiling=").append(isCeiling)
          .append("]");
      return builder.toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (isCeiling ? 1231 : 1237);
      result = prime * result + value;
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof IntegerPlus)) {
        return false;
      }
      final IntegerPlus other = (IntegerPlus) obj;
      if (isCeiling != other.isCeiling) {
        return false;
      }
      if (value != other.value) {
        return false;
      }
      return true;
    }
  }

  private static final class IntegerRanges {
    private final NavigableSet<IntegerPlus> delegate;

    public IntegerRanges() {
      delegate = new TreeSet<IntegerPlus>();
    }

    public boolean addRange(final int start, final int end) {
      final IntegerPlus rangeStart = IntegerPlus.valueOf(start);
      final IntegerPlus rangeEnd =
          start == end ? rangeStart.getCeiling() : IntegerPlus.valueOf(end);

      if (!delegate.subSet(rangeStart, true, rangeEnd, true).isEmpty()) {
        return false;
      }

      delegate.add(rangeStart);
      delegate.add(rangeEnd);
      return true;
    }

    public boolean contains(final int value) {
      if (delegate.isEmpty() || delegate.first().compareTo(value) > 0
          || delegate.last().compareTo(value) < 0) {
        return false;
      }

      final NavigableSet<IntegerPlus> headSet = delegate.headSet(IntegerPlus.valueOf(value), true);
      return headSet.last().compareTo(value) == 0 || headSet.size() % 2 == 1;
    }
  }
}
