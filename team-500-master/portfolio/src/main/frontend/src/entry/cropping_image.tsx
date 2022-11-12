/**
 * Entrypoint for the JavaScript on the Edit/Crop user image page.
 */
import React from "react"
import ReactDOM from "react-dom"
import {CroppingUserImage} from "../page/CroppingImage/CroppingUserImage";
import "./monthly_planner.css"

ReactDOM.render(
    <React.StrictMode>
        <CroppingUserImage/>
    </React.StrictMode>,
    document.getElementById("croppieDivId")
)