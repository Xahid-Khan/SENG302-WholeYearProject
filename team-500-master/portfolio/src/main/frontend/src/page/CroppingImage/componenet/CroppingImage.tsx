import Croppie, {CroppieOptions} from "croppie";
import React, {useEffect} from "react";
import "croppie/croppie.css";

const stockPhoto = "https://humanimals.co.nz/wp-content/uploads/2019/11/blank-profile-picture-973460_640.png";

const croppieOptions : CroppieOptions = {
    showZoomer: true,
    enableOrientation: true,
    mouseWheelZoom: "ctrl",
    viewport: {
        width: 250,
        height: 250,
        type: "square"
    },
    boundary: {
        width: 300,
        height: 300
    },
};

const croppieElement = document.getElementById("croppie");
let croppie = new Croppie(croppieElement, croppieOptions);

const CroppingImage = () => {

    const [croppedImage, setCroppedImage] = React.useState("");
    const [isFileUploaded, setIsFileUploaded] = React.useState(false);
    const [enableSaveImage, setEnableSaveImage] = React.useState(true);

    let file: any = React.createRef();

    useEffect(() => {}, [croppedImage, isFileUploaded, enableSaveImage])

    const onFileUpload = async () => {
        setIsFileUploaded( true);
        processFile();
    }

    const processFile = () => {
        const reader = new FileReader();
        const image = file.current.files[0];

        if (image && checkFileTypeAndSize(image)) {
            reader.readAsDataURL(image);
            reader.onload = () => {
                croppie.bind({url: reader.result.toString()});
            };
            reader.onerror = function (error) {
                document.getElementById("image-preview-error").innerHTML = "Error: " + error;
            };
        }
    };

    const onResult = async () => {
        croppie.result({type:"base64"}).then(base64 => {
            setCroppedImage(base64);
            document.getElementById("userProfileImage").setAttribute("src", base64);
        });
        setEnableSaveImage(false);
        document.getElementById('previewText').setAttribute("hidden", "true");
        document.getElementById("userProfileImage").removeAttribute('hidden');

    };

    const sendImageData = async () => {
        if (croppedImage == "") {
            return;
        }
        await fetch(`edit_Image`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({"croppedImage": croppedImage})
        }).then((response) => {
            if (response.ok === true) {
                window.location.href='my_account'
            } else {
                document.getElementById("image-preview-error").removeAttribute("hidden");
                document.getElementById("image-preview-error").innerHTML = "Something went wrong, Try Again Later";
            }
        });
    };


    const checkFileTypeAndSize = (file : any) : boolean => {
        const errorElement = document.getElementById("image-preview-error");
        const warningElement = document.getElementById("image-preview-warning")

        if (!(file.type == "image/jpeg" || file.type == "image/png" || file.type == "image/gif")) {
            errorElement.innerHTML = "Invalid File Type - Only JPEG, PNG, and GIF are allowed.";
            document.getElementById("image-preview-error").removeAttribute("hidden");
            document.getElementById('cropImageButton').setAttribute('disabled', "true");
            warningElement.setAttribute("hidden", "true");
            return false
        }

        if (file.size < 5 * 1024 || file.size > 10 * 1024 * 1024) {
            errorElement.innerHTML = "Image size must be greater than 5KB and lower than 10MB";
            warningElement.setAttribute("hidden", "true");
            document.getElementById("image-preview-error").removeAttribute("hidden");
            document.getElementById('cropImageButton').setAttribute('disabled', "true");
            return false
        }

        if (file.size > 5 * 1024 * 1024) {
            errorElement.setAttribute("hidden", "true");
            warningElement.removeAttribute("hidden")
            warningElement.innerHTML = "Your file is greater than 5MB - please use CROP button to compress file.";
        } else {
            warningElement.setAttribute("hidden", "true");
        }

        errorElement.setAttribute("hidden", "true");
        return true;
    }

    return (
        <div id={"specialButtons"} style={{display:'block'}}>
            <input
                type="file"
                id="newUploadedImage"
                ref={file}
                onChange={onFileUpload}
                name={"image"}
                accept="image/jpeg,image/png,image/gif" hidden
            />

            <label htmlFor="newUploadedImage">
                <span className="image-upload-button">Choose profile picture</span>
            </label>

            <button
                type="button"
                id={"cropImageButton"}
                disabled={!isFileUploaded}
                onClick={onResult}
                style={{float:"right", marginTop:"-30px"}}
            >
                Crop Image
            </button>
            <div style={{display: "flex", marginTop:"10px", justifyContent: "right"}}>
                <button onClick={() => sendImageData()} type="button" disabled={enableSaveImage} style={{margin:0}}>
                    Save Photo
                </button>
                <button type="button" onClick={() => window.location.href='my_account'}>
                    Cancel
                </button>
            </div>
        </div>
    );
};

export {
    CroppingImage
}