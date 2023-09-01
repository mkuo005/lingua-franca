package org.lflang.target.property.type;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.lflang.Target;
import org.lflang.TargetProperty.DictionaryElement;
import org.lflang.lf.Element;
import org.lflang.lf.KeyValuePair;
import org.lflang.lf.KeyValuePairs;
import org.lflang.target.ClockSyncConfigurator.ClockSyncOption;
import org.lflang.target.CoordinationConfig.CoordinationOption;
import org.lflang.target.DockerConfig.DockerOption;
import org.lflang.target.PlatformConfigurator.PlatformOption;
import org.lflang.target.TracingConfigurator.TracingOption;
import org.lflang.validation.LFValidator;

/**
 * A dictionary type with a predefined set of possible keys and assignable types.
 *
 * @author Marten Lohstroh
 */
public enum DictionaryType implements TargetPropertyType {
    CLOCK_SYNC_OPTION_DICT(Arrays.asList(ClockSyncOption.values())),
    DOCKER_DICT(Arrays.asList(DockerOption.values())),
    PLATFORM_DICT(Arrays.asList(PlatformOption.values())),
    COORDINATION_OPTION_DICT(Arrays.asList(CoordinationOption.values())),
    TRACING_DICT(Arrays.asList(TracingOption.values()));

    /** The keys and assignable types that are allowed in this dictionary. */
    public List<DictionaryElement> options;

    /**
     * A dictionary type restricted to sets of predefined keys and types of values.
     *
     * @param options The dictionary elements allowed by this type.
     */
    private DictionaryType(List<DictionaryElement> options) {
        this.options = options;
    }

    /**
     * Return the dictionary element of which the key matches the given string.
     *
     * @param name The string to match against.
     * @return The matching dictionary element (or null if there is none).
     */
    public DictionaryElement forName(String name) {
        return Target.match(name, options);
    }

    /** Recursively check that the passed in element conforms to the rules of this dictionary. */
    @Override
    public void check(Element e, String name, LFValidator v) {
        KeyValuePairs kv = e.getKeyvalue();
        if (kv == null) {
            TargetPropertyType.produceError(name, this.toString(), v);
        } else {
            for (KeyValuePair pair : kv.getPairs()) {
                String key = pair.getName();
                Element val = pair.getValue();
                Optional<DictionaryElement> match =
                    this.options.stream()
                        .filter(element -> key.equalsIgnoreCase(element.toString()))
                        .findAny();
                if (match.isPresent()) {
                    // Make sure the type is correct, too.
                    TargetPropertyType type = match.get().getType();
                    type.check(val, name + "." + key, v);
                } else {
                    // No match found; report error.
                    TargetPropertyType.produceError(name, this.toString(), v);
                }
            }
        }
    }

    /** Return true if the given element represents a dictionary, false otherwise. */
    @Override
    public boolean validate(Element e) {
        if (e.getKeyvalue() != null) {
            return true;
        }
        return false;
    }

    /** Return a human-readable description of this type. */
    @Override
    public String toString() {
        return "a dictionary with one or more of the following keys: "
            + options.stream().map(option -> option.toString()).collect(Collectors.joining(", "));
    }
}