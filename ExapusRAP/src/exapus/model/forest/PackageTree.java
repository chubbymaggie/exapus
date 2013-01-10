package exapus.model.forest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import com.google.common.collect.Iterables;

import exapus.model.visitors.IForestVisitor;

public class PackageTree extends ForestElement  implements ILayerContainer  {

	private FactForest forest;

	private PackageLayer root;

	public PackageTree(UqName name) {
		super(name);
		root = new PackageLayer(new UqName("<rootLayer>"));
		root.setParent(this); // TODO: this might cause the root layer to show
		// up in views
	}
	
	@Override
	public QName getQName() {
		return new QName(getName());
	}

	public PackageLayer getHiddenRootLayer() {
		return root;
	}

	public Iterable<PackageLayer> getLayers() {
		return root.getLayers();
	}

	public void processSourcePackageFragment(IPackageFragment f) throws JavaModelException {
		if (f.getKind() != IPackageFragmentRoot.K_SOURCE)
			return;
		if (!f.containsJavaResources())
			return;
		PackageLayer layer = getOrAddLayerForPackageFragment(f);
		ICompilationUnit[] compilationUnits = f.getCompilationUnits();
		layer.processCompilationUnits(f.getJavaProject(), compilationUnits);
	}

	public FactForest getParentFactForest() {
		return forest;
	}

	public void setFactForest(FactForest forest) {
		this.forest = forest;
	}

	public String toString() {
		return "T[" + getName() + "]";
	}

	public IProject getCorrespondingIProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getName().toString());
	}

	@Override
	public Member getParentMember() {
		return null;
	}

	@Override
	public ICompilationUnit getCorrespondingICompilationUnit() {
		return null;
	}


	public Iterable<Member> getAllMembers() {
		return root.getAllMembers();
	}

	public Iterable<Ref> getAllReferences() {
		return root.getAllReferences();
	}

	public void addLayer(PackageLayer l) {
		root.addLayer(l);
		l.setParent(this);
	}
	
	
	public PackageLayer getOrAddLayerForPackageFragment(IPackageFragment packageFragment) {
		QName qname = new QName(packageFragment);
		PackageLayer layer = root.getOrAddLayer(qname, this);
		getParentFactForest().fireUpdate(layer);
		return layer;
	}


	private PackageLayer getOrAddLayerCorrespondingToTypeBinding(ITypeBinding t) {
		IType type = (IType) t.getJavaElement();
		IPackageFragment packageFragment = type.getPackageFragment();
		PackageLayer layer = getOrAddLayerForPackageFragment(packageFragment);
		return layer;
	}

	public void addInboundReference(ITypeBinding b, OutboundRef outbound) {
		PackageLayer layer = getOrAddLayerCorrespondingToTypeBinding(b);
		layer.addInboundReference(b, outbound);
	}

	public void addInboundReference(ITypeBinding t, IMethodBinding b, OutboundRef outbound) {
		PackageLayer layer = getOrAddLayerCorrespondingToTypeBinding(t);
		layer.addInboundReference(b, outbound);
	}

	public void addInboundReference(ITypeBinding t, IVariableBinding b, OutboundRef outbound) {
		PackageLayer layer = getOrAddLayerCorrespondingToTypeBinding(t);
		layer.addInboundReference(b, outbound);
	}

	public void acceptVisitor(IForestVisitor v) {
		if(v.visitPackageTree(this))
			for(PackageLayer l : getLayers())
				l.acceptVisitor(v);
	}

	public void insertReferenceCopy(Ref original) {
		PackageLayer originalLayer = original.getParentPackageLayer();
		PackageLayer destinationLayer = root.getOrAddLayer(originalLayer.getQName(), this);
		Member originalMember = original.getParentMember();
		
		//destinationLayer.getOrAddMember(originalMember.getQName(), originalMember.getElement());
		
		//destinationLayer.
		
		
		/*
		ForestElement parent = ref.getParent();
		LinkedList<ForestElement> parents = new LinkedList<ForestElement>();
		while (parent != null && !(parent instanceof PackageTree)) {
			parents.addFirst(parent);
			parent = parent.getParent();
		}
		Iterator<ForestElement> ancestor = parents.iterator();
		insertReference(ancestor, ref);
		*/
	}

}
