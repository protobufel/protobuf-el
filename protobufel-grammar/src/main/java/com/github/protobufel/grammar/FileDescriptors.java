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
import java.util.Objects;

import com.github.protobufel.grammar.Exceptions.DescriptorValidationRuntimeException;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.ExtensionRegistry;

/**
 * Helps in building canonical FileDescriptors.
 *  
 * @author protobufel@gmail.com David Tesler
 */
//TODO make public when its functionality fully finalized and stable. 
final class FileDescriptors {
  private FileDescriptors() {}

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder of(final Builder builder) {
    return new Builder(builder);
  }

  final static class Builder {
    private boolean protocCompatible;
    private boolean customOptionsAsExtensions;
    private ExtensionRegistry registry;
    private FileDescriptor fileDescriptor;
    private FileDescriptorProto proto;
    private final List<FileDescriptor> dependencies;
    private boolean isBuilt;

    private Builder() {
      protocCompatible = false;
      customOptionsAsExtensions = true;
      registry = ExtensionRegistry.getEmptyRegistry();
      fileDescriptor = null;
      proto = null;
      dependencies = new ArrayList<FileDescriptor>();
      isBuilt = false;
    }

    private Builder(final boolean protocCompatible, final boolean customOptionsAsExtensions,
        final ExtensionRegistry registry, final FileDescriptor fileDescriptor,
        final FileDescriptorProto proto, final List<FileDescriptor> dependencies) {
      this.protocCompatible = protocCompatible;
      this.customOptionsAsExtensions = customOptionsAsExtensions;
      this.registry = Objects.requireNonNull(registry).getUnmodifiable();
      this.fileDescriptor = fileDescriptor;
      this.proto = proto;
      this.dependencies = new ArrayList<FileDescriptor>(Objects.requireNonNull(dependencies));
      isBuilt = false;
    }

    private Builder(final Builder other) {
      protocCompatible = other.protocCompatible;
      customOptionsAsExtensions = other.customOptionsAsExtensions;
      registry = other.registry;
      fileDescriptor = other.fileDescriptor;
      proto = other.proto;
      dependencies = new ArrayList<FileDescriptor>(other.dependencies);
      isBuilt = false;
    }

    protected boolean isBuilt() {
      return isBuilt;
    }

    public boolean isProtocCompatible() {
      return protocCompatible;
    }

    public Builder setProtocCompatible(final boolean protocCompatible) {
      if (this.protocCompatible != Objects.requireNonNull(protocCompatible)) {
        isBuilt = false;
        this.protocCompatible = protocCompatible;
      }

      return this;
    }

    public boolean isCustomOptionsAsExtensions() {
      return customOptionsAsExtensions;
    }

    public Builder setCustomOptionsAsExtensions(final boolean customOptionsAsExtensions) {
      if (this.customOptionsAsExtensions != Objects.requireNonNull(customOptionsAsExtensions)) {
        isBuilt = false;
        this.customOptionsAsExtensions = customOptionsAsExtensions;
      }

      return this;
    }

    public ExtensionRegistry getRegistry() {
      return registry;
    }

    public Builder clearRegistry() {
      return setRegistry(ExtensionRegistry.getEmptyRegistry());
    }

    public Builder setRegistry(final ExtensionRegistry registry) {
      if (this.registry != Objects.requireNonNull(registry)) {
        isBuilt = false;
        this.registry = registry;
      }

      return this;
    }

    public FileDescriptor getFileDescriptor() {
      return fileDescriptor;
    }

    public Builder setFileDescriptor(final FileDescriptor fileDescriptor) {
      if (this.fileDescriptor != Objects.requireNonNull(fileDescriptor)) {
        isBuilt = false;
        this.fileDescriptor = fileDescriptor;
        proto = null;
        dependencies.clear();
      }

      return this;
    }

    public FileDescriptorProto getProto() {
      return proto;
    }

    public Builder setProto(final FileDescriptorProtoOrBuilder proto) {
      if (this.proto != Objects.requireNonNull(proto)) {
        isBuilt = false;

        if (proto instanceof FileDescriptorProto.Builder) {
          this.proto = ((FileDescriptorProto.Builder) proto).build();
        } else {
          this.proto = (FileDescriptorProto) proto;
        }

        fileDescriptor = null;
      }

      return this;
    }

    public List<FileDescriptor> getDependencies() {
      return Collections.unmodifiableList(dependencies);
    }

    public Builder clearDependencies() {
      if (!dependencies.isEmpty()) {
        isBuilt = false;
        dependencies.clear();
      }

      return this;
    }

    public Builder addDependencies(final FileDescriptor... dependencies) {
      if (Objects.requireNonNull(dependencies).length == 0) {
        return this;
      }

      isBuilt = false;

      for (final FileDescriptor dependency : dependencies) {
        this.dependencies.add(Objects.requireNonNull(dependency));
      }

      return this;
    }

    public FileDescriptor rebuild() {
      isBuilt = false;
      return build();
    }

    public FileDescriptor build() {
      if (isBuilt) {
        return fileDescriptor;
      }

      if (fileDescriptor != null) {
        fileDescriptor = build(fileDescriptor);
      } else {
        Objects.requireNonNull(proto, "either proto or fileDescriptor must be set");
        fileDescriptor = build(proto, dependencies.toArray(new FileDescriptor[0]));
      }

      if (registry != ExtensionRegistry.getEmptyRegistry()) {
        FileDescriptor.internalUpdateFileDescriptor(fileDescriptor, registry);
      } else if (customOptionsAsExtensions) {
        final ExtensionRegistry registry = buildFullRegistryOf(fileDescriptor);
        FileDescriptor.internalUpdateFileDescriptor(fileDescriptor, registry);
      }

      isBuilt = true;
      return fileDescriptor;
    }

    private FileDescriptor build(final FileDescriptor fileDescriptor) {
      try {
        return FileDescriptor.buildFrom(makeCanonicalProto(fileDescriptor), fileDescriptor
            .getDependencies().toArray(new FileDescriptor[0]));
      } catch (final DescriptorValidationException e) {
        throw new DescriptorValidationRuntimeException(e);
      }
    }

    private FileDescriptor build(final FileDescriptorProto proto,
        final FileDescriptor[] dependencies) {
      try {
        final FileDescriptor fileDescriptor = FileDescriptor.buildFrom(proto, dependencies);
        return FileDescriptor.buildFrom(makeCanonicalProto(fileDescriptor), dependencies);
      } catch (final DescriptorValidationException e) {
        throw new DescriptorValidationRuntimeException(e);
      }
    }

    private FileDescriptorProto makeCanonicalProto(final FileDescriptor fileDescriptor) {
      final FileDescriptorProto.Builder protoBuilder =
          FileDescriptorProto.newBuilder(fileDescriptor.toProto());

      for (final FieldDescriptorProto.Builder field : protoBuilder.getExtensionBuilderList()) {
        makeCanonicalField(field, fileDescriptor.findExtensionByName(field.getName()));
      }

      for (final DescriptorProto.Builder message : protoBuilder.getMessageTypeBuilderList()) {
        makeCanonicalMessage(message, fileDescriptor.findMessageTypeByName(message.getName()));
      }

      // for (EnumDescriptorProto.Builder enumProto :
      // protoBuilder.getEnumTypeBuilderList()) {
      // makeCanonicalEnum(enumProto,
      // fileDescriptor.findEnumTypeByName(enumProto.getName()));
      // }

      for (final ServiceDescriptorProto.Builder serviceProto : protoBuilder.getServiceBuilderList()) {
        makeCanonicalService(serviceProto, fileDescriptor.findServiceByName(serviceProto.getName()));
      }

      // TODO: incorporate options' tree walking into canonicalization to eliminate double walking
      return OptionResolver.newBuilder().setCustomOptionsAsExtensions(false)
          .resolveAllOptionsFor(fileDescriptor, protoBuilder).build();
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
          try {
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
}
