package nz.ac.canterbury.seng302.portfolio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Central location where custom application.properties parameters are stored.
 * @ModelAttribute decorated methods are provided to all Thymeleaf templates.
 */
@ControllerAdvice
public class PortfolioProperties {
    @Value("${nz.ac.canterbury.seng302.portfolio.urlPathPrefix}")
    private String urlPathPrefix;

    @Value("${nz.ac.canterbury.seng302.portfolio.imageUrlPath}")
    private String imageUrlPath;


    /**
     * Provides access to the <code>nz.ac.canterbury.seng302.portfolio.urlPathPrefix</code> property specified
     * in application.properties.
     *
     * @return String url path with a starting / and no trailing /. E.g. <code>/test/portfolio</code> or
     *         the empty string for no prefix.
     */
    @ModelAttribute("globalUrlPathPrefix")
    public String getUrlPathPrefix() {
        return urlPathPrefix;
    }

    /**
     * Provides access to the url property for getting the image specified in application.properties files.
     * @return a String URL path ending with /
     */
    @ModelAttribute("globalImagePath")
    public String getImagePath() {
        return imageUrlPath;
    }
}
