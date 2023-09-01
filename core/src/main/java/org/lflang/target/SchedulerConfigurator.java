package org.lflang.target;

import java.nio.file.Path;
import java.util.List;

import org.lflang.MessageReporter;
import org.lflang.TargetConfig;
import org.lflang.TargetPropertyConfig;
import org.lflang.ast.ASTUtils;
import org.lflang.lf.Element;
import org.lflang.lf.KeyValuePair;
import org.lflang.lf.LfPackage.Literals;
import org.lflang.lf.Model;
import org.lflang.target.SchedulerConfigurator.SchedulerOption;
import org.lflang.target.property.type.UnionType;
import org.lflang.validation.LFValidator.ValidationReporter;

import com.google.common.collect.ImmutableList;

public class SchedulerConfigurator implements TargetPropertyConfig<SchedulerOption> {

    @Override
    public void parseIntoTargetConfig(TargetConfig config, Element value, MessageReporter err) {
                config.schedulerType = this.parse(value);

    }

    @Override
    public SchedulerOption parse(Element value) {
        return (SchedulerOption)
            UnionType.SCHEDULER_UNION.forName(ASTUtils.elementToSingleString(value));
    }

    @Override
    public void validate(KeyValuePair pair, Model ast, TargetConfig config, ValidationReporter reporter) {
        if (pair != null) {
            String schedulerName = ASTUtils.elementToSingleString(pair.getValue());
            try {
                if (!SchedulerOption.valueOf(schedulerName).prioritizesDeadline()) {
                    // Check if a deadline is assigned to any reaction
                    // Filter reactors that contain at least one reaction that
                    // has a deadline handler.
                    if (ast.getReactors().stream()
                        .anyMatch(
                            // Filter reactors that contain at least one reaction that
                            // has a deadline handler.
                            reactor ->
                                ASTUtils.allReactions(reactor).stream()
                                    .anyMatch(reaction -> reaction.getDeadline() != null))) {
                        reporter.warning(
                            "This program contains deadlines, but the chosen "
                                + schedulerName
                                + " scheduler does not prioritize reaction execution "
                                + "based on deadlines. This might result in a sub-optimal "
                                + "scheduling.",
                            pair,
                            Literals.KEY_VALUE_PAIR__VALUE);
                    }
                }
            } catch (IllegalArgumentException e) {
                // the given scheduler is invalid, but this is already checked by
                // checkTargetProperties
            }
        }

    }

    @Override
    public Element getPropertyElement(TargetConfig config) {
        return ASTUtils.toElement(config.schedulerType.toString());
    }

    /**
     * Supported schedulers.
     *
     * @author Soroush Bateni
     */
    public enum SchedulerOption {
        NP(false), // Non-preemptive
        ADAPTIVE(
            false,
            List.of(
                Path.of("scheduler_adaptive.c"),
                Path.of("worker_assignments.h"),
                Path.of("worker_states.h"),
                Path.of("data_collection.h"))),
        GEDF_NP(true), // Global EDF non-preemptive
        GEDF_NP_CI(true); // Global EDF non-preemptive with chain ID
        // PEDF_NP(true);    // Partitioned EDF non-preemptive (FIXME: To be re-added in a future PR)

        /** Indicate whether or not the scheduler prioritizes reactions by deadline. */
        private final boolean prioritizesDeadline;

        /** Relative paths to files required by this scheduler. */
        private final List<Path> relativePaths;

        SchedulerOption(boolean prioritizesDeadline) {
            this(prioritizesDeadline, null);
        }

        SchedulerOption(boolean prioritizesDeadline, List<Path> relativePaths) {
            this.prioritizesDeadline = prioritizesDeadline;
            this.relativePaths = relativePaths;
        }

        /** Return true if the scheduler prioritizes reactions by deadline. */
        public boolean prioritizesDeadline() {
            return this.prioritizesDeadline;
        }

        public List<Path> getRelativePaths() {
            return relativePaths != null
                ? ImmutableList.copyOf(relativePaths)
                : List.of(Path.of("scheduler_" + this + ".c"));
        }

        public static SchedulerOption getDefault() {
            return NP;
        }
    }
}
