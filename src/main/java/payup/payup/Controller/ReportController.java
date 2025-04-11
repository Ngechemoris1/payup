package payup.payup.controller;

import com.itextpdf.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.KeyValueReportDto;
import payup.payup.dto.PaymentDto;
import payup.payup.dto.PropertyPerformanceReportDto;
import payup.payup.dto.TenantPaymentHistoryReportDto;
import payup.payup.exception.ReportGenerationException;
import payup.payup.mapper.PaymentMapper;
import payup.payup.model.Payment;
import payup.payup.service.ReportService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for generating and exporting reports in the PayUp system. Provides endpoints
 * for various report types (e.g., unpaid dues, total payments, occupancy rates) and export options
 * (Excel, PDF). Access is restricted based on user roles ('ADMIN' or 'LANDLORD').
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private PaymentMapper paymentMapper;

    /**
     * Generates a report of unpaid dues, showing amounts owed by tenants.
     *
     * @param page The page number for paginated results (default is 0).
     * @return ResponseEntity containing a KeyValueReportDto with tenant IDs mapped to unpaid dues and HTTP 200 (OK) status.
     * @throws ReportGenerationException if report generation fails.
     */
    @GetMapping("/unpaid-dues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KeyValueReportDto> getUnpaidDuesReport(@RequestParam(defaultValue = "0") int page) {
        logger.info("Generating unpaid dues report for page={}", page);
        validatePage(page);
        try {
            Map<Long, Double> report = reportService.generateUnpaidDuesReport(page);
            logger.debug("Unpaid dues report generated: {} entries", report.size());
            return ResponseEntity.ok(new KeyValueReportDto(report));
        } catch (ReportGenerationException e) {
            logger.error("Failed to generate unpaid dues report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KeyValueReportDto(Map.of(-1L, -1.0)));
        }
    }

    /**
     * Generates a report of total payments made by tenants.
     *
     * @param page The page number for paginated results (default is 0).
     * @return ResponseEntity containing a KeyValueReportDto with tenant IDs mapped to total payments and HTTP 200 (OK) status.
     * @throws ReportGenerationException if report generation fails.
     */
    @GetMapping("/total-payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KeyValueReportDto> getTotalPaymentsReport(@RequestParam(defaultValue = "0") int page) {
        logger.info("Generating total payments report for page={}", page);
        validatePage(page);
        try {
            Map<Long, Double> report = reportService.generateTotalPaymentsReport(page);
            logger.debug("Total payments report generated: {} entries", report.size());
            return ResponseEntity.ok(new KeyValueReportDto(report));
        } catch (ReportGenerationException e) {
            logger.error("Failed to generate total payments report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KeyValueReportDto(Map.of(-1L, Double.NaN)));
        }
    }

    /**
     * Generates a report of occupancy rates across properties.
     *
     * @return ResponseEntity containing a KeyValueReportDto with property IDs mapped to occupancy rates and HTTP 200 (OK) status.
     * @throws ReportGenerationException if report generation fails.
     */
    @GetMapping("/occupancy-rates")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<KeyValueReportDto> getOccupancyRatesReport() {
        logger.info("Generating occupancy rates report");
        try {
            Map<Long, Double> report = reportService.generateOccupancyRatesReport();
            logger.debug("Occupancy rates report generated: {} entries", report.size());
            return ResponseEntity.ok(new KeyValueReportDto(report));
        } catch (ReportGenerationException e) {
            logger.error("Failed to generate occupancy rates report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KeyValueReportDto(Map.of(-1L, Double.NaN)));
        }
    }

    /**
     * Generates a report of revenue collected from properties.
     *
     * @return ResponseEntity containing a KeyValueReportDto with property IDs mapped to total revenue and HTTP 200 (OK) status.
     * @throws ReportGenerationException if report generation fails.
     */
    @GetMapping("/revenue-collection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KeyValueReportDto> getRevenueCollectionReport() {
        logger.info("Generating revenue collection report");
        try {
            Map<Long, Double> report = reportService.generateRevenueCollectionReport();
            logger.debug("Revenue collection report generated: {} entries", report.size());
            return ResponseEntity.ok(new KeyValueReportDto(report));
        } catch (ReportGenerationException e) {
            logger.error("Failed to generate revenue collection report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new KeyValueReportDto(Map.of(-1L, Double.NaN)));
        }
    }

    /**
     * Generates a payment history report for a specific tenant.
     *
     * @param tenantId The ID of the tenant whose payment history is requested.
     * @return ResponseEntity containing a TenantPaymentHistoryReportDto with payment details and HTTP 200 (OK) status.
     * @throws IllegalArgumentException if tenantId is null or invalid.
     */
    @GetMapping("/tenant-payment-history/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTenantPaymentHistoryReport(@PathVariable Long tenantId) {
        logger.info("Generating payment history report for tenantId={}", tenantId);
        if (tenantId == null) {
            logger.warn("Tenant ID is null");
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant ID must not be null"));
        }
        try {
            List<Payment> payments = reportService.generateTenantPaymentHistoryReport(tenantId);
            List<PaymentDto> paymentDtos = payments.stream()
                    .map(paymentMapper::toDto)
                    .collect(Collectors.toList());
            logger.debug("Payment history report generated: {} entries", paymentDtos.size());
            return ResponseEntity.ok(new TenantPaymentHistoryReportDto(tenantId, paymentDtos));
        } catch (Exception e) {
            logger.error("Failed to generate payment history report for tenantId={}: {}", tenantId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TenantPaymentHistoryReportDto(tenantId, List.of()));
        }
    }

    /**
     * Generates a comprehensive property performance report, including occupancy and revenue metrics.
     *
     * @return ResponseEntity containing a PropertyPerformanceReportDto with performance metrics and HTTP 200 (OK) status.
     * @throws ReportGenerationException if report generation fails.
     */
    @GetMapping("/property-performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<PropertyPerformanceReportDto> getPropertyPerformanceReport() {
        logger.info("Generating property performance report");
        try {
            Map<Long, Map<String, Double>> report = reportService.generatePropertyPerformanceReport();
            logger.debug("Property performance report generated: {} entries", report.size());
            return ResponseEntity.ok(new PropertyPerformanceReportDto(report));
        } catch (ReportGenerationException e) {
            logger.error("Failed to generate property performance report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PropertyPerformanceReportDto(Map.of(-1L, Map.of("error", Double.NaN))));
        }
    }

    /**
     * Exports a specified report to Excel format.
     *
     * @param reportType The type of report to export (e.g., "unpaid-dues", "total-payments").
     * @param page       The page number for paginated reports (default is 0).
     * @return ResponseEntity containing the Excel file as a byte array with appropriate headers.
     * @throws IOException if export fails due to I/O issues.
     */
    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReportToExcel(
            @RequestParam String reportType,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        logger.info("Exporting report to Excel: type={}, page={}", reportType, page);
        validatePage(page);
        if (reportType == null || reportType.trim().isEmpty()) {
            logger.warn("Report type is null or empty");
            return ResponseEntity.badRequest()
                    .body("Report type must not be empty".getBytes());
        }

        try {
            Map<?, ?> reportData = getReportData(reportType, page);
            String[] headers = getExcelHeaders(reportType);
            byte[] excelFile = reportService.exportToExcel(reportData, headers);
            logger.debug("Report exported to Excel: type={}", reportType);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reportType + "_report.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelFile);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid report type: {}", reportType);
            return ResponseEntity.badRequest()
                    .body(("Invalid report type: " + reportType).getBytes());
        } catch (IOException e) {
            logger.error("Failed to export report to Excel: type={}, error={}", reportType, e.getMessage());
            throw e;
        }
    }

    /**
     * Exports a specified report to PDF format.
     *
     * @param reportType The type of report to export (e.g., "unpaid-dues", "total-payments").
     * @param page       The page number for paginated reports (default is 0).
     * @return ResponseEntity containing the PDF file as a byte array with appropriate headers.
     * @throws IOException       if export fails due to I/O issues.
     * @throws DocumentException if PDF generation fails.
     */
    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReportToPdf(
            @RequestParam String reportType,
            @RequestParam(defaultValue = "0") int page) throws IOException, DocumentException {
        logger.info("Exporting report to PDF: type={}, page={}", reportType, page);
        validatePage(page);
        if (reportType == null || reportType.trim().isEmpty()) {
            logger.warn("Report type is null or empty");
            return ResponseEntity.badRequest()
                    .body("Report type must not be empty".getBytes());
        }

        try {
            Map<?, ?> reportData = getReportData(reportType, page);
            byte[] pdfFile = reportService.exportToPdf(reportData, reportType + " Report");
            logger.debug("Report exported to PDF: type={}", reportType);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reportType + "_report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfFile);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid report type: {}", reportType);
            return ResponseEntity.badRequest()
                    .body(("Invalid report type: " + reportType).getBytes());
        } catch (IOException | DocumentException e) {
            logger.error("Failed to export report to PDF: type={}, error={}", reportType, e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves report data based on the specified report type and page number.
     *
     * @param reportType The type of report to generate.
     * @param page       The page number for paginated reports.
     * @return A Map containing the report data.
     * @throws IllegalArgumentException if the report type is invalid.
     */
    private Map<?, ?> getReportData(String reportType, int page) {
        switch (reportType.toLowerCase()) {
            case "unpaid-dues":
                return reportService.generateUnpaidDuesReport(page);
            case "total-payments":
                return reportService.generateTotalPaymentsReport(page);
            case "revenue-collection":
                return reportService.generateRevenueCollectionReport();
            case "property-performance":
                return reportService.generatePropertyPerformanceReport();
            default:
                throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }
    }

    /**
     * Provides column headers for Excel export based on the report type.
     *
     * @param reportType The type of report.
     * @return An array of header strings.
     */
    private String[] getExcelHeaders(String reportType) {
        switch (reportType.toLowerCase()) {
            case "unpaid-dues":
            case "total-payments":
                return new String[]{"Tenant ID", "Amount"};
            case "revenue-collection":
                return new String[]{"Property ID", "Revenue"};
            case "property-performance":
                return new String[]{"Property ID", "Occupancy Rate", "Revenue"};
            default:
                return new String[]{"Key", "Value"}; // Fallback
        }
    }

    /**
     * Validates the page number to ensure it is non-negative.
     *
     * @param page The page number to validate.
     * @throws IllegalArgumentException if the page number is negative.
     */
    private void validatePage(int page) {
        if (page < 0) {
            logger.warn("Invalid page number: {}", page);
            throw new IllegalArgumentException("Page number must be non-negative");
        }
    }
}