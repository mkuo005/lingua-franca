package org.lflang.tests.serialization;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.lflang.generator.GeneratorArguments;
import org.lflang.target.Target;
import org.lflang.target.TargetConfig;
import org.lflang.target.property.type.BuildTypeType.BuildType;
import org.lflang.tests.Configurators;
import org.lflang.tests.TestBase;
import org.lflang.tests.TestRegistry.TestCategory;

public class SerializationTest extends TestBase {

  protected SerializationTest() {
    super(Target.ALL);
  }

  @Override
  protected void addExtraLfcArgs(GeneratorArguments args, TargetConfig targetConfig) {
    super.addExtraLfcArgs(args, targetConfig);
    // Use the Debug build type as coverage generation does not work for the serialization tests
    args.buildType = BuildType.DEBUG;
  }

  @Test
  public void runSerializationTestsWithThreadingOff() {
    Assumptions.assumeTrue(supportsSingleThreadedExecution(), Message.NO_SINGLE_THREADED_SUPPORT);
    runTestsForTargets(
        Message.DESC_SERIALIZATION,
        TestCategory.SERIALIZATION::equals,
        Configurators::disableThreading,
        TestLevel.EXECUTION,
        false);
  }

  @Test
  public void runSerializationTests() {
    Assumptions.assumeFalse(isWindows(), Message.NO_WINDOWS_SUPPORT);
    runTestsForTargets(
        Message.DESC_SERIALIZATION,
        TestCategory.SERIALIZATION::equals,
        Configurators::noChanges,
        TestLevel.EXECUTION,
        false);
  }
}
