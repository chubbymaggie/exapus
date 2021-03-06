package exapus.model.view.graphdrawer;

import java.io.File;
import java.io.IOException;

import exapus.gui.editors.forest.graph.GraphViz;
import exapus.gui.editors.forest.graph.IEdgeFormatter;
import exapus.gui.editors.forest.graph.IGraphFormatter;
import exapus.gui.editors.forest.graph.INodeFormatter;
import exapus.model.view.View;
import exapus.model.view.graphbuilder.ForestGraph;

public abstract class GraphDrawer {

    protected View view;

    protected GraphDrawer(View view) {
        this.view = view;
    }

    public static GraphDrawer forView(View v) {
		return new ContainmentGraphDrawer(v);
	}
	
	public File draw(ForestGraph g) throws IOException {
		 GraphViz gv = new GraphViz(g);
		 return gv.toImage(getGraphFormatter(), getNodeFormatter(), getEdgeFormatter());
	}

	protected abstract IGraphFormatter getGraphFormatter();
	
	protected abstract INodeFormatter getNodeFormatter();
	
	protected abstract IEdgeFormatter getEdgeFormatter();
		
}
