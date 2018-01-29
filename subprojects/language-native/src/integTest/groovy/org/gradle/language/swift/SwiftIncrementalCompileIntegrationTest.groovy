/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.swift

import org.gradle.integtests.fixtures.CompilationOutputsFixture
import org.gradle.nativeplatform.fixtures.AbstractInstalledToolChainIntegrationSpec
import org.gradle.nativeplatform.fixtures.RequiresInstalledToolChain
import org.gradle.nativeplatform.fixtures.ToolChainRequirement
import org.gradle.nativeplatform.fixtures.app.SwiftApp

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftIncrementalCompileIntegrationTest extends AbstractInstalledToolChainIntegrationSpec {
    def 'recompiles only the Swift source files that have changed'() {
        given:
        def outputs = new CompilationOutputsFixture(file("build/obj/main/debug"))
        def app = new SwiftApp()
        settingsFile << "rootProject.name = 'app'"
        app.writeToProject(testDirectory)
        def fileToModify = file("src/main/swift/main.swift").makeOlder()

        buildFile << """
            apply plugin: 'swift-application'
         """
        outputs.snapshot { succeeds("assemble", "-i") }
        def moduleSwiftDeps = file("build/obj/main/debug/module.swiftdeps")

        when:
        fileToModify.replace("a: 5, b: 7", "a: 21, b: 21")
        and:
        succeeds("assemble", "-i")

        then:
        outputs.recompiledFiles([ fileToModify, moduleSwiftDeps, file("build/obj/main/debug/output-file-map.json") ])
    }
}
