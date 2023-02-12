package dev.itsyourap.custombuildstepremover;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;

public class CustomBuildStepRemover {
    public static void main(String[] args) {
        String directory = System.getProperty("user.dir");
        boolean recursive = true;
        if (args.length > 0)
            directory = args[0];
        if (args.length > 1)
            recursive = Boolean.parseBoolean(args[1]);

        removeCustomBuildSteps(new File(directory), recursive);
    }

    private static void removeCustomBuildSteps(File directory, boolean recursive) {
        if (directory.exists() && directory.isDirectory() && directory.canWrite()) {
            File[] filesInDirectory = directory.listFiles();
            if (filesInDirectory != null)
                for (File file : filesInDirectory) {
                    if (file.isDirectory()) {
                        if (recursive)
                            removeCustomBuildSteps(file, true);
                    } else {
                        String extension = getExtensionFromFile(file);
                        if (extension != null)
                            if (extension.equals("vcxproj")) {
                                removeCustomBuildStep(file);
                            }
                    }
                }
        } else {
            throw new RuntimeException("Cannot Write to Directory");
        }
    }

    private static void removeCustomBuildStep(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            Element root = document.getDocumentElement();
            NodeList itemDefinitionsGroupNodes = root.getElementsByTagName("ItemDefinitionGroup");
            for (int i = 0; i < itemDefinitionsGroupNodes.getLength(); i++) {
                Node itemDefinitionsGroupNode = itemDefinitionsGroupNodes.item(i);
                NodeList nodeList = itemDefinitionsGroupNode.getChildNodes();
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Node node = nodeList.item(j);
                    if (node.getNodeName().equalsIgnoreCase("CustomBuildStep"))
                        itemDefinitionsGroupNode.removeChild(node);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new FileOutputStream(file));

            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getExtensionFromFile(File file) {
        if (file == null)
            return null;

        String fileName = file.getName();
        if (fileName.contains("."))
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else
            return null;
    }
}
