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

package com.github.protobufel.grammar;

import static org.hamcrest.Matchers.equalTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protobuf_unittest.UnittestCustomOptions.Aggregate;
import protobuf_unittest.UnittestProto.TestAllTypes;

import com.github.protobufel.grammar.DescriptorFactory.FileDescriptorSetBuilder;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.TestUtil;
import com.google.protobuf.TextFormat;

//TODO redo all tests and enable, or refactor th original source!
@Ignore
public class DescriptorFactoryTest {
  private static final Logger log = LoggerFactory.getLogger(DescriptorFactoryTest.class);

  @Before
  public void setUp() throws Exception {
  }

  private void printTheDescriptor() {
    final String protoText = TextFormat.printToUnicodeString(
        DescriptorProtos.getDescriptor().toProto());
    log.debug("the Descriptor proto: \n{}", protoText);
  }
  
  private void printCustomOptions() {
    final String protoText = TextFormat.printToUnicodeString(
        Aggregate.getDescriptor().getFile().toProto());
    log.debug("the CustomOptions proto: \n{}", protoText);
  }
  
  @Test
  public void printTheDescriptorProto() throws Exception {
    printTheDescriptor();
  }
  
  @Test
  public void printCustomOptionsProto() throws Exception {
    printCustomOptions();
  }
  
  @Test
  public void testTextFormatWithDescriptor() throws Exception {
    //TestUtil.getAllSet();
    String allSet = TextFormat.printToString(TestUtil.getAllSet());
    final DescriptorProto expectedAllSetProto = TestAllTypes.getDescriptor().toProto();
    String allSetProto = TextFormat.printToString(expectedAllSetProto);
    log.debug("the message: {}", allSet);
    log.debug("the proto: {}", allSetProto);
    
    DynamicMessage.Builder builder = DynamicMessage.newBuilder(DescriptorProto.getDescriptor());
    TextFormat.merge(allSetProto, builder);
    Message actualAllSetProto = builder.build();
    
    assertThat(actualAllSetProto).isEqualTo(expectedAllSetProto);
    
    FieldDescriptor field = FileDescriptorProto.getDescriptor()
        .findFieldByNumber(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER);
    FileDescriptorProto fileProto = FileDescriptorProto.newBuilder().setName("my file1")
        .addRepeatedField(field, actualAllSetProto)
        .build();
    FileDescriptor fileDescriptor = FileDescriptor.buildFrom(fileProto, new FileDescriptor[0]);
    Descriptor actualAllTypesDescriptor = fileDescriptor.findMessageTypeByName(
        TestAllTypes.getDescriptor().getFullName());
    
    assertThat(actualAllTypesDescriptor, equalTo(TestAllTypes.getDescriptor()));
  }
  
  @Test
  public void testFileSetSerialization() throws Exception {
    final FileDescriptorSet fileDescriptorSet = FileDescriptorSetBuilder.newBuilder()
        .addDescriptor(TestAllTypes.getDescriptor()).build();
    String fileDescriptorSetText = TextFormat.printToString(fileDescriptorSet);
    
    FileDescriptorSet.Builder fileDescriptorSetBuilder = FileDescriptorSet.newBuilder();
    TextFormat.merge(fileDescriptorSetText, fileDescriptorSetBuilder);
    FileDescriptorSet actualFileSet = fileDescriptorSetBuilder.build();
    
    assertThat(actualFileSet, equalTo(fileDescriptorSet));
  }
  
  private FileDescriptorProto.Builder addDeepMessageTypeToFile(
      FileDescriptorProto.Builder builder, Message descriptorProto) {
    //TODO delete this method!!!
    final FieldDescriptor field = FileDescriptorProto.getDescriptor()
        .findFieldByNumber(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER);
    final FieldDescriptor nestedTypeField = DescriptorProto.getDescriptor().findFieldByNumber(
        DescriptorProto.NESTED_TYPE_FIELD_NUMBER);
    builder.addRepeatedField(field, descriptorProto);
    
    for (Message nestedMessageType : (List<Message>) descriptorProto.getField(nestedTypeField)) {
      // builder.addRepeatedField(field, nestedMessageType);
      addDeepMessageTypeToFile(builder, nestedMessageType);
    }

    return builder;
  }
 
  private FileDescriptorSet.Builder addDescriptorToFileSet(FileDescriptorSet.Builder builder, 
      Descriptor descriptor, Set<FileDescriptorProto> fileProtoSet) {
    List<? extends FileDescriptorProtoOrBuilder> fileList = builder.getFileOrBuilderList();
    final FileDescriptor file = descriptor.getFile();
    FileDescriptorProto proto = file.toProto();
    
    if (fileList.contains(proto)) {
      return builder;
    }
    
    builder.addFile(proto);
    
    for (FileDescriptor dependency : file.getDependencies()) {
      proto = dependency.toProto();
      
      if (!fileList.contains(proto)) {
        builder.addFile(proto);
      }
    }
    
    return builder;
  }
}
