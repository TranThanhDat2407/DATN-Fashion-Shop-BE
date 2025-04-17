package com.example.DATN_Fashion_Shop_BE.Review;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ReviewsTest {

    WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }


    @Test
    void insertReview() throws InterruptedException {

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


        WebElement reviewBtn = wait.until(ExpectedConditions.elementToBeClickable(By.className("writingReview")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", reviewBtn);
        Thread.sleep(500);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reviewBtn);

        WebElement titleInput = driver.findElement(By.name("title"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", titleInput);
        Thread.sleep(1000);
        titleInput.sendKeys("sdcds");

        Thread.sleep(1000);
        WebElement commentInput = driver.findElement(By.name("comment"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", commentInput);
        Thread.sleep(1000);
        commentInput.sendKeys("sdcds");

        WebElement sizeDropdown = driver.findElement(By.name("purchasedSize"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sizeDropdown);
        Thread.sleep(1000);
        Select selectSize = new Select(sizeDropdown);
        selectSize.selectByValue("M");
        String selectedValueSize = selectSize.getFirstSelectedOption().getAttribute("value");

        WebElement genderDropdown = driver.findElement(By.name("gender"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", genderDropdown);
        Thread.sleep(1000);
        Select selectGender = new Select(genderDropdown);
        selectGender.selectByValue("♂ (M)");
        String selectedValueGender = selectGender.getFirstSelectedOption().getAttribute("value");

        WebElement ageDropdown = driver.findElement(By.name("ageGroup"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", ageDropdown);
        Thread.sleep(1000);
        Select selectAge = new Select(ageDropdown);
        selectAge.selectByValue("20 - 24 yo");
        String selectedValueAge = selectAge.getFirstSelectedOption().getAttribute("value");

        WebDriverWait waitLocation = new WebDriverWait(driver, Duration.ofSeconds(100));
        boolean retry = true;
        while (retry) {
            try {
                WebElement selectDropdownLocation = waitLocation.until(
                        ExpectedConditions.elementToBeClickable(By.className("selected-item"))
                );
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selectDropdownLocation);

                // Có thể thay thế Thread.sleep bằng chờ điều kiện JavaScript
                Thread.sleep(2000);

                List<WebElement> showLocations = waitLocation.until(
                        ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("showLocation"))
                );

                if (!showLocations.isEmpty()) {
                    WebElement firstLocation = showLocations.get(0);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstLocation);
                    waitLocation.until(ExpectedConditions.elementToBeClickable(firstLocation)).click();

                    String selectedValueLocation = firstLocation.getText();
//                    String selectedValueLocation = "";
                    System.out.println("Selected province: " + selectedValueLocation);
                    if (selectedValueLocation.isEmpty()) {
                        WebElement ValueLocation = driver.findElement(By.className("custom-select"));
                        String classAttr = ValueLocation.getAttribute("class");
                        if (classAttr.contains("activeValidationInput")) {
                            System.out.println("Validation hiển thị: class activeValidationInput đã được thêm vào Location.");
                        } else {
                            System.out.println("Không add class activeValidationInput vào Location.");
                        }
                        System.out.println("Location Empty !");
                    } else {
                        System.out.println("Location Passed");
                        Thread.sleep(2000);
                    }
                    retry = false;
                }
            } catch (StaleElementReferenceException e) {
                System.out.println("Chờ tí đi ........ ");
            } catch (TimeoutException e) {
                System.out.println("TimeoutException: Không thể tìm thấy phần tử.");
                retry = false;
            } catch (Exception e) {
                System.out.println("Lỗi không xác định: " + e.getMessage());
                retry = false;
            }
        }

        WebElement weightDropdown = driver.findElement(By.name("weight"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", weightDropdown);
        Thread.sleep(1000);


        Select selectWeight = new Select(weightDropdown);
        selectWeight.selectByValue("56 - 60kg");
        String selectedValueWeight = selectWeight.getFirstSelectedOption().getAttribute("value");

        WebElement heightDropdown = driver.findElement(By.name("height"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", heightDropdown);
        Thread.sleep(1000);
        Select selectHeight = new Select(heightDropdown);
        selectHeight.selectByValue("156 - 160cm");
        String selectedValueHeight = selectHeight.getFirstSelectedOption().getAttribute("value");

        WebElement shoeSizeDropdown = driver.findElement(By.name("shoeSize"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", shoeSizeDropdown);
        Thread.sleep(1000);
        Select selectShoeSize = new Select(shoeSizeDropdown);
        selectShoeSize.selectByValue("EU40");
        String selectedValueShoeSize = selectShoeSize.getFirstSelectedOption().getAttribute("value");

        WebElement termsCheckbox = driver.findElement(By.name("terms"));
        if (!termsCheckbox.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", termsCheckbox);
        }


        WebElement review_submit_btn = driver.findElement(By.className("review-submit-btn"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", review_submit_btn);
        //Modal
        try {
            WebDriverWait waits = new WebDriverWait(driver, Duration.ofSeconds(5));
            waits.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-container")));

            WebElement toastMessage = driver.findElement(By.className("modal-container"));
            Thread.sleep(1000);

            if (toastMessage.isDisplayed()) {
                String text = toastMessage.getText();
                System.out.println("Modal hiển thị: " + text);
            } else {
                System.out.println("Modal không còn hiển thị.");
                Assert.fail("Modal không hiển thị trong thời gian dự kiến.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Title
        try {
            if (titleInput.getAttribute("value").isEmpty()) {
                titleInput = driver.findElement(By.name("title"));
                String classAttr = titleInput.getAttribute("class");
                if (classAttr.contains("activeValidationInput")) {
                    System.out.println(" Validation hiển thị: class activeValidationInput đã được thêm vào title .");
                } else {
                    System.out.println(" Không thấy class activeValidationInput vào title.");
                }
                System.out.println("Title Empty !");
            } else {
                System.out.println("Title Passed");
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Comment
        try {
            if (commentInput.getAttribute("value").isEmpty()) {
                commentInput = driver.findElement(By.name("comment"));
                String classAttr = commentInput.getAttribute("class");
                if (classAttr.contains("activeValidationInput")) {
                    System.out.println(" Validation hiển thị: class activeValidationInput đã được thêm vào comment .");
                } else {
                    System.out.println(" Không thấy class activeValidationInput vào comment.");
                }
                System.out.println("omment Empty !");
            } else {
                System.out.println("comment Passed");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Purchased Size
        try {
            if (sizeDropdown.getAttribute("value").isEmpty() || selectedValueSize.isEmpty()) {
                sizeDropdown = driver.findElement(By.name("purchasedSize"));
                String classAttr = sizeDropdown.getAttribute("class");
                if (classAttr.contains("activeValidationInput")) {
                    System.out.println(" Validation hiển thị: class activeValidationInput đã được thêm vào purchasedSize .");
                } else {
                    System.out.println(" Không thấy class activeValidationInput vào purchasedSize.");
                }
                System.out.println("purchasedSize Empty !");
            } else {
                System.out.println("purchasedSize Passed");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Gender
        try {
            if (genderDropdown.getAttribute("value").isEmpty() || selectedValueGender.isEmpty()) {
                genderDropdown = driver.findElement(By.name("gender"));
                String classAttr = genderDropdown.getAttribute("class");
                if (classAttr.contains("activeValidationInput")) {
                    System.out.println(" Validation hiển thị: class activeValidationInput đã được thêm vào Gender .");
                } else {
                    System.out.println(" Không thấy class activeValidationInput vào Gender.");
                }
                System.out.println("Gender Empty !");
            } else {
                System.out.println("Gender Passed");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Age
        try {

            if (ageDropdown.getAttribute("value").isEmpty() || selectedValueAge.isEmpty()) {
                String classAttr = ageDropdown.getAttribute("class");
                if (classAttr.contains("activeValidationInput")) {
                    System.out.println("Validation hiển thị: class activeValidationInput đã được thêm vào Age.");
                } else {
                    System.out.println("Không thấy class activeValidationInput vào Age.");
                }
                System.out.println("Age Empty !");
            } else {
                System.out.println("Age Passed");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Weight
        try {
            if (weightDropdown.getAttribute("value").isEmpty() || selectedValueWeight.isEmpty()) {
                String classAttr = weightDropdown.getAttribute("class");
                if (classAttr.contains("activeValidationInput")) {
                    System.out.println("Validation hiển thị: class activeValidationInput đã được thêm vào Weight.");
                } else {
                    System.out.println("Không thấy class activeValidationInput vào Weight.");
                }
                System.out.println("Weight Empty !");
            } else {
                System.out.println("Weight Passed");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Height
        try {
            if (heightDropdown.getAttribute("value").isEmpty() || selectedValueHeight.isEmpty()) {
                String classAttr = heightDropdown.getAttribute("class");
                if (classAttr.contains("activeValidationInput")) {
                    System.out.println("Validation hiển thị: class activeValidationInput đã được thêm vào Height.");
                } else {
                    System.out.println("Không thấy class activeValidationInput vào Height.");
                }
                System.out.println("Height Empty !");
            } else {
                System.out.println("Height Passed");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Height
        try {
            if (shoeSizeDropdown.getAttribute("value").isEmpty() || selectedValueShoeSize.isEmpty()) {
                String classAttr = shoeSizeDropdown.getAttribute("class");
                if (classAttr.contains("activeValidationInput")) {
                    System.out.println("Validation hiển thị: class activeValidationInput đã được thêm vào Shoe Size.");
                } else {
                    System.out.println("Không thấy class activeValidationInput vào Shoe Size.");
                }
                System.out.println("Shoe Size Empty !");
            } else {
                System.out.println("Shoe Size Passed");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
