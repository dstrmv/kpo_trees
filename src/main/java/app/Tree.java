package app;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import javax.naming.OperationNotSupportedException;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

//import org.graphstream.graph.Graph;
//import org.graphstream.graph.Node;
//import org.graphstream.graph.implementations.*;
//import org.graphstream.ui.layout.HierarchicalLayout;
//import org.graphstream.ui.view.Viewer;

public class Tree {

    private List<Node> nodes;
    private int maxChildNum = 0;
    private double[] alphaOnEachStep;


    public Tree() {
        nodes = new ArrayList<>();
    }

    public int size() {
        return nodes.size();
    }

    void generate(int maxChildNum, int nodesNum, TreeType type) {
        this.maxChildNum = maxChildNum;
        alphaOnEachStep = new double[nodesNum];
        Random random = new Random();

        do {
            this.clear();
            int current = 0;

            while (size() < nodesNum) {

                if (size() == 0) {
                    nodes.add(new Node(null));
                    alphaOnEachStep[0] = 1;
                }

                if (current == size()) {
                    break;
                }

                int childNum = 0;
                if (type == TreeType.IRREGULAR) {
                    childNum = random.nextInt(maxChildNum);
                } else if (type == TreeType.REGULAR) {
                    childNum = maxChildNum - 1;
                }
                Node buffer = nodes.get(current);
                for (int i = 0; i < childNum; i++) {
                    if (size() >= nodesNum) {
                        return;
                    }
                    Node child = new Node(buffer);
                    buffer.addChildren(new Node(buffer));

                    nodes.add(child);
                    alphaOnEachStep[size() - 1] = alpha();
                }
                current++;
            }
        } while (size() < nodesNum);
    }

    void clear() {
        nodes.clear();
        Arrays.fill(alphaOnEachStep, 0);
    }

    public double[] getAlphaOnEachStep() {
        return alphaOnEachStep;
    }

    public double expectedVertexValue() {

        double expVal = 0;
        int[] children = getChildrenAmount();
        return IntStream.range(0, maxChildNum)
                .mapToDouble(num -> num * (double) children[num] / size())
                .sum();
    }

    public String getSuspendedNodes () {
        StringBuilder sb = new StringBuilder();
        for (Node n: nodes) {
            if (!n.hasChildren()) {
                sb.append(n).append("\n");
            }
        }
        return sb.toString();
    }

    public double disperse() {

        int[] children = getChildrenAmount();
        double x2 = IntStream.range(0, maxChildNum).mapToDouble(num -> (double) num * num * children[num] / size()).sum();

        return x2 - Math.pow(expectedVertexValue(), 2);
    }


    public int suspendedNodesNum() {
        if (size() == 0) {
            throw new IllegalStateException("tree is empty");
        }

        return (int) nodes.stream().filter(node -> !node.hasChildren()).count();
    }

    public int maxLevel() {
        if (nodes.size() == 0) {
            throw new IllegalStateException("tree is empty");
        }
        return nodes.get(nodes.size() - 1).getLevel();
    }

    public double alpha() {
        // (m - 1) / (m - 2);
        // m = 3: alpha = 2
        return size() / (double) suspendedNodesNum();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Node n : nodes) {
            sb.append(n.toString()).append("\n");
        }
        return sb.toString();
    }

    public void draw() {
        this.draw(size());
    }

    public void draw(int numOfVertex) {

        if (numOfVertex <= 0 || numOfVertex > size()) {
            throw new IllegalArgumentException("number of vertex cant'be zero or greater than tree size");
        }

        DelegateTree<Integer, String> visualTree;
        visualTree = new DelegateTree<>();
        visualTree.setRoot(nodes.get(0).num);

        for (int i = 1; i < size(); i++) {
            visualTree.addChild(String.valueOf(i), nodes.get(i).parentNum, nodes.get(i).num);
        }

        Layout<Integer, String> layout = new TreeLayout<>(visualTree, 30, 30);
        // layout.setSize(new Dimension(300, 300));

        VisualizationViewer<Integer, String> vv = new VisualizationViewer<>(layout);
        //BasicVisualizationServer<Integer, String> vv = new BasicVisualizationServer<>(layout);
        vv.setPreferredSize(new Dimension(800, 600));

        vv.getRenderContext().setVertexFillPaintTransformer(i -> !nodes.get(i - 1).children.isEmpty() ? Color.GREEN : Color.RED);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(visualTree));
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);

        JFrame frame = new JFrame("Simple graph view");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }

    public int[] showHistogram() {

        int[] xarray = IntStream.range(0, maxChildNum).toArray();
        int[] yarray = getChildrenAmount();
        CategoryChart chart = new CategoryChartBuilder().width(640).height(480)
                .title("Гистограмма")
                .xAxisTitle("Число дочерних узлов")
                .yAxisTitle("Число встреченных узлов с данным количеством дочерних узлов")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setAvailableSpaceFill(0.99);
        chart.getStyler().setOverlapped(true);

        chart.addSeries("Число встреченных узлов", xarray, yarray);
        new SwingWrapper<>(chart).displayChart();

        return yarray;
    }

    public int[] getChildrenAmount() {
        int[] childrenAmount = new int[maxChildNum];
        for (Node n : nodes) {
            childrenAmount[n.children.size()]++;
        }
        return childrenAmount;
    }


    private class Node {

        private Node parent;
        private int num;
        private int parentNum;
        private List<Node> children;

        public Node(Node parent) {
            this.parent = parent;
            this.parentNum = parent == null ? 0 : parent.num;
            num = nodes.size() + 1;
            children = new ArrayList<>();
        }

        public void addChildren(Node child) {
            this.children.add(child);
        }

        public int getLevel() {
            if (this.parent == null) {
                return 1;
            } else {
                return this.parent.getLevel() + 1;
            }
        }

        private int childrenNum() {
            return children == null ? 0 : children.size();
        }

        private boolean hasChildren() {
            return childrenNum() != 0;
        }

        @Override
        public String toString() {
            return String.format("%d-%d level %d", num, parentNum, getLevel());
        }
    }

}
