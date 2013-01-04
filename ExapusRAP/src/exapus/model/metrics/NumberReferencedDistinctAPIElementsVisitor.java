package exapus.model.metrics;

import exapus.model.forest.*;
import exapus.model.visitors.IForestVisitor;

public class NumberReferencedDistinctAPIElementsVisitor implements IForestVisitor {
    @Override
    public boolean visitInboundFactForest(InboundFactForest forest) {
        return false;
    }

    @Override
    public boolean visitOutboundFactForest(OutboundFactForest forest) {
        return true;
    }

    @Override
    public boolean visitPackageTree(PackageTree packageTree) {
        return true;
    }

    @Override
    public boolean visitPackageLayer(PackageLayer packageLayer) {
        return true;
    }

    @Override
    public boolean visitMember(Member member) {
        return true;
    }

    @Override
    public boolean visitOutboundReference(OutboundRef outboundRef) {
        if (outboundRef.getMetric() instanceof NumberReferencedDistinctAPIElements) {
            ((NumberReferencedDistinctAPIElements) outboundRef.getMetric()).addName(outboundRef.getReferencedName().toString(), outboundRef);
        }
        return true;
    }

    @Override
    public boolean visitInboundReference(InboundRef inboundRef) {
        return false;
    }
}
