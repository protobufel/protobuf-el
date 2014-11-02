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

import static com.github.protobufel.grammar.Misc.getProtocFileDescriptorProto;
import static com.github.protobufel.grammar.Misc.getProtocFileDescriptorProtos;
import static com.github.protobufel.grammar.Misc.FieldTypeRefsMode.RELATIVE_TO_PARENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.grammar.Misc.FieldTypeRefsMode;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto.Builder;
import com.google.protobuf.Descriptors.FileDescriptor;

// public class ProtoFilesTest extends MockitoBase {
@RunWith(MockitoJUnitRunner.class)
public class ImportPublicTest1 {
  private static final Logger log = LoggerFactory.getLogger(ImportPublicTest1.class);
  private static final String PROTOC_SUBDIR = "protoc/";
  private static final String MAIN_TEST_RESOURCES_DIR = "";
  private static final Pattern ALL_PROTOS_PATTERN = Pattern.compile(".+?\\.proto");
  private File baseDir;
  private File mainBasedir;
  private final Pattern filePattern = ALL_PROTOS_PATTERN;
  private List<FileDescriptorProto> protocFdProtos;

  @Before
  public void setUp() throws Exception {
    mainBasedir = new File(getClass().getResource(MAIN_TEST_RESOURCES_DIR).toURI());
    baseDir = new File(getClass().getResource(PROTOC_SUBDIR).toURI());
  }

  @Ignore
  @Test
  public void viewProtos() throws Exception {
    protocFdProtos =
        getProtocFileDescriptorProtos(Pattern.compile("nonTree.+?\\.proto"), false,
            FieldTypeRefsMode.AS_IS);

    for (final FileDescriptorProto proto : protocFdProtos) {
      log.info(proto.toString());
    }
  }

  // TODO fix and enable
  @Ignore
  @Test
  public void viewFileDescriptors() throws Exception {
    final FileDescriptorProto child1 =
        getProtocFileDescriptorProto("nonTreeChild1.proto", false, FieldTypeRefsMode.AS_IS);
    final FileDescriptorProto parent1 =
        getProtocFileDescriptorProto("nonTreeParent1.proto", false, FieldTypeRefsMode.AS_IS);
    final FileDescriptorProto parent2 =
        getProtocFileDescriptorProto("nonTreeParent2.proto", false, FieldTypeRefsMode.AS_IS);

    final FileDescriptor fileParent2 = FileDescriptor.buildFrom(parent2, new FileDescriptor[0]);
    final FileDescriptor fileChild1 =
        FileDescriptor.buildFrom(child1, new FileDescriptor[] {fileParent2});
    final FileDescriptor fileParent1 =
        FileDescriptor.buildFrom(parent1, new FileDescriptor[] {fileChild1});

    log.info(fileParent1.toProto().toString());
    log.info(fileChild1.toProto().toString());
    log.info(fileParent2.toProto().toString());

    log.info("messages: {}", fileParent1.getMessageTypes());
    log.info("messages: {}", fileParent1.getEnumTypes());

    assertThat(fileParent1.findMessageTypeByName("Child1"), nullValue());
    assertThat(fileParent1.findEnumTypeByName("Parent2"), not(nullValue()));
  }

  @Test
  public void testUnresolvedNamesPostBuildFileProto() throws Exception {
    final FileDescriptorProto child1 =
        getProtocFileDescriptorProto("nonTreeChild1.proto", false, FieldTypeRefsMode.AS_IS);
    final FileDescriptorProto child2 =
        getProtocFileDescriptorProto("nonTreeChild2.proto", false, FieldTypeRefsMode.AS_IS);
    final FileDescriptorProto originalParent1 =
        getProtocFileDescriptorProto("nonTreeParent1.proto", false, FieldTypeRefsMode.AS_IS);
    final FileDescriptorProto parent2 =
        getProtocFileDescriptorProto("nonTreeParent2.proto", false, FieldTypeRefsMode.AS_IS);

    final Builder parentBuilder = FileDescriptorProto.newBuilder(originalParent1);
    parentBuilder.getMessageTypeBuilder(0).getFieldBuilder(2).clearType().setTypeName("Child2");
    final FileDescriptorProto parent1 = parentBuilder.build();


    final FileDescriptor fileChild2 = FileDescriptor.buildFrom(child2, new FileDescriptor[0]);
    final FileDescriptor fileParent2 =
        FileDescriptor.buildFrom(parent2, new FileDescriptor[] {fileChild2});
    final FileDescriptor fileChild1 =
        FileDescriptor.buildFrom(child1, new FileDescriptor[] {fileParent2});
    final FileDescriptor fileParent1 =
        FileDescriptor.buildFrom(parent1, new FileDescriptor[] {fileChild1});

    log.info(fileParent1.toProto().toString());
  }

  @Test
  public void testUnresolvedNamesAllPostBuildFileProto() throws Exception {
    final FileDescriptorProto child1 =
        getProtocFileDescriptorProto("nonTreeChild1.proto", false, RELATIVE_TO_PARENT);
    final FileDescriptorProto child2 =
        getProtocFileDescriptorProto("nonTreeChild2.proto", false, RELATIVE_TO_PARENT);
    final FileDescriptorProto parent1 =
        getProtocFileDescriptorProto("nonTreeParent1.proto", false, RELATIVE_TO_PARENT);
    final FileDescriptorProto parent2 =
        getProtocFileDescriptorProto("nonTreeParent2.proto", false, RELATIVE_TO_PARENT);

    final FileDescriptor fileChild2 = FileDescriptor.buildFrom(child2, new FileDescriptor[0]);
    final FileDescriptor fileParent2 =
        FileDescriptor.buildFrom(parent2, new FileDescriptor[] {fileChild2});
    final FileDescriptor fileChild1 =
        FileDescriptor.buildFrom(child1, new FileDescriptor[] {fileParent2});
    final FileDescriptor fileParent1 =
        FileDescriptor.buildFrom(parent1, new FileDescriptor[] {fileChild1});

    log.info(fileParent1.toProto().toString());
  }
}
