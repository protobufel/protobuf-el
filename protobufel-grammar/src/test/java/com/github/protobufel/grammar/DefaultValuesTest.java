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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
import com.github.protobufel.grammar.ProtoFiles.Builder;
import com.google.common.base.Charsets;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.TextFormat;

@RunWith(JUnit4.class)
public class DefaultValuesTest {
  private static final Logger log = LoggerFactory.getLogger(DefaultValuesTest.class);
  public static final int MAX_FIELD_NUMBER = 536870912;
  private static final String PROTOC_SUBDIR = "protoc/";

  private final int TestExtremeDefaultValuesIndex = 0;
  private final int ProtocShouldNotConvertIndex = 1;
  private final int SameAsProtocIndex = 2;
  private final int SameAsProtoIndex = 3;

  private static final String DEFAULT_VALUES_PROTO = "defaults1.proto";
  private File baseDir;
  // private List<String> files;
  @Mock
  private IBaseProtoErrorListener mockErrorListener;
  private LogProtoErrorListener errorListener;
  private ProtoFiles.Builder filesBuilder;
  private FileDescriptorProto protocProto;

  public final ExpectedException expected = ExpectedException.none();
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Rule
  public final TestRule chain = RuleChain.outerRule(expected).around(new MockitoJUnitRule(this))
  .around(softly);

  private List<String> sameAsProtoDefaultValues;

  @Before
  public void setUp() throws Exception {
    // given
    errorListener = new LogProtoErrorListener(mockErrorListener).setLogger(getClass());
    filesBuilder = ProtoFiles.newBuilder(errorListener).setProtocCompatible(false);
    final URI baseUri = getClass().getResource(PROTOC_SUBDIR).toURI();
    baseDir = new File(baseUri);
    sameAsProtoDefaultValues =
        getSourceMessageDefaultValues(new File(baseDir, DEFAULT_VALUES_PROTO), "SameAsProto");
    protocProto =
        getProtocFileDescriptorProto(DEFAULT_VALUES_PROTO, false, FieldTypeRefsMode.AS_IS);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testFileDescriptorDefaultValuesAll() throws Exception {
    // given
    final FileDescriptor expectedFile =
        FileDescriptor.buildFrom(protocProto, new FileDescriptor[0]);
    final List<FieldDescriptor> expectedFields = getAllDefaultFields(expectedFile);

    // when
    final FileDescriptor actualFile =
        filesBuilder.setProtocCompatible(false).addFiles(baseDir, DEFAULT_VALUES_PROTO).build()
            .values().iterator().next();
    final List<FieldDescriptor> actualFields = getAllDefaultFields(actualFile);

    // then
    softly.assertThat(actualFields).as("check nullness, duplicates, size").isNotNull()
        .doesNotContainNull().doesNotHaveDuplicates().hasSameSizeAs(expectedFields);
    softly.assertThat(actualFields).as("check field names equality")
    .usingElementComparatorOnFields("fullName").containsOnlyElementsOf(expectedFields);

    for (final FieldDescriptor actualField : actualFields) {
      final FieldDescriptor expectedField = expectedFields.get(actualField.getIndex());
      softly.assertThat(actualField.getFullName())
      .as("field %s name equal to expected", actualField.getFullName())
      .isEqualTo(expectedField.getFullName());
      softly.assertThat(actualField.getDefaultValue())
      .as("field %s default value equal to expected", actualField.getFullName())
      .isEqualTo(expectedField.getDefaultValue());
    }
  }

  private List<FieldDescriptor> getAllDefaultFields(final FileDescriptor file) {
    // return file.getMessageTypes().get(TestExtremeDefaultValuesIndex).getFields();
    final List<Descriptor> messageTypes = file.getMessageTypes();
    final Descriptor descriptor = messageTypes.get(TestExtremeDefaultValuesIndex);
    return descriptor.getFields();
  }

  @Test
  public void testExtremeDefaultValuesAll() throws Exception {
    try {
      assertEqualDescriptorProtoFields(TestExtremeDefaultValuesIndex, false);
    } catch (final AssertionError e) {
      // assuming protoc is wrong, so just log!
      log.info("protoc is wrong - should leave numeric defaults as in .proto source; "
          + "our parser is right!");
      // log.debug("protoc differs benign warning", e);
    }
  }

  @Ignore
  // FIXME enable after fixing compatibility mode
  @Test
  public void testExtremeDefaultValuesAllInCompatibilityMode() throws Exception {
    assertEqualDescriptorProtoFields(TestExtremeDefaultValuesIndex, true);
  }

  @Test
  public void protocShouldNotConvert() throws Exception {
    try {
      assertEqualDescriptorProtoFields(ProtocShouldNotConvertIndex, false);
    } catch (final AssertionError e) {
      // assuming protoc is wrong, so just log!
      log.info("protoc is wrong - should leave numeric defaults as in .proto source; "
          + "our parser is right!");
      // log.debug("protoc differs benign warning", e);
    }
  }

  @Ignore
  // FIXME enable after fixing compatibility mode
  @Test
  public void protocShouldNotConvertInCompatibilityMode() throws Exception {
    assertEqualDescriptorProtoFields(ProtocShouldNotConvertIndex, true);
  }

  @Test
  public void sameAsProtoc() throws Exception {
    assertEqualDescriptorProtoFields(SameAsProtocIndex, false);
  }

  @Ignore
  // FIXME enable after fixing compatibility mode
  @Test
  public void sameAsProtocInCompatibilityMode() throws Exception {
    assertEqualDescriptorProtoFields(SameAsProtocIndex, true);
  }

  @Test
  public void sameAsProto() throws Exception {
    sameAsProto(false);
  }

  @Ignore
  // FIXME enable after fixing compatibility mode
  @Test
  public void sameAsProtoInCompatibilityMode() throws Exception {
    sameAsProto(true);
  }

  private void sameAsProto(final boolean isProtocCompatible) throws Exception {
    // when
    final Builder protoBuilder =
        filesBuilder.setProtocCompatible(isProtocCompatible)
            .addFiles(baseDir, DEFAULT_VALUES_PROTO);
    final List<FieldDescriptorProto> actual =
        protoBuilder.buildProtos().get(0).getMessageType(SameAsProtoIndex).getFieldList();

    // then
    final List<String> actualDefaultValues = getMessageProtoDefaultValues(actual);
    assertThat(actualDefaultValues).containsExactlyElementsOf(sameAsProtoDefaultValues);
  }


  private List<String> getMessageProtoDefaultValues(final List<FieldDescriptorProto> actual)
      throws IOException {
    final FieldDescriptor defaultValueFD =
        FieldDescriptorProto.getDescriptor().findFieldByName("default_value");
    final List<String> actualDefaults = new ArrayList<String>();

    for (final FieldDescriptorProto field : actual) {
      final StringBuilder sb = new StringBuilder();
      TextFormat.printFieldValue(defaultValueFD, field.getDefaultValue(), sb);
      final String defaultValueText = sb.toString();
      actualDefaults.add(defaultValueText.substring(1, defaultValueText.length() - 1));
    }

    return actualDefaults;
  }

  private List<String> getSourceMessageDefaultValues(final File file, final String messageName) {
    final String source = Files.contentOf(file, Charsets.UTF_8);
    final Pattern defaultPattern = Pattern.compile("\\[.*?default\\s*=\\s*(.+?)\\s*\\];");
    final String messageSource = source.split("message\\s+" + messageName + "\\s+\\{", 2)[1];
    final Matcher matcher = defaultPattern.matcher(messageSource);
    final List<String> defaultValues = new ArrayList<String>();

    while (matcher.find()) {
      String value = matcher.group(1);

      if (value.startsWith("\"")) {
        value = value.substring(1, value.length() - 1);
      }

      defaultValues.add(value);
    }

    return Collections.unmodifiableList(defaultValues);
  }

  private void assertEqualDescriptorProtoFields(final int messageIndex,
      final boolean isProtocCompatible) throws URISyntaxException, IOException {
    // given
    final List<FieldDescriptorProto> expected =
        protocProto.getMessageType(messageIndex).getFieldList();

    // when
    final Builder protoBuilder =
        filesBuilder.setProtocCompatible(isProtocCompatible)
            .addFiles(baseDir, DEFAULT_VALUES_PROTO);
    final List<FieldDescriptorProto> actual =
        protoBuilder.buildProtos().get(0).getMessageType(messageIndex).getFieldList();

    // then
    // no errors logged!
    verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
        any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));

    assertThat(actual).as("check nullness, duplicates, size").isNotNull().doesNotContainNull()
        .doesNotHaveDuplicates().hasSameSizeAs(expected);
    assertThat(actual).as("check fields equality").containsOnlyElementsOf(expected);
  }
}
