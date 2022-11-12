package nz.ac.canterbury.seng302.identityprovider.controller;

import nz.ac.canterbury.seng302.identityprovider.database.PhotoModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserPhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class UserImageController {
    @Autowired
    private UserPhotoRepository photoRepository;

    /**
     * This end-point will check if the user photo exist and return it.
     * @param userId Integer
     * @param response Http servlet Response
     * @throws IOException Throws an Input/Output exception
     */
    @GetMapping(value = "/userImage/{id}")
    void showUserImage(@PathVariable("id") int userId, HttpServletResponse response, Model model) throws IOException {
        response.setContentType("image/*");
        try (ServletOutputStream imageStream = response.getOutputStream()) {
            PhotoModel userPhoto = photoRepository.findById(userId).orElse(null);
            if (userPhoto != null) {
                model.addAttribute("userPhoto", userPhoto);
                imageStream.write(userPhoto.getUserPhoto());
            }
        }
    }

}
