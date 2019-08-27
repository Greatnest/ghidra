/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package docking.widgets.tree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.junit.*;

import docking.test.AbstractDockingTest;
import docking.widgets.OptionDialog;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;

public class GTreeSlowLoadingNode1Test extends AbstractDockingTest {

	private static final int MAX_DEPTH = 4;
	private static final int MIN_CHILD_COUNT = 3;
	private static final int MAX_CHILD_COUNT = 40;

	private volatile boolean pauseChildLoading = false;

	private JFrame frame;
	private GTree gTree;

	@Before
	public void setUp() throws Exception {

		gTree = new GTree(new EmptyRootNode());

		frame = new JFrame("GTree Test");
		frame.getContentPane().add(gTree);
		frame.setSize(400, 400);
		frame.setVisible(true);

		waitForTree();
	}

	@After
	public void tearDown() throws Exception {
		gTree.dispose();
		frame.dispose();
	}

	@Test
	public void testBasicLoading() {
		gTree.setRootNode(new TestRootNode(100));

		waitForTree();

		// make sure we have some children
		GTreeRootNode rootNode = gTree.getRootNode();
		GTreeNode nonLeaf1 = rootNode.getChild(0);
		assertNotNull(nonLeaf1);
		GTreeNode leaf1 = rootNode.getChild(1);
		assertNotNull(leaf1);
		GTreeNode nonLeaf2 = rootNode.getChild(2);
		assertNotNull(nonLeaf2);

		int childCount = nonLeaf1.getChildCount();
		assertTrue("Did not find children for: " + nonLeaf1, childCount > 1);
		assertEquals("An expected leaf node has some children", 0, leaf1.getChildCount());
		childCount = nonLeaf2.getChildCount();
		assertTrue("Did not find children for: " + nonLeaf2, childCount > 1);
	}

	@Test
	public void testSlowNodeShowsProgressBar() {
		gTree.setRootNode(new TestRootNode(5000));

		waitForTree();

		GTreeRootNode rootNode = gTree.getRootNode();
		GTreeNode nonLeaf1 = rootNode.getChild(0);
		assertNotNull(nonLeaf1);

		gTree.expandPath(nonLeaf1);

		assertProgressPanel(true);

		assertTrue(nonLeaf1.isInProgress());

		// Press the cancel button on the progress monitor
		pressProgressPanelCancelButton();

		waitForTree();

		// Verify no progress component
		assertProgressPanel(false);
	}

//==================================================================================================
// Private Methods
//==================================================================================================	

	private void waitForTree() {
		waitForTree(gTree);
	}

	private void assertProgressPanel(boolean isShowing) {
		JComponent panel = (JComponent) getInstanceField("progressPanel", gTree);
		if (!isShowing) {
			assertNull("Panel is showing when it should not be", panel);
			return;
		}

		if (panel == null || !panel.isShowing()) {
			int maxWaits = 50;// wait a couple seconds, as the progress bar may be delayed
			int tryCount = 0;
			while (tryCount < maxWaits) {
				panel = (JComponent) getInstanceField("progressPanel", gTree);
				if (panel != null && panel.isShowing()) {
					return;// finally showing!
				}
				tryCount++;
				try {
					Thread.sleep(50);
				}
				catch (Exception e) {
					// who cares?
				}
			}
		}

		Assert.fail("Progress panel is not showing as expected");
	}

	private void pressProgressPanelCancelButton() {
		Object taskMonitorComponent = getInstanceField("monitor", gTree);
		final JButton cancelButton =
			(JButton) getInstanceField("cancelButton", taskMonitorComponent);
		runSwing(() -> cancelButton.doClick(), false);

		OptionDialog confirDialog = waitForDialogComponent(frame, OptionDialog.class, 2000);
		final JButton confirmCancelButton = findButtonByText(confirDialog, "Yes");
		runSwing(() -> confirmCancelButton.doClick());
	}

//==================================================================================================
// Inner Classes
//==================================================================================================

	private class EmptyRootNode extends AbstractGTreeRootNode {

		EmptyRootNode() {
			setChildren(new ArrayList<GTreeNode>());
		}

		@Override
		public Icon getIcon(boolean expanded) {
			return null;
		}

		@Override
		public String getName() {
			return "Empty Test GTree Root Node";
		}

		@Override
		public String getToolTip() {
			return null;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}
	}

	private class TestRootNode extends AbstractGTreeRootNode {

		TestRootNode(int loadDelayMillis) {
			List<GTreeNode> children = new ArrayList<>();
			children.add(new TestSlowLoadingNode(loadDelayMillis, 1));
			children.add(new TestLeafNode());
			children.add(new TestSlowLoadingNode(loadDelayMillis, 1));
			setChildren(children);
		}

		@Override
		public Icon getIcon(boolean expanded) {
			return null;
		}

		@Override
		public String getName() {
			return "Test GTree Root Node";
		}

		@Override
		public String getToolTip() {
			return null;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}
	}

	private class TestSlowLoadingNode extends GTreeSlowLoadingNode {

		private final long loadDelayMillis;
		private final int depth;

		TestSlowLoadingNode(long loadDelayMillis, int depth) {
			this.loadDelayMillis = loadDelayMillis;
			this.depth = depth;
		}

		@Override
		public List<GTreeNode> generateChildren(TaskMonitor monitor) throws CancelledException {

			if (depth > MAX_DEPTH) {
				return new ArrayList<>();
			}

			if (monitor.isCancelled()) {
				return new ArrayList<>();
			}

			sleep(loadDelayMillis);

			while (pauseChildLoading) {
				sleep(100);
			}

			int childCount = getRandomInt(MIN_CHILD_COUNT, MAX_CHILD_COUNT);
			List<GTreeNode> children = new ArrayList<>();
			for (int i = 0; i < childCount; i++) {
				if (monitor.isCancelled()) {
					return new ArrayList<>();
				}
				int value = getRandomInt(0, 1);
				if (value == 0) {
					children.add(new TestSlowLoadingNode(loadDelayMillis, depth + 1));
				}
				else {
					children.add(new TestLeafNode());
				}
			}
			return children;
		}

		@Override
		public Icon getIcon(boolean expanded) {
			return null;
		}

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public String getToolTip() {
			return null;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

	}

	private class TestLeafNode extends AbstractGTreeNode {

		private String name = getClass().getSimpleName() + getRandomString();

		@Override
		public Icon getIcon(boolean expanded) {
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getToolTip() {
			return null;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}

	}
}
