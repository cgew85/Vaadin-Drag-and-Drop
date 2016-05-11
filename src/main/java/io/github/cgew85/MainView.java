package io.github.cgew85;

import com.vaadin.data.Container;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ClientSideCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cgew85 on 06.05.2016.
 */
@SpringView(name = MainView.VIEW_NAME)
@ViewScope
public class MainView extends HorizontalLayout implements View {

    static final String VIEW_NAME = "MainView";

    private Tree treeA;
    private Tree treeB;
    private Tree treeC;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
    }

    @PostConstruct
    public void init() {
        setMargin(true);
        setSpacing(true);
        setSizeFull();

        addComponent(getTreeA());
        addComponent(getTreeB());
        addComponent(getTreeC());

        initializeTreeA();
        initializeTreeB();
        initializeTreeC(new SourceIs(getTreeA(), getTreeB()));
    }

    private void initializeTreeA() {
        final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();
        treeA.setContainerDataSource(hierarchicalContainer);
        treeA.addContainerProperty("text", String.class, "");
        treeA.addContainerProperty("groupName", String.class, "");
        treeA.setItemCaptionPropertyId("text");
        final List<GroupOfItems> listOfGroups = new ArrayList<>();
        final GroupOfItems groupOfItemsA = new GroupOfItems("Group A", new ArrayList<Item>(){{
            add(new Item("Item A"));
            add(new Item("Item B"));
        }});
        final GroupOfItems groupOfItemsB = new GroupOfItems("Group B", new ArrayList<Item>(){{
            add(new Item("Item C"));
            add(new Item("Item D"));
        }});

        listOfGroups.add(groupOfItemsA);
        listOfGroups.add(groupOfItemsB);

        listOfGroups.forEach(groupOfItems -> setupContainer(groupOfItems, hierarchicalContainer, treeA));
    }

    private void initializeTreeB() {
        final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();
        treeB.setContainerDataSource(hierarchicalContainer);
        treeB.addContainerProperty("text", String.class, "");
        treeB.addContainerProperty("groupName", String.class, "");
        treeB.setItemCaptionPropertyId("text");
        final List<GroupOfItems> listOfGroups = new ArrayList<>();
        final GroupOfItems groupOfItemsC = new GroupOfItems("Group C", new ArrayList<Item>(){{
            add(new Item("Item E"));
            add(new Item("Item F"));
        }});
        final GroupOfItems groupOfItemsD = new GroupOfItems("Group D", new ArrayList<Item>(){{
            add(new Item("Item G"));
            add(new Item("Item H"));
        }});

        listOfGroups.add(groupOfItemsC);
        listOfGroups.add(groupOfItemsD);

        listOfGroups.forEach(groupOfItems -> setupContainer(groupOfItems, hierarchicalContainer, treeB));
    }

    private void initializeTreeC(final ClientSideCriterion clientSideCriterion) {
        final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();
        treeC.setContainerDataSource(hierarchicalContainer);
        treeC.addContainerProperty("text", String.class, "");
        treeC.addContainerProperty("groupName", String.class, "");
        treeC.setItemCaptionPropertyId("text");

        // Add an empty group
        hierarchicalContainer.addItem("defaultGroup");
        hierarchicalContainer.getItem("defaultGroup").getItemProperty("text").setValue("Drag and drop items here");
        hierarchicalContainer.setChildrenAllowed("defaultGroup", true);
        treeC.expandItem("defaultGroup");

        treeC.setDropHandler(new DropHandler() {
            @Override
            public void drop(DragAndDropEvent dragAndDropEvent) {
                final DataBoundTransferable dataBoundTransferable = (DataBoundTransferable) dragAndDropEvent.getTransferable();
                // Check if source is of the same container kind as the target
                if(!(dataBoundTransferable.getSourceContainer() instanceof Container.Hierarchical)) return;
                // Get the source container
                final HierarchicalContainer sourceContainer = (HierarchicalContainer) dataBoundTransferable.getSourceContainer();
                // The item that was dragged into the target
                final Object sourceItemId = dataBoundTransferable.getItemId();
                String text = sourceContainer.getItem(sourceItemId).getItemProperty("text").getValue().toString();
                // The parent item id of the item that was dragged
                final Object parentItemId = sourceContainer.getParent(sourceItemId);
                //Get further information about the drop event
                final AbstractSelect.AbstractSelectTargetDetails dropData = ((AbstractSelect.AbstractSelectTargetDetails) dragAndDropEvent.getTargetDetails());
                final VerticalDropLocation verticalDropLocation = dropData.getDropLocation();
                if(verticalDropLocation == VerticalDropLocation.TOP || verticalDropLocation == VerticalDropLocation.BOTTOM) {
                    // Case: Something was dropped above order under an item
                    // Check if item is a group or an item
                    if(isItemAGroup(sourceContainer, sourceItemId)) {
                        // Item is a group
                        // Check if group is already present in the target tree
                        if(isGroupAlreadyPresent(sourceContainer, hierarchicalContainer, sourceItemId)) {
                            // Group is already present in the tree
                            // Add items as children of that group
                            final Optional<Object> targetParentItemId = getParentItemIdForGivenItem(hierarchicalContainer, sourceContainer, sourceItemId);
                            if(targetParentItemId.isPresent()) {
                                for(Object sourceChildItemId : sourceContainer.getChildren(sourceItemId)) {
                                    final Object newItemId = hierarchicalContainer.addItem();
                                    final String sourceText = sourceContainer.getItem(sourceChildItemId).getItemProperty("text").getValue().toString();
                                    final String sourceGroupName = sourceContainer.getItem(sourceChildItemId).getItemProperty("groupName").getValue().toString();
                                    hierarchicalContainer.getItem(newItemId).getItemProperty("text").setValue(sourceText);
                                    hierarchicalContainer.getItem(newItemId).getItemProperty("groupName").setValue(sourceGroupName);
                                    hierarchicalContainer.setChildrenAllowed(newItemId, false);
                                    hierarchicalContainer.setParent(newItemId, targetParentItemId.get());
                                }
                            } else {
                                Notification.show("Error");
                            }
                        } else {
                            // Group is not present in the tree
                            final Object newGroupId = addGroupToTargetContainer(hierarchicalContainer, sourceContainer, sourceItemId);
                            treeC.expandItem(newGroupId);
                        }
                    } else {
                        // Item is an item
                    }

                } else if(verticalDropLocation == VerticalDropLocation.MIDDLE) {
                    // Case: Something was dropped on an item
                    List<String> listOfStringsAlreadyInsideTheContainer = new ArrayList<>();
                    // Terrible way to determine duplicates btw
                    listOfStringsAlreadyInsideTheContainer.addAll(hierarchicalContainer.getItemIds().stream().map(itemId -> hierarchicalContainer.getItem(itemId).getItemProperty("text").getValue().toString() + hierarchicalContainer.getItem(itemId).getItemProperty("groupName").getValue().toString()).collect(Collectors.toList()));
                    if(Objects.isNull(parentItemId)) {
                        // Item is a node itself
                        final Collection<?> children = sourceContainer.getChildren(sourceItemId);
                        if(!Objects.isNull(children)) {
                            children.forEach(child -> {
                                String childText = sourceContainer.getItem(child).getItemProperty("text").getValue().toString();
                                String groupName = (String) sourceItemId;
                                if(listOfStringsAlreadyInsideTheContainer.contains(childText + groupName)) return;
                                Object itemId = hierarchicalContainer.addItem();
                                if(!hierarchicalContainer.areChildrenAllowed(dropData.getItemIdOver())) {
                                    // Case: Items aren't allowed to be dropped as children, put them in standard group
                                    hierarchicalContainer.setParent(itemId, "defaultGroup");
                                    hierarchicalContainer.getItem(itemId).getItemProperty("text").setValue(childText);
                                    hierarchicalContainer.getItem(itemId).getItemProperty("groupName").setValue(groupName);
                                    hierarchicalContainer.setChildrenAllowed(itemId, false);
                                } else {
                                    // Case: Items are allowed to be dropped as children
                                    hierarchicalContainer.setParent(itemId, dropData.getItemIdOver());
                                    hierarchicalContainer.getItem(itemId).getItemProperty("text").setValue(childText);
                                    hierarchicalContainer.getItem(itemId).getItemProperty("groupName").setValue(groupName);
                                    hierarchicalContainer.setChildrenAllowed(itemId, false);
                                }
                            });
                        }
                    } else {
                        // Item is just an item
                        if(listOfStringsAlreadyInsideTheContainer.contains(text + parentItemId)) return;
                        Object itemId = hierarchicalContainer.addItem();
                        if(hierarchicalContainer.areChildrenAllowed(dropData.getItemIdOver())) {
                            // Case: Items are allowed to be dropped as children
                            hierarchicalContainer.setParent(itemId, dropData.getItemIdOver());
                            hierarchicalContainer.getItem(itemId).getItemProperty("text").setValue(text);
                            hierarchicalContainer.getItem(itemId).getItemProperty("groupName").setValue(parentItemId);
                            hierarchicalContainer.setChildrenAllowed(itemId, false);
                        } else {
                            // Case: Items aren't allowed to be dropped as children, put them in standard group
                            hierarchicalContainer.setParent(itemId, "defaultGroup");
                            hierarchicalContainer.getItem(itemId).getItemProperty("text").setValue(text);
                            hierarchicalContainer.getItem(itemId).getItemProperty("groupName").setValue(parentItemId);
                            hierarchicalContainer.setChildrenAllowed(itemId, false);
                        }
                    }
                }

                // Remove crap
                List<Object> itemIdsToRemove = hierarchicalContainer.rootItemIds().stream().filter(itemId -> !hierarchicalContainer.hasChildren(itemId)).collect(Collectors.toList());
                itemIdsToRemove.forEach(itemId -> hierarchicalContainer.removeItem(itemId));
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
//                return new And(clientSideCriterion, Tree.TargetItemAllowsChildren.get(), AbstractSelect.AcceptItem.ALL);
                return AcceptAll.get();
            }
        });
    }

    /**
     * Add a group from a source container to a target container.
     *
     * @param targetContainer
     * @param sourceContainer
     * @param sourceItemId
     * @return Object itemId of the group item
     */
    private Object addGroupToTargetContainer(final Container.Hierarchical targetContainer, final Container.Hierarchical sourceContainer, final Object sourceItemId) {
        final List<Item> listOfItems = new ArrayList<>();
        final String targetGroupName = sourceContainer.getItem(sourceItemId).getItemProperty("text").getValue().toString();
        for(Object childItemId : sourceContainer.getChildren(sourceItemId)) {
            String childText = sourceContainer.getItem(childItemId).getItemProperty("text").getValue().toString();
            listOfItems.add(new Item(childText));
        }

        // Add new group to target tree
        final Object parentItemId = targetContainer.addItem();
        targetContainer.getItem(parentItemId).getItemProperty("text").setValue(targetGroupName);
        targetContainer.setChildrenAllowed(parentItemId, true);

        // Add items to created group
        for(Item item : listOfItems) {
            Object newItemId = targetContainer.addItem();
            targetContainer.setParent(newItemId, parentItemId);
            targetContainer.setChildrenAllowed(newItemId, false);
            targetContainer.getItem(newItemId).getItemProperty("text").setValue(item.getText());
            targetContainer.getItem(newItemId).getItemProperty("groupName").setValue(targetGroupName);
        }

        return parentItemId;
    }

    private Optional<Object> getParentItemIdForGivenItem(final Container.Hierarchical targetContainer, final Container.Hierarchical sourceContainer, final Object sourceItemId) {
        final String sourceParentGroupName = sourceContainer.getItem(sourceItemId).getItemProperty("text").getValue().toString();
        for(Object targetParentItemId : targetContainer.rootItemIds()) {
            if(targetContainer.getItem(targetParentItemId).getItemProperty("text").getValue().toString().equals(sourceParentGroupName)) {
                return Optional.of(targetContainer.getItem(targetParentItemId));
            }
        }
        return Optional.empty();
    }

    /**
     * If true the source item doesn't have a parent item, so it is a node itself.
     * If false the source item has a parent and is therefore a child element of a node.
     *
     * @param sourceContainer
     * @param sourceItemId
     * @return
     */
    private boolean isItemAGroup(final Container.Hierarchical sourceContainer, final Object sourceItemId) {
        return Objects.isNull(sourceContainer.getParent(sourceItemId));
    }

    /**
     * If true the group is already in the target list.
     *
     * @param targetContainer
     * @param sourceItemId
     * @return
     */
    private boolean isGroupAlreadyPresent(final Container.Hierarchical sourceContainer, final Container.Hierarchical targetContainer, final Object sourceItemId) {
        for(Object rootItemId : targetContainer.rootItemIds()) {
            String targetContainerGroupName = targetContainer.getItem(rootItemId).getItemProperty("text").getValue().toString();
            String sourceItemGroupName = sourceContainer.getItem(sourceItemId).getItemProperty("text").getValue().toString();
            if(targetContainerGroupName.equals(sourceItemGroupName)) return true;
        }

        return false;
    }

    private void setupContainer(GroupOfItems groupOfItems, HierarchicalContainer hierarchicalContainer, Tree tree) {
        final String groupName = groupOfItems.getGroupName();
        hierarchicalContainer.addItem(groupName);
        hierarchicalContainer.getItem(groupName).getItemProperty("text").setValue(groupName);
        if(groupOfItems.getListOfItems().size() == 0) {
            // Case: Node has no child elements
            hierarchicalContainer.setChildrenAllowed(groupName, false);
        } else {
            // Case: Node has child elements
            groupOfItems.getListOfItems().forEach(item -> {
                String itemName = item.getText();
                hierarchicalContainer.addItem(itemName);
                hierarchicalContainer.getItem(itemName).getItemProperty("text").setValue(itemName);
                hierarchicalContainer.setParent(itemName, groupName);
                hierarchicalContainer.setChildrenAllowed(itemName, false);
            });

            tree.expandItemsRecursively(groupName);
        }
    }

    @AllArgsConstructor
    private class Item {

        @Getter
        @Setter
        private String text;
    }

    @AllArgsConstructor
    private class GroupOfItems {

        @Getter
        @Setter
        private String groupName;
        @Getter
        @Setter
        private List<Item> listOfItems;

    }

    private Tree getTreeA() {
        if(Objects.isNull(treeA)) {
            treeA = new Tree("Tree A");
            treeA.setSelectable(false);
            treeA.setDragMode(Tree.TreeDragMode.NODE);
        }

        return treeA;
    }

    private Tree getTreeB() {
        if(Objects.isNull(treeB)) {
            treeB = new Tree("Tree B");
            treeB.setSelectable(false);
            treeB.setDragMode(Tree.TreeDragMode.NODE);
        }

        return treeB;
    }

    private Tree getTreeC() {
        if(Objects.isNull(treeC)) {
            treeC = new Tree("Tree C");
            treeC.setSelectable(false);
            treeC.setDragMode(Tree.TreeDragMode.NODE);
        }

        return treeC;
    }
}
