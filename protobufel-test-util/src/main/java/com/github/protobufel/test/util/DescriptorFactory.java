//
// Copyright © 2014, David Tesler (https://github.com/protobufel)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the <organization> nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
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

package com.github.protobufel.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.UnknownFieldSet;

public class DescriptorFactory {
  private final FileDescriptorProto.Builder fileBuilder;

  private DescriptorFactory() {
    this(FileDescriptorProto.getDefaultInstance());
  }

  private DescriptorFactory(FileDescriptorProto prototype) {
    this.fileBuilder = FileDescriptorProto.newBuilder(prototype);
  }

  private Descriptor findDescriptor(final FileDescriptor fileDescriptor, 
      final String messageTypeName) {
    if ((messageTypeName == null) || (fileDescriptor == null)) {
      throw new NullPointerException();
    }
    
    for (Descriptor descriptor : fileDescriptor.getMessageTypes()) {
      if (messageTypeName.equals(descriptor.getFullName())) {
        return descriptor;
      }
    }
    
    for (Descriptor descriptor : fileDescriptor.getMessageTypes()) {
      findDescriptor(descriptor, messageTypeName);
    }
    
    return null;
  }

  private Descriptor findDescriptor(Descriptor descriptor, final String messageTypeName) {
    for (Descriptor child : descriptor.getNestedTypes()) {
      if (messageTypeName.equals(child.getFullName())) {
        return descriptor;
      }
    }
    
    for (Descriptor child : descriptor.getNestedTypes()) {
      final Descriptor foundDescriptor = findDescriptor(child, messageTypeName);
      
      if (foundDescriptor != null) {
        return foundDescriptor;
      }
    }
    
    return null;
  }
  
  private FileDescriptor createFileDescriptor(FileDescriptorProto fileProto, 
      FileDescriptor[] dependencies) throws DescriptorValidationException {
    return FileDescriptor.buildFrom(fileProto, dependencies);
  }

  private FileDescriptorProto createFileDescriptorProto(String fileName, String packageName, 
      UnknownFieldSet unknownFields) { 
    FileDescriptorProto.Builder fileBuilder = FileDescriptorProto.newBuilder();
    return fileBuilder
        .setName(fileName)
        .setPackage(packageName)
        .setUnknownFields(unknownFields)
        .addAllDependency(Collections.<String>emptyList())
        .addAllEnumType(Collections.<EnumDescriptorProto>emptyList())
        .addAllExtension(Collections.<FieldDescriptorProto>emptyList())
        .addAllMessageType(Collections.<DescriptorProto>emptyList())
        .addAllPublicDependency(Collections.<Integer>emptyList())
        .addAllService(Collections.<ServiceDescriptorProto>emptyList())
        .build();
  }
  
  private DescriptorProto createMessageDescriptorProto(String messageName, 
      UnknownFieldSet unknownFields) {
    DescriptorProto.Builder messageBuilder = DescriptorProto.newBuilder();
    return messageBuilder
        .setName(messageName)
        .setUnknownFields(unknownFields)
        .addAllEnumType(Collections.<EnumDescriptorProto>emptyList())
        .addAllExtension(Collections.<FieldDescriptorProto>emptyList())
        .addAllField(Collections.<FieldDescriptorProto>emptyList())
        .addAllNestedType(Collections.<DescriptorProto>emptyList())
        .build();
  }
  
  private FieldDescriptorProto createFieldDescriptorProto(String name, int index, Type type, 
      String typeName, Label label, String defaultValue, String extendee, 
      UnknownFieldSet unknownFields, FieldOptions options) {
    FieldDescriptorProto.Builder fieldBuilder = FieldDescriptorProto.newBuilder();
    return fieldBuilder
        .setName(name)
        .setNumber(index)
        .setType(type)
        .setTypeName(typeName)
        .setLabel(label)
        .setDefaultValue(defaultValue)
        .setExtendee(extendee)
        .setUnknownFields(unknownFields)
        .setOptions(options)
        .build(); 
  }
  
  public static final class FileDescriptorSetBuilder {
    private final Set<FileDescriptorProto> fileProtoSet;
    
    private FileDescriptorSetBuilder() {
      this(new LinkedHashSet<FileDescriptorProto>());
    }

    private FileDescriptorSetBuilder(LinkedHashSet<FileDescriptorProto> fileProtoSet) {
      this.fileProtoSet = fileProtoSet;
    }
    
    public static FileDescriptorSetBuilder newBuilder() {
      return new FileDescriptorSetBuilder();
    }
    
    public static FileDescriptorSetBuilder newBuilder(final FileDescriptorSet fileDescriptorSet) {
      final FileDescriptorSetBuilder fileDescriptorSetBuilder = new FileDescriptorSetBuilder();
      fileDescriptorSetBuilder.fileProtoSet.addAll(fileDescriptorSet.getFileList());
      return fileDescriptorSetBuilder;
    }
    
    public FileDescriptorSetBuilder from(final FileDescriptorSet fileDescriptorSet) {
      fileProtoSet.clear();
      fileProtoSet.addAll(fileDescriptorSet.getFileList());
      return this;
    }
    
    public FileDescriptorSetBuilder mergeFrom(final FileDescriptorSet fileDescriptorSet) {
      fileProtoSet.addAll(fileDescriptorSet.getFileList());
      return this;
    }
    
    public FileDescriptorSetBuilder addFile(final FileDescriptor file) {
      if (!fileProtoSet.add(file.toProto())) {
        return this;
      }
      
      for (FileDescriptor dependency : file.getDependencies()) {
        addFile(dependency);
      }
      
      return this;
    }
    
    public FileDescriptorSetBuilder addDescriptor(final Descriptor descriptor) {
      return addFile(descriptor.getFile());
    }
    
    public FileDescriptorSetBuilder addAllFiles(final Iterable<FileDescriptor> fileDescriptors) {
      for (FileDescriptor fileDescriptor : fileDescriptors) {
        addFile(fileDescriptor);
      }
      
      return this;
    }
    
    public FileDescriptorSetBuilder addAllDescriptors(final Iterable<Descriptor> descriptors) {
      for (Descriptor descriptor : descriptors) {
        addDescriptor(descriptor);
      }
      
      return this;
    }
    
    public FileDescriptorSet build() {
      return FileDescriptorSet.newBuilder().addAllFile(fileProtoSet).build();
    }
  }

  public static final class FileDescriptorCache {
    private final Map<String, Descriptor> descriptorCache;
    private final Map<String, FileDescriptor> fileCache;
    
    private FileDescriptorCache(final FileDescriptorSet fileDescriptorSet) {
      if (fileDescriptorSet == null) {
        throw new NullPointerException();
      }
      
      this.fileCache = new LinkedHashMap<String, FileDescriptor>(buildFilesFrom(fileDescriptorSet));
      this.descriptorCache = new HashMap<String, Descriptor>();
      updateDecsriptorCache();
    }
    
    public static FileDescriptorCache buildFrom(final FileDescriptorSet fileDescriptorSet) {
      return new FileDescriptorCache(fileDescriptorSet);
    }
    
    public Descriptor getDescriptor(String descriptorFullName) {
      if ((descriptorFullName == null) || descriptorFullName.isEmpty()) {
        throw new NullPointerException("descriptorFullName is null or empty");
      }
      
      return descriptorCache.get(descriptorFullName);
    }
    
    public FileDescriptor getFileDescriptor(String fileName) {
      if ((fileName == null) || fileName.isEmpty()) {
        throw new NullPointerException("fileName is null or empty");
      }
      
      return fileCache.get(fileName);
    }
    
    private void updateDecsriptorCache() {
      for (FileDescriptor file : fileCache.values()) {
        for (Descriptor descriptor : file.getMessageTypes()) {
          updateDecsriptorCache(descriptor);
        }
      }
    }
    
    private void updateDecsriptorCache(Descriptor descriptor) {
      descriptorCache.put(descriptor.getFullName(), descriptor);
      
      for (Descriptor child : descriptor.getNestedTypes()) {
        updateDecsriptorCache(child);
      }
    }
    
    private Map<String, FileDescriptor> buildFilesFrom(
        final FileDescriptorSet fileDescriptorSet) {
      final Map<String, FileDescriptor> fileMap = new LinkedHashMap<String, FileDescriptor>();
      final LinkedList<FileDescriptorProto> fileProtos = new LinkedList<FileDescriptorProto>();
      final FileDescriptor[] emptyDependencies = new FileDescriptor[0];
      
      try {
        //find all leaves
        for (FileDescriptorProto fileProto : fileDescriptorSet.getFileList()) {
          if (fileProto.getDependencyCount() == 0) {
            fileMap.put(fileProto.getName(), FileDescriptor.buildFrom(fileProto, emptyDependencies));
          } else {
            fileProtos.add(fileProto);
          }
        }
        
        while (!fileProtos.isEmpty()) {
          //find all fileProto(-s) with only dependencies from the files and make them into files
          boolean isLeafFound = false;
          
          for (Iterator<FileDescriptorProto> iterator = fileProtos.descendingIterator(); 
              iterator.hasNext();) {
            FileDescriptorProto fileProto = iterator.next();

            if (fileMap.keySet().containsAll(fileProto.getDependencyList())) {
              final FileDescriptor[] dependencies = 
                  new FileDescriptor[fileProto.getDependencyCount()];
              int i = 0;

              for (String fileName : fileProto.getDependencyList()) {
                dependencies[i++] = fileMap.get(fileName);
              }

              fileMap.put(fileProto.getName(), FileDescriptor.buildFrom(fileProto, dependencies));
              iterator.remove();
              isLeafFound = true;
            }
          }
          
          if (!isLeafFound) {
            List<String> nameList = new ArrayList<String>(fileProtos.size());
            
            for (FileDescriptorProto fileProto : fileProtos) {
              nameList.add(fileProto.getName());
            }
            
            throw new RuntimeException(String.format(
                "these FileDescriprorProtos are circular or refer to nonexisting files: %s", 
                nameList));
          }
        }
        
        return fileMap;
      } catch (DescriptorValidationException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
