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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.grammar.ProtoFiles.Builder;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.ExtensionRegistry;

public class Examples {
  private static final Logger log = LoggerFactory.getLogger(Examples.class);

  private Examples() {}

  public static List<FileDescriptorProto> getAllProtosFor(final String baseDir,
      final String globFiles, final Map<String, String> protoTexts,
      final List<FileDescriptorProto> existingProtos, final boolean customOptionsAsExtensions,
      final ExtensionRegistry customOptionsRegistry) {
    return newProtoFilesBuilder(baseDir, globFiles, protoTexts, existingProtos,
        customOptionsAsExtensions, customOptionsRegistry).buildProtos();
  }

  public static Collection<FileDescriptor> getAllFileDecsriptors(final String baseDir,
      final String globFiles, final Map<String, String> protoTexts,
      final List<FileDescriptorProto> existingProtos, final boolean customOptionsAsExtensions,
      final ExtensionRegistry customOptionsRegistry) {
    return newProtoFilesBuilder(baseDir, globFiles, protoTexts, existingProtos,
        customOptionsAsExtensions, customOptionsRegistry).build().values();
  }

  public static Map<String, FileDescriptor> getAllFileDecsriptorMap(final String baseDir,
      final String globFiles, final Map<String, String> protoTexts,
      final List<FileDescriptorProto> existingProtos, final boolean customOptionsAsExtensions,
      final ExtensionRegistry customOptionsRegistry) {
    return newProtoFilesBuilder(baseDir, globFiles, protoTexts, existingProtos,
        customOptionsAsExtensions, customOptionsRegistry).build();
  }

  public static Builder newProtoFilesBuilder(final String baseDir, final String globFiles,
      final Map<String, String> protoTexts, final List<FileDescriptorProto> existingProtos,
      final boolean customOptionsAsExtensions, final ExtensionRegistry customOptionsRegistry) {
    final Builder protoBuilder =
        ProtoFiles.newBuilder().setCustomOptionsAsExtensions(customOptionsAsExtensions)
            .setRegistry(customOptionsRegistry).addProtos(existingProtos)
            .addFilesByGlob(new File(baseDir), globFiles);

    for (final Entry<String, String> entry : protoTexts.entrySet()) {
      final String protoName = entry.getKey();
      final String protoText = entry.getValue();
      protoBuilder.addSource(protoName, protoText);
    }

    return protoBuilder;
  }
}
