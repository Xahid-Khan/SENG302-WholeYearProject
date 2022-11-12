/**
 * Entrypoint for the JavaScript on the monthly_planner page.
 */
import React from "react"
import ReactDOM from "react-dom"
import {MonthlyPlannerPage} from "../page/monthly_planner/MonthlyPlannerPage";
import "./monthly_planner.css"

ReactDOM.render(
    <React.StrictMode>
        <MonthlyPlannerPage/>
    </React.StrictMode>,
    document.getElementById("react-root")
)