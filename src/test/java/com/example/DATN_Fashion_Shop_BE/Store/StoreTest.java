package com.example.DATN_Fashion_Shop_BE.Store;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

public class StoreTest {

    WebDriver driver;

    String storeName ="John Doe";
    String storeNameUpdate = storeName + "Update Test 1";

    String storePhone = "0123456785" ;
    String storeEmail = "johndo3e@example.com" ;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test
    void insertStore() throws InterruptedException {
        driver.get("http://localhost:4200/admin/login_admin");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("admin@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btn-login"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement storeMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_store']"));
        storeMenu.click();
        Thread.sleep(1000);

        WebElement buttonIcon = driver.findElement(By.className("button__icon"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonIcon);
        Thread.sleep(2000);

        WebElement nameInput = driver.findElement(By.cssSelector("input[formcontrolname='name']"));
        nameInput.sendKeys(storeName);

        WebElement emailStoreInput = driver.findElement(By.cssSelector("input[formcontrolname='email']"));
        emailStoreInput.sendKeys(storeEmail);
        Thread.sleep(1000);

        WebElement phoneInput = driver.findElement(By.cssSelector("input[formcontrolname='phoneNumber']"));
        phoneInput.sendKeys(storePhone);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement openHourInput = driver.findElement(By.cssSelector("input[formcontrolname='openHour']"));
        openHourInput.clear();
        js.executeScript("arguments[0].value='2025-11-11T07:30';", openHourInput);
        js.executeScript("arguments[0].dispatchEvent(new Event('input'));", openHourInput);  // Kích hoạt sự kiện input
        Thread.sleep(1000);

        WebElement closeHourInput = driver.findElement(By.cssSelector("input[formcontrolname='closeHour']"));
        closeHourInput.clear();
        js.executeScript("arguments[0].value='2025-11-11T18:00';", closeHourInput);
        js.executeScript("arguments[0].dispatchEvent(new Event('input'));", closeHourInput);  // Kích hoạt sự kiện input
        Thread.sleep(1000);



        WebElement cityDropdown = driver.findElement(By.cssSelector("select[formcontrolname='city']"));
        Select select = new Select(cityDropdown);  // Sử dụng Select để thao tác với dropdown

        select.selectByValue("202");

        Thread.sleep(1000);
        WebElement districtDropdown = driver.findElement(By.cssSelector("select[formcontrolname='district']"));
        Select selectDistrict = new Select(districtDropdown);
        selectDistrict.selectByValue("1454");

        Thread.sleep(1000);
        WebElement wardDropdown = driver.findElement(By.cssSelector("select[formcontrolname='ward']"));
        Select selectWard = new Select(wardDropdown);
        selectWard.selectByValue("21203");

        Thread.sleep(1000);
        WebElement streetInput = driver.findElement(By.cssSelector("input[formcontrolname='street']"));
        streetInput.sendKeys("Đường số 12, Quận 12");

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'pushable')]//span[contains(text(), 'APPLY')]")
        ));

        addButton.click();
        Thread.sleep(1000);


        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Success"), "Toast title không đúng");

        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Store created successfully!"), "Toast message không đúng");


    }

    @Test
    void updateStore () throws InterruptedException {
        driver.get("http://localhost:4200/admin/login_admin");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("admin@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btn-login"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement storeMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_store']"));
        storeMenu.click();
        Thread.sleep(1000);


        WebElement inputSearch = driver.findElement(By.name("storeName"));
        inputSearch.clear();
        inputSearch.sendKeys(storeName);
        Thread.sleep(3000);


        List<WebElement> buttonIcon = driver.findElements(By.className("btn-edit"));
        WebElement firstBtn = buttonIcon.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstBtn);
        Thread.sleep(2000);



        WebElement nameInput = driver.findElement(By.cssSelector("input[formcontrolname='name']"));
        nameInput.clear();
        nameInput.sendKeys(storeNameUpdate);

        Thread.sleep(1000);

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'pushable')]//span[contains(text(), 'APPLY')]")
        ));

        addButton.click();
        Thread.sleep(1000);


        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Success"), "Toast title không đúng");

        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Store updated successfully!"), "Toast message không đúng");



    }


    @Test
    void deleteStore () throws InterruptedException {
        driver.get("http://localhost:4200/admin/login_admin");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("admin@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btn-login"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement storeMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_store']"));
        storeMenu.click();
        Thread.sleep(1000);

        WebElement inputSearch = driver.findElement(By.name("storeName"));
        inputSearch.clear();
        inputSearch.sendKeys(storeNameUpdate);
        Thread.sleep(3000);

        List<WebElement> buttonIcon = driver.findElements(By.className("btn-delete"));
        WebElement firstBtn = buttonIcon.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstBtn);
        Thread.sleep(2000);

        WebElement confirmDeleteBtn = driver.findElement(By.className("btn-login"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmDeleteBtn);
        Thread.sleep(2000);



        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

// Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Store Deleted successfully!"), "Toast title không đúng");

// Lấy nội dung của toast message
        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
