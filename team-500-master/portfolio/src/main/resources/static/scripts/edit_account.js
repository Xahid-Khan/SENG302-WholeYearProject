// Size of the largest file that can be uploaded to the server (in bytes).

const MAX_FILE_SIZE = 10 * 1024 * 1024;
// Size of the largest file that can be uploaded to the server and saved **without our compression being applied to it**.

const FILE_COMPRESSION_THRESHOLD = 5 * 1024 * 1024;
// Size of the (square) image element in pixels.


const IMAGE_SIZE = 150;


(() => {
    let blockFormSubmit = false;

    const formElement = document.getElementById("form");
    const submitBtnElement = document.getElementById("submit");
    const inputElement = document.getElementById("newUploadedImage");
    const warningElement = document.getElementById("image-preview-warning");
    const errorElement = document.getElementById("image-preview-error");
    const imagePreviewElement = document.getElementById("image-preview");

    let fetchPreviewController = null;

    formElement.addEventListener('submit', (evt) => {
        if (blockFormSubmit) {
            evt.preventDefault();
            alert("Please choose a valid picture before proceeding.");
        }
    })

    const showWarning = (message) => {
        warningElement.style.display = 'block';
        warningElement.innerText = message;
    }

    const hideWarning = () => {
        warningElement.style.display = 'none';
    }

    const showError = (message) => {
        errorElement.style.display = 'block';
        errorElement.innerText = message;
        showIllegalImage();

        blockFormSubmit = true;
        submitBtnElement.setAttribute("disabled", true);
    }

    const hideErrors = () => {
        errorElement.style.display = 'none';

        blockFormSubmit = false;
        submitBtnElement.removeAttribute("disabled");
    }

    const hideImagePreview = () => {
        imagePreviewElement.style.display = "none"
    }

    const showImagePreview = async (imageData) => {
        const image = await createImageBitmap(imageData, {
            resizeWidth: IMAGE_SIZE,
            resizeHeight: IMAGE_SIZE
        });

        if (fetchPreviewController.signal.aborted) {
            return;
        }

        const ctx = imagePreviewElement.getContext('2d');
        ctx.clearRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        ctx.drawImage(image, 0, 0);
    }

    const showLoadingImage = () => {
        const ctx = imagePreviewElement.getContext('2d');
        ctx.clearRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);

        ctx.fillStyle = "black";
        ctx.font = "bold 14px sans-serif";
        ctx.textAlign = "center";
        ctx.fillText("Loading preview...", IMAGE_SIZE / 2, IMAGE_SIZE / 2);
    }

    const showIllegalImage = () => {
        const ctx = imagePreviewElement.getContext('2d');

        ctx.clearRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);

        ctx.strokeStyle = 'red';
        ctx.lineWidth = 3;

        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(IMAGE_SIZE, IMAGE_SIZE);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(0, IMAGE_SIZE);
        ctx.lineTo(IMAGE_SIZE, 0);
        ctx.stroke();
    }

    const fetchAndDisplayPreview = async () => {
        hideWarning();

        if (!inputElement.files || inputElement.files.length === 0) {
            return;
        }
        else if (inputElement.files[0].size === 0) {
            showError("Please select a file with content.");
        }
        else if (inputElement.files[0].size > MAX_FILE_SIZE) {
            showError("This file is too large. Please select a file of at most 10MB in size.");
        }
        else {
            if (inputElement.files[0].size > FILE_COMPRESSION_THRESHOLD) {
                showWarning("This file will be compressed. To avoid compression please choose a file less than 5MB in size.")
            }

            // Let's try to generate a preview...
            try {
                const formData = new FormData();
                formData.append("image", inputElement.files[0], inputElement.files[0].name);

                fetchPreviewController = new AbortController();

                const res = await fetch("edit_account/preview_picture", {
                    method: 'POST',
                    body: formData,
                    signal: fetchPreviewController.signal
                });

                if (fetchPreviewController.signal.aborted) {
                    return;
                }

                if (!res.ok) {
                    if (res.status === 400) {
                        try {
                            showError(await res.text());
                        }
                        catch (e) {
                            showError(`An unexpected error occurred while generating a preview.`);
                            console.error(e);
                        }
                    }
                    else {
                        showError(`An unexpected error occurred while generating a preview.`);
                        console.warn(res);
                    }
                }
                else {
                    // We're OK!
                    const data = await res.blob();

                    if (fetchPreviewController.signal.aborted) {
                        return;
                    }

                    try {
                        await showImagePreview(data);
                        hideErrors();
                    }
                    catch (e) {
                        hideImagePreview();
                        showError(`An unexpected error occurred while generating a preview.`);
                        console.error(e);
                    }
                }
            }
            catch (e) {
                if (e instanceof DOMException && e.name === 'AbortError') {
                }
                else {
                    hideImagePreview();
                    showError(`An unexpected error occurred while generating a preview.`)
                    console.error(e);
                }
            }
        }
    }

    const cancelCurrentPreviewFetchIfNeccessary = () => {
        if (fetchPreviewController !== null) {
            fetchPreviewController.abort();
        }
    }

    inputElement.addEventListener('change', () => {
        // File selected
        imagePreviewElement.style.display = "inline-block";

        cancelCurrentPreviewFetchIfNeccessary();
        showLoadingImage();
        fetchAndDisplayPreview();
    })
})();