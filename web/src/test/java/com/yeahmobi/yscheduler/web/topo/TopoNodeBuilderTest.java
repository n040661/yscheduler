// package com.yeahmobi.yscheduler.web.topo;
//
// import java.util.Arrays;
//
// import org.junit.Assert;
// import org.junit.Test;
//
// import com.yeahmobi.yscheduler.web.controller.topo.TopoNode;
// import com.yeahmobi.yscheduler.web.controller.topo.TopoTreeManager;
//
// public class TopoNodeBuilderTest {
//
// TopoTreeManager topoNodeManager = new TopoTreeManager();
//
// @Test
// public void testTag() {
// TopoNode root = build();
// this.topoNodeManager.tag(root);
// Assert.assertEquals(root.getColumn(), 3);
//
// }
//
// private void assertTree(TopoNode root) {
// TopoNode node1 = root;
// TopoNode node3 = new TopoNode();
// TopoNode node4 = new TopoNode();
// TopoNode node5 = new TopoNode();
// TopoNode node6 = new TopoNode();
// TopoNode node7 = new TopoNode();
// }
//
// private TopoNode build() {
// TopoNode node1 = new TopoNode();
// TopoNode node2 = new TopoNode();
// TopoNode node3 = new TopoNode();
// TopoNode node4 = new TopoNode();
// TopoNode node5 = new TopoNode();
// TopoNode node6 = new TopoNode();
// TopoNode node7 = new TopoNode();
//
// node1.setNodes(Arrays.asList(node2, node3));
// node2.setNodes(Arrays.asList(node4));
// node4.setNodes(Arrays.asList(node5, node6, node7));
//
// return node1;
//
// }
// }
