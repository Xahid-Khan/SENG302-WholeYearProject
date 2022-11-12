'use strict'
//let openPopupButton = document.getElementById("userPhotoButton");
let popupForm = document.getElementById("popupForm");
let newImageInput = document.getElementById("newUploadedImage");
let errorDiv = document.getElementById("errorMessageImage");

//openPopupButton.addEventListener('submit', openPopupForm);

function openPopupForm() {
    popupForm.classList.add("open-popupForm");
}

function closePopupForm() {
    if (newImageInput.value.size > 5242000) {
        errorDiv.value = "Image size must be less than 5MB.";
    }
    popupForm.classList.remove("open-popupForm");

}

function cancelPopupForm() {
    newImageInput.value = null;
    popupForm.classList.remove("open-popupForm");
}