package org.openlcb.cdi.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import javax.swing.*;

import org.openlcb.EventID;
import org.openlcb.cdi.CdiRep;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.EventIdTextField;

import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_ENTRY_DATA;

/**
 * Simple example CDI display.
 *
 * Works with a CDI reader.
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @author  Paul Bender Copyright 2016
 * @author  Balazs Racz Copyright 2016
 */
public class CdiPanel extends JPanel {

    private ConfigRepresentation rep;

    public CdiPanel () { super(); }
    
    /**
     * @param rep Representation of the config to be loaded
     * @param factory Implements hooks for optional interface elements
     */
    public void initComponents(ConfigRepresentation rep, GuiItemFactory factory) {
        initComponents(rep);
        // ensure not null
        if (factory != null)
            this.factory = factory;
    }

    /**
     * @param rep Representation of the config to be loaded
     */
    public void initComponents(ConfigRepresentation rep) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        this.rep = rep;
        this.factory = new GuiItemFactory(); // default with no behavior
    }
    
    GuiItemFactory factory;

    private void displayCdi() {
        if (rep.getCdiRep().getIdentification() != null) {
            add(createIdentificationPane(c));
        }
        rep.visit(new RendererVisitor());
        rep.visit(new ConfigRepresentation.Visitor() {
            @Override
            public void visitSegment(ConfigRepresentation.SegmentEntry e) {
                add(createSegmentPane(e));
            }
        });
        // add glue at bottom
        add(Box.createVerticalGlue());
    }

    private class RendererVisitor extends ConfigRepresentation.Visitor {
        private JPanel currentPane;
        private JPanel currentLeaf;
        @Override
        public void visitSegment(ConfigRepresentation.SegmentEntry e) {
            currentPane = createSegmentPane(e);
            super.visitSegment(e);

            String name = "Segment" + (e.getName() != null ? (": " + e.getName()) : "");
            JPanel ret = new util.CollapsiblePanel(name, currentPane);
            // ret.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED)); //debugging
            ret.setAlignmentY(Component.TOP_ALIGNMENT);
            ret.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(ret);
        }

        @Override
        public void visitString(ConfigRepresentation.StringEntry e) {
            currentLeaf = new StringPane(e);
            super.visitString(e);
        }

        @Override
        public void visitInt(ConfigRepresentation.IntegerEntry e) {
            currentLeaf = new IntPane(e);
            super.visitInt(e);
        }

        @Override
        public void visitEvent(ConfigRepresentation.EventEntry e) {
            currentLeaf = new EventIdPane(e);
            super.visitEvent(e);
        }

        @Override
        public void visitLeaf(ConfigRepresentation.CdiEntry e) {
            currentLeaf.setAlignmentX(Component.LEFT_ALIGNMENT);
            currentPane.add(currentLeaf);
            currentLeaf = null;
        }
    }

    JPanel createIdentificationPane(CdiRep c) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        //p.setBorder(BorderFactory.createTitledBorder("Identification"));

        CdiRep.Identification id = c.getIdentification();
        
        JPanel p1 = new JPanel();
        p.add(p1);
        p1.setLayout(new util.javaworld.GridLayout2(4,2));
        p1.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p1.add(new JLabel("Manufacturer: "));
        p1.add(new JLabel(id.getManufacturer()));
        
        p1.add(new JLabel("Model: "));
        p1.add(new JLabel(id.getModel()));
        
        p1.add(new JLabel("Hardware Version: "));
        p1.add(new JLabel(id.getHardwareVersion()));
        
        p1.add(new JLabel("Software Version: "));
        p1.add(new JLabel(id.getSoftwareVersion()));
        
        p1.setMaximumSize(p1.getPreferredSize());
        
        // include map if present
        JPanel p2 = createPropertyPane(id.getMap());
        if (p2!=null) {
            p2.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(p2);
        }
        
        JPanel ret = new util.CollapsiblePanel("Identification", p);
        ret.setAlignmentY(Component.TOP_ALIGNMENT);
        ret.setAlignmentX(Component.LEFT_ALIGNMENT);
        return ret;
    }

    /**
     * Creates UI for a properties Map (for segments and groups).
     * @param map the properties to display
     * @return panel with UI
     */
    JPanel createPropertyPane(CdiRep.Map map) {
        if (map != null) {
            JPanel p2 = new JPanel();
            p2.setAlignmentX(Component.LEFT_ALIGNMENT);
            p2.setBorder(BorderFactory.createTitledBorder("Properties"));
            
            java.util.List keys = map.getKeys();
            if (keys.isEmpty()) return null;

            p2.setLayout(new util.javaworld.GridLayout2(keys.size(),2));

            for (int i = 0; i<keys.size(); i++) {
                String key = (String)keys.get(i);

                p2.add(new JLabel(key+": "));
                p2.add(new JLabel(map.getEntry(key)));
                
            }
            p2.setMaximumSize(p2.getPreferredSize());
            return p2;
        } else 
            return null;
    }

    JPanel createSegmentPane(ConfigRepresentation.SegmentEntry item) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        //p.setBorder(BorderFactory.createTitledBorder(name));

        String d = item.getDescription();
        if (d != null) p.add(createDescriptionPane(d));

        // include map if present
        JPanel p2 = createPropertyPane(item.getMap());
        if (p2 != null) p.add(p2);
        return p;
    }


    JPanel createSegmentPaneOld(ConfigRepresentation.SegmentEntry item) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        String name = "Segment"+(item.getName()!=null?(": "+item.getName()):"");
        //p.setBorder(BorderFactory.createTitledBorder(name));

        String d = item.getDescription();
        if (d!=null) p.add(createDescriptionPane(d));
        
        // include map if present
        JPanel p2 = createPropertyPane(item.getMap());
        if (p2!=null) p.add(p2);


        // find and process items
        java.util.List<CdiRep.Item> items = item.getItems();
        if (items != null) {
             DisplayPane pane = null; 
              
             for (int i=0; i<items.size(); i++) {
                CdiRep.Item it = (CdiRep.Item) items.get(i);
                
                origin = origin +it.getOffset();
 
                 if (it instanceof CdiRep.Group) {
                     pane = createGroupPane((CdiRep.Group) it, origin, space);
                 } else if (it instanceof CdiRep.BitRep) {
                     pane = createBitPane((CdiRep.BitRep) it, origin, space);
                 } else if (it instanceof CdiRep.IntegerRep) {
                     pane = createIntPane((CdiRep.IntegerRep) it, origin, space);
                 } else if (it instanceof CdiRep.EventID) {
                     pane = createEventIdPane((CdiRep.EventID) it, origin, space);
                 } else if (it instanceof CdiRep.StringRep) {
                     pane = createStringPane((CdiRep.StringRep) it, origin, space);
                 }
                 if (pane != null) {
                    origin = pane.getOrigin() + pane.getVarSize();
                    if(it instanceof CdiRep.Group) {
                        // groups should collapse.  
                        JPanel colPane = new util.CollapsiblePanel(it.getName(), pane);
                        colPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                        p.add(colPane);
                    } else {
                        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
                        p.add(pane);
                    }
                 } else {
                     System.out.println("could not process type of " + it);
                 }
            }
        }
        
        JPanel ret = new util.CollapsiblePanel(name, p);
        // ret.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED)); //debugging
        ret.setAlignmentY(Component.TOP_ALIGNMENT);
        ret.setAlignmentX(Component.LEFT_ALIGNMENT);
        return ret;
    }

    JPanel createDescriptionPane(String d) {
        if (d == null) return null;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea area = new JTextArea(d);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setFont(UIManager.getFont("Label.font"));
        area.setEditable(false);
        area.setOpaque(false);
        area.setWrapStyleWord(true); 
        area.setLineWrap(true);
        p.add(area);
        return p;
    }

    abstract class DisplayPane extends JPanel {
    }

    DisplayPane createGroupPane(CdiRep.Group item, long origin, int space) {
        DisplayPane ret = new GroupPane(item, origin, space);
        return ret;        
    }
    
    public class GroupPane extends DisplayPane {
        GroupPane(CdiRep.Group item, long origin, int space){
            super(origin, space);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? (item.getName()) : "Group");
            setBorder(BorderFactory.createTitledBorder(name));
            setName(name);

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            factory.handleGroupPaneStart(this);
            
            // include map if present
            JPanel p2 = createPropertyPane(item.getMap());
            if (p2 != null) {
                add(p2);
            }

            // find and process items as replicated
            int rep = item.getReplication();
            if (rep == 0) {
                rep = 1;  // default
            }
            JPanel currentPane = this;
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (int i = 0; i < rep; i++) {
                if (rep != 1) {
                    // nesting a pane
                    currentPane = new JPanel();
                    currentPane.setLayout(new BoxLayout(currentPane, BoxLayout.Y_AXIS));
                    currentPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                    name = (item.getRepName() != null ? (item.getRepName()) : "Group")+" "+(i+1);
                    currentPane.setBorder(BorderFactory.createTitledBorder(name));
                    factory.handleGroupPaneStart(currentPane);
                            
                    currentPane.setName(name);
                     
                    tabbedPane.add(currentPane);
                    
                }
                java.util.List<CdiRep.Item> items = item.getItems();
                if (items != null) {
                    for (int j = 0; j < items.size(); j++) {
                        CdiRep.Item it = (CdiRep.Item) items.get(j);
                        DisplayPane pane = null;
                        
                        origin = origin +it.getOffset();
                        size = size + it.getOffset();
                        //System.err.println("Origin " + origin + " csize " + size + " type " + it
                        //        .getClass().getSimpleName());

                        // Following code smells bad.  CdiRep is a representational
                        // class, shouldn't contain a "makeRepresentation" method,
                        // but some sort of dispatch would be better than this.
                        
                        if (it instanceof CdiRep.Group) {
                            pane = createGroupPane((CdiRep.Group) it, origin, space);
                            pane.setName(name);
                        } else if (it instanceof CdiRep.BitRep) {
                            pane = createBitPane((CdiRep.BitRep) it, origin, space);
                        } else if (it instanceof CdiRep.IntegerRep) {
                            pane = createIntPane((CdiRep.IntegerRep) it, origin, space);
                        } else if (it instanceof CdiRep.EventID) {
                            pane = createEventIdPane((CdiRep.EventID) it, origin, space);
                        } else if (it instanceof CdiRep.StringRep) {
                            pane = createStringPane((CdiRep.StringRep) it, origin,space);
                        }
                        if (pane != null) {
                            size = size + pane.getVarSize();
                            origin = pane.getOrigin() + pane.getVarSize();
                            currentPane.add(pane);
                        } else { // pane == null, either didn't select a type or something went wrong in creation.
                            System.out.println("could not process type of " + it);
                        }
                    }
                }
                factory.handleGroupPaneEnd(currentPane);

            }
            add(tabbedPane);
            if (rep != 1) factory.handleGroupPaneEnd(this);  // if 1, currentpane is this
        }
        
    }
    
    public class EventIdPane extends JPanel {
        private final ConfigRepresentation.EventEntry entry;
        private final CdiRep.Item item;
        JFormattedTextField textField;
        
        EventIdPane(ConfigRepresentation.EventEntry e) {
            entry = e;
            item = entry.getCdiItem();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : "EventID");
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
            add(p3);

            textField = factory.handleEventIdTextField(EventIdTextField.getEventIdTextField());
            textField.setMaximumSize(textField.getPreferredSize());
            p3.add(textField);

            entry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (propertyChangeEvent.getPropertyName() == UPDATE_ENTRY_DATA) {
                        if (e.lastVisibleValue == null) {
                            textField.setText("");
                        } else {
                            textField.setText(e.lastVisibleValue);
                        }
                    }
                }
            });
            entry.fireUpdate();

            JButton b;
            b = factory.handleReadButton(new JButton("Refresh")); // was: read
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    entry.reload();
                }
            });
            p3.add(b);
            b = factory.handleWriteButton(new JButton("Write"));
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    byte[] contents = org.openlcb.Utilities.bytesFromHexString((String)textField.getValue());
                    entry.setValue(new EventID(contents));
                 }
            });
            p3.add(b);
            p3.add(Box.createHorizontalGlue());
        }
    }
    

    public class IntPane extends JPanel {
        JTextField textField = null;
        JComboBox box = null;
        CdiRep.Map map = null;
        private final ConfigRepresentation.IntegerEntry entry;
        private final CdiRep.Item item;


        IntPane(ConfigRepresentation.IntegerEntry e) {
            this.entry = e;
            this.item = entry.getCdiItem();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : "Integer");
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
            add(p3);

            // see if map is present
            String[] labels;
            map = item.getMap();
            if ((map != null) && (map.getKeys().size() > 0)) {
                // map present, make selection box
                box = new JComboBox(map.getValues().toArray(new String[]{""})) {
                    public java.awt.Dimension getMaximumSize() {
                        return getPreferredSize();
                    }
                };
                p3.add(box);
            } else {
                // map not present, just an entry box
                textField = new JTextField(24) {
                    public java.awt.Dimension getMaximumSize() {
                        return getPreferredSize();
                    }
                };
                p3.add(textField);
                textField.setToolTipText("Signed integer value of up to "+entry.size+" bytes");
            }

            entry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (propertyChangeEvent.getPropertyName() == UPDATE_ENTRY_DATA) {
                        if (e.lastVisibleValue == null) return;
                        if (textField != null) {
                            textField.setText(entry.lastVisibleValue);
                        } else {
                            box.setSelectedItem(entry.lastVisibleValue);
                        }
                    }
                }
            });
            entry.fireUpdate();

            JButton b;
            b = factory.handleReadButton(new JButton("Refresh")); // was: read
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    entry.reload();
                }
            });
            p3.add(b);
            b = factory.handleWriteButton(new JButton("Write"));
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    long value;
                    if (textField != null) {
                        value = Long.parseLong(textField.getText());
                    } else {
                        // have to get key from stored value
                        String entry = (String) box.getSelectedItem();
                        String key = map.getKey(entry);
                        value = Long.parseLong(key);
                    }
                    entry.setValue(value);
                }
            });
            p3.add(b);
            p3.add(Box.createHorizontalGlue());
        }
    }

    public class StringPane extends JPanel {
        JTextField textField;
        private final ConfigRepresentation.StringEntry entry;
        private final CdiRep.Item item;

        StringPane(ConfigRepresentation.StringEntry e) {
            this.entry = e;
            this.item = entry.getCdiItem();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName()!=null? item.getName() : "String");
            setBorder(BorderFactory.createTitledBorder(name));
        
            String d = item.getDescription();
            if (d!=null) add(createDescriptionPane(d));

            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
            add(p3);

            textField = new JTextField(entry.size) {
                public java.awt.Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
            textField = factory.handleStringValue(textField);
            
            p3.add(textField);
            textField.setToolTipText("String of up to "+entry.size+" characters");

            entry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (propertyChangeEvent.getPropertyName() == UPDATE_ENTRY_DATA) {
                        if (e.lastVisibleValue == null) {
                            textField.setText("");
                        } else {
                            textField.setText(e.lastVisibleValue);
                        }
                    }
                }
            });
            entry.fireUpdate();

            JButton b;
            b = factory.handleReadButton(new JButton("Refresh")); // was: read
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    entry.reload();
                }
            });
            p3.add(b);
            b = factory.handleWriteButton(new JButton("Write"));
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    entry.setValue(textField.getText());
                }
            });
            p3.add(b);
            p3.add(Box.createHorizontalGlue());
        }
     }

     /** 
      * Provide access to e.g. a MemoryConfig service.
      * 
      * Default just writes output for debug
      */
    public static class ReadWriteAccess {
        public void doWrite(long address, int space, byte[] data, final
                            MemoryConfigurationService.McsWriteHandler handler) {
            System.out.println("Write to "+address+" in space "+space);
        }
        public void doRead(long address, int space, int length, final MemoryConfigurationService
                .McsReadHandler handler) {
            System.out.println("Read from "+address+" in space "+space);
        }
    }
     
    /** 
     * Handle GUI hook requests if needed
     * 
     * Default behavior is to do nothing
     */
    public static class GuiItemFactory {
        public JButton handleReadButton(JButton button) {
            return button;
        }
        public JButton handleWriteButton(JButton button) {
            return button;
        }
        public void handleGroupPaneStart(JPanel pane) {
            return;
        }
        public void handleGroupPaneEnd(JPanel pane) {
            return;
        }
        public JFormattedTextField handleEventIdTextField(JFormattedTextField field) {
            return field;
        }
        public JTextField handleStringValue(JTextField value) {
            return value;
        }

    }
}
