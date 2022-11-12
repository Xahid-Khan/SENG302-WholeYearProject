/**
 * Entrypoint for the JavaScript on the groups page.
 */
import React from "react"
import ReactDOM from "react-dom"
import "./monthly_planner.css"
import {GroupPage} from "../page/group_page/GroupPage";

ReactDOM.render(
    <React.StrictMode>

        <GroupPage/>
    </React.StrictMode>,
    document.getElementById("react-root")
)