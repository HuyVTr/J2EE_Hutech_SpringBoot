package fit.hutech.spring.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import fit.hutech.spring.dtos.BookSalesDTO;
import fit.hutech.spring.repositories.IOrderDetailRepository;

@Service
public class ReportService {

    @Autowired
    private IOrderDetailRepository orderDetailRepository;
    @Autowired
    private fit.hutech.spring.repositories.IOrderRepository orderRepository;
    @Autowired
    private fit.hutech.spring.repositories.IUserRepository userRepository;

    public ByteArrayInputStream generateReport(fit.hutech.spring.dtos.ReportRequest request) throws IOException {
        if ("BOOK_SALES".equals(request.getReportType())) {
            return generateBookSalesReport(request);
        } else if ("USER_SPENDING".equals(request.getReportType())) {
            return generateUserSpendingReport(request);
        } else if ("REVENUE_PLATFORM".equals(request.getReportType())) {
            return generatePlatformReport(request);
        }
        return null; // Should throw exception or return empty
    }

    // --- 1. BOOK SALES REPORT ---
    private ByteArrayInputStream generateBookSalesReport(fit.hutech.spring.dtos.ReportRequest request)
            throws IOException {
        List<BookSalesDTO> data = orderDetailRepository.findTopSellingBooks(PageRequest.of(0, 1000));

        // Sort
        java.util.Comparator<BookSalesDTO> comparator = java.util.Comparator.comparing(BookSalesDTO::getTotalSold); // Default
        if ("revenue".equalsIgnoreCase(request.getSortBy())) {
            comparator = java.util.Comparator.comparing(dto -> dto.getBook().getPrice() * dto.getTotalSold());
        } else if ("price".equalsIgnoreCase(request.getSortBy())) {
            comparator = java.util.Comparator.comparing(dto -> dto.getBook().getPrice());
        }

        if ("DESC".equalsIgnoreCase(request.getSortDirection()))
            comparator = comparator.reversed();
        data.sort(comparator);

        // Limit
        if (request.getLimit() != null && request.getLimit() > 0 && request.getLimit() < data.size()) {
            data = data.subList(0, request.getLimit());
        }

        return generateExcel(data, request.getSelectedColumns(), "Book Sales", (row, item, col) -> {
            switch (col) {
                case "id":
                    row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum())
                            .setCellValue(item.getBook().getId());
                    break;
                case "title":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getBook().getTitle());
                    break;
                case "author":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getBook().getAuthor());
                    break;
                case "category":
                    row.createCell(row.getLastCellNum()).setCellValue(
                            item.getBook().getCategory() != null ? item.getBook().getCategory().getName() : "");
                    break;
                case "price":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getBook().getPrice());
                    break;
                case "sold":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getTotalSold());
                    break;
                case "revenue":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getTotalSold() * item.getBook().getPrice());
                    break;
            }
        });
    }

    // --- 2. USER SPENDING REPORT ---
    private ByteArrayInputStream generateUserSpendingReport(fit.hutech.spring.dtos.ReportRequest request)
            throws IOException {
        List<fit.hutech.spring.dtos.UserSpendingDTO> data = orderRepository.findTopSpenders(PageRequest.of(0, 1000));

        // Sort (Default by Money)
        java.util.Comparator<fit.hutech.spring.dtos.UserSpendingDTO> comparator = java.util.Comparator
                .comparing(fit.hutech.spring.dtos.UserSpendingDTO::getTotalSpent);
        if ("name".equalsIgnoreCase(request.getSortBy())) {
            comparator = java.util.Comparator.comparing(dto -> dto.getUser().getUsername());
        }

        if ("DESC".equalsIgnoreCase(request.getSortDirection()))
            comparator = comparator.reversed();
        data.sort(comparator);

        if (request.getLimit() != null && request.getLimit() > 0 && request.getLimit() < data.size()) {
            data = data.subList(0, request.getLimit());
        }

        return generateExcel(data, request.getSelectedColumns(), "User Spending", (row, item, col) -> {
            switch (col) {
                case "id":
                    row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum())
                            .setCellValue(item.getUser().getId());
                    break;
                case "username":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getUser().getUsername());
                    break;
                case "email":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getUser().getEmail());
                    break;
                case "fullname":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getUser().getFullName());
                    break;
                case "provider":
                    row.createCell(row.getLastCellNum()).setCellValue(
                            item.getUser().getProvider() != null ? item.getUser().getProvider() : "LOCAL");
                    break;
                case "total_spent":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getTotalSpent());
                    break;
            }
        });
    }

    // --- 3. PLATFORM STATS REPORT ---
    private ByteArrayInputStream generatePlatformReport(fit.hutech.spring.dtos.ReportRequest request)
            throws IOException {
        List<fit.hutech.spring.dtos.PlatformStatsDTO> data = userRepository.countUsersByPlatform();

        // Simple list, just export
        if (request.getSelectedColumns() == null || request.getSelectedColumns().isEmpty()) {
            request.setSelectedColumns(List.of("platform", "count"));
        }

        return generateExcel(data, request.getSelectedColumns(), "Platform Statistics", (row, item, col) -> {
            switch (col) {
                case "platform":
                    row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum())
                            .setCellValue(item.getProvider());
                    break;
                case "count":
                    row.createCell(row.getLastCellNum()).setCellValue(item.getCount());
                    break;
            }
        });
    }

    // --- GENERIC EXCEL GENERATOR HELPER ---
    private <T> ByteArrayInputStream generateExcel(List<T> data, List<String> columns, String sheetName,
            RowDataFiller<T> filler) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Header
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            int colIdx = 0;
            if (columns == null || columns.isEmpty())
                columns = List.of("id"); // Safety fallback

            for (String col : columns) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(getColumnLabel(col));
                cell.setCellStyle(headerStyle);
            }

            // Data
            int rowIdx = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowIdx++);
                // Reset cell num tracker effectively by creating mapping logic inside filler
                // Actually, the simple way is loop columns
                for (String col : columns) {
                    filler.fill(row, item, col);
                }
            }

            for (int i = 0; i < columns.size(); i++)
                sheet.autoSizeColumn(i);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @FunctionalInterface
    interface RowDataFiller<T> {
        void fill(Row row, T item, String column);
    }

    private String getColumnLabel(String col) {
        switch (col) {
            case "id":
                return "ID";
            case "title":
                return "Tên Sách";
            case "author":
                return "Tác Giả";
            case "category":
                return "Thể Loại";
            case "price":
                return "Giá Bán";
            case "sold":
                return "Số Lượng Bán";
            case "revenue":
                return "Doanh Thu";
            case "username":
                return "Tên Đăng Nhập";
            case "email":
                return "Email";
            case "fullname":
                return "Họ Tên";
            case "provider":
                return "Nền Tảng";
            case "total_spent":
                return "Tổng Chi Tiêu";
            case "platform":
                return "Nền Tảng";
            case "count":
                return "Số Lượng User";
            default:
                return col;
        }
    }

    // Backup method for backward compatibility
    public ByteArrayInputStream exportTopSellingBooks() throws IOException {
        fit.hutech.spring.dtos.ReportRequest req = new fit.hutech.spring.dtos.ReportRequest();
        req.setReportType("BOOK_SALES");
        req.setLimit(5);
        req.setSortBy("quantity");
        req.setSortDirection("DESC");
        req.setSelectedColumns(List.of("id", "title", "category", "price", "sold"));
        return generateReport(req);
    }
}
