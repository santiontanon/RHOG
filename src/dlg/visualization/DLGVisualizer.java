package dlg.visualization;


import dlg.core.DLG;
import dlg.core.TreeDLG;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import dlg.util.Label;


public class DLGVisualizer extends Display {
    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";


    public static JFrame newWindow(String name,int dx,int dy,DLG g) throws Exception {
        DLGVisualizer ad = new DLGVisualizer(dx,dy,g);
        JFrame frame = new JFrame(name);
        frame.getContentPane().add(ad);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        return frame;
    }


    public DLGVisualizer(int dx,int dy,DLG g) throws Exception {
        // initialize display and data
        super(new Visualization());
        initDataGroups(g);

        // set up the renderers
        // draw the nodes as basic shapes
//        Renderer nodeR = new ShapeRenderer(20);
        LabelRenderer nodeR = new LabelRenderer("name");
        nodeR.setHorizontalPadding(4);
        nodeR.setVerticalPadding(2);
        nodeR.setRoundedCorner(8, 8); // round the corners
        EdgeRenderer edgeR = new LabelEdgeRenderer(Constants.EDGE_TYPE_LINE,Constants.EDGE_ARROW_FORWARD);
        edgeR.setArrowHeadSize(6,6);
        
        // draw aggregates as polygons with curved edges
        PolygonRenderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
        polyR.setCurveSlack(0.15f);

        DefaultRendererFactory drf = new DefaultRendererFactory(nodeR,edgeR);
        drf.add("ingroup('aggregates')", polyR);
        m_vis.setRendererFactory(drf);

        // set up the visual operators
        // first set up all the color actions
        ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
        nFill.setDefaultColor(ColorLib.gray(255));
        nFill.add("_hover", ColorLib.gray(200));

        ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
        nEdges.setDefaultColor(ColorLib.gray(100));

        // bundle the color actions
        ActionList colors = new ActionList();
        colors.add(nFill);
        colors.add(nEdges);

        // now create the main layout routine
        ActionList layout = new ActionList(Activity.INFINITY);
        ForceDirectedLayout fdl = new ForceDirectedLayout(GRAPH, true);
        ForceSimulator m_fsim = new ForceSimulator();
        m_fsim.addForce(new NBodyForce());
        if (g instanceof TreeDLG) {
            m_fsim.addForce(new SpringForce(1E-4f,100));
        } else {
            m_fsim.addForce(new SpringForce(1E-4f,200));
        }
        m_fsim.addForce(new DragForce());
        fdl.setForceSimulator(m_fsim);

        layout.add(colors);
        layout.add(fdl);
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        // set up the display
        setSize(dx,dy);
        pan(250, 250);
        setHighQuality(true);
        addControlListener(new AggregateDragControl());
        addControlListener(new ZoomControl());
        addControlListener(new PanControl());

//      ActionList draw = new ActionList();
//      draw.add(new GraphDistanceFilter(GRAPH, 50));
//      m_vis.putAction("draw", draw);


        // set things running
        m_vis.run("layout");
    }

    
    private void initDataGroups(DLG dlg) throws Exception {
        Graph g = new Graph(true);
        
        g.addColumn("name", String.class);

        // Create vertices:
        for(int i = 0;i<dlg.getNVertices();i++) g.addNode();

        // Create edges:
        for(int i = 0;i<dlg.getNVertices();i++) {
            for(int j = 0;j<dlg.getNVertices();j++) {
                if (dlg.getEdge(i, j)!=null) {
                    Node n1 = g.getNode(i);
                    Node n2 = g.getNode(j);

                    Edge e = g.addEdge(n1, n2);
                    e.set("name",dlg.getEdge(i, j).get());
                }
            }
        }

        VisualGraph vg = m_vis.addGraph(GRAPH, g);
        
        // Set labels:
        Iterator vertex_iterator = vg.nodes();
        for(int i = 0;i<dlg.getNVertices();i++) {
            Label label = dlg.getVertex(i);
            VisualItem vi = (VisualItem)vertex_iterator.next();
            vi.set("name", "V" + i +": " + label);
            vi.setStroke(new BasicStroke(2.0f));
            vi.setStrokeColor(ColorLib.rgb(0,0,0));
            vi.set(VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0));
        }

        // Set colors:
        Iterator edge_iterator = vg.edges();
        while(edge_iterator.hasNext()) {
            VisualItem vi = (VisualItem)edge_iterator.next();
            vi.setFillColor(ColorLib.gray(0));
            vi.setTextColor(ColorLib.rgb(0,128,0));
        }
        m_vis.setInteractive(EDGES, null, false);

    }


} // end of class FTVisualizer

/*
class AggregateLayout extends Layout {

    private int m_margin = 5; // convex hull pixel margin
    private double[] m_pts;   // buffer for computing convex hulls

    public AggregateLayout(String aggrGroup) {
        super(aggrGroup);
    }

    public void run(double frac) {

        AggregateTable aggr = (AggregateTable)m_vis.getGroup(m_group);
        // do we have any  to process?
        int num = aggr.getTupleCount();
        if ( num == 0 ) return;

        // update buffers
        int maxsz = 0;
        for ( Iterator aggrs = aggr.tuples(); aggrs.hasNext();  )
            maxsz = Math.max(maxsz, 4*2*
                    ((AggregateItem)aggrs.next()).getAggregateSize());
        if ( m_pts == null || maxsz > m_pts.length ) {
            m_pts = new double[maxsz];
        }

        // compute and assign convex hull for each aggregate
        Iterator aggrs = m_vis.visibleItems(m_group);
        while ( aggrs.hasNext() ) {
            AggregateItem aitem = (AggregateItem)aggrs.next();

            int idx = 0;
            if ( aitem.getAggregateSize() == 0 ) continue;
            VisualItem item = null;
            Iterator iter = aitem.items();
            while ( iter.hasNext() ) {
                item = (VisualItem)iter.next();
                if ( item.isVisible() ) {
                    addPoint(m_pts, idx, item, m_margin);
                    idx += 2*4;
                }
            }
            // if no aggregates are visible, do nothing
            if ( idx == 0 ) continue;

            // compute convex hull
            double[] nhull = GraphicsLib.convexHull(m_pts, idx);

            // prepare viz attribute array
            float[]  fhull = (float[])aitem.get(VisualItem.POLYGON);
            if ( fhull == null || fhull.length < nhull.length )
                fhull = new float[nhull.length];
            else if ( fhull.length > nhull.length )
                fhull[nhull.length] = Float.NaN;

            // copy hull values
            for ( int j=0; j<nhull.length; j++ )
                fhull[j] = (float)nhull[j];
            aitem.set(VisualItem.POLYGON, fhull);
            aitem.setValidated(false); // force invalidation
        }
    }

    private static void addPoint(double[] pts, int idx,
                                 VisualItem item, int growth)
    {
        Rectangle2D b = item.getBounds();
        double minX = (b.getMinX())-growth, minY = (b.getMinY())-growth;
        double maxX = (b.getMaxX())+growth, maxY = (b.getMaxY())+growth;
        pts[idx]   = minX; pts[idx+1] = minY;
        pts[idx+2] = minX; pts[idx+3] = maxY;
        pts[idx+4] = maxX; pts[idx+5] = minY;
        pts[idx+6] = maxX; pts[idx+7] = maxY;
    }

} // end of class AggregateLayout
*/

class AggregateDragControl extends ControlAdapter {

    private VisualItem activeItem;
    protected Point2D down = new Point2D.Double();
    protected Point2D temp = new Point2D.Double();
    protected boolean dragged;

    public AggregateDragControl() {
    }

    public void itemEntered(VisualItem item, MouseEvent e) {
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        if ( !(item instanceof AggregateItem) )
            setFixed(item, true);
    }

    public void itemExited(VisualItem item, MouseEvent e) {
        if ( activeItem == item ) {
            activeItem = null;
            setFixed(item, false);
        }
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getDefaultCursor());
    }

    public void itemPressed(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = false;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), down);
        if ( item instanceof AggregateItem )
            setFixed(item, true);
    }

    public void itemReleased(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        if ( dragged ) {
            activeItem = null;
            setFixed(item, false);
            dragged = false;
        }
    }

    public void itemDragged(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = true;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX()-down.getX();
        double dy = temp.getY()-down.getY();

        move(item, dx, dy);

        down.setLocation(temp);
    }

    protected static void setFixed(VisualItem item, boolean fixed) {
        if ( item instanceof AggregateItem ) {
            Iterator items = ((AggregateItem)item).items();
            while ( items.hasNext() ) {
                setFixed((VisualItem)items.next(), fixed);
            }
        } else {
            item.setFixed(fixed);
        }
    }

    protected static void move(VisualItem item, double dx, double dy) {
        if ( item instanceof AggregateItem ) {
            Iterator items = ((AggregateItem)item).items();
            while ( items.hasNext() ) {
                move((VisualItem)items.next(), dx, dy);
            }
        } else {
            double x = item.getX();
            double y = item.getY();
            item.setStartX(x);  item.setStartY(y);
            item.setX(x+dx);    item.setY(y+dy);
            item.setEndX(x+dx); item.setEndY(y+dy);
        }
    }
} // end of class AggregateDragControl

