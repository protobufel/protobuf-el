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
* Also, you can manipulate DynamicMessage/Builder and/or GeneratedMessage/Builder with 
* JSR341 EL 3.0 Expression Language, in your regular Java client, or within any Servlet/JSF! 

* @see com.github.protobufel.el
* @see com.github.protobufel.crud.el 
**/

```

For more see the documentation at https://protobufel.github.io/protobuf-el/0.6 or directly the JavaDoc at https://protobufel.github.io/protobuf-el/0.6/apidocs/index.html

All project's public artifacts can be found on Maven at https://search.maven.org/#search%7Cga%7C1%7Cprotobufel :

##### Individual artifact Maven locations:

1. CRUD operations using Java JSR-341 EL 3.0 Expression Language with Google Porotocol Buffers 2.6.1:
    
    - groupId:artifactId  *com.github.protobufel:protobufel-crud*
    - add this to your Maven pom:
        ```
        
        <dependency>
            <groupId>com.github.protobufel</groupId>
            <artifactId>protobufel-crud</artifactId>
            <version>0.6</version>
        </dependency>
        ```
2. Java JSR-341 EL 3.0 Expression Language with Google Porotocol Buffers 2.6.1:
    
    - groupId:artifactId  *com.github.protobufel:protobufel-el*
    - add this to your Maven pom:
        ```
        
        <dependency>
            <groupId>com.github.protobufel</groupId>
            <artifactId>protobufel-el</artifactId>
            <version>0.6</version>
        </dependency>
        ```
3. Enhanced DynamicMessage and DynamicMessage.Builder with Google Porotocol Buffers 2.6.1:
    
    - groupId:artifactId  *com.github.protobufel:protobufel*
    - add this to your Maven pom:
        ```
        
        <dependency>
            <groupId>com.github.protobufel</groupId>
            <artifactId>protobufel</artifactId>
            <version>0.6</version>
        </dependency>
        ```
4. Java FileDescriptor/FileDescriptorProto/FileDescriptorSet builder and .proto files parser 100% compatible with Google Porotocol Buffers 2.6.1:
    
    - groupId:artifactId  *com.github.protobufel:protobufel-grammar*
    - add this to your Maven pom:
        ```
        
        <dependency>
            <groupId>com.github.protobufel</groupId>
            <artifactId>protobufel-grammar</artifactId>
            <version>0.6</version>
        </dependency>
        ```
5. Fast Java 7 file system scanning/processing (requires Java 7+):
    
    - groupId:artifactId  *com.github.protobufel:common-files-v7*
    - add this to your Maven pom:
        ```
        
        <dependency>
            <groupId>com.github.protobufel</groupId>
            <artifactId>common-files-v7</artifactId>
            <version>0.6</version>
        </dependency>
        ```
6. Java Extended Regex with multiple excludes/includes, requiring Java 7+ (can be easily backported to Java 6):
    
    - groupId:artifactId  *com.github.protobufel:extended-regex*
    - add this to your Maven pom:
        ```
        
        <dependency>
            <groupId>com.github.protobufel</groupId>
            <artifactId>extended-regex</artifactId>
            <version>0.6</version>
        </dependency>
        ```
7. Maven Resource Exec Plugin, requiring Java 7+:
    
    - groupId:artifactId  *com.github.protobufel:resource-exec-maven-plugin*
    - add this to your Maven pom:
        ```
        
        <dependency>
            <groupId>com.github.protobufel</groupId>
            <artifactId>resource-exec-maven-plugin</artifactId>
            <version>0.6</version>
        </dependency>
        ```

###### Note: Maven Central provides additional instructions for the following build systems:

    1. Apache Buildr
    2. Apache Ivy
    3. Groovy Grape
    4. Gradle/Grails
    5. Scala SBT
    6. Leiningen
    
    For more info, search the artifact, click on its version, and see the details.


Happy coding!

David Tesler

[![Build Status](https://travis-ci.org/protobufel/protobuf-el.svg?branch=master)](https://travis-ci.org/protobufel/protobuf-el)
