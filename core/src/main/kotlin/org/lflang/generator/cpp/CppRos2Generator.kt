package org.lflang.generator.cpp

import jakarta.ws.rs.NotSupportedException
import org.lflang.generator.LFGeneratorContext
import org.lflang.util.FileUtil
import org.lflang.util.LFCommand
import java.nio.file.Path

/** C++ platform generator for the ROS2 platform.*/
class CppRos2Generator(generator: CppGenerator) : CppPlatformGenerator(generator) {

    override val srcGenPath: Path = generator.fileConfig.srcGenPath.resolve("src")
    private val packagePath: Path = generator.fileConfig.srcGenPath
    private val nodeGenerator = CppRos2NodeGenerator(mainReactor, targetConfig, fileConfig);
    private val packageGenerator = CppRos2PackageGenerator(generator, nodeGenerator.nodeName)

    override fun generatePlatformFiles() {
        FileUtil.writeToFile(
            nodeGenerator.generateHeader(),
            packagePath.resolve("include").resolve("${nodeGenerator.nodeName}.hh"),
            true
        )
        FileUtil.writeToFile(
            nodeGenerator.generateSource(),
            packagePath.resolve("src").resolve("${nodeGenerator.nodeName}.cc"),
            true
        )

        FileUtil.writeToFile(packageGenerator.generatePackageXml(), packagePath.resolve("package.xml"), true)
        FileUtil.writeToFile(
            packageGenerator.generatePackageCmake(generator.cppSources),
            packagePath.resolve("CMakeLists.txt"),
            true
        )
        val scriptPath = fileConfig.binPath.resolve(fileConfig.name);
        FileUtil.writeToFile(packageGenerator.generateBinScript(), scriptPath)
        scriptPath.toFile().setExecutable(true);
    }

    override fun doCompile(context: LFGeneratorContext, onlyGenerateBuildFiles: Boolean): Boolean {
        val ros2Version = System.getenv("ROS_DISTRO")

        if (ros2Version.isNullOrBlank()) {
            messageReporter.nowhere(
            ).error(
                "Could not find a ROS2 installation! Please install ROS2 and source the setup script. " +
                        "Also see https://docs.ros.org/en/galactic/Installation.html"
            )
            return false
        }
        val colconCommand = commandFactory.createCommand(
            "colcon", colconArgs(), fileConfig.outPath)
            val returnCode = colconCommand?.run(context.cancelIndicator)
        if (returnCode != 0 && !messageReporter.errorsOccurred) {
            // If errors occurred but none were reported, then the following message is the best we can do.
            messageReporter.nowhere().error("colcon failed with error code $returnCode")
        }

        return !messageReporter.errorsOccurred
    }

    private fun colconArgs(additionalCmakeArgs: List<String> = listOf()): List<String> {
        return listOf(
                "build",
                "--packages-select",
                fileConfig.name,
                packageGenerator.reactorCppName,
                "--cmake-args",
                "-DLF_REACTOR_CPP_SUFFIX=${packageGenerator.reactorCppSuffix}",
            ) + cmakeArgs + additionalCmakeArgs
    }

    override fun getBuildCommands(additionalCmakeArgs: List<String>, parallelize: Boolean): List<List<String>> {
//        return listOf(listOf("colcon") + colconArgs(additionalCmakeArgs))
        throw NotImplementedError("Docker file generation for ROS 2 interoperability is not supported")
    }
}
