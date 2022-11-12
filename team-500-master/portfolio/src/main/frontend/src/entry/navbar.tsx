/**
 * Entrypoint for the JavaScript on the navbar.
 */

import ReactDOM from "react-dom";
import React from "react";
import {NavBar} from "../component/navbar/NavBar";
import {Socket} from "./live_updating";

ReactDOM.render(
    <React.StrictMode>
        <NavBar/>
    </React.StrictMode>,
    document.getElementById("navbar-react-root")
)