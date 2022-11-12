import React from "react";
import {ToasterRoot} from "../../component/toast/ToasterRoot";
import {ShowHomeFeed} from "./show_home_feed";

export function HomeFeedPage() {

    // const isStudent = localStorage.getItem("isStudent") === "true"

    return (
        <ToasterRoot>
            <div className={"raised-card groups-feed"}>
                <ShowHomeFeed/>
            </div>
        </ToasterRoot>

    )
}