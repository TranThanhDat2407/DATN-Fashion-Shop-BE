package com.example.DATN_Fashion_Shop_BE.Cart;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CartTest {
    WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

//        driver.get("http://localhost:4200/client/VND/en/login");
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        Thread.sleep(1000);

//        WebElement emailInput = driver.findElement(By.name("email"));
//        emailInput.sendKeys("customer1@example.com");
//
//        WebElement passInput = driver.findElement(By.name("password"));
//        passInput.sendKeys("Abc123");
//
//        WebElement btnLogin = driver.findElement(By.className("btnLogin"));
//        btnLogin.click();
//        Thread.sleep(2000);
//
//        WebElement btnCategoty = driver.findElement(By.className("btn2"));
//        btnCategoty.click();
//        Thread.sleep(1000);
//
//        List<WebElement> categoryItems = driver.findElements(By.cssSelector("div.category-item"));
//        if (!categoryItems.isEmpty()) {
//            WebElement firstItem = categoryItems.get(0);
//            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstItem);
//            Thread.sleep(300);
//            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstItem);
//        }
//        Thread.sleep(2000);
//
//
//        WebElement categoriesChild = driver.findElement(By.className("categoriesChild"));
//        categoriesChild.click();
//        Thread.sleep(2000);
//
//
//        List<WebElement> productItems = driver.findElements(By.className("product-item"));
//        if (!productItems.isEmpty()) {
//            WebElement firstProduct = productItems.get(0);
//            firstProduct.click();
//        }
//        Thread.sleep(2000);
    }

    @Test
    void BuyNowSuccessfully() throws InterruptedException {
        driver.get("http://localhost:4200/client/VND/en/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("customer1@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btnLogin"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement btnCategoty = driver.findElement(By.className("btn2"));
        btnCategoty.click();
        Thread.sleep(1000);

        List<WebElement> categoryItems = driver.findElements(By.cssSelector("div.category-item"));
        if (!categoryItems.isEmpty()) {
            WebElement firstItem = categoryItems.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstItem);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstItem);
        }
        Thread.sleep(2000);


        WebElement categoriesChild = driver.findElement(By.className("categoriesChild"));
        categoriesChild.click();
        Thread.sleep(2000);


        List<WebElement> productItems = driver.findElements(By.className("product-item"));
        if (!productItems.isEmpty()) {
            WebElement firstProduct = productItems.get(0);
            firstProduct.click();
        }
        Thread.sleep(2000);

        WebElement btnAddToCart = driver.findElement(By.className("btn-cart"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnAddToCart);
        Thread.sleep(1000);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnAddToCart);

        Thread.sleep(1000);

        try {
            WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement modal = waits.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-success")));

            Thread.sleep(500); // Đợi animation (nếu có)

            WebElement title = modal.findElement(By.tagName("span"));
            WebElement message = modal.findElement(By.tagName("p"));

            System.out.println("Modal hiển thị: " + title.getText() + " - " + message.getText());
        } catch (Exception e) {
            throw new RuntimeException("Không lấy được text từ modal", e);
        }

        Thread.sleep(1000);
        WebElement btnCart = driver.findElement(By.className("btn-cartTest"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnCart);
        Thread.sleep(300);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnCart);

        Thread.sleep(3000);

        try {
            WebElement btnBuyNow = driver.findElement(By.className("btnBuyCart"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnBuyNow);

            Thread.sleep(2000);

            String currentUrl = driver.getCurrentUrl();
            System.out.println("URL hiện tại: " + currentUrl);


            if (currentUrl.contains("/checkout/shipping")) {
                System.out.println(" Điều hướng thành công!. Sang trang thanh toán thành công. ");
            } else {
                System.out.println(" Không điều hướng đúng. URL hiện tại: " + currentUrl);
                Assert.fail("URL không đúng sau khi click nút 'MUA NGAY'");
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra điều hướng sau khi click", e);
        }


    }

    @Test
    void addToCartSuccessfully() throws InterruptedException {
        driver.get("http://localhost:4200/client/VND/en");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        WebElement btnCategoty = driver.findElement(By.className("btn2"));
        btnCategoty.click();
        Thread.sleep(1000);

        List<WebElement> categoryItems = driver.findElements(By.cssSelector("div.category-item"));
        if (!categoryItems.isEmpty()) {
            WebElement firstItem = categoryItems.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstItem);
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstItem);
        }
        Thread.sleep(2000);


        WebElement categoriesChild = driver.findElement(By.className("categoriesChild"));
        categoriesChild.click();
        Thread.sleep(2000);


        List<WebElement> productItems = driver.findElements(By.className("product-item"));
        if (!productItems.isEmpty()) {
            WebElement firstProduct = productItems.get(0);
            firstProduct.click();
        }
        Thread.sleep(2000);

        WebElement btnAddToCart = driver.findElement(By.className("btn-cart"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnAddToCart);
        Thread.sleep(1000);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnAddToCart);

        Thread.sleep(1000);

        try {
            WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement modal = waits.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-success")));

            Thread.sleep(500); // Đợi animation (nếu có)

            WebElement title = modal.findElement(By.tagName("span"));
            WebElement message = modal.findElement(By.tagName("p"));

            System.out.println("Modal hiển thị: " + title.getText() + " - " + message.getText());
        } catch (Exception e) {
            WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement modal = waits.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-error")));

            Thread.sleep(500); // Đợi animation (nếu có)

            WebElement title = modal.findElement(By.tagName("span"));
            WebElement message = modal.findElement(By.tagName("p"));

            System.out.println("Modal hiển thị: " + title.getText() + " - " + message.getText());
            throw new RuntimeException("Không lấy được text từ modal", e);
        }
    }

    @Test
    void updateCartSuccessfully() throws InterruptedException {
        driver.get("http://localhost:4200/client/VND/en");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        driver.get("http://localhost:4200/client/VND/en/login");
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("customer1@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btnLogin"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement btnCart = driver.findElement(By.className("btn-cartTest"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnCart);
        Thread.sleep(300);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnCart);
        Thread.sleep(1000);

        try {
            List<WebElement> btnRedoubleQty = driver.findElements(By.className("btn-redoubleQty"));
            if (!btnRedoubleQty.isEmpty()) {
                List<WebElement> inputAllQuantity = driver.findElements(By.className("input-qty"));
                if (!inputAllQuantity.isEmpty()) {
                    WebElement inputQty = inputAllQuantity.get(0);
                    int oldQty = Integer.parseInt(inputQty.getAttribute("value"));
                    System.out.println("Giá trị ban đầu: " + oldQty);

                    WebElement firstItem = btnRedoubleQty.get(0);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstItem);
                    Thread.sleep(300);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstItem);

                    Thread.sleep(1000);
                    List<WebElement> inputAllQuantityStep2 = driver.findElements(By.className("input-qty"));
                    inputQty = inputAllQuantityStep2.get(0);
                    int newQty = Integer.parseInt(inputQty.getAttribute("value"));
                    System.out.println("Giá trị sau khi click: " + newQty);

                    if (newQty != oldQty) {
                        System.out.println(" Test Passed: Giá trị đã thay đổi.");
                    } else {
                        System.out.println(" Test Failed: Giá trị không thay đổi.");
                        Assert.fail("Giá trị qty không thay đổi sau khi click.");
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Đã xảy ra lỗi khi kiểm tra giá trị qty.");
        }


    }

    @Test
    void deleteCartSuccessfully() throws InterruptedException {
        driver.get("http://localhost:4200/client/VND/en");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        driver.get("http://localhost:4200/client/VND/en/login");
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("customer1@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btnLogin"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement btnCart = driver.findElement(By.className("btn-cartTest"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnCart);
        Thread.sleep(300);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnCart);
        Thread.sleep(1000);

        try {
            List<WebElement> btnRemoveCart = driver.findElements(By.className("item-btnRemove"));
            if (!btnRemoveCart.isEmpty()) {

                WebElement firstItem = btnRemoveCart.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstItem);
                Thread.sleep(300);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstItem);

                Thread.sleep(2000);
                List<WebElement> btnAllConfig = driver.findElements(By.className("btn-login"));

                WebElement btnConfig = btnAllConfig.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnConfig);
                Thread.sleep(300);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnConfig);

                try {
                    WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(10));

                    WebElement modal = waits.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-success")));

                    Thread.sleep(500); // Đợi animation (nếu có)

                    WebElement title = modal.findElement(By.tagName("span"));
                    WebElement message = modal.findElement(By.tagName("p"));

                    System.out.println("Modal hiển thị: " + title.getText() + " - " + message.getText());
                } catch (Exception e) {
                    WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(10));
                    WebElement modal = waits.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-error")));

                    Thread.sleep(500); // Đợi animation (nếu có)

                    WebElement title = modal.findElement(By.tagName("span"));
                    WebElement message = modal.findElement(By.tagName("p"));

                    System.out.println("Modal hiển thị: " + title.getText() + " - " + message.getText());
                    throw new RuntimeException("Không lấy được text từ modal", e);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Đã xảy ra lỗi khi xoa cart.");
        }


    }


    @Test
    void deleteAllCartSuccessfully() throws InterruptedException {
        driver.get("http://localhost:4200/client/VND/en");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        driver.get("http://localhost:4200/client/VND/en/login");
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("customer1@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btnLogin"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement btnCart = driver.findElement(By.className("btn-cartTest"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnCart);
        Thread.sleep(300);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnCart);
        Thread.sleep(1000);
        List<WebElement> btnRedoubleQty = driver.findElements(By.className("btn-redoubleQty"));
        if (!btnRedoubleQty.isEmpty()){
            try {
                WebElement btnClearCart = driver.findElement(By.className("clear-cart"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnClearCart);
                Thread.sleep(300);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnClearCart);

                Thread.sleep(2000);
                List<WebElement> btnAllConfig = driver.findElements(By.className("btn-login"));

                WebElement btnConfig = btnAllConfig.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnConfig);
                Thread.sleep(300);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnConfig);

                try {
                    WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(10));

                    WebElement modal = waits.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-success")));

                    Thread.sleep(500); // Đợi animation (nếu có)

                    WebElement title = modal.findElement(By.tagName("span"));
                    WebElement message = modal.findElement(By.tagName("p"));

                    System.out.println("Modal hiển thị: " + title.getText() + " - " + message.getText());
                } catch (Exception e) {
                    WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(10));
                    WebElement modal = waits.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-error")));

                    Thread.sleep(500); // Đợi animation (nếu có)

                    WebElement title = modal.findElement(By.tagName("span"));
                    WebElement message = modal.findElement(By.tagName("p"));

                    System.out.println("Modal hiển thị: " + title.getText() + " - " + message.getText());
                    throw new RuntimeException("Không lấy được text từ modal", e);


                }
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("Đã xảy ra lỗi khi clear cart.");
            }

        }else{
            Assert.fail("Ko co product nao trong Cart");
        }



    }


    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
