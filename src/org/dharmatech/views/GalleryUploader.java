/* Configure Pages */
/*API
public class GalleryUploader {
    public GalleryUploader(String filepath)
    public void start(JFrame frame) 
    public void start(JFrame frame, WebEditor webEditor) 
    public static void main(String[] args)
}
*/
package org.dharmatech.views;
import org.dharmatech.models.*;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;
import java.util.*;

public class GalleryUploader extends WebEditorGUIGeneric {
    
    private JFrame uploaderGUI;
    private RegJTable imagesTb;
    //private WebEditor webEditor;
    private RegJButton upload;
    private RegJButton close;
    private RegJButton select;
    //private RegJButton insert;
    private RegJButton delete;
    private RegJButton moveUp;
    private RegJButton moveDown;
    private JLabel statusLb;
    private JLabel uploadDestLb;
    private RegJPanel previewPn;
    private JComboBox<Integer> nprCB;
    private JComboBox<String> descriptModeCB;
    
    private final String[] colNames = {"SourceFile", "File Name", "Description"};
    public GalleryUploader() {
        setupGUI();
        HashMap<String, RegJButton> guButtonGroup = new HashMap<String, RegJButton>();
        //HashMap<String, RegJTextArea> guTextAreaGroup = new HashMap<String, RegJTextArea>();
        HashMap<String, JLabel> guLabelGroup = new HashMap<String, JLabel>();
        HashMap<String, JFrame> guFrameGroup = new HashMap<String, JFrame>();
        HashMap<String, RegJTable> guTableGroup = new HashMap<String, RegJTable>();
        HashMap<String, RegJPanel> guPanelGroup = new HashMap<String, RegJPanel>();
        HashMap<String, JComboBox<Integer>> guComboBoxIntGroup = new HashMap<String, JComboBox<Integer>>();
        HashMap<String, JComboBox<String>> guComboBoxStrGroup = new HashMap<String, JComboBox<String>>();

        guButtonGroup.put("upload".toLowerCase(), upload);
        guButtonGroup.put("close".toLowerCase(), close);
        guButtonGroup.put("select".toLowerCase(), select);
        //guButtonGroup.put("insert".toLowerCase(), insert);
        guButtonGroup.put("delete".toLowerCase(), delete);
        guButtonGroup.put("moveUp".toLowerCase(), moveUp);
        guButtonGroup.put("moveDown".toLowerCase(), moveDown);
        
        guFrameGroup.put("uploaderGUI".toLowerCase(), uploaderGUI);
        
        guLabelGroup.put("statusLb".toLowerCase(), statusLb);
        guLabelGroup.put("uploadDestLb".toLowerCase(), uploadDestLb);
        
        guTableGroup.put("imagesTb".toLowerCase(), imagesTb);

        guPanelGroup.put("previewPn".toLowerCase(), previewPn);

        guComboBoxIntGroup.put("nprCB".toLowerCase(), nprCB);
        
        guComboBoxStrGroup.put("descriptModeCB".toLowerCase(), descriptModeCB);

        initGUIObjsButton(guButtonGroup);
        initGUIObjsLabel(guLabelGroup);
        //initGUIObjsTextArea(guTextAreaGroup);
        initGUIObjsFrame(guFrameGroup);
        initGUIObjsTable(guTableGroup);
        initGUIObjsPanel(guPanelGroup);
        initGUIObjsComboBoxInt(guComboBoxIntGroup);
        initGUIObjsComboBoxStr(guComboBoxStrGroup);

    }

    //GUI setup
    private void setupGUI() {
        JFrame uploaderGUI = new JFrame();
        this.uploaderGUI = uploaderGUI;
        RegJPanel leftMain = new RegJPanel();
        RegJPanel rightPreview = new RegJPanel();
        uploaderGUI.getContentPane().add(BorderLayout.WEST, leftMain);
        uploaderGUI.getContentPane().add(BorderLayout.CENTER, rightPreview);
        
        //Left side - main items
        RegJPanel top = new RegJPanel();
        RegJPanel center = new RegJPanel();
        RegJPanel bot = new RegJPanel();
        leftMain.setLayout(new BorderLayout());
        leftMain.add(BorderLayout.NORTH, top);
        leftMain.add(BorderLayout.CENTER, center);
        leftMain.add(BorderLayout.SOUTH, bot);        
        
        //Top
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        RegJPanel infoPanel = new RegJPanel();
        RegJPanel nprPanel = new RegJPanel();
        RegJPanel descriptModePanel = new RegJPanel();
        infoPanel.setLayout(new BorderLayout());
        nprPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        descriptModePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        top.add(infoPanel);
        top.add(nprPanel);
        top.add(descriptModePanel);
        //Info
        RegJPanel lbInfoPanel = new RegJPanel();
        JLabel uploadDestLbInfo = new JLabel("Upload Destination: ");
        RegJPanel lbPanel = new RegJPanel();
        JLabel uploadDestLb = new JLabel("Unspecified");
        lbInfoPanel.add(uploadDestLbInfo);
        lbPanel.add(uploadDestLb);
        infoPanel.add(BorderLayout.WEST, lbInfoPanel);
        infoPanel.add(BorderLayout.CENTER, lbPanel);
        this.uploadDestLb = uploadDestLb;
        //Number per row
        JLabel nprLb = new JLabel("Number per row: ");
        JComboBox<Integer> nprCB = new JComboBox<Integer>();
        nprPanel.add(nprLb);
        nprPanel.add(nprCB);
        this.nprCB = nprCB;
        //Description Mode
        JLabel descriptModeLb = new JLabel("Description Display Mode: ");
        JComboBox<String> descriptModeCB = new JComboBox<String>();
        descriptModePanel.add(descriptModeLb);
        descriptModePanel.add(descriptModeCB);
        this.descriptModeCB = descriptModeCB;
        
        //Center
        center.setLayout(new BorderLayout());
        RegJPanel left = new RegJPanel();
        center.add(BorderLayout.WEST, left);
        //Table
        Object data[][] = new Object[1][3];
        RegJTable imagesTb = new RegJTable(data, colNames);
        JScrollPane scroll = new JScrollPane(imagesTb);
        scroll.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        left.add(scroll);
        this.imagesTb = imagesTb;
        //Buttons
        RegJPanel right = new RegJPanel();
        center.add(BorderLayout.CENTER, right);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        RegJButton select = new RegJButton("Add...");
        //RegJButton insert = new RegJButton("Insert");
        RegJButton delete = new RegJButton("Delete");
        RegJButton moveUp = new RegJButton("Move Up");
        RegJButton moveDown = new RegJButton("Move Down");
        right.add(select);
        //right.add(insert);
        right.add(delete);
        right.add(moveUp);
        right.add(moveDown);
        Dimension butMax = moveDown.getMaximumSize();
        select.setMaximumSize(butMax);
        //insert.setMaximumSize(butMax);
        delete.setMaximumSize(butMax);
        moveUp.setMaximumSize(butMax);
        moveDown.setMaximumSize(butMax);
        this.select = select;
        //this.insert = insert;
        this.delete = delete;
        this.moveUp = moveUp;
        this.moveDown = moveDown;
        //table.getDocument().getDocumentListener(new EditDocumentListener());
        //Bot
        RegJPanel opsPanel = new RegJPanel();
        RegJPanel statusPanel = new RegJPanel();
        bot.setLayout(new BorderLayout());
        bot.add(BorderLayout.NORTH, opsPanel);
        bot.add(BorderLayout.CENTER, statusPanel);
        //Ops
        opsPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        RegJButton upload = new RegJButton("Upload / Save");
        RegJButton close = new RegJButton("Close");
        opsPanel.add(upload);
        opsPanel.add(close);
        this.upload = upload;
        this.close = close;
        //Status
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        JLabel statusLb = new JLabel("Status: ");
        statusPanel.add(statusLb);
        this.statusLb = statusLb;
        
        //Right: Preview
        //rightPreview.setPreferredSize(new Dimension(300, 400));
        RegJPanel previewPn = new RegJPanel();
        JScrollPane scrollPreview = new JScrollPane(previewPn);
        scroll.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rightPreview.setLayout(new BorderLayout());
        rightPreview.add(BorderLayout.CENTER, scrollPreview);
        //rightPreview.setBorder(BorderFactory.createLineBorder(Color.gray));
        previewPn.setLayout(new BoxLayout(previewPn, BoxLayout.Y_AXIS));
        this.previewPn = previewPn;
        
        //GUI
        uploaderGUI.setSize(700,500);
        //previewPn.setPreferredSize(new Dimension(300,500));
        
    }
    
    
    public static void main(String[] args) {
        
    }
}
