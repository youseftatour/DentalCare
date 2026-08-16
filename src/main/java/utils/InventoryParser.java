package utils;

import entity.InventoryItem;
import entity.Supplier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InventoryParser {

    public record ParseResult(Map<String, Supplier> suppliers, int acceptedItems,
                              int skippedItems, List<String> errors) {
        public boolean hasErrors() { return !errors.isEmpty(); }
    }

    private InventoryParser() {
    }

    public static Map<String, Supplier> parseSuppliersWithItems(File xmlFile) {
        return parse(xmlFile).suppliers();
    }

    public static ParseResult parse(File xmlFile) {
        if (xmlFile == null || !xmlFile.isFile()) {
            return new ParseResult(Collections.emptyMap(), 0, 0,
                List.of("XML file does not exist."));
        }

        Map<String, Supplier> suppliers = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        Set<String> serialNumbers = new HashSet<>();
        int accepted = 0;
        int skipped = 0;

        try {
            DocumentBuilderFactory factory = createSecureFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXParseException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXParseException {
                    throw exception;
                }
            });
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
                    errors.add("Supplier " + (i + 1) + " is missing SupplierName.");
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
                        errors.add("Supplier " + name + " contains an item missing Name or SerialNumber.");
                        skipped++;
                        continue;
                    }

                    if (!serialNumbers.add(serial)) {
                        errors.add("Duplicate SerialNumber in XML: " + serial);
                        skipped++;
                        continue;
                    }

                    Integer quantity = parseNonNegativeInt(getTagValue(itemElement, "Quantity"));
                    Integer threshold = parseNonNegativeInt(
                        getTagValue(itemElement, "LowStockAlertThreshold"));
                    DateValue expiry = parseOptionalDate(getTagValue(itemElement, "ExpirationDate"));
                    if (quantity == null || threshold == null || !expiry.valid()) {
                        errors.add("Item " + serial + " has an invalid quantity, threshold, or date.");
                        skipped++;
                        continue;
                    }

                    supplier.addItem(new InventoryItem(
                        0,
                        itemName,
                        description,
                        quantity,
                        name,
                        expiry.value(),
                        serial,
                        threshold
                    ));
                    accepted++;
                }

                suppliers.put(name, supplier);
            }

            return new ParseResult(Collections.unmodifiableMap(suppliers), accepted, skipped,
                List.copyOf(errors));

        } catch (Exception e) {
            utils.AppLogger.warn(InventoryParser.class,
                "Inventory XML was rejected: {}", e.getMessage());
            return new ParseResult(Collections.emptyMap(), 0, 0,
                List.of("XML could not be parsed safely."));
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

    private static Integer parseNonNegativeInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static DateValue parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return new DateValue(null, true);
        }

        try {
            return new DateValue(LocalDate.parse(value), true);
        } catch (DateTimeParseException e) {
            return new DateValue(null, false);
        }
    }

    private record DateValue(LocalDate value, boolean valid) { }
}


