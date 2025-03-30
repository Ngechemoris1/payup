package payup.payup.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import payup.payup.exception.ReportGenerationException;
import payup.payup.model.Bill;
import payup.payup.model.Payment;
import payup.payup.model.Property;
import payup.payup.model.Room;
import payup.repository.BillRepository;
import payup.repository.PaymentRepository;
import payup.repository.PropertyRepository;
import payup.repository.RoomRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private static final int PAGE_SIZE = 100;

    @Autowired private BillRepository billRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PropertyRepository propertyRepository;
    @Autowired private RoomRepository roomRepository;

    @Cacheable("unpaidDuesReport")
    public Map<Long, Double> generateUnpaidDuesReport(int page) {
        logger.info("Generating unpaid dues report for page {}", page);
        try {
            validatePage(page);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Bill> unpaidBillsPage = billRepository.findByIsPaidFalse(pageable);
            Map<Long, Double> unpaidDues = unpaidBillsPage.stream()
                    .collect(Collectors.groupingBy(bill -> bill.getTenant().getId(), Collectors.summingDouble(Bill::getAmount)));
            logger.debug("Generated unpaid dues report: {} entries", unpaidDues.size());
            return unpaidDues;
        } catch (Exception e) {
            logger.error("Failed to generate unpaid dues report: {}", e.getMessage());
            throw new ReportGenerationException("Error generating unpaid dues report", e);
        }
    }

    @Cacheable("totalPaymentsReport")
    public Map<Long, Double> generateTotalPaymentsReport(int page) {
        logger.info("Generating total payments report for page {}", page);
        try {
            validatePage(page);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Payment> paymentsPage = paymentRepository.findAll(pageable);
            Map<Long, Double> totalPayments = paymentsPage.stream()
                    .collect(Collectors.groupingBy(payment -> payment.getTenant().getId(), Collectors.summingDouble(payment -> payment.getAmount().doubleValue())));
            logger.debug("Generated total payments report: {} entries", totalPayments.size());
            return totalPayments;
        } catch (Exception e) {
            logger.error("Failed to generate total payments report: {}", e.getMessage());
            throw new ReportGenerationException("Error generating total payments report", e);
        }
    }

    @Cacheable("occupancyRatesReport")
    public Map<Long, Double> generateOccupancyRatesReport() {
        logger.info("Generating occupancy rates report");
        try {
            List<Property> properties = propertyRepository.findAll();
            Map<Long, Double> occupancyRates = properties.stream()
                    .collect(Collectors.toMap(
                            Property::getId,
                            property -> {
                                List<Room> rooms = roomRepository.findByProperty(property);
                                long totalRooms = rooms.size();
                                long occupiedRooms = rooms.stream().filter(Room::isOccupied).count();
                                return totalRooms == 0 ? 0.0 : (double) occupiedRooms / totalRooms * 100;
                            }));
            logger.debug("Generated occupancy rates report: {} entries", occupancyRates.size());
            return occupancyRates;
        } catch (Exception e) {
            logger.error("Failed to generate occupancy rates report: {}", e.getMessage());
            throw new ReportGenerationException("Error generating occupancy rates report", e);
        }
    }

    public Map<Long, Double> generateRevenueCollectionReport() {
        logger.info("Generating revenue collection report");
        try {
            List<Payment> payments = paymentRepository.findAll();
            Map<Long, Double> revenueByProperty = payments.stream()
                    .collect(Collectors.groupingBy(payment -> payment.getTenant().getProperty().getId(), 
                            Collectors.summingDouble(payment -> payment.getAmount().doubleValue())));
            logger.debug("Generated revenue collection report: {} entries", revenueByProperty.size());
            return revenueByProperty;
        } catch (Exception e) {
            logger.error("Failed to generate revenue collection report: {}", e.getMessage());
            throw new ReportGenerationException("Error generating revenue collection report", e);
        }
    }

    public List<Payment> generateTenantPaymentHistoryReport(Long tenantId) {
        logger.info("Generating payment history report for tenantId={}", tenantId);
        if (tenantId == null) {
            logger.error("Tenant ID cannot be null");
            throw new IllegalArgumentException("Tenant ID must not be null");
        }
        List<Payment> payments = paymentRepository.findByTenantId(tenantId);
        logger.debug("Generated payment history report: {} entries", payments.size());
        return payments;
    }

    public Map<Long, Map<String, Double>> generatePropertyPerformanceReport() {
        logger.info("Generating property performance report");
        try {
            Map<Long, Double> occupancyRates = generateOccupancyRatesReport();
            Map<Long, Double> revenueCollection = generateRevenueCollectionReport();
            Map<Long, Map<String, Double>> performanceReport = new HashMap<>();
            occupancyRates.forEach((propertyId, rate) -> {
                Map<String, Double> metrics = new HashMap<>();
                metrics.put("occupancyRate", rate);
                metrics.put("revenue", revenueCollection.getOrDefault(propertyId, 0.0));
                performanceReport.put(propertyId, metrics);
            });
            logger.debug("Generated property performance report: {} entries", performanceReport.size());
            return performanceReport;
        } catch (Exception e) {
            logger.error("Failed to generate property performance report: {}", e.getMessage());
            throw new ReportGenerationException("Error generating property performance report", e);
        }
    }

    public byte[] exportToExcel(Map<?, ?> reportData, String[] headers) throws IOException {
        logger.info("Exporting report to Excel");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderCellStyle(workbook));
            }
            int rowNum = 1;
            for (Map.Entry<?, ?> entry : reportData.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey().toString());
                row.createCell(1).setCellValue(entry.getValue().toString());
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            logger.debug("Exported report to Excel: {} rows", rowNum - 1);
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Failed to export report to Excel: {}", e.getMessage());
            throw e;
        }
    }

    public byte[] exportToPdf(Map<?, ?> reportData, String title) throws DocumentException, IOException {
        logger.info("Exporting report to PDF: title={}", title);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph(title));
            document.add(new Paragraph("\n"));
            for (Map.Entry<?, ?> entry : reportData.entrySet()) {
                document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
            }
            document.close();
            logger.debug("Exported report to PDF");
            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            logger.error("Failed to export report to PDF: {}", e.getMessage());
            throw new RuntimeException("Error exporting report to PDF", e);
        }
    }

    private void validatePage(int page) {
        if (page < 0) {
            logger.error("Invalid page number: {}", page);
            throw new IllegalArgumentException("Page number must be non-negative");
        }
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle headerCellStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerCellStyle.setFont(headerFont);
        return headerCellStyle;
    }
}