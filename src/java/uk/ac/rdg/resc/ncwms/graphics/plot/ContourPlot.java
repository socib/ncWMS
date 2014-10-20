package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Class to draw contour plots in a graphics context.
 * 
 * This class implements the contour algorithm used by Octave ([1]).
 * 
 * Given a grid of data values and a level, the algorithm starts by marking
 * all the edges of the grid that intersect the horizontal plane at that level,
 * and then builds each contour line by joining the intersections at edges of
 * neighboring cells.
 * 
 * For efficiency, the marks of the edges of each cell are stored as 
 * the bit-wise combination of four flags (S, E, N, and W) in a member array
 * which is allocated on construction and reused for each level.
 * The indices of the vertices of the grid and the cell edges are related
 * as in this diagram:
 *                  
 *       (i+1,j  )-----(i+1,j+1)
 *           |      N      |
 *           |             |
 *           | W         E |
 *           |             |
 *           |      S      |
 *       (i  ,j  )-----(i  ,j+1)
 *
 *   [1]: <http://hg.savannah.gnu.org/hgweb/octave/file/cac9d4c49522/libinterp/corefcn/__contourc__.cc`>
 */
public class ContourPlot {
    
    private static byte S = (1 << 0);
    private static byte E = (1 << 1);
    private static byte N = (1 << 2);
    private static byte W = (1 << 3);
    private byte[] marks;

    private Map<Float, ArrayList<ArrayList<Point2D.Float>>> contours;
    private List<Shape> labels;
    private Color color;
    private Stroke stroke;
    private NumberFormat textFormat;
    private Font textFont;
    private Color textColor;
    private Stroke textStroke;
    private float textSpacing;
    private float textPadding;
    
    /**
     * Create a contour plot composed of contour lines at given levels.
     * 
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param levels the levels of the contours.
     * @param color the color of the contour lines.
     * @param stroke the stroke of the contour lines.
     * @param textFormat the numeric formatter for labels, or null.
     * @param textFont the labels' font.
     * @param textColor the color of the outline of the labels, or null.
     * @param textStroke the stroke of the outline of the labels, or null.
     * @param textSpacing the label space step for labels on the same contour.
     * @param textPadding the label padding in font units.
     * @param textClip the region that will be effectively drawn, or null.
     */
    public ContourPlot(float[] xpnts, float[] ypnts, float[] cpnts,
                       int rows, int cols, float[] levels,
                       Color color, Stroke stroke,
                       NumberFormat textFormat, Font textFont,
                       Color textColor, Stroke textStroke,
                       float textSpacing, float textPadding)
    {
        this.color = color;
        this.stroke = stroke;
        this.textFormat = textFormat;
        this.textFont = textFont;
        this.textColor = textColor;
        this.textStroke = textStroke;
        this.textSpacing = textSpacing;
        this.textPadding = textPadding;
        this.marks = new byte[(rows - 1) * (cols - 1)];
        this.contours = new HashMap<Float, ArrayList<ArrayList<Point2D.Float>>>(levels.length, 1.0f);
        for (float level : levels)
            this.contours.put(level, contour(xpnts, ypnts, cpnts, rows, cols, level));
        this.labels = null;
    }
    
    
    /**
     * Draw the contour plot in the graphics context.
     * 
     * @param g2 the graphics context to draw in.
     * @param transform the transform defining the axis placement in the context.
     */
    public void draw(Graphics2D g2, AffineTransform transform)
    {
        Color g2color = g2.getColor();
        if (stroke != null)
            g2.setStroke(stroke);
        if (color != null)
            g2.setColor(color);
        
        for (ArrayList<ArrayList<Point2D.Float>> paths : contours.values())
            for (ArrayList<Point2D.Float> points : paths)
            {
                Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO, points.size());
                Point2D.Float point = new Point2D.Float();
                Iterator<Point2D.Float> iter = points.iterator();
                point.setLocation(iter.next());
                if (transform != null)
                    transform.transform(point, point);
                path.moveTo(point.x, point.y);
                while(iter.hasNext())
                {
                    point.setLocation(iter.next());
                    if (transform != null)
                        transform.transform(point, point);
                    path.lineTo(point.x, point.y);
                }
                g2.draw(path);
            }
        
        if (labels == null && textFormat != null && textFont != null)
            labels = label(contours, textFormat, textSpacing, textPadding, textFont,
                           transform, g2.getClip(), g2.getFontRenderContext());
        if (labels != null)
        {
            for (Shape label : labels)
            {
                if (textStroke != null)
                {
                    g2.setStroke(textStroke);
                    if (color == null)
                        g2.setColor(g2color);
                    else
                        g2.setColor(color);
                    g2.draw(label);
                }
                if (textColor != null)
                    g2.setColor(textColor);
                g2.fill(label);
            }
        }
    }
    
    
    /**
     * Build a list of contour lines for a level.
     * 
     * Mark the edges that intersect the level plane, and follow the contours
     * starting at each vertical and horizontal edge.
     * 
     * Contours starting at an interior edge and interrupted at a cell with 
     * invalid values (NaN) are not closed. To return them as a single line,
     * contour lines starting at both faces of the same edge (between 
     * neighboring cells) are joined together. Thus, the returned paths might
     * not be composed of a sequence of line segments.
     *  
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param level the level value.
     * @return the list of contour lines (paths) for that level.
     */
    private ArrayList<ArrayList<Point2D.Float>> contour(float[] xpnts, float[] ypnts, float[] cpnts,
                                                        int rows, int cols, float level)
    {
        ArrayList<ArrayList<Point2D.Float>> contour = new ArrayList<ArrayList<Point2D.Float>>();
        ArrayList<Point2D.Float> path, thap;
        mark(cpnts, rows, cols, level);
        // Find contours that start at a horizontal edge.
        for (int j = 0; j < cols - 1; j++)
        {
            path = follow(xpnts, ypnts, cpnts, rows, cols, level, 0, j, S);
            if (path != null)
                contour.add(path);
            path = follow(xpnts, ypnts, cpnts, rows, cols, level, rows - 2, j, N);
            if (path != null)
                contour.add(path);
            for (int i = 1; i < rows - 1; i++)
            {
                thap = follow(xpnts, ypnts, cpnts, rows, cols, level, i - 1, j, N);
                path = follow(xpnts, ypnts, cpnts, rows, cols, level, i    , j, S);
                if (thap != null && path != null)
                    contour.add(join(thap, path));
                else if (thap != null)
                    contour.add(thap);
                else if (path != null)
                    contour.add(path);
            }
        }
        // Find contours that start at a vertical edge.
        for (int i = 0; i < rows - 1; i++)
        {
            path = follow(xpnts, ypnts, cpnts, rows, cols, level, i, 0, W);
            if (path != null)
                contour.add(path);
            path = follow(xpnts, ypnts, cpnts, rows, cols, level, i, cols - 2, E);
            if (path != null)
                contour.add(path);
            for (int j = 1; j < cols - 1; j++)
            {
                thap = follow(xpnts, ypnts, cpnts, rows, cols, level, i, j - 1, E);
                path = follow(xpnts, ypnts, cpnts, rows, cols, level, i, j    , W);
                if (thap != null && path != null)
                    contour.add(join(thap, path));
                else if (thap != null)
                    contour.add(thap);
                else if (path != null)
                    contour.add(path);
            }
        }
        return contour;
    }
    
    
    /**
     * Mark the edges of the grid that intersect the level plane.
     * 
     * To mark each edge of the grid, we use an array with a flag for each cell.
     * The flag contains a bit-wise combination of flags S, W, E and N, 
     * indicating which edges of the cell intersect the level plane.
     * For efficiency, the flags are kept in the private member `marks`,
     * which is updated by this method.
     * 
     * @param cpnts the values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param level the level value.
     */
    private void mark(float[] cpnts, int rows, int cols, float level)
    {
        int index;
        float fsw, fse, fnw, fne;
        float EPSILON = 1.1920929E-7f;
        for (int i = 0; i < rows - 1; i++)
            for (int j = 0; j < cols - 1; j++)
            {
                index = (cols-1) * i + j;
                fsw = cpnts[cols *  i    + j  ] - level;
                fse = cpnts[cols *  i    + j+1] - level;
                fne = cpnts[cols * (i+1) + j+1] - level;
                fnw = cpnts[cols * (i+1) + j  ] - level;
                if (Math.abs(fsw) < EPSILON) fsw = EPSILON;
                if (Math.abs(fse) < EPSILON) fse = EPSILON;
                if (Math.abs(fne) < EPSILON) fne = EPSILON;
                if (Math.abs(fnw) < EPSILON) fnw = EPSILON;
                marks[index] = 0;
                if (fsw * fse < 0)
                    marks[index] |= S;
                if (fse * fne < 0)
                    marks[index] |= E;
                if (fne * fnw < 0)
                    marks[index] |= N;
                if (fnw * fsw < 0)
                    marks[index] |= W;
            }
    }

    /**
     * Follow the contour line starting at the given cell and edge, if any.
     * 
     * Given (the indices of) a cell and an optional edge, follow the contour
     * line starting at that cell and edge. If the edge is unknown, `A`,
     * search the starting edge among the cell edges (S, E, N, W order).
     * If the given starting edge is not marked, or there are no marked edges
     * for that cell, (either because they do not intersect the level plane
     * or because they have been already visited), return null.
     * Otherwise, follow the contour line throw the edges of neighboring cells
     * and return the resulting sequence of points.
     *  
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param level the level value.
     * @param i row index of the cell the contour starts at.
     * @param j column index of the cell the contour starts at.
     * @param edge edge of the cell the contour starts at.
     * @return the sequence of points of the contour line, of null if there is no such line.
     */
    private ArrayList<Point2D.Float> follow(float[] xpnts, float[] ypnts, float[] cpnts,
                                            int rows, int cols, float level,
                                            int i, int j, byte edge)
    {

        int k, k0, k1;
        boolean still;
        float t, x, y;

        k = (cols - 1) * i + j;
        if ((marks[k] & edge) == 0)
            return null;

        ArrayList<Point2D.Float> path = new ArrayList<Point2D.Float>();
        if (edge == S) {
            k0 = cols *  i + j;
            k1 = cols *  i + j+1;
        } else if (edge == E) {
            k0 = cols *  i    + j+1;
            k1 = cols * (i+1) + j+1;
        } else if (edge == N) {
            k0 = cols * (i+1) + j+1;
            k1 = cols * (i+1) + j;
        } else {
            k0 = cols * (i+1) + j;
            k1 = cols *  i    + j;
        }
        // The uncommented lines are the ones used in the original code,
        // and they are equivalent but much more robust.
        // t = (level - cpnts[k0]) / (cpnts[k1] - cpnts[k0]);
        // x = (1.0f - t) * xpnts[k0] + t * xpnts[k1];
        // y = (1.0f - t) * ypnts[k0] + t * ypnts[k1];
        t = (cpnts[k1] - level) / (cpnts[k0] - level);
        x = xpnts[k0] + (xpnts[k1] - xpnts[k0]) / (1.0f - t);
        y = ypnts[k0] + (ypnts[k1] - ypnts[k0]) / (1.0f - t);
        path.add(new Point2D.Float(x, y));
        
        for (;
             (0 <= i) && (i < rows - 1) && (0 <= j) && (j < cols - 1) && marks[k] != 0;
             k = (cols - 1) * i + j)
        {
            marks[k] &= ~edge;
            if (edge == S) {
                edge = E;
                edge = ((marks[k] & edge) == 0) ? N : edge;
                edge = ((marks[k] & edge) == 0) ? W : edge;
                edge = ((marks[k] & edge) == 0) ? S : edge;
            } else if (edge == E) {
                edge = S;
                edge = ((marks[k] & edge) == 0) ? W : edge;
                edge = ((marks[k] & edge) == 0) ? N : edge;
                edge = ((marks[k] & edge) == 0) ? E : edge;
            } else if (edge == N) {
                edge = W;
                edge = ((marks[k] & edge) == 0) ? S : edge;
                edge = ((marks[k] & edge) == 0) ? E : edge;
                edge = ((marks[k] & edge) == 0) ? N : edge;
            } else {
                edge = N;
                edge = ((marks[k] & edge) == 0) ? E : edge;
                edge = ((marks[k] & edge) == 0) ? S : edge;
                edge = ((marks[k] & edge) == 0) ? W : edge;
            }
            still = ((marks[k] & edge) == 0);
            marks[k] &= ~edge;
            if (edge == S) {
                k0 = cols * i    + j;
                k1 = cols * i    + j+1;
                i--;
                edge = N;
            } else if (edge == E) {
                k0 = cols *  i    + j+1;
                k1 = cols * (i+1) + j+1;
                j++;
                edge = W;
            } else if (edge == N) {
                k0 = cols * (i+1) + j+1;
                k1 = cols * (i+1) + j;
                i++;
                edge = S;
            } else {
                k0 = cols * (i+1) + j;
                k1 = cols *  i    + j;
                j--;
                edge = E;
            }
            if (! still) {
                t = (cpnts[k1] - level) / (cpnts[k0] - level);
                x = xpnts[k0] + (xpnts[k1] - xpnts[k0]) / (1.0f - t);
                y = ypnts[k0] + (ypnts[k1] - ypnts[k0]) / (1.0f - t);
                path.add(new Point2D.Float(x, y));
            }
        }
        return path;
    }
    
    /**
     * Compose a line by joining two segments starting at a common point.
     * 
     * The head of the first segment is assumed to be equal to the head
     * of the last segment. The first segment is reversed and the common 
     * is not duplicated.
     * 
     * @param thap the first segment.
     * @param path the second segment.
     * @return the first segment reversed plus the second segment.
     */
    private static ArrayList<Point2D.Float> join(ArrayList<Point2D.Float> thap,
                                                 ArrayList<Point2D.Float> path)
    {
        final int m = thap.size();
        final int n = path.size();
        ArrayList<Point2D.Float> joined = new ArrayList<Point2D.Float>(m + n - 1);
        for (int i = m - 1; i > 0; i--)
            joined.add(thap.get(i));
        for (int i = 0; i < n; i++)
            joined.add(path.get(i));
        return joined;
    }

    /**
     * Compute the labels for the contour levels.
     * 
     * The labels are computed so that they do not overlap.
     * This requires a graphics context to compute the metrics of the labels,
     * and the transform that will scale them in the draw operation, if any.
     * Also, a clip region may prevent the location of labels so close to the 
     * edges of the image that they would be cut when drawn.
     * 
     * @param contours the map of level to corresponding contour lines.
     * @param padding the padding space around labels.
     * @param spacing the spacing between consecutive labels on the same contour lines.
     * @param formatter the formatter to convert the level value to text.
     * @param font the font to use for the text label.
     * @param frc the font rendering context for metrics computation.
     * @param transform the transform that will scale the font when drawn, or null.
     * @param clip the clip region that will be effectively drawn, or null.
     * @return the list of labels as outlines (shapes) to draw.
     */
    private List<Shape> label(Map<Float, ArrayList<ArrayList<Point2D.Float>>> contours,
                              NumberFormat formatter, float spacing, float padding,
                              Font font, AffineTransform transform, Shape clip, FontRenderContext frc)
    {
        float level;
        ArrayList<ArrayList<Point2D.Float>> paths;
        String text;
        double radius;
        TextLayout layout;
        Rectangle2D bounds, padded, inset;
        Shape border, extent;
        Area localCovering, totalCovering;
        Point2D point;
        AffineTransform locate;
        point = new Point2D.Float();
        locate = new AffineTransform();
        totalCovering = new Area();
        ArrayList<Shape> outlines = new ArrayList<Shape>();
        for (Entry<Float, ArrayList<ArrayList<Point2D.Float>>> entry : contours.entrySet())
        {
            level = entry.getKey();
            paths = entry.getValue();
            text = formatter.format(level);
            layout = new TextLayout(text, font, frc);
            bounds = layout.getBounds();
            padded = new Rectangle2D.Double(
                    - 0.5 * bounds.getWidth() - padding, - 0.5 * bounds.getHeight() - padding,
                    bounds.getWidth()  + 2.0 * padding, bounds.getHeight() + 2.0 * padding);
            radius = spacing * Math.max(padded.getWidth(), padded.getHeight());
            border = new Ellipse2D.Double(- radius, - radius, 2.0 * radius, 2.0 * radius);
            for (ArrayList<Point2D.Float> points : paths)
            {
                localCovering = null;
                for(Iterator<Point2D.Float> iter = points.iterator(); iter.hasNext();)
                {
                    point.setLocation(iter.next());
                    if (transform != null)
                        transform.transform(point, point);
                    locate.setToTranslation(point.getX(), point.getY());
                    inset = locate.createTransformedShape(padded).getBounds2D();
                    if (localCovering == null)
                    {
                        localCovering = new Area(inset);
                    }
                    else if (! localCovering.intersects(inset))
                    {
                        if ((clip == null || clip.contains(inset))
                             && ! totalCovering.intersects(inset))
                        {
                            extent = locate.createTransformedShape(border);
                            localCovering.add(new Area(extent));
                            totalCovering.add(new Area(inset));
                            locate.translate(-bounds.getCenterX(), -bounds.getCenterY());
                            outlines.add(layout.getOutline(locate));
                        }
                    }
                }
            }
        }
        return outlines;
    }
}
