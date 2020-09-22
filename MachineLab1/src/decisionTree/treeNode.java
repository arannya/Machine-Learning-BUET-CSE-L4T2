package decisionTree;

import java.util.ArrayList;
import java.util.LinkedList;

public class treeNode {

	treeNode parent =null;
	 ArrayList<treeNode> children;
	int attrValue=-1;
	int attrNum=-1;
	int decisionValue=-1;
	public treeNode(treeNode parent, ArrayList<treeNode>child,int attr, int val,int Value) {
		super();
		this.children=new  ArrayList<treeNode>();
		this.parent = parent;
		
		this.children=child;
		this.attrNum=attr;
		this.attrValue = val;
		this.decisionValue=Value;
	}
	
}
