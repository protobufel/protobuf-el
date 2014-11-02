package com.github.protobufel.grammar;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistry.ExtensionInfo;

/**
 * Utilities for ExtensionRegistry construction.
 * 
 * @author protobufel@gmail.com David Tesler
 */
public class ExtensionRegistries {
  private ExtensionRegistries() {}

  /**
   * Finds extension by its containing type and the dot separated multi-part name.
   */
  public static ExtensionInfo findExtensionByName(final ExtensionRegistry registry,
      final Descriptor containingType, final String name) {
    final int index = name.lastIndexOf(".");
    final String nameLastPart = index == -1 ? name : name.substring(index + 1);
    return registry.findImmutableExtensionByName(containingType.getFullName() + "." + nameLastPart);
  }

  /**
   * Builds a full unmodifiable, with dependencies, ExtensionRegistry for the given FileDescriptor.
   */
  public static ExtensionRegistry buildFullRegistryOf(final FileDescriptor file) {
    return addWithDependeciesToRegistry(file, ExtensionRegistry.newInstance()).getUnmodifiable();
  }

  /**
   * Adds all extensions, including dependencies, for the given FileDescriptor to the registry.
   */
  public static ExtensionRegistry addWithDependeciesToRegistry(final FileDescriptor file,
      final ExtensionRegistry registry) {
    for (final FileDescriptor dependency : getFileWithAllDependencies(file)) {
      addToRegistry(dependency, registry);
    }

    return registry;
  }

  private static Iterable<FileDescriptor> getFileWithAllDependencies(final FileDescriptor file) {
    final Set<FileDescriptor> cache = new LinkedHashSet<FileDescriptor>();

    cache.add(file);

    // we must follow the original ProtoBuf here!
    for (final FileDescriptor dependency : file.getDependencies()) {
      cache.add(dependency);
      addPublicDependencies(cache, dependency);
    }

    return cache;
  }

  private static void addPublicDependencies(final Set<FileDescriptor> cache,
      final FileDescriptor file) {
    for (final FileDescriptor dependency : file.getPublicDependencies()) {
      if (cache.add(dependency)) {
        addPublicDependencies(cache, dependency);
      }
    }
  }

  /**
   * Adds all extensions, without dependencies, for the given FileDescriptor to the registry.
   */
  public static ExtensionRegistry addToRegistry(final FileDescriptor file,
      final ExtensionRegistry registry) {
    for (final FieldDescriptor extension : file.getExtensions()) {
      addToRegistry(extension, registry);
    }

    for (final Descriptor descriptor : file.getMessageTypes()) {
      addToRegistry(descriptor, registry);
    }

    return registry;
  }

  /**
   * Adds all extensions for the given Descriptor to the registry.
   */
  public static ExtensionRegistry addToRegistry(final Descriptor descriptor,
      final ExtensionRegistry registry) {
    for (final FieldDescriptor extension : descriptor.getExtensions()) {
      addToRegistry(extension, registry);
    }

    for (final Descriptor child : descriptor.getNestedTypes()) {
      addToRegistry(child, registry);
    }

    return registry;
  }

  /**
   * Adds extension to the registry, making a default DynamicMessage if its type is a Message.
   */
  public static ExtensionRegistry addToRegistry(final FieldDescriptor extension,
      final ExtensionRegistry registry) {
    if (extension.getJavaType() == JavaType.MESSAGE) {
      registry.add(extension, DynamicMessage.getDefaultInstance(extension.getMessageType()));
    } else {
      registry.add(extension);
    }

    return registry;
  }
}
