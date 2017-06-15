[![Build Status](https://travis-ci.org/protobufel/protobuf-el.svg?branch=master)](https://travis-ci.org/protobufel/protobuf-el)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.protobufel/protobufel-el.svg?style=plastic)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22protobufel-el%22)
----
1. Google Protocol Buffers' (ProtoBuf) Java Parser
2. JSR-341 Expression Language 3.0 with ProtoBuf
3. CRUD and JSR-341 Expression Language 3.0 with ProtoBuf 
3. Enhanced ProtoBuf DynamicMessage and Builder
4. Extended Java Regex
5. Fast Java PathMatcher and File Navigation
6. Maven Resource Execution Plugin

----


At present, there are no fully compliant Google Protocol Buffers 2.6.1 Java parsers of .proto files, at least known to me.
The one and only golden standard for this task is the original Google's ``protoc`` C++ implementation. It produces two major 
type of artifacts:

1. the source code in the target language along with the embedded Message definitions, i.e. `Descriptor`s, and/or 
2. FileDescriptorSet, which is the collection of all FileDescriporProto(-s) corresponding to the original .proto files.
    
The first kind of artifacts is all one needs to get a highly performant set of generated ProtoBuf `Message`s and `Builder`s, based on GeneratedMessage and GeneratedMessage.Builder. 

The second deliverable, however, is just a list of ProtoBuf `Message`s describing the desired "schemas", they are not even the schemas, `Descriptor`s, themselves. And there is no official API available, yet, to make/get these `Descriptor`s. The ProtoBuf Team wants you to use the generated stuff, and rightly so, as this is the whole purpose of the library. However, there are cases, like command-line, or GUI/Web utilities, dynamically generating Messages and Descriptors upon end-user specifications. In such scenarios, generating and compiling the source code is not an option. Here, ProtoBuf gives you DynamicMessage and its Builder, that only need to know a particular Descriptor to build a ProtoBuf Message.

The Descriptor can be had via its container, FileDescriptor. And to get/build a FileDescriptor, one needs to provide a FileDescriptorProto or a set of interdependent `FileDescriptorProto`s. This can be done via FileDescriptorProto.Builder, or if in the presence of the source .proto(-s) through the parser. Anyway, then FileDescriptor needs to be build. ProtoBuf provides only basic solution for this, ``FileDescriptor.buildFrom(FileDescriptorProto blueprint, FileDescriptor[] dependencies)``. You'll have to resolve all these dependencies by yourself!

Thus, is the provided Java Parser and FieDescriptor Builder library. Whether you have a source .proto as a file, String, or URL; or have already some FileDescriptorProto(-s), all you need is com.github.protobufel.grammar.ProtoFiles builder:
```java

final Map<String, FileDecsriptor> fileDescriptors = ProtoFiles.addFilesByGlob("**/*.proto")
    .addSource("first.proto", "message One { repeated Two sub_message = 1; }\n" 
        + "message Two { optional string name = 1; }")
    .addProto(existingFileDescriptorProto)
    .build();
```

-----

Note, the ProtoFiles' built-in Java parser is *really fully compliant*, it parses ALL original ProtoBuf 2.6.1 test .proto files *identically* to the `protoc`, with insignificant exceptions, where it is more true to the spec than the gold standard itself!

-----

Then, based on the given FileDescriptor, you can find the required message Descriptor, and build a Message via the original com.google.protobuf.DynamicMessage, or the advanced com.github.protobufel.DynamicMessage. 

The advanced com.github.protobufel.DynamicMessage.Builder combines all the relevant features of the original ProtoBuf DynamicMessage.Builder and GeneratedMessage.Builder, including hierarchical sub-builders, not implemented in com.google.protobuf.DynamicMessage.Builder. See the example below:

```java

import static com.github.protobufel.MessageAdapter.*;

import com.github.protobufel.DynamicMessage;
import com.github.protobufel.DynamicMessage.Builder;
import com.github.protobufel.grammar.ProtoFiles;

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

```

#### JSR-341 EL 3.0 Expression Language

This is the expression language of Java EE 7, and is used all over the EE, in JSP, JSF, Validation Framework.
Also, it is fully supported on Java SE 7, as a standalone library. And, though, it is backward compatible with its predecessors, is really a new powerful, small but capable Java embedded dynamic language, or kind of dynamic mini-Java 8 on Java 7 and up! 

For more on it, see https://jcp.org/aboutJava/communityprocess/final/jsr341/index.html and read the specification pdf!

The `com.github.protobufel.el` and `com.github.protobufel.crud.el` provide you with the extension of EL 3.0 to ProtoBuf. It's a very thin layer on the top of the official implementation, and therefore, can be used anywhere where EL 3.0 is running or applicable, i.e. in the Servelet, JSP, JSF, or standalone Java 7. 

In short, you can use *any* combination of POJOs, JavaBeans, and ProtoBuf `Message`s and `Builder`s with EL 3.0. And here is how:

Given the `protoc` generated code of galaxy.proto:

```

package com.fictional.test;

option java_package = "com.fictional.test";
option java_outer_classname = "GalaxyProto";
option optimize_for = SPEED;
option java_generate_equals_and_hash = true;

message Galaxy {
  required string name = 6;
  optional Color color = 1;
  optional Tag tag = 2;
  repeated string keyword = 3;
  repeated Star star = 4;
  repeated int32 code = 5;
  optional Data data = 7;
  optional Galaxy satellite = 8;
  optional string nickname = 9;

  enum Color {
    RED = 0;
    GREEN = 1;
    YELLOW = 2;
    BLUE = 3;
  }

  message Star {
    required string name = 1;
    optional Color color = 2;
    optional Tag tag = 3;
    repeated string keyword = 4;
    repeated Planet planet = 5;
    repeated int32 code = 6;
    optional Data data = 7;
  }
}

message Country {
  required string name = 1;
  optional Galaxy.Color color = 2;
  optional Tag tag = 3;
  repeated string keyword = 4;
  repeated City city = 5;
  repeated int32 code = 6;
  optional Data data = 7;
  repeated Country vassal = 8;
}

message City {
  required string name = 1 [default = "Unknown"];
  optional Galaxy.Color color = 2;
  optional Tag tag = 3;
  repeated string keyword = 4;
  repeated City city = 5;
  repeated int32 code = 6;
  optional Data data = 7;

}


message Data {
  required Size size = 1;
  optional double mass = 2;
  optional float volume = 3;

  enum Size {
    TINY = 0;
    SMALL = 1;
    MEDIUM = 2;
    LARGE = 3;
  }
}

message Tag {
  required string tag = 1;
}


message Planet {
  required string name = 1;
  optional Galaxy.Color color = 2;
  optional Tag tag = 3;
  repeated string keyword = 4;
  repeated Country country = 5;
  repeated int32 code = 6;
}
```

We can play with it in EL like this: 

```java

  ...
  
  private final List<String> expressions = Arrays.asList(
      "builder.star.getBuilders().stream().forEach(b->(b.name = b.name += '*')); builder.build()",
      
      "builder.star[0].clone().build()", "builder.star[0].name = 'Star1*'; builder.build()",
      
      "builder.star.getBuilders().stream().flatMap(b->b.planet.getBuilders().stream())"
          + ".filter(b->(b.country.size() > 0)).forEach(b->b.country.remove(0)).toList();"
          + " builder.build()", "list=builder.star[0].keyword.add('***1').getList()",
          
      "builder.keyword.add('***1').getList()",
      
      "starBuilder=builder.star; starBuilder.add().name='New Star2';"
          + "starBuilder.getList().stream().map(e->e.name).toList()",
          
      "builder.getStarBuilder(0).setName('Star1*'); builder.build()");

  public static void main(final String[] args) {
    final Examples examples = new Examples();
    final Galaxy galaxy = newGalaxy();

    for (final String expression : examples.expressions) {
      examples.evaluateMe(galaxy, expression);
    }
  }

  public void evaluateMe(final Galaxy galaxy, final String expression) {
    final ProtoELProcessorEx protoElp = new ProtoELProcessorEx();

    final Builder builder = galaxy.toBuilder();
    protoElp.defineBean("builder", builder);

    final Object result = protoElp.eval(expression);
    log.info("result of expression '{}' is '{}'", expression, result);
  }

  public static Galaxy newGalaxy() {
    return Galaxy
        .newBuilder()
        .setName("Galaxy1")
        .addCode(1)
        .addCode(2)
        .addCode(100)
        .addKeyword("keyword 1")
        .addKeyword("keyword 2")
        .addKeyword("keyword 3")
        .addKeyword("keyword 4")
        .setColor(Color.BLUE)
        .addStar(
            Star.newBuilder()
                .setName("Star1")
                .setColor(Color.GREEN)
                .setTag(Tag.newBuilder().setTag("tag1"))
                .addPlanet(
                    Planet
                        .newBuilder()
                        .setName("Planet1")
                        .setColor(Color.YELLOW)
                        .addCountry(
                            Country.newBuilder().setName("Country1")
                                .addCity(City.newBuilder().setName("City1"))))
                .addPlanet(
                    Planet
                        .newBuilder()
                        .setName("Planet2")
                        .setColor(Color.RED)
                        .addCountry(
                            Country.newBuilder().setName("Country1")
                                .addCity(City.newBuilder().setName("City1")))
                        .addCountry(
                            Country.newBuilder().setName("Country2")
                                .addCity(City.newBuilder().setName("City1"))
                                .addCity(City.newBuilder().setName("City2")))))
        .addStar(
            Star.newBuilder()
                .setName("Star2")
                .setColor(Color.GREEN)
                .setTag(Tag.newBuilder().setTag("tag2"))
                .addPlanet(Planet.newBuilder().setName("Planet1").setColor(Color.GREEN))
                .addPlanet(
                    Planet
                        .newBuilder()
                        .setName("Planet2")
                        .addCountry(
                            Country.newBuilder().setName("Country1")
                                .addCity(City.newBuilder().setName("City1")))
                        .addCountry(
                            Country.newBuilder().setName("Country2")
                                .addCity(City.newBuilder().setName("City1"))
                                .addCity(City.newBuilder().setName("City2"))))).build();
  }
```

Any ProtoBuf Message or Builder, including DynamicMessage/Builder and GeneratedMessage/Builder, will be treated conveniently and intuitively, not unlike the regular JavaBean/POJO. Here are some examples:

-  `galaxy.name` 
-  `galaxy['name']`
-  `galaxyBuilder.name = 'Silky Way'`
-  `galaxyBuilder['name'] = 'E Bay'`
-  `galaxy.star` - returns the list of Star(-s) 
-  `galaxy.star[1].name` 
-  `galaxyBuilder.star` - returns the repeated field Star's special wrapper
-  `galaxyBuilder.star[0].name`
-  `galaxyBuilder.star.add()` - returns the new empty star added to the Star field
-  `sun=galaxyBuilder.star.newInstance(); sun.name='Sun'; galaxyBuilder.star.add(1, sun).size()`
-  `galaxyBuilder.star.remove(1).getLast()`
-  `galaxyBuilder.star.remove(1).toParent().color = 'RED'` - galaxy color set to RED
-  `galaxyBuilder.star.getBuilders()`
-  `galaxyBuilder.star.getList()` - returns list of Stars
-  `galaxyBuilder.keyword[0] == 'my keyword'` 

###### Use `com.github.protobufel.crud.el` for CRUD queries with EL 3.0 and Protocol Buffers:

```java


  public List<Message> mutateRecordWithEnum(final List<Message> originalCityList) {
    return ProtoMessageQueryProcessor.builder()
        .setExpression("(index==0) ? (record.color='RED') : null; record")
        .process(originalCityList);
  }

```  

`ProtoMessageQueryProcessor` allows you to add your own JavaBeans to be used within your expression, and also  
 defines its own JavaBeans available in the expression:
 
    1. `records` - the immutable `List<Message>` of the original records
    2. `results` - the immutable `List<Message>` of the results being produced
    3. `record` - the current row's Message.Builder, will be in `results`
    4. `index` - the current row's index in the results
    
You can also specify the so called `empty expression` to the `ProtoMessageQueryProcessor.Builder` to produce the original records.

In addition, you can add your own `ValidationListener` to the `ProtoMessageQueryProcessor.Builder`, and the `QueryResultListener`. If your expression returns `null`, the current result will be skipped, so this is the way to remove the original record from the results. 

The `QueryResultListener.resultAdded(originalRecordIndex)` allows you to keep track of the being produced results. Initially, the `results` list is empty; and the result producing loop follows the original records. So, the originals versus the results diffs can be easily and efficiently calculated.          

##### Fast File System Resources Scanning/Processing on Java 7

The JDK 7 PathMatcher API is very limited and simplistic. It allows for filtering single directories, not the file system's trees and forests. Here comes `com.github.protobufel.common.files` package. It allows for multi-root resource processing, and doing it fast and efficient. Multiple includes/excludes in the Java 7 defined glob/regex syntax are supported. Entire sub-trees will be skipped if these conditions are not satisfied, so its orders of magnitude faster than the regular scans with regexes, which only test the tree leafs. And here is how:

```java

    final IFileSet fileSet =
        FileSet.builder().allowDirs(allowDirs).allowFiles(allowFiles).directory(dir)
            .addIncludes(includes).addExcludes(excludes).build();

    final Iterable<Path> files =
        PathVisitors.getResourceFiles(rootDir, fileSet, followLinks, allowDuplicates, log);

```

For more, see the documentation at https://protobufel.github.io/protobuf-el/0.7-SNAPSHOT or directly the JavaDoc at https://protobufel.github.io/protobuf-el/0.7-SNAPSHOT/apidocs/index.html

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
        
        <plugin>
            <groupId>com.github.protobufel</groupId>
            <artifactId>resource-exec-maven-plugin</artifactId>
            <version>0.6</version>
        </plugin>
        ```

###### Note: Maven Central provides additional instructions for the following build systems:

    1. Apache Buildr
    2. Apache Ivy
    3. Groovy Grape
    4. Gradle/Grails
    5. Scala SBT
    6. Leiningen
    
    For more info, search the artifact, click on its version, and see the details.

### Build from the source

1. The entire source is a pure Java Maven multi-module project. So, use an IDE of your choice to import the entire project.
2. There are 2 shared test resource modules with the pre-generated by protoc Java sources and `FileDescriptorSet`(-s) based on the included .proto files. If, for some reason, they need to be re-generated with `protoc`, then do the following:  

    1. install Protocol Buffers 2.6.1 `protoc` on your system
    2. define environment variable `PROTOC_EXEC` and set it to the `protoc` path
    3. run Maven with `env-protoc` profile and `env=protoc`

Again, the re-generation of the Java sources and FileDescriptorSet(-s) is not necessary; only in the case of the Protocol Buffers' version differing from 2.6.1.

If you are interested in the ProtoBuf Java Parser and Builder, look at the `/protobufel-grammar/src/test/java/com/github/protobufel/grammar/CompareWithOriginalsTest.java`.
It tests the source against *ALL* ProtoBuf 2.6.1 original test `.proto`s, comparing the bulk results!
There are minor differences between the original results produced by `protoc` and the source:

1. `protoc` converts all primitive fields' default values to decimal format, contrary to the spec; we don't. However, the resulting `FileDescriptor` and any its descriptors will have the identical values. See https://github.com/google/protobuf/issues/61
2. `protoc` allows the maximum extension range of `536870912` when `message_set_wire_format = true`; we don't. See https://github.com/google/protobuf/issues/26
3. `protoc` serializes custom options differently from Java API. However, the deserialization produces the identical results, i.e. these two forms are equivalent with reguard to deserialization. As the consequence, our Java Parser/Builder is identical to any manual FileDescriptorProto construction in Java. Regardless, the resulting `FileDecsriptor`s are equal in function in both cases. See https://github.com/google/protobuf/issues/59   


Happy coding!

David Tesler
