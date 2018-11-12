/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        Tree[] trees = new Tree[400];


       // Tree tree = new Tree();
        for (int i = 0; i < trees.length; i++) {
            trees[i] = new Tree();
            trees[i].generate(3, 50, TreeType.IRREGULAR);
        }

        // среднее арифметическое
        double alpha = Arrays.stream(trees).mapToDouble(Tree::alpha).sum()/trees.length;
        System.out.println(alpha);

       // System.out.println(tree.toString());
        //System.out.println(tree.size());
        trees[0].draw();
        trees[0].showHistogram();
        //System.out.println(tree.alpha());

    }
}
