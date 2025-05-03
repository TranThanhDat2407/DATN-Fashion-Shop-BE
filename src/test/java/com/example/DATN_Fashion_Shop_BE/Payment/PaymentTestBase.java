package com.example.DATN_Fashion_Shop_BE.Payment;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.time.Duration;
import java.util.List;

public class PaymentTestBase {
    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final Logger logger = LoggerFactory.getLogger(PaymentTestBase.class);

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void addProductToCart() throws InterruptedException {
        driver.get("http://localhost:4200/client/VND/vn/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("bvt.vantai@gmail.com");

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
            WebElement firstProduct = productItems.get(1);
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




}