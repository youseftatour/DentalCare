package utils;

import entity.InventoryItem;
import entity.Supplier;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class InventoryParser {

    public static Map<String, Supplier> parseSuppliersWithItems(File xmlFile) {
        Map<String, Supplier> suppliers = new LinkedHashMap<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList supplierNodes = doc.getElementsByTagName("Supplier");
            for (int i = 0; i < supplierNodes.getLength(); i++) {
                Node supplierNode = supplierNodes.item(i);
                if (supplierNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element supplierEl = (Element) supplierNode;

                    String name = getTagValue(supplierEl, "SupplierName");
                    String email = getTagValue(supplierEl, "Email");
                    String phone = getTagValue(supplierEl, "Phone");
                    String address = getTagValue(supplierEl, "Address");

                    Supplier supplier = new Supplier(name, email, phone, address);

                    NodeList itemNodes = supplierEl.getElementsByTagName("InventoryItem");
                    for (int j = 0; j < itemNodes.getLength(); j++) {
                        Node itemNode = itemNodes.item(j);
                        if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element itemEl = (Element) itemNode;

                            String itemName = getTagValue(itemEl, "Name");
                            String description = getTagValue(itemEl, "Description");
                            int quantity = Integer.parseInt(getTagValue(itemEl, "Quantity"));
                            String expiryStr = getTagValue(itemEl, "ExpirationDate");
                            LocalDate expiryDate = expiryStr.isEmpty() ? null : LocalDate.parse(expiryStr);
                            String serial = getTagValue(itemEl, "SerialNumber");
                            int threshold = Integer.parseInt(getTagValue(itemEl, "LowStockAlertThreshold"));

                            InventoryItem item = new InventoryItem(
                                    0,
                                    itemName,
                                    description,
                                    quantity,
                                    name, // supplierName
                                    expiryDate,
                                    serial,
                                    threshold
                            );

                            supplier.addItem(item);
                        }
                    }

                    suppliers.put(name, supplier);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }

        return suppliers;
    }

    private static String getTagValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return "";
        Node node = list.item(0);
        return node != null ? node.getTextContent().trim() : "";
    }
}
