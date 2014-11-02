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
import static com.github.protobufel.grammar.Misc.getFileDescriptorProtos;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.grammar.ErrorListeners.IBaseProtoErrorListener;
import com.github.protobufel.grammar.ErrorListeners.LogProtoErrorListener;
import com.github.protobufel.grammar.Misc.FieldTypeRefsMode;
import com.github.protobufel.grammar.Misc.FileDescriptorByNameComparator;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

@RunWith(JUnit4.class)
public class CompareWithOriginalsTest {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(CompareWithOriginalsTest.class);
  public static final int MAX_FIELD_NUMBER = 536870912;
  private static final String PROTOBUF_ORIGINAL_SUBDIR = "protobuf-original/";

  private static final Pattern ALL_PROTOS_PATTERN = Pattern.compile(".*\\.proto");
  private File baseDir;
  private final Pattern filePattern = ALL_PROTOS_PATTERN;
  // private List<String> files;
  @Mock
  private IBaseProtoErrorListener mockErrorListener;
  private LogProtoErrorListener errorListener;
  private ProtoFiles.Builder filesBuilder;
  private List<FileDescriptorProto> protocFdProtos;

  public final ExpectedException expected = ExpectedException.none();
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Rule
  public final TestRule chain = RuleChain.outerRule(expected).around(new MockitoJUnitRule(this))
  .around(softly);

  @SuppressWarnings("null")
  @Before
  public void setUp() throws Exception {
    // given
    errorListener = new LogProtoErrorListener(mockErrorListener).setLogger(getClass());
    filesBuilder = ProtoFiles.newBuilder(errorListener).setProtocCompatible(false);
    baseDir = new File(getClass().getResource(PROTOBUF_ORIGINAL_SUBDIR).toURI());
    protocFdProtos =
        getFileDescriptorProtos(filePattern, false, FieldTypeRefsMode.AS_IS,
            PROTOBUF_ORIGINAL_SUBDIR + "FileDescriptorSet", getClass());
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testRawReserializationProtoc() throws Exception {
    // given
    final List<FileDescriptorProto> expectedProtoList = protocFdProtos;

    for (final FileDescriptorProto expected : expectedProtoList) {
      // when
      final FileDescriptorProto actual = FileDescriptorProto.parseFrom(expected.toByteString());

      // then
      softly.assertThat(actual).as("equality").isEqualTo(expected);
      softly.assertThat(actual.toString()).as("textual equality").isEqualTo(expected.toString());
    }
  }

  @Test
  public void testRawReserialization() throws Exception {
    // given
    final List<FileDescriptorProto> expectedProtoList =
        filesBuilder.setCustomOptionsAsExtensions(false).setProtocCompatible(false)
        .addFilesByGlob(baseDir, "**/*.proto").buildProtos(false);

    for (final FileDescriptorProto expected : expectedProtoList) {
      // when
      final FileDescriptorProto actual = FileDescriptorProto.parseFrom(expected.toByteString());

      // then
      softly.assertThat(actual).as("equality").isEqualTo(expected);
      softly.assertThat(actual.toString()).as("textual equality").isEqualTo(expected.toString());
    }
  }

  @Test
  public void testCustomOptionsAsUnknown() throws URISyntaxException, IOException {
    // given
    final List<FileDescriptorProto> expectedProtoList = protocFdProtos;

    // when
    final List<FileDescriptorProto> actualProtoList =
        filesBuilder.setCustomOptionsAsExtensions(false).setProtocCompatible(false)
        .addFilesByGlob(baseDir, "**/*.proto").buildProtos(false);

    // then
    // no errors logged!
    verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
        any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));

    softly.assertThat(actualProtoList).as("check actualProtoList is not null").isNotNull();
    softly.assertThat(actualProtoList).as("check actualProtoList without nulls")
    .doesNotContainNull();
    softly.assertThat(actualProtoList).as("check actualProtoList has no duplicates")
    .extracting("package", "name").doesNotHaveDuplicates();
    softly.assertThat(actualProtoList).as("check actualProtoList size")
    .hasSameSizeAs(expectedProtoList);

    softly.assertThat(actualProtoList).as("check all protos' by name and package")
    .usingElementComparatorOnFields("package", "name")
    .containsOnlyElementsOf(expectedProtoList);

    final List<FileDescriptorProto> sortedActualFds =
        FileDescriptorByNameComparator.of().immutableSortedCopy(actualProtoList);
    final List<FileDescriptorProto> sortedExpectedFds =
        FileDescriptorByNameComparator.of().immutableSortedCopy(expectedProtoList);

    softly.assertThat(sortedActualFds).as("check all protos' by filtered content")
    .usingElementComparator(Misc.getDisregardUnsupportedProtoComparator())
    .containsExactlyElementsOf(sortedExpectedFds);
  }

  @Test
  public void customOptionsAsUnknownEqualsToFullReserialization() throws URISyntaxException,
  IOException {
    // given
    final List<FileDescriptorProto> protosWithExtensions =
        ProtoFiles.newBuilder().setCustomOptionsAsExtensions(true).setProtocCompatible(false)
        .addFilesByGlob(baseDir, "**/*.proto").buildProtos(false);

    final List<FileDescriptorProto> expectedProtoList =
        new ArrayList<FileDescriptorProto>(protosWithExtensions.size());

    for (final FileDescriptorProto proto : protosWithExtensions) {
      expectedProtoList.add(FileDescriptorProto.parseFrom(proto.toByteString()));
    }

    // when
    final List<FileDescriptorProto> actualProtoList =
        filesBuilder.setCustomOptionsAsExtensions(false).setProtocCompatible(false)
        .addFilesByGlob(baseDir, "**/*.proto").buildProtos(false);

    // then
    // no errors logged!
    verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
        any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));

    softly.assertThat(actualProtoList).as("check actualProtoList is not null").isNotNull();
    softly.assertThat(actualProtoList).as("check actualProtoList without nulls")
    .doesNotContainNull();
    softly.assertThat(actualProtoList).as("check actualProtoList has no duplicates")
    .extracting("package", "name").doesNotHaveDuplicates();
    softly.assertThat(actualProtoList).as("check actualProtoList size")
    .hasSameSizeAs(expectedProtoList);

    softly.assertThat(actualProtoList).as("check all protos' by name and package")
    .usingElementComparatorOnFields("package", "name")
    .containsOnlyElementsOf(expectedProtoList);

    final List<FileDescriptorProto> sortedActualFds =
        FileDescriptorByNameComparator.of().immutableSortedCopy(actualProtoList);
    final List<FileDescriptorProto> sortedExpectedFds =
        FileDescriptorByNameComparator.of().immutableSortedCopy(expectedProtoList);

    softly.assertThat(sortedActualFds).as("proto equlity unfiltered")
    .containsExactlyElementsOf(sortedExpectedFds);
  }

  @Test
  public void testCustomOptionsAsExtensions() throws URISyntaxException, IOException {
    // given
    final List<FileDescriptorProto> expectedProtoList =
        ProtoFiles.newBuilder(errorListener).setCustomOptionsAsExtensions(true)
        .addProtos(protocFdProtos).buildProtos(false);

    // when
    final List<FileDescriptorProto> actualProtoList =
        filesBuilder.setCustomOptionsAsExtensions(true).setProtocCompatible(false)
        .addFilesByGlob(baseDir, "**/*.proto").buildProtos(false);

    // then
    // no errors logged!
    verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
        any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));

    softly.assertThat(actualProtoList).as("check actualProtoList is not null").isNotNull();
    softly.assertThat(actualProtoList).as("check actualProtoList without nulls")
    .doesNotContainNull();
    softly.assertThat(actualProtoList).as("check actualProtoList has no duplicates")
    .extracting("package", "name").doesNotHaveDuplicates();
    softly.assertThat(actualProtoList).as("check actualProtoList size")
    .hasSameSizeAs(expectedProtoList);

    softly.assertThat(actualProtoList).as("check all protos' by name and package")
    .usingElementComparatorOnFields("package", "name")
    .containsOnlyElementsOf(expectedProtoList);

    final List<FileDescriptorProto> sortedActualFds =
        FileDescriptorByNameComparator.of().immutableSortedCopy(actualProtoList);
    final List<FileDescriptorProto> sortedExpectedFds =
        FileDescriptorByNameComparator.of().immutableSortedCopy(expectedProtoList);

    softly.assertThat(sortedActualFds).as("check all protos' by filtered content")
    .usingElementComparator(Misc.getDisregardUnsupportedProtoComparator())
    .containsExactlyElementsOf(sortedExpectedFds);
  }

  /**
   * protoc custom options' UnknownFieldSet serialization is different from Java; the resulted
   * extensions are the same, but UnknownFieldSets are different, thus the invariant has been
   * violated. When the serializations become identical, the test will start failing, indicating
   * everything is alright, so the expected exception will have to be safely removed!
   */
  @Test
  public void unknownToExtensionsToUnknownShouldBeEqualProtoc() throws URISyntaxException,
  IOException {
    // when
    final Collection<FileDescriptor> files =
        filesBuilder.setCustomOptionsAsExtensions(false).setProtocCompatible(false)
        .addProtos(protocFdProtos).build(false).values();

    // then
    // no errors logged!
    verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
        any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));

    expected.expect(Throwable.class);

    for (final FileDescriptor file : files) {
      assertReserializationInvariant(file);
    }
  }

  @Test
  public void unknownToExtensionsToUnknownShouldBeEqual() throws URISyntaxException, IOException {
    // when
    final Collection<FileDescriptor> files =
        filesBuilder.setCustomOptionsAsExtensions(false).setProtocCompatible(false)
        .addFilesByGlob(baseDir, "**/*.proto").build(false).values();

    // then
    // no errors logged!
    verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
        any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));

    for (final FileDescriptor file : files) {
      assertReserializationInvariant(file);
    }
  }

  private void assertReserializationInvariant(final FileDescriptor fileWithUnknownFieldsProto)
      throws InvalidProtocolBufferException {
    assertReserializationInvariant(fileWithUnknownFieldsProto,
        buildFullRegistryOf(fileWithUnknownFieldsProto));
  }

  private void assertReserializationInvariant(final FileDescriptor fileWithUnknownFieldsProto,
      final ExtensionRegistry registry) throws InvalidProtocolBufferException {
    final FileDescriptorProto expectedProtoWithUnknownFields = fileWithUnknownFieldsProto.toProto();

    final FileDescriptorProto actualProtoWithExtensions =
        FileDescriptorProto.parseFrom(expectedProtoWithUnknownFields.toByteString(), registry);
    final FileDescriptorProto actualProtoWithUnknownFields =
        FileDescriptorProto.parseFrom(actualProtoWithExtensions.toByteString());

    softly
    .assertThat(actualProtoWithUnknownFields.toString())
    .as("check reserialization invariant textual equality for %s",
        fileWithUnknownFieldsProto.getName())
        .isEqualTo(expectedProtoWithUnknownFields.toString());

    softly.assertThat(actualProtoWithUnknownFields)
    .as("check reserialization invariant for %s", fileWithUnknownFieldsProto.getName())
    .isEqualTo(expectedProtoWithUnknownFields);
  }

  @Test
  public void extensionsToUnknownToExtensionsShouldBeEqualProtoc() throws URISyntaxException,
  IOException {
    // when
    final Collection<FileDescriptor> files =
        filesBuilder.setCustomOptionsAsExtensions(true).setProtocCompatible(false)
        .addProtos(protocFdProtos).build(false).values();

    // then
    // no errors logged!
    verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
        any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));

    for (final FileDescriptor file : files) {
      assertReserializationInvariant2(file);
    }
  }

  @Test
  public void extensionsToUnknownToExtensionsShouldBeEqual() throws URISyntaxException, IOException {
    // when
    final Collection<FileDescriptor> files =
        filesBuilder.setCustomOptionsAsExtensions(true).setProtocCompatible(false)
        .addFilesByGlob(baseDir, "**/*.proto").build(false).values();

    // then
    // no errors logged!
    verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
        any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));

    for (final FileDescriptor file : files) {
      assertReserializationInvariant2(file);
    }
  }

  private void assertReserializationInvariant2(final FileDescriptor fileWithExtensionsProto)
      throws InvalidProtocolBufferException {
    assertReserializationInvariant2(fileWithExtensionsProto,
        buildFullRegistryOf(fileWithExtensionsProto));
  }

  private void assertReserializationInvariant2(final FileDescriptor fileWithExtensionsProto,
      final ExtensionRegistry registry) throws InvalidProtocolBufferException {
    final FileDescriptorProto expectedProtoWithExtensions = fileWithExtensionsProto.toProto();

    final FileDescriptorProto actualProtoWithUnknownFields =
        FileDescriptorProto.parseFrom(expectedProtoWithExtensions.toByteString());
    final FileDescriptorProto actualProtoWithExtensions =
        FileDescriptorProto.parseFrom(actualProtoWithUnknownFields.toByteString(), registry);

    softly.assertThat(actualProtoWithExtensions)
    .as("check reserialization invariant2 for %s", fileWithExtensionsProto.getName())
    .isEqualTo(expectedProtoWithExtensions);
  }
}
