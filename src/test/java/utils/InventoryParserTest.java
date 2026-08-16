package utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryParserTest {
    @TempDir Path directory;

    @Test
    void parsesValidInventoryXml() throws Exception {
        var result = InventoryParser.parse(xml("""
            <Root><Supplier><SupplierName>Acme</SupplierName>
            <InventoryItem><Name>Gloves</Name><SerialNumber>G-1</SerialNumber>
            <Quantity>10</Quantity><LowStockAlertThreshold>2</LowStockAlertThreshold>
            <ExpirationDate>2027-01-01</ExpirationDate></InventoryItem>
            </Supplier></Root>
            """));
        assertEquals(1, result.acceptedItems());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void rejectsMissingFieldsNegativeValuesMalformedDatesAndDuplicates() throws Exception {
        var result = InventoryParser.parse(xml("""
            <Root><Supplier><SupplierName>Acme</SupplierName>
            <InventoryItem><Name></Name><SerialNumber>A</SerialNumber><Quantity>1</Quantity><LowStockAlertThreshold>1</LowStockAlertThreshold></InventoryItem>
            <InventoryItem><Name>X</Name><SerialNumber>B</SerialNumber><Quantity>-1</Quantity><LowStockAlertThreshold>1</LowStockAlertThreshold></InventoryItem>
            <InventoryItem><Name>X</Name><SerialNumber>C</SerialNumber><Quantity>1</Quantity><LowStockAlertThreshold>1</LowStockAlertThreshold><ExpirationDate>bad</ExpirationDate></InventoryItem>
            <InventoryItem><Name>X</Name><SerialNumber>D</SerialNumber><Quantity>1</Quantity><LowStockAlertThreshold>1</LowStockAlertThreshold></InventoryItem>
            <InventoryItem><Name>X</Name><SerialNumber>D</SerialNumber><Quantity>1</Quantity><LowStockAlertThreshold>1</LowStockAlertThreshold></InventoryItem>
            </Supplier></Root>
            """));
        assertEquals(1, result.acceptedItems());
        assertEquals(4, result.skippedItems());
        assertEquals(4, result.errors().size());
    }

    @Test
    void blocksExternalEntities() throws Exception {
        var result = InventoryParser.parse(xml("""
            <!DOCTYPE root [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
            <Root><Supplier><SupplierName>&xxe;</SupplierName></Supplier></Root>
            """));
        assertTrue(result.suppliers().isEmpty());
        assertTrue(result.hasErrors());
    }

    private java.io.File xml(String content) throws Exception {
        Path file = directory.resolve("inventory-" + System.nanoTime() + ".xml");
        Files.writeString(file, content);
        return file.toFile();
    }
}
