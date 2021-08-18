import com.bazayer.model.Shipment;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import static com.bazayer.utils.ReportingUtils.getDateFormat;
import static com.bazayer.utils.ReportingUtils.writeCsv;

public class RunTrackingNumberReport {

    private static String source = "src/main/resources/csv-input";
    private static String output = "/Users/ahamraoui/o2";

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            source = args[0];
        }
        if (args.length > 1) {
            output = args[1];
        }

        List<Shipment> shipments = buildShipments();
        List<Shipment> shipmentsWithoutDuplicateTrackingNumber = shipments.stream().distinct().collect(Collectors.toList());
        List<Shipment> shipmentsWithoutNullPhoneValues = shipmentsWithoutDuplicateTrackingNumber.stream()
                .filter(shipment -> StringUtils.isNotBlank(shipment.getRecipientPhoneNumber())).collect(Collectors.toList());

        System.out.println("List without recipient'sphone number : ");
        System.out.println("#######################################");
        shipmentsWithoutDuplicateTrackingNumber.stream()
                .filter(shipment -> StringUtils.isBlank(shipment.getRecipientPhoneNumber()))
                .collect(Collectors.toList()).forEach(System.out::println);
        System.out.println("#######################################");
        System.out.println("Duplicates : ");
        System.out.println("#######################################");
        shipments.stream().filter(i -> Collections.frequency(shipments, i) >1)
                .collect(Collectors.toSet()).forEach(System.out::println);

        buildCsvData(shipmentsWithoutNullPhoneValues, "O2_report");
    }

    private static List<Shipment> buildShipments() {
        List<Shipment> shipments = new ArrayList<>();
        Iterator it = FileUtils.iterateFiles(new File(source), null, false);
        while (it.hasNext()) {
            List<Shipment> itData = buildShipments((File)it.next());
            shipments.addAll(itData);
        }
        return shipments;
    }
    private static void buildCsvData(List<Shipment> shipments, String fileName) throws Exception {
        String updateDate = getDateFormat();
        String[] headers = {"recipientFullName", "recipientPhoneNumber", "address", "addressCity", "addressState",
                "addressZip", "trackingNumber", "deliveryCompany", "trackingUrl", "update_date", "last8phone"};

        List<String[]> stringArray = new ArrayList();
        stringArray.add(headers);
        shipments.forEach(shipment -> stringArray.add(new String[] {
                retrieveWithDefaultValue(retrieveRecipientFullName(shipment)),
                retrieveWithTrim(retrievePhoneNumber(shipment.getRecipientPhoneNumber())),
                retrieveWithDefaultValue(retrieveAddress(shipment.getAddress())),
                retrieveWithDefaultValue(shipment.getAddressCity()),
                retrieveWithDefaultValue(shipment.getAddressState()),
                retrieveWithTrim(shipment.getAddressZip()),
                retrieveWithTrim(shipment.getTrackingNumber()),
                retrieveWithDefaultValue(retrieveCompanyDelivery(shipment.getDeliveryCompany())),
                retrieveWithDefaultValue(retrieveTrackingUrl(retrieveWithTrim(shipment.getTrackingNumber()), shipment.getDeliveryCompany())),
                updateDate,
                retrieveWithTrim(retrieveLast8PhoneNumber(shipment.getRecipientPhoneNumber()))
        }));
        writeCsv(stringArray, fileName, output);
    }

    private static String retrievePhoneNumber(String phone) {
        if (StringUtils.isNotBlank(phone)) {
            phone = phone.replaceAll("\\n", "");
            phone = phone.replaceAll("\\r", "");
            phone = phone.replaceAll("\\(", "");
            phone = phone.replaceAll("\\)", "");
            phone = phone.replaceAll("\\+", "");
            phone = phone.replaceAll(" ", "");
        }
        return phone;
    }

    private static String retrieveLast8PhoneNumber(String phoneValue) {
        String phone = retrievePhoneNumber(phoneValue);
        if (StringUtils.isNotBlank(phone)) {
            String trimPhone = phone.trim();
            if (trimPhone.length() >= 8) {
                String last8phoneNumber = trimPhone.substring(trimPhone.length() - 8);
                return last8phoneNumber;
            }
            return trimPhone;
        }
        return phone;
    }

    private static String retrieveAddress(String address) {
        if (StringUtils.isNotBlank(address)) {
            address = address.replaceAll("\\n", "");
            address = address.replaceAll("\\r", "");
        }
        return address;
    }

    private static String retrieveWithTrim(String value) {
        if (StringUtils.isNotBlank(value)) {
            String trimString = value.trim();
            return StringUtils.deleteWhitespace(trimString);
        }
        return " ";
    }


    private static String retrieveWithDefaultValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return " ";
    }

    private static String retrieveRecipientFullName(Shipment shipment) {
        String fullName;
        if (StringUtils.isNotBlank(shipment.getRecipientFullName())) {
            fullName = shipment.getRecipientFullName();
        } else {
            fullName = shipment.getFirstName() + " " + shipment.getLastName();
        }
        fullName = fullName.replaceAll("\\n", "");
        fullName = fullName.replaceAll("\\r", "");

        return fullName;
    }

    private static String retrieveCompanyDelivery(String companyDelivery) {
        if (StringUtils.isBlank(companyDelivery)) {
            return "UPS";
        }
        return companyDelivery;
    }
    private static String retrieveTrackingUrl(String trackingNumber, String companyDelivery) {
        if ("UPS".equalsIgnoreCase(companyDelivery)) {
            return "https://www.ups.com/track?loc=fr_FR&tracknum=" + trackingNumber + "&requester=WT/trackdetails";
        }

        if ("DHL".equalsIgnoreCase(companyDelivery)) {
            return "https://www.dhl.com/fr-fr/home/suivi.html?tracking-id=" + trackingNumber;
        }

        return "https://www.ups.com/track?loc=fr_FR&tracknum=" + trackingNumber + "&requester=WT/trackdetails";
    }

    private static List<Shipment> buildShipments(File file) {
        try {
            Reader shipmentReader = new BufferedReader(new FileReader(file));
            CsvToBean<Shipment> csvShipmentReader = new CsvToBeanBuilder(shipmentReader)
                    .withType(Shipment.class)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return csvShipmentReader.parse();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
