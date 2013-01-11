package exapus.model.view.evaluator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import exapus.model.forest.FactForest;
import exapus.model.forest.InboundFactForest;
import exapus.model.forest.InboundRef;
import exapus.model.forest.Member;
import exapus.model.forest.OutboundFactForest;
import exapus.model.forest.OutboundRef;
import exapus.model.forest.PackageLayer;
import exapus.model.forest.PackageTree;
import exapus.model.forest.UqName;
import exapus.model.view.Selection;
import exapus.model.visitors.SelectiveBottomUpCopyingForestVisitor;

public class APICentricTaggingSelectionVisitor extends SelectiveBottomUpCopyingForestVisitor {

	protected Iterable<Selection> selections;	
	protected Iterable<Selection> dual_selections;
	
	public APICentricTaggingSelectionVisitor(Iterable<Selection> selections, Iterable<Selection> dual_selections) {
		super();
		this.selections = selections;
		this.dual_selections = dual_selections;
	}

	@Override
	protected boolean select(final PackageTree packageTree) {			
		return Iterables.any(selections, new Predicate<Selection>() {
			@Override
			public boolean apply(Selection selection) {
				return selection.matchPackageTree(packageTree);

			}
		});
	}

	@Override
	protected boolean select(final PackageLayer packageLayer) {
		return Iterables.any(selections, new Predicate<Selection>() {
			@Override
			public boolean apply(Selection selection) {
				return selection.matchPackageLayer(packageLayer);
			}
		});
	}

	@Override
	protected boolean select(final Member member) {
		return Iterables.any(selections, new Predicate<Selection>() {
			@Override
			public boolean apply(Selection selection) {
				return selection.matchMember(member);

			}
		});
	}

	
 	
	@Override
	public boolean visitInboundReference(final InboundRef inboundRef) {
		FactForest forestCopy = getCopy();
		PackageTree untaggedTree = forestCopy.getPackageTree(InboundFactForest.DEFAULT_TREE_NAME);

		for(Selection selection : selections) {
			//have to match all selection as they can each copy the ref to a different packagetree
			if(selection.matchRef(inboundRef) 
					&& Iterables.any(dual_selections, new Predicate<Selection>() {
						@Override
						public boolean apply(Selection selection) {
							return selection.matchRef(inboundRef.getDual());
						}
					})) {
				PackageTree destinationTree;
				if(selection.hasTag()) 
					destinationTree = forestCopy.getOrAddPackageTree(new UqName(selection.getTagString()));
				else 
					destinationTree = untaggedTree;
				destinationTree.copyReference(inboundRef);
				return true;
				}
			}
		return false;
	}

	@Override
	protected boolean select(InboundFactForest forest) {
		return true;
	}

	@Override
	protected boolean select(OutboundFactForest forest) {
		return false;
	}
	
	@Override
	protected boolean select(OutboundRef outboundRef) {
		return false;
	}

	@Override
	protected boolean select(InboundRef inboundRef) {
		//not called as visitinBoundReference is overridden
		return false;
	}

}
