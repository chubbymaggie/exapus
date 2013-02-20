package exapus.model.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

import exapus.model.details.GraphDetails;
import exapus.model.forest.FactForest;
import exapus.model.metrics.MetricType;
import exapus.model.store.Store;
import exapus.model.view.evaluator.Evaluator;
import exapus.model.view.graphbuilder.ForestGraph;
import exapus.model.view.graphbuilder.GraphBuilder;
import exapus.model.view.graphdrawer.GraphDrawer;

@XmlRootElement(name = "view")
public class View {
	
	public View() {
		//only to be used by jaxb
	}

	public View(String n, Perspective p) {
		name = n;
		perspective = p;
		apiselection = new ArrayList<Selection>();
		projectselection = new ArrayList<Selection>();
		renderable = false;
        metricType = MetricType.defaultValue(getRenderable());
        graphDetails = GraphDetails.defaultValue();
        sealed = false;
	}
	
	
	private FactForest forest = null;
	
	private File graph = null;
	
	private String sourceViewName = null;

	private boolean renderable;

    private GraphDetails graphDetails;
	
	private Perspective perspective;
	
    @XmlElementWrapper(name="APISelection")
    @XmlElements({ @XmlElement(name="Universal", type=UniversalSelection.class), 
    	@XmlElement(name="ScopedSelection", type=ScopedSelection.class)})
	private List<Selection> apiselection;
	
    @XmlElementWrapper(name="ProjectSelection")
    @XmlElements({ @XmlElement(name="Universal", type=UniversalSelection.class), 
    	@XmlElement(name="ScopedSelection", type=ScopedSelection.class)})
	private List<Selection> projectselection;

    private MetricType metricType;
	
	private String name;
	
	private boolean sealed;
	
	public void seal() {
		sealed = true;
	}

	public void unseal() {
		sealed = false;
	}
	
	public boolean sealed() {
		return sealed;
	}
	
	@XmlElement
	public String getSourceViewName() {
		return sourceViewName;
	}
	
	public void setSourceViewName(String n) {
		if(Objects.equal(this.sourceViewName, n))
			return;
		this.sourceViewName = n;
		makeDirty();
    }
	
	@XmlElement
	public Perspective getPerspective() {
		return perspective;
	}

	public boolean getRenderable() {
		return renderable;
	}
	
	public void setRenderable(boolean renderable) {
		if(renderable != this.renderable) {
			this.renderable = renderable;
			makeDirty();

            if (!MetricType.supportsMetric(this.renderable, getMetricType())) {
                this.metricType = MetricType.defaultValue(this.renderable);
            }
		}
	}
	
	public void setPerspective(Perspective perspective) {
		if(perspective != this.perspective) {
			this.perspective = perspective;
			makeDirty();
		}
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
		makeDependentViewsDirty();
	}
	
	protected void makeDependentViewsDirty() {
		for(View v : Store.getCurrent().getRegisteredViews()) 
			if(name.equals(v.getSourceViewName())) {
				v.makeDirty();
            }
	}

	@XmlElement
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
    	if(!metricType.equals(this.metricType)) {
            this.metricType = metricType;
            makeDirty();
    	}
    }

    public GraphDetails getGraphDetails() {
        return graphDetails;
    }

    public void setGraphDetails(GraphDetails graphDetails) {
        if (this.graphDetails != graphDetails) {
            this.graphDetails = graphDetails;
            System.err.println("setGraphDetails");
            makeDirty();
        }
    }

    public String toString() {
		return name;
	}
	
	public boolean isAPICentric() {
		return perspective == Perspective.API_CENTRIC;
	}
	
	public boolean isProjectCentric() {
		return perspective == Perspective.PROJECT_CENTRIC;
	}
	
	
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

	public static View fromView(View original) {
		View duplicate = new View("Copy of " + original.getName(), original.getPerspective());
		duplicate.setRenderable(original.getRenderable());
		for(Selection sel : original.getAPISelections())
			duplicate.addAPISelection(Selection.fromSelection(sel));
		for(Selection sel : original.getProjectSelections())
			duplicate.addProjectSelection(Selection.fromSelection(sel));
		duplicate.setMetricType(original.getMetricType());
		duplicate.setSourceViewName(original.getSourceViewName());
		return duplicate;
	}

	public File toFile() throws Exception {
		 ViewWriter viewWriter = new ViewWriter(this);
		 return viewWriter.writeTemporary();
	}
	
}
