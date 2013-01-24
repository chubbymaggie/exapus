package exapus.model.view;

import exapus.model.forest.Direction;
import exapus.model.forest.Member;
import exapus.model.forest.PackageLayer;
import exapus.model.forest.PackageTree;
import exapus.model.forest.QName;
import exapus.model.forest.Ref;

public class ScopedSelection extends Selection {

	public ScopedSelection(QName name, Scope scope, String tag) {
		super();
		this.name = name;
		this.scope = scope;
		this.tag = tag;
	}
	
	public ScopedSelection(QName name, Scope scope) {
		this(name,scope,null);
	}

	
	public ScopedSelection(QName name) {
		this(name, Scope.PREFIX_SCOPE);
	}
	

	private Scope scope;
		
	private QName name;
	
	private String tag;
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public boolean hasTag() {
		return tag != null;
	}
	
	
	//to avoid recomputation for type/method scopes, could go into QName but there it would consume bytes
	//should move to type/method scopes when refactored into a scope hierarchy 
	private QName methodPackageName;
	private QName typePackageName;
	
	private QName getMethodPackageName() {
		if(methodPackageName == null) {
			methodPackageName = getTypePackageName().getButLast();
		}
		return methodPackageName;
	}
	
	private QName getTypePackageName() {
		if(typePackageName == null) {
			typePackageName = name.getButLast();
		}
		return typePackageName;
	}
	
	@Override
	public boolean matchPackageTree(PackageTree packageTree) {
		if(scope.equals(Scope.ROOT_SCOPE)) { 
			//projects correspond to package trees, this is the only place where ROOT_SCOPE makes sense
			if(packageTree.getParentFactForest().getDirection().equals(Direction.OUTBOUND))
				return packageTree.getQName().equals(name);
			//package layers are grouped in one dummy packagetree
			return true;
		}
		return true;
	}

	@Override
	public boolean matchPackageLayer(PackageLayer packageLayer) {
		//multiple scopes can be selected at the same time, cannot trust filtering to have happened above
		if(scope.equals(Scope.ROOT_SCOPE)) 
			return packageLayer.getParentPackageTree().getQName().equals(name);
		
		//scope java.lang, include parent layer java to preserve hierarchy .. filter later on 
		if(scope.equals(Scope.PACKAGE_SCOPE)) 
			return packageLayer.getQName().isPrefixOf(name);
		
		//scope java.lang, include parent layer java to preserve hierarchy .. filter later on 
		if(scope.equals(Scope.PREFIX_SCOPE)) 
			return packageLayer.getQName().isPrefixOf(name) || name.isPrefixOf(packageLayer.getQName());
		
		if(scope.equals(Scope.TYPE_SCOPE)) 
			return packageLayer.getQName().isPrefixOf(getTypePackageName());
			
		if(scope.equals(Scope.METHOD_SCOPE)) 
			return packageLayer.getQName().isPrefixOf(getMethodPackageName());
		
		return false;
	}

	@Override
	public boolean matchMember(Member member) {
		if(scope.equals(Scope.ROOT_SCOPE)) 
			return member.getParentPackageTree().getQName().equals(name);
		
		if(scope.equals(Scope.PACKAGE_SCOPE))
			return member.getParentPackageLayer().getQName().equals(name);
	
		if(scope.equals(Scope.PREFIX_SCOPE)) 
			return name.isPrefixOf(member.getQName());
		
		//methods/fields/inner classes are also members, should be included
		if(scope.equals(Scope.TYPE_SCOPE))
			return name.isPrefixOf(member.getQName());
		
		if(scope.equals(Scope.METHOD_SCOPE)) 
			//prefix parent members have to be included to preserve hierarchy
			//unwanted inner members will have to be filtered out at the reference level
			return getTypePackageName().isPrefixOf(member.getQName());
			
		return false;
	}

	//do the actual work here, other methods only attempt to filter out unwanted elements beforehand
	@Override
	public boolean matchRef(Ref ref) {
		if(scope.equals(Scope.ROOT_SCOPE)) 
			return ref.getParentPackageTree().getQName().equals(name);
	
		if(scope.equals(Scope.PACKAGE_SCOPE))
			return ref.getParentPackageLayer().getQName().equals(name);
		
		if(scope.equals(Scope.PREFIX_SCOPE)) 
			return name.isPrefixOf(ref.getQName());
		
		if(scope.equals(Scope.TYPE_SCOPE))
			return name.isPrefixOf(ref.getQName());
		
		if(scope.equals(Scope.METHOD_SCOPE)) 
			return name.isPrefixOf(ref.getQName());
		
		return false;
	}
	
	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public QName getQName() {
		return name;
	}

	private void setQName(QName name) {
		this.name = name;
	}

	@Override
	public String getNameString() {
		return getQName().toString();
	}

	@Override
	public String getScopeString() {
		return getScope().toString();
	}

	@Override
	public String getTagString() {
		return (hasTag() ? getTag() : "");
	}
	
	
}
