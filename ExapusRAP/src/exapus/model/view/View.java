package exapus.model.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import exapus.model.forest.FactForest;
import exapus.model.view.evaluator.Evaluator;
import exapus.model.view.graphbuilder.ForestGraph;
import exapus.model.view.graphbuilder.GraphBuilder;
import exapus.model.view.graphdrawer.GraphDrawer;

public abstract class View {

	public View(String n, Perspective p) {
		name = n;
		perspective = p;
		apiselection = new ArrayList<Selection>();
		projectselection = new ArrayList<Selection>();
		renderable = true;
	}

	
	private FactForest forest = null;
	private File graph = null;
	
	private static 	Perspective[] supportedPerspectives = {Perspective.API_CENTRIC, Perspective.PROJECT_CENTRIC};

	public static Perspective[] supportedPerspectives() {
		return supportedPerspectives;
	}

	private boolean renderable;
	
	private Perspective perspective;
	
	private List<Selection> apiselection;
	
	private List<Selection> projectselection;
	
	private String name;

	public Perspective getPerspective() {
		return perspective;
	}

	public boolean getRenderable() {
		return renderable;
	}
	
	public void setRenderable(boolean renderable) {
		this.renderable = renderable;
		makeDirty();
	}
	
	public void setPerspective(Perspective perspective) {
		this.perspective = perspective;
		makeDirty();
	}

	public Iterable<Selection> getAPISelections() {
		return apiselection;
	}

	public void addAPISelection(Selection selection) {
		apiselection.add(selection);
		makeDirty();
	}
	
	public boolean removeAPISelection(Selection selection) {
		boolean result = apiselection.remove(selection);
		makeDirty();
		return result;
	}

	public Iterable<Selection> getProjectSelections() {
		return projectselection;
	}

	public void addProjectSelection(Selection selection) {
		projectselection.add(selection);
		makeDirty();
	}
	
	public boolean removeProjectSelection(Selection selection) {
		boolean result = projectselection.remove(selection);
		makeDirty();
		return result;
	}
	
	protected void makeDirty() {
		forest = null;
		graph = null;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	public abstract boolean isAPICentric();
	
	public abstract boolean isProjectCentric();
	
	
	private FactForest lazyEvaluate() {
		if(forest == null) 
			forest = Evaluator.evaluate(this);
		return forest;
	}
	
	public FactForest evaluate() {
		return lazyEvaluate();
	}
	
	public File draw() {
		return lazyDraw();
	}
	
	private File lazyDraw() {
		if(graph == null) {
			FactForest forest = evaluate();
			ForestGraph fg = GraphBuilder.forView(this).build(forest);
			try {
				graph = GraphDrawer.forView(this).draw(fg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return graph;
	}
	
	
}
