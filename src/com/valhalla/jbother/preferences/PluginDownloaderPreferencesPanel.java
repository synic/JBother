package com.valhalla.jbother.preferences;

import com.valhalla.settings.TempSettings;
import com.valhalla.settings.Settings;
import com.valhalla.gui.MJTextField;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.Locale;
import java.awt.*;

/**
 * Created by luke on Jul 25, 2005 2:02:31 PM
 */

public class PluginDownloaderPreferencesPanel extends JPanel
  implements PreferencesPanel
{
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private JLabel pluginsMirrorLabel = new JLabel(resources.getString("pluginsMirror")
            + ":");

    private JLabel pluginsMirrorScriptLabel = new JLabel(resources
            .getString("pluginsMirrorScript")
            + ":");

    private MJTextField pluginsMirrorTF = new MJTextField(5);

    private MJTextField pluginsMirrorScriptTF = new MJTextField(15);

    public PluginDownloaderPreferencesPanel(PreferencesDialog dialog) {
        pluginsMirrorLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        pluginsMirrorScriptLabel.setBorder(BorderFactory
                .createEmptyBorder(0, 5, 0, 5));

        setBorder(BorderFactory.createTitledBorder(resources
                .getString("pluginsMirrorSettings")));
        setLayout(grid);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;

        // port
        c.weightx = 0.0;
        grid.setConstraints(pluginsMirrorLabel, c);
        add(pluginsMirrorLabel);

        c.gridx++;
        grid.setConstraints(pluginsMirrorTF, c);
        add(pluginsMirrorTF);

        // interface
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy++;
        grid.setConstraints(pluginsMirrorScriptLabel, c);
        add(pluginsMirrorScriptLabel);

        c.gridx = 1;
        c.gridy = 1;
        grid.setConstraints(pluginsMirrorScriptTF, c);
        add(pluginsMirrorScriptTF);

        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        JLabel emptyLabel = new JLabel("");
        grid.setConstraints(emptyLabel, c);
        add(emptyLabel);

        loadSettings();
    }

    public TempSettings getSettings() {
        TempSettings mySettings = new TempSettings();
        String pluginsMirrorStr = pluginsMirrorTF.getText();
        mySettings.setProperty("pluginsDownloadMirror", pluginsMirrorStr);

        String pluginsMirrorScriptStr = pluginsMirrorScriptTF.getText();
        mySettings.setProperty("pluginsDownloadScript", pluginsMirrorScriptStr);

        return mySettings;
    }

    private void loadSettings() {
        pluginsMirrorTF.setText(Settings.getInstance().
          getProperty("pluginsDownloadMirror","www.jbother.org"));
        pluginsMirrorScriptTF.setText(Settings.getInstance().
          getProperty("pluginsDownloadScript","/plugins/index.rb"));
    }

}
