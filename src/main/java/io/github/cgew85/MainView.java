package io.github.cgew85;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.And;
import com.vaadin.event.dd.acceptcriteria.ClientSideCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by cgew85 on 06.05.2016.
 */

@SpringView(name = MainView.VIEW_NAME)
@ViewScope
public class MainView extends HorizontalLayout implements View {

    public static final String VIEW_NAME = "MainView";

    private Tree treeA;
    private Tree treeB;
    private Table table;

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
        addComponent(getTable());

        initializeTreeA(new SourceIs(getTable()));
        initializeTreeB(new SourceIs(getTable()));
        initializeTable(new SourceIs(getTreeA(), getTreeB()));
    }

    private void initializeTreeA(final ClientSideCriterion clientSideCriterion) {
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

        listOfGroups.forEach(groupOfItems -> {
            String groupName = groupOfItems.getGroupName();
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

                treeA.expandItemsRecursively(groupName);
            }

            treeA.setDropHandler(new DropHandler() {
                @Override
                public void drop(DragAndDropEvent dragAndDropEvent) {
                    final DataBoundTransferable dataBoundTransferable = (DataBoundTransferable) dragAndDropEvent.getTransferable();
                    final Container sourceContainer = dataBoundTransferable.getSourceContainer();
                    final Object sourceItemId = dataBoundTransferable.getItemId();
                    final com.vaadin.data.Item sourceItem = sourceContainer.getItem(sourceItemId);
                    final String groupName = (String) sourceItem.getItemProperty("groupName").getValue();
                    final String text = (String) sourceItem.getItemProperty("text").getValue();
                    final AbstractSelect.AbstractSelectTargetDetails dropData = ((AbstractSelect.AbstractSelectTargetDetails) dragAndDropEvent.getTargetDetails());
                    final Object targetItemId = dropData.getItemIdOver();
                    if(targetItemId != null && groupName != null && text != null) {
                        final String treeGroup = (String) targetItemId;
                        if(treeGroup.equals(groupName)) {
                            final Object newItemId = getTreeA().addItem();
                            getTreeA().getItem(newItemId).getItemProperty("text").setValue(text);
                            getTreeA().getItem(newItemId).getItemProperty("groupName").setValue(groupName);
                            getTreeA().setParent(newItemId, targetItemId);
                            getTreeA().setChildrenAllowed(newItemId, false);

                            sourceContainer.removeItem(sourceItemId);
                        } else {
                            final String message = text + " is not a " + treeGroup.toLowerCase().replaceAll("s$", "");
                            Notification.show(message, Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return new And(clientSideCriterion, Tree.TargetItemAllowsChildren.get(), AbstractSelect.AcceptItem.ALL);
                }
            });
        });
    }

    private void initializeTreeB(final ClientSideCriterion clientSideCriterion) {
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

        listOfGroups.forEach(groupOfItems -> {
            String groupName = groupOfItems.getGroupName();
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

                treeB.expandItemsRecursively(groupName);
            }

            treeB.setDropHandler(new DropHandler() {
                @Override
                public void drop(DragAndDropEvent dragAndDropEvent) {
                    final DataBoundTransferable dataBoundTransferable = (DataBoundTransferable) dragAndDropEvent.getTransferable();
                    final Container sourceContainer = dataBoundTransferable.getSourceContainer();
                    final Object sourceItemId = dataBoundTransferable.getItemId();
                    final com.vaadin.data.Item sourceItem = sourceContainer.getItem(sourceItemId);
                    final String groupName = (String) sourceItem.getItemProperty("groupName").getValue();
                    final String text = (String) sourceItem.getItemProperty("text").getValue();
                    final AbstractSelect.AbstractSelectTargetDetails dropData = ((AbstractSelect.AbstractSelectTargetDetails) dragAndDropEvent.getTargetDetails());
                    final Object targetItemId = dropData.getItemIdOver();
                    if(targetItemId != null && groupName != null && text != null) {
                        final String treeGroup = (String) targetItemId;
                        if(treeGroup.equals(groupName)) {
                            final Object newItemId = getTreeB().addItem();
                            getTreeB().getItem(newItemId).getItemProperty("text").setValue(text);
                            getTreeB().getItem(newItemId).getItemProperty("groupName").setValue(groupName);
                            getTreeB().setParent(newItemId, targetItemId);
                            getTreeB().setChildrenAllowed(newItemId, false);

                            sourceContainer.removeItem(sourceItemId);
                        } else {
                            final String message = text + " is not a " + treeGroup.toLowerCase().replaceAll("s$", "");
                            Notification.show(message, Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return new And(clientSideCriterion, Tree.TargetItemAllowsChildren.get(), AbstractSelect.AcceptItem.ALL);
                }
            });
        });
    }

    private void initializeTable(final ClientSideCriterion clientSideCriterion) {
        final BeanItemContainer<TableItem> beanItemContainer = new BeanItemContainer<>(TableItem.class);
        table.setContainerDataSource(beanItemContainer);
        table.setVisibleColumns("groupName","text");
        table.setDropHandler(new DropHandler() {
            @Override
            public void drop(DragAndDropEvent dragAndDropEvent) {
                final DataBoundTransferable dataBoundTransferable = (DataBoundTransferable) dragAndDropEvent.getTransferable();
                if(!(dataBoundTransferable.getSourceContainer() instanceof Container.Hierarchical)) {
                    return;
                }

                final HierarchicalContainer sourceContainer = (HierarchicalContainer)dataBoundTransferable.getSourceContainer();
                final Object sourceItemId = dataBoundTransferable.getItemId();
                String text = sourceContainer.getItem(sourceItemId).getItemProperty("text").getValue().toString();
                final Object parentItemId = sourceContainer.getParent(sourceItemId);
                final LinkedHashMap<Object, TableItem> linkedHashMap = new LinkedHashMap<>();
                if(parentItemId == null) {
                    final Collection<?> children = sourceContainer.getChildren(sourceItemId);
                    if(children != null) {
                        for (final Object childId : children) {
                            String childText = sourceContainer.getItem(childId).getItemProperty("text").getValue().toString();
                            linkedHashMap.put(childId, new TableItem((String) sourceItemId, childText));
                        }
                    }
                } else {
                    linkedHashMap.put(sourceItemId, new TableItem((String) parentItemId, text));
                }

                final AbstractSelect.AbstractSelectTargetDetails dropData = ((AbstractSelect.AbstractSelectTargetDetails) dragAndDropEvent.getTargetDetails());
                final Object targetItemId = dropData.getItemIdOver();

                for(final Object sourceId : linkedHashMap.keySet()) {
                    final TableItem tableItem = linkedHashMap.get(sourceId);
                    if(targetItemId != null) {
                        switch(dropData.getDropLocation()) {
                            case BOTTOM:
                                beanItemContainer.addItemAfter(targetItemId, linkedHashMap);
                                break;
                            case MIDDLE:
                            case TOP:
                                final Object prevItemId = beanItemContainer.prevItemId(targetItemId);
                                beanItemContainer.addItemAfter(prevItemId, linkedHashMap);
                                break;
                        }
                    } else {
                        beanItemContainer.addItem(tableItem);
                    }
                    sourceContainer.removeItem(sourceId);
                }
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return new And(clientSideCriterion, AbstractSelect.AcceptItem.ALL);
            }
        });
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

    public Table getTable() {
        if(Objects.isNull(table)) {
            table = new Table("Table");
            table.setWidth(100.0f, Unit.PERCENTAGE);
            table.setHeight(300.0f, Unit.PIXELS);
            table.setDragMode(Table.TableDragMode.ROW);
        }

        return table;
    }

    public Tree getTreeA() {
        if(Objects.isNull(treeA)) {
            treeA = new Tree("Tree A");
            treeA.setSelectable(false);
            treeA.setDragMode(Tree.TreeDragMode.NODE);
        }

        return treeA;
    }

    public Tree getTreeB() {
        if(Objects.isNull(treeB)) {
            treeB = new Tree("Tree B");
            treeB.setSelectable(false);
            treeB.setDragMode(Tree.TreeDragMode.NODE);
        }

        return treeB;
    }
}

