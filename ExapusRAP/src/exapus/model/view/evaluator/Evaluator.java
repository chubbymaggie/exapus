package exapus.model.view.evaluator;

import exapus.model.forest.FactForest;
import exapus.model.metrics.MetricType;
import exapus.model.stats.StatsCollectionVisitor;
import exapus.model.tags.TagsPropagationVisitor;
import exapus.model.view.View;
import exapus.model.visitors.ICopyingForestVisitor;

public abstract class Evaluator {

	private View view;
	
	protected FactForest result;

	protected abstract void cleanResult();
	
	public FactForest getResult() {
		return result;
	}
			
	public static Evaluator forView(View v) {
		if(v.isAPICentric())
			return new APICentricEvaluator(v);
		if(v.isProjectCentric())
			return new ProjectCentricEvaluator(v);
		return null;
	}
	
	public static FactForest evaluate(View v) {
		Evaluator e = forView(v);
		e.evaluate();
		return e.getResult();
	}
	
	protected Evaluator(View v) {
		view = v;
		cleanResult();
	}

	public View getView() {
		return view;
	}

	public void evaluate() {
        System.err.println("Evaluating view " + getView().getName());
        long startTime = System.currentTimeMillis();

        FactForest forest = fetchForest();
		calculateMetrics(forest);
        propagateTags(forest);
		result = forest;

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.err.printf("Spent on %s: %d s\n", getView().getName(), elapsedTime / 1000);
    }

    private FactForest fetchForest() {
        long startTime = System.currentTimeMillis();

        ICopyingForestVisitor v = newVisitor();
        FactForest dualForest = getDualSourceForest();
        v.setDualForest(dualForest);
        FactForest sourceForest = getSourceForest();

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.err.printf("\tFetching the forest: %d ms\n", elapsedTime);

        return v.copy(sourceForest);
    }

    private void propagateTags(FactForest forest) {
        long startTime = System.currentTimeMillis();

        forest.acceptVisitor(new TagsPropagationVisitor(getView()));

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.err.printf("\tTags propagation: %d ms\n", elapsedTime);
    }

    protected abstract ICopyingForestVisitor newVisitor();

	protected abstract FactForest getCompleteForest();
	
	protected abstract FactForest getDualCompleteForest();
	
	protected FactForest getSourceForest() {
		View sourceView= getView().getSourceView();
		if(sourceView != null)
			return sourceView.evaluate();
		return getCompleteForest();
	}
	
	protected FactForest getDualSourceForest() {
		View sourceView= getView().getDualSourceView();
		if(sourceView != null)
			return sourceView.evaluate();
		return getDualCompleteForest();	
	}
	
	protected void calculateMetrics(FactForest forest) {
		calculateMetrics(getView(), forest);
	}
	
    protected void calculateMetrics(View view, FactForest forest) {
    	MetricType type = view.getMetricType();
        if (type != null) {
            long startTime = System.currentTimeMillis();

            if (type == MetricType.ALL) {
                for (MetricType metric : MetricType.supportedMetrics(view.getRenderable())) {
                    if (metric == MetricType.ALL) continue;
                    forest.acceptVisitor(metric.getVisitor(view));
                }
            } else {
                forest.acceptVisitor(type.getVisitor(view));
            }

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.err.printf("\tMetric calculation: %d ms\n", elapsedTime);

            calculateStats(view, forest);
        }
    }

    private void calculateStats(View view, FactForest forest) {
        long startTime = System.currentTimeMillis();

        forest.acceptVisitor(new StatsCollectionVisitor(view));

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.err.printf("\tStats calculation: %d ms\n", elapsedTime);
    }

}
