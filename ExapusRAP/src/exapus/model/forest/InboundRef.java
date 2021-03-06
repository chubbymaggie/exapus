package exapus.model.forest;

import org.eclipse.jdt.core.SourceRange;

import exapus.model.visitors.IForestVisitor;

public class InboundRef extends Ref {

	public InboundRef(Pattern p, Element e, QName n, SourceRange r, int l) {
		super(Direction.INBOUND, p, e, n, r, l);
	}

	public static InboundRef fromOutboundRef(OutboundRef o) {
		InboundRef inbound = new InboundRef(o.getReferencingPattern(), o.getReferencingElement(), o.getReferencingName(), o.getSourceRange(), o.getLineNumber());
		inbound.setDual(o);
		o.setDual(inbound);
		return inbound;
	}
	
	public static InboundRef fromInboundRef(InboundRef inboundRef) {
		InboundRef copy = new InboundRef(inboundRef.getReferencingPattern(), inboundRef.getReferencingElement(), inboundRef.getReferencingName(), inboundRef.getSourceRange(), inboundRef.getLineNumber());
		copy.setDual(inboundRef.getDual());
		return copy;		
	}
	
	public QName getReferencingName() {
		return rname;
	}
	
	public QName getReferencedName() {
		return this.getQName();
	}
	
	public Element getReferencingElement() {
		return element;
	}
	
	public Element getReferencedElement() {
		return super.getElementOfParentMember();
	}

	public String toString() {
		return "IR[" +  "<-" + getReferencingElement().toString()  + ":" + getReferencingPattern().toString() +  getReferencingName().toString() + "]";
	}

	@Override
	public void acceptVisitor(IForestVisitor v) {
			v.visitInboundReference(this);
	}
		
}
