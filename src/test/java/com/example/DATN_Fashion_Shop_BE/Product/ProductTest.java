package com.example.DATN_Fashion_Shop_BE.Product;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

public class ProductTest {

    WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test
    void insertProduct() throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_product']"));
        categoryMenu.click();
        Thread.sleep(1000);

        WebElement buttonIcon = driver.findElement(By.className("button__icon"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonIcon);
        Thread.sleep(2000);


        //NAME
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement inputEn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("en")));
        WebElement inputVi = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("vi")));
        WebElement inputJp = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("jp")));

        inputEn.sendKeys("Shirt Product Test 4");
        Thread.sleep(1000);
        inputVi.sendKeys("Áo sơ mi");
        Thread.sleep(1000);
        inputJp.sendKeys("シャツ");
        Thread.sleep(1000);

        if (inputEn.getAttribute("value").isEmpty()) {
            Assert.fail("Input English bị trống");
        }
        if (inputVi.getAttribute("value").isEmpty()) {
            Assert.fail("Input Vietnamese bị trống");
        }
        if (inputJp.getAttribute("value").isEmpty()) {
            Assert.fail("Input Japanese bị trống");
        }
        Thread.sleep(2000);

        //DESCRIPTION
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement textAreaDescriptionEn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.description-test[name='en']")));
        WebElement textAreaDescriptionVi = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.description-test[name='vi']")));
        WebElement textAreaDescriptionJp = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.description-test[name='jp']")));

        textAreaDescriptionEn.sendKeys("This is an English description. DESCRIPTION");
        textAreaDescriptionVi.sendKeys("Đây là mô tả bằng tiếng Việt.");
        textAreaDescriptionJp.sendKeys("これは日本語の説明です。");

        if (textAreaDescriptionEn.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea English bị trống");
        }
        if (textAreaDescriptionVi.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea Vietnamese bị trống");
        }
        if (textAreaDescriptionJp.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea Japanese bị trống");
        }

        Thread.sleep(2000);


//MATERIAL
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement textAreaMaterialEn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.material-test[name='en']")));
        WebElement textAreaMaterialVi = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.material-test[name='vi']")));
        WebElement textAreaMaterialJp = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.material-test[name='jp']")));

        textAreaMaterialEn.sendKeys("This is material information in English.");
        textAreaMaterialVi.sendKeys("Đây là thông tin chất liệu bằng tiếng Việt.");
        textAreaMaterialJp.sendKeys("これは日本語の素材情報です。");

        if (textAreaMaterialEn.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea Material English bị trống");
        }
        if (textAreaMaterialVi.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea Material Vietnamese bị trống");
        }
        if (textAreaMaterialJp.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea Material Japanese bị trống");
        }
        Thread.sleep(2000);
//CARE
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement textAreaCareEn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.care-test[name='en']")));
        WebElement textAreaCareVi = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.care-test[name='vi']")));
        WebElement textAreaCareJp = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.care-test[name='jp']")));

        textAreaCareEn.sendKeys("This is care instruction in English.");
        textAreaCareVi.sendKeys("Đây là hướng dẫn chăm sóc bằng tiếng Việt.");
        textAreaCareJp.sendKeys("これは日本語のケア手順です。");

        if (textAreaCareEn.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea Care English bị trống");
        }
        if (textAreaCareVi.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea Care Vietnamese bị trống");
        }
        if (textAreaCareJp.getAttribute("value").trim().isEmpty()) {
            Assert.fail("Textarea Care Japanese bị trống");
        }
        Thread.sleep(2000);


        WebElement uploadDiv = driver.findElement(By.cssSelector("div.product-variant-edit"));
        WebElement hiddenInput = uploadDiv.findElement(By.cssSelector("input[type='file']"));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", hiddenInput);
        hiddenInput.sendKeys("C:\\Users\\Admin\\Pictures\\product\\pr\\product_18_12_PINK_TM4.png\nC:\\Users\\Admin\\Pictures\\product\\pr\\product_18_image_2.png");

        Thread.sleep(2000);
        WebElement inputBasePrice = driver.findElement(By.name("basePrice"));
        inputBasePrice.clear();
        inputBasePrice.sendKeys("12500000");


        Thread.sleep(2000);
        WebElement btnSave = driver.findElement(By.className("btn-save"));
        btnSave.click();


        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Product created successfully!"), "Toast title không đúng");

        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

        WebElement productList = driver.findElement(By.cssSelector("div[routerlink='/admin/list_product']"));
        productList.click();
        Thread.sleep(1000);


        WebElement inputNameSearch = driver.findElement(By.name("nameSearch"));
        inputNameSearch.sendKeys("Shirt Product Test 4");

        Thread.sleep(2500);
        List<WebElement> firstBtnEdit = driver.findElements(By.className("btn-edit"));
        if (!firstBtnEdit.isEmpty()) {
            firstBtnEdit.get(0).click();
        }
        Thread.sleep(2500);


        List<WebElement> btnAddColor = driver.findElements(By.className("color-item-add"));
        btnAddColor.get(0).click();
        Thread.sleep(2000);

        List<WebElement> btnCheckColor = driver.findElements(By.className("check-color"));
        if (!btnCheckColor.isEmpty()) {
//            btnCheckColor.get(6).click();
            btnCheckColor.get(7).click();
//            btnCheckColor.get(8).click();
            WebElement btnCloseColor = driver.findElement(By.className("btn-close-color"));
            btnCloseColor.click();
        }
        Thread.sleep(1000);

        List<WebElement> btnAddSize = driver.findElements(By.className("color-item-add"));
        btnAddSize.get(1).click();
        Thread.sleep(2000);
        List<WebElement> btnCheckSize = driver.findElements(By.className("check-color"));
        if (!btnCheckSize.isEmpty()) {
//            btnCheckSize.get(0).click();
            btnCheckSize.get(1).click();
//            btnCheckSize.get(2).click();
            WebElement btnCloseSize = driver.findElement(By.className("close-btn"));
            btnCloseSize.click();
        }
        Thread.sleep(1000);

        WebElement btnBoth = driver.findElement(By.className("btn-save-both"));
        btnBoth.click();


        WebElement checkBoxIsActive = driver.findElement(By.id("flexSwitchCheckChecked"));
        checkBoxIsActive.click();
        Thread.sleep(2000);


        WebElement btn_save_product = driver.findElement(By.className("btn-save-product"));
        btn_save_product.click();
        Thread.sleep(2000);
    }
    @Test
    void insertCategoryProduct() throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_product']"));
        categoryMenu.click();
        Thread.sleep(1000);


        WebElement productList = driver.findElement(By.cssSelector("div[routerlink='/admin/list_product']"));
        productList.click();
        Thread.sleep(1000);


        WebElement inputNameSearch = driver.findElement(By.name("nameSearch"));
        inputNameSearch.sendKeys("Shirt Product Test 4");

        Thread.sleep(2500);
        List<WebElement> firstBtnEdit = driver.findElements(By.className("btn-edit"));
        if (!firstBtnEdit.isEmpty()) {
            firstBtnEdit.get(0).click();
        }
        Thread.sleep(2500);


        Thread.sleep(2000);
        WebElement btnAddCategoty = driver.findElement(By.className("btn-outline-primary"));
        btnAddCategoty.click();
        Thread.sleep(2000);

        // Chọn Category Parent
        WebDriverWait waitS2 = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement selectedParent = waitS2.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".custom-select .selected-item")
        ));
        selectedParent.click();

        List<WebElement> categoryOptions = waitS2.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector(".custom-select .dropdown ul li")
        ));
        if (!categoryOptions.isEmpty()) {
            categoryOptions.get(0).click();
            Thread.sleep(1000);
        } else {
            System.out.println("Không tìm thấy category nào!");
        }

// Chọn Category Child
        WebDriverWait waitS3 = new WebDriverWait(driver, Duration.ofSeconds(10));

// Lấy tất cả các custom-select
        List<WebElement> childCustomSelects = driver.findElements(By.className("custom-select"));

        if (childCustomSelects.size() >= 2) {
            WebElement secondCustomSelect = childCustomSelects.get(1);

            WebElement childSelectedItem = secondCustomSelect.findElement(By.cssSelector(".selected-item"));
            waitS3.until(ExpectedConditions.elementToBeClickable(childSelectedItem)).click();

            // Chờ dropdown mở xong (ul của dropdown phải hiển thị)
            waitS3.until(ExpectedConditions.visibilityOf(
                    secondCustomSelect.findElement(By.cssSelector(".dropdown ul"))
            ));

            List<WebElement> childCategoryOptions = secondCustomSelect.findElements(By.cssSelector(".dropdown ul li"));

            if (!childCategoryOptions.isEmpty()) {
                childCategoryOptions.get(1).click();
                Thread.sleep(1000); // Delay nếu cần chờ dữ liệu tải sau khi chọn
            } else {
                System.out.println("Không tìm thấy category con nào!");
            }
        } else {
            System.out.println("Không có custom-select cho category child.");
        }

        WebDriverWait waitS4 = new WebDriverWait(driver, Duration.ofSeconds(20));

// Bước 1: Click vào dropdown (vị trí thứ 3)
        WebElement subChildSelectBtn = waitS4.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[@class='selected-item'])[3]")
        ));
        subChildSelectBtn.click();

// Bước 2: Đảm bảo dropdown đã được hiển thị trong DOM và sau đó kiểm tra visibility
        waitS4.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@class='dropdown']//ul")
        ));

// Đảm bảo phần tử dropdown đã visible
        waitS4.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='dropdown']//ul")
        ));

// Bước 3: Lấy phần tử thứ ba từ danh sách dropdown
        WebElement thirdOption = waitS4.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[@class='dropdown']//ul/li)[2]")
        ));

// Lưu tên option trước khi click
        String optionText = thirdOption.getText();

// Bước 4: Sử dụng JavaScript để click vào phần tử (trong trường hợp .click() bình thường không hoạt động)
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", thirdOption); // Scroll nếu cần
        js.executeScript("arguments[0].click();", thirdOption); // Click bằng JavaScript

// Bước 5: In ra thông báo đã chọn category
        System.out.println("Đã chọn category sub child: " + optionText);





        Thread.sleep(1000);


        WebElement btnSaveCategory = driver.findElement(By.className("btn-primary"));
        btnSaveCategory.click();
        Thread.sleep(2000);

        WebElement btnClose = driver.findElement(By.className("btn-outline-danger"));
        JavascriptExecutor jsa = (JavascriptExecutor) driver;
        jsa.executeScript("arguments[0].click();", btnClose);
        Thread.sleep(2000);


    }

    @Test
     void updateProduct() throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_product']"));
        categoryMenu.click();
        Thread.sleep(1000);

        WebElement inputNameSearch = driver.findElement(By.name("nameSearch"));
        inputNameSearch.sendKeys("Shirt Product Test 4");

        Thread.sleep(2500);
        List<WebElement> firstBtnEdit = driver.findElements(By.className("btn-edit"));
        if (!firstBtnEdit.isEmpty()) {
            firstBtnEdit.get(0).click();
        }
        Thread.sleep(2500);

        //NAME
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement inputEn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("en")));
        inputNameSearch.clear();
        inputEn.sendKeys("Shirt Product Test 4 â ");
        Thread.sleep(1000);


        if (inputEn.getAttribute("value").isEmpty()) {
            Assert.fail("Input English bị trống");
        }

        Thread.sleep(2000);

        WebElement uploadDiv = driver.findElement(By.cssSelector("div.product-variant-edit"));
        WebElement hiddenInput = uploadDiv.findElement(By.cssSelector("input[type='file']"));
        hiddenInput.sendKeys("C:\\Users\\Admin\\Pictures\\product\\pr\\product_18_image_4.png");

        Thread.sleep(2000);
        WebElement inputBasePrice = driver.findElement(By.name("basePrice"));
        inputBasePrice.clear();
        inputBasePrice.sendKeys("12200000");


        WebElement btn_save_product = driver.findElement(By.className("btn-save-product"));
        btn_save_product.click();
        Thread.sleep(2000);

        // Đợi toast hiển thị
        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

        // Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Product updated successfully!"), "Toast title không đúng");

// Lấy nội dung của toast message
        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");



    }
    @Test
    void deleteProduct() throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_product']"));
        categoryMenu.click();
        Thread.sleep(1000);

        WebElement inputNameSearch = driver.findElement(By.name("nameSearch"));
        inputNameSearch.sendKeys("Shirt Product Test 4Shirt Product Test 4 â");

        Thread.sleep(2500);
        List<WebElement> firstBtnEdit = driver.findElements(By.className("btn-edit"));
        if (!firstBtnEdit.isEmpty()) {
            firstBtnEdit.get(0).click();
        }
        Thread.sleep(2500);


        WebElement checkBoxIsActive = driver.findElement(By.id("flexSwitchCheckChecked"));
        checkBoxIsActive.click();
        Thread.sleep(2000);


        WebElement btn_save_product = driver.findElement(By.className("btn-save-product"));
        btn_save_product.click();
        Thread.sleep(2000);

        // Đợi toast hiển thị
        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

        // Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Product updated successfully!"), "Toast title không đúng");

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
