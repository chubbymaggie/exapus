package exapus.gui.editors.view.definition;

import java.util.ArrayList;
import java.util.LinkedList;

import exapus.model.details.GraphDetails;
import exapus.model.metrics.MetricType;
import exapus.model.view.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import exapus.gui.editors.view.IViewEditorPage;
import exapus.gui.editors.view.ViewEditor;
import exapus.gui.views.store.StoreListContentProvider;
import exapus.model.store.Store;

//todo: implement view change listener, also in other editor parts
public class ViewDefinitionEditor extends EditorPart implements IViewEditorPage{

	private ComboViewer comboVWPerspective;
	private Button checkRenderable;
	private TableViewer tableVWAPI;
	private TableViewer tableVWProjects;
	private ViewEditor viewEditor;
    private ComboViewer comboMetrics;
    private ComboViewer comboGraphDetails;
	private ComboViewer comboVWSource;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@SuppressWarnings("serial")
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(3, false));

		//Perspective
		Label lblPerspective = new Label(parent, SWT.NONE);
		GridData gd_lblPerspective = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		lblPerspective.setLayoutData(gd_lblPerspective);
		lblPerspective.setText("Perspective:");

		comboVWPerspective = new ComboViewer(parent, SWT.READ_ONLY);
		comboVWPerspective.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		comboVWPerspective.setContentProvider(ArrayContentProvider.getInstance());
		comboVWPerspective.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object selected = selection.getFirstElement();
				if(selected instanceof Perspective) {
					getView().setPerspective((Perspective)selected);
					updateComboVWSource();
				}
			}
		});

		//Source view
		Label lblSource = new Label(parent, SWT.NONE);
		lblSource.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSource.setText("Source:");

		comboVWSource = new ComboViewer(parent, SWT.READ_ONLY);
		comboVWSource.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		comboVWSource.setContentProvider(ArrayContentProvider.getInstance());
	
		comboVWSource.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object selected = selection.getFirstElement();
				if(selected instanceof String) {
					String selectedSourceName = (String) selected;
					if(selectedSourceName.equals(getWorkspaceSourceName()))
						getView().setSourceViewName(null);
					else
						getView().setSourceViewName(selectedSourceName);
				}
			}
		});

	
        //APIs
		Label lblAPILabel = new Label(parent, SWT.NONE);
		lblAPILabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lblAPILabel.setText("APIs:");

		tableVWAPI = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL);
		ToolBar toolbarAPI = new ToolBar(parent, SWT.VERTICAL);
		configureSelectionTableAndToolBar(tableVWAPI, toolbarAPI, Perspective.API_CENTRIC);

		//Projects
		Label lblProjectsLabel = new Label(parent, SWT.NONE);
		lblProjectsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lblProjectsLabel.setText("Projects:");

		tableVWProjects = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL);
		ToolBar toolbarProjects = new ToolBar(parent, SWT.VERTICAL);
		configureSelectionTableAndToolBar(tableVWProjects, toolbarProjects, Perspective.PROJECT_CENTRIC);

        // MetricType
        Label lblMetrics = new Label(parent, SWT.NONE);
        lblMetrics.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
        lblMetrics.setText("Metrics:");

        comboMetrics = new ComboViewer(parent, SWT.READ_ONLY);
        comboMetrics.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboMetrics.setContentProvider(ArrayContentProvider.getInstance());
        comboMetrics.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object selected = selection.getFirstElement();
                if (selected instanceof MetricType) {
                    getView().setMetricType((MetricType) selected);
                }
            }
        });
        
        //Graph details
        Label lblGraphDetails = new Label(parent, SWT.NONE);
        lblGraphDetails.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
        lblGraphDetails.setText("Graph details:");

        comboGraphDetails = new ComboViewer(parent, SWT.READ_ONLY);
        comboGraphDetails.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboGraphDetails.setContentProvider(ArrayContentProvider.getInstance());
        comboGraphDetails.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object selected = selection.getFirstElement();
                if (selected instanceof GraphDetails) {
                    getView().setGraphDetails((GraphDetails) selected);
                }
            }
        });
        
		//Renderable
		Label lblRenderable = new Label(parent, SWT.NONE);
		GridData gd_lblRenderable = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		lblRenderable.setLayoutData(gd_lblRenderable);

		checkRenderable = new Button(parent, SWT.CHECK);
		GridData gd_checkRenderable = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		checkRenderable.setLayoutData(gd_checkRenderable);
		checkRenderable.setText("Render as graph.");

		checkRenderable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getView().setRenderable(checkRenderable.getSelection());
                comboMetrics.setInput(MetricType.supportedMetrics(checkRenderable.getSelection()));
                comboMetrics.setSelection(new StructuredSelection(MetricType.defaultValue(checkRenderable.getSelection())));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		comboVWPerspective.setInput(Perspective.supportedPerspectives());
        comboGraphDetails.setInput(GraphDetails.supportedDetails());
        comboMetrics.setInput(MetricType.supportedMetrics(checkRenderable.getSelection()));


    }

    private void configureSelectionTableAndToolBar(final TableViewer tableVW, ToolBar toolbar, final Perspective perspective) {
		Table tableAPI = tableVW.getTable();
		GridData gd_tableAPI = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_tableAPI.heightHint = tableAPI.getItemHeight() * 4;
		tableAPI.setLayoutData(gd_tableAPI);
		//tableAPI.setHeaderVisible(true);
		tableVW.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn APISelCol = new TableViewerColumn(tableVW, SWT.NONE);
		APISelCol.getColumn().setText("Name");
		APISelCol.getColumn().setWidth(250);

		APISelCol.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Selection sel = (Selection) cell.getElement();
				cell.setText(sel.getNameString());
			}
		});
		TableViewerColumn APIScopeCol = new TableViewerColumn(tableVW, SWT.NONE);
		APIScopeCol.getColumn().setText("Scope");
		APIScopeCol.getColumn() .setWidth(150);
		APIScopeCol.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Selection sel = (Selection) cell.getElement();
				cell.setText(sel.getScopeString());
			}
		});

		if(perspective.equals(Perspective.API_CENTRIC)) {
			TableViewerColumn APITagCol = new TableViewerColumn(tableVW, SWT.NONE);
			APITagCol.getColumn().setText("Tag");
			APITagCol.getColumn() .setWidth(150);
			APITagCol.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(ViewerCell cell) {
					Selection sel = (Selection) cell.getElement();
					cell.setText(sel.getTagString());
				}
			});
		}


	    ToolItem toolItemAddAPI = new ToolItem(toolbar, SWT.PUSH);
	    toolItemAddAPI.setText("Add");
	    toolItemAddAPI.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected( final SelectionEvent event ) {
	    		showSelectionDialog(perspective);
	    	}
	    });

	    /*
	    final ToolItem toolItemEditAPI = new ToolItem(toolbar, SWT.PUSH);
	    toolItemEditAPI.setEnabled(false);
	    toolItemEditAPI.setText("Edit");
	    */

	    final ToolItem toolItemDeleteAPI = new ToolItem(toolbar, SWT.PUSH);
	    toolItemDeleteAPI.setEnabled(false);
	    toolItemDeleteAPI.setText("Delete");
	    toolItemDeleteAPI.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected( final SelectionEvent event ) {
	    		IStructuredSelection sel = (IStructuredSelection) tableVW.getSelection();
	    		if(sel.isEmpty())
	    			return;
	    		Selection selectedSelection = (Selection) sel.getFirstElement();
		    	if(perspective.equals(Perspective.API_CENTRIC))
		    		getView().removeAPISelection(selectedSelection);
		    	if(perspective.equals(Perspective.PROJECT_CENTRIC))
		    		getView().removeProjectSelection(selectedSelection);
		    	updateControls();
	    	}
	    });


	    tableVW.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				boolean enabled = !event.getSelection().isEmpty();
				//toolItemEditAPI.setEnabled(enabled);
				toolItemDeleteAPI.setEnabled(enabled);
			}
		});
	}

	protected void showSelectionDialog(Perspective perspective) {
		SelectionDialog selectionDialog  = new SelectionDialog(getSite().getShell(), perspective);
		int returnCode = selectionDialog.open();
		if(returnCode == IDialogConstants.OK_ID) {
			Selection newSelection = selectionDialog.getSelection();
			if(newSelection != null) {
				if(perspective.equals(Perspective.API_CENTRIC))
					getView().addAPISelection(newSelection);
				if(perspective.equals(Perspective.PROJECT_CENTRIC))
					getView().addProjectSelection(newSelection);
			}}
		updateControls();
	}

	private View getView() {
		return Store.getCurrent().getView(getEditorInput().getName());

	}

	@Override
	public void setFocus() {
		updateControls();
	}


	public void updateControls() {
		View view = getView();
		comboVWPerspective.setSelection(new StructuredSelection(view.getPerspective()));
		updateComboVWSource();
		checkRenderable.setSelection(view.getRenderable());
		tableVWAPI.setInput(Iterables.toArray(view.getAPISelections(),Object.class));
		tableVWProjects.setInput(Iterables.toArray(view.getProjectSelections(),Object.class));
        comboMetrics.setSelection(new StructuredSelection(view.getMetricType()));
        comboGraphDetails.setSelection(new StructuredSelection(view.getGraphDetails()));
	}


	private String getWorkspaceSourceName() {
		return getView().isAPICentric() ? "Workspace Packages" : "Workspace Projects";
	}
	
	private ArrayList<String> viewSourceNames() {
		ArrayList<String> elements = new ArrayList<String>();
		View thisView = getView();
		Perspective thisPerspective = thisView.getPerspective();
		for(View v : Store.getCurrent().getRegisteredViews()) {
			if(v.getPerspective().equals(thisPerspective)
				&& !v.equals(thisView))
				elements.add(v.getName());
		}
		elements.add(getWorkspaceSourceName());
		return elements;
	}
	
	private void updateComboVWSource() {
		comboVWSource.setInput(viewSourceNames());
		String sourceViewName = getView().getSourceViewName();
		comboVWSource.setSelection(new StructuredSelection(sourceViewName == null ? getWorkspaceSourceName() : sourceViewName));
	}

	public void setViewEditor(ViewEditor viewEditor) {
		this.viewEditor = viewEditor;
	}

}
