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
import java.util.Arrays;
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

import difflib.DiffRow;
import difflib.DiffRow.Tag;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

@RunWith(JUnit4.class)
public class DiffWithOriginalsTest {
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DiffWithOriginalsTest.class);
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
  public void testDiffs2() throws URISyntaxException, IOException, PatchFailedException {
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

    final List<String> actualLines = toLines(sortedActualFds);
    final List<String> expectedLines = toLines(sortedExpectedFds);
    final Patch<String> patch = DiffUtils.diff(actualLines, expectedLines);
    // log.debug("patch text {}", patch.getDeltas());

    final DiffRowGenerator diffBuilder =
        new DiffRowGenerator.Builder().ignoreWhiteSpaces(false).ignoreBlankLines(false)
            .columnWidth(132).showInlineDiffs(false).build();
    final List<DiffRow> diffRows = diffBuilder.generateDiffRows(actualLines, expectedLines);
    final List<DiffRow> diffList = new ArrayList<>();

    for (final DiffRow diffRow : diffRows) {
      if (diffRow.getTag() != Tag.EQUAL) {
        diffList.add(diffRow);
      }
    }

    log.debug("diff rows {}", diffList);

    softly.assertThat(patch.applyTo(actualLines)).as("check the patched")
    .containsExactlyElementsOf(expectedLines);

    softly.assertThat(patch.restore(expectedLines)).as("check the reverse patched")
    .containsExactlyElementsOf(actualLines);

    // final StringBuilder sb = new StringBuilder();
    //
    // for (final Delta<FileDescriptorProto> delta : patch.getDeltas()) {
    // sb.append(delta);
    // }
    //
    // log.debug("patch text {}", sb.toString());
  }

  private <T> List<String> toLines(final List<T> list) {
    final List<String> lines = new ArrayList<String>();

    for (final T el : list) {
      lines.addAll(toLines(el.toString()));
    }

    return lines;
  }

  private List<String> toLines(final String text) {
    return Arrays.asList(text.split("\\r?\\n"));
  }
}
