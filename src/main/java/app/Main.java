/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Tree[] trees = new Tree[400];
        for (int i = 0; i < trees.length; i++) {
            trees[i] = new Tree();
            trees[i].generate(3, 200, TreeType.IRREGULAR);
        }

        trees[0].draw();

    }
}
