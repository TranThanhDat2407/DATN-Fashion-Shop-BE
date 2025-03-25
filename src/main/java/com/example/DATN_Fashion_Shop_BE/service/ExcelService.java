package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreOrderResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreDailyRevenueResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.StoreRevenueByDateRangeResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ExcelService {
    public final StoreService storeService;

    private void applyStyles(Workbook workbook, Sheet sheet, int numColumns) {
        // Tạo style cho header
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setFontName("Arial");
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);

        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Áp dụng style cho header
        Row headerRow = sheet.getRow(0);
        for (int i = 0; i < numColumns; i++) {
            Cell cell = headerRow.getCell(i);
            cell.setCellStyle(headerStyle);
        }

        // Tạo style cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Áp dụng style cho dữ liệu
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < numColumns; j++) {
                Cell cell = row.getCell(j);
                cell.setCellStyle(dataStyle);
            }
        }

        // Tự động điều chỉnh độ rộng cột
        for (int i = 0; i < numColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void exportRevenueByDateRangeToExcel(
            Long storeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            OutputStream outputStream
    ) throws IOException {
        // Lấy dữ liệu từ service
        List<StoreRevenueByDateRangeResponse> revenueData =
                storeService.getRevenueByDateRange(storeId, startDate, endDate);

        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Doanh thu theo tháng");

        // Tạo header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Tháng");
        headerRow.createCell(1).setCellValue("Năm");
        headerRow.createCell(2).setCellValue("Doanh thu");

        // Điền dữ liệu
        int rowNum = 1;
        for (StoreRevenueByDateRangeResponse data : revenueData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getMonth());
            row.createCell(1).setCellValue(data.getYear());
            row.createCell(2).setCellValue(data.getTotalRevenue());
        }

        // Áp dụng style
        applyStyles(workbook, sheet, 3);

        // Ghi file Excel
        workbook.write(outputStream);

        // Đóng workbook
        workbook.close();
    }

    public void exportDailyRevenueByMonthAndYearToExcel(
            Long storeId,
            Integer month,
            Integer year,
            OutputStream outputStream
    ) throws IOException {
        // Lấy dữ liệu từ service
        List<StoreDailyRevenueResponse> revenueData =
                storeService.getDailyRevenueByMonthAndYear(storeId, month, year);

        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Doanh thu hàng ngày");

        // Tạo header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Ngày");
        headerRow.createCell(1).setCellValue("Tháng");
        headerRow.createCell(2).setCellValue("Năm");
        headerRow.createCell(3).setCellValue("Doanh thu");

        // Điền dữ liệu
        int rowNum = 1;
        for (StoreDailyRevenueResponse data : revenueData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getDay());
            row.createCell(1).setCellValue(data.getMonth());
            row.createCell(2).setCellValue(data.getYear());
            row.createCell(3).setCellValue(data.getTotalRevenue());
        }

        // Áp dụng style
        applyStyles(workbook, sheet, 4);

        // Ghi file Excel
        workbook.write(outputStream);

        // Đóng workbook
        workbook.close();
    }

    public void exportStoreOrder(
            Long storeId,
            Integer month,
            Integer year,
            OutputStream outputStream
    ) throws IOException {
        // Lấy dữ liệu từ service
        List<StoreDailyRevenueResponse> revenueData =
                storeService.getDailyRevenueByMonthAndYear(storeId, month, year);

        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Doanh thu hàng ngày");

        // Tạo header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Ngày");
        headerRow.createCell(1).setCellValue("Tháng");
        headerRow.createCell(2).setCellValue("Năm");
        headerRow.createCell(3).setCellValue("Doanh thu");

        // Điền dữ liệu
        int rowNum = 1;
        for (StoreDailyRevenueResponse data : revenueData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getDay());
            row.createCell(1).setCellValue(data.getMonth());
            row.createCell(2).setCellValue(data.getYear());
            row.createCell(3).setCellValue(data.getTotalRevenue());
        }

        // Áp dụng style
        applyStyles(workbook, sheet, 4);

        // Ghi file Excel
        workbook.write(outputStream);

        // Đóng workbook
        workbook.close();
    }

    public void exportStoreOrdersToExcel(
            List<StoreOrderResponse> storeOrders,
            OutputStream outputStream
    ) throws IOException {
        if (storeOrders.isEmpty()) {
            throw new IllegalArgumentException("Không có dữ liệu để xuất Excel.");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách đơn hàng");

            // Tạo header
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Mã Hóa Đơn");
            headerRow.createCell(1).setCellValue("Tên Khách hàng");
            headerRow.createCell(2).setCellValue("Trạng Thái");
            headerRow.createCell(3).setCellValue("Phương Thức Thanh Toán");
            headerRow.createCell(4).setCellValue("Phương Thức Vận Chuyển");
            headerRow.createCell(5).setCellValue("Tổng Doanh thu");
            headerRow.createCell(6).setCellValue("Ngày Tạo");
            headerRow.createCell(7).setCellValue("Ngày Chỉnh Sửa");
            headerRow.createCell(8).setCellValue("Tạo Bởi ( ID Nhân Viên )");
            headerRow.createCell(9).setCellValue("Cập Nhật Bởi ( ID Nhân Viên )");

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0 \"đ\""));

            // Định dạng ngày tháng
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            // Điền dữ liệu
            int rowNum = 1;
            for (StoreOrderResponse data : storeOrders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(data.getOrderId());
                row.createCell(1).setCellValue(data.getUser() != null ?
                        data.getUser().getFirstName() + " " + data.getUser().getLastName() : "GUEST"
                );
                row.createCell(2).setCellValue(data.getOrderStatus().getStatusName());
                row.createCell(3).setCellValue(data.getPaymentMethod().getMethodName());
                row.createCell(4).setCellValue(data.getShippingMethod() != null ?
                        data.getShippingMethod().getMethodName() : "IN STORE"
                );


                Cell totalPriceCell = row.createCell(5);
                totalPriceCell.setCellValue(data.getTotalPrice());
                totalPriceCell.setCellStyle(currencyStyle);

                row.createCell(6).setCellValue(data.getCreatedAt().format(dateFormatter));
                row.createCell(7).setCellValue(data.getUpdatedAt().format(dateFormatter));
                row.createCell(8).setCellValue(data.getCreatedBy() != null ?
                        data.getCreatedBy().toString() : "SYSTEM");
                row.createCell(9).setCellValue(data.getUpdatedBy() != null ?
                        data.getUpdatedBy().toString() : "SYSTEM");
            }

            // Áp dụng style
            applyStyles(workbook, sheet, 10);

            // Ghi file Excel
            workbook.write(outputStream);
        }
    }
}
