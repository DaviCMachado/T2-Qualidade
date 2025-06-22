package tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import io.github.bonigarcia.wdm.WebDriverManager;

import static org.junit.jupiter.api.Assertions.*;

public class UniHannoverWebsiteTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "https://www.uni-hannover.de/";

    @BeforeAll
    static void setupWebDriverManager() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);

        try {
            WebElement acceptCookiesButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("cookie-layer-accept-all")));
            acceptCookiesButton.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("cookie-layer-accept-all")));
        } catch (Exception e) {
            System.out.println("Não encontrou banner de cookies ou não conseguiu interagir com ele.");
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testKontaktLinkNavigation() {
        String expectedKontaktUrl = "https://www.uni-hannover.de/de/kontakt";

        WebElement kontaktLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(@href, '/kontakt') and contains(., 'Kontakt')]")));

        assertTrue(kontaktLink.isDisplayed(), "O link 'Kontakt' deve estar visível.");
        assertTrue(kontaktLink.isEnabled(), "O link 'Kontakt' deve estar clicável.");

        kontaktLink.click();

        wait.until(ExpectedConditions.urlToBe(expectedKontaktUrl));
        assertEquals(expectedKontaktUrl, driver.getCurrentUrl(), "A URL deve ser a página de contato em alemão.");
    }

    // 2 - Link English
    @Test
    void englishButton() {
        String expectedEnglishUrl = "https://www.uni-hannover.de/en/";

        WebElement englishButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/en/'][2]")));

        // Asserção: Verifica se a hiperligação foi encontrado e está visível/clicável
        assertTrue(englishButton.isDisplayed(), "O link 'English' deve estar visível.");
        assertTrue(englishButton.isEnabled(), "O link 'English' deve estar clicável.");

        englishButton.click();

        wait.until(ExpectedConditions.urlToBe(expectedEnglishUrl));
        assertEquals(expectedEnglishUrl, driver.getCurrentUrl(), "A URL deve ser a página em ingles.");
    }

    @Test
    void testVisibleLogo() {
        WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt = 'Logo Leibniz Universität Hannover']")));

        assertTrue(logo.isDisplayed());
        assertTrue(logo.isEnabled());

        logo.click();
        wait.until(ExpectedConditions.urlToBe("https://www.uni-hannover.de/de/"));
        assertEquals("https://www.uni-hannover.de/de/", driver.getCurrentUrl());
    }

    @Test
    void testSearchInputVisibilityAndTyping() {

        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder = 'Suche']")));

        assertNotNull(searchInput, "O campo de busca deve estar presente.");
        assertTrue(searchInput.isDisplayed(), "O campo de busca deve estar visivel.");
        assertTrue(searchInput.isEnabled(), "O campo de busca deve estar habilitado.");

        String testText = "Testando";
        searchInput.sendKeys(testText);

        assertEquals(testText, searchInput.getAttribute("value"), "O campo de texto deve ser igual.");
    }

    @Test
    void testSearchButton() {
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@aria-label = 'Suche starten']")));
        // 2. Verify presence and visibility
        assertNotNull(searchButton, "O botao de busca deve estar presente.");
        assertTrue(searchButton.isDisplayed(), "O botao de busca deve estar visivel.");
        assertTrue(searchButton.isEnabled(), "O botao de busca deve estar habilitado.");

        searchButton.click();

        wait.until(ExpectedConditions.titleContains("Suche"));
    }

    @Test
    void testFakultatenDropdown() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));

        By fakultatenButtonSelector = By.xpath("//button[contains(@class, 'c-topbar__link') and contains(@class, 'c-topbar__dropdown-icon') and span[text()='Fakultäten']]");

        WebElement fakultatenButton = longWait.until(ExpectedConditions.elementToBeClickable(fakultatenButtonSelector));

        By dropdownMenuSelector = By.xpath("//div[contains(@class, 'c-dropdown') and contains(@class, 'c-dropdown--is-active')]");

        assertNotNull(fakultatenButton, "O botão 'Fakultäten' deve estar presente.");
        assertTrue(fakultatenButton.isDisplayed(), "O botão deve estar visível.");
        assertTrue(fakultatenButton.isEnabled(), "O botão deve estar habilitado.");

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(dropdownMenuSelector));
            System.out.println("O menu dropdown não está visível antes do clique (como esperado).");
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("Atenção: O menu dropdown já estava visível antes do clique, pode não ser o estado inicial esperado.");
        }

        fakultatenButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownMenuSelector));
    }


    @Test
    void testScrollToTopButtonFunctionality() {

        By scrollToTopButtonSelector = By.xpath("//div[@class = 'c-button c-button--to-top u-icon--arrow-up js-scroll-to-top']");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        System.out.println("Página rolada para o final.");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        WebElement scrollToTopButton = wait.until(ExpectedConditions.elementToBeClickable(scrollToTopButtonSelector));
        assertNotNull(scrollToTopButton, "O botão 'Voltar ao Topo' deve estar presente.");
        assertTrue(scrollToTopButton.isDisplayed(), "O botão 'Voltar ao Topo' deve estar visível.");
        assertTrue(scrollToTopButton.isEnabled(), "O botão 'Voltar ao Topo' deve estar clicável.");

        scrollToTopButton.click();
        System.out.println("Clicou no botão 'Voltar ao Topo'.");

        WebDriverWait scrollWait = new WebDriverWait(driver, Duration.ofSeconds(5));

        scrollWait.until(d -> {
            Number scrollYNumber = (Number) js.executeScript("return window.scrollY;");
            Long scrollY = scrollYNumber.longValue();
            return scrollY == 0; // Condição: espera até que a rolagem vertical seja 0
        });

        Number finalScrollYNumber = (Number) js.executeScript("return window.scrollY;");
        Long finalScrollY = finalScrollYNumber.longValue();
        assertEquals(0L, finalScrollY, "A página deve ter rolado de volta para o topo (scrollY = 0).");
        System.out.println("Página verificada: voltou ao topo.");
    }


    @Test
    void testStudIPShortcutMenuItem() {
        List<WebElement> spans = driver.findElements(By.cssSelector("span.c-shortcut-menu__item--inner"));
        WebElement targetSpan = null;
        for (WebElement span : spans) {
            if (span.getText().trim().equals("Stud.IP")) {
                targetSpan = span;
                break;
            }
        }
        assertNotNull(targetSpan, "Elemento com texto 'Stud.IP' deve existir.");
        assertTrue(targetSpan.isDisplayed(), "Elemento Stud.IP deve estar visível.");

        // Se for um link, verificar href
        WebElement parentLink = targetSpan.findElement(By.xpath("./ancestor::a"));
        assertNotNull(parentLink, "Elemento 'Stud.IP' deve estar dentro de um link.");
        assertTrue(parentLink.isEnabled(), "Link deve ser clicável.");
        String href = parentLink.getAttribute("href");
        assertTrue(href != null && href.toLowerCase().contains("studip"), "Link deve conter 'studip' no href.");
    }

    @Test
    void testInternationaleTagungenButton() {
        List<WebElement> buttons = driver.findElements(By.cssSelector("a.c-button[href='/de/universitaet/aktuelles/veranstaltungen/internationale-tagungen']"));
        WebElement targetButton = null;
        for (WebElement button : buttons) {
            if (button.getText().trim().equals("Alle internationalen Tagungen anzeigen")) {
                targetButton = button;
                break;
            }
        }
        assertNotNull(targetButton, "Botão 'Alle internationalen Tagungen anzeigen' deve existir.");
        assertTrue(targetButton.isDisplayed(), "Botão deve estar visível.");
        assertTrue(targetButton.isEnabled(), "Botão deve estar habilitado.");
        assertEquals(BASE_URL + "de/universitaet/aktuelles/veranstaltungen/internationale-tagungen", targetButton.getAttribute("href"), "Href deve estar correto.");
    }

    @Test
    void testFooterLinkLeibnizUni() {
        List<WebElement> links = driver.findElements(By.cssSelector("a.o-footer__link[href='/de/copyright']"));
        WebElement targetLink = null;
        for (WebElement link : links) {
            if (link.getText().trim().contains("Leibniz Universität Hannover")) {
                targetLink = link;
                break;
            }
        }
        assertNotNull(targetLink, "Link do rodapé deve existir.");
        assertTrue(targetLink.isDisplayed(), "Link deve estar visível.");
        assertTrue(targetLink.isEnabled(), "Link deve estar habilitado.");
        assertEquals(BASE_URL + "de/copyright", targetLink.getAttribute("href"), "Href deve estar correto.");
    }

    @Test
    void testSectionElementsByClass() {
        List<WebElement> els = driver.findElements(By.className("c-section__element"));
        assertFalse(els.isEmpty(), "Devem existir elementos de seção.");
        boolean anyVisible = els.stream().anyMatch(WebElement::isDisplayed);
        assertTrue(anyVisible, "Pelo menos um elemento de seção deve estar visível.");

        // Opcional: verificar se algum tem título h2
        boolean hasHeadline = els.stream().anyMatch(el -> !el.findElements(By.tagName("h2")).isEmpty());
        assertTrue(hasHeadline, "Pelo menos um elemento de seção deve conter título h2.");
    }

    @Test
    void testSectionHeadlinesByClass() {
        List<WebElement> els = driver.findElements(By.className("c-headline--h2"));
        assertFalse(els.isEmpty(), "Devem existir títulos de seção.");
        boolean anyVisible = els.stream().anyMatch(WebElement::isDisplayed);
        assertTrue(anyVisible, "Pelo menos um título deve estar visível.");

        for (WebElement el : els) {
            assertFalse(el.getText().trim().isEmpty(), "Título não deve estar vazio.");
        }
    }

    @Test
    void testGridColumnsByClass() {
        List<WebElement> els = driver.findElements(By.className("c-section-grid__column"));
        assertFalse(els.isEmpty(), "Devem existir colunas de grid.");
        boolean anyVisible = els.stream().anyMatch(WebElement::isDisplayed);
        assertTrue(anyVisible, "Pelo menos uma coluna deve estar visível.");

        boolean hasContent = els.stream().anyMatch(el -> !el.findElements(By.tagName("p")).isEmpty() || !el.findElements(By.tagName("h2")).isEmpty());
        assertTrue(hasContent, "Pelo menos uma coluna deve conter conteúdo de texto.");
    }

    @Test
    void testExternalFacebookLink() {
        WebElement fbLink = driver.findElement(By.cssSelector("a[href*='facebook.com']"));
        assertTrue(fbLink.isDisplayed(), "Link para Facebook deve existir.");
        assertTrue(fbLink.isEnabled(), "Link para Facebook deve estar habilitado.");
        assertTrue(fbLink.getAttribute("href").contains("facebook.com"), "Link deve conter facebook.com");

        String target = fbLink.getAttribute("target");
        assertEquals("_blank", target, "Link do Facebook deve abrir em nova aba.");
    }


    @Test
    void testHomeButton() {

        By elemento = By.xpath("//div[@class = 'c-group__icon u-icon u-icon--users4']");

        By botaoHome = By.xpath("//span[@class = 'c-breadcrumb-list__wrap u-icon u-icon--home2']");

        WebElement elemento1 = driver.findElement(elemento);

        elemento1.click();

        WebElement elementoHome = driver.findElement(botaoHome);
        assertTrue(elementoHome.isDisplayed());
        assertTrue(elementoHome.isEnabled());

        elementoHome.click();

        String homeUrl = "https://www.uni-hannover.de/de/";

        wait.until(ExpectedConditions.urlToBe(homeUrl));
        assertEquals(homeUrl, driver.getCurrentUrl(), "A URL deve ser a da home.");
    }
}