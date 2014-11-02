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

import static com.github.protobufel.grammar.Misc.getProtocFileDescriptorProtos;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.stringContainsInOrder;
// import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.protobufel.grammar.ErrorListeners.IBaseProtoErrorListener;
import com.github.protobufel.grammar.ErrorListeners.LogProtoErrorListener;
import com.github.protobufel.grammar.Misc.FieldTypeRefsMode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
// import static org.mockito.Matchers.isNull;

// public class ProtoFilesTest extends MockitoBase {
// TODO redo all tests and enable
@Ignore
@RunWith(MockitoJUnitRunner.class)
@Category(IntegrationTests.class)
public class ProtoFilesIT {
  private static final Logger log = LoggerFactory.getLogger(ProtoFilesIT.class);
  private static final String PROTOC_SUBDIR = "protoc-errors//";
  private static final String MAIN_TEST_RESOURCES_DIR = "";

  private static final Pattern ALL_PROTOS_PATTERN = Pattern.compile(".+?\\.proto");
  private File baseDir;
  private File mainBasedir;
  private final Pattern filePattern = ALL_PROTOS_PATTERN;
  // private List<String> files;
  @Mock
  private IBaseProtoErrorListener mockErrorListener;
  private LogProtoErrorListener errorListener;
  private ProtoFiles.Builder filesBuilder;
  private List<FileDescriptorProto> protocFdProtos;

  @Before
  public void setUp() throws Exception {
    // given
    errorListener = new LogProtoErrorListener(mockErrorListener).setLogger(getClass());
    filesBuilder = ProtoFiles.newBuilder(errorListener);

    mainBasedir = new File(getClass().getResource(MAIN_TEST_RESOURCES_DIR).toURI());
    baseDir = new File(getClass().getResource(PROTOC_SUBDIR).toURI());

    // protoc FieldDescriptorSet - only to compare good protos against
    protocFdProtos = getProtocFileDescriptorProtos(filePattern, false, FieldTypeRefsMode.AS_IS);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testInvalidExtensionRange1() throws Exception {
    // given
    baseDir = new File(baseDir, "NonUniqueExtensionName1");
    final File errorsFile = new File(baseDir, "errors.txt");
    assertThat("cannot read file", errorsFile.canRead());
    final String expectedErrorText = Files.asCharSource(errorsFile, Charsets.UTF_8).read();

    // when
    final Map<String, FileDescriptor> result =
        filesBuilder.addFilesByRegex(baseDir, filePattern).build();

    // then
    final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    // TODO: add position capturing and asserts!
    verify(mockErrorListener, atLeastOnce()).validationError(anyInt(), anyInt(),
        argument.capture(), any(RuntimeException.class));
    verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
        anyInt(), anyString(), any(RecognitionException.class));
    // verify(mockErrorListener, atLeast(protocFdProtos.size())).setProtoName(anyString());
    final List<String> actualErrors = argument.getAllValues();

    assertThat(result, is(nullValue()));
    assertThat(expectedErrorText, stringContainsInOrder(actualErrors));
  }
}
