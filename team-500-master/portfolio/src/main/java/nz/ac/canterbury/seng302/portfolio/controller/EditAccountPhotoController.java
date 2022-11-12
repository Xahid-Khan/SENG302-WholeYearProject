package nz.ac.canterbury.seng302.portfolio.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import nz.ac.canterbury.seng302.portfolio.DTO.User;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.contract.BaseImageContract;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.PhotoCropService;
import nz.ac.canterbury.seng302.portfolio.service.RegisterClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

/** This controller handles all editing photo interactions. */
@Controller
public class EditAccountPhotoController extends AuthenticatedController {
  private static final int MIN_PROFILE_PICTURE_SIZE = 5 * 1024;
  private static final int PROFILE_PICTURE_COMPRESSION_THRESHOLD = 5 * 1024 * 1024;
  private static final int MAX_PROFILE_PICTURE_SIZE = 5 * 1024 * 1024;

  @Autowired private PhotoCropService photoCropService;

  @Autowired private RegisterClientService registerClientService;

  @Autowired
  public EditAccountPhotoController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * This method removes the left and right trailing white-spaces from the form data.
   *
   * @param binder a data binder from web-request
   */
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
  }

  /**
   * Gets the editing user image page.
   *
   * @return the editing user image page
   */
  @GetMapping(value = "/edit_user_image")
  public String getPage() {
    return "edit_user_image";
  }

  /**
   * Handles editing an image.
   *
   * @param principal the user's token
   * @param newImage a BaseImageContract of the new image
   * @return a HTTP status with the result
   */
  @PostMapping(value = "/edit_Image")
  public ResponseEntity<?> changeUserProfilePhoto(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestBody BaseImageContract newImage) {
    try {
      if (newImage.croppedImage().length() != 0) {
        int userId = getUserId(principal);
        String fileType = newImage.croppedImage().substring(5, 14);
        byte[] imageData = Base64.getDecoder().decode(newImage.croppedImage().substring(22));
        if (imageData.length < MAX_PROFILE_PICTURE_SIZE) {
          registerClientService.uploadUserPhoto(userId, fileType, imageData);
        } else {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    return ResponseEntity.ok().build();
  }

  /**
   * Apply the cropping algorithm to the uploaded image and return the cropped image for previewing
   * to the user.
   *
   * @param file to generate preview for
   * @return cropped preview of the given image file
   */
  @PostMapping("/edit_account/preview_picture")
  public ResponseEntity<byte[]> generateCroppedPreview(
      @RequestParam(value = "image") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("No image found.".getBytes(StandardCharsets.UTF_8));
    }

    if (MIN_PROFILE_PICTURE_SIZE > file.getSize() || file.getSize() > MAX_PROFILE_PICTURE_SIZE) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              String.format(
                      "Image is too small or too large. "
                          + "Please provide an image of size between %d and %d bytes.",
                      MIN_PROFILE_PICTURE_SIZE, MAX_PROFILE_PICTURE_SIZE)
                  .getBytes(StandardCharsets.UTF_8));
    }

    try {
      byte[] uploadImage =
          photoCropService.processImageFile(
              file, file.getSize() > PROFILE_PICTURE_COMPRESSION_THRESHOLD);
      return ResponseEntity.ok(uploadImage);
    } catch (UnsupportedMediaTypeStatusException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("This file format is not supported.".getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Failed to read/write image.".getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * A controller (endpoint) for deleting a user photo.
   *
   * @param principal An Authority State to verify user.
   * @return a String to redirect the page to.
   */
  @PostMapping(value = "/edit_account/imageDelete")
  public String deleteUserPhoto(
      @AuthenticationPrincipal PortfolioPrincipal principal) {
    int userId = getUserId(principal);
    registerClientService.deleteUserPhoto(userId);
    return "redirect:/my_account";
  }
}
