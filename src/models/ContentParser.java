/* File Utilities 
*/
/* Author: Wei Zhang
   Latest Version: 2016 Jan 18
*/
/*API
class ContentParser {
    public ContentParser()
    
}
*/

package models;
import utilities.*;
import controllers.DesignInfoSet;

import java.util.regex.*;
import java.io.*;
import java.util.HashMap;

public class ContentParser {
    private DesignInfoSet designInfoSet;
    public ContentParser(DesignInfoSet designInfoSet) { //for compiling
        setDesignInfoSet(designInfoSet);
    }
    public void setDesignInfoSet(DesignInfoSet designInfoSet) {
        this.designInfoSet = new DesignInfoSet(designInfoSet);
    }
    //Check if the given Type_Id section exists in the current text
    public boolean patternExists(String text, String target) {
        String pattern = "(?i)<<<###_" + target + "_###>>>";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        return(m.find());
    }
    public boolean patternExists(String text, String type, int id) {
        String target = type + "_" + id;
        return(patternExists(text, target));
    }
    //Find id from the given text of the given type
    //Returns the integer id; -1 means no match    
    public int patternIdFind(String text, String type) {
        String pattern = "(?i)<<<###_" + type + "_" + "(\\d+)" + "_###>>>";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return(Integer.parseInt(m.group(1)));
        } else {
            return(-1);
        }
    }
    
    //Find Type_Id sections from the given text
    //Returns the matcher object m; m.group(1) is the type, m.group(2) is the id 
    public Matcher patternTypeIdFind(String text) {
        String pattern = "(?i)<<<###_" + "([0-9a-zA-Z_]+)" + "_" + "(\\d+)" + "_###>>>";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        return(m);
    }
    
    //Remove the first pattern found in the text
    public String removeFirstPattern(String text) {
        String pattern = "(?i)<<<###_" + "([0-9a-zA-Z_]+" + "_" + "\\d+)" + "_###>>>";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            int start = m.start();
            int end = m.end();
            if (end < text.length() - 1) {
                return text.substring(0, start) + text.substring(end + 1, text.length());
            } else {
                return text.substring(0, start);
            }
        } else {
            return text;
        }
    }
    
    //Framework compile
    
    //MainText
    public String compileMainText(String text, String pageCfg) {
        String mainText = FileUtilities.read(pageCfg, "UTF-8");
        if (mainText == null) {
            mainText = "";
        }
        String compiledText = text.replace("<<<###_MAINTEXT_###>>>", insertMarker(mainText, "MAINTEXT"));
        return(compiledText);
    }
    //Navigation
    public String compileNavigation(String text, String navText) {
        String compiledText = text.replace("<<<###_NAVIGATION_###>>>", insertMarker(navText, "NAVIGATION"));
        return(compiledText);
    }
    
    //WebModule Compile
    //Compile routine
    private String compileRoutine(String text, String pageName, WebModuleEnum typeEnum) {
        if (text == null) {
            throw new NullPointerException("Null text when compiling " + typeEnum.getValue());
        }
        if (pageName == null) {
            throw new NullPointerException("Null pageName when compiling " + typeEnum.getValue());
        }
        if (typeEnum == null) {
            throw new NullPointerException("Null typeEnum when compiling " + typeEnum.getValue());
        }
        String compiledText = new String(text);
        int id = patternIdFind(compiledText, typeEnum.getValue());
        while (id >= 1) {
            WebModuleDefault module = null;
            switch (typeEnum) {
                case TITLE: 
                    module = new WebModuleTitle(new DesignInfoSet(designInfoSet), pageName, id);
                    break;
                case PARAGRAPH: 
                    module = new WebModuleParagraph(new DesignInfoSet(designInfoSet), pageName, id);
                    break;
                case CODE: 
                    module = new WebModuleCode(new DesignInfoSet(designInfoSet), pageName, id);
                    break;
                case FILE: 
                    module = new WebModuleFile(new DesignInfoSet(designInfoSet), pageName, id);
                    break;
                case IMAGE: 
                    module = new WebModuleImage(new DesignInfoSet(designInfoSet), pageName, id);
                    break;
                case GALLERY: 
                    module = new WebModuleGallery(new DesignInfoSet(designInfoSet), pageName, id);
                    break;
                case DIVIDER: 
                    module = new WebModuleDivider(new DesignInfoSet(designInfoSet), pageName, id);
                    break;
                default:
                    throw new IllegalArgumentException("Type " 
                        + typeEnum.getValue() + " is not legal");    
            }
            compiledText = compiledText.replaceAll("(?i)" + module.getTargetWithMarker(), module.getMarkedContent());
            id = patternIdFind(compiledText, typeEnum.getValue());
        }        
        return(compiledText);
    }
    //Title
    public String compileTitles(String text, String pageName) {
        return compileRoutine(text, pageName, WebModuleEnum.TITLE);
    }    
    //Paragraph
    public String compileParagraphs(String text, String pageName) {
        return compileRoutine(text, pageName, WebModuleEnum.PARAGRAPH);
    }
    //Code
    public String compileCodes(String text, String pageName) {
        return compileRoutine(text, pageName, WebModuleEnum.CODE);
    }
    //File
    public String compileFiles(String text, String pageName) {
        return compileRoutine(text, pageName, WebModuleEnum.FILE);
    }
    //Image
    public String compileImages(String text, String pageName) {
        return compileRoutine(text, pageName, WebModuleEnum.IMAGE);
    }
    //Gallery
    public String compileGalleries(String text, String pageName) {
        return compileRoutine(text, pageName, WebModuleEnum.GALLERY);
    }
    //Divider
    public String compileDividers(String text, String pageName) {
        return compileRoutine(text, pageName, WebModuleEnum.DIVIDER);
    }
    //Insert identifier 
    private String insertMarker(String textItem, String target) {
        return("<!-- " + target + " START -->" 
            + textItem 
            + "<!-- " + target + " END -->");
    }
    private String insertMarker(String text, WebModuleEnum typeEnum, int id) {
        return(insertMarker(text, typeEnum.getValue() + "_" + id));
    }    
    
}