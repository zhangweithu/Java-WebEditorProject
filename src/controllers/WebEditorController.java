/* WebEditor Controller */
/* Author: Wei Zhang
   Latest Version: 2016 Jan 17
*/
/* API
class WebEditorController {
    public WebEditorController()
    public void start()
    public static void main(String[] args)
}
*/
/*
Compile: javac views\*.java controllers\*.java models\*.java utilities\*.java -d ..\class
Execute: java controllers.WebEditorController
*/

package controllers;
import views.*;
import models.*;
import utilities.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class WebEditorController {
    private WebEditor webEditor;
    private String rootDir;
    private ArrayList<Page> pages = new ArrayList<Page>();
    private int currentPage = -1;
    private HashMap<String, String> designSet = new HashMap<String, String>();
    
    //private FileEditorController fileEditorController;
    
    //Configurations
    private static final String resourceDirRel = "Resources";
    private static final String websiteDirRel = "Website";
    private static final String rootCfgRel = "cfg" + File.separator + "Root.cfg";
    private static final String lastOpenCfgRel = "cfg" + File.separator + "Last_OpenDir.txt";
    private String pagesCfgTxt;
    private String frameworkCfg;
    private String mobileFrameworkCfg;
    private String websiteNameCfg;
    private String websiteURLCfg;
    private String stylesheet;
    private String mobileStylesheet;
    
   
    public WebEditorController() {
        webEditor = new WebEditor();
        designSet.put("resourceDirRel", resourceDirRel);
        designSet.put("websiteDirRel", websiteDirRel);
        designSet.put("lastOpenCfgRel", lastOpenCfgRel);
    }
    
    //Initialize WebEditor GUI
    private void basicSetup() {
        webEditor.frameSetDefaultCloseOperation("webEditorGUI", 
            JFrame.EXIT_ON_CLOSE);
            
        webEditor.comboBoxStrAddActionListener("pagesBox", 
            new PagesBoxListener());
        
        webEditor.buttonAddActionListener("selRoot", 
            new SelectRootActionListener());
        webEditor.buttonAddActionListener("addTitle", 
            new InsertActionListener(WebModuleEnum.TITLE));
        webEditor.buttonAddActionListener("addParagraph", 
            new InsertActionListener(WebModuleEnum.PARAGRAPH));
        webEditor.buttonAddActionListener("addCode", 
            new InsertActionListener(WebModuleEnum.CODE));
        webEditor.buttonAddActionListener("addFile", 
            new InsertActionListener(WebModuleEnum.FILE));
        webEditor.buttonAddActionListener("addImage", 
            new InsertActionListener(WebModuleEnum.IMAGE));
        webEditor.buttonAddActionListener("addGallery", 
            new InsertActionListener(WebModuleEnum.GALLERY));
        webEditor.buttonAddActionListener("addDivider", 
            new InsertActionListener(WebModuleEnum.DIVIDER));
        webEditor.buttonAddActionListener("edit", 
            new EditActionListener());
        webEditor.buttonAddActionListener("delete", 
            new DeleteActionListener());
        webEditor.buttonAddActionListener("rename", 
            new RenameActionListener());
        webEditor.buttonAddActionListener("save", 
            new SaveActionListener());
        webEditor.buttonAddActionListener("compile", 
            new CompileActionListener());
        webEditor.textAreaAddDocumentListener("contentTA", new EditDocumentListener());

    }
    
    private class ReloadCfg implements Command {
        public void execute(Object data) 
        {
            webEditor.buttonClick("save");
            init();
            webEditor.labelSetText("statusLb", "Configuration reloaded.");
        }
    }
    
    private void init() {
        //Initialize status
        menuItemsSetEnabledAll(false);
        
        webEditor.labelSetText("statusLb", "Initializing...");
        webEditor.labelSetText("rootDirLb", "Root Dir Unspecified");
        webEditor.textAreaSetText("contentTA", "");
        
        //Load root
        boolean rootLoaded = initRoot();
        if (!rootLoaded) {  //Root configuration not successful; nothing to load further
            return;
        }        
        //Load pages
        loadPages();
        //Initialize menuItems
        initMenuItems();
        
        //Finish setup
        webEditor.labelSetText("statusLb", "Ready");
    }
    
    //Setup root; true means successful root configuration
    private boolean initRoot() {
        if (rootCfgRel == null) {
            throw new NullPointerException("Null rootCfgRel");
        }
        //Root
        String line = FileUtilities.read(rootCfgRel, "UTF-8");
        if (line != null) {
            line = line.trim();
            webEditor.labelSetText("rootDirLb", line);
            rootDir = line;
        } else {
            return false;  //If no root, nothing to load
        }
        File f = new File(rootDir);
        if (f.exists() && !f.isFile()) {
            setupCfg();
        } else {
            rootDir = null;  //Do not accept illeagal root dir
            return false;
        }
        return true;
    }
    //Setup menuItems
    private void initMenuItems() {
        webEditor.menuItemAddActionListener("frameworkItem", new MenuTemplateActionListener(frameworkCfg));
        webEditor.menuItemAddActionListener("styleItem", new MenuTemplateActionListener(stylesheet));
        webEditor.menuItemAddActionListener("mobileFrameworkItem", new MenuTemplateActionListener(mobileFrameworkCfg));
        webEditor.menuItemAddActionListener("mobileStyleItem", new MenuTemplateActionListener(mobileStylesheet));
        webEditor.menuItemAddActionListener("pageItem", new PageItemActionListener());
        webEditor.menuItemAddActionListener("infoItem", new InfoItemActionListener());
        menuItemsSetEnabledAll(true);
    }
    //Load pages
    private void loadPages() {
        if (rootDir == null) {
            throw new NullPointerException("Null rootDir");
        }
        if (pagesCfgTxt == null) {
            throw new NullPointerException("Null pagesCfgTxt");
        }
        String line = FileUtilities.read(pagesCfgTxt, "UTF-8");
        pages.clear();
        pages = new ArrayList<Page>();
        webEditor.comboBoxStrRemoveAllItems("pagesBox");
        StringBuilder allPageNames = new StringBuilder("");
        if (line != null) {
            //Create a defensive copy of designSet
            HashMap<String, String> designSetCopy = new HashMap<String, String>(designSet);
            String[] lines = line.split("\n");
            for(String sline: lines) {
                String[] pars = sline.split(",");  //What if the name or title contains ","?
                if (pars.length >= 4) {
                    int level = Integer.parseInt(pars[0]);
                    String pageName = pars[1].replace("\"", "");
                    String pageTitle = pars[2].replace("\"", "");
                    boolean displayInNav = Boolean.parseBoolean(pars[3].replace("\"", ""));
                    StringBuilder uns = new StringBuilder();
                    for (int l = 1; l < level; l++) {
                        uns.append("--");
                    }
                    String item = uns.toString() + pageName
                        + " (" + pageTitle + ")";
                    webEditor.comboBoxStrAddItem("pagesBox", item); 
                    allPageNames.append(pageName + "\n");
                    pages.add(new Page(level, pageName, pageTitle, displayInNav, designSetCopy));
                }
            }
            webEditor.comboBoxStrSetSelectedIndex("pagesBox", 0);
        }
        designSet.put("allPageNames", new String(allPageNames));
        //Need to reset designSet copy
        HashMap<String, String> designSetCopy = new HashMap<String, String>(designSet);
        for (Page p : pages) {
            p.setDesignSet(designSetCopy);
        }
    }
    //Setup config
    private void setupCfg() {
        menuItemsSetEnabledAll(false);
        if (rootDir == null) {
            throw new NullPointerException("Null rootDir");
        }
        File r = new File(rootDir);
        if (r.exists() && !r.isFile()) {
            pagesCfgTxt = rootDir + File.separator + resourceDirRel 
                + File.separator + "Pages.cfg.txt";
            frameworkCfg = rootDir + File.separator + resourceDirRel 
                + File.separator + "Framework.txt";
            mobileFrameworkCfg = rootDir + File.separator+ resourceDirRel
                + File.separator + "Framework-mobile.txt";
            websiteNameCfg = rootDir + File.separator + resourceDirRel 
                + File.separator + "Website_Name.txt";
            websiteURLCfg = rootDir + File.separator + resourceDirRel 
                + File.separator + "Website_URL.txt";
            stylesheet = rootDir + File.separator + websiteDirRel 
                + File.separator + "styles"  + File.separator + "stylesheet.css";
            mobileStylesheet = rootDir + File.separator + websiteDirRel
                + File.separator + "styles"  + File.separator + "stylesheet-mobile.css";

            designSet.put("rootDir", rootDir);               
            designSet.put("pagesCfgTxt", pagesCfgTxt);
            designSet.put("frameworkCfg", frameworkCfg);
            designSet.put("mobileFrameworkCfg", mobileFrameworkCfg);
            designSet.put("websiteNameCfg", websiteNameCfg);
            designSet.put("websiteURLCfg", websiteURLCfg);
            designSet.put("stylesheet", stylesheet);
            designSet.put("mobileStylesheet", mobileStylesheet); 
            HashMap<String, String> designSetCopy = new HashMap<String, String>(designSet);
            for (Page p : pages) {
                p.setDesignSet(designSetCopy);
            }
        } else {
            webEditor.labelSetText("rootDirLb", "Root Dir Unspecified");
        }
    }
    
    public void start() {
        if (webEditor == null) {
            throw new NullPointerException("Null webEditor");
        }
        basicSetup();
        init();
        webEditor.frameSetVisible("webEditorGUI", true);
        webEditor.textAreaRequestFocus("contentTA");
    }
    
    /*
    Action Listeners
    */
    //JCombobox Listener
    private class PagesBoxListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            if (pages.isEmpty()) {
                return;
            }
            int sel = webEditor.comboBoxStrGetSelectedIndex("pagesBox");
            currentPage = sel;
            String line = pages.get(sel).getPageContent();
            //String pagePath = rootDir + File.separator + resourceDirRel 
            //    + File.separator + (pages.get(sel)).getCfgPath();
            //String line = FileUtilities.read(pagePath, "UTF-8");
            webEditor.textAreaSetText("contentTA", line);
            webEditor.textAreaSetCaretPosition("contentTA", 0);
            webEditor.buttonSetEnabled("save", false);
            webEditor.textAreaRequestFocus("contentTA");
        }
    }
    
    //Menu Listener Group
    //Framework/Stylesheet Listener
    private class MenuTemplateActionListener implements ActionListener {
        private String filePath;
        public MenuTemplateActionListener() {
        }
        public MenuTemplateActionListener(String filePath) {
            this.filePath = filePath;
        }
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
        @Override
        public void actionPerformed (ActionEvent event) {
            if (filePath == null) {
                throw new NullPointerException("Null filePath to menu Item");
            }
            FileEditorController fileEditorController = null;
            try {
                fileEditorController = new FileEditorController(filePath);
            } catch (IOException ex) {
                System.out.println("IOException when launching menu editor");
                ex.printStackTrace();
            } catch(Exception ex) {
                System.out.println("Exception when launching menu editor");
                ex.printStackTrace();
            }
            fileEditorController.start();
        }
    }
    //PageItem Listener
    private class PageItemActionListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {            
            PagesCfgController pagesCfgController = null;
            try {
                pagesCfgController = new PagesCfgController(pagesCfgTxt, new ReloadCfg());
            } catch (IOException ex) {
                System.out.println("IOException when launching page config");
                ex.printStackTrace();
            } catch(Exception ex) {
                System.out.println("Exception when launching page config");
                ex.printStackTrace();
            }
            pagesCfgController.start();
        }
    }
    //InfoItem Listener
    private class InfoItemActionListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            SiteInfoCfgController siteInfoCfgController = null;
            try {
                siteInfoCfgController = new SiteInfoCfgController(websiteURLCfg, websiteNameCfg, new ReloadCfg());
            } catch (IOException ex) {
                System.out.println("IOException when launching site info config");
                ex.printStackTrace();
            } catch(Exception ex) {
                System.out.println("Exception when launching site info config");
                ex.printStackTrace();
            }
            siteInfoCfgController.start();            
        }
    }
    
    //Button Listener Group
    //Selection Listener
    private class SelectRootActionListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            JFileChooser fileOpen = webEditor.frameFileChooser("webEditorGUI", 
                JFileChooser.DIRECTORIES_ONLY, null);
            if (fileOpen.getSelectedFile() == null) { //Cancelled
                return;
            }
            rootDir = fileOpen.getSelectedFile().toString();
            FileUtilities.write(rootCfgRel, rootDir, "UTF-8");
            init();
        }
    }    
    
    //Edit
    //Edit function
    private void editAction() {
        String chosen = webEditor.textAreaGetSelectedText("contentTA");
        if (chosen == null) {
            return;
        }
        HashMap<String, String> designSetCopy = new HashMap<String, String>(designSet);
        ContentParser cP = new ContentParser(designSetCopy);
        Matcher m = cP.patternTypeIdFind(chosen);
        if (m.find()) {
            String type = m.group(1).toUpperCase();
            int id = Integer.parseInt(m.group(2));
            String pageName = pages.get(currentPage).getPageName();
            WebModuleDefault module = null;
            switch(type) {
                case "TITLE": 
                    module = new WebModuleTitle(designSetCopy, pageName, id);
                    break;
                case "PARAGRAPH": 
                    module = new WebModuleParagraph(designSetCopy, pageName, id);
                    break;
                case "CODE": 
                    module = new WebModuleCode(designSetCopy, pageName, id);
                    break;
                case "FILE": 
                    module = new WebModuleFile(designSetCopy, pageName, id);
                    break;
                case "IMAGE": 
                    module = new WebModuleImage(designSetCopy, pageName, id);
                    break;
                case "GALLERY": 
                    module = new WebModuleGallery(designSetCopy, pageName, id);
                    break;
                case "DIVIDER": 
                    module = new WebModuleDivider(designSetCopy, pageName, id);
                    break;
                default:
                    return;
            }
            module.startEditor();
        }        
    }
    //Delete action
    private void deleteAction() {
    }
    //Rename action
    private void renameAction() {
    }
    //Edit button Listener
    private class EditActionListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            editAction();
        }
    }
    //Delete button Listener
    private class DeleteActionListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            deleteAction();
        }
    }
    //Rename button Listener
    private class RenameActionListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            renameAction();
        }
    }    
    //Insert ActionListener Group
    //Insert: Get next available ID to use
    private int getNextID(WebModuleEnum typeEnum) {
        String type = typeEnum.getValue(); //getEnumVal(typeEnum);
        String text = webEditor.textAreaGetText("contentTA");
        int id = 1;
        HashMap<String, String> designSetCopy = new HashMap<String, String>(designSet);
        ContentParser cP = new ContentParser(designSetCopy);
        while (cP.patternExists(text, type, id)) {
            id++;            
        }
        cP = null;
        return id;
    }
    //Insert Action Listeners
    //Base inner InsertActionListener class
    private class InsertActionListener implements ActionListener {
        private WebModuleEnum typeEnum = WebModuleEnum.CODE;
        public InsertActionListener(WebModuleEnum typeEnum) {
            super();
            if (typeEnum == null) {
                throw new NullPointerException("Null type for insert");
            }
            this.typeEnum = typeEnum;
        }
        @Override
        public void actionPerformed(ActionEvent event) {
            String type = getEnumVal(typeEnum);
            int id = getNextID(typeEnum);
            String s = webEditor.textAreaGetText("contentTA");
            int location = webEditor.textAreaGetCaretPosition("contentTA");
            String newStr = " <<<###_" + type + "_" + id + "_###>>> \n";
            s = s.substring(0, location) 
                + newStr
                + s.substring(location, s.length());
            webEditor.textAreaSetText("contentTA", s);
            webEditor.textAreaSetSelection("contentTA", location, location + newStr.length());
            webEditor.buttonClick("edit");
        }
    }
    
    //Save action
    private void saveAction() {
        if (pages == null) {
            throw new NullPointerException("Null pages");
        }
        if (pages.size() > 0) {
            pages.get(currentPage).save(webEditor.textAreaGetText("contentTA"));
            webEditor.buttonSetEnabled("save", false);
            webEditor.labelSetText("statusLb", "Saved Page " + pages.get(currentPage).getPageName() + ".");
        } 
    }
    //Compile action
    private void compileAction() {
        if (pages == null) {
            throw new NullPointerException("Null pages");
        }
        if (currentPage >= 0 && pages.size() > 0) {
            webEditor.buttonClick("save");
        }
        if (pages.size() > 0) {
            webEditor.labelSetText("statusLb", "Compiling...");
            String navTextMain = compileBuildNav(0);
            String navTextMobile = compileBuildNav(1);           
            
            for (int i = 0; i < pages.size(); i++) {
                webEditor.labelSetText("statusLb", "Compiling Page " 
                    + pages.get(i).getPageName() + " ... " 
                    + "(" + (i+1) + "/" + pages.size() + ")");
                pages.get(i).compileMain(frameworkCfg, navTextMain);
                pages.get(i).compileMobile(mobileFrameworkCfg, navTextMobile);
            }
            webEditor.labelSetText("statusLb", "Compiled " + pages.size() + " Pages.");
        }
        //Copy images resource to website folder
        String sourceImageDirStr = "images";
        String destImageDirStr = rootDir
            + File.separator + websiteDirRel
            + File.separator + "images";
        File sourceImageDir = new File(sourceImageDirStr);
        File[] allImages = sourceImageDir.listFiles();
        File destImageDir = new File(destImageDirStr);
        for (File f : allImages) {
            File d = new File(destImageDir + File.separator + f.getName());
            if (!d.exists()) {
                try {
                    FileUtilities.copyFile(f.getAbsolutePath(), d.getAbsolutePath(), false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    private String compileBuildNav(int webType) {
        //webType: 0 - main PC html;  1 - mobile php
        //Build navigation panel
        StringBuilder navListText = new StringBuilder("");
        //for (int i = 0; i < pages.size(); i++) {
        //    System.out.println(pages.get(i).getLevel() + ":" + pages.get(i).getPageName() + ", " + pages.get(i).getPageTitle() + ": " + pages.get(i).getDisplayInNav());
        //}
        switch(webType) {
            case 0:  //main PC html
                for (int i = 0; i < pages.size(); i++) {
                    if (pages.get(i).getDisplayInNav()) {
                        navListText.append("\n<tr><td class=\"nav-entry\"><a class=\"nav\" href=\""
                            + pages.get(i).getPageName() + ".html\">" + pages.get(i).getPageTitle() + "</a></td></tr>");
                    }
                }
                break;
            case 1:  //mobile php
                for (int i = 0; i < pages.size(); i++) {
                    if (pages.get(i).getDisplayInNav()) {
                        navListText.append("<option value=\"/mobile/" 
                            + pages.get(i).getPageName() + ".php\">" + pages.get(i).getPageTitle() + "</option>");
                    }
                }
                break;
        }
        return (new String(navListText));
    }
    
    
    //Save button Listener
    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            saveAction();
        }
    }
    //Compile button Listener
    private class CompileActionListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent event) {
            compileAction();
        }
    }

    //textArea listener
    //Document Edit Listener
    private class EditDocumentListener implements DocumentListener {
        @Override
        public void changedUpdate(DocumentEvent event) {
            webEditor.buttonSetEnabled("save", true);
        }
        public void insertUpdate(DocumentEvent event) {
            webEditor.buttonSetEnabled("save", true);
        }
        public void removeUpdate(DocumentEvent event) {
            webEditor.buttonSetEnabled("save", true);
        }
    }
    
    
    //Generic information fetch
    
    //enable/disable all menuItems
    private void menuItemsSetEnabledAll(boolean enable) {
        webEditor.menuItemSetEnabled("frameworkItem", enable);
        webEditor.menuItemSetEnabled("styleItem", enable);
        webEditor.menuItemSetEnabled("mobileFrameworkItem", enable);
        webEditor.menuItemSetEnabled("mobileStyleItem", enable);
        webEditor.menuItemSetEnabled("pageItem", enable);
        webEditor.menuItemSetEnabled("infoItem", enable);
    }

    private String getEnumVal(WebModuleEnum e) {
        switch(e) {
            case TITLE: return ("TITLE");
            case PARAGRAPH: return ("PARAGRAPH");
            case CODE: return ("CODE");
            case FILE: return ("FILE");
            case IMAGE: return ("IMAGE");
            case GALLERY: return ("GALLERY");
            case DIVIDER: return ("DIVIDER");            
        }
        return null;
    }
 
    public static void main(String[] args) {
        WebEditorController wEController = new WebEditorController();
        wEController.start();
    }
}