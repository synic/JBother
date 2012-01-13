/*
 Copyright (C) 2003 Adam Olsen

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 1, or (at your option)
 any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.valhalla.jbother;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;

import com.valhalla.gui.MJTextArea;
import com.valhalla.gui.MJTextField;
import com.valhalla.gui.Standard;

/**
 * A visual representation of a DataForm
 *
 * @author Adam Olsen
 */
public class JBDataForm extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JPanel main;

    private JPanel container = new JPanel();

    private JButton okButton = new JButton(resources.getString("okButton"));

    private JButton cancelButton = new JButton(resources
            .getString("cancelButton"));

    private Vector fields = new Vector();

    private GridBagLayout layout = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private Vector listeners = new Vector();

    private Form form;

    public JBDataForm(JFrame parent, Form form) {
        super(parent, form.getTitle());
        this.form = form;
        main = (JPanel) getContentPane();
        main.setLayout(new BorderLayout(5, 5));
        main.setBorder(BorderFactory.createTitledBorder(form.getTitle()));
        JLabel instructions = new JLabel("<html>"
                + form.getInstructions().replaceAll("\\n", "<br>") + "</html>");
        instructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        main.add(instructions, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        main.add(buttonPanel, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(container);
        main.add(scroll);
        container.setLayout(layout);
        container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        c.gridx = 0;
        c.gridy = -1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;

        Iterator fields = form.getFields();
        while (fields.hasNext()) {
            FormField field = (FormField) fields.next();

            Field f;

            if (field.getType().equals(FormField.TYPE_BOOLEAN)) {
                f = new BooleanField(field);
            } else if (field.getType().equals(FormField.TYPE_FIXED)) {
                f = new FixedField(field);
            } else if (field.getType().equals(FormField.TYPE_HIDDEN)) {
                f = new HiddenField(field);
            } else if (field.getType().equals(FormField.TYPE_TEXT_MULTI)) {
                f = new MultiTextField(field);
            } else if (field.getType().equals(FormField.TYPE_TEXT_PRIVATE)) {
                f = new PrivateField(field);
            } else if (field.getType().equals(FormField.TYPE_LIST_SINGLE)
                    || field.getType().equals(FormField.TYPE_JID_SINGLE)) {
                f = new ListSingleField(field);
            } else if (field.getType().equals(FormField.TYPE_LIST_MULTI)
                    || field.getType().equals(FormField.TYPE_JID_MULTI)) {
                f = new ListMultiField(field);
            } else {
                f = new Field(field);
            }

            this.fields.add(f);

            if (!field.getType().equals(FormField.TYPE_HIDDEN)
                    && !field.getType().equals(FormField.TYPE_FIXED)) {
                c.gridy++;
                c.gridx = 0;
                Component left = f.getLeftComponent();
                Component right = f.getRightComponent();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = .5;

                layout.setConstraints(left, c);
                container.add(left);

                c.fill = GridBagConstraints.NONE;
                c.gridx++;
                c.weightx = .5;
                layout.setConstraints(right, c);
                container.add(right);
            } else if (field.getType().equals(FormField.TYPE_FIXED)) {
                c.gridy++;
                c.gridx = 0;
                c.gridwidth = 2;
                JLabel label = new JLabel("<html><b>" + f.getFirstValue()
                        + "</b></html>");
                layout.setConstraints(label, c);
                container.add(label);
                c.gridwidth = 1;
            }
        }

        c.gridy++;
        c.gridwidth = 2;
        c.weighty = .9;
        JLabel blank = new JLabel("");
        layout.setConstraints(blank, c);
        container.add(blank);

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireEvent(e);
            }
        };

        okButton.setActionCommand("ok");
        cancelButton.setActionCommand("cancel");
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);

        pack();
        setSize(new Dimension(630, 450));
        setLocationRelativeTo(parent);
    }

    public Form getAnswerForm() {
        Form a = null;
        try {
            a = form.createAnswerForm();
        } catch (Exception ex) {
            Standard.warningMessage(this, resources.getString("dataForm"),
                    resources.getString("unknownError"));
            dispose();
            return null;
        }

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = (Field) this.fields.get(i);
            if (field.getVariable() == null || field.getVariable().equals(""))
                continue;

            if (!ensureAnswer(field.getFormField(), field.getAnswer()))
                return null;
            Object answer = field.getAnswer();

            //com.valhalla.Logger.debug( i + " " + field.getVariable() + " " +
            // answer + " " + field.getFormField().getType() );

            if (answer instanceof Boolean) {
                a.setAnswer(field.getVariable(), ((Boolean) answer)
                        .booleanValue());
            } else if (answer instanceof java.util.List) {
                a.setAnswer(field.getVariable(), (java.util.List) answer);
            } else {
                a.setAnswer(field.getVariable(), (String) answer);
            }
        }

        return a;
    }

    private boolean ensureAnswer(FormField field, Object answer) {
        if (!field.isRequired())
            return true;

        if ((answer instanceof String && ((String) answer).equals(""))
                || answer == null
                || (answer instanceof java.util.List && ((java.util.List) answer)
                        .size() <= 0)) {
            String w = MessageFormat.format(resources.getString("emptyField"),
                    new Object[] { field.getLabel() });
            Standard.warningMessage(this, resources.getString(getTitle()), w);

            return false;
        }

        return true;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    private void fireEvent(ActionEvent e) {
        for (int i = 0; i < listeners.size(); i++) {
            ActionListener l = (ActionListener) listeners.get(i);
            l.actionPerformed(e);
        }
    }
}

class Field {
    protected JComponent rightComp = new MJTextField(15);

    protected FormField field;

    public Field(FormField field) {
        this.field = field;
        ((JTextField) rightComp).setText(getFirstValue());
    }

    public FormField getFormField() {
        return field;
    }

    protected String getFirstValue() {
        Iterator it = field.getValues();
        if (!it.hasNext())
            return "";
        return (String) it.next();
    }

    public String getVariable() {
        return field.getVariable();
    }

    public Object getAnswer() {
        return ((JTextField) rightComp).getText();
    }

    public JComponent getLeftComponent() {
        return new JLabel("<html>" + field.getLabel() + ":</html>");
    }

    public JComponent getRightComponent() {
        return rightComp;
    }
}

class MultiTextField extends Field {
    private MJTextArea text = new MJTextArea(3, 20);

    public MultiTextField(FormField field) {
        super(field);
        rightComp = new JScrollPane(text);
        text.setText(getFirstValue());
    }

    public Object getAnswer() {
        return text.getText();
    }
}

class ListSingleField extends Field {
    public ListSingleField(FormField field) {
        super(field);

        ArrayList list = new ArrayList();

        Iterator options = field.getOptions();
        int selected = -1;
        int count = 0;
        String def = getFirstValue();

        while (options.hasNext()) {
            FormField.Option option = (FormField.Option) options.next();
            list.add(option);

            if (option.getValue().equals(def))
                selected = count;
            count++;
        }

        rightComp = new JComboBox(list.toArray());
        ((JComboBox) rightComp).setRenderer(new MyListRenderer());
        if (selected != -1) {
            ((JComboBox) rightComp).setSelectedIndex(selected);
        }
    }

    public Object getAnswer() {
        FormField.Option option = (FormField.Option) ((JComboBox) rightComp)
                .getSelectedItem();
        ArrayList answers = new ArrayList();
        answers.add(option.getValue());
        return answers;
    }
}

class ListMultiField extends Field {
    public ListMultiField(FormField field) {
        super(field);

        ArrayList list = new ArrayList();

        Iterator options = field.getOptions();
        ArrayList selected = new ArrayList();
        int count = 0;

        while (options.hasNext()) {
            FormField.Option option = (FormField.Option) options.next();
            list.add(option);

            Iterator values = field.getValues();
            while (values.hasNext()) {
                String value = (String) values.next();
                if (option.getValue().equals(value))
                    selected.add(new Integer(count));
            }

            count++;
        }

        rightComp = new JList(list.toArray());

        if (selected.size() > 0) {
            int indecis[] = new int[selected.size()];
            for (int i = 0; i < selected.size(); i++) {
                Integer in = (Integer) selected.get(i);
                indecis[i] = in.intValue();
            }
            ((JList) rightComp).setSelectedIndices(indecis);
        }

        ((JList) rightComp).setCellRenderer(new MyListRenderer());
    }

    public Object getAnswer() {
        Object[] sel = ((JList) rightComp).getSelectedValues();
        ArrayList answers = new ArrayList();
        for (int i = 0; i < sel.length; i++) {
            FormField.Option option = (FormField.Option) sel[i];
            answers.add(option.getValue());
        }

        return answers;
    }
}

class PrivateField extends Field {
    public PrivateField(FormField field) {
        super(field);
        rightComp = new JPasswordField(15);
        ((JPasswordField) rightComp).setText(getFirstValue());
        ((JPasswordField) rightComp).setFont(new MJTextField().getFont());
    }

    public Object getAnswer() {
        return new String(((JPasswordField) rightComp).getPassword());
    }
}

class HiddenField extends Field {
    public HiddenField(FormField field) {
        super(field);
    }

    public Object getAnswer() {
        return getFirstValue();
    }
}

class BooleanField extends Field {
    public BooleanField(FormField field) {
        super(field);
        rightComp = new JCheckBox();

        if (getFirstValue().equals("1"))
            ((JCheckBox) rightComp).setSelected(true);
    }

    public Object getAnswer() {
        if (((JCheckBox) rightComp).isSelected()) {
            return new Boolean(true);
        } else {
            return new Boolean(false);
        }
    }
}

class FixedField extends Field {
    public FixedField(FormField field) {
        super(field);
    }

    public Object getAnswer() {
        return getFirstValue();
    }
}

class MyListRenderer extends JLabel implements ListCellRenderer {
    public MyListRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        FormField.Option op = (FormField.Option) value;

        if (!op.getLabel().equals("")) {
            setText(op.getLabel());
        } else {
            setText(op.getValue());
        }

        setBackground(isSelected ? list.getSelectionBackground() : list
                .getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list
                .getForeground());
        return this;
    }
}