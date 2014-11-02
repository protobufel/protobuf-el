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

import static com.github.protobufel.grammar.ExtensionRegistries.buildFullRegistryOf;
import static com.github.protobufel.grammar.PrimitiveTypesUtil.getSimpleFieldValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.protobufel.grammar.Exceptions.DescriptorValidationRuntimeException;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.ExtensionRegistry;


/**
 * FileDescriptor/Proto canonicalization utility. To be removed/refactored when no longer needed.
 * 
 * @author protobufel@gmail.com David Tesler
 */
// FIXME most of it wrong and obsolete! isCanonical/deeeplyCanononical might be useful in the
// future.
// FIXME remove junk and move this into FileDescriptors!
final class FileDescriptorEx implements Comparable<FileDescriptorEx> {
  private static final FileDescriptorEx DESCRIPTOR_PROTO = new FileDescriptorEx();
  private static boolean protocCompatible = false;
  private final transient FileDescriptor delegate;
  private FileDescriptorProto proto;
  private transient volatile int hashCode = 0;
  private transient volatile int deeplyCanonical = -1;
  private final List<FileDescriptorEx> dependencies;
  private final boolean reparseCustomOptions;

  private FileDescriptorEx(final FileDescriptorEx other) {
    delegate = other.delegate;
    proto = other.proto;
    dependencies = other.dependencies; // share reference to entire list!
    deeplyCanonical = other.deeplyCanonical;
    hashCode = other.hashCode;
    reparseCustomOptions = other.reparseCustomOptions;
  }

  private FileDescriptorEx() {
    delegate = DescriptorProtos.getDescriptor();
    proto = delegate.toProto();
    dependencies = Collections.<FileDescriptorEx>emptyList();
    deeplyCanonical = 1;
    hashCode = delegate.hashCode();
    reparseCustomOptions = false;
  }

  private FileDescriptorEx(final FileDescriptor delegate, final boolean reparseCustomOptions) {
    if (delegate == null) {
      throw new NullPointerException();
    }

    if (delegate.getDependencies().isEmpty()) {
      dependencies = Collections.<FileDescriptorEx>emptyList();
    } else {
      dependencies = new ArrayList<FileDescriptorEx>(delegate.getDependencies().size());
    }

    this.delegate = delegate;
    this.reparseCustomOptions = reparseCustomOptions;
    proto = null;
  }

  private static FileDescriptorEx getDescriptorProto() {
    return DESCRIPTOR_PROTO;
  }

  private static FileDescriptorEx getInstance(final FileDescriptor delegate,
      final boolean reparseCustomOptions) {
    return delegate == DESCRIPTOR_PROTO.delegate ? DESCRIPTOR_PROTO : new FileDescriptorEx(
        delegate, reparseCustomOptions);
  }

  static boolean isProtocCompatible() {
    return protocCompatible;
  }

  static void setProtocCompatible(final boolean protocCompatible) {
    FileDescriptorEx.protocCompatible = protocCompatible;
  }

  private static FileDescriptorEx buildFrom(final FileDescriptorProto proto,
      final FileDescriptor[] dependencies, final boolean reparseCustomOptions) {
    try {
      return getInstance(FileDescriptor.buildFrom(proto, dependencies), reparseCustomOptions);
    } catch (final DescriptorValidationException e) {
      throw new DescriptorValidationRuntimeException(e);
    }
  }

  private static FileDescriptor buildCanonicalFileDescriptor(final FileDescriptor file,
      final boolean reparseCustomOptions) {
    return getInstance(file, reparseCustomOptions).getCanonicalFileDescriptor(true);
  }

  private static FileDescriptor buildCanonicalFileDescriptor(final FileDescriptorProto proto,
      final FileDescriptor[] dependencies, final boolean reparseCustomOptions) {
    return FileDescriptorEx.buildFrom(proto, dependencies, reparseCustomOptions)
        .getCanonicalFileDescriptor(true);
  }

  private static FileDescriptor buildBasicCanonicalFileDescriptor(final FileDescriptorProto proto,
      final FileDescriptor[] dependencies, final boolean reparseCustomOptions) {
    return FileDescriptorEx.buildFrom(proto, dependencies, reparseCustomOptions)
        .getBasicCanonicalFileDescriptor(true);
  }

  public String getName() {
    return delegate.getName();
  }

  public String getPackage() {
    return delegate.getPackage();
  }

  public FileOptions getOptions() {
    return delegate.getOptions();
  }

  public List<Descriptor> getMessageTypes() {
    return delegate.getMessageTypes();
  }

  public List<EnumDescriptor> getEnumTypes() {
    return delegate.getEnumTypes();
  }

  public List<ServiceDescriptor> getServices() {
    return delegate.getServices();
  }

  public List<FieldDescriptor> getExtensions() {
    return delegate.getExtensions();
  }

  public List<FileDescriptorEx> getDependencies() {
    if (dependencies != Collections.EMPTY_LIST && dependencies.isEmpty()) {
      synchronized (dependencies) {
        if (dependencies.isEmpty()) {
          for (final FileDescriptor dependency : delegate.getDependencies()) {
            dependencies.add(getInstance(dependency, reparseCustomOptions));
          }
        }
      }
    }

    return Collections.unmodifiableList(dependencies);
  }

  public List<FileDescriptorEx> getPublicDependencies() {
    final int publicCount = delegate.toProto().getPublicDependencyCount();

    if (publicCount == 0) {
      return Collections.emptyList();
    }

    final List<FileDescriptorEx> publicDependencies = new ArrayList<FileDescriptorEx>(publicCount);
    final List<FileDescriptorEx> dependencies = getDependencies();

    for (final int publicIndex : delegate.toProto().getPublicDependencyList()) {
      publicDependencies.add(dependencies.get(publicIndex));
    }

    return Collections.unmodifiableList(publicDependencies);
  }

  public Descriptor findMessageTypeByName(final String name) {
    return delegate.findMessageTypeByName(name);
  }

  public EnumDescriptor findEnumTypeByName(final String name) {
    return delegate.findEnumTypeByName(name);
  }

  public ServiceDescriptor findServiceByName(final String name) {
    return delegate.findServiceByName(name);
  }

  public FieldDescriptor findExtensionByName(final String name) {
    return delegate.findExtensionByName(name);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public int hashCode() {
    if (hashCode != 0) {
      return hashCode;
    }

    final int prime = 31;
    int result = 1;
    result = prime * result + delegate.hashCode();
    result = prime * result + toProto().hashCode();
    hashCode = result;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof FileDescriptorEx)) {
      return false;
    }

    final FileDescriptorEx other = (FileDescriptorEx) obj;
    return delegate == other.delegate || getName().equals(other.getName())
        && toProto().equals(other.toProto());
  }

  @Override
  public int compareTo(final FileDescriptorEx other) {
    if (this == other || delegate == other.delegate) {
      return 0;
    }

    // this is just an optimization, as TextFormat will output the name as the
    // first variable
    final int compareTo = delegate.getName().compareTo(other.delegate.getName());

    if (compareTo != 0) {
      return compareTo;
    }

    return toProto().toString().compareTo(other.toProto().toString());
  }

  public FileDescriptor getFileDescriptor() {
    return delegate;
  }

  public FileDescriptor getBasicCanonicalFileDescriptor(final boolean forceRebuild) {
    if (!forceRebuild && isCanonical()) {
      return delegate;
    }

    try {
      return FileDescriptor.buildFrom(toCanonicalProto(),
          delegate.getDependencies().toArray(new FileDescriptor[0]));
    } catch (final DescriptorValidationException e) {
      throw new DescriptorValidationRuntimeException(e);
    }
  }

  public FileDescriptor getCanonicalFileDescriptor(final boolean forceRebuild) {
    if (!forceRebuild && isCanonical()) {
      return delegate;
    }

    return buildFileDescriptorWithReserializedProto(toCanonicalProto(), delegate.getDependencies()
        .toArray(new FileDescriptor[0]));
  }

  public FileDescriptor getDeepCanonicalFileDescriptor(final boolean forceRebuild) {
    if (deeplyCanonical == 1 || !forceRebuild && isDeeplyCanonical()) {
      return delegate;
    }

    final List<FileDescriptorEx> dependenciesEx = getDependencies();
    final FileDescriptor[] dependencies = new FileDescriptor[dependenciesEx.size()];
    int i = 0;

    for (final FileDescriptorEx dependency : dependenciesEx) {
      dependencies[i++] = dependency.getDeepCanonicalFileDescriptor(forceRebuild);
    }

    return buildFileDescriptorWithReserializedProto(toProto(), dependencies);
  }

  private FileDescriptor getDeepCanonicalFileDescriptor(final FileDescriptor file,
      final boolean forceRebuild) throws DescriptorValidationException {
    if (!forceRebuild && isDeeplyCanonical(file)) {
      return file;
    }

    final FileDescriptor[] dependencies = new FileDescriptor[file.getDependencies().size()];
    int i = 0;

    for (final FileDescriptor dependency : file.getDependencies()) {
      dependencies[i++] = getDeepCanonicalFileDescriptor(dependency, forceRebuild);
    }

    final FileDescriptorProto proto = isCanonical(file) ? file.toProto() : makeCanonicalProto(file);
    return buildFileDescriptorWithReserializedProto(proto, dependencies);
  }

  private FileDescriptor buildFileDescriptorWithReserializedProto(final FileDescriptorProto proto,
      final FileDescriptor[] dependencies) {
    try {
      final FileDescriptor fileDescriptor = FileDescriptor.buildFrom(proto, dependencies);
      final ExtensionRegistry registry = buildFullRegistryOf(fileDescriptor);
      FileDescriptor.internalUpdateFileDescriptor(fileDescriptor, registry);
      return fileDescriptor;
    } catch (final DescriptorValidationException e) {
      throw new DescriptorValidationRuntimeException(e);
    }
  }

  public boolean isDeeplyCanonical() {
    if (deeplyCanonical == -1) {
      if (!isCanonical()) {
        deeplyCanonical = 0;
        return false;
      }

      for (final FileDescriptorEx dependency : getDependencies()) {
        if (!dependency.isDeeplyCanonical()) {
          deeplyCanonical = 0;
          return false;
        }
      }

      deeplyCanonical = 1;
      return true;
    }

    return deeplyCanonical == 0 ? false : true;
  }

  boolean isDeeplyCanonicalNoDependenciesBuilt() {
    if (deeplyCanonical == -1) {
      if (dependencies == Collections.EMPTY_LIST) {
        deeplyCanonical = isCanonical() ? 1 : 0;
      } else if (!dependencies.isEmpty()) {
        if (!isCanonical()) {
          deeplyCanonical = 0;
          return false;
        }

        for (final FileDescriptorEx dependency : dependencies) {
          if (!dependency.isDeeplyCanonicalNoDependenciesBuilt()) {
            deeplyCanonical = 0;
            return false;
          }
        }

        deeplyCanonical = 1;
        return true;
      } else { // either build and query dependencies, or query FileDescriptors
        // directly
        // we query FileDescriptors directly here
        deeplyCanonical = isDeeplyCanonical(delegate) ? 1 : 0;
      }
    }

    return deeplyCanonical == 0 ? false : true;
  }

  private boolean isDeeplyCanonical(final FileDescriptor file) {
    if (!isCanonical(file)) {
      return false;
    }

    for (final FileDescriptor dependency : file.getDependencies()) {
      if (!isDeeplyCanonical(dependency)) {
        return false;
      }
    }

    return true;
  }

  public boolean isCanonical() {
    return toProto() == delegate.toProto();
  }

  public FileDescriptorProto toProto() {
    if (proto == null) {
      synchronized (delegate) {
        if (proto == null) {
          if (!protocCompatible && isCanonical(delegate)) {
            proto = delegate.toProto();
          } else {
            proto = makeCanonicalProto(delegate);
          }
        }
      }
    }

    return proto;
  }

  public FileDescriptorProto toCanonicalProto() {
    if (proto == null) {
      synchronized (delegate) {
        if (proto == null) {
          proto = makeCanonicalProto(delegate);
        }
      }
    }

    return proto;
  }

  // TODO: should do defaultValue.equals(convertToCanonical(defaultValue)) check;
  // very consuming and problematic!
  private boolean isCanonical(final FileDescriptor file) {
    final FileDescriptorProto proto = file.toProto();

    if (proto.hasOptions() && proto.getOptions().getUninterpretedOptionCount() > 0) {
      return false;
    }

    for (final FieldDescriptorProto field : proto.getExtensionList()) {
      if (!isFieldCanonical(field)) {
        return false;
      }
    }

    for (final ServiceDescriptorProto serviceProto : proto.getServiceList()) {
      if (!isCanonical(serviceProto)) {
        return false;
      }
    }

    for (final EnumDescriptorProto enumProto : proto.getEnumTypeList()) {
      if (!isCanonical(enumProto)) {
        return false;
      }
    }

    for (final DescriptorProto message : proto.getMessageTypeList()) {
      if (!isMessageRefsCanonical(message)) {
        return false;
      }
    }

    return true;
  }

  private boolean isCanonical(final ServiceDescriptorProto serviceProto) {
    if (serviceProto.hasOptions() && serviceProto.getOptions().getUninterpretedOptionCount() > 0) {
      return false;
    }

    for (final MethodDescriptorProto methodProto : serviceProto.getMethodList()) {
      if (methodProto.hasOptions() && methodProto.getOptions().getUninterpretedOptionCount() > 0) {
        return false;
      }
    }

    return true;
  }

  private boolean isMessageRefsCanonical(final DescriptorProto message) {
    if (message.hasOptions() && message.getOptions().getUninterpretedOptionCount() > 0) {
      return false;
    }

    for (final FieldDescriptorProto field : message.getExtensionList()) {
      if (!isFieldCanonical(field)) {
        return false;
      }
    }

    for (final FieldDescriptorProto field : message.getFieldList()) {
      if (!isFieldCanonical(field)) {
        return false;
      }
    }

    for (final EnumDescriptorProto enumProto : message.getEnumTypeList()) {
      if (!isCanonical(enumProto)) {
        return false;
      }
    }

    for (final DescriptorProto child : message.getNestedTypeList()) {
      if (!isMessageRefsCanonical(child)) {
        return false;
      }
    }

    return true;
  }

  private boolean isCanonical(final EnumDescriptorProto enumProto) {
    if (enumProto.hasOptions() && enumProto.getOptions().getUninterpretedOptionCount() > 0) {
      return false;
    }

    for (final EnumValueDescriptorProto enumValue : enumProto.getValueList()) {
      if (enumValue.hasOptions() && enumValue.getOptions().getUninterpretedOptionCount() > 0) {
        return false;
      }
    }

    return true;
  }

  private boolean isFieldCanonical(final FieldDescriptorProto field) {
    return (!field.hasExtendee() || field.getExtendee().startsWith("."))
        && (!field.hasTypeName() || field.getTypeName().startsWith(".") && field.hasType())
        && !(field.hasOptions() && field.getOptions().getUninterpretedOptionCount() > 0);
  }

  private FileDescriptorProto makeCanonicalProto(final FileDescriptor file) {
    final FileDescriptorProto.Builder protoBuilder = FileDescriptorProto.newBuilder(file.toProto());

    for (final FieldDescriptorProto.Builder field : protoBuilder.getExtensionBuilderList()) {
      makeCanonicalField(field, file.findExtensionByName(field.getName()));
    }

    for (final DescriptorProto.Builder message : protoBuilder.getMessageTypeBuilderList()) {
      makeCanonicalMessage(message, file.findMessageTypeByName(message.getName()));
    }

    // for (EnumDescriptorProto.Builder enumProto :
    // protoBuilder.getEnumTypeBuilderList()) {
    // makeCanonicalEnum(enumProto,
    // file.findEnumTypeByName(enumProto.getName()));
    // }

    for (final ServiceDescriptorProto.Builder serviceProto : protoBuilder.getServiceBuilderList()) {
      makeCanonicalService(serviceProto, file.findServiceByName(serviceProto.getName()));
    }

    return OptionResolver.newBuilder().setCustomOptionsAsExtensions(reparseCustomOptions)
        .resolveAllOptionsFor(file, protoBuilder).build();
  }

  private void makeCanonicalService(final ServiceDescriptorProto.Builder service,
      final ServiceDescriptor serviceDescriptor) {
    for (final MethodDescriptorProto.Builder method : service.getMethodBuilderList()) {
      final MethodDescriptor methodDescriptor =
          serviceDescriptor.findMethodByName(method.getName());
      method.setInputType(ensureLeadingDot(methodDescriptor.getInputType().getFullName()));
      method.setOutputType(ensureLeadingDot(methodDescriptor.getOutputType().getFullName()));
    }
  }

  private void makeCanonicalMessage(final DescriptorProto.Builder message,
      final Descriptor messageDescriptor) {
    final List<FieldDescriptor> extensionsList = messageDescriptor.getExtensions();

    if (extensionsList.size() != message.getExtensionBuilderList().size()) {
      throw new IllegalStateException(String.format(
          "Message %s has a size of extensions differ from its Descriptor!", message.getName()));
    }

    if (!extensionsList.isEmpty()) {
      final Map<Integer, FieldDescriptor> extensionMap = new HashMap<Integer, FieldDescriptor>();

      for (final FieldDescriptor field : extensionsList) {
        extensionMap.put(field.getNumber(), field);
      }

      for (final FieldDescriptorProto.Builder field : message.getExtensionBuilderList()) {
        final FieldDescriptor extensionField = extensionMap.get(field.getNumber());
        makeCanonicalField(field, extensionField);
      }
    }

    // for (FieldDescriptorProto.Builder field :
    // message.getExtensionBuilderList()) {
    // makeCanonicalField(field,
    // messageDescriptor.findFieldByNumber(field.getNumber()));
    // }

    for (final FieldDescriptorProto.Builder field : message.getFieldBuilderList()) {
      makeCanonicalField(field, messageDescriptor.findFieldByNumber(field.getNumber()));
    }

    for (final DescriptorProto.Builder child : message.getNestedTypeBuilderList()) {
      makeCanonicalMessage(child, messageDescriptor.findNestedTypeByName(child.getName()));
    }
  }

  private void makeCanonicalField(final FieldDescriptorProto.Builder field,
      final FieldDescriptor fieldDescriptor) {
    if (field.hasExtendee() && !field.getExtendee().startsWith(".")) {
      field.setExtendee(ensureLeadingDot(fieldDescriptor.getContainingType().getFullName()));
    }

    if (field.hasTypeName() && !field.getTypeName().startsWith(".")) {
      if (fieldDescriptor.getJavaType() == JavaType.ENUM) {
        field.setTypeName(ensureLeadingDot(fieldDescriptor.getEnumType().getFullName()));
        field.setType(Type.TYPE_ENUM);
      } else {
        // this must be a Message/Group field
        field.setTypeName(ensureLeadingDot(fieldDescriptor.getMessageType().getFullName()));

        if (!field.hasType()) {
          switch (fieldDescriptor.getType()) {
            case MESSAGE:
              field.setType(Type.TYPE_MESSAGE);
              break;
            case GROUP:
              field.setType(Type.TYPE_GROUP);
              break;
            default:
              field.setType(Type.valueOf("TYPE_" + fieldDescriptor.getType().name()));
          }
        }
      }
    }

    // reparsing of the default value if present - protoc does this contrary to the spec!
    if (protocCompatible) {
      if (field.hasDefaultValue()) {
        final StringBuilder sb = new StringBuilder();

        try {
          // TextFormat.printFieldValue(fieldDescriptor, fieldDescriptor.getDefaultValue(), sb);
          // field.setDefaultValue(sb.toString());
          final String defaultValue =
              getSimpleFieldValue(fieldDescriptor, fieldDescriptor.getDefaultValue());
          field.setDefaultValue(defaultValue);
        } catch (final IOException e) {
          // this should not happen!
          throw new RuntimeException(e);
        }
      }
    }
  }

  private String ensureLeadingDot(final String name) {
    return name.startsWith(".") ? name : "." + name;
  }
}
