package org.lflang.generator.c;

import java.util.List;

import org.lflang.ASTUtils;
import org.lflang.generator.CodeBuilder;
import org.lflang.lf.Mode;
import org.lflang.lf.Reactor;

public class CModesGenerator {
    /**
     * Generate fields in the self struct for mode instances
     * 
     * @param reactor
     * @param body
     * @param constructorCode
     */
    public static void generateDeclarations(
        Reactor reactor,
        CodeBuilder body,
        CodeBuilder constructorCode
    ) {
        List<Mode> allModes = ASTUtils.allModes(reactor);
        if (!allModes.isEmpty()) {
            // Reactor's mode instances and its state.
            body.pr(String.join("\n", 
                "reactor_mode_t _lf__modes["+reactor.getModes().size()+"];",
                "reactor_mode_state_t _lf__mode_state;"
            ));
            
            // Initialize the mode instances
            constructorCode.pr("// Initialize modes");
            int initialMode = -1;

            for (int i = 0; i < allModes.size(); i++){
                var mode = allModes.get(i);
                constructorCode.pr(mode, String.join("\n", 
                    "self->_lf__modes["+i+"].state = &self->_lf__mode_state;",
                    "self->_lf__modes["+i+"].name = \""+mode.getName()+"\";",
                    "self->_lf__modes["+i+"].deactivation_time = 0;"
                ));
                if (initialMode < 0 && mode.isInitial()) {
                    initialMode = i;
                }
            }

            assert initialMode >= 0 : "initial mode must be non-negative!!";
            
            // Initialize mode state with initial mode active upon start
            constructorCode.pr(String.join("\n", 
                "// Initialize mode state",
                "self->_lf__mode_state.parent_mode = NULL;",
                "self->_lf__mode_state.initial_mode = &self->_lf__modes["+initialMode+"];",
                "self->_lf__mode_state.active_mode = self->_lf__mode_state.initial_mode;",
                "self->_lf__mode_state.next_mode = NULL;",
                "self->_lf__mode_state.mode_change = 0;"
            ));
        }
    }
}
