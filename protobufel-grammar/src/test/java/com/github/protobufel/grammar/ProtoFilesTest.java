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

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
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
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

@Ignore
@RunWith(JUnit4.class)
public class ProtoFilesTest {
  private static final Logger log = LoggerFactory.getLogger(ProtoFilesTest.class);
  private static final String PROTOC_SUBDIR = "protoc/";
  private static final String MAIN_TEST_RESOURCES_DIR = "";

  // private static final Pattern ALL_PROTOS_PATTERN = Pattern.compile(".+?\\.proto"); wrong by
  // allowing descriptor.proto!
  private static final Pattern ALL_PROTOS_PATTERN = Pattern.compile("[^/]+?\\.proto");
  private File baseDir;
  private File mainBasedir;
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

  // //TODO there is small difference in Options, which is likely valid; investigate and enable a
  // test
  // @Ignore
  // @Test
  // public void testNewBuilderWithGoodFiles() throws Exception {
  // final Set<FileDescriptorProto> expected = ProtoFiles.buildCanonicalProtosFrom(
  // protocFdProtos, mockErrorListener);
  // //when
  // final Builder protoBuilder = filesBuilder.addFilesByRegex(baseDir, filePattern);
  // // final Builder protoBuilder = filesBuilder.addFiles(baseDir,
  // // "import1.proto", "import2.proto", "publicImport1.proto", "simple1.proto");
  // FileDescriptorSetInfo info = protoBuilder.build();
  // FileDescriptorSet fds = info.getFileDescriptorSet();
  //
  // //log.debug("TextFormat of protocFdProtos {}", protocFdProtos.get(0).toString());
  //
  // //then
  // // no errors logged!
  // verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
  // any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  // //verify(mockErrorListener, atLeast(protocFdProtos.size())).setProtoName(anyString());
  //
  // //FIXME implement integrated second conversion for expected as in
  // testCustomOptionsWithResolver1
  //
  // final Set<FileDescriptorProto> actual = new HashSet<>(fds.getFileList());
  //
  // ImmutableSet<String> actualStrings = FluentIterable.from(actual)
  // .transform(new Function<FileDescriptorProto, String>() {
  // @Override
  // public String apply(FileDescriptorProto input) {
  // return input.toString();
  // }
  // })
  // .toSet();
  //
  // ImmutableSet<String> expectedStrings = FluentIterable.from(expected)
  // .transform(new Function<FileDescriptorProto, String>() {
  // @Override
  // public String apply(FileDescriptorProto input) {
  // return input.toString();
  // }
  // })
  // .toSet();
  //
  // //assertThat(actualStrings, equalTo(expectedStrings));
  //
  // assertThat(FileDescriptorByNameComparator.of().immutableSortedCopy(fds.getFileList()),
  // equalTo(FileDescriptorByNameComparator.of().immutableSortedCopy(protocFdProtos)));
  //
  // // assertThat(new HashSet<FileDescriptorProto>(fds.getFileList()),
  // // equalTo(new HashSet<FileDescriptorProto>(protocFdProtos)));
  // }
  //
  // //FIXME remove this test - no longer needed after OptionResolver integration!!! :)
  // @Ignore
  // @Test
  // public void testCustomOptions1() throws Exception {
  // //when
  // final LinkedHashMap<String, FileDescriptor> cache =
  // new LinkedHashMap<String, FileDescriptor>();
  //
  // final FileDescriptorSet fds = filesBuilder.addFiles(baseDir, "CustomOptions1.proto")
  // .buildNew(cache);
  //
  // //then
  // // no errors logged!
  // verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
  // any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  //
  // FileDescriptorProto expectedProto = getProtocFileDescriptorProto("CustomOptions1.proto", false,
  // FieldTypeRefsMode.AS_IS);
  //
  // assertThat(fds.getFileCount(), equalTo(1));
  //
  // FileDescriptorProto actualProto = fds.getFile(0);
  // assertThat(actualProto, sameInstance(cache.get("CustomOptions1.proto").toProto()));
  // assertThat(actualProto, equalTo(expectedProto));
  // }
  //
  // @Test
  // public void testCustomOptionsWithOptionsResolver1() throws Exception {
  // //when
  // // final LinkedHashMap<String, FileDescriptor> cache =
  // // new LinkedHashMap<String, FileDescriptor>();
  //
  // final String protoName = "CustomOptions1.proto";
  // FileDescriptorSetInfo resultInfo = filesBuilder.addFiles(baseDir, protoName)
  // //.buildNew(cache);
  // .build();
  // final FileDescriptorSet fds = resultInfo.getFileDescriptorSet();
  // final FileDescriptorEx fileEx = resultInfo.getFileDescriptorCache().get(protoName);
  // //final FileDescriptor file = fileEx.getDeepCanonicalFileDescriptor(true);
  //
  // //then
  // // no errors logged!
  // verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
  // any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  //
  // FileDescriptorProto originalExpectedProto = getProtocFileDescriptorProto(protoName,
  // false, FieldTypeRefsMode.AS_IS);
  // FileDescriptor expectedFile = FileDescriptorEx.buildCanonicalFileDescriptor(
  // originalExpectedProto, new FileDescriptor[] {DescriptorProtos.getDescriptor()});
  // FileDescriptorProto expectedProto = expectedFile.toProto();
  //
  // assertThat(fds.getFileCount(), equalTo(1));
  // final FileDescriptorProto actualProto = fds.getFile(0);
  //
  // // protoc requires(!) and combines a custom descriptor.proto, so all options, and the rest,
  // // refer to their custom descriptor.proto as their descriptors!!! So messages won't be equal
  // // here and there, and extensions will be failing due to the
  // // field.containingType() != message.getDescriptor()!
  // // we are preventing this, as anything in google.protobuf package, including descriptor.proto,
  // // is reserved, and automatically added by the ProtoFiles for building! A very nasty problem!
  // // In addition, two different protos will have different, not equal custom options/extensions
  // of
  // // Message type, because these types will have different descriptors, always!
  // assertThat(actualProto.getUnknownFields(), equalTo(expectedProto.getUnknownFields()));
  // assertThat(actualProto.getOptions(), not(equalTo(expectedProto.getOptions()))); //
  // assertThat(actualProto, not(equalTo(expectedProto))); //
  // assertThat(actualProto.toString(), equalTo(expectedProto.toString()));
  // }
  //
  //
  // @Test
  // public void testEnumMessageFieldsDescriptorWithProtocWithOptionsResolver1() throws Exception {
  // //when
  // // final LinkedHashMap<String, FileDescriptor> cache =
  // // new LinkedHashMap<String, FileDescriptor>();
  //
  // final String protoName = "EnumAndMessageFields1.proto";
  // FileDescriptorSetInfo resultInfo = filesBuilder.addFiles(baseDir, protoName)
  // //.buildNew(cache);
  // .build();
  // final FileDescriptorSet fds = resultInfo.getFileDescriptorSet();
  // final FileDescriptorEx fileEx = resultInfo.getFileDescriptorCache().get(protoName);
  // //final FileDescriptor file = fileEx.getDeepCanonicalFileDescriptor(true);
  //
  // //then
  // // no errors logged!
  // verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
  // any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  //
  // FileDescriptorProto originalExpectedProto = getProtocFileDescriptorProto(protoName,
  // false, FieldTypeRefsMode.AS_IS);
  // FileDescriptor expectedFile = FileDescriptorEx.buildCanonicalFileDescriptor(
  // originalExpectedProto, new FileDescriptor[0]);
  // FileDescriptorProto expectedProto = expectedFile.toProto();
  //
  // assertThat(fds.getFileCount(), equalTo(1));
  // final FileDescriptorProto actualProto = fds.getFile(0);
  //
  // // protoc requires(!) and combines a custom descriptor.proto, so all options, and the rest,
  // // refer to their custom descriptor.proto as their descriptors!!! So messages won't be equal
  // // here and there, and extensions will be failing due to the
  // // field.containingType() != message.getDescriptor()!
  // // we are preventing this, as anything in google.protobuf package, including descriptor.proto,
  // // is reserved, and automatically added by the ProtoFiles for building! A very nasty problem!
  // // In addition, two different protos will have different, not equal custom options/extensions
  // of
  // // Message type, because these types will have different descriptors, always!
  // assertThat(actualProto.getUnknownFields(), equalTo(expectedProto.getUnknownFields()));
  // //assertThat(actualProto.getOptions(), not(equalTo(expectedProto.getOptions()))); //
  // assertThat(actualProto, equalTo(expectedProto)); //
  // assertThat(actualProto.toString(), equalTo(expectedProto.toString()));
  // }
  //
  //
  // //FIXME remove along with FileProtos.buildNew(), or fix it!!!
  // @Ignore
  // @Test
  // public void testCustomOptionsWithOptionsResolver2() throws Exception {
  // //when
  // final LinkedHashMap<String, FileDescriptor> cache =
  // new LinkedHashMap<String, FileDescriptor>();
  //
  // FileDescriptorSet fds = filesBuilder.addFiles(baseDir, "CustomOptions1.proto")
  // .buildNew(cache);
  // //.build();
  // //final FileDescriptor file = fileEx.getDeepCanonicalFileDescriptor(true);
  //
  // //then
  // // no errors logged!
  // verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
  // any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  //
  // FileDescriptorProto originalExpectedProto =
  // getProtocFileDescriptorProto("CustomOptions1.proto",
  // false, FieldTypeRefsMode.AS_IS);
  // FileDescriptor expectedFile = FileDescriptorEx.buildCanonicalFileDescriptor(
  // originalExpectedProto, new FileDescriptor[] {DescriptorProtos.getDescriptor()});
  // FileDescriptorProto expectedProto = expectedFile.toProto();
  //
  // assertThat(fds.getFileCount(), equalTo(1));
  // final FileDescriptorProto actualProto = fds.getFile(0);
  //
  // // protoc requires(!) and combines a custom descriptor.proto, so all options, and the rest,
  // // refer to their custom descriptor.proto as their descriptors!!! So messages won't be equal
  // // here and there, and extensions will be failing due to the
  // // field.containingType() != message.getDescriptor()!
  // // we are preventing this, as anything in google.protobuf package, including descriptor.proto,
  // // is reserved, and automatically added by the ProtoFiles for building! A very nasty problem!
  // // In addition, two different protos will have different, not equal custom options/extensions
  // of
  // // Message type, because these types will have different descriptors, always!
  // assertThat(actualProto.getUnknownFields(), equalTo(expectedProto.getUnknownFields()));
  // assertThat(actualProto.getOptions(), not(equalTo(expectedProto.getOptions()))); //
  // assertThat(actualProto, not(equalTo(expectedProto))); //
  // assertThat(actualProto.toString(), equalTo(expectedProto.toString()));
  // }
  //
  // private FieldDescriptor getField(MessageOrBuilder message, String name) {
  // return message.getDescriptorForType().findFieldByName(name);
  // }
  //
  // private FieldDescriptor getField(Descriptor type, String name) {
  // return type.findFieldByName(name);
  // }
  //
  // @Test
  // public void testOriginalCustomOptions1() throws Exception {
  // //when
  // final FileDescriptor descriptor = UnittestCustomOptions.getDescriptor();
  // final FileDescriptorProto proto = descriptor.toProto();
  // log.info("UnittestCustomOptions proto:\n{}", proto);
  //
  // TestMessageWithCustomOptions msg = UnittestCustomOptions.TestMessageWithCustomOptions
  // .newBuilder().setField1("hello!").build();
  // final MessageOptions options = msg.getDescriptorForType().getOptions();
  // log.info("TestMessageWithCustomOptions options:\n{}", options);
  // }
  //
  // @Test
  // public void testDuplicateLocalTypes() throws Exception {
  // //when
  // FileDescriptorSetInfo info = filesBuilder
  // .addFiles(mainBasedir, "TypeDuplicates1.proto")
  // .build();
  //
  // //then
  // // no errors logged!
  // verify(mockErrorListener, atLeastOnce()).validationError(anyInt(), anyInt(), anyString(),
  // any(DescriptorValidationRuntimeException.class));
  // }
  //
  // @Test
  // public void testEnumMessageFieldsDescriptor() throws Exception {
  // //when
  // final String testProto = "EnumAndMessageFields1.proto";
  // final String messageName = "Message1";
  //
  // FileDescriptorSetInfo info = filesBuilder
  // .addFiles(baseDir, testProto)
  // .build();
  // Descriptor actualDescriptor = info.getFileDescriptorCache()
  // .get(testProto)
  // .findMessageTypeByName(messageName);
  // Descriptor expectedDescriptor = null;
  //
  // for (FileDescriptorProto proto : protocFdProtos) {
  // if (testProto.equals(proto.getName())) {
  // expectedDescriptor = FileDescriptor
  // .buildFrom(proto, new FileDescriptor[0])
  // .findMessageTypeByName(messageName);
  // break;
  // }
  // }
  //
  // if (expectedDescriptor == null) {
  // throw new NullPointerException("expectedDescriptor");
  // }
  //
  // //then
  // // no errors logged!
  // verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
  // any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  //
  // @SuppressWarnings("unchecked")
  // final Iterable<Object> actualFields = Iterable.class.cast(actualDescriptor.getFields());
  // assertThat(actualFields,
  // everyItem(hasProperty("type",
  // either(is(FieldDescriptor.Type.ENUM)).or(is(FieldDescriptor.Type.MESSAGE)))));
  // }
  //
  // //FIXME remove as a more comprehensive test passes!
  // @Ignore
  // @Test
  // public void testEnumMessageFieldsDescriptorWithProtoc() throws Exception {
  // //when
  // final String testProto = "EnumAndMessageFields1.proto";
  // final String messageName = "Message1";
  //
  // FileDescriptorSetInfo info = filesBuilder
  // .addFiles(baseDir, testProto)
  // .build();
  // Descriptor actualDescriptor = info.getFileDescriptorCache()
  // .get(testProto)
  // .findMessageTypeByName(messageName);
  //
  // final FileDescriptorProto originalExpectedProto = getProtocFileDescriptorProto(testProto,
  // false, FieldTypeRefsMode.AS_IS);
  // // final FileDescriptor expectedFile = FileDescriptorEx.buildCanonicalFileDescriptor(
  // // originalExpectedProto, new FileDescriptor[] {DescriptorProtos.getDescriptor()});
  // final FileDescriptor expectedFile = FileDescriptorEx.buildCanonicalFileDescriptor(
  // originalExpectedProto, new FileDescriptor[0]);
  //
  //
  // final Descriptor expectedDescriptor = expectedFile.findMessageTypeByName(messageName);
  //
  //
  // // for (FileDescriptorProto proto : protocFdProtos) {
  // // if (testProto.equals(proto.getName())) {
  // // expectedDescriptor = FileDescriptor
  // // .buildFrom(proto, new FileDescriptor[0])
  // // .findMessageTypeByName(messageName);
  // // break;
  // // }
  // // }
  //
  // if (expectedDescriptor == null) {
  // throw new NullPointerException("expectedDescriptor");
  // }
  //
  // //then
  // // no errors logged!
  // verify(mockErrorListener, never()).validationError(anyInt(), anyInt(), anyString(),
  // any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  //
  // for (FieldDescriptor actualField : actualDescriptor.getFields()) {
  // assertThat(actualField.getType(),
  // equalTo(expectedDescriptor.findFieldByName(actualField.getName()).getType()));
  // }
  //
  // assertThat(actualDescriptor.toProto().toString(),
  // equalTo(expectedDescriptor.toProto().toString()));
  // assertThat(actualDescriptor, comparesToDescriptor(expectedDescriptor));
  // }
  //
  // @Test
  // public void testFileOptionsDescriptorRef1() throws Exception {
  // final FileDescriptor file = DescriptorProtos.getDescriptor();
  // final Descriptor optionsType = FileOptions.getDescriptor();
  // final Descriptor optionsType2 = file.findMessageTypeByName("FileOptions");
  //
  // log.info("optionsType: ref: '{}', calss: '{}'", optionsType, optionsType.getClass());
  // log.info("optionsType2: ref: '{}', calss: '{}'", optionsType2, optionsType2.getClass());
  // assertThat(optionsType, equalTo(optionsType2));
  // }
  //
  // @Test
  // public void testExtensionContainingType1() throws Exception {
  // //when
  // // final LinkedHashMap<String, FileDescriptor> cache =
  // // new LinkedHashMap<String, FileDescriptor>();
  //
  // FileDescriptorSetInfo resultInfo = filesBuilder.addFiles(baseDir, "CustomOptions1.proto")
  // //.buildNew(cache);
  // .build();
  // final FileDescriptorSet fds = resultInfo.getFileDescriptorSet();
  // final FileDescriptorEx fileEx =
  // resultInfo.getFileDescriptorCache().get("CustomOptions1.proto");
  //
  // final Descriptor protoOptionsType = fds.getFile(0).getOptions().getDescriptorForType();
  // final Descriptor fileOptionsType =
  // fileEx.getFileDescriptor().getOptions().getDescriptorForType();
  //
  // final Descriptor expectedType = FileOptions.getDescriptor();
  //
  // assertThat(protoOptionsType, equalTo(expectedType));
  // assertThat(fileOptionsType, equalTo(expectedType));
  //
  // Descriptor extensionType = null;
  //
  // for (FieldDescriptor extension : fileEx.getFileDescriptor().getExtensions()) {
  // if (extension.getContainingType().getFullName().equals("google.protobuf.FileOptions")) {
  // extensionType = extension.getContainingType();
  // break;
  // }
  // }
  //
  // assertThat(extensionType, equalTo(expectedType));
  //
  // final FileDescriptor file = fileEx.getDeepCanonicalFileDescriptor(true);
  // assertThat(file.getOptions().getDescriptorForType(), equalTo(expectedType));
  //
  // for (FieldDescriptor extension : fileEx.getFileDescriptor().getExtensions()) {
  // if (extension.getContainingType().getFullName().equals("google.protobuf.FileOptions")) {
  // extensionType = extension.getContainingType();
  // break;
  // }
  // }
  //
  // assertThat(extensionType, equalTo(expectedType));
  // }
  //
  // @Test
  // public void testExtensionContainingType2() throws Exception {
  // //when
  // // final LinkedHashMap<String, FileDescriptor> cache =
  // // new LinkedHashMap<String, FileDescriptor>();
  //
  // FileDescriptorSetInfo resultInfo = filesBuilder.addFiles(baseDir, "CustomOptions1.proto")
  // //.buildNew(cache);
  // .build();
  // final FileDescriptorSet fds = resultInfo.getFileDescriptorSet();
  // final FileDescriptorEx fileEx =
  // resultInfo.getFileDescriptorCache().get("CustomOptions1.proto");
  // final FileDescriptor file = fileEx.getFileDescriptor();
  // final FileDescriptor fileCanonical = fileEx.getDeepCanonicalFileDescriptor(true);
  //
  // final Descriptor fileOptionsType = file.getOptions().getDescriptorForType();
  // final Descriptor fileCanonicalOptionsType = fileCanonical.getOptions().getDescriptorForType();
  // final Descriptor protoCanonicalOptionsType =
  // fileCanonical.toProto().getOptions().getDescriptorForType();
  //
  // final Descriptor expectedType = FileOptions.getDescriptor();
  //
  // assertThat(file.getDependencies().size(), equalTo(1));
  // assertThat(file.getDependencies().get(0), equalTo(DescriptorProtos.getDescriptor()));
  //
  // assertThat(protoCanonicalOptionsType, equalTo(expectedType));
  // assertThat(fileCanonicalOptionsType, equalTo(expectedType));
  //
  //
  // Descriptor extensionType = null;
  //
  // for (FieldDescriptor extension : fileCanonical.getExtensions()) {
  // if (extension.getContainingType().getFullName().equals("google.protobuf.FileOptions")) {
  // extensionType = extension.getContainingType();
  // break;
  // }
  // }
  //
  // assertThat(extensionType, equalTo(expectedType));
  // }
  //
  // @Test
  // public void testReservedSpaceValidation1() throws Exception {
  // //when
  // FileDescriptorSetInfo resultInfo = filesBuilder.addFiles(baseDir, "CustomOptions1.proto")
  // // will be silently replaced by the default descriptor.proto!
  // .addSource("google/protobuf/descriptor.proto", "package google.protobuf; message Message1 {}")
  // .build();
  //
  // //then
  // // one reserved space error validation
  // verify(mockErrorListener, never()).validationError(anyInt(), anyInt(),
  // endsWith("has package in reserved space"), any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  //
  // assertThat(resultInfo, is(not(nullValue())));
  // }
  //
  // @Test
  // public void testReservedSpaceValidation2() throws Exception {
  // //when
  // FileDescriptorSetInfo resultInfo = filesBuilder.addFiles(baseDir, "CustomOptions1.proto")
  // .addSource("google/protobuf/descriptor1.proto", "package google.protobuf; message Message1 {}")
  // .addSource("google/protobuf/descriptor2.proto", "package google.protobuf; message Message1 {}")
  // .build();
  //
  // //then
  // // one reserved space error validation
  // verify(mockErrorListener, times(2)).validationError(anyInt(), anyInt(),
  // endsWith("has package in reserved space"), any(RuntimeException.class));
  // verify(mockErrorListener, never()).syntaxError(any(Recognizer.class), any(), anyInt(),
  // anyInt(), anyString(), any(RecognitionException.class));
  //
  // assertThat(resultInfo, nullValue());
  // }
  //
  // @Test
  // public void testCanonicalization1() throws Exception {
  // final FileDescriptor allDescriptorsFile = DescriptorProtos.getDescriptor();
  // final FileDescriptorProto original = allDescriptorsFile.toProto();
  //
  // final FileDescriptorProto actual = ProtoFiles.buildCanonicalProtosFrom(
  // Collections.singleton(original), mockErrorListener).iterator().next();
  //
  // assertThat(actual, equalTo(original));
  // }
  //
  // @Test
  // public void testCanonicalization2() throws Exception {
  // final FileDescriptor baseFile = MultipleFilesTestProto.getDescriptor();
  // final Set<FileDescriptorProto> originals = new LinkedHashSet<>(
  // getFileSetFrom(baseFile).getFileList());
  //
  // final LinkedHashSet<FileDescriptorProto> actuals = ProtoFiles.buildCanonicalProtosFrom(
  // originals, mockErrorListener);
  //
  // assertThat(actuals, equalTo(originals));
  // }
  //
  // private FileDescriptorSet getFileSetFrom(final FileDescriptor baseFile) {
  // final Set<FileDescriptor> fileSet = new LinkedHashSet<>();
  // addDependencies(fileSet, baseFile);
  //
  // FileDescriptorSet.Builder builder = FileDescriptorSet.newBuilder();
  //
  // for (FileDescriptor file : fileSet) {
  // builder.addFile(file.toProto());
  // }
  //
  // return builder.build();
  // }
  //
  // private void addDependencies(final Set<FileDescriptor> fileSet, final FileDescriptor baseFile)
  // {
  // for (FileDescriptor file : baseFile.getDependencies()) {
  // fileSet.add(file);
  // }
  //
  // for (FileDescriptor file : baseFile.getDependencies()) {
  // addDependencies(fileSet, file);
  // }
  // }
}
