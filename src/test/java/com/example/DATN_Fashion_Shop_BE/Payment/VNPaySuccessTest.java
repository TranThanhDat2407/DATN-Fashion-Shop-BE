package com.example.DATN_Fashion_Shop_BE.Payment;
import com.example.DATN_Fashion_Shop_BE.service.MomoService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VNPaySuccessTest extends PaymentTestBase{
    private static final Logger logger = LoggerFactory.getLogger(VNPaySuccessTest.class);
    @Test
    public void testVNPayPaymentSuccess() throws InterruptedException {
        addProductToCart();
        completeShippingSteps();
        proceedToPayment();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        proceedToReviewButton(wait);
        verifyReviewPageAndPlaceOrder();
        selectNCBBank(driver, wait);
        enterCardDetails(wait);
        enterOTPAndComplete(wait);
        // Kiểm tra kết quả cuối cùng
        verifyPaymentSuccess();
    }

    @AfterMethod
    public void tearDown() {
        try {
            if (driver != null) {

                driver.quit();
                logger.info("Đã đóng trình duyệt thành công");


                driver = null;
            }
        } catch (Exception e) {
            logger.error("Lỗi khi đóng trình duyệt: " + e.getMessage());
        }
    }

    private void completeShippingSteps() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        selectShippingMethod(wait);

        selectDeliveryAddress(wait);

        proceedToPaymentButton(wait);
    }

    private void selectShippingMethod(WebDriverWait wait) throws InterruptedException {

        WebElement deliverySection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#delivery.accordion-collapse.collapse.show")));

        WebElement shippingAddressRadio = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("shippingAddress")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", shippingAddressRadio);
        Thread.sleep(500);

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", shippingAddressRadio);

        Assertions.assertTrue(shippingAddressRadio.isSelected(),
                "Phương thức giao hàng phải được chọn");
    }

    private void selectDeliveryAddress(WebDriverWait wait) throws InterruptedException {
        try {

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".fr-list.bordered-between-content")));

            int attempts = 0;
            while (attempts < 3) {
                try {
                    List<WebElement> addressButtons = wait.until(ExpectedConditions
                            .presenceOfAllElementsLocatedBy(By.cssSelector("#btn-address:not(:disabled)")));

                    if (!addressButtons.isEmpty()) {
                        WebElement firstAddress = addressButtons.get(0);


                        ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                                firstAddress);


                        Thread.sleep(800);


                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstAddress);


                        wait.until(ExpectedConditions.stalenessOf(firstAddress));
                        break;
                    }
                } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                    attempts++;
                    if (attempts == 3) throw e;
                    Thread.sleep(1000);
                }
            }
        } catch (TimeoutException e) {
            logger.warn("Không tìm thấy địa chỉ để chọn, có thể đã có địa chỉ mặc định");
        }
    }

    private void proceedToPaymentButton(WebDriverWait wait) throws InterruptedException {
        try {

            safeClick(By.id("btn-show-payment"));
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/checkout/payment"),
                    ExpectedConditions.presenceOfElementLocated(By.id("payment-section"))
            ));

        } catch (TimeoutException e) {

            takeScreenshot("payment_button_fail");
            throw new RuntimeException("Không thể chuyển sang trang payment", e);
        }
    }

    private void proceedToPayment() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement vnpayRadio = driver.findElement(By.id("pay-2"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", vnpayRadio);

        Thread.sleep(1000);


        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='tiếp-tục-button']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", continueButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueButton);
    }


    private void proceedToReviewButton(WebDriverWait wait) throws InterruptedException {
        try {
            // Chờ cho nút "Tiếp tục" xuất hiện và có thể click
            // Sử dụng selector kết hợp class và data-test attribute để chắc chắn
            WebElement reviewButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.btn-show-review[data-test='tiếp-tục-button']")));

            // Scroll đến nút để đảm bảo nó hiển thị trên màn hình
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", reviewButton);
            Thread.sleep(500); // Chờ ngắn để hoàn thành scroll

            // Kiểm tra xem nút có bị disable không
            if (!reviewButton.isEnabled()) {
                // Nếu bị disable, kiểm tra xem có thông báo validation nào không
                List<WebElement> errorMessages = driver.findElements(By.cssSelector(".fr-text.error-message"));
                if (!errorMessages.isEmpty()) {
                    Assertions.fail("Không thể tiếp tục do lỗi: " + errorMessages.get(0).getText());
                } else {
                    Assertions.fail("Nút tiếp tục bị disable mà không có thông báo lỗi");
                }
            }

            // Click bằng JavaScript để tránh các vấn đề về click thông thường
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reviewButton);

            // Chờ URL chuyển sang trang review
            wait.until(ExpectedConditions.urlContains("/checkout/review"));

            // Chờ một element quan trọng trên trang review load xong
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("button[data-test='đặt-hàng-button']")));

            // Log thông tin để debug
            System.out.println("Đã chuyển sang trang review thành công");

        } catch (TimeoutException e) {
            // Chụp màn hình khi fail
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            // Lưu screenshot vào thư mục nào đó
            Assertions.fail("Không thể chuyển sang trang review: " + e.getMessage());
        }
    }

    private void verifyReviewPageAndPlaceOrder() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // 1. Chờ và kiểm tra trang review đã load hoàn toàn
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#overview.accordion-collapse.collapse.show")));

            // 2. Kiểm tra bảng tổng hợp đơn hàng
            WebElement orderTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".fr-table table")));
            Assertions.assertTrue(orderTable.isDisplayed(), "Bảng tổng hợp đơn hàng phải hiển thị");

            // 3. Kiểm tra các thông tin quan trọng trong đơn hàng
            verifyOrderSummaryDetails(wait);

            // 4. Kiểm tra nút đặt hàng
            WebElement placeOrderButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[data-test='đặt-hàng-button']")));
            Assertions.assertTrue(placeOrderButton.isDisplayed(), "Nút đặt hàng phải hiển thị");
            Assertions.assertTrue(placeOrderButton.isEnabled(), "Nút đặt hàng phải enabled");

            // 5. Scroll và click đặt hàng
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", placeOrderButton);
            Thread.sleep(800);


            takeScreenshot("before-place-order");


            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", placeOrderButton);

            // 6. Kiểm tra đã chuyển sang cổng thanh toán
            verifyRedirectToPaymentGateway(wait);

        } catch (TimeoutException e) {
            takeScreenshot("review-page-error");
            Assertions.fail("Lỗi khi xử lý trang review: " + e.getMessage());
        }
    }

    private void verifyOrderSummaryDetails(WebDriverWait wait) {
        // Kiểm tra tổng đơn hàng
        WebElement totalAmount = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("td[data-test='tổng-cộng'] span")));
        Assertions.assertFalse(totalAmount.getText().isEmpty(), "Tổng đơn hàng phải hiển thị");

        // Kiểm tra phí vận chuyển
        WebElement shippingFee = driver.findElement(By.cssSelector("td[data-test='phi-vận-chuyển'] span"));
        Assertions.assertFalse(shippingFee.getText().isEmpty(), "Phí vận chuyển phải hiển thị");

        // Kiểm tra tổng sau giảm giá
        WebElement totalAfterDiscount = driver.findElement(By.cssSelector("th[data-test='tổng'] span"));
        Assertions.assertFalse(totalAfterDiscount.getText().isEmpty(), "Tổng sau giảm giá phải hiển thị");

        // Kiểm tra VAT
        WebElement vatAmount = driver.findElement(By.cssSelector("td[data-test='da-bao-gồm-thuế-gia-trị-gia-tang'] span"));
        Assertions.assertFalse(vatAmount.getText().isEmpty(), "VAT phải hiển thị");

        // Kiểm tra tổng đơn đặt hàng
        WebElement grandTotal = driver.findElement(By.cssSelector("th[data-test='tổng-dơn-dặt-hang'] span"));
        Assertions.assertFalse(grandTotal.getText().isEmpty(), "Tổng đơn đặt hàng phải hiển thị");
    }

    private void verifyRedirectToPaymentGateway(WebDriverWait wait) {
        try {
            // 1. Chuyển sang tab mới nếu cần
            String originalWindow = driver.getWindowHandle();
            if (driver.getWindowHandles().size() > 1) {
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalWindow.equals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
            }

            // 2. Kiểm tra logo VNPAY - cách chắc chắn nhất
            WebElement vnpayLogo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("img[src*='/paymentv2/Images/brands/logo.svg'][alt='VNPAY']")));

            // 3. Kiểm tra tiêu đề trang
            WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class, 'main-title-mobile') and contains(., 'Chọn phương thức thanh toán')]")));

            logger.info("Xác nhận trang VNPAY thành công qua logo và tiêu đề");

        } catch (TimeoutException e) {
            takeScreenshot("vnpay-verification-failed");
            throw new AssertionError("Không xác nhận được trang VNPAY", e);
        }
    }

    private void takeScreenshot(String name) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshot, new File("screenshots/" + name + ".png"));
        } catch (Exception e) {
            System.out.println("Không thể chụp màn hình: " + e.getMessage());
        }
    }


    private void selectNCBBank(WebDriver driver, WebDriverWait wait) {
        try {
            // 1. Tìm và chờ phần tử accordion sẵn sàng
            WebElement domesticBankSection = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("div.list-method-button[data-bs-target='#accordionList2']")));

            // 2. Kiểm tra và mở accordion nếu cần
            boolean needToOpen = !Boolean.parseBoolean(domesticBankSection.getAttribute("aria-expanded"))
                    || !driver.findElement(By.id("accordionList2")).isDisplayed();

            if (needToOpen) {
                logger.info("[ACTION] Mở accordion...");
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                        domesticBankSection);

                // Click bằng JavaScript để đảm bảo
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", domesticBankSection);

                // Chờ nội dung hiển thị hoàn toàn
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#accordionList2.collapse.show")));

                // Chờ thêm để DOM ổn định
                Thread.sleep(1000); // Tăng thời gian chờ
            }

            // 3. Tìm và click nút NCB với cơ chế retry
            int attempts = 0;
            while (attempts < 3) {
                try {
                    // Tìm lại phần tử mỗi lần retry
                    WebElement ncbButton = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button#NCB.list-bank-item, [search-value*='ncb'] button")));

                    // Scroll vào view
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});",
                            ncbButton);

                    // Click bằng JavaScript
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ncbButton);

                    // Chờ chuyển trang hoặc thay đổi UI
                    try {
                        wait.until(ExpectedConditions.or(
                                ExpectedConditions.urlContains("Ncb"),
                                ExpectedConditions.visibilityOfElementLocated(
                                        By.xpath("//div[contains(., 'Thanh toán qua Ngân hàng NCB')]"))
                        ));
                        break; // Thoát vòng lặp nếu thành công
                    } catch (TimeoutException e) {
                        attempts++;
                        if (attempts == 3) throw e;
                        Thread.sleep(1000);
                    }

                } catch (StaleElementReferenceException e) {
                    attempts++;
                    if (attempts == 3) throw e;
                    Thread.sleep(1000);
                }
            }

            logger.info("[SUCCESS] Đã chọn thành công ngân hàng NCB và chuyển sang trang nhập thông tin");

        } catch (Exception e) {
            logger.error("[ERROR] Lỗi khi thao tác: " + e.getMessage());
            takeScreenshot("error-selectNCB-" + System.currentTimeMillis());
            throw new RuntimeException("Lỗi khi chọn ngân hàng NCB", e);
        }
    }






    private void enterCardDetails(WebDriverWait wait) {
        try {
            // 1. Đảm bảo trang đã load
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            // 2. Kích hoạt tab "Thẻ nội địa" nếu cần
            activateDomesticCardTab(wait);

            // 3. Nhập thông tin thẻ với timeout dài hơn
            enterCardFieldWithRetry(wait, By.id("cardNumber"), "9704198526191432198", "Số thẻ");
            enterCardFieldWithRetry(wait, By.id("cardHolder"), "NGUYEN VAN A", "Tên chủ thẻ");
            enterCardFieldWithRetry(wait, By.id("cardDate"), "07/25", "Ngày phát hành");

        } catch (Exception e) {
            takeScreenshot("payment-error-" + System.currentTimeMillis());
            throw new RuntimeException("Xử lý thanh toán thất bại", e);
        }
    }

    private void activateDomesticCardTab(WebDriverWait wait) {
        try {
            WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(.,'Thẻ nội địa')]")));
            ((JavascriptExecutor)driver).executeScript("arguments[0].click();", tab);
            waitABit(1000); // Chờ tab chuyển đổi
        } catch (Exception e) {
            logger.warn("Không thể kích hoạt tab thẻ nội địa: " + e.getMessage());
        }
    }

    private void enterCardFieldWithRetry(WebDriverWait wait, By locator, String value, String fieldName) {
        final int MAX_ATTEMPTS = 3;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                // Sử dụng wait mới với timeout dài hơn cho mỗi lần thử
                WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(20));

                // Chờ từng điều kiện riêng biệt
                WebElement field = waits.until(ExpectedConditions.presenceOfElementLocated(locator));
                waits.until(ExpectedConditions.visibilityOf(field));
                waits.until(ExpectedConditions.elementToBeClickable(field));

                // Scroll đặc biệt cho các trường hợp khó
                ((JavascriptExecutor)driver).executeScript(
                        "window.scrollTo(0, arguments[0].getBoundingClientRect().top - window.innerHeight/3);",
                        field);

                // Thử cả 3 cách nhập liệu
                if (!tryAllInputMethods(field, value)) {
                    throw new Exception("Tất cả phương pháp nhập liệu thất bại");
                }

                logger.info("Đã nhập thành công {} ở lần thử {}", fieldName, attempt);
                return;

            } catch (Exception e) {
                logger.warn("Lỗi nhập {} (lần {}): {}", fieldName, attempt, e.getMessage());
                if (attempt == MAX_ATTEMPTS) {
                    throw new RuntimeException("Không thể nhập " + fieldName + " sau " + MAX_ATTEMPTS + " lần thử", e);
                }
                refreshPaymentPage();
                waitABit(2000);
            }
        }
    }
    private boolean tryAllInputMethods(WebElement field, String value) {
        try {
            // Cách 1: Nhập thông thường
            field.clear();
            field.sendKeys(value);
            return true;
        } catch (Exception e1) {
            try {
                // Cách 2: Nhập bằng JavaScript
                ((JavascriptExecutor)driver).executeScript(
                        "arguments[0].value = arguments[1];", field, value);
                return true;
            } catch (Exception e2) {
                try {
                    // Cách 3: Nhập từng ký tự
                    field.clear();
                    for (char c : value.toCharArray()) {
                        field.sendKeys(String.valueOf(c));
                        waitABit(50);
                    }
                    return true;
                } catch (Exception e3) {
                    return false;
                }
            }
        }
    }

    private void refreshPaymentPage() {
        try {
            // Thử click lại tab thanh toán
            WebElement tab = driver.findElement(By.cssSelector(".nav-link.active"));
            ((JavascriptExecutor)driver).executeScript("arguments[0].click();", tab);
            waitABit(1000);
        } catch (Exception e) {
            logger.warn("Không thể refresh trang thanh toán: " + e.getMessage());
        }
    }

    private void waitABit(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void enterOTPAndComplete(WebDriverWait wait) {
        try {
            // 1. Chờ OTP dialog xuất hiện (nếu có)
            WebElement otpDialog = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".otp-dialog, .otp-container")));
            logger.info("Đã hiển thị dialog OTP");

            // 2. Nhập OTP
            WebElement otpInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input#otp, input[name='otp']")));
            otpInput.clear();
            otpInput.sendKeys("123456");
            logger.info("Đã nhập OTP");

            // 3. Xác nhận OTP
            WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button#confirmBtn, button[type='submit']")));

            // Sử dụng safeClick để tránh vấn đề che phủ
            safeClick((By) confirmButton);
            logger.info("Đã xác nhận OTP");

            // 4. Chờ thông báo thành công
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("payment_success"),
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector(".payment-success, .transaction-success"))
            ));
            logger.info("Xác nhận thanh toán thành công");

        } catch (TimeoutException e) {
            takeScreenshot("otp-process-failed");
            throw new AssertionError("Quá trình nhập OTP thất bại: " + e.getMessage(), e);
        } catch (Exception e) {
            takeScreenshot("otp-process-error");
            throw new RuntimeException("Lỗi khi xử lý OTP", e);
        }
    }
    private void verifyPaymentSuccess() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            // Kiểm tra thông báo thành công hoặc order number
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".order-success-message")));
            Assertions.assertTrue(successMessage.isDisplayed(),
                    "Không hiển thị thông báo đặt hàng thành công");

            // Hoặc kiểm tra URL đơn hàng thành công
            Assertions.assertTrue(driver.getCurrentUrl().contains("http://localhost:4200/client/vnd/vi/payment_success"),
                    "Không chuyển đến trang thành công sau thanh toán");

        } catch (TimeoutException e) {
            takeScreenshot("payment-verification-failed");
            Assertions.fail("Xác minh thanh toán thất bại: " + e.getMessage());
        }
    }

    private void safeClick(By locator) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        int attempts = 0;

        while (attempts < 3) {
            try {
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));

                new Actions(driver)
                        .moveToElement(element)
                        .perform();
                Thread.sleep(300);

                new Actions(driver)
                        .click(element)
                        .perform();

                return;

            } catch (ElementClickInterceptedException e) {

                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();",
                        driver.findElement(locator));
                return;

            } catch (StaleElementReferenceException e) {
                attempts++;
                if (attempts == 3) throw e;
                Thread.sleep(1000);
            }
        }
    }
}
