package org.icyphy.linguafranca.diagram.synthesis.action

import de.cau.cs.kieler.klighd.IAction
import de.cau.cs.kieler.klighd.kgraph.KNode
import org.icyphy.linguaFranca.Reactor
import org.icyphy.linguafranca.diagram.synthesis.LinguaFrancaSynthesis

import static extension de.cau.cs.kieler.klighd.util.ModelingUtil.*
import static extension org.icyphy.linguafranca.diagram.synthesis.action.MemorizingExpandCollapseAction.*

class ExpandAllReactorsAction implements IAction {
    
    public static val ID = "org.icyphy.linguafranca.diagram.synthesis.action.ExpandAllReactorsAction"
    
    override execute(ActionContext context) {
        val vc = context.viewContext
        for (node : vc.viewModel.eAllContentsOfType(KNode).filter[vc.getSourceElement(it) instanceof Reactor].toIterable) {
            node.setExpansionState(node.getProperty(LinguaFrancaSynthesis.REACTOR_INSTANCE), vc.viewer, true)
        }
        return IAction.ActionResult.createResult(true);
    }
    
}
