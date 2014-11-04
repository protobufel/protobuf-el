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

/**
 * Provides fast and convenient directory and file system scanning and processing.
 * <p>
 * Java 7 and 8 {@link java.nio.file} package provides a basic {@link java.nio.file.PathMatcher}
 * interface and a bare bone implementation. Unfortunately, the algorithm doesn't filter out
 * directories, and only works directly on files for globs and regexes, meaning the processing of
 * <strong>the entire subtree(-s)</strong>!
 * <p>
 * In contrast, the classes of this package, while dealing with the same glob or regexes syntax,
 * will intelligently skip entire directories, if the pattern(-s) are not satisfied. In effect, for
 * the large file trees, the savings would amount to many orders of magnitude!
 * <p>
 * In addition to the efficient processing, there are classes dealing with resources by specifying
 * multiple blob/regex excludes and/or includes; all while using the same efficient algorithm.
 * 
 * @see <a href="doc-files/Examples.java.txt">Examples.java</a>
 * @see com.github.protobufel.common.files.PathVisitors
 * @see com.github.protobufel.common.files.resources.Resources
 * 
 * @author protobufel@gmail.com David Tesler
 */
@org.eclipse.jdt.annotation.NonNullByDefault
package com.github.protobufel.common.files;