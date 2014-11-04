1. Google Protocol Buffers' (ProtoBuf) Java Parser
2. JSR-341 Expression Language 3.0 with ProtoBuf
3. Enhanced ProtoBuf DynamicMessage and Builder
4. Extended Java Regex
5. Fast Java PathMatcher and File Navigation
6. Maven  Resource Execution Plugin

```java

import static com.github.protobufel.MessageAdapter.*;

import com.github.protobufel.DynamicMessage;
import com.github.protobufel.DynamicMessage.Builder;

// build your FileDecsriptor(-s) 
final Map<String, FileDecsriptor> fileDescriptors = ProtoFiles.addFilesByGlob("**/*.proto")
    .addSource("first.proto", "message One { repeated Two sub_message = 1; }\n" 
        + "message Two { optional string name = 1; }")
    .addProto(existingFileDescriptorProto)
    .build();

// use com.github.protobufel.DynamicMessage for processing
final FileDescriptor fileDescriptor = fileDescriptors.get("first.proto");
final Descriptor descOne = fileDescriptor.findMessageTypeByName("One");
final Descriptor descTwo = fileDescriptor.findMessageTypeByName("Two");
final FieldDescriptor subMessageField = getFieldDescriptor(descOne, "subMessage");
final FieldDescriptor nameField = getFieldDescriptor(descTwo, "name");

final Builder builderOne = DynamicMessage.newBuilder(descOne);
builderOne.addFieldBuilder(subMessageField).setField(nameField, "world");
builderOne.addFieldBuilder(subMessageField, 0).setField(nameField, "hello ")
    .toParent()
    .getBuilderList(descTwo).get(0).setField(nameField, "world!");
final DynamicMessage message = builderOne.build();      

/**
* Or you can manipulate DynamicMessage/Builder and/or GeneratedMessage/Builder with 
* JSR341 EL 3.0 expression language, including in your regular Java client, or your servlet/JSF! 

* @see com.github.protobufel.el
* @see com.github.protobufel.crud.el 
**/

```

Happy coding!

David Tesler
