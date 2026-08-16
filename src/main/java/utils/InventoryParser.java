package utils;

import entity.InventoryItem;
import entity.Supplier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class InventoryParser {

    private InventoryParser() {
    }

    public static Map<String, Supplier> parseSuppliersWithItems(File xmlFile) {
        if (xmlFile == null || !xmlFile.isFile()) {
            return Collections.emptyMap();
        }

        Map<String, Supplier> suppliers = new LinkedHashMap<>();

        try {
            DocumentBuilderFactory factory = createSecureFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList supplierNodes = doc.getElementsByTagName("Supplier");

            for (int i = 0; i < supplierNodes.getLength(); i++) {
                Node supplierNode = supplierNodes.item(i);

                if (supplierNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element supplierElement = (Element) supplierNode;

                String name = getTagValue(supplierElement, "SupplierName");
                if (name.isBlank()) {
                    continue;
                }

                String email = getTagValue(supplierElement, "Email");
                String phone = getTagValue(supplierElement, "Phone");
                String address = getTagValue(supplierElement, "Address");

                Supplier supplier = new Supplier(name, email, phone, address);

                NodeList itemNodes = supplierElement.getElementsByTagName("InventoryItem");

                for (int j = 0; j < itemNodes.getLength(); j++) {
                    Node itemNode = itemNodes.item(j);

                    if (itemNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element itemElement = (Element) itemNode;

                    String itemName = getTagValue(itemElement, "Name");
                    String description = getTagValue(itemElement, "Description");
                    String serial = getTagValue(itemElement, "SerialNumber");

                    if (itemName.isBlank() || serial.isBlank()) {
                        continue;
                    }

                    int quantity = parseNonNegativeInt(
                        getTagValue(itemElement, "Quantity"), 0
                    );

                    int threshold = parseNonNegativeInt(
                        getTagValue(itemElement, "LowStockAlertThreshold"), 0
                    );

                    LocalDate expiryDate = parseOptionalDate(
                        getTagValue(itemElement, "ExpirationDate")
                    );

                    supplier.addItem(new InventoryItem(
                        0,
                        itemName,
                        description,
                        quantity,
                        name,
                        expiryDate,
                        serial,
                        threshold
                    ));
                }

                suppliers.put(name, supplier);
            }

            return suppliers;

        } catch (Exception e) {
            utils.AppLogger.error(InventoryParser.class, "Inventory XML parsing failed", e);
            return Collections.emptyMap();
        }
    }

    private static DocumentBuilderFactory createSecureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (IllegalArgumentException ignored) {
            // Some parser implementations do not expose these attributes.
            // The parser features above still block external entities/DTDs.
        }

        return factory;
    }

    private static String getTagValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);

        if (list.getLength() == 0) {
            return "";
        }

        Node node = list.item(0);
        return node != null ? node.getTextContent().trim() : "";
    }

    private static int parseNonNegativeInt(String value, int defaultValue) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(parsed, 0);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}


